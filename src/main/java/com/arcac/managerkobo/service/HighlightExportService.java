package com.arcac.managerkobo.service;

import com.arcac.managerkobo.model.Bookmark;
import java.io.Closeable;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

/** Exporta subrayados en formatos tabulares o agrupados por libro. */
public class HighlightExportService {

    public void export(
            List<Bookmark> highlights, Path destination, ExportFormat format)
            throws IOException {
        switch (format) {
            case CSV -> exportCsv(highlights, destination);
            case TXT -> exportTxt(highlights, destination);
            case PDF -> exportPdf(highlights, destination);
        }
    }

    public void exportCsv(List<Bookmark> highlights, Path destination)
            throws IOException {
        StringBuilder csv = new StringBuilder();
        csv.append('\uFEFF');
        csv.append("Libro,Autor,Texto,Nota,Fecha,Capítulo,Color")
                .append(System.lineSeparator());

        for (Bookmark mark : highlights) {
            csv.append(csv(mark.getBookTitle())).append(',')
                    .append(csv(mark.getBookAuthor())).append(',')
                    .append(csv(mark.getText())).append(',')
                    .append(csv(mark.getUserNote())).append(',')
                    .append(csv(mark.getDateCreated())).append(',')
                    .append(csv(mark.getChapterTitle())).append(',')
                    .append(mark.getColor())
                    .append(System.lineSeparator());
        }
        Files.writeString(destination, csv, StandardCharsets.UTF_8);
    }

    public void exportTxt(List<Bookmark> highlights, Path destination)
            throws IOException {
        StringBuilder text = new StringBuilder();
        for (HighlightGroup group : grouped(highlights)) {
            text.append(group.title()).append(System.lineSeparator())
                    .append(System.lineSeparator());
            for (Bookmark mark : group.highlights()) {
                text.append("- ")
                        .append(cleanText(mark.getText()))
                        .append(System.lineSeparator());
            }
            text.append(System.lineSeparator());
        }
        Files.writeString(destination, text, StandardCharsets.UTF_8);
    }

    public void exportPdf(List<Bookmark> highlights, Path destination)
            throws IOException {
        try (PDDocument document = new PDDocument();
                PdfWriter writer = new PdfWriter(document)) {
            for (HighlightGroup group : grouped(highlights)) {
                writer.ensureSpace(38);
                writer.writeWrapped(group.title(), writer.boldFont(), 16, 20, "");
                writer.moveDown(7);
                for (Bookmark mark : group.highlights()) {
                    writer.writeWrapped(cleanText(mark.getText()),
                            writer.regularFont(), 11, 15, "- ");
                    writer.moveDown(5);
                }
                writer.moveDown(10);
            }
            writer.close();
            document.save(destination.toFile());
        }
    }

    private List<HighlightGroup> grouped(List<Bookmark> highlights) {
        Map<String, List<Bookmark>> groups = highlights.stream()
                .collect(Collectors.groupingBy(
                        this::groupId,
                        LinkedHashMap::new,
                        Collectors.toList()));

        List<HighlightGroup> result = new ArrayList<>();
        for (List<Bookmark> marks : groups.values()) {
            String title = fallback(marks.get(0).getBookTitle(), "Libro desconocido");
            result.add(new HighlightGroup(title, List.copyOf(marks)));
        }
        result.sort(Comparator.comparing(
                HighlightGroup::title, String.CASE_INSENSITIVE_ORDER));
        return result;
    }

    private String groupId(Bookmark mark) {
        return fallback(mark.getVolumeId(),
                "unknown:" + fallback(mark.getBookTitle(), "Libro desconocido"));
    }

    private String cleanText(String value) {
        return fallback(value, "").replaceAll("\\s+", " ").strip();
    }

    private String fallback(String value, String alternative) {
        return value == null || value.isBlank() ? alternative : value;
    }

