package com.arcac.managerkobo.ai;

import java.util.prefs.Preferences;

/** Guarda la clave de Gemini en las preferencias del usuario del sistema. */
public final class GeminiApiKeyStore {
    private static final String KEY_NAME = "geminiApiKey";
    private static final Preferences PREFERENCES = Preferences.userNodeForPackage(
            GeminiApiKeyStore.class);

    private GeminiApiKeyStore() { }

    public static String get() {
        String environmentKey = System.getenv("GEMINI_API_KEY");
        if (environmentKey != null && !environmentKey.isBlank()) {
            return environmentKey.strip();
        }
        return PREFERENCES.get(KEY_NAME, "").strip();
    }

    public static boolean comesFromEnvironment() {
        String value = System.getenv("GEMINI_API_KEY");
        return value != null && !value.isBlank();
    }

    public static void save(String apiKey) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalArgumentException("La clave no puede estar vacía.");
        }
        PREFERENCES.put(KEY_NAME, apiKey.strip());
    }

    public static void remove() {
        PREFERENCES.remove(KEY_NAME);
    }
}
