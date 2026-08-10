package com.arcac.managerkobo.ui.components;

import com.arcac.managerkobo.model.Book;
import com.arcac.managerkobo.service.ReadingStatistics;
import com.arcac.managerkobo.ui.theme.AppTheme;
import com.arcac.managerkobo.ui.util.I18n;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.util.Locale;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import static com.arcac.managerkobo.util.ReadingFormat.duration;
import static com.arcac.managerkobo.util.ReadingFormat.textOr;

/** Resumen visual y datos destacados de la biblioteca. */
public class LibraryOverviewPanel extends JPanel {
    private final ReadingStatistics statistics;

    public LibraryOverviewPanel(ReadingStatistics statistics) {
        this.statistics = statistics;
        setOpaque(false);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        add(createMetrics());
        add(Box.createVerticalStrut(16));
        add(createReadingPace());
        add(Box.createVerticalStrut(18));
        add(createInsights());
    }

    private JPanel createReadingPace() {
        RoundedPanel pace = new RoundedPanel(15, AppTheme.PANEL_ALT);
        pace.setLayout(new GridLayout(1, 4, 18, 0));
        pace.setBorder(new EmptyBorder(10, 18, 10, 18));
        pace.setAlignmentX(LEFT_ALIGNMENT);
        pace.setMaximumSize(new Dimension(Integer.MAX_VALUE, 64));
        pace.add(paceMetric("Terminados este año",
                String.valueOf(statistics.finishedBooksThisYear())));
        pace.add(paceMetric("Subrayados",
                String.valueOf(statistics.totalHighlights())));
        pace.add(paceMetric("Ritmo mensual",
                String.format(Locale.getDefault(), "%.1f", statistics.monthlyBookPace())));
        pace.add(paceMetric("Proyección anual",
                String.valueOf(statistics.annualBookProjection())));
        pace.setToolTipText("Estimación basada en los libros terminados que tienen fecha registrada.");
        return pace;
    }

    private JPanel paceMetric(String caption, String value) {
        JPanel panel = verticalPanel();
        JLabel valueLabel = label(value, 18, Font.BOLD, AppTheme.TEXT);
        valueLabel.setAlignmentX(CENTER_ALIGNMENT);
        panel.add(valueLabel);
        panel.add(Box.createVerticalStrut(2));
        JLabel captionLabel = label(caption, 11, Font.PLAIN, AppTheme.MUTED_TEXT);
        captionLabel.setAlignmentX(CENTER_ALIGNMENT);
        panel.add(captionLabel);
        return panel;
    }

    private JPanel createMetrics() {
        JPanel metrics = new JPanel(new GridLayout(1, 4, 18, 0));
        metrics.setOpaque(false);
        metrics.setAlignmentX(LEFT_ALIGNMENT);
        metrics.setMaximumSize(new Dimension(Integer.MAX_VALUE, 82));
        metrics.add(metric("Biblioteca",
                String.valueOf(statistics.totalBooks()), AppTheme.BLUE));
        metrics.add(metric("Terminados",
                String.valueOf(statistics.finishedBooks()), AppTheme.GREEN));
        metrics.add(metric("Horas leídas", totalHoursText(), AppTheme.PURPLE));
        metrics.add(metric("Ritmo de lectura",
                readingPaceNumber(statistics.averageReadingWordsPerMinute()),
                AppTheme.ORANGE));
        return metrics;
    }

    private JPanel metric(String name, String value, Color accent) {
        JPanel metric = verticalPanel();
        metric.add(Box.createVerticalGlue());
        MetricCircle circle = new MetricCircle(value, accent);
        circle.setAlignmentX(CENTER_ALIGNMENT);
        metric.add(circle);
        metric.add(Box.createVerticalStrut(5));

        JLabel nameLabel = label(name, 11, Font.PLAIN, AppTheme.MUTED_TEXT);
        nameLabel.setHorizontalAlignment(SwingConstants.CENTER);
        nameLabel.setAlignmentX(CENTER_ALIGNMENT);
        metric.add(nameLabel);
        metric.add(Box.createVerticalGlue());
        return metric;
    }

    private JPanel createInsights() {
        RoundedPanel insights = new RoundedPanel(17, AppTheme.PANEL_ALT);
        insights.setLayout(new BoxLayout(insights, BoxLayout.Y_AXIS));
        insights.setBorder(new EmptyBorder(16, 18, 16, 18));
        insights.setAlignmentX(LEFT_ALIGNMENT);
        insights.setMaximumSize(new Dimension(Integer.MAX_VALUE, 202));

        var topAuthor = statistics.readingSecondsByAuthor()
                .entrySet().stream().findFirst().orElse(null);
        addInsight(insights, AppTheme.BLUE,
                topAuthor == null
                        ? "Aún no hay datos sobre el tiempo de lectura por autor."
                        : "El autor con más tiempo de lectura es " + topAuthor.getKey()
                                + ", con "
                                + duration(topAuthor.getValue()) + ".",
                "autor con más tiempo");

        Book highlighted = statistics.mostHighlightedBook();
        addInsight(insights, AppTheme.ORANGE,
                highlighted == null
                        ? "Todavía no hay un libro más subrayado."
                        : "El libro más subrayado es «"
                                + textOr(highlighted.getTitle(), "Sin título")
                                + "», con "
                                + statistics.mostHighlightedCount() + ".",
                "libro más subrayado");
        addInsight(insights, AppTheme.PURPLE,
                "La media por libro terminado es de "
                        + duration(
                                statistics.averageSecondsPerFinishedBook())
                        + ".",
                "media por libro");
        addReadingPaceInsight(insights);
        Book fastest = statistics.fastestReadBook();
        addInsight(insights, AppTheme.BLUE,
                fastest == null
                        ? "Aún no hay datos suficientes para calcular el libro leído más rápido."
                        : "El libro que has leído más rápido es «"
                                + textOr(fastest.getTitle(), "Sin título")
                                + "».",
                "libro que has leído más rápido");
        Book slowest = statistics.slowestReadBook();
        addInsight(insights, AppTheme.ORANGE,
                slowest == null
                        ? "Aún no hay datos suficientes para calcular el libro leído más lento."
                        : "El libro leído más lento es «"
                                + textOr(slowest.getTitle(), "Sin título")
                                + "».",
                "libro leído más lento");
        return insights;
    }

    private void addReadingPaceInsight(JPanel insights) {
        double pace = statistics.averageReadingWordsPerMinute();
        if (pace <= 0) {
            addInsight(insights, AppTheme.GREEN,
                    "Aún no hay datos suficientes para comparar tu ritmo de lectura.",
                    "ritmo de lectura");
            return;
        }
        String level = pace < 175 ? "lento" : pace <= 300 ? "normal" : "rápido";
        addInsight(insights, AppTheme.GREEN,
                "Tu ritmo de lectura estimado es " + level
                        + " frente al rango adulto de referencia.",
                "ritmo de lectura estimado es " + level);
    }

    private String readingPaceNumber(double wordsPerMinute) {
        return wordsPerMinute <= 0 ? "--" : String.valueOf(Math.round(wordsPerMinute));
    }

    private void addInsight(JPanel panel, Color dotColor,
            String text, String boldText) {
        String escapedText = escapeHtml(I18n.text(text));
        String escapedBold = escapeHtml(I18n.text(boldText));
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

    private String totalHoursText() {
        long hours = statistics.totalSecondsRead() / 3600;
        return hours > 999 ? "999+" : String.valueOf(hours);
    }

    private String escapeHtml(String value) {
        return value.replace("&", "&amp;")
                .replace("<", "&lt;").replace(">", "&gt;");
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
}
