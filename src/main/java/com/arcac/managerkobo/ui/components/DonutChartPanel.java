package com.arcac.managerkobo.ui.components;

import com.arcac.managerkobo.ui.theme.AppTheme;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.util.List;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

/** Gráfico de anillo reutilizable con total central y leyenda. */
public class DonutChartPanel extends RoundedPanel {

    public record Segment(String label, int value, Color color) { }

    private final List<Segment> segments;
    private final int total;

    public DonutChartPanel(List<Segment> segments) {
        super(18, AppTheme.PANEL);
        this.segments = segments == null ? List.of() : List.copyOf(segments);
        this.total = this.segments.stream()
                .mapToInt(segment -> Math.max(0, segment.value()))
                .sum();

        setLayout(new GridLayout(1, 2, 28, 0));
        setBorder(new EmptyBorder(14, 16, 14, 16));
        add(new DonutCanvas());
        add(createLegend());
    }

    private JPanel createLegend() {
        JPanel legend = new JPanel();
        legend.setOpaque(false);
        legend.setLayout(new BoxLayout(legend, BoxLayout.Y_AXIS));
        legend.setBorder(new EmptyBorder(9, 0, 8, 0));
        legend.add(Box.createVerticalGlue());
        for (int index = 0; index < segments.size(); index++) {
            legend.add(legendRow(segments.get(index)));
            if (index < segments.size() - 1) {
                legend.add(Box.createVerticalStrut(10));
            }
        }
        legend.add(Box.createVerticalGlue());
        return legend;
    }

    private JPanel legendRow(Segment segment) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setOpaque(false);
        row.add(label("●", 15, Font.BOLD, segment.color()),
                BorderLayout.WEST);
        row.add(label(segment.label(), 13, Font.PLAIN, AppTheme.TEXT),
                BorderLayout.CENTER);

        int percentage = total == 0 ? 0
                : (int) Math.round(segment.value() * 100.0 / total);
        row.add(label(segment.value() + " · " + percentage + "%",
                13, Font.BOLD, AppTheme.TEXT), BorderLayout.EAST);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));
        return row;
    }

    private JLabel label(String text, int size, int style, Color color) {
        JLabel label = new JLabel(text);
        label.setFont(AppTheme.font(style, size));
        label.setForeground(color);
        return label;
    }

    private final class DonutCanvas extends JPanel {
        DonutCanvas() {
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D copy = (Graphics2D) graphics.create();
            copy.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);

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
                for (int index = 0; index < segments.size(); index++) {
                    Segment segment = segments.get(index);
                    int angle = index == segments.size() - 1
                            ? 360 - usedAngle
                            : (int) Math.round(
                                    Math.max(0, segment.value())
                                            * 360.0 / total);
                    copy.setColor(segment.color());
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
}