    private String csv(String value) {
        String safeValue = value == null ? "" : value;
        return "\"" + safeValue.replace("\"", "\"\"") + "\"";
    }

    private record HighlightGroup(String title, List<Bookmark> highlights) {
    }

    public enum ExportFormat {
        CSV("CSV", ".csv"),
        TXT("Texto", ".txt"),
        PDF("PDF", ".pdf");

        private final String label;
        private final String extension;

        ExportFormat(String label, String extension) {
            this.label = label;
            this.extension = extension;
        }

        public String extension() {
            return extension;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    /** Escritura paginada y con ajuste de líneas para PDF. */
    private static final class PdfWriter implements Closeable {
        private static final float MARGIN = 52;
        private static final float PAGE_BOTTOM = 52;
        private static final float CONTENT_WIDTH =
                PDRectangle.A4.getWidth() - MARGIN * 2;

        private final PDDocument document;
        private final PDFont regular =
                new PDType1Font(Standard14Fonts.FontName.HELVETICA);
        private final PDFont bold =
                new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
        private PDPageContentStream stream;
        private float y;

        private PdfWriter(PDDocument document) throws IOException {
            this.document = document;
            newPage();
        }

        private PDFont regularFont() {
            return regular;
        }

        private PDFont boldFont() {
            return bold;
        }

        private void newPage() throws IOException {
            closeStream();
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);
            stream = new PDPageContentStream(document, page);
            y = PDRectangle.A4.getHeight() - MARGIN;
        }

        private void ensureSpace(float requiredHeight) throws IOException {
            if (y - requiredHeight < PAGE_BOTTOM) {
                newPage();
            }
        }

        private void moveDown(float amount) {
            y -= amount;
        }

        private void writeWrapped(String source, PDFont font, float fontSize,
                float lineHeight, String firstPrefix) throws IOException {
            String safe = encodable(source, font);
            List<String> lines = wrap(safe, font, fontSize,
                    CONTENT_WIDTH - width(firstPrefix, font, fontSize));
            for (int index = 0; index < lines.size(); index++) {
                ensureSpace(lineHeight);
                String prefix = index == 0 ? firstPrefix : "  ";
                writeLine(prefix + lines.get(index), font, fontSize);
                y -= lineHeight;
            }
        }

        private void writeLine(String text, PDFont font, float fontSize)
                throws IOException {
            stream.beginText();
            stream.setFont(font, fontSize);
            stream.newLineAtOffset(MARGIN, y);
            stream.showText(text);
            stream.endText();
        }

        private List<String> wrap(
                String text, PDFont font, float size, float maximumWidth)
                throws IOException {
            List<String> lines = new ArrayList<>();
            StringBuilder line = new StringBuilder();
            for (String word : text.split(" ")) {
                String candidate = line.isEmpty() ? word : line + " " + word;
                if (!line.isEmpty() && width(candidate, font, size) > maximumWidth) {
                    lines.add(line.toString());
                    line.setLength(0);
                    line.append(word);
                } else {
                    if (!line.isEmpty()) {
                        line.append(' ');
                    }
                    line.append(word);
                }
            }
            if (!line.isEmpty()) {
                lines.add(line.toString());
            }
            if (lines.isEmpty()) {
                lines.add("");
            }
            return lines;
        }

        private float width(String text, PDFont font, float size)
                throws IOException {
            return font.getStringWidth(text) / 1000f * size;
        }

        private String encodable(String text, PDFont font) {
            StringBuilder safe = new StringBuilder();
            text.codePoints().forEach(codePoint -> {
                String character = new String(Character.toChars(codePoint));
                try {
                    font.encode(character);
                    safe.append(character);
                } catch (IOException | IllegalArgumentException exception) {
                    safe.append('?');
                }
            });
            return safe.toString();
        }

        private void closeStream() throws IOException {
            if (stream != null) {
                stream.close();
                stream = null;
            }
        }

        @Override
        public void close() throws IOException {
            closeStream();
        }
    }
}
