package com.arcac.managerkobo.ui.components;

import com.arcac.managerkobo.model.Book;
import com.arcac.managerkobo.model.Bookmark;
import com.arcac.managerkobo.ai.HighlightAiService.Operation;
import com.arcac.managerkobo.service.BookCoverService;
import com.arcac.managerkobo.service.HighlightExportService;
import com.arcac.managerkobo.service.HighlightExportService.ExportFormat;
import com.arcac.managerkobo.ui.components.HighlightListItem.BookGroup;
import com.arcac.managerkobo.ui.components.HighlightListItem.Highlight;
import com.arcac.managerkobo.ui.dialogs.AiSummaryDialog;
import com.arcac.managerkobo.ui.theme.AppTheme;
import com.arcac.managerkobo.ui.util.I18n;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Color;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JMenuItem;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.ImageIcon;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import static com.arcac.managerkobo.util.ReadingFormat.textOr;

/** Lista plana o agrupada por libro, con búsqueda y grupos desplegables. */
public class HighlightListPanel extends JPanel {
    private static final int LARGE_GROUP_THRESHOLD = 40;

    private final List<Bookmark> allHighlights;
    private final boolean groupByBook;
    private final Map<String, Book> booksByContentId;
    private final Map<String, ImageIcon> coverIcons = new HashMap<>();
    private final Set<String> resolvedCoverIds = new HashSet<>();
    private final BookCoverService coverService = new BookCoverService();
    private final Set<String> expandedGroupIds = new HashSet<>();
    private final Set<String> loadingGroupIds = new HashSet<>();
    private final Set<String> selectedHighlightIds = new HashSet<>();
    private final DefaultListModel<HighlightListItem> listModel =
            new DefaultListModel<>();
    private final JList<HighlightListItem> highlightList =
            new ViewportWidthList<>(listModel);
    private final JLabel resultCount = new JLabel();
    private final JLabel emptyMessage = new JLabel("No se encontraron subrayados.");
    private final JTextField search = new JTextField();
    private final HighlightExportService exportService = new HighlightExportService();
    private List<Bookmark> currentVisibleHighlights = List.of();
    private boolean selectionMode;
    private JScrollPane scrollPane;
    private JPanel toolbar;

    public HighlightListPanel(List<Bookmark> highlights, boolean groupByBook) {
        this(highlights, groupByBook, List.of());
    }

    public HighlightListPanel(List<Bookmark> highlights, boolean groupByBook,
            List<Book> books) {
        this.allHighlights = highlights == null ? List.of() : new ArrayList<>(highlights);
        this.groupByBook = groupByBook;
        this.booksByContentId = books == null ? Map.of() : books.stream()
                .filter(book -> book.getContentId() != null)
                .collect(Collectors.toMap(
                        Book::getContentId,
                        book -> book,
                        (first, duplicate) -> first));
        setLayout(new BorderLayout(0, 14));
        setOpaque(false);
        add(createToolbar(), BorderLayout.NORTH);
        add(createScrollPane(), BorderLayout.CENTER);
        configureSearch();
        refresh(true);
    }

