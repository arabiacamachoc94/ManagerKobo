package com.arcac.managerkobo.ui.components;

import com.arcac.managerkobo.ui.theme.AppTheme;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Rectangle;
import java.util.List;
import javax.swing.JPanel;
import javax.swing.Scrollable;

/** Gráfica horizontal sencilla, responsive y coherente con el tema de la app. */
public class StatisticsBarChartPanel extends JPanel implements Scrollable {
    private static final int ROW_HEIGHT = 54;
    private List<BarValue> values = List.of();
    private Color accent = AppTheme.BLUE;
    private double fixedMaximum;

    public StatisticsBarChartPanel() {
        setOpaque(false);
        setPreferredSize(new Dimension(600, 200));
    }

    public void setValues(List<BarValue> values, Color accent) {
        setValues(values, accent, 0);
    }

    public void setValues(List<BarValue> values, Color accent, double fixedMaximum) {
        this.values = values == null ? List.of() : List.copyOf(values);
        this.accent = accent;
        this.fixedMaximum = fixedMaximum;
        int height = Math.max(180, this.values.size() * ROW_HEIGHT + 28);
        setPreferredSize(new Dimension(600, height));
        revalidate();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g2 = (Graphics2D) graphics.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        if (values.isEmpty()) {
            g2.setColor(AppTheme.MUTED_TEXT);
            g2.setFont(AppTheme.font(Font.PLAIN, 14));
            g2.drawString("No hay datos suficientes para esta gráfica.", 20, 42);
            g2.dispose();
            return;
        }

        double maximum = fixedMaximum > 0
                ? fixedMaximum
                : values.stream().mapToDouble(BarValue::value).max().orElse(1);
        int left = Math.min(220, Math.max(115, getWidth() / 3));
        int barStart = left + 18;
        int rightSpace = 105;
        int maximumBarWidth = Math.max(40, getWidth() - barStart - rightSpace);

        Font labelFont = AppTheme.font(Font.BOLD, 13);
        Font valueFont = AppTheme.font(Font.PLAIN, 12);
        FontMetrics labelMetrics = g2.getFontMetrics(labelFont);

        for (int index = 0; index < values.size(); index++) {
            BarValue item = values.get(index);
            int y = 20 + index * ROW_HEIGHT;
            int barWidth = (int) Math.round(maximumBarWidth * item.value() / maximum);

            g2.setFont(labelFont);
            g2.setColor(AppTheme.TEXT);
            String label = ellipsize(item.label(), labelMetrics, left - 12);
            g2.drawString(label, 4, y + 21);

            g2.setColor(AppTheme.BORDER);
            g2.fillRoundRect(barStart, y + 7, maximumBarWidth, 20, 12, 12);
            g2.setColor(accent);
            g2.fillRoundRect(barStart, y + 7, Math.max(3, barWidth), 20, 12, 12);

            g2.setFont(valueFont);
            g2.setColor(AppTheme.MUTED_TEXT);
            g2.drawString(item.displayValue(), barStart + maximumBarWidth + 10, y + 22);
        }
        g2.dispose();
    }

    private String ellipsize(String text, FontMetrics metrics, int maximumWidth) {
        if (metrics.stringWidth(text) <= maximumWidth) {
            return text;
        }
        String suffix = "…";
        int length = text.length();
        while (length > 1
                && metrics.stringWidth(text.substring(0, length) + suffix) > maximumWidth) {
            length--;
        }
        return text.substring(0, length) + suffix;
    }

    @Override
    public Dimension getPreferredScrollableViewportSize() {
        return getPreferredSize();
    }

    @Override
    public int getScrollableUnitIncrement(
            Rectangle visibleRect, int orientation, int direction) {
        return ROW_HEIGHT;
    }

    @Override
    public int getScrollableBlockIncrement(
            Rectangle visibleRect, int orientation, int direction) {
        return Math.max(ROW_HEIGHT, visibleRect.height - ROW_HEIGHT);
    }

    @Override
    public boolean getScrollableTracksViewportWidth() {
        return true;
    }

    @Override
    public boolean getScrollableTracksViewportHeight() {
        return false;
    }

    public record BarValue(String label, double value, String displayValue) {
    }
}
