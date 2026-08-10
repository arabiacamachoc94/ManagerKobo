package com.arcac.managerkobo.ui.components;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JPanel;

/** JPanel reutilizable con fondo y borde redondeados. */
public class RoundedPanel extends JPanel {
    private final int radius;
    private final Color fillColor;

    public RoundedPanel(int radius, Color fillColor) {
        this.radius = radius;
        this.fillColor = fillColor;
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D g2 = (Graphics2D) graphics.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int width = Math.max(0, getWidth() - 3);
        int height = Math.max(0, getHeight() - 3);
        g2.setColor(fillColor);
        g2.fillRoundRect(1, 1, width + 1, height + 1, radius, radius);
        g2.dispose();
        super.paintComponent(graphics);
    }
}
