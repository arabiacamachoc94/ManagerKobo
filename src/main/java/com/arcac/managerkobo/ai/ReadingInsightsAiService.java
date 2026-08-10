package com.arcac.managerkobo.ai;

import com.arcac.managerkobo.model.Book;
import com.arcac.managerkobo.service.ReadingStatistics;
import com.arcac.managerkobo.ui.util.AppPreferences;
import java.util.stream.Collectors;

/** Genera una interpretación breve a partir de estadísticas agregadas. */
public class ReadingInsightsAiService {
    private final GeminiClient client;

    public ReadingInsightsAiService() {
        this(new GeminiClient());
    }

    ReadingInsightsAiService(GeminiClient client) {
        this.client = client;
    }

    public boolean isConfigured() {
        return client.isConfigured();
    }

    public String analyze(ReadingStatistics statistics) {
        return cleanAnalysis(client.generate(buildPrompt(statistics)));
    }

    private String buildPrompt(ReadingStatistics statistics) {
        String data = """
                total=%d; terminados=%d; en_curso=%d; sin_empezar=%d
                finalizacion=%.1f%%; progreso_medio=%.1f%%; horas_totales=%.1f
                horas_medias_por_terminado=%.1f; subrayados=%d; con_nota=%d
                libro_mas_subrayado=%s (%d); lecturas_actuales=%s
                terminados_por_mes=%s; terminados_este_ano=%d
                ritmo_mensual=%.1f; proyeccion_anual=%d; ritmo_lector=%s
                libro_mas_rapido=%s; libro_mas_lento=%s
                """.formatted(
                statistics.totalBooks(), statistics.finishedBooks(),
                statistics.readingBooks(), statistics.unreadBooks(),
                statistics.completionRate(), statistics.averageProgress(),
                statistics.totalHoursRead(),
                statistics.averageSecondsPerFinishedBook() / 3600.0,
                statistics.totalHighlights(), statistics.highlightsWithNote(),
                bookName(statistics.mostHighlightedBook()),
                statistics.mostHighlightedCount(),
                currentBooks(statistics), statistics.finishedBooksByMonth(),
                statistics.finishedBooksThisYear(), statistics.monthlyBookPace(),
                statistics.annualBookProjection(),
                readingPace(statistics.averageReadingWordsPerMinute()),
                bookName(statistics.fastestReadBook()),
                bookName(statistics.slowestReadBook()));

        if (AppPreferences.isEnglish()) {
            return """
                    Interpret the reading data below; do not merely restate it.
                    Write approximately 90 words in English, in two short paragraphs.
                    Identify the two most meaningful patterns by comparing related
                    measures (completion and active reading, pace and reading history,
                    or highlights and completed books) and explain what those
                    relationships show. Mention at most two exact figures and avoid
                    describing each field separately. Output only the final prose.
                    No headings, lists, Markdown, word count, validation, invented
                    causes, missing-data comments, advice or recommendations.

                    %s
                    """.formatted(data);
        }
        return """
                Interpreta los datos de lectura siguientes; no te limites a repetirlos.
                Escribe unas 90 palabras en español, repartidas en dos párrafos
                breves. Detecta los dos patrones más significativos comparando datos
                relacionados (finalización y lecturas activas, ritmo e historial de
                lectura, o subrayados y libros terminados) y explica qué muestran
                esas relaciones. Menciona como máximo dos cifras exactas y evita
                describir cada campo por separado. Devuelve solo el texto final.
                Sin títulos, listas, Markdown, recuentos, comprobaciones, causas
                inventadas, comentarios sobre datos ausentes, consejos ni sugerencias.

                %s
                """.formatted(data);
    }

    private String cleanAnalysis(String response) {
        if (response == null || response.isBlank()) return response;

        String warning = "\n\n⚠ ";
        int warningStart = response.indexOf(warning);
        String warningText = warningStart >= 0 ? response.substring(warningStart) : "";
        String result = warningStart >= 0
                ? response.substring(0, warningStart) : response;

        String[] markers = {"\nTotal:", "\nConstraints check:",
                "\nComprobación de restricciones:", "\nWord count:"};
        int technicalStart = result.length();
        for (String marker : markers) {
            int index = result.indexOf(marker);
            if (index >= 0) technicalStart = Math.min(technicalStart, index);
        }

        java.util.regex.Matcher numberedWords = java.util.regex.Pattern
                .compile("(?m)^\\s*\\d+\\s*:\\s*\\S+")
                .matcher(result);
        if (numberedWords.find()) {
            technicalStart = Math.min(technicalStart, numberedWords.start());
        }
        result = result.substring(0, technicalStart).strip();

        // Algunos modelos dejan una palabra aislada justo antes del recuento.
        result = result.replaceFirst("(?s)\\R\\s*[\\p{L}]+\\s*$", "").strip();
        return looksComplete(result) ? result : result + warningText;
    }

    private boolean looksComplete(String text) {
        int words = text.isBlank() ? 0 : text.split("\\s+").length;
        return words >= 55 && text.matches("(?s).*?[.!?»]$");
    }

    private String currentBooks(ReadingStatistics statistics) {
        return statistics.inProgressBooks().stream()
                .map(book -> bookName(book) + " (" + book.getPercentRead()
                        + "%, " + decimal(book.getSecondsRead() / 3600.0) + " h)")
                .collect(Collectors.joining(", "));
    }

    private String bookName(Book book) {
        return book == null || book.getTitle() == null || book.getTitle().isBlank()
                ? "Sin datos" : book.getTitle();
    }

    private String decimal(double value) {
        return String.format(java.util.Locale.ROOT, "%.1f", value);
    }

    private String readingPace(double wordsPerMinute) {
        return wordsPerMinute <= 0 ? "Sin datos"
                : Math.round(wordsPerMinute) + " palabras por minuto";
    }
}
