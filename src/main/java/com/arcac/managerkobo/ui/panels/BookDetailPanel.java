package com.arcac.managerkobo.ui.panels;

import com.arcac.managerkobo.model.Book;
import com.arcac.managerkobo.model.Bookmark;
import com.arcac.managerkobo.ui.components.HighlightListPanel;
import com.arcac.managerkobo.ui.components.RoundedPanel;
import com.arcac.managerkobo.ui.theme.AppTheme;
import com.arcac.managerkobo.ui.util.IconLoader;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.List;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.border.EmptyBorder;

/** Vista de un libro, sus estadísticas y sus subrayados. */
public class BookDetailPanel extends JPanel {

    public BookDetailPanel(Book book, List<Bookmark> highlights,
            Runnable backAction) {
        setLayout(new BorderLayout(0, 16));
        setBackground(AppTheme.BACKGROUND);
        add(createHeader(book, backAction), BorderLayout.NORTH);

        JPanel content = new JPanel(new BorderLayout(0, 18));
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(0, 32, 28, 32));
        content.add(createOverview(book, highlights), BorderLayout.NORTH);
        content.add(new HighlightListPanel(highlights, false), BorderLayout.CENTER);
        add(content, BorderLayout.CENTER);
    }

    private JPanel createOverview(Book book, List<Bookmark> highlights) {
        JPanel overview = new JPanel();
        overview.setOpaque(false);
        overview.setLayout(new BoxLayout(overview, BoxLayout.Y_AXIS));
        overview.add(createSummary(book));
        overview.add(Box.createVerticalStrut(12));
        overview.add(createBookStatistics(book, highlights));
        return overview;
    }

    private JPanel createHeader(Book book, Runnable backAction) {
        JPanel header = new JPanel(new BorderLayout(16, 0));
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(25, 32, 10, 32));

        JButton back = new JButton();
        back.setIcon(IconLoader.loadTinted("/icons/back.png", 18, AppTheme.TEXT));
        back.setToolTipText("Volver");
        back.getAccessibleContext().setAccessibleName("Volver");
        back.setPreferredSize(new Dimension(42, 42));
        back.setForeground(AppTheme.TEXT);
        back.setBackground(AppTheme.PANEL_ALT);
        back.setFocusPainted(false);
        back.setBorder(new EmptyBorder(10, 14, 10, 14));
        back.addActionListener(event -> backAction.run());
        header.add(back, BorderLayout.WEST);

        JLabel title = label(fallback(book.getTitle(), "Sin título"),
                24, Font.BOLD, AppTheme.TEXT);
        header.add(title, BorderLayout.CENTER);
        return header;
    }

    private JPanel createSummary(Book book) {
        RoundedPanel card = new RoundedPanel(18, AppTheme.PANEL);
        card.setLayout(new BorderLayout(22, 12));
        card.setBorder(new EmptyBorder(20, 22, 20, 22));

        JLabel cover = new JLabel();
        cover.setIcon(IconLoader.loadTinted(
                "/icons/libro.png", 46, AppTheme.PURPLE));
        card.add(cover, BorderLayout.WEST);

        JPanel info = verticalPanel();
        info.add(label(fallback(book.getAuthor(), "Autor desconocido"),
                16, Font.BOLD, AppTheme.TEXT));
        info.add(Box.createVerticalStrut(5));
        info.add(label(metadata(book), 12, Font.PLAIN, AppTheme.MUTED_TEXT));
        info.add(Box.createVerticalStrut(13));

        JProgressBar progress = new JProgressBar(0, 100);
        progress.setValue(book.getPercentRead());
        progress.setString(book.getPercentRead() + "% leído");
        progress.setStringPainted(true);
        progress.setForeground(AppTheme.PURPLE);
        progress.setBackground(AppTheme.BORDER);
        progress.setBorderPainted(false);
        info.add(progress);
        card.add(info, BorderLayout.CENTER);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 112));
        card.setAlignmentX(LEFT_ALIGNMENT);
        return card;
    }

    private JPanel createBookStatistics(Book book, List<Bookmark> highlights) {
        int notes = (int) highlights.stream()
                .filter(Bookmark::hasUserNote)
                .count();
        double hours = book.getHoursRead();
        String density = hours <= 0
                ? "Sin datos"
                : String.format("%.1f / h", highlights.size() / hours);

        RoundedPanel card = new RoundedPanel(18, AppTheme.PANEL_ALT);
        card.setLayout(new GridLayout(1, 5, 12, 0));
        card.setBorder(new EmptyBorder(15, 20, 15, 20));
        card.add(metric(formatTime(book.getSecondsRead()), "Tiempo leído"));
        card.add(metric(String.valueOf(book.getTimesStartedReading()), "Sesiones"));
        card.add(metric(String.valueOf(highlights.size()), "Subrayados"));
        card.add(metric(String.valueOf(notes), "Notas"));
        card.add(metric(density, "Subrayados por hora"));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 82));
        card.setAlignmentX(LEFT_ALIGNMENT);
        return card;
    }

    private JPanel metric(String value, String caption) {
        JPanel panel = verticalPanel();
        panel.add(label(value, 16, Font.BOLD, AppTheme.TEXT));
        panel.add(Box.createVerticalStrut(3));
        panel.add(label(caption, 11, Font.PLAIN, AppTheme.MUTED_TEXT));
        return panel;
    }

    private JPanel verticalPanel() {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        return panel;
    }

    private JLabel label(String text, int size, int style,
            java.awt.Color color) {
        JLabel result = new JLabel(text);
        result.setFont(AppTheme.font(style, size));
        result.setForeground(color);
        return result;
    }

    private String metadata(Book book) {
        String publisher = fallback(book.getPublisher(), "Editorial desconocida");
        String language = fallback(book.getLanguage(), "Idioma desconocido")
                .toUpperCase();
        return publisher + " · " + language + " · " + status(book);
    }

    private String status(Book book) {
        if (book.isFinished()) {
            return "Terminado";
        }
        if (book.isInProgress()) {
            return "Leyendo";
        }
        return "Sin empezar";
    }

    private String formatTime(int seconds) {
        return (seconds / 3600) + " h " + ((seconds % 3600) / 60) + " min";
    }

    private String fallback(String value, String alternative) {
        return value == null || value.isBlank() ? alternative : value;
    }
}
