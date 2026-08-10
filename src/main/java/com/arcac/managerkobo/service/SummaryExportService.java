package com.arcac.managerkobo.service;

import com.arcac.managerkobo.model.Book;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import static com.arcac.managerkobo.util.ReadingFormat.textOr;

/** Genera un resumen visual fijo, independiente del tamaño de la ventana. */
public class SummaryExportService {
    private static final int WIDTH = 1200;
    private static final int HEIGHT = 900;
    private static final Color INK = new Color(31, 35, 48);
    private static final Color MUTED = new Color(99, 106, 124);
    private static final Color BORDER = new Color(218, 222, 232);
    private static final Color PANEL = new Color(247, 248, 252);
    private static final Color PURPLE = new Color(132, 92, 230);
    private static final Color GREEN = new Color(73, 190, 123);
    private static final Color BLUE = new Color(69, 145, 225);
    private static final Color ORANGE = new Color(239, 145, 65);
    private final BookCoverService coverService = new BookCoverService();

    public void export(ReadingStatistics statistics,
            LocalDateTime synchronization, Path destination, boolean english)
            throws IOException {
        BufferedImage image = render(statistics, synchronization, english);
        if (!ImageIO.write(image, "jpg", destination.toFile())) {
            throw new IOException("No hay un generador JPEG disponible.");
        }
    }

    private BufferedImage render(ReadingStatistics statistics,
            LocalDateTime synchronization, boolean english) {
        BufferedImage image = new BufferedImage(
                WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, 0, WIDTH, HEIGHT);

        drawText(graphics, "Kobo Manager", 56, 60, 30, Font.BOLD, INK);
        drawText(graphics, text(english, "Resumen de lectura", "Reading overview"),
                56, 96, 18, Font.PLAIN, MUTED);
        String sync = synchronization == null
                ? text(english, "Sincronización no disponible", "Synchronization unavailable")
                : text(english, "Última sincronización: ", "Last synchronization: ")
                        + synchronization.format(
                        DateTimeFormatter.ofPattern("dd/MM/yyyy · HH:mm"));
        drawRightText(graphics, sync, WIDTH - 56, 72, 13, MUTED);
        graphics.setColor(BORDER);
        graphics.drawLine(56, 120, WIDTH - 56, 120);

        int cardWidth = 255;
        drawMetric(graphics, 56, 145, cardWidth,
                text(english, "LIBROS", "BOOKS"), statistics.totalBooks(), BLUE);
        drawMetric(graphics, 329, 145, cardWidth,
                text(english, "TERMINADOS", "FINISHED"), statistics.finishedBooks(), GREEN);
        drawMetric(graphics, 602, 145, cardWidth,
                text(english, "HORAS LEÍDAS", "HOURS READ"),
                Math.round(statistics.totalHoursRead()), PURPLE);
        drawMetric(graphics, 875, 145, cardWidth,
                text(english, "RITMO DE LECTURA (PPM)", "READING PACE (WPM)"),
                statistics.averageReadingWordsPerMinute() <= 0
                        ? "--"
                        : String.valueOf(Math.round(
                                statistics.averageReadingWordsPerMinute())),
                ORANGE);

        drawText(graphics, text(english, "Lecturas en curso", "Current reads"),
                56, 290, 19, Font.BOLD, INK);
        drawCurrentBooks(graphics, statistics.inProgressBooks(),
                56, 310, 530, 190, english);

        drawText(graphics, text(english, "Datos destacados", "Key insights"),
                614, 290, 19, Font.BOLD, INK);
        drawHighlights(graphics, statistics, 614, 310, 530, 190, english);

        drawText(graphics, text(english, "Estado de la biblioteca", "Library status"),
                56, 545, 19, Font.BOLD, INK);
        drawDonutCard(graphics, 56, 565, 530, 260,
                List.of(statistics.finishedBooks(), statistics.readingBooks(), statistics.unreadBooks()),
                english ? List.of("Finished", "Reading", "Not started")
                        : List.of("Terminados", "Leyendo", "Sin empezar"),
                List.of(GREEN, PURPLE, BLUE));

        int[] progress = progressRanges(statistics.inProgressBooks());
        drawText(graphics, text(english, "Progreso de lecturas activas",
                        "Current reading progress"),
                614, 545, 19, Font.BOLD, INK);
        drawDonutCard(graphics, 614, 565, 530, 260,
                List.of(progress[0], progress[1], progress[2], progress[3]),
                english ? List.of("< 25%", "25–49%", "50–74%", "75% or more")
                        : List.of("< 25%", "25–49%", "50–74%", "75% o más"),
                List.of(BLUE, PURPLE, ORANGE, GREEN));

        drawRightText(graphics, text(english, "Generado con Kobo Manager",
                        "Generated with Kobo Manager"), WIDTH - 56,
                HEIGHT - 30, 11, MUTED);
        graphics.dispose();
        return image;
    }

