package com.arcac.managerkobo.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.arcac.managerkobo.model.Book;
import com.arcac.managerkobo.model.Bookmark;
import com.arcac.managerkobo.service.HighlightExportService.ExportFormat;
import java.awt.image.BufferedImage;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import javax.imageio.ImageIO;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ExportServicesTest {
    @TempDir
    Path temporaryDirectory;

    private List<Bookmark> highlights;
    private ReadingStatistics statistics;

    @BeforeEach
    void createExportData() {
        Book finished = book("finished", "El viaje terminado",
                "Ana Demo", 2, 100, 18_000, 60_000);
        Book current = book("current",
                "Una lectura actual con un título suficientemente largo",
                "Luis Demo", 1, 55, 9_000, 80_000);
        highlights = List.of(
                highlight("finished", "El viaje terminado",
                        "Primera idea\ncon espacios   repetidos"),
                highlight("finished", "El viaje terminado",
                        "Segunda idea destacada"),
                highlight("current", "Una lectura actual",
                        "Fragmento de la lectura actual"));
        statistics = new LibraryStatisticsService()
                .calculate(List.of(finished, current), highlights);
    }

    @Test
    void exportsHighlightsAsSimpleUtf8Text() throws Exception {
        Path destination = temporaryDirectory.resolve("subrayados.txt");

        new HighlightExportService().export(
                highlights, destination, ExportFormat.TXT);

        String text = Files.readString(destination, StandardCharsets.UTF_8);
        assertTrue(text.contains("El viaje terminado"));
        assertTrue(text.contains("- Primera idea con espacios repetidos"));
        assertTrue(text.contains("- Segunda idea destacada"));
    }

    @Test
    void exportsReadableHighlightsPdf() throws Exception {
        Path destination = temporaryDirectory.resolve("subrayados.pdf");

        new HighlightExportService().export(
                highlights, destination, ExportFormat.PDF);

        assertTrue(Files.size(destination) > 0);
        try (PDDocument document = Loader.loadPDF(destination.toFile())) {
            assertTrue(document.getNumberOfPages() >= 1);
            String text = new PDFTextStripper().getText(document);
            assertTrue(text.contains("El viaje terminado"));
            assertTrue(text.contains("Primera idea con espacios repetidos"));
        }
    }

    @Test
    void exportsSummaryAsValidJpegWithExpectedSize() throws Exception {
        Path destination = temporaryDirectory.resolve("resumen.jpg");

        new SummaryExportService().export(statistics,
                LocalDateTime.of(2026, 8, 10, 18, 30), destination, false);

        BufferedImage image = ImageIO.read(destination.toFile());
        assertNotNull(image);
        assertEquals(1_200, image.getWidth());
        assertEquals(900, image.getHeight());
        assertTrue(Files.size(destination) > 1_000);
    }

    @Test
    void exportsOnePageReadingReportWithSelectableText() throws Exception {
        Path destination = temporaryDirectory.resolve("informe.pdf");
        String analysis = "La actividad refleja continuidad entre el avance "
                + "de las lecturas y el tiempo registrado. Los subrayados se "
                + "concentran en obras concretas y muestran diferentes formas "
                + "de relacionarse con cada libro.";

        new ReadingReportPdfService().export(statistics,
                LocalDateTime.of(2026, 8, 10, 18, 30), destination,
                analysis, false);

        assertTrue(Files.size(destination) > 0);
        try (PDDocument document = Loader.loadPDF(destination.toFile())) {
            assertEquals(1, document.getNumberOfPages());
            String text = new PDFTextStripper().getText(document);
            assertTrue(text.contains("Informe personal de lectura"));
            assertTrue(text.contains("Datos destacados"));
            assertTrue(text.contains("El viaje terminado"));
            assertTrue(text.contains("Análisis final con IA"));
            assertTrue(text.contains("La actividad refleja continuidad"));
        }
    }

    private Book book(String id, String title, String author, int status,
            int progress, int seconds, int wordCount) {
        Book book = new Book();
        book.setContentId(id);
        book.setTitle(title);
        book.setAuthor(author);
        book.setReadStatus(status);
        book.setPercentRead(progress);
        book.setSecondsRead(seconds);
        book.setWordCount(wordCount);
        return book;
    }

    private Bookmark highlight(String volumeId, String title, String text) {
        Bookmark highlight = new Bookmark();
        highlight.setVolumeId(volumeId);
        highlight.setBookTitle(title);
        highlight.setText(text);
        highlight.setType("highlight");
        return highlight;
    }
}