    private JPanel createToolbar() {
        toolbar = new JPanel();
        toolbar.setOpaque(false);
        search.putClientProperty("JTextField.placeholderText",
                groupByBook
                        ? "Buscar libro, autor o texto subrayado..."
                        : "Buscar en los subrayados...");
        search.setPreferredSize(new Dimension(360, 40));
        resultCount.setForeground(AppTheme.MUTED_TEXT);
        resultCount.setFont(AppTheme.font(Font.PLAIN, 12));
        toolbar.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent event) {
                configureToolbarLayout(toolbar.getWidth());
            }
        });
        configureToolbarLayout(0);
        return toolbar;
    }

    private void configureToolbarLayout(int width) {
        // Con varias acciones, una sola fila necesita bastante anchura.
        // Cambiamos antes al diseño vertical para no comprimir ni desbordar la lista.
        boolean compact = width < 900;
        Object previousMode = toolbar.getClientProperty("compactLayout");
        if (previousMode instanceof Boolean && (Boolean) previousMode == compact) {
            return;
        }

        toolbar.putClientProperty("compactLayout", compact);
        toolbar.removeAll();
        if (compact) {
            toolbar.setLayout(new BoxLayout(toolbar, BoxLayout.Y_AXIS));
            search.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
            search.setAlignmentX(LEFT_ALIGNMENT);
            toolbar.add(search);
            toolbar.add(Box.createVerticalStrut(7));
            resultCount.setAlignmentX(LEFT_ALIGNMENT);
            toolbar.add(resultCount);
            if (groupByBook) {
                toolbar.add(Box.createVerticalStrut(8));
                JPanel actions = createActions(true);
                actions.setAlignmentX(LEFT_ALIGNMENT);
                toolbar.add(actions);
            }
        } else {
            toolbar.setLayout(new BorderLayout(12, 6));
            toolbar.add(search, BorderLayout.CENTER);
            if (groupByBook) {
                toolbar.add(createActions(false), BorderLayout.EAST);
            }
            toolbar.add(resultCount, BorderLayout.SOUTH);
        }
        toolbar.revalidate();
        toolbar.repaint();
    }

    private JPanel createActions(boolean compact) {
        JPanel actions = new JPanel();
        actions.setOpaque(false);
        if (compact) {
            int rows = selectionMode ? 3 : 1;
            actions.setLayout(new GridLayout(rows, 2, 6, 6));
            actions.setMaximumSize(new Dimension(
                    Integer.MAX_VALUE, selectionMode ? 124 : 40));
        }

        if (selectionMode) {
            JButton cancel = actionButton("Cancelar", AppTheme.PANEL_ALT);
            cancel.addActionListener(event -> leaveSelectionMode());
            actions.add(cancel);

            JButton clear = actionButton("Limpiar", AppTheme.PANEL_ALT);
            clear.setToolTipText(I18n.text("Desmarcar todos los subrayados"));
            clear.addActionListener(event -> {
                selectedHighlightIds.clear();
                refresh(false);
            });
            actions.add(clear);

            JButton copy = actionButton("Copiar", AppTheme.PURPLE);
            copy.addActionListener(event -> copySelectedHighlights());
            actions.add(copy);

            JButton export = actionButton("Exportar", AppTheme.GREEN);
            export.setToolTipText(I18n.text(
                    "Exportar los subrayados seleccionados"));
            export.addActionListener(event -> exportHighlights(selectedHighlights()));
            actions.add(export);

            JButton ai = actionButton(I18n.text("✨ Acciones IA ▾"), AppTheme.PURPLE);
            ai.setToolTipText(I18n.text("Aplicar una acción inteligente a la selección"));
            JPopupMenu aiMenu = createAiMenu();
            ai.addActionListener(event -> aiMenu.show(ai, 0, ai.getHeight()));
            actions.add(ai);
        } else {
            JButton select = actionButton("Seleccionar", AppTheme.PURPLE);
            select.addActionListener(event -> enterSelectionMode());
            actions.add(select);

            JButton export = actionButton("Exportar", AppTheme.GREEN);
            JPopupMenu exportMenu = createExportMenu();
            export.addActionListener(event ->
                    exportMenu.show(export, 0, export.getHeight()));
            actions.add(export);
        }
        return actions;
    }

    private JPopupMenu createAiMenu() {
        JPopupMenu menu = new JPopupMenu();
        JMenuItem summary = new JMenuItem(I18n.text("Resumir"));
        summary.addActionListener(event -> openAiAction(Operation.SUMMARY, null));
        menu.add(summary);

        JMenuItem ideas = new JMenuItem(I18n.text("Ideas clave"));
        ideas.addActionListener(event -> openAiAction(Operation.KEY_IDEAS, null));
        menu.add(ideas);

        JMenuItem question = new JMenuItem(I18n.text("Preguntar..."));
        question.addActionListener(event -> askQuestion());
        menu.add(question);
        return menu;
    }

    private void askQuestion() {
        if (selectedHighlights().isEmpty()) {
            showWarning("Selecciona uno o varios subrayados para usar Gemini.");
            return;
        }
        String question = JOptionPane.showInputDialog(this,
                I18n.text("¿Qué quieres preguntar sobre los subrayados seleccionados?"),
                I18n.text("Preguntar a Gemini"), JOptionPane.QUESTION_MESSAGE);
        if (question != null && !question.isBlank()) {
            openAiAction(Operation.QUESTION, question.strip());
        }
    }

    private void openAiAction(Operation operation, String question) {
        List<Bookmark> selected = selectedHighlights();
        if (selected.isEmpty()) {
            showWarning("Selecciona uno o varios subrayados para usar Gemini.");
            return;
        }
        AiSummaryDialog dialog = new AiSummaryDialog(
                SwingUtilities.getWindowAncestor(this), selected,
                operation, question);
        dialog.setVisible(true);
    }

    private JButton actionButton(String text, Color background) {
        JButton button = new RoundedButton(I18n.text(text));
        button.putClientProperty("JButton.buttonType", "roundRect");
        button.putClientProperty("JComponent.roundRect", true);
        button.setFont(AppTheme.font(Font.BOLD, 12));
        button.setForeground(Color.WHITE);
        button.setBackground(background);
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(9, 10, 9, 10));
        return button;
    }

    private JPopupMenu createExportMenu() {
        JPopupMenu menu = new JPopupMenu();
        JMenuItem visible = new JMenuItem(I18n.text("Exportar resultados visibles"));
        visible.addActionListener(event -> exportHighlights(currentVisibleHighlights));
        menu.add(visible);

        JMenuItem all = new JMenuItem(I18n.text("Exportar todos"));
        all.addActionListener(event -> exportHighlights(allHighlights));
        menu.add(all);
        return menu;
    }

    private JScrollPane createScrollPane() {
        highlightList.setCellRenderer(new GroupedHighlightCellRenderer());
        highlightList.setBackground(AppTheme.BACKGROUND);
        highlightList.setForeground(AppTheme.TEXT);
        highlightList.setOpaque(false);
        highlightList.setFixedCellHeight(-1);
        highlightList.setBorder(null);
        highlightList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                handleListClick(event.getPoint());
            }
        });

        emptyMessage.setForeground(AppTheme.MUTED_TEXT);
        emptyMessage.setBorder(new EmptyBorder(30, 8, 0, 8));
        scrollPane = new JScrollPane(highlightList);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(18);
        scrollPane.getViewport().addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent event) {
                highlightList.setFixedCellHeight(1);
                highlightList.setFixedCellHeight(-1);
                highlightList.revalidate();
            }
        });
        return scrollPane;
    }

    private void configureSearch() {
        search.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent event) { refresh(true); }
            @Override public void removeUpdate(DocumentEvent event) { refresh(true); }
            @Override public void changedUpdate(DocumentEvent event) { refresh(true); }
        });
    }

    private void handleListClick(Point point) {
        if (!groupByBook || listModel.isEmpty()) {
            return;
        }
        int index = highlightList.locationToIndex(point);
        Rectangle bounds = index < 0 ? null : highlightList.getCellBounds(index, index);
        if (bounds == null || !bounds.contains(point)) {
            return;
        }
        HighlightListItem item = listModel.get(index);
        if (item instanceof BookGroup group) {
            if (group.loading()) {
                return;
            }
            int relativeX = point.x - bounds.x;
            boolean arrowClicked = selectionMode
                    ? relativeX >= 40 && relativeX <= 82
                    : relativeX <= 48;
            if (selectionMode && !arrowClicked) {
                toggleBookSelection(group.groupId());
            } else {
                if (group.expanded()) {
                    expandedGroupIds.remove(group.groupId());
                    refresh(false);
                } else if (group.highlightCount() >= LARGE_GROUP_THRESHOLD) {
                    expandLargeGroup(group.groupId());
                } else {
                    expandedGroupIds.add(group.groupId());
                    refresh(false);
                }
            }
        } else if (selectionMode && item instanceof Highlight highlight) {
            String id = selectionId(highlight.bookmark());
            if (!selectedHighlightIds.add(id)) {
                selectedHighlightIds.remove(id);
            }
            highlightList.clearSelection();
            refresh(false);
        }
    }

    private void refresh(boolean moveToTop) {
        Point previousPosition = scrollPane.getViewport().getViewPosition();
        String query = normalized(search.getText());
        List<Bookmark> visible = filteredHighlights(query);
        currentVisibleHighlights = List.copyOf(visible);

        listModel.clear();
        if (groupByBook) {
            populateGroupedModel(visible, query);
        } else {
            visible.forEach(mark -> listModel.addElement(new Highlight(
                    mark, false, false, false)));
        }

        scrollPane.setViewportView(visible.isEmpty() ? emptyMessage : highlightList);
        updateResultCount();
        SwingUtilities.invokeLater(() -> scrollPane.getViewport().setViewPosition(
                moveToTop ? new Point(0, 0) : previousPosition));
    }

    private void updateResultCount() {
        int selected = selectedHighlightIds.size();
        String text = currentVisibleHighlights.size() + " subrayados";
        if (groupByBook) {
            text += " · " + countGroups(currentVisibleHighlights) + " libros";
        }
        if (selected > 0) {
            text += " · " + selected + " seleccionados";
        }
        resultCount.setText(I18n.text(text));
    }

    private List<Bookmark> selectedHighlights() {
        return allHighlights.stream()
                .filter(mark -> selectedHighlightIds.contains(selectionId(mark)))
                .toList();
    }

    private void enterSelectionMode() {
        selectionMode = true;
        rebuildToolbar();
        refresh(false);
    }

    private void leaveSelectionMode() {
        selectionMode = false;
        selectedHighlightIds.clear();
        rebuildToolbar();
        refresh(false);
    }

    private void rebuildToolbar() {
        toolbar.putClientProperty("compactLayout", null);
        configureToolbarLayout(toolbar.getWidth());
    }

    private void toggleBookSelection(String groupId) {
        List<Bookmark> bookHighlights = currentVisibleHighlights.stream()
                .filter(mark -> groupId(mark).equals(groupId))
                .toList();
        boolean allSelected = !bookHighlights.isEmpty()
                && bookHighlights.stream().allMatch(mark ->
                        selectedHighlightIds.contains(selectionId(mark)));
        if (allSelected) {
            bookHighlights.forEach(mark ->
                    selectedHighlightIds.remove(selectionId(mark)));
        } else {
            bookHighlights.forEach(mark ->
                    selectedHighlightIds.add(selectionId(mark)));
        }
        refresh(false);
    }

    private void copySelectedHighlights() {
        List<Bookmark> selected = selectedHighlights();
        if (selected.isEmpty()) {
            showWarning("Selecciona uno o varios subrayados para copiarlos.");
            return;
        }
        String text = selected.stream()
                .map(this::copyText)
                .collect(Collectors.joining(System.lineSeparator()
                        + System.lineSeparator()));
        Toolkit.getDefaultToolkit().getSystemClipboard()
                .setContents(new StringSelection(text), null);
        JOptionPane.showMessageDialog(this,
                selected.size() + " subrayados copiados al portapapeles.",
                "Copiar subrayados", JOptionPane.INFORMATION_MESSAGE);
    }

    private String copyText(Bookmark mark) {
        StringBuilder text = new StringBuilder();
        text.append(textOr(mark.getBookTitle(), "Libro desconocido"));
        if (mark.getBookAuthor() != null && !mark.getBookAuthor().isBlank()) {
            text.append(" — ").append(mark.getBookAuthor());
        }
        text.append(System.lineSeparator())
                .append(textOr(mark.getText(), ""));
        if (mark.hasUserNote()) {
            text.append(System.lineSeparator())
                    .append("Nota: ").append(mark.getUserNote());
        }
        return text.toString();
    }

    private void exportHighlights(List<Bookmark> highlights) {
        if (highlights.isEmpty()) {
            showWarning("No hay subrayados para exportar en esta opción.");
            return;
        }

        ExportFormat format = (ExportFormat) JOptionPane.showInputDialog(
                this,
                "Selecciona el formato del archivo:",
                "Formato de exportación",
                JOptionPane.QUESTION_MESSAGE,
                null,
                ExportFormat.values(),
                ExportFormat.TXT);
        if (format == null) {
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Exportar subrayados");
        String extension = format.extension().substring(1);
        chooser.setFileFilter(new FileNameExtensionFilter(
                format + " (*" + format.extension() + ")", extension));
        chooser.setSelectedFile(new File(
                "subrayados_kobo" + format.extension()));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        Path destination = withExtension(
                chooser.getSelectedFile().toPath(), format.extension());
        if (destination.toFile().exists()) {
            int answer = JOptionPane.showConfirmDialog(this,
                    "El archivo ya existe. ¿Quieres reemplazarlo?",
                    "Confirmar exportación", JOptionPane.YES_NO_OPTION);
            if (answer != JOptionPane.YES_OPTION) {
                return;
            }
        }

        try {
            exportService.export(highlights, destination, format);
            JOptionPane.showMessageDialog(this,
                    highlights.size() + " subrayados exportados en "
                            + format + " correctamente.",
                    "Exportación completada", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException exception) {
            JOptionPane.showMessageDialog(this,
                    "No se pudo crear el archivo: " + exception.getMessage(),
                    "Error de exportación", JOptionPane.ERROR_MESSAGE);
        }
    }

    private Path withExtension(Path path, String extension) {
        String fileName = path.getFileName().toString();
        if (fileName.toLowerCase(Locale.ROOT).endsWith(extension)) {
            return path;
        }
        String baseName = fileName.replaceFirst("(?i)\\.(csv|txt|pdf)$", "");
        return path.resolveSibling(baseName + extension);
    }

    private void showWarning(String message) {
        JOptionPane.showMessageDialog(this, message,
                "Subrayados", JOptionPane.WARNING_MESSAGE);
    }

    private List<Bookmark> filteredHighlights(String query) {
        return allHighlights.stream()
                .filter(mark -> contains(mark.getText(), query)
                        || contains(mark.getBookTitle(), query)
                        || contains(mark.getBookAuthor(), query)
                        || contains(mark.getChapterTitle(), query))
                .sorted(Comparator.comparing(
                        Bookmark::getDateCreated,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
    }

    private void populateGroupedModel(List<Bookmark> visible, String query) {
        Map<String, List<Bookmark>> groups = visible.stream()
                .collect(Collectors.groupingBy(
                        this::groupId,
                        LinkedHashMap::new,
                        Collectors.toList()));
        groups.entrySet().stream()
                .sorted(Comparator.comparing(
                        entry -> textOr(entry.getValue().get(0).getBookTitle(),
                                "Libro desconocido"),
                        String.CASE_INSENSITIVE_ORDER))
                .forEach(entry -> {
                    Bookmark first = entry.getValue().get(0);
                    boolean expanded = !query.isEmpty()
                            || expandedGroupIds.contains(entry.getKey());
                    listModel.addElement(new BookGroup(
                            entry.getKey(),
                            textOr(first.getBookTitle(), "Libro desconocido"),
                            textOr(first.getBookAuthor(), "Autor desconocido"),
                            entry.getValue().size(),
                            (int) entry.getValue().stream()
                                    .filter(mark -> selectedHighlightIds.contains(
                                            selectionId(mark)))
                                    .count(),
                            selectionMode,
                            expanded,
                            loadingGroupIds.contains(entry.getKey()),
                            coverIcons.get(entry.getKey())));
                    requestCover(entry.getKey());
                    if (expanded) {
                        entry.getValue().forEach(mark ->
                                listModel.addElement(new Highlight(
                                        mark,
                                        true,
                                        selectionMode,
                                        selectedHighlightIds.contains(selectionId(mark)))));
                    }
                });
    }

    private void expandLargeGroup(String groupId) {
        loadingGroupIds.add(groupId);
        refresh(false);

        Timer deferredExpansion = new Timer(90, event -> {
            loadingGroupIds.remove(groupId);
            expandedGroupIds.add(groupId);
            refresh(false);
        });
        deferredExpansion.setRepeats(false);
        deferredExpansion.start();
    }

    private void requestCover(String groupId) {
        Book book = booksByContentId.get(groupId);
        if (book == null || !resolvedCoverIds.add(groupId)) {
            return;
        }

        coverService.loadAsync(book, 34, 42, loadedCover -> {
            if (loadedCover != null) {
                coverIcons.put(groupId, loadedCover);
                refresh(false);
            }
        });
    }

    private int countGroups(List<Bookmark> highlights) {
        return (int) highlights.stream().map(this::groupId).distinct().count();
    }

    private String groupId(Bookmark mark) {
        if (mark.getVolumeId() != null && !mark.getVolumeId().isBlank()) {
            return mark.getVolumeId();
        }
        return "unknown:" + textOr(mark.getBookTitle(), "Libro desconocido");
    }

    private String selectionId(Bookmark mark) {
        if (mark.getBookmarkId() != null && !mark.getBookmarkId().isBlank()) {
            return mark.getBookmarkId();
        }
        return groupId(mark) + ":" + textOr(mark.getContentId(), "")
                + ":" + textOr(mark.getDateCreated(), "")
                + ":" + textOr(mark.getText(), "").hashCode();
    }

    private boolean contains(String value, String query) {
        return query.isEmpty() || (value != null && normalized(value).contains(query));
    }

    private String normalized(String value) {
        return value == null ? "" : value.strip().toLowerCase(Locale.ROOT);
    }

    /** Evita que el ancho preferido de los renderers ensanche la lista. */
    private static final class ViewportWidthList<E> extends JList<E> {
        private ViewportWidthList(DefaultListModel<E> model) {
            super(model);
        }

        @Override
        public boolean getScrollableTracksViewportWidth() {
            return true;
        }
    }
}
