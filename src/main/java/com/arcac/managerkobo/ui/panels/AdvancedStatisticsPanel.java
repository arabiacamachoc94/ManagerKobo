package com.arcac.managerkobo.ui.panels;

import com.arcac.managerkobo.model.Book;
import com.arcac.managerkobo.service.ReadingStatistics;
import com.arcac.managerkobo.ui.components.RoundedPanel;
import com.arcac.managerkobo.ui.components.StatisticsBarChartPanel;
import com.arcac.managerkobo.ui.components.StatisticsBarChartPanel.BarValue;
import com.arcac.managerkobo.ui.theme.AppTheme;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Rectangle;
import java.util.List;
import java.util.Map;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.Scrollable;
import javax.swing.border.EmptyBorder;

/** Primera versión de la pantalla interactiva de estadísticas avanzadas. */
public class AdvancedStatisticsPanel extends JPanel {
    private static final int MAX_VISIBLE_VALUES = 5;

    private final ReadingStatistics statistics;
    private final JComboBox<ChartType> chartSelector = new JComboBox<>(ChartType.values());
    private final StatisticsBarChartPanel chart = new StatisticsBarChartPanel();
    private final JLabel summaryTitle = new JLabel();
    private final JLabel summaryDetail = new JLabel();

    public AdvancedStatisticsPanel(ReadingStatistics statistics) {
        this(statistics, true);
    }

    public AdvancedStatisticsPanel(
            ReadingStatistics statistics, boolean showHeader) {
        this.statistics = statistics;
        setLayout(new BorderLayout());
        setBackground(AppTheme.BACKGROUND);
        if (showHeader) {
            add(createHeader(), BorderLayout.NORTH);
            add(createScrollableContent(), BorderLayout.CENTER);
        } else {
            setOpaque(false);
            add(createBody(false), BorderLayout.CENTER);
        }
        chartSelector.addActionListener(event -> updateChart());
        updateChart();
    }

    private JPanel createHeader() {
        JPanel header = verticalPanel();
        header.setBorder(new EmptyBorder(30, 32, 22, 32));
        header.add(label("Estadísticas", 29, Font.BOLD, AppTheme.TEXT));
        header.add(Box.createVerticalStrut(5));
        header.add(label("Explora los patrones encontrados en tu biblioteca",
                14, Font.PLAIN, AppTheme.MUTED_TEXT));
        return header;
    }

