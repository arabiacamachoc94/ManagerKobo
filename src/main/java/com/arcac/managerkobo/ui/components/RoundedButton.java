package com.arcac.managerkobo.ui.components;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JButton;

/** Botón que pinta un fondo redondeado sin depender del Look & Feel. */
public class RoundedButton extends JButton {
    private final int arc;

    public RoundedButton(String text) {
        this(text, 18);
    }

    public RoundedButton(String text, int arc) {
        super(text);
        this.arc = arc;
        setOpaque(false);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setRolloverEnabled(true);
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D copy = (Graphics2D) graphics.create();
        copy.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        if (!isEnabled()) {
            copy.setComposite(AlphaComposite.SrcOver.derive(0.45f));
        }
        copy.setColor(buttonColor());
        copy.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);
        copy.dispose();
        super.paintComponent(graphics);
    }

    private Color buttonColor() {
        Color base = getBackground();
        if (getModel().isPressed()) return adjust(base, -24);
        if (getModel().isRollover()) return adjust(base, 15);
        return base;
    }

    private Color adjust(Color color, int amount) {
        return new Color(
                clamp(color.getRed() + amount),
                clamp(color.getGreen() + amount),
                clamp(color.getBlue() + amount),
                color.getAlpha());
    }

    private int clamp(int value) {
        return Math.max(0, Math.min(255, value));
    }
}
