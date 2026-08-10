package com.arcac.managerkobo.service;

import com.arcac.managerkobo.model.Book;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import javax.swing.ImageIcon;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import static com.arcac.managerkobo.util.ReadingFormat.duration;
import static com.arcac.managerkobo.util.ReadingFormat.textOr;

/** Informe PDF sencillo con texto seleccionable y paginación automática. */
public class ReadingReportPdfService {
    private final BookCoverService coverService = new BookCoverService();

    public void export(ReadingStatistics statistics,
            LocalDateTime synchronization, Path destination, String aiAnalysis,
            boolean english) throws IOException {
        try (PDDocument document = new PDDocument();
                Writer writer = new Writer(document, english)) {
            writer.header("Kobo Manager",
                    text(english, "Informe personal de lectura", "Personal reading report"),
                    synchronization == null
                    ? text(english, "Última sincronización: no disponible",
                            "Last synchronization: unavailable")
                    : text(english, "Última sincronización: ",
                            "Last synchronization: ") + synchronization.format(
                            DateTimeFormatter.ofPattern("dd/MM/yyyy · HH:mm")));
            writer.space(12);
            writer.metricCards(
                    new String[]{text(english, "LIBROS", "BOOKS"),
                            text(english, "HORAS LEÍDAS", "HOURS READ"),
                            text(english, "RITMO LECTOR", "READING PACE"),
                            text(english, "RITMO MENSUAL", "MONTHLY PACE")},
                    new String[]{String.valueOf(statistics.totalBooks()),
                            String.valueOf(Math.round(statistics.totalHoursRead())),
                            statistics.averageReadingWordsPerMinute() <= 0
                                    ? "--"
                                    : Math.round(statistics.averageReadingWordsPerMinute())
                                            + text(english, " ppm", " wpm"),
                            String.format(english ? Locale.ENGLISH
                                            : Locale.forLanguageTag("es-ES"), "%.1f",
                                    statistics.monthlyBookPace())});
            writer.space(12);

            writer.section(text(english, "Estado de la biblioteca", "Library status"));
            writer.statusChart(statistics.finishedBooks(), statistics.readingBooks(),
                    statistics.unreadBooks(), statistics.totalBooks());
            writer.space(15);

            writer.section(text(english, "Datos destacados", "Key insights"));
            writer.keyValue(text(english, "Autor con más tiempo de lectura",
                            "Author with most reading time"),
                    authorWithMostReadingTime(statistics, english));
            writer.keyValue(text(english, "Libro con más tiempo de lectura",
                            "Book with most reading time"),
                    bookTitle(statistics.mostReadBook(), english));
            writer.keyValue(text(english, "Libro más subrayado",
                            "Most-highlighted book"),
                    bookTitle(statistics.mostHighlightedBook(), english));
            writer.keyValue(text(english, "Media por libro empezado",
                            "Average per started book"),
                    duration(statistics.averageSecondsPerStartedBook()));
            writer.keyValue(text(english, "Ritmo medio de lectura",
                            "Average reading pace"),
                    paceText(statistics.averageReadingWordsPerMinute(), english));
            writer.keyValue(text(english, "Libro leído más rápido",
                            "Fastest-read book"),
                    fastestBookText(statistics, english));
            writer.keyValue(text(english, "Libro leído más lento",
                            "Slowest-read book"),
                    slowestBookText(statistics, english));
            writer.space(15);

            writer.section(text(english, "Lecturas en curso", "Current reads"));
            writeCurrentBooks(writer, statistics, english);
            if (aiAnalysis != null && !aiAnalysis.isBlank()) {
                writer.space(16);
                writer.section(text(english, "Análisis final con IA",
                        "Final AI analysis"));
                writer.aiAnalysis(aiAnalysis.strip());
                writer.footerNote(text(english,
                        "Generado por Gemini a partir de estadísticas agregadas.",
                        "Generated by Gemini from aggregated statistics."));
            }
            writer.close();
            document.save(destination.toFile());
        }
    }

