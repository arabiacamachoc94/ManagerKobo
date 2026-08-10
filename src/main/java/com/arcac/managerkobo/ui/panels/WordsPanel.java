package com.arcac.managerkobo.ui.panels;

import com.arcac.managerkobo.model.LookedUpWord;
import com.arcac.managerkobo.ui.components.RoundedButton;
import com.arcac.managerkobo.ui.components.RoundedPanel;
import com.arcac.managerkobo.ui.theme.AppTheme;
import com.arcac.managerkobo.ui.util.I18n;
import com.arcac.managerkobo.ui.util.UiStyles;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Cursor;
import java.awt.Component;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Locale;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.Scrollable;
import javax.swing.SwingUtilities;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import static com.arcac.managerkobo.util.ReadingFormat.textOr;

/** Pantalla básica de palabras consultadas en el diccionario del Kobo. */
public class WordsPanel extends JPanel {
    private static final int PAGE_HEADER_HEIGHT = 124;
    private final List<LookedUpWord> words;
    private final JPanel groupsPanel = new ScrollableGroupsPanel();
    private final Map<String, Boolean> expandedGroups = new HashMap<>();

    public WordsPanel(List<LookedUpWord> words) {
        List<LookedUpWord> safeWords = words == null ? List.of() : words;
        this.words = List.copyOf(safeWords);
        setLayout(new BorderLayout());
        setBackground(AppTheme.BACKGROUND);
        add(createHeader(safeWords), BorderLayout.NORTH);
        add(createContent(), BorderLayout.CENTER);
    }

    private JPanel createHeader(List<LookedUpWord> words) {
        long books = words.stream()
                .map(LookedUpWord::volumeId)
                .filter(value -> value != null && !value.isBlank())
                .distinct()
                .count();

        JPanel header = new JPanel(new BorderLayout(16, 0));
        header.setOpaque(false);
        header.setPreferredSize(new Dimension(0, PAGE_HEADER_HEIGHT));
        header.setBorder(new EmptyBorder(30, 32, 22, 32));
        JPanel titles = verticalPanel();
        titles.add(label("Palabras", 29, Font.BOLD, AppTheme.TEXT));
        titles.add(Box.createVerticalStrut(5));
        titles.add(label(words.size() + " palabras consultadas en "
                        + books + (books == 1 ? " libro" : " libros"),
                14, Font.PLAIN, AppTheme.MUTED_TEXT));
        header.add(titles, BorderLayout.CENTER);

        return header;
    }