    private JPanel createBody(boolean withPagePadding) {
        JPanel body = new ScrollableVerticalPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBorder(withPagePadding
                ? new EmptyBorder(0, 32, 30, 32)
                : new EmptyBorder(0, 0, 0, 0));

        JPanel selector = new JPanel(new BorderLayout(12, 0));
        selector.setOpaque(false);
        selector.add(label("Mostrar", 13, Font.BOLD, AppTheme.MUTED_TEXT),
                BorderLayout.WEST);
        chartSelector.setPreferredSize(new Dimension(330, 40));
        selector.add(chartSelector, BorderLayout.CENTER);
        selector.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        selector.setAlignmentX(LEFT_ALIGNMENT);
        body.add(selector);
        body.add(Box.createVerticalStrut(14));

        RoundedPanel chartCard = new RoundedPanel(18, AppTheme.PANEL);
        chartCard.setLayout(new BorderLayout());
        chartCard.setBorder(new EmptyBorder(18, 18, 14, 18));
        chartCard.add(chart, BorderLayout.CENTER);
        chartCard.setPreferredSize(new Dimension(600, 330));
        chartCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 330));
        chartCard.setAlignmentX(LEFT_ALIGNMENT);
        body.add(chartCard);
        return body;
    }

    private JPanel createScrollableContent() {
        JPanel body = createBody(true);
        JScrollPane pageScroll = new JScrollPane(body);
        pageScroll.setBorder(null);
        pageScroll.setOpaque(false);
        pageScroll.getViewport().setOpaque(false);
        pageScroll.setHorizontalScrollBarPolicy(
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        pageScroll.getVerticalScrollBar().setUnitIncrement(18);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(pageScroll, BorderLayout.CENTER);
        return wrapper;
    }

    private void updateChart() {
        ChartType selected = (ChartType) chartSelector.getSelectedItem();
        if (selected == null) {
            return;
        }

        switch (selected) {
            case READING_BY_AUTHOR -> showReadingByAuthor();
            case HIGHLIGHTS_BY_AUTHOR -> showHighlightsByAuthor();
            case READING_BY_BOOK -> showReadingByBook();
            case HIGHLIGHTS_BY_BOOK -> showHighlightsByBook();
            case HIGHLIGHT_DENSITY -> showHighlightDensity();
            case BOOKS_BY_LANGUAGE -> showBooksByLanguage();
            case READING_PROGRESS -> showReadingProgress();
        }
    }

    private void showReadingByAuthor() {
        List<BarValue> values = statistics.readingSecondsByAuthor().entrySet().stream()
                .limit(MAX_VISIBLE_VALUES)
                .map(entry -> new BarValue(
                        entry.getKey(), entry.getValue(), formatTime(entry.getValue())))
                .toList();
        chart.setValues(values, AppTheme.BLUE);

        Map.Entry<String, Long> first = statistics.readingSecondsByAuthor()
                .entrySet().stream().findFirst().orElse(null);
        summaryTitle.setText(first == null
                ? "Sin datos de lectura por autor"
                : "Autor más leído: " + first.getKey());
        summaryDetail.setText(first == null ? ""
                : formatTime(first.getValue()) + " de lectura acumulada");
    }

    private void showHighlightsByAuthor() {
        List<BarValue> values = statistics.highlightsByAuthor().entrySet().stream()
                .limit(MAX_VISIBLE_VALUES)
                .map(entry -> new BarValue(
                        entry.getKey(), entry.getValue(),
                        entry.getValue() + " subrayados"))
                .toList();
        chart.setValues(values, AppTheme.ORANGE);

        Map.Entry<String, Integer> first = statistics.highlightsByAuthor()
                .entrySet().stream().findFirst().orElse(null);
        summaryTitle.setText(first == null
                ? "Sin subrayados asociados a autores"
                : "Autor más subrayado: " + first.getKey());
        summaryDetail.setText(first == null ? ""
                : first.getValue() + " subrayados guardados");
    }

    private void showReadingByBook() {
        List<BarValue> values = statistics.booksByReadingTime().stream()
                .limit(MAX_VISIBLE_VALUES)
                .map(book -> new BarValue(
                        fallback(book.getTitle(), "Sin título"),
                        book.getSecondsRead(),
                        formatTime(book.getSecondsRead())))
                .toList();
        chart.setValues(values, AppTheme.PURPLE);

        summaryTitle.setText("Media por libro empezado: "
                + formatTime(statistics.averageSecondsPerStartedBook()));
        summaryDetail.setText("Media por libro terminado: "
                + formatTime(statistics.averageSecondsPerFinishedBook()));
    }

    private void showHighlightsByBook() {
        List<BarValue> values = statistics.highlightsByBook().entrySet().stream()
                .limit(MAX_VISIBLE_VALUES)
                .map(entry -> new BarValue(
                        fallback(entry.getKey().getTitle(), "Sin título"),
                        entry.getValue(),
                        entry.getValue() + " subrayados"))
                .toList();
        chart.setValues(values, AppTheme.ORANGE);

        Map.Entry<Book, Integer> first = statistics.highlightsByBook()
                .entrySet().stream().findFirst().orElse(null);
        summaryTitle.setText(first == null
                ? "No hay subrayados asociados a libros"
                : "Libro más subrayado: "
                        + fallback(first.getKey().getTitle(), "Sin título"));
        int startedBooks = statistics.finishedBooks() + statistics.readingBooks();
        double average = startedBooks == 0
                ? 0
                : statistics.totalHighlights() / (double) startedBooks;
        summaryDetail.setText(first == null ? ""
                : first.getValue() + " subrayados · Media: "
                        + String.format("%.1f por libro empezado", average));
    }

    private void showHighlightDensity() {
        List<BarValue> values = statistics.highlightDensityByBook()
                .entrySet().stream()
                .limit(MAX_VISIBLE_VALUES)
                .map(entry -> new BarValue(
                        fallback(entry.getKey().getTitle(), "Sin título"),
                        entry.getValue(),
                        String.format("%.1f / h", entry.getValue())))
                .toList();
        chart.setValues(values, AppTheme.GREEN);

        Map.Entry<Book, Double> first = statistics.highlightDensityByBook()
                .entrySet().stream().findFirst().orElse(null);
        summaryTitle.setText(first == null
                ? "No hay datos suficientes para calcular la densidad"
                : "Mayor densidad: "
                        + fallback(first.getKey().getTitle(), "Sin título"));
        summaryDetail.setText(first == null
                ? "Se requieren al menos 30 minutos y 2 subrayados por libro."
                : statistics.highlightsByBook().getOrDefault(first.getKey(), 0)
                        + " subrayados en "
                        + formatTime(first.getKey().getSecondsRead()));
    }

    private void showBooksByLanguage() {
        int total = Math.max(1, statistics.totalBooks());
        List<BarValue> values = statistics.booksByLanguage().entrySet().stream()
                .limit(MAX_VISIBLE_VALUES)
                .map(entry -> new BarValue(
                        entry.getKey(),
                        entry.getValue(),
                        entry.getValue() + " · "
                                + Math.round(entry.getValue() * 100.0 / total) + "%"))
                .toList();
        chart.setValues(values, AppTheme.BLUE, total);

        Map.Entry<String, Integer> first = statistics.booksByLanguage()
                .entrySet().stream().findFirst().orElse(null);
        summaryTitle.setText(first == null
                ? "No hay información de idiomas"
                : "Idioma principal: " + first.getKey());
        summaryDetail.setText(first == null ? ""
                : first.getValue() + " de " + statistics.totalBooks() + " libros");
    }

    private void showReadingProgress() {
        List<BarValue> values = statistics.inProgressBooks().stream()
                .limit(MAX_VISIBLE_VALUES)
                .map(book -> new BarValue(
                        fallback(book.getTitle(), "Sin título"),
                        book.getPercentRead(),
                        book.getPercentRead() + "%"))
                .toList();
        chart.setValues(values, AppTheme.PURPLE, 100);
        summaryTitle.setText(statistics.inProgressBooks().size()
                + " libros en progreso");
        double average = statistics.inProgressBooks().stream()
                .mapToInt(Book::getPercentRead)
                .average().orElse(0);
        summaryDetail.setText("Progreso medio de las lecturas actuales: "
                + Math.round(average) + "%");
    }

    private JPanel verticalPanel() {
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

    private String formatTime(long seconds) {
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        return hours + " h " + minutes + " min";
    }

    private String fallback(String value, String alternative) {
        return value == null || value.isBlank() ? alternative : value;
    }

    private enum ChartType {
        READING_BY_AUTHOR("Tiempo de lectura por autor"),
        HIGHLIGHTS_BY_AUTHOR("Subrayados por autor"),
        READING_BY_BOOK("Tiempo de lectura por libro"),
        HIGHLIGHTS_BY_BOOK("Subrayados por libro"),
        HIGHLIGHT_DENSITY("Densidad de subrayados por libro"),
        BOOKS_BY_LANGUAGE("Libros por idioma"),
        READING_PROGRESS("Progreso de libros en curso");

        private final String label;

        ChartType(String label) {
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    private static class ScrollableVerticalPanel extends JPanel implements Scrollable {
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