    private void writeCurrentBooks(Writer writer, ReadingStatistics statistics,
            boolean english) throws IOException {
        List<Book> books = statistics.inProgressBooks();
        if (books.isEmpty()) {
            writer.text(text(english, "No hay libros en progreso.",
                    "There are no books in progress."));
            return;
        }
        int visibleBooks = Math.min(4, books.size());
        List<Book> visible = books.subList(0, visibleBooks);
        List<ImageIcon> covers = visible.stream()
                .map(book -> coverService.loadCover(book, 64, 76))
                .toList();
        writer.bookRow(visible, covers, english);
        if (books.size() > visibleBooks) {
            writer.small(english
                    ? "And " + (books.size() - visibleBooks) + " more current read(s)."
                    : "Y " + (books.size() - visibleBooks) + " lectura(s) en curso más.");
        }
    }

    private String authorWithMostReadingTime(ReadingStatistics statistics,
            boolean english) {
        return statistics.readingSecondsByAuthor().entrySet().stream()
                .max(java.util.Map.Entry.comparingByValue())
                .map(java.util.Map.Entry::getKey)
                .filter(value -> value != null && !value.isBlank())
                .orElse(text(english, "Sin datos", "No data"));
    }

    private String bookTitle(Book book, boolean english) {
        return book == null ? text(english, "Sin datos", "No data")
                : textOr(book.getTitle(), text(english, "Sin título", "Untitled"));
    }

    private String paceText(double wordsPerMinute, boolean english) {
        return wordsPerMinute <= 0 ? text(english, "Sin datos", "No data")
                : Math.round(wordsPerMinute)
                        + text(english, " palabras/min", " words/min");
    }

    private String fastestBookText(ReadingStatistics statistics,
            boolean english) {
        if (statistics.fastestReadBook() == null) {
            return text(english, "Sin datos", "No data");
        }
        return compactTitle(statistics.fastestReadBook(), english);
    }

    private String slowestBookText(ReadingStatistics statistics,
            boolean english) {
        if (statistics.slowestReadBook() == null) {
            return text(english, "Sin datos", "No data");
        }
        return compactTitle(statistics.slowestReadBook(), english);
    }

    private String compactTitle(Book book, boolean english) {
        String title = bookTitle(book, english);
        return title.length() <= 48 ? title : title.substring(0, 45) + "...";
    }

    private String text(boolean english, String spanish, String translated) {
        return english ? translated : spanish;
    }

    private static final class Writer implements Closeable {
        private static final float MARGIN = 52;
        private static final float BOTTOM = 52;
        private static final float WIDTH = PDRectangle.A4.getWidth() - MARGIN * 2;
        private final PDDocument document;
        private final boolean english;
        private final PDFont regular = new PDType1Font(
                Standard14Fonts.FontName.HELVETICA);
        private final PDFont bold = new PDType1Font(
                Standard14Fonts.FontName.HELVETICA_BOLD);
        private PDPageContentStream stream;
        private float y;
        private int pageNumber;

        Writer(PDDocument document, boolean english) throws IOException {
            this.document = document;
            this.english = english;
            newPage();
        }

        void header(String title, String subtitle, String detail) throws IOException {
            ensure(100);
            stream.setNonStrokingColor(112 / 255f, 78 / 255f, 198 / 255f);
            stream.addRect(MARGIN, y - 64, WIDTH, 74);
            stream.fill();
            writeColoredAt(title, bold, 23, MARGIN + 22, y - 14,
                    1f, 1f, 1f);
            writeColoredAt(subtitle, regular, 12, MARGIN + 22, y - 34,
                    1f, 1f, 1f);
            writeColoredAt(detail, regular, 8, MARGIN + 22, y - 52,
                    230 / 255f, 226 / 255f, 245 / 255f);
            y -= 79;
        }

        void section(String value) throws IOException {
            ensure(30);
            line(value, bold, 15, 21);
            stream.setStrokingColor(
                    210 / 255f, 214 / 255f, 225 / 255f);
            stream.moveTo(MARGIN, y + 11);
            stream.lineTo(MARGIN + WIDTH, y + 11);
            stream.stroke();
            y -= 6;
        }