    private void drawMetric(Graphics2D g, int x, int y, int width,
            String title, long value, Color accent) {
        drawMetric(g, x, y, width, title, String.valueOf(value), accent);
    }

    private void drawMetric(Graphics2D g, int x, int y, int width,
            String title, String value, Color accent) {
        roundedPanel(g, x, y, width, 105);
        g.setColor(accent);
        g.fillRoundRect(x + 18, y + 18, 8, 69, 8, 8);
        drawText(g, value, x + 45, y + 60, 28, Font.BOLD, INK);
        drawText(g, title, x + 45, y + 84, 11, Font.BOLD, MUTED);
    }

    private void drawCurrentBooks(Graphics2D g, List<Book> books,
            int x, int y, int width, int height, boolean english) {
        if (books.isEmpty()) {
            roundedPanel(g, x, y, width, height);
            drawText(g, text(english, "No hay libros en progreso",
                            "There are no books in progress"), x + 22, y + 52,
                    14, Font.PLAIN, MUTED);
            return;
        }
        int visible = Math.min(3, books.size());
        int gap = 12;
        int cardWidth = (width - gap * (visible - 1)) / visible;
        for (int index = 0; index < visible; index++) {
            Book book = books.get(index);
            int cardX = x + index * (cardWidth + gap);
            roundedPanel(g, cardX, y, cardWidth, height);

            ImageIcon cover = coverService.loadCover(book, 82, 104);
            if (cover != null) {
                int coverX = cardX + (cardWidth - cover.getIconWidth()) / 2;
                cover.paintIcon(null, g, coverX, y + 13);
            } else {
                g.setColor(BORDER);
                g.fillRoundRect(cardX + cardWidth / 2 - 29,
                        y + 13, 58, 82, 8, 8);
                drawCenteredText(g, "K", cardX + cardWidth / 2,
                        y + 64, 24, Font.BOLD, PURPLE);
            }

            String title = textOr(book.getTitle(),
                    text(english, "Sin título", "Untitled"));
            drawCenteredText(g, ellipsize(g, title, cardWidth - 18),
                    cardX + cardWidth / 2, y + 137, 11, Font.BOLD, INK);
            drawCenteredText(g, book.getPercentRead() + "%",
                    cardX + cardWidth / 2, y + 157,
                    10, Font.BOLD, PURPLE);
            g.setColor(BORDER);
            g.fillRoundRect(cardX + 12, y + height - 18,
                    cardWidth - 24, 6, 6, 6);
            g.setColor(PURPLE);
            g.fillRoundRect(cardX + 12, y + height - 18,
                    (cardWidth - 24) * book.getPercentRead() / 100,
                    6, 6, 6);
        }
    }

    private void drawHighlights(Graphics2D g, ReadingStatistics stats,
            int x, int y, int width, int height, boolean english) {
        roundedPanel(g, x, y, width, height);
        String author = stats.readingSecondsByAuthor().entrySet().stream()
                .max(java.util.Map.Entry.comparingByValue())
                .map(java.util.Map.Entry::getKey)
                .filter(value -> value != null && !value.isBlank())
                .orElse(text(english, "Sin datos", "No data"));
        String highlighted = stats.mostHighlightedBook() == null
                ? text(english, "Sin datos", "No data")
                : textOr(stats.mostHighlightedBook().getTitle(),
                        text(english, "Sin título", "Untitled"));
        drawDataLine(g, x + 22, y + 29,
                text(english, "Autor con más tiempo", "Top author by time"), author);
        drawDataLine(g, x + 22, y + 58,
                text(english, "Libro más subrayado", "Most-highlighted book"), highlighted);
        drawDataLine(g, x + 22, y + 87,
                text(english, "Progreso medio", "Average progress"),
                Math.round(stats.averageProgress()) + "%");
        drawDataLine(g, x + 22, y + 116,
                text(english, "Lecturas activas", "Current reads"),
                String.valueOf(stats.readingBooks()));
        String fastest = stats.fastestReadBook() == null
                ? text(english, "Sin datos", "No data")
                : textOr(stats.fastestReadBook().getTitle(),
                        text(english, "Sin título", "Untitled"));
        drawDataLine(g, x + 22, y + 145,
                text(english, "Libro leído más rápido", "Fastest-read book"),
                fastest);
        String slowest = stats.slowestReadBook() == null
                ? text(english, "Sin datos", "No data")
                : textOr(stats.slowestReadBook().getTitle(),
                        text(english, "Sin título", "Untitled"));
        drawDataLine(g, x + 22, y + 174,
                text(english, "Libro leído más lento", "Slowest-read book"),
                slowest);
    }

