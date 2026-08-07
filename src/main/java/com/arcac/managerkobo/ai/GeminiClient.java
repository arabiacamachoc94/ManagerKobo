package com.arcac.managerkobo.ai;

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
                    "Falta configurar la clave API de Gemini.");
        }
        if (prompt == null || prompt.isBlank()) {
            throw new AiException("No hay contenido para enviar a Gemini.");
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
                        "Gemini no devolvió una respuesta de texto.");
            }
            String result = String.join("\n", parts).strip();
            String finishReason = firstJsonString(
                    response.body(), "finishReason");
            return addFinishWarning(result, finishReason);
        } catch (AiException exception) {
            throw exception;
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new AiException("La petición a Gemini fue interrumpida.",
                    exception);
        } catch (Exception exception) {
            throw new AiException(
                    "No se pudo conectar con Gemini: "
                            + rootMessage(exception), exception);
        }
    }

    private boolean validModel(String value) {
        return value != null && value.matches("[A-Za-z0-9._-]+");
    }

    private String apiErrorMessage(int statusCode) {
        return switch (statusCode) {
            case 400 -> "Gemini no pudo procesar la solicitud.";
            case 401, 403 -> "La clave API de Gemini no es válida o no tiene permiso.";
            case 404 -> "El modelo de Gemini configurado no está disponible.";
            case 429 -> "Se ha alcanzado el límite de uso de Gemini. "
                    + "Puede ser la cuota del plan gratuito o un límite temporal; "
                    + "inténtalo de nuevo más tarde.";
            default -> statusCode >= 500
                    ? "Gemini no está disponible temporalmente. Inténtalo más tarde."
                    : "Gemini devolvió un error (código " + statusCode + ").";
        };
    }

    private String addFinishWarning(String result, String finishReason) {
        if (finishReason == null || finishReason.isBlank()
                || "STOP".equalsIgnoreCase(finishReason)) {
            return result;
        }
        if ("MAX_TOKENS".equalsIgnoreCase(finishReason)) {
            return result + "\n\n⚠ RESUMEN INCOMPLETO\n"
                    + "Gemini alcanzó el límite de respuesta. Reduce la "
                    + "selección o procesa el libro en varios bloques.";
        }
        return result + "\n\n⚠ Gemini finalizó la respuesta antes de tiempo "
                + "(" + finishReason + ").";
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

    /** Extrae cadenas JSON sin añadir una dependencia solo para esta prueba. */
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