        void text(String value) throws IOException {
            wrapped(value, regular, 11, 16, WIDTH);
        }

        void small(String value) throws IOException {
            wrapped(value, regular, 9, 13, WIDTH);
        }

        void footerNote(String value) throws IOException {
            if (y >= BOTTOM + 10) {
                writeMutedAt(value, regular, 8, MARGIN, BOTTOM);
            }
        }

        void aiAnalysis(String value) throws IOException {
            String normalized = limitWords(value, 110).replace("\r", "")
                    .replace("**", "")
                    .replace("__", "")
                    .replace("###", "")
                    .replace("##", "")
                    .replace("•", "-");
            boolean previousBlank = false;
            for (String rawLine : normalized.split("\n", -1)) {
                String line = rawLine.strip();
                if (line.isEmpty()) {
                    if (!previousBlank) space(3);
                    previousBlank = true;
                    continue;
                }
                previousBlank = false;
                if (isAnalysisHeading(line)) {
                    line(line, bold, 10, 15);
                } else {
                    wrapped(line, regular, 8.5f, 11, WIDTH);
                }
            }
        }

        private boolean isAnalysisHeading(String value) {
            return value.equalsIgnoreCase("PANORAMA")
                    || value.equalsIgnoreCase("PATRONES")
                    || value.equalsIgnoreCase("SUGERENCIA")
                    || value.equalsIgnoreCase("OVERVIEW")
                    || value.equalsIgnoreCase("PATTERNS")
                    || value.equalsIgnoreCase("SUGGESTION");
        }

        void keyValue(String key, String value) throws IOException {
            ensure(16);
            writeAt(key, bold, 10, MARGIN, y);
            float keyWidth = width(key, bold, 10);
            float maximumValueWidth = Math.max(80, WIDTH - keyWidth - 20);
            float valueSize = 10;
            while (valueSize > 8f
                    && width(value, regular, valueSize) > maximumValueWidth) {
                valueSize -= 0.5f;
            }
            String fittedValue = ellipsize(
                    value, regular, valueSize, maximumValueWidth);
            float valueWidth = width(fittedValue, regular, valueSize);
            writeAt(fittedValue, regular, valueSize,
                    MARGIN + WIDTH - valueWidth, y);
            y -= 16;
        }

        private String ellipsize(String value, PDFont font, float size,
                float maximumWidth) throws IOException {
            if (width(value, font, size) <= maximumWidth) return value;
            String suffix = "...";
            int end = value.length();
            while (end > 1 && width(value.substring(0, end).stripTrailing()
                    + suffix, font, size) > maximumWidth) {
                end--;
            }
            return value.substring(0, Math.max(1, end)).stripTrailing() + suffix;
        }

        private String limitWords(String value, int maximumWords) {
            String normalized = value == null ? "" : value.strip();
            String[] words = normalized.split("\\s+");
            if (words.length <= maximumWords) return normalized;
            return String.join(" ", java.util.Arrays.copyOf(words, maximumWords))
                    + "...";
        }

        void metricCards(String[] labels, String[] values) throws IOException {
            ensure(62);
            float gap = 10;
            int cardCount = Math.min(labels.length, values.length);
            float cardWidth = (WIDTH - gap * (cardCount - 1)) / cardCount;
            for (int index = 0; index < cardCount; index++) {
                float x = MARGIN + index * (cardWidth + gap);
                stream.setNonStrokingColor(
                        246 / 255f, 247 / 255f, 251 / 255f);
                stream.addRect(x, y - 48, cardWidth, 52);
                stream.fill();
                float[][] accents = {
                        {69 / 255f, 145 / 255f, 225 / 255f},
                        {132 / 255f, 92 / 255f, 230 / 255f},
                        {239 / 255f, 145 / 255f, 65 / 255f},
                        {73 / 255f, 190 / 255f, 123 / 255f}
                };
                float[] accent = accents[index % accents.length];
                stream.setNonStrokingColor(accent[0], accent[1], accent[2]);
                stream.addRect(x, y - 48, 5, 52);
                stream.fill();
                writeAt(values[index], bold, 19, x + 18, y - 20);
                writeMutedAt(labels[index], bold, 7, x + 18, y - 39);
            }
            y -= 56;
        }

