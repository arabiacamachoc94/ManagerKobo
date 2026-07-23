package com.arcac.managerkobo.ui.panels;

import com.arcac.managerkobo.model.Book;
import com.arcac.managerkobo.service.LibraryStatisticsService;
import com.arcac.managerkobo.service.ReadingStatistics;
import com.arcac.managerkobo.ui.components.RoundedPanel;
import com.arcac.managerkobo.ui.theme.AppTheme;
import com.arcac.managerkobo.ui.util.IconLoader;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.List;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.EmptyBorder;

/** Pantalla de resumen principal: estadísticas, lectura actual y libros en progreso. */
public class DashboardPanel extends JPanel {
    private final List<Book> books;
    private final ReadingStatistics statistics;
    private final Runnable syncAction;
    private final JButton syncButton = new JButton("Sincronizar Kobo");


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
        JPanel metrics = responsiveGrid(4, 165, 105, 14);
        metrics.add(metric("/icons/libro.png", "Total libros", String.valueOf(statistics.totalBooks()), AppTheme.BLUE));
        metrics.add(metric("✓", "Terminados", String.valueOf(statistics.finishedBooks()), AppTheme.GREEN));
        metrics.add(metric("/icons/tiempo.png", "Horas leídas", formatHours(statistics.totalHoursRead()), AppTheme.PURPLE));
        metrics.add(metric("/icons/lapiz.png", "Subrayados", String.valueOf(statistics.totalHighlights()), AppTheme.ORANGE));
        body.add(metrics);
        body.add(Box.createVerticalStrut(22));

        body.add(label("Más estadísticas", 18, Font.BOLD, AppTheme.TEXT));
        body.add(Box.createVerticalStrut(12));
        JPanel extraMetrics = responsiveGrid(4, 145, 82, 14);
        extraMetrics.add(compactMetric("En progreso", String.valueOf(statistics.readingBooks()), AppTheme.PURPLE));
        extraMetrics.add(compactMetric("Por leer", String.valueOf(statistics.unreadBooks()), AppTheme.BLUE));
        extraMetrics.add(compactMetric("Finalización", formatPercent(statistics.completionRate()), AppTheme.GREEN));
        extraMetrics.add(compactMetric("Progreso medio", formatPercent(statistics.averageProgress()), AppTheme.ORANGE));
        body.add(extraMetrics);
        body.add(Box.createVerticalStrut(24));

        body.add(label("Lectura actual", 18, Font.BOLD, AppTheme.TEXT));
        body.add(Box.createVerticalStrut(12));
        Book current = books.stream().filter(b -> b.getPercentRead() > 0 && b.getPercentRead() < 100).findFirst().orElse(null);
        body.add(bookCard(current));
        body.add(Box.createVerticalStrut(24));

        body.add(label("Resumen de libros", 18, Font.BOLD, AppTheme.TEXT));
        body.add(Box.createVerticalStrut(12));
        JPanel secondary = responsiveGrid(3, 215, 115, 14);
        secondary.add(summaryCard("Último libro leído", statistics.lastReadBook(),
                statistics.lastReadBook() == null ? "" : formatDate(statistics.lastReadBook().getDateLastRead()), AppTheme.BLUE));
        secondary.add(summaryCard("Libro con más tiempo", statistics.mostReadBook(),
                statistics.mostReadBook() == null ? "" : formatHours(statistics.mostReadBook().getHoursRead()), AppTheme.ORANGE));
        secondary.add(summaryCard("Libro más subrayado", statistics.mostHighlightedBook(),
                statistics.mostHighlightedCount() + " subrayados", AppTheme.GREEN));
        body.add(secondary);
        body.add(Box.createVerticalStrut(12));

