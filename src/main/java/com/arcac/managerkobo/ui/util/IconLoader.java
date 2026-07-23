package com.arcac.managerkobo.ui.util;

import java.awt.Color;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

/** Carga iconos del classpath, los redimensiona y adapta su color al tema. */
public final class IconLoader {
    private static final Map<String, ImageIcon> CACHE = new ConcurrentHashMap<>();

    private IconLoader() { }

    public static ImageIcon loadTinted(String resourcePath, int size, Color color) {
        String key = resourcePath + "|" + size + "|" + color.getRGB();
        ImageIcon cached = CACHE.get(key);
        if (cached != null) return cached;

        try {
            BufferedImage source = readImage(resourcePath);
            if (source == null) return null;
            BufferedImage tinted = new BufferedImage(
                    source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_ARGB);
            int tintRgb = color.getRGB() & 0x00FFFFFF;
            for (int y = 0; y < source.getHeight(); y++) {
                for (int x = 0; x < source.getWidth(); x++) {
                    int alpha = (source.getRGB(x, y) >>> 24) & 0xFF;
                    tinted.setRGB(x, y, (alpha << 24) | tintRgb);
                }
            }
            Image scaled = tinted.getScaledInstance(size, size, Image.SCALE_SMOOTH);
            ImageIcon icon = new ImageIcon(scaled);
            CACHE.put(key, icon);
            return icon;
        } catch (IOException exception) {
            return null;
        }
    }

    /** Usa el classpath en el JAR y la carpeta Maven como respaldo durante desarrollo. */
    private static BufferedImage readImage(String resourcePath) throws IOException {
        URL resource = IconLoader.class.getResource(resourcePath);
        if (resource != null) return ImageIO.read(resource);

        String relativePath = resourcePath.startsWith("/")
                ? resourcePath.substring(1) : resourcePath;
        Path developmentPath = Path.of("src", "main", "resources").resolve(relativePath);
        return Files.isRegularFile(developmentPath) ? ImageIO.read(developmentPath.toFile()) : null;
    }
}
