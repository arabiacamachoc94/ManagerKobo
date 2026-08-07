package com.arcac.managerkobo.ui.theme;

import java.awt.Color;
import java.awt.Font;
import com.arcac.managerkobo.ui.util.AppPreferences;

/** Paleta y tipografía compartidas por toda la interfaz. */
public final class AppTheme {
    public static Color BACKGROUND;
    public static Color SIDEBAR;
    public static Color PANEL;
    public static Color PANEL_ALT;
    public static Color BORDER;
    public static Color TEXT;
    public static Color MUTED_TEXT;
    public static final Color PURPLE = new Color(132, 92, 230);
    public static final Color GREEN = new Color(73, 190, 123);
    public static final Color BLUE = new Color(69, 145, 225);
    public static final Color ORANGE = new Color(239, 145, 65);
    public static Color NAV_SELECTED;

    static {
        reload();
    }

    private AppTheme() { }

    public static void reload() {
        boolean light = AppPreferences.isLightTheme();
        BACKGROUND = light ? new Color(245, 247, 252) : new Color(12, 15, 27);
        SIDEBAR = light ? new Color(235, 238, 247) : new Color(16, 20, 36);
        PANEL = light ? Color.WHITE : new Color(29, 34, 56);
        PANEL_ALT = light ? new Color(226, 230, 241) : new Color(35, 40, 65);
        BORDER = light ? new Color(202, 208, 222) : new Color(61, 67, 94);
        TEXT = light ? new Color(31, 35, 48) : new Color(239, 241, 248);
        MUTED_TEXT = light ? new Color(99, 106, 124) : new Color(161, 166, 184);
        NAV_SELECTED = light ? new Color(111, 81, 199) : new Color(78, 61, 151);
    }

    public static Font font(int style, int size) {
        return new Font("SansSerif", style, size);
    }
}
