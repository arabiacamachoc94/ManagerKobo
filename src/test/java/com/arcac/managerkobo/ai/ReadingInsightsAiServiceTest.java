package com.arcac.managerkobo.ai;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.arcac.managerkobo.model.Book;
import com.arcac.managerkobo.service.LibraryStatisticsService;
import com.arcac.managerkobo.service.ReadingStatistics;
import java.util.List;
import org.junit.jupiter.api.Test;

class ReadingInsightsAiServiceTest {

    @Test
    void removesWordCountingAndValidationAddedByTheModel() {
        String analysis = completeAnalysis();
        String modelResponse = analysis + """

                a
                70: los
                71: subrayados
                Total: 86 words.
                Constraints check: Language Spanish (Yes)

                ⚠ RESUMEN INCOMPLETO
                Gemini alcanzó el límite de respuesta.
                """;
        ReadingInsightsAiService service = serviceReturning(modelResponse);

        String result = service.analyze(emptyStatistics());

        assertEquals(analysis, result);
        assertFalse(result.contains("Total:"));
        assertFalse(result.contains("Constraints check:"));
        assertFalse(result.contains("RESUMEN INCOMPLETO"));
    }

    @Test
    void keepsIncompleteWarningWhenTextIsActuallyTooShort() {
        String modelResponse = "El análisis quedó interrumpido"
                + "\n\n⚠ RESUMEN INCOMPLETO\n"
                + "Gemini alcanzó el límite de respuesta.";
        ReadingInsightsAiService service = serviceReturning(modelResponse);

        String result = service.analyze(emptyStatistics());

        assertTrue(result.contains("RESUMEN INCOMPLETO"));
    }

    @Test
    void preservesAValidCompletedAnalysis() {
        String analysis = completeAnalysis();
        ReadingInsightsAiService service = serviceReturning(analysis);

        assertEquals(analysis, service.analyze(emptyStatistics()));
    }

    @Test
    void promptUsesOnePercentSignForCurrentBookProgress() {
        CapturingGeminiClient client = new CapturingGeminiClient("Respuesta corta.");
        ReadingInsightsAiService service = new ReadingInsightsAiService(client);
        Book book = new Book();
        book.setTitle("Lectura actual");
        book.setReadStatus(1);
        book.setPercentRead(50);
        book.setSecondsRead(3_600);
        ReadingStatistics statistics = new LibraryStatisticsService()
                .calculate(List.of(book));

        service.analyze(statistics);

        assertTrue(client.prompt.contains("Lectura actual (50%, 1.0 h)"));
        assertFalse(client.prompt.contains("50%%"));
    }

    private ReadingInsightsAiService serviceReturning(String response) {
        return new ReadingInsightsAiService(
                new CapturingGeminiClient(response));
    }

    private ReadingStatistics emptyStatistics() {
        return new LibraryStatisticsService().calculate(List.of());
    }

    private String completeAnalysis() {
        return "La biblioteca refleja una lectura constante, con una proporción "
                + "alta de libros terminados frente a los que permanecen activos. "
                + "El ritmo registrado mantiene una relación equilibrada entre "
                + "el tiempo dedicado y el avance acumulado en las obras.\n\n"
                + "Los subrayados se concentran en determinadas lecturas, lo que "
                + "diferencia los libros consultados con mayor detalle de aquellos "
                + "seguidos de forma más continua. El historial mensual muestra "
                + "además una actividad distribuida y sin cambios bruscos.";
    }

    private static final class CapturingGeminiClient extends GeminiClient {
        private final String response;
        private String prompt;

        CapturingGeminiClient(String response) {
            super("test-key", "test-model");
            this.response = response;
        }

        @Override
        public String generate(String prompt) {
            this.prompt = prompt;
            return response;
        }
    }
}
