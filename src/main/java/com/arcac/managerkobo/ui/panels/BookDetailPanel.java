package com.arcac.managerkobo.ui.panels;

import com.arcac.managerkobo.model.Book;
import com.arcac.managerkobo.model.Bookmark;
import com.arcac.managerkobo.service.BookCoverService;
import com.arcac.managerkobo.ui.components.HighlightListPanel;
import com.arcac.managerkobo.ui.components.RoundedButton;
import com.arcac.managerkobo.ui.components.RoundedPanel;
import com.arcac.managerkobo.ui.theme.AppTheme;
import com.arcac.managerkobo.ui.util.IconLoader;
import com.arcac.managerkobo.ui.util.I18n;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import static com.arcac.managerkobo.util.ReadingFormat.duration;
import static com.arcac.managerkobo.util.ReadingFormat.textOr;
import static com.arcac.managerkobo.util.ReadingFormat.hasReliableReadingPace;
import static com.arcac.managerkobo.util.ReadingFormat.wordsPerMinute;

/** Vista de un libro, sus estadísticas y sus subrayados. */
public class BookDetailPanel extends JPanel {
    private final BookCoverService coverService = new BookCoverService();

    public BookDetailPanel(Book book, List<Bookmark> highlights,
            Runnable backAction) {
        setLayout(new BorderLayout(0, 14));
        setBackground(AppTheme.BACKGROUND);
        add(createHeader(backAction), BorderLayout.NORTH);

        JPanel content = new JPanel(new BorderLayout(0, 14));
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(0, 32, 28, 32));
        content.add(createOverview(book, highlights), BorderLayout.NORTH);
        content.add(new HighlightListPanel(highlights, false), BorderLayout.CENTER);
        add(content, BorderLayout.CENTER);
    }

    private JPanel createHeader(Runnable backAction) {
        JPanel header = new JPanel(new BorderLayout(16, 0));
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(22, 32, 8, 32));

        JButton back = new RoundedButton("", 28);
        back.setIcon(IconLoader.loadTinted("/icons/back.svg", 18, AppTheme.TEXT));
        back.setToolTipText("Volver");
        back.getAccessibleContext().setAccessibleName("Volver");
        back.setPreferredSize(new Dimension(42, 42));
        back.setForeground(AppTheme.TEXT);
        back.setBackground(AppTheme.PANEL_ALT);
        back.setFocusPainted(false);
        back.setBorder(new EmptyBorder(10, 14, 10, 14));
        back.addActionListener(event -> backAction.run());
        header.add(back, BorderLayout.WEST);
        header.add(label("Detalle del libro", 24, Font.BOLD, AppTheme.TEXT),
                BorderLayout.CENTER);
        return header;
    }

    /** Portada, información y métricas reunidas en una única tarjeta compacta. */
    private JPanel createOverview(Book book, List<Bookmark> highlights) {
        RoundedPanel card = new RoundedPanel(18, AppTheme.PANEL);
        card.setLayout(new BorderLayout(18, 14));
        card.setBorder(new EmptyBorder(15, 18, 15, 18));

        JPanel bookInfo = new JPanel(new BorderLayout(16, 0));
        bookInfo.setOpaque(false);
        bookInfo.add(createCover(book), BorderLayout.WEST);

        JPanel text = verticalPanel();
        text.add(label(textOr(book.getTitle(), "Sin título"),
                18, Font.BOLD, AppTheme.TEXT));
        text.add(Box.createVerticalStrut(3));
        text.add(label(textOr(book.getAuthor(), "Autor desconocido"),
                14, Font.BOLD, AppTheme.MUTED_TEXT));
        text.add(Box.createVerticalStrut(3));
        text.add(label(metadata(book), 11, Font.PLAIN, AppTheme.MUTED_TEXT));
        text.add(Box.createVerticalStrut(9));

        JProgressBar progress = new JProgressBar(0, 100);
        progress.setValue(book.getPercentRead());
        progress.setString(book.getPercentRead() + I18n.text("% leído"));
        progress.setStringPainted(true);
        progress.setForeground(AppTheme.PURPLE);
        progress.setBackground(AppTheme.BORDER);
        progress.setBorderPainted(false);
        text.add(progress);
        bookInfo.add(text, BorderLayout.CENTER);
        card.add(bookInfo, BorderLayout.NORTH);

        List<MetricValue> values = createMetricValues(book, highlights);
        JPanel metrics = new JPanel(new GridLayout(0, 4, 14, 9));
        metrics.setOpaque(false);
        values.forEach(value ->
                metrics.add(metric(value.value(), value.caption())));
        card.add(metrics, BorderLayout.CENTER);
        return card;
    }

    private List<MetricValue> createMetricValues(
            Book book, List<Bookmark> highlights) {
        int notes = (int) highlights.stream()
                .filter(Bookmark::hasUserNote)
                .count();
        double hours = book.getHoursRead();
        String density = hours <= 0
                ? "Sin datos"
                : String.format("%.1f / h", highlights.size() / hours);

        List<MetricValue> values = new ArrayList<>();
        values.add(new MetricValue(
                duration(book.getSecondsRead()), "Tiempo leído"));
        if (book.isInProgress() && book.getRestOfBookEstimate() > 0) {
            values.add(new MetricValue(
                    duration(book.getRestOfBookEstimate()),
                    "Tiempo restante estimado"));
        }
        values.add(new MetricValue(
                String.valueOf(highlights.size()), "Subrayados"));
        values.add(new MetricValue(String.valueOf(notes), "Notas"));
        values.add(new MetricValue(density, "Subrayados por hora"));
        values.add(new MetricValue(
                hasReliableReadingPace(book)
                        ? Math.round(wordsPerMinute(book)) + " ppm"
                        : "Sin datos",
                "Velocidad de lectura"));
        addDate(values, book.getDateLastRead(), "Última lectura");
        addDate(values, book.getLastTimeStartedReading(),
                "Último inicio registrado");
        addDate(values, book.getLastTimeFinishedReading(), "Finalización");

        return values;
    }

    private void addDate(List<MetricValue> values,
            String date, String caption) {
        if (hasText(date)) {
            values.add(new MetricValue(formatDate(date), caption));
        }
    }

    private JLabel createCover(Book book) {
        JLabel cover = new JLabel();
        cover.setHorizontalAlignment(SwingConstants.CENTER);
        cover.setVerticalAlignment(SwingConstants.CENTER);
        cover.setPreferredSize(new Dimension(64, 90));
        cover.setIcon(IconLoader.loadTinted(
                "/icons/libro.svg", 38, AppTheme.PURPLE));

        coverService.loadAsync(book, 64, 90, loadedCover -> {
            if (loadedCover != null) cover.setIcon(loadedCover);
        });
        return cover;
    }

    private JPanel metric(String value, String caption) {
        JPanel panel = verticalPanel();
        panel.add(label(value, 15, Font.BOLD, AppTheme.TEXT));
        panel.add(Box.createVerticalStrut(2));
        panel.add(label(caption, 11, Font.PLAIN, AppTheme.MUTED_TEXT));
        return panel;
    }

    private JPanel verticalPanel() {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        return panel;
    }

    private JLabel label(String text, int size, int style, Color color) {
        JLabel result = new JLabel(text);
        result.setFont(AppTheme.font(style, size));
        result.setForeground(color);
        return result;
    }

    private String metadata(Book book) {
        String publisher = textOr(book.getPublisher(), "Editorial desconocida");
        String language = textOr(book.getLanguage(), "Idioma desconocido")
                .toUpperCase();
        return publisher + " · " + language + " · " + status(book);
    }

    private String status(Book book) {
        if (book.isFinished()) {
            return I18n.text("Terminado");
        }
        if (book.isInProgress()) {
            return I18n.text("Leyendo");
        }
        return I18n.text("Sin empezar");
    }

    private String formatDate(String value) {
        return value.length() >= 10 ? value.substring(0, 10) : value;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private record MetricValue(String value, String caption) { }
}