        void statusChart(int finished, int reading, int unread, int total)
                throws IOException {
            ensure(84);
            String[] labels = english
                    ? new String[]{"Finished", "Reading", "Not started"}
                    : new String[]{"Terminados", "Leyendo", "Sin empezar"};
            int[] values = {finished, reading, unread};
            float[][] colors = {
                    {73 / 255f, 190 / 255f, 123 / 255f},
                    {132 / 255f, 92 / 255f, 230 / 255f},
                    {69 / 255f, 145 / 255f, 225 / 255f}
            };
            float barX = MARGIN + 105;
            float barWidth = WIDTH - 155;
            for (int index = 0; index < labels.length; index++) {
                float rowY = y - index * 24;
                int percentage = total == 0 ? 0
                        : (int) Math.round(values[index] * 100.0 / total);
                writeAt(labels[index], regular, 10, MARGIN, rowY);
                stream.setNonStrokingColor(
                        231 / 255f, 234 / 255f, 241 / 255f);
                stream.addRect(barX, rowY - 7, barWidth, 9);
                stream.fill();
                stream.setNonStrokingColor(
                        colors[index][0], colors[index][1], colors[index][2]);
                stream.addRect(barX, rowY - 7,
                        barWidth * percentage / 100f, 9);
                stream.fill();
                writeAt(values[index] + " · " + percentage + "%", bold, 9,
                        MARGIN + WIDTH - 45, rowY);
            }
            y -= 76;
        }

        void bookRow(List<Book> books, List<ImageIcon> covers,
                boolean english) throws IOException {
            ensure(154);
            float cardTop = y + 4;
            float gap = 9;
            float cardWidth = (WIDTH - gap * (books.size() - 1))
                    / books.size();
            for (int index = 0; index < books.size(); index++) {
                Book book = books.get(index);
                ImageIcon cover = covers.get(index);
                float cardX = MARGIN + index * (cardWidth + gap);
                stream.setNonStrokingColor(
                        246 / 255f, 247 / 255f, 251 / 255f);
                stream.addRect(cardX, cardTop - 146, cardWidth, 146);
                stream.fill();

                if (cover != null) {
                    BufferedImage image = toBufferedImage(cover);
                    PDImageXObject pdfImage = LosslessFactory.createFromImage(
                            document, image);
                    float imageX = cardX
                            + (cardWidth - cover.getIconWidth()) / 2;
                    stream.drawImage(pdfImage, imageX, cardTop - 82,
                            cover.getIconWidth(), cover.getIconHeight());
                } else {
                    stream.setNonStrokingColor(
                            225 / 255f, 228 / 255f, 236 / 255f);
                    float placeholderX = cardX + cardWidth / 2 - 22;
                    stream.addRect(placeholderX, cardTop - 78, 44, 66);
                    stream.fill();
                    writeCenteredAt("K", bold, 18,
                            cardX + cardWidth / 2, cardTop - 48);
                }

                float centerX = cardX + cardWidth / 2;
                String title = textOr(book.getTitle(),
                        english ? "Untitled" : "Sin título");
                writeCenteredAt(shorten(title, 18), bold, 9,
                        centerX, cardTop - 98);
                writeMutedCenteredAt(shorten(textOr(book.getAuthor(),
                                english ? "Unknown author" : "Autor desconocido"), 18),
                        regular, 7, centerX, cardTop - 111);
                writeCenteredAt(book.getPercentRead() + "%",
                        bold, 8, centerX, cardTop - 125);

                float barX = cardX + 10;
                float barWidth = cardWidth - 20;
                stream.setNonStrokingColor(
                        225 / 255f, 228 / 255f, 236 / 255f);
                stream.addRect(barX, cardTop - 138, barWidth, 6);
                stream.fill();
                stream.setNonStrokingColor(
                        132 / 255f, 92 / 255f, 230 / 255f);
                stream.addRect(barX, cardTop - 138,
                        barWidth * Math.max(0, Math.min(100,
                                book.getPercentRead())) / 100f, 6);
                stream.fill();
            }
            y -= 154;
        }

