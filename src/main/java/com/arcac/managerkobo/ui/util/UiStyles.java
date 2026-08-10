package com.arcac.managerkobo.ui.util;

import com.arcac.managerkobo.ui.theme.AppTheme;
import java.awt.Color;
import java.awt.Font;
import javax.swing.JButton;
import javax.swing.border.EmptyBorder;

/** Medidas y estilos comunes para los controles de la aplicación. */
public final class UiStyles {
    public static final int CONTROL_HEIGHT = 40;
    public static final int BUTTON_HEIGHT = 36;

    private UiStyles() { }

    public static void actionButton(JButton button, Color background) {
        standardButton(button, background, Color.WHITE);
    }

    public static void secondaryButton(JButton button) {
        standardButton(button, AppTheme.PANEL_ALT, AppTheme.TEXT);
    }

    private static void standardButton(JButton button, Color background,
            Color foreground) {
        button.putClientProperty("JButton.buttonType", "roundRect");
        button.putClientProperty("JComponent.roundRect", true);
        button.setFont(AppTheme.font(Font.BOLD, 12));
        button.setForeground(foreground);
        button.setBackground(background);
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(9, 14, 9, 14));
    }
}