        JScrollPane scroll = new JScrollPane(body);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUnitIncrement(18);
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(scroll, BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel summaryCard(String heading, Book book, String valueDetail, Color accent) {
        RoundedPanel card = new RoundedPanel(18, AppTheme.PANEL);
        card.setLayout(new BorderLayout(14, 0));
        card.setBorder(new EmptyBorder(17, 19, 17, 19));
        JLabel bookIcon = label("", 30, Font.PLAIN, accent);
        bookIcon.setIcon(IconLoader.loadTinted("/icons/libro.png", 30, accent));
        card.add(bookIcon, BorderLayout.WEST);
        JPanel text = transparentVertical();
        text.add(label(heading, 12, Font.BOLD, AppTheme.MUTED_TEXT));
        text.add(Box.createVerticalStrut(5));
        text.add(label(book == null ? "Sin datos" : fallback(book.getTitle(), "Sin título"),
                15, Font.BOLD, AppTheme.TEXT));
        text.add(Box.createVerticalStrut(3));
        String detail = book == null ? "" : fallback(book.getAuthor(), "Autor desconocido")
                + (valueDetail == null || valueDetail.isBlank() ? "" : " · " + valueDetail);
        text.add(label(detail, 12, Font.PLAIN, AppTheme.MUTED_TEXT));
        card.add(text, BorderLayout.CENTER);
        return card;
    }

    private JPanel compactMetric(String title, String value, Color accent) {
        RoundedPanel card = new RoundedPanel(16, AppTheme.PANEL);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(13, 17, 13, 17));
        JPanel text = transparentVertical();
        text.add(label(value, 20, Font.BOLD, accent));
        text.add(Box.createVerticalStrut(3));
        text.add(label(title, 12, Font.PLAIN, AppTheme.MUTED_TEXT));
        card.add(text, BorderLayout.CENTER);
        return card;
    }

    private JPanel metric(String icon, String title, String value, Color accent) {
        RoundedPanel card = new RoundedPanel(18, AppTheme.PANEL);
        card.setLayout(new BorderLayout(14, 0));
        card.setBorder(new EmptyBorder(18, 18, 18, 18));
        JLabel iconLabel = label("", 28, Font.BOLD, accent);
        if (icon.startsWith("/")) {
            iconLabel.setIcon(IconLoader.loadTinted(icon, 28, accent));
        } else {
            iconLabel.setText(icon);
        }
        card.add(iconLabel, BorderLayout.WEST);
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
        JLabel bookIcon = label("", 42, Font.PLAIN, AppTheme.PURPLE);
        bookIcon.setIcon(IconLoader.loadTinted("/icons/libro.png", 42, AppTheme.PURPLE));
        card.add(bookIcon, BorderLayout.WEST);
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

    /**
     * Grid que reduce el número de columnas cuando no existe ancho suficiente.
     * La altura se recalcula para que BoxLayout y el scroll respeten las filas nuevas.
     */
    private JPanel responsiveGrid(int maxColumns, int minimumCardWidth,
                                  int rowHeight, int gap) {
        JPanel panel = new JPanel(new GridLayout(0, maxColumns, gap, gap));
        panel.setOpaque(false);
        panel.putClientProperty("responsiveColumns", maxColumns);
        panel.setPreferredSize(new Dimension(maxColumns * minimumCardWidth, rowHeight));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, rowHeight));
        panel.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent event) {
                updateResponsiveGrid(panel, maxColumns, minimumCardWidth, rowHeight, gap);
            }
        });
        return panel;
    }

    private void updateResponsiveGrid(JPanel panel, int maxColumns,
                                      int minimumCardWidth, int rowHeight, int gap) {
        int availableWidth = Math.max(minimumCardWidth, panel.getWidth());
        int columns = Math.max(1, Math.min(maxColumns,
                (availableWidth + gap) / (minimumCardWidth + gap)));
        int previousColumns = (int) panel.getClientProperty("responsiveColumns");
        int rows = Math.max(1, (int) Math.ceil(panel.getComponentCount() / (double) columns));
        int requiredHeight = rows * rowHeight + (rows - 1) * gap;

        if (columns != previousColumns || panel.getPreferredSize().height != requiredHeight) {
            GridLayout layout = (GridLayout) panel.getLayout();
            layout.setColumns(columns);
            panel.putClientProperty("responsiveColumns", columns);
            panel.setPreferredSize(new Dimension(availableWidth, requiredHeight));
            panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, requiredHeight));
            panel.revalidate();
            if (panel.getParent() != null) panel.getParent().revalidate();
        }
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

    private String formatPercent(double value) {
        return Math.round(value) + "%";
    }

    private String formatDate(String value) {
        if (value == null || value.isBlank()) return "Sin fecha";
        return value.length() >= 10 ? value.substring(0, 10) : value;
    }
}
