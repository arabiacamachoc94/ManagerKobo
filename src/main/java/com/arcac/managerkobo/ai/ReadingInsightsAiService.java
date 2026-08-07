package com.arcac.managerkobo.ai;

import com.arcac.managerkobo.model.Book;
import com.arcac.managerkobo.service.ReadingStatistics;
import com.arcac.managerkobo.ui.util.AppPreferences;
import java.util.Map;
import java.util.stream.Collectors;

/** Genera una interpretación breve a partir de estadísticas agregadas. */
public class ReadingInsightsAiService {
    private final GeminiClient client = new GeminiClient();

    public boolean isConfigured() {
        return client.isConfigured();
    }

    public String analyze(ReadingStatistics statistics) {
        return client.generate(buildPrompt(statistics));
    }

    private String buildPrompt(ReadingStatistics statistics) {
        String data = """
                Biblioteca:
                - Libros totales: %d
                - Terminados: %d
                - En curso: %d
                - Sin empezar: %d
                - Porcentaje de finalización: %.1f%%
                - Progreso medio: %.1f%%
                - Tiempo total: %.1f horas
                - Media por libro empezado: %.1f horas
                - Media por libro terminado: %.1f horas
                - Subrayados: %d
                - Subrayados con nota: %d

                Libro con más tiempo: %s
                Libro más subrayado: %s (%d subrayados)

                Tiempo por autor (principales): %s
                Subrayados por autor (principales): %s
                Libros con más tiempo: %s
                Libros con más subrayados: %s
                Mayor densidad de subrayados: %s
                Idiomas de la biblioteca: %s
                Lecturas actuales: %s

                Actividad mensual:
                - Subrayados: %s
                - Notas: %s
                - Palabras consultadas: %s
                - Libros terminados (estimado): %s
                - Terminados este año: %d
                - Ritmo mensual: %.1f libros
                - Proyección anual: %d libros
                """.formatted(
                statistics.totalBooks(), statistics.finishedBooks(),
                statistics.readingBooks(), statistics.unreadBooks(),
                statistics.completionRate(), statistics.averageProgress(),
                statistics.totalHoursRead(),
                statistics.averageSecondsPerStartedBook() / 3600.0,
                statistics.averageSecondsPerFinishedBook() / 3600.0,
                statistics.totalHighlights(), statistics.highlightsWithNote(),
                bookName(statistics.mostReadBook()),
                bookName(statistics.mostHighlightedBook()),
                statistics.mostHighlightedCount(),
                topMap(statistics.readingSecondsByAuthor(), true),
                topMap(statistics.highlightsByAuthor(), false),
                statistics.booksByReadingTime().stream().limit(5)
                        .map(book -> bookName(book) + " ("
                                + decimal(book.getSecondsRead() / 3600.0) + " h)")
                        .collect(Collectors.joining(", ")),
                topBookMap(statistics.highlightsByBook(), false),
                topBookMap(statistics.highlightDensityByBook(), true),
                statistics.booksByLanguage(), currentBooks(statistics),
                statistics.highlightsByMonth(), statistics.notesByMonth(),
                statistics.wordsByMonth(), statistics.finishedBooksByMonth(),
                statistics.finishedBooksThisYear(), statistics.monthlyBookPace(),
                statistics.annualBookProjection());

        if (AppPreferences.isEnglish()) {
            return """
                    Analyze the following aggregated reading statistics. Write a
                    natural, concise final insight of 85 to 105 words in English.
                    Write exactly two short paragraphs: the first should explain
                    the most relevant reading patterns and the second should give
                    one practical, neutral suggestion. Do not add headings, lists,
                    Markdown or bold text. Do not list every number, invent causes,
                    make psychological claims or mention missing data.

                    DATA:
                    %s
                    """.formatted(data);
        }
        return """
                Analiza las siguientes estadísticas agregadas de lectura. Escribe
                un análisis final natural y conciso de entre 85 y 105 palabras en
                español. Escribe exactamente dos párrafos breves: el primero debe
                explicar los patrones de lectura más relevantes y el segundo debe
                ofrecer una única sugerencia práctica y neutral. No añadas títulos,
                listas, Markdown ni negritas. No enumeres todas las cifras, no
                inventes causas, no hagas afirmaciones psicológicas y no menciones
                los datos que falten.

                DATOS:
                %s
                """.formatted(data);
    }

    private String currentBooks(ReadingStatistics statistics) {
        return statistics.inProgressBooks().stream()
                .map(book -> bookName(book) + " (" + book.getPercentRead()
                        + "%%, " + decimal(book.getSecondsRead() / 3600.0) + " h)")
                .collect(Collectors.joining(", "));
    }

    private String topMap(Map<String, ? extends Number> values, boolean seconds) {
        return values.entrySet().stream()
                .sorted((first, second) -> Double.compare(
                        second.getValue().doubleValue(),
                        first.getValue().doubleValue()))
                .limit(5)
                .map(entry -> entry.getKey() + " (" + (seconds
                        ? decimal(entry.getValue().doubleValue() / 3600.0) + " h"
                        : entry.getValue()) + ")")
                .collect(Collectors.joining(", "));
    }

    private String topBookMap(Map<Book, ? extends Number> values, boolean decimal) {
        return values.entrySet().stream()
                .sorted((first, second) -> Double.compare(
                        second.getValue().doubleValue(),
                        first.getValue().doubleValue()))
                .limit(5)
                .map(entry -> bookName(entry.getKey()) + " ("
                        + (decimal ? decimal(entry.getValue().doubleValue())
                                : entry.getValue()) + ")")
                .collect(Collectors.joining(", "));
    }

    private String bookName(Book book) {
        return book == null || book.getTitle() == null || book.getTitle().isBlank()
                ? "Sin datos" : book.getTitle();
    }

    private String decimal(double value) {
        return String.format(java.util.Locale.ROOT, "%.1f", value);
    }
}
