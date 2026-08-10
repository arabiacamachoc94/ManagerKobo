package com.arcac.managerkobo.service;

import com.arcac.managerkobo.model.Book;
import java.io.File;
import java.io.InputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/** Cuenta las palabras de los EPUB del Kobo y conserva el resultado localmente. */
public class BookWordCountService {
    private static final Path CACHE = Path.of("data", "book-word-counts.properties");
    private static final Pattern WORD = Pattern.compile(
            "[\\p{L}\\p{N}]+(?:['’\\-][\\p{L}\\p{N}]+)*");

    public void enrich(List<Book> books, boolean koboConnected) {
        Properties cache = loadCache();
        boolean changed = false;
        for (Book book : books) {
            String key = book.getContentId();
            if (key == null || key.isBlank()) continue;
            if (book.getWordCount() > 0) {
                if (parseCount(cache.getProperty(key)) <= 0) {
                    cache.setProperty(key, String.valueOf(book.getWordCount()));
                    changed = true;
                }
                continue;
            }
            int cached = parseCount(cache.getProperty(key));
            if (cached > 0) {
                book.setWordCount(cached);
                continue;
            }
            if (!koboConnected) continue;
            Path epub = findEpub(book.getContentId());
            int count = countWords(epub);
            if (count > 0) {
                book.setWordCount(count);
                cache.setProperty(key, String.valueOf(count));
                changed = true;
            }
        }
        if (changed) saveCache(cache);
    }

    private Path findEpub(String contentId) {
        String prefix = "file:///mnt/onboard/";
        if (contentId == null || !contentId.startsWith(prefix)) return null;
        try {
            String encoded = contentId.substring(prefix.length())
                    .replace("+", "%2B");
            String relative = URLDecoder.decode(encoded, StandardCharsets.UTF_8);
            File[] roots = File.listRoots();
            if (roots == null) return null;
            for (File root : roots) {
                Path candidate = root.toPath().resolve(relative);
                if (Files.isRegularFile(candidate)) return candidate;
            }
        } catch (Exception ignored) {
            // El identificador no corresponde a un archivo accesible.
        }
        return null;
    }

    private int countWords(Path epub) {
        if (epub == null) return 0;
        long total = 0;
        try (InputStream input = Files.newInputStream(epub);
                ZipInputStream zip = new ZipInputStream(input)) {
            ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
                String name = entry.getName().toLowerCase();
                if (!entry.isDirectory() && (name.endsWith(".xhtml")
                        || name.endsWith(".html") || name.endsWith(".htm"))) {
                    String html = new String(zip.readAllBytes(), StandardCharsets.UTF_8);
                    String text = html.replaceAll("(?is)<script.*?</script>", " ")
                            .replaceAll("(?is)<style.*?</style>", " ")
                            .replaceAll("(?s)<[^>]+>", " ")
                            .replace("&nbsp;", " ")
                            .replace("&amp;", "&")
                            .replace("&quot;", "\"")
                            .replace("&#39;", "'");
                    Matcher matcher = WORD.matcher(text);
                    while (matcher.find()) total++;
                }
                zip.closeEntry();
            }
        } catch (Exception ignored) {
            return 0;
        }
        return total > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) total;
    }

    private Properties loadCache() {
        Properties properties = new Properties();
        if (!Files.isRegularFile(CACHE)) return properties;
        try (InputStream input = Files.newInputStream(CACHE)) {
            properties.load(input);
        } catch (Exception ignored) {
            // La app puede recalcular la caché en la siguiente sincronización.
        }
        return properties;
    }

    private void saveCache(Properties properties) {
        try {
            Files.createDirectories(CACHE.getParent());
            try (var output = Files.newOutputStream(CACHE)) {
                properties.store(output, "Kobo Manager - word counts");
            }
        } catch (Exception ignored) {
            // El ritmo simplemente quedará sin datos si no se puede guardar.
        }
    }

    private int parseCount(String value) {
        try {
            return value == null ? 0 : Math.max(0, Integer.parseInt(value));
        } catch (NumberFormatException ignored) {
            return 0;
        }
    }
}
