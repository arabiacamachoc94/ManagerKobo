package com.arcac.managerkobo.ui.util;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BaseMultiResolutionImage;
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

    public static ImageIcon load(String resourcePath, int size) {
        String key = resourcePath + "|" + size + "|original";
        ImageIcon cached = CACHE.get(key);
        if (cached != null) return cached;

        try {
            BufferedImage source = readImage(resourcePath);
            if (source == null) return null;
            ImageIcon icon = createHiDpiIcon(source, size);
            CACHE.put(key, icon);
            return icon;
        } catch (IOException exception) {
            return null;
        }
    }

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
            ImageIcon icon = createHiDpiIcon(tinted, size);
            CACHE.put(key, icon);
            return icon;
        } catch (IOException exception) {
            return null;
        }
    }

    /** Conserva variantes nítidas para escalas de pantalla 100%, 200% y 300%. */
    private static ImageIcon createHiDpiIcon(BufferedImage source, int size) {
        BufferedImage normal = resize(source, size);
        BufferedImage doubleSize = resize(source, size * 2);
        BufferedImage tripleSize = resize(source, size * 3);
        Image multiResolution = new BaseMultiResolutionImage(
                normal, doubleSize, tripleSize);
        return new ImageIcon(multiResolution);
    }

    private static BufferedImage resize(BufferedImage source, int size) {
        int padding = Math.max(1, Math.round(size * 0.05f));
        int contentSize = Math.max(1, size - padding * 2);
        BufferedImage scaledContent = progressiveResize(source, contentSize);
        BufferedImage target = new BufferedImage(
                size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = target.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY);
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.drawImage(scaledContent, padding, padding, null);
        graphics.dispose();
        return target;
    }

    /** Reduce por etapas para conservar mejor los bordes de siluetas grandes. */
    private static BufferedImage progressiveResize(
            BufferedImage source, int targetSize) {
        BufferedImage current = source;
        while (current.getWidth() / 2 >= targetSize * 2
                && current.getHeight() / 2 >= targetSize * 2) {
            int nextSize = Math.max(targetSize,
                    Math.min(current.getWidth(), current.getHeight()) / 2);
            current = drawScaled(current, nextSize);
        }
        return drawScaled(current, targetSize);
    }

    private static BufferedImage drawScaled(
            BufferedImage source, int targetSize) {
        BufferedImage target = new BufferedImage(
                targetSize, targetSize, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = target.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        graphics.setRenderingHint(RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY);
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.drawImage(source, 0, 0, targetSize, targetSize, null);
        graphics.dispose();
        return target;
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
