package com.arcac.managerkobo.ui.panels;

import com.arcac.managerkobo.model.Book;
import com.arcac.managerkobo.service.ReadingStatistics;
import com.arcac.managerkobo.ui.components.RoundedPanel;
import com.arcac.managerkobo.ui.theme.AppTheme;
import com.arcac.managerkobo.ui.util.IconLoader;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

/** Resumen principal de la actividad de lectura. */
public class DashboardPanel extends JPanel {
    private final ReadingStatistics statistics;
    private final Runnable syncAction;
    private final Consumer<Book> openBookAction;
    private final LocalDateTime lastSynchronization;
    private final JButton syncButton = new JButton("Sincronizar Kobo");

    public DashboardPanel(List<Book> books, ReadingStatistics statistics,
            Runnable syncAction, Consumer<Book> openBookAction,
            LocalDateTime lastSynchronization) {
        this.statistics = statistics;
        this.syncAction = syncAction;
        this.openBookAction = openBookAction;
        this.lastSynchronization = lastSynchronization;
        setLayout(new BorderLayout());
        setBackground(AppTheme.BACKGROUND);
        add(createHeader(), BorderLayout.NORTH);
        add(createContent(), BorderLayout.CENTER);
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout(20, 0));
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(30, 32, 22, 32));

        JPanel titles = verticalPanel();
        titles.add(label("Dashboard", 29, Font.BOLD, AppTheme.TEXT));
        titles.add(Box.createVerticalStrut(5));
        titles.add(label(librarySummary(), 14, Font.PLAIN, AppTheme.MUTED_TEXT));
        titles.add(Box.createVerticalStrut(4));
        titles.add(label(lastSynchronizationText(),
                12, Font.PLAIN, AppTheme.MUTED_TEXT));
        header.add(titles, BorderLayout.CENTER);

        stylePrimaryButton(syncButton);
        syncButton.addActionListener(event -> syncAction.run());
        header.add(syncButton, BorderLayout.EAST);
        return header;
    }

    public void setSyncing(boolean syncing) {
        syncButton.setEnabled(!syncing);
        syncButton.setText(syncing ? "Sincronizando..." : "Sincronizar Kobo");
    }

    private JPanel createContent() {
        ScrollableVerticalPanel body = new ScrollableVerticalPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBorder(new EmptyBorder(4, 32, 32, 32));

        Book currentBook = findCurrentBook();
        JPanel hero = responsiveGrid(2, 360, 245, 16);
        hero.add(createCurrentReading(currentBook));
        hero.add(createLibrarySummary());
        body.add(hero);
        body.add(Box.createVerticalStrut(22));

        body.add(sectionTitle("Estado de la biblioteca"));
        body.add(Box.createVerticalStrut(11));
        body.add(fullWidth(createLibraryStatus(), 150));
        body.add(Box.createVerticalStrut(24));

        body.add(sectionTitle("Explora tus estadísticas"));
        body.add(Box.createVerticalStrut(12));
        AdvancedStatisticsPanel advancedPanel =
                new AdvancedStatisticsPanel(statistics, false);
        advancedPanel.setPreferredSize(new Dimension(600, 384));
        advancedPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 384));
        body.add(advancedPanel);
        body.add(Box.createVerticalStrut(24));

        body.add(sectionTitle("Datos destacados"));
        body.add(Box.createVerticalStrut(11));
        body.add(fullWidth(createInsights(), 150));

        JScrollPane scroll = new JScrollPane(body);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUnitIncrement(18);
        return wrap(scroll);
    }

    private JPanel createCurrentReading(Book book) {
        RoundedPanel card = new RoundedPanel(20, AppTheme.PANEL);
        card.setLayout(new BorderLayout(18, 16));
        card.setBorder(new EmptyBorder(21, 22, 20, 22));

        JPanel heading = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        heading.setOpaque(false);
        JLabel icon = label("", 34, Font.PLAIN, AppTheme.PURPLE);
        icon.setIcon(IconLoader.loadTinted("/icons/libro.png", 34, AppTheme.PURPLE));
        heading.add(icon);
        heading.add(label("LECTURA ACTUAL", 12, Font.BOLD, AppTheme.PURPLE));
        card.add(heading, BorderLayout.NORTH);

        JPanel content = verticalPanel();
        if (book == null) {
            content.add(label("No hay libros en progreso", 19, Font.BOLD, AppTheme.TEXT));
            content.add(Box.createVerticalStrut(7));
            content.add(label("Empieza un libro y sincroniza el Kobo para verlo aquí.",
                    13, Font.PLAIN, AppTheme.MUTED_TEXT));
        } else {
            content.add(label(fallback(book.getTitle(), "Sin título"),
                    19, Font.BOLD, AppTheme.TEXT));
            content.add(Box.createVerticalStrut(5));
            content.add(label(fallback(book.getAuthor(), "Autor desconocido"),
                    13, Font.PLAIN, AppTheme.MUTED_TEXT));
            content.add(Box.createVerticalStrut(15));
            content.add(progressBar(book.getPercentRead(), AppTheme.PURPLE,
                    book.getPercentRead() + "% leído"));
            content.add(Box.createVerticalStrut(13));
            content.add(label("Tiempo leído: " + formatTime(book.getSecondsRead())
                            + "  ·  " + highlightCount(book) + " subrayados",
                    12, Font.PLAIN, AppTheme.MUTED_TEXT));
        }
        card.add(content, BorderLayout.CENTER);

        if (book != null) {
            JButton openButton = new JButton("Abrir libro");
            styleSecondaryButton(openButton);
            openButton.addActionListener(event -> openBookAction.accept(book));
            JPanel footer = new JPanel(new BorderLayout());
            footer.setOpaque(false);
            footer.add(openButton, BorderLayout.EAST);
            card.add(footer, BorderLayout.SOUTH);
        }
        return card;
    }

    private JPanel createLibrarySummary() {
        RoundedPanel card = new RoundedPanel(20, AppTheme.PANEL);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(21, 23, 20, 23));
        JLabel title = label("Tu biblioteca de un vistazo",
                17, Font.BOLD, AppTheme.TEXT);
        JPanel titleRow = new JPanel(new BorderLayout());
        titleRow.setOpaque(false);
        
        titleRow.add(title, BorderLayout.WEST);
        titleRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));
        titleRow.setAlignmentX(LEFT_ALIGNMENT);
        card.add(titleRow);
        card.add(Box.createVerticalStrut(15));
        card.add(summaryRow("Biblioteca", statistics.totalBooks() + " libros", AppTheme.BLUE));
        card.add(summaryRow("Terminados", String.valueOf(statistics.finishedBooks()), AppTheme.GREEN));
        card.add(summaryRow("Tiempo leído", formatTime(statistics.totalSecondsRead()), AppTheme.PURPLE));
        card.add(summaryRow("Subrayados", String.valueOf(statistics.totalHighlights()), AppTheme.ORANGE));
        card.add(summaryRow("Notas propias", String.valueOf(statistics.highlightsWithNote()), AppTheme.BLUE));
        return card;
    }

    private JPanel summaryRow(String name, String value, Color accent) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setOpaque(false);
        row.setBorder(new EmptyBorder(6, 0, 6, 0));
        JLabel nameLabel = label(name, 13, Font.PLAIN, AppTheme.MUTED_TEXT);
        nameLabel.setBorder(new EmptyBorder(0, 9, 0, 0));
        row.add(nameLabel, BorderLayout.WEST);
        row.add(label(value, 14, Font.BOLD, accent), BorderLayout.EAST);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        return row;
    }

    private JPanel createLibraryStatus() {
        RoundedPanel panel = new RoundedPanel(18, AppTheme.PANEL);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(17, 20, 17, 20));
        panel.add(statusRow("Terminados", statistics.finishedBooks(), AppTheme.GREEN));
        panel.add(Box.createVerticalStrut(12));
        panel.add(statusRow("Leyendo", statistics.readingBooks(), AppTheme.PURPLE));
        panel.add(Box.createVerticalStrut(12));
        panel.add(statusRow("Sin empezar", statistics.unreadBooks(), AppTheme.BLUE));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));
        panel.setAlignmentX(LEFT_ALIGNMENT);
        return panel;
    }

    private JPanel statusRow(String name, int amount, Color color) {
        JPanel row = new JPanel(new BorderLayout(14, 0));
        row.setOpaque(false);
        JLabel nameLabel = label(name, 13, Font.BOLD, AppTheme.TEXT);
        nameLabel.setPreferredSize(new Dimension(100, 24));
        row.add(nameLabel, BorderLayout.WEST);
        int total = Math.max(1, statistics.totalBooks());
        int percent = (int) Math.round(amount * 100.0 / total);
        row.add(progressBar(amount, total, color, amount + " · " + percent + "%"),
                BorderLayout.CENTER);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));
        return row;
    }

    private JPanel createInsights() {
        RoundedPanel panel = new RoundedPanel(18, AppTheme.PANEL_ALT);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(17, 21, 17, 21));

        Map.Entry<String, Long> topAuthor = statistics.readingSecondsByAuthor()
                .entrySet().stream().findFirst().orElse(null);
        addInsight(panel, topAuthor == null
                ? "Aún no hay suficiente información sobre tus autores."
                : "Tu autor más leído es " + topAuthor.getKey() + ", con "
                        + formatTime(topAuthor.getValue()) + ".");

        Book highlighted = statistics.mostHighlightedBook();
        addInsight(panel, highlighted == null
                ? "Todavía no hay libros con subrayados."
                : "El libro que más has subrayado es «"
                        + fallback(highlighted.getTitle(), "Sin título") + "» ("
                        + statistics.mostHighlightedCount() + ").");

        addInsight(panel, "Dedicas una media de "
                + formatTime(statistics.averageSecondsPerFinishedBook())
                + " a cada libro terminado.");
        addInsight(panel, statistics.readingBooks() == 1
                ? "Tienes 1 lectura activa en este momento."
                : "Tienes " + statistics.readingBooks()
                        + " lecturas activas en este momento.");
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));
        return panel;
    }

    private void addInsight(JPanel panel, String text) {
        JLabel item = label("•  " + text, 13, Font.PLAIN, AppTheme.TEXT);
        item.setBorder(new EmptyBorder(4, 0, 4, 0));
        item.setMaximumSize(new Dimension(Integer.MAX_VALUE, 29));
        panel.add(item);
    }

    private JProgressBar progressBar(int value, Color color, String text) {
        return progressBar(value, 100, color, text);
    }

    private JProgressBar progressBar(int value, int maximum, Color color, String text) {
        JProgressBar progress = new JProgressBar(0, maximum);
        progress.setValue(value);
        progress.setForeground(color);
        progress.setBackground(AppTheme.BORDER);
        progress.setBorderPainted(false);
        progress.setStringPainted(true);
        progress.setString(text);
        progress.setFont(AppTheme.font(Font.BOLD, 11));
        progress.setForeground(color);
        progress.setPreferredSize(new Dimension(200, 18));
        progress.setMaximumSize(new Dimension(Integer.MAX_VALUE, 18));
        return progress;
    }

    private Book findCurrentBook() {
        Comparator<String> dates =
                Comparator.nullsFirst(Comparator.naturalOrder());
        return statistics.inProgressBooks().stream()
                .max(Comparator.comparing(Book::getDateLastRead, dates))
                .orElse(null);
    }

    private int highlightCount(Book book) {
        return statistics.highlightsByBook().getOrDefault(book, 0);
    }

    private String librarySummary() {
        return statistics.totalBooks() + " libros · "
                + statistics.readingBooks() + " en progreso · "
                + statistics.finishedBooks() + " terminados";
    }

    private String lastSynchronizationText() {
        if (lastSynchronization == null) {
            return "Última sincronización: todavía no disponible";
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy · HH:mm");
        return "Última sincronización: " + lastSynchronization.format(formatter);
    }

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
        int rows = Math.max(1,
                (int) Math.ceil(panel.getComponentCount() / (double) columns));
        int requiredHeight = rows * rowHeight + (rows - 1) * gap;
        if (columns != previousColumns
                || panel.getPreferredSize().height != requiredHeight) {
            ((GridLayout) panel.getLayout()).setColumns(columns);
            panel.putClientProperty("responsiveColumns", columns);
            panel.setPreferredSize(new Dimension(availableWidth, requiredHeight));
            panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, requiredHeight));
            panel.revalidate();
        }
    }

    private void stylePrimaryButton(JButton button) {
        button.setBackground(AppTheme.GREEN);
        button.setForeground(Color.WHITE);
        button.setFont(AppTheme.font(Font.BOLD, 13));
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(11, 17, 11, 17));
    }

    private void styleSecondaryButton(JButton button) {
        button.setBackground(AppTheme.PURPLE);
        button.setForeground(Color.WHITE);
        button.setFont(AppTheme.font(Font.BOLD, 12));
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(8, 14, 8, 14));
    }

    private JPanel verticalPanel() {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        return panel;
    }

    private JPanel wrap(JScrollPane scroll) {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(scroll, BorderLayout.CENTER);
        return wrapper;
    }

    /**
     * Evita que BoxLayout reduzca una tarjeta a su ancho preferido.
     */
    private JPanel fullWidth(JPanel content, int height) {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(content, BorderLayout.CENTER);
        wrapper.setPreferredSize(new Dimension(600, height));
        wrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, height));
        wrapper.setAlignmentX(LEFT_ALIGNMENT);
        return wrapper;
    }

    private JLabel sectionTitle(String text) {
        return label(text, 18, Font.BOLD, AppTheme.TEXT);
    }

    private JLabel label(String text, int size, int style, Color color) {
        JLabel result = new JLabel(text);
        result.setFont(AppTheme.font(style, size));
        result.setForeground(color);
        return result;
    }

    private String fallback(String value, String alternative) {
        return value == null || value.isBlank() ? alternative : value;
    }

    private String formatTime(long seconds) {
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        return hours + " h " + minutes + " min";
    }

    private static class ScrollableVerticalPanel extends JPanel implements Scrollable {
        @Override
        public void doLayout() {
            super.doLayout();
            Insets insets = getInsets();
            int availableWidth = Math.max(0,
                    getWidth() - insets.left - insets.right);

            /*
             * BoxLayout puede conservar el ancho preferido de algunos paneles.
             * Aquí todas las secciones reciben explícitamente el ancho útil.
             */
            for (Component component : getComponents()) {
                component.setBounds(insets.left, component.getY(),
                        availableWidth, component.getHeight());
            }
        }

        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        @Override
        public int getScrollableUnitIncrement(
                Rectangle visibleRect, int orientation, int direction) {
            return 18;
        }

        @Override
        public int getScrollableBlockIncrement(
                Rectangle visibleRect, int orientation, int direction) {
            return Math.max(80, visibleRect.height - 40);
        }

        @Override
        public boolean getScrollableTracksViewportWidth() {
            return true;
        }

        @Override
        public boolean getScrollableTracksViewportHeight() {
            return false;
        }
    }
}