        private void writeCenteredAt(String value, PDFont font, float size,
                float centerX, float positionY) throws IOException {
            writeAt(value, font, size,
                    centerX - width(value, font, size) / 2, positionY);
        }

        private void writeMutedCenteredAt(String value, PDFont font,
                float size, float centerX, float positionY) throws IOException {
            writeMutedAt(value, font, size,
                    centerX - width(value, font, size) / 2, positionY);
        }

        private BufferedImage toBufferedImage(ImageIcon icon) {
            BufferedImage image = new BufferedImage(icon.getIconWidth(),
                    icon.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
            Graphics2D graphics = image.createGraphics();
            icon.paintIcon(null, graphics, 0, 0);
            graphics.dispose();
            return image;
        }

        private String shorten(String value, int maximumCharacters) {
            return value.length() <= maximumCharacters ? value
                    : value.substring(0, maximumCharacters - 1) + "…";
        }

        void space(float amount) {
            y -= amount;
        }

        private void line(String value, PDFont font, float size,
                float lineHeight) throws IOException {
            ensure(lineHeight);
            writeAt(value, font, size, MARGIN, y);
            y -= lineHeight;
        }

        private void wrapped(String value, PDFont font, float size,
                float lineHeight, float maximumWidth) throws IOException {
            StringBuilder line = new StringBuilder();
            for (String word : safe(value, font).split(" ")) {
                String candidate = line.isEmpty() ? word : line + " " + word;
                if (!line.isEmpty() && width(candidate, font, size) > maximumWidth) {
                    line(line.toString(), font, size, lineHeight);
                    line.setLength(0);
                    line.append(word);
                } else {
                    if (!line.isEmpty()) line.append(' ');
                    line.append(word);
                }
            }
            if (!line.isEmpty()) line(line.toString(), font, size, lineHeight);
        }

        private void writeAt(String value, PDFont font, float size,
                float x, float positionY) throws IOException {
            stream.setNonStrokingColor(
                    31 / 255f, 35 / 255f, 48 / 255f);
            rawText(value, font, size, x, positionY);
        }

        private void writeMutedAt(String value, PDFont font, float size,
                float x, float positionY) throws IOException {
            stream.setNonStrokingColor(
                    99 / 255f, 106 / 255f, 124 / 255f);
            rawText(value, font, size, x, positionY);
        }

        private void writeColoredAt(String value, PDFont font, float size,
                float x, float positionY, float red, float green, float blue)
                throws IOException {
            stream.setNonStrokingColor(red, green, blue);
            rawText(value, font, size, x, positionY);
        }

        private void rawText(String value, PDFont font, float size,
                float x, float positionY) throws IOException {
            stream.beginText();
            stream.setFont(font, size);
            stream.newLineAtOffset(x, positionY);
            stream.showText(safe(value, font));
            stream.endText();
        }

        private void ensure(float height) throws IOException {
            if (y - height < BOTTOM) newPage();
        }

        private void newPage() throws IOException {
            closeStream();
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);
            stream = new PDPageContentStream(document, page);
            y = PDRectangle.A4.getHeight() - MARGIN;
            pageNumber++;
        }

        private float width(String value, PDFont font, float size)
                throws IOException {
            return font.getStringWidth(safe(value, font)) / 1000f * size;
        }

        private String safe(String value, PDFont font) {
            StringBuilder result = new StringBuilder();
            value.codePoints().forEach(codePoint -> {
                String character = new String(Character.toChars(codePoint));
                try {
                    font.encode(character);
                    result.append(character);
                } catch (Exception ignored) {
                    result.append('?');
                }
            });
            return result.toString();
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
