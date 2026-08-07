package com.arcac.managerkobo.ui.panels;

import com.arcac.managerkobo.model.Book;
import com.arcac.managerkobo.model.Bookmark;
import com.arcac.managerkobo.ui.components.BookCard;
import com.arcac.managerkobo.ui.table.BookTableModel;
import com.arcac.managerkobo.ui.table.BookTableModel.BookFilter;
import com.arcac.managerkobo.ui.theme.AppTheme;
import com.arcac.managerkobo.ui.components.RoundedButton;
import com.arcac.managerkobo.ui.util.I18n;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Rectangle;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.Scrollable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/** Biblioteca visual en forma de cuadrícula de portadas. */
public class LibraryPanel extends JPanel {
    private static final int CARD_GAP = 18;

    private final BookTableModel bookModel;
    private final Consumer<Book> openBookAction;
    private final BookGridPanel grid = new BookGridPanel();
    private final JLabel resultCount = new JLabel();

    public LibraryPanel(List<Book> books, List<Bookmark> highlights,
            Consumer<Book> openBookAction) {
        this.bookModel = new BookTableModel(books, highlights);
        this.openBookAction = openBookAction;
        setLayout(new BorderLayout());
        setBackground(AppTheme.BACKGROUND);
        add(createHeader(books.size()), BorderLayout.NORTH);
        add(createContent(), BorderLayout.CENTER);
        bookModel.addTableModelListener(event -> refreshCards());
        refreshCards();
    }

    private JPanel createHeader(int totalBooks) {
        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBorder(new EmptyBorder(30, 32, 20, 32));
        header.add(label("Mi Biblioteca", 29, Font.BOLD, AppTheme.TEXT));
        header.add(Box.createVerticalStrut(5));
        header.add(label(totalBooks
                + " libros encontrados · Pulsa una portada para abrir el libro",
                14, Font.PLAIN, AppTheme.MUTED_TEXT));
        return header;
    }

