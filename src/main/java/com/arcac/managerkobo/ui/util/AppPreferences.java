package com.arcac.managerkobo.ui.util;

import java.util.Locale;
import java.util.prefs.Preferences;

/** Preferencias visuales generales de Kobo Manager. */
public final class AppPreferences {
    private static final Preferences STORE = Preferences.userNodeForPackage(
            AppPreferences.class);
    private static final String LANGUAGE = "language";
    private static final String THEME = "theme";

    private AppPreferences() { }

    public static String language() {
        return STORE.get(LANGUAGE, "Español");
    }

    public static boolean isEnglish() {
        return "English".equals(language());
    }

    public static void setLanguage(String language) {
        STORE.put(LANGUAGE, "English".equals(language) ? "English" : "Español");
    }

    public static String theme() {
        return STORE.get(THEME, "Oscuro");
    }

    public static boolean isLightTheme() {
        return "Claro".equals(theme());
    }

    public static void setTheme(String theme) {
        STORE.put(THEME, "Claro".equals(theme) ? "Claro" : "Oscuro");
    }

    public static void applyLocale() {
        Locale.setDefault("English".equals(language())
                ? Locale.ENGLISH : new Locale("es", "ES"));
    }
}