    private void exportWords() {
        if (words.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    I18n.text("No hay datos para exportar."),
                    I18n.text("Exportación no disponible"),
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle(I18n.text("Exportar palabras"));
        chooser.setApproveButtonText(I18n.text("Exportar"));
        I18n.translateTree(chooser);
        chooser.setSelectedFile(new java.io.File("palabras_kobo.txt"));
        chooser.setFileFilter(new FileNameExtensionFilter(
                I18n.text("Archivo de texto") + " (*.txt)", "txt"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;

        Path destination = chooser.getSelectedFile().toPath();
        if (!destination.toString().toLowerCase(Locale.ROOT).endsWith(".txt")) {
            destination = Path.of(destination + ".txt");
        }
        if (Files.exists(destination)) {
            int answer = JOptionPane.showConfirmDialog(this,
                    I18n.text("El archivo ya existe. ¿Quieres reemplazarlo?"),
                    I18n.text("Confirmar exportación"),
                    JOptionPane.YES_NO_OPTION);
            if (answer != JOptionPane.YES_OPTION) return;
        }

        List<String> lines = words.stream()
                .map(LookedUpWord::text)
                .filter(value -> value != null && !value.isBlank())
                .map(String::strip)
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .distinct()
                .toList();
        try {
            Files.write(destination, lines, StandardCharsets.UTF_8);
            JOptionPane.showMessageDialog(this,
                    I18n.text("Palabras exportadas correctamente en:")
                            + "\n" + destination.toAbsolutePath(),
                    I18n.text("Exportación completada"),
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (java.io.IOException exception) {
            JOptionPane.showMessageDialog(this,
                    I18n.text("No se pudo crear el archivo: ")
                            + exception.getMessage(),
                    I18n.text("Error de exportación"),
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private JPanel createContent() {
        JPanel body = new JPanel(new BorderLayout(0, 14));
        body.setOpaque(false);
        body.setBorder(new EmptyBorder(4, 32, 30, 32));

        JTextField search = new JTextField();
        search.putClientProperty("JTextField.placeholderText",
                I18n.text("Buscar palabra, libro o autor..."));
        search.setPreferredSize(new Dimension(400, 40));
        search.getDocument().addDocumentListener(new DocumentListener() {
            private void update() {
                refreshGroups(search.getText());
            }
            @Override public void insertUpdate(DocumentEvent event) { update(); }
            @Override public void removeUpdate(DocumentEvent event) { update(); }
            @Override public void changedUpdate(DocumentEvent event) { update(); }
        });
        JPanel toolbar = new JPanel(new BorderLayout(12, 0));
        toolbar.setOpaque(false);
        toolbar.setBorder(new EmptyBorder(0, 0, 0, 10));
        toolbar.add(search, BorderLayout.CENTER);
        JButton export = new RoundedButton(I18n.text("Exportar"));
        UiStyles.actionButton(export, AppTheme.PURPLE);
        export.setToolTipText(I18n.text(words.isEmpty()
                ? "No hay datos para exportar."
                : "Exportar una lista simple de palabras"));
        export.addActionListener(event -> exportWords());
        toolbar.add(export, BorderLayout.EAST);
        body.add(toolbar, BorderLayout.NORTH);
        groupsPanel.setOpaque(false);
        groupsPanel.setLayout(new BoxLayout(groupsPanel, BoxLayout.Y_AXIS));
        groupsPanel.setBorder(new EmptyBorder(0, 0, 0, 10));
        JScrollPane scroll = new JScrollPane(groupsPanel);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setHorizontalScrollBarPolicy(
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUnitIncrement(18);
        body.add(scroll, BorderLayout.CENTER);
        refreshGroups("");
        return body;
    }

    private void refreshGroups(String searchText) {
        String query = searchText == null
                ? "" : searchText.strip().toLowerCase(Locale.ROOT);
        Map<String, List<LookedUpWord>> grouped = words.stream()
                .filter(word -> matches(word, query))
                .collect(java.util.stream.Collectors.groupingBy(
                        this::bookKey, LinkedHashMap::new,
                        java.util.stream.Collectors.toList()));

        groupsPanel.removeAll();
        if (grouped.isEmpty()) {
            JLabel empty = label(I18n.text("No se encontraron palabras."),
                    14, Font.PLAIN, AppTheme.MUTED_TEXT);
            empty.setBorder(new EmptyBorder(24, 8, 0, 8));
            groupsPanel.add(empty);
        } else {
            boolean searching = !query.isEmpty();
            grouped.forEach((key, bookWords) -> {
                groupsPanel.add(createBookGroup(key, bookWords, searching));
                groupsPanel.add(Box.createVerticalStrut(10));
            });
        }
        groupsPanel.revalidate();
        groupsPanel.repaint();
    }

    private JPanel createBookGroup(String key, List<LookedUpWord> bookWords,
            boolean searching) {
        LookedUpWord first = bookWords.get(0);
        RoundedPanel card = new RoundedPanel(16, AppTheme.PANEL);
        card.setLayout(new BorderLayout());
        card.setAlignmentX(LEFT_ALIGNMENT);

        JPanel header = new JPanel(new BorderLayout(14, 0));
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(14, 18, 14, 18));
        header.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        JPanel titleBlock = verticalPanel();
        titleBlock.add(label(textOr(first.bookTitle(), "Libro desconocido"),
                15, Font.BOLD, AppTheme.TEXT));
        titleBlock.add(Box.createVerticalStrut(3));
        titleBlock.add(label(textOr(first.bookAuthor(), "Autor desconocido"),
                12, Font.PLAIN, AppTheme.MUTED_TEXT));
        header.add(titleBlock, BorderLayout.CENTER);

        JPanel countBlock = new JPanel(new BorderLayout(10, 0));
        countBlock.setOpaque(false);
        countBlock.add(label(bookWords.size() + " "
                        + I18n.text(bookWords.size() == 1
                                ? "palabra" : "palabras"),
                12, Font.BOLD, AppTheme.PURPLE), BorderLayout.CENTER);
        JLabel arrow = label("›", 24, Font.PLAIN, AppTheme.MUTED_TEXT);
        countBlock.add(arrow, BorderLayout.EAST);
        header.add(countBlock, BorderLayout.EAST);
        card.add(header, BorderLayout.NORTH);

        JPanel wordGrid = new JPanel(new GridLayout(0, 3, 9, 9));
        wordGrid.setOpaque(false);
        wordGrid.setBorder(new EmptyBorder(2, 18, 18, 18));
        bookWords.forEach(word -> wordGrid.add(createWordCard(word)));
        boolean expanded = searching
                || expandedGroups.getOrDefault(key, false);
        wordGrid.setVisible(expanded);
        arrow.setText(expanded ? "⌄" : "›");
        card.add(wordGrid, BorderLayout.CENTER);
        fitGroupHeight(card);

        MouseAdapter toggle = new MouseAdapter() {
            @Override public void mousePressed(MouseEvent event) {
                if (!SwingUtilities.isLeftMouseButton(event)) return;
                boolean show = !wordGrid.isVisible();
                expandedGroups.put(key, show);
                wordGrid.setVisible(show);
                arrow.setText(show ? "⌄" : "›");
                fitGroupHeight(card);
                card.revalidate();
                groupsPanel.revalidate();
            }
        };
        addMouseListenerRecursively(header, toggle);
        return card;
    }

    private void fitGroupHeight(JPanel card) {
        card.setMaximumSize(new Dimension(
                Integer.MAX_VALUE, card.getPreferredSize().height));
    }

    private JPanel createWordCard(LookedUpWord word) {
        RoundedPanel chip = new RoundedPanel(13, AppTheme.PANEL_ALT);
        chip.setLayout(new BoxLayout(chip, BoxLayout.Y_AXIS));
        chip.setBorder(new EmptyBorder(11, 13, 10, 13));
        JLabel wordLabel = label(textOr(word.text(), "Sin palabra")
                        .toUpperCase(Locale.ROOT),
                13, Font.BOLD, AppTheme.TEXT);
        wordLabel.setAlignmentX(LEFT_ALIGNMENT);
        chip.add(wordLabel);
        chip.add(Box.createVerticalStrut(5));
        JLabel detail = label(dictionaryName(word.dictionarySuffix())
                        + "  ·  " + formatDate(word.dateCreated()),
                11, Font.PLAIN, AppTheme.MUTED_TEXT);
        detail.setAlignmentX(LEFT_ALIGNMENT);
        chip.add(detail);
        return chip;
    }

    private boolean matches(LookedUpWord word, String query) {
        return query.isEmpty()
                || contains(word.text(), query)
                || contains(word.bookTitle(), query)
                || contains(word.bookAuthor(), query)
                || contains(dictionaryName(word.dictionarySuffix()), query);
    }

    private boolean contains(String value, String query) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(query);
    }

    private String bookKey(LookedUpWord word) {
        if (word.volumeId() != null && !word.volumeId().isBlank()) {
            return word.volumeId();
        }
        return textOr(word.bookTitle(), "") + "\u0000"
                + textOr(word.bookAuthor(), "");
    }

    private String dictionaryName(String suffix) {
        if (suffix == null || suffix.isBlank()) return I18n.text("Desconocido");
        return switch (suffix.toLowerCase(Locale.ROOT)) {
            case "-es" -> I18n.text("Español");
            case "-en" -> I18n.text("Inglés");
            case "-fr" -> I18n.text("Francés");
            case "-de" -> I18n.text("Alemán");
            case "-it" -> I18n.text("Italiano");
            case "-pt" -> I18n.text("Portugués");
            default -> suffix.replace("-", "").toUpperCase(Locale.ROOT);
        };
    }

    private String formatDate(String value) {
        if (value == null || value.isBlank()) return I18n.text("Sin fecha");
        return value.length() >= 10 ? value.substring(0, 10) : value;
    }

    private void addMouseListenerRecursively(Component component,
            MouseAdapter listener) {
        component.addMouseListener(listener);
        if (component instanceof java.awt.Container container) {
            for (Component child : container.getComponents()) {
                addMouseListenerRecursively(child, listener);
            }
        }
    }

    private JPanel verticalPanel() {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        return panel;
    }

    private static final class ScrollableGroupsPanel extends JPanel
            implements Scrollable {
        @Override public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }
        @Override public int getScrollableUnitIncrement(
                Rectangle visibleRect, int orientation, int direction) {
            return 18;
        }
        @Override public int getScrollableBlockIncrement(
                Rectangle visibleRect, int orientation, int direction) {
            return Math.max(80, visibleRect.height - 40);
        }
        @Override public boolean getScrollableTracksViewportWidth() {
            return true;
        }
        @Override public boolean getScrollableTracksViewportHeight() {
            return false;
        }
    }

    private JLabel label(String text, int size, int style,
            java.awt.Color color) {
        JLabel result = new JLabel(text);
        result.setFont(AppTheme.font(style, size));
        result.setForeground(color);
        return result;
    }
}
