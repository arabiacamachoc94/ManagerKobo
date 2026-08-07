package com.arcac.managerkobo.service;

import com.arcac.managerkobo.model.Book;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.Comparator;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.stream.Stream;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.SwingWorker;

/**
 * Busca portadas en la caché local, en el Kobo conectado o en la URL incluida
 * en la base de datos, por ese orden.
 */
public class BookCoverService {
    private static final Path COVER_DIRECTORY = Path.of("data", "covers");
    private static final int MAX_DOWNLOAD_BYTES = 8 * 1024 * 1024;

    private final HttpClient client = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL)
            .connectTimeout(Duration.ofSeconds(6))
            .build();

    public ImageIcon loadCover(Book book, int maxWidth, int maxHeight) {
        if (book == null || !hasText(book.getImageId())
                && !hasText(book.getImageUrl())
                && !hasText(book.getIsbn())) {
            return null;
        }

        try {
            Files.createDirectories(COVER_DIRECTORY);
            Path cachedCover = COVER_DIRECTORY.resolve(cacheName(book));
            BufferedImage image = read(cachedCover);

            if (image == null && hasText(book.getImageId())) {
                image = read(findCoverOnConnectedKobo(book.getImageId()));
                saveInCache(image, cachedCover);
            }
            if (image == null && hasText(book.getImageUrl())) {
                image = download(book.getImageUrl());
                saveInCache(image, cachedCover);
            }
            if (image == null && hasText(book.getIsbn())) {
                image = downloadByIsbn(book.getIsbn());
                saveInCache(image, cachedCover);
            }
            return image == null ? null : scale(image, maxWidth, maxHeight);
        } catch (Exception ignored) {
            return null;
        }
    }

    public void loadAsync(Book book, int maxWidth, int maxHeight,
            Consumer<ImageIcon> completion) {
        new SwingWorker<ImageIcon, Void>() {
            @Override
            protected ImageIcon doInBackground() {
                return loadCover(book, maxWidth, maxHeight);
            }

            @Override
            protected void done() {
                ImageIcon cover = null;
                try {
                    cover = get();
                } catch (Exception ignored) {
                    // La interfaz conservará su icono provisional.
                }
                completion.accept(cover);
            }
        }.execute();
    }

    private Path findCoverOnConnectedKobo(String imageId) {
        File[] roots = File.listRoots();
        if (roots == null) {
            return null;
        }

        String expected = imageId.toLowerCase(Locale.ROOT);
        for (File root : roots) {
            Path[] candidates = {
                root.toPath().resolve(".kobo-images"),
                root.toPath().resolve(".kobo").resolve("images")
            };
            for (Path directory : candidates) {
                Path cover = findBestMatchingImage(directory, expected);
                if (cover != null) {
                    return cover;
                }
            }
        }
        return null;
    }

    private Path findBestMatchingImage(Path directory, String imageId) {
        if (!Files.isDirectory(directory)) {
            return null;
        }
        String normalizedId = normalizeIdentifier(imageId);
        try (Stream<Path> files = Files.walk(directory, 4)) {
            return files.filter(Files::isRegularFile)
                    .filter(path -> matchesImageName(
                            path.getFileName().toString(), imageId,
                            normalizedId))
                    .max(Comparator.comparingLong(this::safeSize))
                    .orElse(null);
        } catch (Exception ignored) {
            return null;
        }
    }

    private boolean matchesImageName(
            String fileName, String imageId, String normalizedId) {
        String lowerName = fileName.toLowerCase(Locale.ROOT);
        if (lowerName.contains(imageId)) {
            return true;
        }
        String normalizedName = normalizeIdentifier(lowerName);
        return normalizedId.length() >= 12
                && normalizedName.contains(normalizedId);
    }

    private String normalizeIdentifier(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]", "");
    }

    private long safeSize(Path path) {
        try {
            return Files.size(path);
        } catch (Exception ignored) {
            return 0;
        }
    }

    private BufferedImage download(String imageUrl) throws Exception {
        URI uri = URI.create(imageUrl);
        if (!"http".equalsIgnoreCase(uri.getScheme())
                && !"https".equalsIgnoreCase(uri.getScheme())) {
            return null;
        }
        HttpRequest request = HttpRequest.newBuilder(uri)
                .timeout(Duration.ofSeconds(12))
                .header("User-Agent", "KoboManager/1.0")
                .GET()
                .build();
        HttpResponse<byte[]> response = client.send(
                request, HttpResponse.BodyHandlers.ofByteArray());
        byte[] bytes = response.body();
        if (response.statusCode() < 200 || response.statusCode() >= 300
                || bytes == null || bytes.length == 0
                || bytes.length > MAX_DOWNLOAD_BYTES) {
            return null;
        }
        return ImageIO.read(new ByteArrayInputStream(bytes));
    }

    private BufferedImage downloadByIsbn(String isbn) throws Exception {
        String cleanIsbn = isbn.replaceAll("[^0-9Xx]", "");
        if (cleanIsbn.length() < 10) {
            return null;
        }
        return download("https://covers.openlibrary.org/b/isbn/"
                + cleanIsbn + "-L.jpg?default=false");
    }

    private BufferedImage read(Path path) {
        if (path == null || !Files.isRegularFile(path)) {
            return null;
        }
        try {
            return ImageIO.read(path.toFile());
        } catch (Exception ignored) {
            return null;
        }
    }

    private void saveInCache(BufferedImage image, Path destination) {
        if (image == null) {
            return;
        }
        try {
            ImageIO.write(image, "png", destination.toFile());
        } catch (Exception ignored) {
            // La portada aún puede mostrarse aunque no se pueda guardar.
        }
    }

    private ImageIcon scale(BufferedImage source, int maxWidth, int maxHeight) {
        double factor = Math.min(maxWidth / (double) source.getWidth(),
                maxHeight / (double) source.getHeight());
        int width = Math.max(1, (int) Math.round(source.getWidth() * factor));
        int height = Math.max(1, (int) Math.round(source.getHeight() * factor));
        Image scaled = source.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        return new ImageIcon(scaled);
    }

    private String cacheName(Book book) throws Exception {
        String identifier = hasText(book.getImageId())
                ? book.getImageId()
                : hasText(book.getImageUrl())
                        ? book.getImageUrl() : book.getIsbn();
        byte[] digest = MessageDigest.getInstance("SHA-256")
                .digest(identifier.getBytes(StandardCharsets.UTF_8));
        StringBuilder name = new StringBuilder();
        for (byte value : digest) {
            name.append(String.format("%02x", value));
        }
        return name + ".png";
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
