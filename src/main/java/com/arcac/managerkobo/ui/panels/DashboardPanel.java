package com.arcac.managerkobo.ui.panels;

import com.arcac.managerkobo.model.Book;
import com.arcac.managerkobo.service.LibraryStatisticsService;
import com.arcac.managerkobo.service.ReadingStatistics;
import com.arcac.managerkobo.ui.components.RoundedPanel;
import com.arcac.managerkobo.ui.theme.AppTheme;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.List;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JButton;
import javax.swing.border.EmptyBorder;

/** Pantalla de resumen principal: estadísticas, lectura actual y libros en progreso. */
public class DashboardPanel extends JPanel {
    private final List<Book> books;
    private final ReadingStatistics statistics;
    private final Runnable syncAction;
    private final JButton syncButton = new JButton("Sincronizar Kobo");

    public DashboardPanel(List<Book> books) {
        this(books, new LibraryStatisticsService().calculate(books), () -> { });
    }

    public DashboardPanel(List<Book> books, ReadingStatistics statistics) {
        this(books, statistics, () -> { });
    }

    public DashboardPanel(List<Book> books, ReadingStatistics statistics, Runnable syncAction) {
        this.books = books;
        this.statistics = statistics;
        this.syncAction = syncAction;
        setLayout(new BorderLayout());
        setBackground(AppTheme.BACKGROUND);
        add(createHeader(), BorderLayout.NORTH);
        add(createContent(), BorderLayout.CENTER);
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(30, 32, 25, 32));
        JPanel titles = transparentVertical();
        titles.add(label("Dashboard", 29, Font.BOLD, AppTheme.TEXT));
        titles.add(Box.createVerticalStrut(5));
        titles.add(label("Resumen de tu actividad de lectura", 14, Font.PLAIN, AppTheme.MUTED_TEXT));
        header.add(titles, BorderLayout.WEST);