    private void drawDataLine(Graphics2D g, int x, int y, String label, String value) {
        drawText(g, label, x, y, 12, Font.BOLD, MUTED);
        drawRightText(g, ellipsize(g, value, 250), x + 485, y, 13, INK);
    }

    private void drawDonutCard(Graphics2D g, int x, int y, int width, int height,
            List<Integer> values, List<String> labels, List<Color> colors) {
        roundedPanel(g, x, y, width, height);
        int total = values.stream().mapToInt(Integer::intValue).sum();
        int diameter = 170;
        int donutX = x + 38;
        int donutY = y + 43;
        int start = 90;
        int used = 0;
        if (total == 0) {
            g.setColor(BORDER);
            g.fillOval(donutX, donutY, diameter, diameter);
        } else {
            for (int index = 0; index < values.size(); index++) {
                int angle = index == values.size() - 1 ? 360 - used
                        : (int) Math.round(values.get(index) * 360.0 / total);
                g.setColor(colors.get(index));
                g.fillArc(donutX, donutY, diameter, diameter, start, -angle);
                start -= angle;
                used += angle;
            }
        }
        g.setColor(Color.WHITE);
        g.fillOval(donutX + 47, donutY + 47, 76, 76);
        drawCenteredText(g, String.valueOf(total), donutX + 85, donutY + 94,
                23, Font.BOLD, INK);
        for (int index = 0; index < labels.size(); index++) {
            int lineY = y + 58 + index * 40;
            g.setColor(colors.get(index));
            g.fillOval(x + 250, lineY - 10, 10, 10);
            drawText(g, labels.get(index), x + 270, lineY, 12, Font.PLAIN, INK);
            drawRightText(g, String.valueOf(values.get(index)), x + width - 28,
                    lineY, 12, INK);
        }
    }

    private int[] progressRanges(List<Book> books) {
        int[] result = new int[4];
        for (Book book : books) {
            int progress = book.getPercentRead();
            if (progress < 25) result[0]++;
            else if (progress < 50) result[1]++;
            else if (progress < 75) result[2]++;
            else result[3]++;
        }
        return result;
    }

    private void roundedPanel(Graphics2D g, int x, int y, int width, int height) {
        g.setColor(PANEL);
        g.fillRoundRect(x, y, width, height, 22, 22);
        g.setColor(BORDER);
        g.setStroke(new BasicStroke(1));
        g.drawRoundRect(x, y, width, height, 22, 22);
    }

    private void drawText(Graphics2D g, String text, int x, int y,
            int size, int style, Color color) {
        g.setFont(new Font("SansSerif", style, size));
        g.setColor(color);
        g.drawString(text, x, y);
    }

    private void drawRightText(Graphics2D g, String text, int x, int y,
            int size, Color color) {
        g.setFont(new Font("SansSerif", Font.PLAIN, size));
        g.setColor(color);
        g.drawString(text, x - g.getFontMetrics().stringWidth(text), y);
    }

    private void drawCenteredText(Graphics2D g, String text, int centerX, int y,
            int size, int style, Color color) {
        g.setFont(new Font("SansSerif", style, size));
        g.setColor(color);
        g.drawString(text, centerX - g.getFontMetrics().stringWidth(text) / 2, y);
    }

    private String ellipsize(Graphics2D g, String text, int maximumWidth) {
        FontMetrics metrics = g.getFontMetrics();
        if (metrics.stringWidth(text) <= maximumWidth) return text;
        String result = text;
        while (!result.isEmpty()
                && metrics.stringWidth(result + "…") > maximumWidth) {
            result = result.substring(0, result.length() - 1);
        }
        return result + "…";
    }

    private String text(boolean english, String spanish, String translated) {
        return english ? translated : spanish;
    }

}