    private JPanel createContent() {
        JPanel body = new JPanel(new BorderLayout(0, 16));
        body.setOpaque(false);
        body.setBorder(new EmptyBorder(8, 32, 30, 32));
        body.add(createToolbar(), BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(grid);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setHorizontalScrollBarPolicy(
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUnitIncrement(20);
        body.add(scroll, BorderLayout.CENTER);
        return body;
    }

    private JPanel createToolbar() {
        JPanel toolbar = new JPanel(new BorderLayout(12, 0));
        toolbar.setOpaque(false);

        JTextField search = new JTextField();
        search.putClientProperty("JTextField.placeholderText",
                "Buscar título o autor...");
        search.setPreferredSize(new Dimension(360, 40));
        search.getDocument().addDocumentListener(new DocumentListener() {
            private void update() { bookModel.filter(search.getText()); }
            @Override public void insertUpdate(DocumentEvent event) { update(); }
            @Override public void removeUpdate(DocumentEvent event) { update(); }
            @Override public void changedUpdate(DocumentEvent event) { update(); }
        });

        JButton filter = new RoundedButton("Filtrar ▾");
        filter.setBackground(AppTheme.PURPLE);
        filter.setForeground(java.awt.Color.WHITE);
        filter.setFont(AppTheme.font(Font.BOLD, 13));
        filter.setFocusPainted(false);
        filter.setBorder(new EmptyBorder(11, 17, 11, 17));
        JPopupMenu filterMenu = createFilterMenu(filter);
        filter.addActionListener(event ->
                filterMenu.show(filter, 0, filter.getHeight()));

        resultCount.setFont(AppTheme.font(Font.PLAIN, 12));
        resultCount.setForeground(AppTheme.MUTED_TEXT);
        resultCount.setBorder(new EmptyBorder(0, 4, 0, 4));
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        actions.add(resultCount);
        actions.add(filter);

        toolbar.add(search, BorderLayout.CENTER);
        toolbar.add(actions, BorderLayout.EAST);
        return toolbar;
    }

    private JPopupMenu createFilterMenu(JButton filterButton) {
        JPopupMenu menu = new JPopupMenu();
        JPanel options = new JPanel();
        options.setBorder(new EmptyBorder(10, 12, 10, 12));
        options.setLayout(new BoxLayout(options, BoxLayout.Y_AXIS));

        Map<BookFilter, JCheckBox> checks = new LinkedHashMap<>();
        checks.put(BookFilter.READING, new JCheckBox(I18n.text("Leyendo")));
        checks.put(BookFilter.FINISHED, new JCheckBox(I18n.text("Terminados")));
        checks.put(BookFilter.NOT_STARTED, new JCheckBox(I18n.text("Sin empezar")));
        checks.put(BookFilter.WITH_HIGHLIGHTS,
                new JCheckBox(I18n.text("Con subrayados")));
        checks.values().forEach(check -> {
            check.setAlignmentX(LEFT_ALIGNMENT);
            options.add(check);
        });
        options.add(Box.createVerticalStrut(8));

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        actions.setAlignmentX(LEFT_ALIGNMENT);
        JButton clear = compactFilterButton("✕", "Limpiar filtros");
        clear.addActionListener(event -> {
            checks.values().forEach(check -> check.setSelected(false));
            bookModel.setBookFilters(EnumSet.noneOf(BookFilter.class));
            filterButton.setText(I18n.text("Filtrar ▾"));
            menu.setVisible(false);
        });
        JButton apply = compactFilterButton("✓", "Aplicar filtros");
        apply.addActionListener(event -> {
            EnumSet<BookFilter> selected = EnumSet.noneOf(BookFilter.class);
            checks.forEach((filter, check) -> {
                if (check.isSelected()) selected.add(filter);
            });
            bookModel.setBookFilters(selected);
            filterButton.setText(selected.isEmpty()
                    ? I18n.text("Filtrar ▾")
                    : I18n.text("Filtrar") + " (" + selected.size() + ") ▾");
            menu.setVisible(false);
        });
        actions.add(clear);
        actions.add(apply);
        options.add(actions);
        menu.add(options);
        return menu;
    }

    private JButton compactFilterButton(String text, String tooltip) {
        JButton button = new RoundedButton(text);
        button.setToolTipText(I18n.text(tooltip));
        button.setPreferredSize(new Dimension(34, 30));
        button.setFocusPainted(false);
        return button;
    }

    private void refreshCards() {
        grid.removeAll();
        for (int row = 0; row < bookModel.getRowCount(); row++) {
            Book book = bookModel.getBookAt(row);
            if (book != null) grid.add(new BookCard(book, openBookAction));
        }
        resultCount.setText(I18n.text(bookModel.getRowCount() + " libros"));
        grid.revalidate();
        grid.repaint();
        SwingUtilities.invokeLater(() -> grid.scrollRectToVisible(
                new Rectangle(0, 0, 1, 1)));
    }

    private JLabel label(String text, int size, int style,
            java.awt.Color color) {
        JLabel label = new JLabel(text);
        label.setFont(AppTheme.font(style, size));
        label.setForeground(color);
        return label;
    }

    /** Cuadrícula que recalcula columnas y altura según el ancho disponible. */
    private static final class BookGridPanel extends JPanel
            implements Scrollable {
        BookGridPanel() {
            setOpaque(false);
            setBorder(new EmptyBorder(0, 0, 0, 8));
        }

        @Override
        public void doLayout() {
            Insets insets = getInsets();
            int width = Math.max(BookCard.CARD_WIDTH,
                    getWidth() - insets.left - insets.right);
            int columns = Math.max(1,
                    (width + CARD_GAP) / (BookCard.CARD_WIDTH + CARD_GAP));
            int cardWidth = Math.max(BookCard.CARD_WIDTH,
                    (width - (columns - 1) * CARD_GAP) / columns);
            int startX = insets.left;

            for (int index = 0; index < getComponentCount(); index++) {
                int column = index % columns;
                int row = index / columns;
                getComponent(index).setBounds(
                        startX + column * (cardWidth + CARD_GAP),
                        insets.top + row * (BookCard.CARD_HEIGHT + CARD_GAP),
                        cardWidth, BookCard.CARD_HEIGHT);
            }
        }

        @Override
        public Dimension getPreferredSize() {
            Insets insets = getInsets();
            int width = Math.max(BookCard.CARD_WIDTH,
                    getParent() == null ? getWidth() : getParent().getWidth());
            int contentWidth = Math.max(BookCard.CARD_WIDTH,
                    width - insets.left - insets.right);
            int columns = Math.max(1, (contentWidth + CARD_GAP)
                    / (BookCard.CARD_WIDTH + CARD_GAP));
            int rows = Math.max(1,
                    (int) Math.ceil(getComponentCount() / (double) columns));
            int height = insets.top + insets.bottom
                    + rows * BookCard.CARD_HEIGHT
                    + Math.max(0, rows - 1) * CARD_GAP;
            return new Dimension(width, height);
        }

        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        @Override
        public int getScrollableUnitIncrement(
                Rectangle visibleRect, int orientation, int direction) {
            return 20;
        }

        @Override
        public int getScrollableBlockIncrement(
                Rectangle visibleRect, int orientation, int direction) {
            return Math.max(80, visibleRect.height - 40);
        }

        @Override
        public boolean getScrollableTracksViewportWidth() {
            return true;
        }

        @Override
        public boolean getScrollableTracksViewportHeight() {
            return false;
        }
    }
}
