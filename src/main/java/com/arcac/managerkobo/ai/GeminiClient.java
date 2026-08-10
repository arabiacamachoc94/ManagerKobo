package com.arcac.managerkobo.ai;

import com.arcac.managerkobo.ui.util.AppPreferences;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/** Cliente REST mínimo para generación de texto con Gemini. */
public class GeminiClient {
    private static final String DEFAULT_MODEL = "gemini-3.6-flash";
    private static final String API_ROOT =
            "https://generativelanguage.googleapis.com/v1beta/models/";

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(12))
            .build();
    private final String explicitApiKey;
    private final String model;

    public GeminiClient() {
        this(null,
                System.getenv().getOrDefault("GEMINI_MODEL", DEFAULT_MODEL));
    }

    GeminiClient(String apiKey, String model) {
        this.explicitApiKey = apiKey == null ? null : apiKey.strip();
        this.model = validModel(model) ? model : DEFAULT_MODEL;
    }

    public boolean isConfigured() {
        return !currentApiKey().isBlank();
    }

    public String generate(String prompt) {
        if (!isConfigured()) {
            throw new AiException(
                    text("Falta configurar la clave API de Gemini.",
                            "The Gemini API key has not been configured."));
        }
        if (prompt == null || prompt.isBlank()) {
            throw new AiException(text("No hay contenido para enviar a Gemini.",
                    "There is no content to send to Gemini."));
        }

        String requestJson = """
                {"contents":[{"role":"user","parts":[{"text":"%s"}]}],
                 "generationConfig":{"temperature":0.2,"maxOutputTokens":4096}}
                """.formatted(escapeJson(prompt));
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_ROOT + model + ":generateContent"))
                .timeout(Duration.ofSeconds(60))
                .header("Content-Type", "application/json")
                .header("x-goog-api-key", currentApiKey())
                .POST(HttpRequest.BodyPublishers.ofString(requestJson))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(
                    request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new AiException(apiErrorMessage(response.statusCode()));
            }
            List<String> parts = jsonStrings(response.body(), "text");
            if (parts.isEmpty()) {
                throw new AiException(
                        text("Gemini no devolvió una respuesta de texto.",
                                "Gemini did not return a text response."));
            }
            String result = String.join("\n", parts).strip();
            String finishReason = firstJsonString(
                    response.body(), "finishReason");
            return addFinishWarning(result, finishReason);
        } catch (AiException exception) {
            throw exception;
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new AiException(text("La petición a Gemini fue interrumpida.",
                            "The request to Gemini was interrupted."),
                    exception);
        } catch (Exception exception) {
            throw new AiException(
                    text("No se pudo conectar con Gemini: ",
                            "Could not connect to Gemini: ")
                            + rootMessage(exception), exception);
        }
    }

    private boolean validModel(String value) {
        return value != null && value.matches("[A-Za-z0-9._-]+");
    }

    private String apiErrorMessage(int statusCode) {
        return switch (statusCode) {
            case 400 -> text("Gemini no pudo procesar la solicitud.",
                    "Gemini could not process the request.");
            case 401, 403 -> text(
                    "La clave API de Gemini no es válida o no tiene permiso.",
                    "The Gemini API key is invalid or does not have permission.");
            case 404 -> text("El modelo de Gemini configurado no está disponible.",
                    "The configured Gemini model is unavailable.");
            case 429 -> text(
                    "Se ha alcanzado el límite de uso de Gemini. Puede ser la cuota "
                            + "del plan gratuito o un límite temporal; inténtalo de nuevo más tarde.",
                    "The Gemini usage limit has been reached. This may be the free-tier "
                            + "quota or a temporary limit; try again later.");
            default -> statusCode >= 500
                    ? text("Gemini no está disponible temporalmente. Inténtalo más tarde.",
                            "Gemini is temporarily unavailable. Try again later.")
                    : text("Gemini devolvió un error (código ",
                            "Gemini returned an error (code ") + statusCode + ").";
        };
    }

    private String addFinishWarning(String result, String finishReason) {
        if (finishReason == null || finishReason.isBlank()
                || "STOP".equalsIgnoreCase(finishReason)) {
            return result;
        }
        if ("MAX_TOKENS".equalsIgnoreCase(finishReason)) {
            return result + text(
                    "\n\n⚠ RESUMEN INCOMPLETO\nGemini alcanzó el límite de "
                            + "respuesta. Reduce la selección o procesa el libro en varios bloques.",
                    "\n\n⚠ INCOMPLETE SUMMARY\nGemini reached the response limit. "
                            + "Reduce the selection or process the book in several batches.");
        }
        return result + text(
                "\n\n⚠ Gemini finalizó la respuesta antes de tiempo ",
                "\n\n⚠ Gemini ended the response early ")
                + "(" + finishReason + ").";
    }

    private String text(String spanish, String english) {
        return AppPreferences.isEnglish() ? english : spanish;
    }

    private String currentApiKey() {
        return explicitApiKey == null ? GeminiApiKeyStore.get() : explicitApiKey;
    }

    private String escapeJson(String value) {
        StringBuilder escaped = new StringBuilder(value.length() + 32);
        for (int index = 0; index < value.length(); index++) {
            char character = value.charAt(index);
            switch (character) {
                case '"' -> escaped.append("\\\"");
                case '\\' -> escaped.append("\\\\");
                case '\b' -> escaped.append("\\b");
                case '\f' -> escaped.append("\\f");
                case '\n' -> escaped.append("\\n");
                case '\r' -> escaped.append("\\r");
                case '\t' -> escaped.append("\\t");
                default -> {
                    if (character < 0x20) {
                        escaped.append(String.format("\\u%04x", (int) character));
                    } else {
                        escaped.append(character);
                    }
                }
            }
        }
        return escaped.toString();
    }

    private String firstJsonString(String json, String property) {
        List<String> values = jsonStrings(json, property);
        return values.isEmpty() ? null : values.get(0);
    }

    private List<String> jsonStrings(String json, String property) {
        List<String> values = new ArrayList<>();
        if (json == null) return values;
        String marker = "\"" + property + "\"";
        int position = 0;
        while ((position = json.indexOf(marker, position)) >= 0) {
            int colon = json.indexOf(':', position + marker.length());
            if (colon < 0) break;
            int quote = skipWhitespaceToQuote(json, colon + 1);
            if (quote < 0) {
                position = colon + 1;
                continue;
            }
            ParsedString parsed = parseJsonString(json, quote + 1);
            if (parsed != null) {
                values.add(parsed.value());
                position = parsed.nextIndex();
            } else {
                position = quote + 1;
            }
        }
        return values;
    }

    private int skipWhitespaceToQuote(String json, int start) {
        int index = start;
        while (index < json.length() && Character.isWhitespace(json.charAt(index))) {
            index++;
        }
        return index < json.length() && json.charAt(index) == '"' ? index : -1;
    }

    private ParsedString parseJsonString(String json, int start) {
        StringBuilder value = new StringBuilder();
        for (int index = start; index < json.length(); index++) {
            char character = json.charAt(index);
            if (character == '"') return new ParsedString(value.toString(), index + 1);
            if (character != '\\') {
                value.append(character);
                continue;
            }
            if (++index >= json.length()) return null;
            char escaped = json.charAt(index);
            switch (escaped) {
                case '"', '\\', '/' -> value.append(escaped);
                case 'b' -> value.append('\b');
                case 'f' -> value.append('\f');
                case 'n' -> value.append('\n');
                case 'r' -> value.append('\r');
                case 't' -> value.append('\t');
                case 'u' -> {
                    if (index + 4 >= json.length()) return null;
                    try {
                        value.append((char) Integer.parseInt(
                                json.substring(index + 1, index + 5), 16));
                        index += 4;
                    } catch (NumberFormatException exception) {
                        return null;
                    }
                }
                default -> value.append(escaped);
            }
        }
        return null;
    }

    private String rootMessage(Throwable throwable) {
        Throwable current = throwable;
        while (current.getCause() != null) current = current.getCause();
        return current.getMessage() == null
                ? current.getClass().getSimpleName() : current.getMessage();
    }

    private record ParsedString(String value, int nextIndex) { }

    public static class AiException extends RuntimeException {
        public AiException(String message) { super(message); }
        public AiException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
