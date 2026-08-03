package com.arcac.managerkobo.ui.panels;

import com.arcac.managerkobo.model.Book;
import com.arcac.managerkobo.service.BookCoverService;
import com.arcac.managerkobo.service.ReadingStatistics;
import com.arcac.managerkobo.ui.components.BookCard;
import com.arcac.managerkobo.ui.components.RoundedPanel;
import com.arcac.managerkobo.ui.theme.AppTheme;
import com.arcac.managerkobo.ui.util.IconLoader;
import java.awt.BorderLayout;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;

/** Resumen principal de la actividad de lectura. */
public class DashboardPanel extends JPanel {
    private final ReadingStatistics statistics;
    private final Consumer<Book> openBookAction;
    private final LocalDateTime lastSynchronization;
    private final BookCoverService coverService = new BookCoverService();

    public DashboardPanel(List<Book> books, ReadingStatistics statistics,
            Consumer<Book> openBookAction,
            LocalDateTime lastSynchronization) {
        this.statistics = statistics;
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
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(
                        0, 0, 1, 0, AppTheme.BORDER),
                new EmptyBorder(30, 32, 22, 32)));

        JPanel titles = verticalPanel();
        titles.add(label("Dashboard", 29, Font.BOLD, AppTheme.TEXT));
        titles.add(Box.createVerticalStrut(5));
        titles.add(label(librarySummary(), 14, Font.PLAIN, AppTheme.MUTED_TEXT));
        titles.add(Box.createVerticalStrut(4));
        titles.add(label(lastSynchronizationText(),
                12, Font.PLAIN, AppTheme.MUTED_TEXT));
        header.add(titles, BorderLayout.CENTER);

        JLabel logo = new JLabel();
        logo.setIcon(IconLoader.load("/icons/logo2-transparent.png", 58));
        logo.setHorizontalAlignment(SwingConstants.CENTER);
        logo.setPreferredSize(new Dimension(64, 64));
        header.add(logo, BorderLayout.EAST);

        return header;
    }

    private JPanel createContent() {
        ScrollableVerticalPanel body = new ScrollableVerticalPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBorder(new EmptyBorder(22, 32, 32, 32));

        Book currentBook = findCurrentBook();
        JPanel hero = responsiveGrid(2, 390, 295, 16);
        hero.add(titledSection("Lectura actual",
                createCurrentReading(currentBook)));
        hero.add(titledSection("Lecturas en curso",
                createCurrentBooksSummary()));
        body.add(hero);
        body.add(Box.createVerticalStrut(22));

        body.add(sectionTitle("Tu biblioteca de un vistazo"));
        body.add(Box.createVerticalStrut(13));
        body.add(fullWidth(createLibraryOverview(), 255));
        body.add(Box.createVerticalStrut(24));

        body.add(createDonutCharts());
        body.add(Box.createVerticalStrut(24));

        body.add(sectionTitle("Explora tus estadísticas"));
        body.add(Box.createVerticalStrut(12));
        AdvancedStatisticsPanel advancedPanel =
                new AdvancedStatisticsPanel(statistics, false);
        advancedPanel.setPreferredSize(new Dimension(600, 384));
        advancedPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 384));
        body.add(advancedPanel);

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

        if (book != null) {
            card.add(createBookCover(book, 68, 96), BorderLayout.WEST);
        }

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

    private JPanel createLibraryOverview() {
        JPanel overview = new JPanel();
        overview.setOpaque(false);
        overview.setLayout(new BoxLayout(overview, BoxLayout.Y_AXIS));
        overview.add(createLibrarySummary());
        overview.add(Box.createVerticalStrut(24));
        overview.add(createInsightsText());
        return overview;
    }

    private JPanel createLibrarySummary() {
        JPanel metrics = new JPanel(new GridLayout(1, 4, 18, 0));
        metrics.setOpaque(false);
        metrics.setAlignmentX(LEFT_ALIGNMENT);
        metrics.setMaximumSize(new Dimension(Integer.MAX_VALUE, 82));
        metrics.add(summaryMetric("Biblioteca",
                String.valueOf(statistics.totalBooks()), AppTheme.BLUE));
        metrics.add(summaryMetric("Terminados",
                String.valueOf(statistics.finishedBooks()), AppTheme.GREEN));
        metrics.add(summaryMetric("Horas leídas",
                totalHoursText(), AppTheme.PURPLE));
        metrics.add(summaryMetric("Subrayados",
                String.valueOf(statistics.totalHighlights()), AppTheme.ORANGE));
        return metrics;
    }

    private JPanel summaryMetric(String name, String value, Color accent) {
        JPanel metric = verticalPanel();
        metric.setOpaque(false);
        metric.add(Box.createVerticalGlue());
        MetricCircle circle = new MetricCircle(value, accent);
        circle.setAlignmentX(CENTER_ALIGNMENT);
        metric.add(circle);
        metric.add(Box.createVerticalStrut(5));

        JLabel nameLabel = label(name,
                11, Font.PLAIN, AppTheme.MUTED_TEXT);
        nameLabel.setHorizontalAlignment(SwingConstants.CENTER);
        nameLabel.setAlignmentX(CENTER_ALIGNMENT);
        metric.add(nameLabel);
        metric.add(Box.createVerticalGlue());
        return metric;
    }

    private String totalHoursText() {
        long seconds = statistics.totalSecondsRead();
        long hours = seconds / 3600;
        return hours > 999 ? "999+" : String.valueOf(hours);
    }

    private JPanel createDonutCharts() {
        JPanel charts = responsiveGrid(2, 360, 245, 16);
        charts.add(titledSection("Estado de la biblioteca",
                createLibraryStatusDonut()));
        charts.add(titledSection("Progreso de lecturas activas",
                createReadingProgressDonut()));
        return charts;
    }

    private JPanel createLibraryStatusDonut() {
        return createDonutPanel(
                new int[] {
                    statistics.finishedBooks(), statistics.readingBooks(),
                    statistics.unreadBooks()
                },
                new String[] {"Terminados", "Leyendo", "Sin empezar"},
                new Color[] {
                    AppTheme.GREEN, AppTheme.PURPLE, AppTheme.BLUE
                });
    }

    private JPanel createReadingProgressDonut() {
        int[] ranges = new int[4];
        for (Book book : statistics.inProgressBooks()) {
            int progress = book.getPercentRead();
            if (progress < 25) ranges[0]++;
            else if (progress < 50) ranges[1]++;
            else if (progress < 75) ranges[2]++;
            else ranges[3]++;
        }
        return createDonutPanel(ranges,
                new String[] {"Menos del 25%", "25–49%", "50–74%", "75% o más"},
                new Color[] {
                    AppTheme.BLUE, AppTheme.PURPLE,
                    AppTheme.ORANGE, AppTheme.GREEN
                });
    }

    private JPanel createDonutPanel(
            int[] values, String[] labels, Color[] colors) {
        RoundedPanel panel = new RoundedPanel(18, AppTheme.PANEL);
        panel.setLayout(new GridLayout(1, 2, 28, 0));
        panel.setBorder(new EmptyBorder(14, 16, 14, 16));

        panel.add(new DonutChart(values, colors));

        JPanel legend = verticalPanel();
        legend.setBorder(new EmptyBorder(9, 0, 8, 0));
        legend.add(Box.createVerticalGlue());
        int total = java.util.Arrays.stream(values).sum();
        for (int index = 0; index < values.length; index++) {
            legend.add(legendRow(labels[index], values[index], total,
                    colors[index]));
            if (index < values.length - 1) {
                legend.add(Box.createVerticalStrut(10));
            }
        }
        legend.add(Box.createVerticalGlue());
        panel.add(legend);
        return panel;
    }

    private JPanel legendRow(
            String name, int amount, int totalAmount, Color color) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setOpaque(false);
        JLabel dot = label("●", 15, Font.BOLD, color);
        row.add(dot, BorderLayout.WEST);
        row.add(label(name, 13, Font.PLAIN, AppTheme.TEXT),
                BorderLayout.CENTER);

        int total = Math.max(1, totalAmount);
        int percentage = (int) Math.round(amount * 100.0 / total);
        row.add(label(amount + " · " + percentage + "%",
                13, Font.BOLD, AppTheme.TEXT), BorderLayout.EAST);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));
        return row;
    }

    private JPanel createCurrentBooksSummary() {
        RoundedPanel card = new RoundedPanel(20, AppTheme.PANEL);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(14, 16, 14, 16));

        List<Book> currentBooks = statistics.inProgressBooks();
        JPanel books = new JPanel();
        books.setOpaque(false);
        books.setLayout(new GridLayout(1, 2, 10, 0));
        if (currentBooks.isEmpty()) {
            books.setLayout(new BorderLayout());
            JLabel message = label("No hay libros en progreso",
                    13, Font.PLAIN, AppTheme.MUTED_TEXT);
            message.setHorizontalAlignment(SwingConstants.CENTER);
            books.add(message);
            card.add(books, BorderLayout.CENTER);
        } else {
            final int pageSize = 2;
            final int lastPage = (currentBooks.size() - 1) / pageSize;
            final int[] currentPage = {0};
            JButton previous = carouselButton("‹", "Lecturas anteriores");
            JButton next = carouselButton("›", "Lecturas siguientes");
            previous.setVisible(lastPage > 0);
            next.setVisible(lastPage > 0);

            Runnable showPage = () -> {
                books.removeAll();
                int first = currentPage[0] * pageSize;
                int limit = Math.min(first + pageSize, currentBooks.size());
                for (int index = first; index < limit; index++) {
                    books.add(createCurrentBookMiniCard(currentBooks.get(index)));
                }
                while (books.getComponentCount() < pageSize) {
                    JPanel emptySlot = new JPanel();
                    emptySlot.setOpaque(false);
                    books.add(emptySlot);
                }
                previous.setEnabled(currentPage[0] > 0);
                next.setEnabled(currentPage[0] < lastPage);
                books.revalidate();
                books.repaint();
            };
            previous.addActionListener(event -> {
                if (currentPage[0] > 0) {
                    currentPage[0]--;
                    showPage.run();
                }
            });
            next.addActionListener(event -> {
                if (currentPage[0] < lastPage) {
                    currentPage[0]++;
                    showPage.run();
                }
            });
            showPage.run();

            JPanel carousel = new JPanel(new BorderLayout(7, 0));
            carousel.setOpaque(false);
            carousel.add(previous, BorderLayout.WEST);
            carousel.add(books, BorderLayout.CENTER);
            carousel.add(next, BorderLayout.EAST);
            card.add(carousel, BorderLayout.CENTER);
        }
        return card;
    }

    private JPanel titledSection(String title, JPanel content) {
        JPanel section = new JPanel(new BorderLayout(0, 11));
        section.setOpaque(false);
        JLabel heading = sectionTitle(title);
        if ("Lecturas en curso".equals(title)) {
            heading.setHorizontalAlignment(SwingConstants.CENTER);
        }
        section.add(heading, BorderLayout.NORTH);
        section.add(content, BorderLayout.CENTER);
        return section;
    }

    private JButton carouselButton(String text, String tooltip) {
        JButton button = new JButton(text);
        button.setToolTipText(tooltip);
        button.setFont(AppTheme.font(Font.BOLD, 19));
        button.setForeground(AppTheme.TEXT);
        button.setBackground(AppTheme.PANEL_ALT);
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(4, 7, 4, 7));
        button.setPreferredSize(new Dimension(27, 44));
        return button;
    }

    private JPanel createCurrentBookMiniCard(Book book) {
        return new BookCard(book, openBookAction, true);
    }

    private JLabel createBookCover(Book book, int width, int height) {
        JLabel cover = new JLabel();
        cover.setHorizontalAlignment(SwingConstants.CENTER);
        cover.setPreferredSize(new Dimension(width + 8, height));
        cover.setIcon(IconLoader.loadTinted(
                "/icons/libro.png", 40, AppTheme.PURPLE));

        new SwingWorker<ImageIcon, Void>() {
            @Override
            protected ImageIcon doInBackground() {
                return coverService.loadCover(book, width, height);
            }

            @Override
            protected void done() {
                try {
                    ImageIcon loadedCover = get();
                    if (loadedCover != null) {
                        cover.setIcon(loadedCover);
                    }
                } catch (Exception ignored) {
                    // Se conserva el icono genérico.
                }
            }
        }.execute();
        return cover;
    }

    private JPanel createInsightsText() {
        RoundedPanel insights = new RoundedPanel(17, AppTheme.PANEL_ALT);
        insights.setLayout(new BoxLayout(insights, BoxLayout.Y_AXIS));
        insights.setBorder(new EmptyBorder(16, 18, 16, 18));
        insights.setAlignmentX(LEFT_ALIGNMENT);
        insights.setMaximumSize(new Dimension(Integer.MAX_VALUE, 145));
        var topAuthor = statistics.readingSecondsByAuthor()
                .entrySet().stream().findFirst().orElse(null);
        addInsightText(insights, AppTheme.BLUE,
                topAuthor == null
                        ? "Aún no hay datos sobre tu autor más leído."
                        : "Tu autor más leído es " + topAuthor.getKey()
                                + ", con "
                                + formatTime(topAuthor.getValue()) + ".",
                "autor más leído");

        Book highlighted = statistics.mostHighlightedBook();
        addInsightText(insights, AppTheme.ORANGE,
                highlighted == null
                        ? "Todavía no hay un libro más subrayado."
                        : "El libro más subrayado es «"
                                + fallback(highlighted.getTitle(), "Sin título")
                                + "», con "
                                + statistics.mostHighlightedCount() + ".",
                "libro más subrayado");
        addInsightText(insights, AppTheme.PURPLE,
                "La media por libro terminado es de "
                        + formatTime(
                                statistics.averageSecondsPerFinishedBook())
                        + ".",
                "media por libro");
        addInsightText(insights, AppTheme.GREEN,
                statistics.readingBooks() == 1
                        ? "Tienes 1 lectura activa en este momento."
                        : "Tienes " + statistics.readingBooks()
                                + " lecturas activas en este momento.",
                statistics.readingBooks() == 1
                        ? "lectura activa" : "lecturas activas");
        return insights;
    }

    private void addInsightText(JPanel panel, Color dotColor,
            String text, String boldText) {
        String escapedText = text.replace("&", "&amp;")
                .replace("<", "&lt;").replace(">", "&gt;");
        String escapedBold = boldText.replace("&", "&amp;")
                .replace("<", "&lt;").replace(">", "&gt;");
        escapedText = escapedText.replace(escapedBold,
                "<b>" + escapedBold + "</b>");
        String color = String.format("#%02x%02x%02x",
                dotColor.getRed(), dotColor.getGreen(), dotColor.getBlue());
        JLabel line = label("<html><font color='" + color
                + "'>●</font>&nbsp;&nbsp;" + escapedText + "</html>",
                13, Font.PLAIN, AppTheme.TEXT);
        line.setAlignmentX(LEFT_ALIGNMENT);
        line.setBorder(new EmptyBorder(3, 0, 3, 0));
        line.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        panel.add(line);
    }

    private JProgressBar progressBar(int value, Color color, String text) {
        return progressBar(value, 100, color, text);
    }

    private JProgressBar progressBar(
            int value, int maximum, Color color, String text) {
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

    private JPanel responsiveGrid(
            int maxColumns, int minimumCardWidth, int rowHeight, int gap) {
        JPanel panel = new JPanel(new GridLayout(0, maxColumns, gap, gap));
        panel.setOpaque(false);
        panel.putClientProperty("responsiveColumns", maxColumns);
        panel.setPreferredSize(
                new Dimension(maxColumns * minimumCardWidth, rowHeight));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, rowHeight));
        panel.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent event) {
                updateResponsiveGrid(
                        panel, maxColumns, minimumCardWidth, rowHeight, gap);
            }
        });
        return panel;
    }

    private void updateResponsiveGrid(
            JPanel panel, int maxColumns, int minimumCardWidth,
            int rowHeight, int gap) {
        int availableWidth = Math.max(minimumCardWidth, panel.getWidth());
        int columns = Math.max(1, Math.min(maxColumns,
                (availableWidth + gap) / (minimumCardWidth + gap)));
        int previousColumns =
                (int) panel.getClientProperty("responsiveColumns");
        int rows = Math.max(1,
                (int) Math.ceil(panel.getComponentCount() / (double) columns));
        int requiredHeight = rows * rowHeight + (rows - 1) * gap;

        if (columns != previousColumns
                || panel.getPreferredSize().height != requiredHeight) {
            ((GridLayout) panel.getLayout()).setColumns(columns);
            panel.putClientProperty("responsiveColumns", columns);
            panel.setPreferredSize(new Dimension(
                    Math.max(minimumCardWidth, availableWidth), requiredHeight));
            panel.setMaximumSize(
                    new Dimension(Integer.MAX_VALUE, requiredHeight));
            panel.revalidate();
        }
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

    private static final class MetricCircle extends JLabel {
        private final Color fill;

        MetricCircle(String value, Color fill) {
            super(value, SwingConstants.CENTER);
            this.fill = fill;
            setForeground(Color.WHITE);
            setFont(AppTheme.font(Font.BOLD, 15));
            setPreferredSize(new Dimension(56, 56));
            setMinimumSize(getPreferredSize());
            setMaximumSize(getPreferredSize());
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D copy = (Graphics2D) graphics.create();
            copy.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            copy.setColor(fill);
            copy.fillOval(2, 2, getWidth() - 4, getHeight() - 4);
            copy.setColor(AppTheme.TEXT);
            copy.setStroke(new BasicStroke(1.5f));
            copy.drawOval(2, 2, getWidth() - 5, getHeight() - 5);
            copy.dispose();
            super.paintComponent(graphics);
        }
    }

    private static final class DonutChart extends JPanel {
        private final int[] values;
        private final Color[] colors;

        DonutChart(int[] values, Color[] colors) {
            this.values = values.clone();
            this.colors = colors.clone();
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D copy = (Graphics2D) graphics.create();
            copy.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);

            int total = java.util.Arrays.stream(values).sum();
            int diameter = Math.min(152,
                    Math.min(getWidth() - 18, getHeight() - 18));
            diameter = Math.max(40, diameter);
            int x = (getWidth() - diameter) / 2;
            int y = (getHeight() - diameter) / 2;

            if (total == 0) {
                copy.setColor(AppTheme.BORDER);
                copy.fillOval(x, y, diameter, diameter);
            } else {
                int start = 90;
                int usedAngle = 0;
                for (int index = 0; index < values.length; index++) {
                    int angle = index == values.length - 1
                            ? 360 - usedAngle
                            : (int) Math.round(values[index] * 360.0 / total);
                    copy.setColor(colors[index]);
                    copy.fillArc(x, y, diameter, diameter, start, -angle);
                    start -= angle;
                    usedAngle += angle;
                }
            }

            int hole = (int) Math.round(diameter * 0.58);
            int holeX = x + (diameter - hole) / 2;
            int holeY = y + (diameter - hole) / 2;
            copy.setColor(AppTheme.PANEL);
            copy.fillOval(holeX, holeY, hole, hole);

            String totalText = String.valueOf(total);
            copy.setFont(AppTheme.font(Font.BOLD, 24));
            copy.setColor(AppTheme.TEXT);
            int textWidth = copy.getFontMetrics().stringWidth(totalText);
            int baseline = y + diameter / 2
                    + copy.getFontMetrics().getAscent() / 3;
            copy.drawString(totalText,
                    x + (diameter - textWidth) / 2, baseline);
            copy.dispose();
        }
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
