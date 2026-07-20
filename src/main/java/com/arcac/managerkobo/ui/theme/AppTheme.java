package com.arcac.managerkobo.ui.theme;

import java.awt.Color;
import java.awt.Font;

/** Paleta y tipografía compartidas por toda la interfaz. */
public final class AppTheme {
    public static final Color BACKGROUND = new Color(12, 15, 27);
    public static final Color SIDEBAR = new Color(16, 20, 36);
    public static final Color PANEL = new Color(29, 34, 56);
    public static final Color PANEL_ALT = new Color(35, 40, 65);
    public static final Color BORDER = new Color(61, 67, 94);
    public static final Color TEXT = new Color(239, 241, 248);
    public static final Color MUTED_TEXT = new Color(161, 166, 184);
    public static final Color PURPLE = new Color(132, 92, 230);
    public static final Color GREEN = new Color(73, 190, 123);
    public static final Color BLUE = new Color(69, 145, 225);
    public static final Color ORANGE = new Color(239, 145, 65);
    public static final Color NAV_SELECTED = new Color(78, 61, 151);

    private AppTheme() { }

    public static Font font(int style, int size) {
        return new Font("SansSerif", style, size);
    }
}