        syncButton.setBackground(AppTheme.GREEN);
        syncButton.setForeground(Color.WHITE);
        syncButton.setFont(AppTheme.font(Font.BOLD, 13));
        syncButton.setFocusPainted(false);
        syncButton.setBorder(new EmptyBorder(11, 17, 11, 17));
        syncButton.addActionListener(event -> syncAction.run());
        header.add(syncButton, BorderLayout.EAST);
        return header;
    }

    public void setSyncing(boolean syncing) {
        syncButton.setEnabled(!syncing);
        syncButton.setText(syncing ? "Sincronizando..." : "Sincronizar Kobo");
    }

    private JPanel createContent() {
        JPanel body = transparentVertical();
        body.setBorder(new EmptyBorder(5, 32, 30, 32));
        JPanel metrics = new JPanel(new GridLayout(1, 4, 14, 0));
        metrics.setOpaque(false);
        metrics.setMaximumSize(new Dimension(Integer.MAX_VALUE, 105));
        metrics.add(metric("▤", "Total libros", String.valueOf(statistics.totalBooks()), AppTheme.BLUE));
        metrics.add(metric("✓", "Terminados", String.valueOf(statistics.finishedBooks()), AppTheme.GREEN));
        metrics.add(metric("◷", "Horas leídas", formatHours(statistics.totalHoursRead()), AppTheme.PURPLE));
        metrics.add(metric("✎", "Subrayados", String.valueOf(statistics.totalHighlights()), AppTheme.ORANGE));
        body.add(metrics);
        body.add(Box.createVerticalStrut(28));
        body.add(label("Lectura actual", 18, Font.BOLD, AppTheme.TEXT));
        body.add(Box.createVerticalStrut(12));
        Book current = books.stream().filter(b -> b.getPercentRead() > 0 && b.getPercentRead() < 100).findFirst().orElse(null);
        body.add(bookCard(current));
        body.add(Box.createVerticalStrut(24));

        JPanel secondary = new JPanel(new GridLayout(1, 2, 14, 0));
        secondary.setOpaque(false);
        secondary.setMaximumSize(new Dimension(Integer.MAX_VALUE, 115));
        secondary.add(summaryCard("Último libro leído", statistics.lastReadBook(), AppTheme.BLUE));
        secondary.add(summaryCard("Libro con más tiempo", statistics.mostReadBook(), AppTheme.ORANGE));
        body.add(secondary);
        body.add(Box.createVerticalGlue());
        return body;
    }

    private JPanel summaryCard(String heading, Book book, Color accent) {
        RoundedPanel card = new RoundedPanel(18, AppTheme.PANEL);
        card.setLayout(new BorderLayout(14, 0));
        card.setBorder(new EmptyBorder(17, 19, 17, 19));
        card.add(label("▥", 30, Font.PLAIN, accent), BorderLayout.WEST);
        JPanel text = transparentVertical();
        text.add(label(heading, 12, Font.BOLD, AppTheme.MUTED_TEXT));
        text.add(Box.createVerticalStrut(5));
        text.add(label(book == null ? "Sin datos" : fallback(book.getTitle(), "Sin título"),
                15, Font.BOLD, AppTheme.TEXT));
        text.add(Box.createVerticalStrut(3));
        String detail = book == null ? "" : fallback(book.getAuthor(), "Autor desconocido")
                + " · " + formatHours(book.getHoursRead());
        text.add(label(detail, 12, Font.PLAIN, AppTheme.MUTED_TEXT));
        card.add(text, BorderLayout.CENTER);
        return card;
    }

    private JPanel metric(String icon, String title, String value, Color accent) {
        RoundedPanel card = new RoundedPanel(18, AppTheme.PANEL);
        card.setLayout(new BorderLayout(14, 0));
        card.setBorder(new EmptyBorder(18, 18, 18, 18));
        card.add(label(icon, 28, Font.BOLD, accent), BorderLayout.WEST);
        JPanel text = transparentVertical();
        text.add(label(title, 12, Font.PLAIN, AppTheme.MUTED_TEXT));
        text.add(label(value, 25, Font.BOLD, AppTheme.TEXT));
        card.add(text);
        return card;
    }

    private JPanel bookCard(Book book) {
        RoundedPanel card = new RoundedPanel(18, AppTheme.PANEL);
        card.setLayout(new BorderLayout(18, 8));
        card.setBorder(new EmptyBorder(20, 22, 20, 22));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 125));
        card.add(label("▥", 42, Font.PLAIN, AppTheme.PURPLE), BorderLayout.WEST);
        JPanel text = transparentVertical();
        text.add(label(book == null ? "No hay libros en progreso" : fallback(book.getTitle(), "Sin título"), 17, Font.BOLD, AppTheme.TEXT));
        text.add(Box.createVerticalStrut(6));
        text.add(label(book == null ? "Conecta o sincroniza tu Kobo" : fallback(book.getAuthor(), "Autor desconocido"), 13, Font.PLAIN, AppTheme.MUTED_TEXT));
        text.add(Box.createVerticalStrut(12));
        JProgressBar progress = new JProgressBar(0, 100);
        progress.setValue(book == null ? 0 : book.getPercentRead());
        progress.setForeground(AppTheme.PURPLE);
        progress.setBackground(AppTheme.BORDER);
        progress.setBorderPainted(false);
        progress.setMaximumSize(new Dimension(Integer.MAX_VALUE, 7));
        text.add(progress);
        card.add(text);
        return card;
    }

    private JPanel transparentVertical() {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        return panel;
    }

    private JLabel label(String text, int size, int style, Color color) {
        JLabel label = new JLabel(text);
        label.setFont(AppTheme.font(style, size));
        label.setForeground(color);
        return label;
    }

    private String fallback(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private String formatHours(double hours) {
        int wholeHours = (int) hours;
        int minutes = (int) Math.round((hours - wholeHours) * 60);
        if (minutes == 60) {
            wholeHours++;
            minutes = 0;
        }
        return wholeHours + " h " + minutes + " min";
    }
}
