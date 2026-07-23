package com.arcac.managerkobo.ui.components;

import com.arcac.managerkobo.model.Bookmark;
import com.arcac.managerkobo.ui.theme.AppTheme;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.Component;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.Scrollable;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/** Listado reutilizable de subrayados con búsqueda y filtro opcional por libro. */
public class HighlightListPanel extends JPanel {
    private static final String ALL_BOOKS = "Todos los libros";
    private final List<Bookmark> allHighlights;
    private final ScrollableCardsPanel cards = new ScrollableCardsPanel();
    private final JLabel resultCount = new JLabel();
    private final JTextField search = new JTextField();
    private final JComboBox<String> bookFilter = new JComboBox<>();
    private final boolean showBookFilter;
    private JScrollPane scrollPane;
    private JPanel toolbar;

    public HighlightListPanel(List<Bookmark> highlights, boolean showBookFilter) {
        this.allHighlights = highlights == null ? List.of() : new ArrayList<>(highlights);
        this.showBookFilter = showBookFilter;
        setLayout(new BorderLayout(0, 14));
        setOpaque(false);
        add(createToolbar(), BorderLayout.NORTH);
        add(createScrollPane(), BorderLayout.CENTER);
        configureFilters();
        refresh();
    }

    private JPanel createToolbar() {
        toolbar = new JPanel();
        toolbar.setOpaque(false);
        search.putClientProperty("JTextField.placeholderText", "Buscar en los subrayados...");
        search.setPreferredSize(new Dimension(360, 40));

        if (showBookFilter) {
            bookFilter.addItem(ALL_BOOKS);
            allHighlights.stream().map(Bookmark::getBookTitle)
                    .filter(title -> title != null && !title.isBlank()).distinct()
                    .sorted(String.CASE_INSENSITIVE_ORDER).forEach(bookFilter::addItem);
            bookFilter.setPreferredSize(new Dimension(250, 40));
        }
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
        boolean compact = width < 620;
        Object previousMode = toolbar.getClientProperty("compactLayout");
        if (previousMode instanceof Boolean && (Boolean) previousMode == compact) return;
        toolbar.putClientProperty("compactLayout", compact);
        toolbar.removeAll();

        if (compact) {
            toolbar.setLayout(new BoxLayout(toolbar, BoxLayout.Y_AXIS));
            search.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
            search.setAlignmentX(LEFT_ALIGNMENT);
            toolbar.add(search);
            if (showBookFilter) {
                toolbar.add(Box.createVerticalStrut(8));
                bookFilter.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
                bookFilter.setAlignmentX(LEFT_ALIGNMENT);
                toolbar.add(bookFilter);
            }
            toolbar.add(Box.createVerticalStrut(7));
            resultCount.setAlignmentX(LEFT_ALIGNMENT);
            toolbar.add(resultCount);
        } else {
            toolbar.setLayout(new BorderLayout(12, 6));
            toolbar.add(search, BorderLayout.CENTER);
            if (showBookFilter) toolbar.add(bookFilter, BorderLayout.EAST);
            toolbar.add(resultCount, BorderLayout.SOUTH);
        }
        toolbar.revalidate();
        toolbar.repaint();
        revalidate();
    }

    private JScrollPane createScrollPane() {
        cards.setOpaque(false);
        cards.setLayout(new BoxLayout(cards, BoxLayout.Y_AXIS));
        scrollPane = new JScrollPane(cards);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(18);
        return scrollPane;
    }

    private void configureFilters() {
        search.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { refresh(); }
            @Override public void removeUpdate(DocumentEvent e) { refresh(); }
            @Override public void changedUpdate(DocumentEvent e) { refresh(); }
        });
        bookFilter.addActionListener(event -> refresh());
    }

    private void refresh() {
        String query = search.getText() == null ? "" : search.getText().strip().toLowerCase();
        String selectedBook = showBookFilter ? (String) bookFilter.getSelectedItem() : ALL_BOOKS;
        List<Bookmark> visible = allHighlights.stream()
                .filter(mark -> contains(mark.getText(), query)
                        || contains(mark.getBookTitle(), query)
                        || contains(mark.getBookAuthor(), query)
                        || contains(mark.getChapterTitle(), query))
                .filter(mark -> selectedBook == null || ALL_BOOKS.equals(selectedBook)
                        || selectedBook.equals(mark.getBookTitle()))
                .sorted(Comparator.comparing(Bookmark::getDateCreated,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();

        cards.removeAll();
        if (visible.isEmpty()) {
            JLabel empty = new JLabel("No se encontraron subrayados.");
            empty.setForeground(AppTheme.MUTED_TEXT);
            empty.setBorder(new EmptyBorder(30, 8, 0, 8));
            cards.add(empty);
        } else {
            visible.forEach(mark -> {
                cards.add(createCard(mark));
                cards.add(Box.createVerticalStrut(10));
            });
        }
        resultCount.setText(visible.size() + " subrayados");
        cards.revalidate();
        cards.repaint();
        SwingUtilities.invokeLater(() ->
                scrollPane.getViewport().setViewPosition(new Point(0, 0)));
    }

    private JPanel createCard(Bookmark mark) {
        RoundedPanel card = new RoundedPanel(16, AppTheme.PANEL);
        card.setLayout(new BorderLayout(12, 10));
        card.setBorder(new EmptyBorder(15, 18, 15, 18));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 180));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel book = new JLabel(fallback(mark.getBookTitle(), "Libro desconocido"));
        book.setFont(AppTheme.font(Font.BOLD, 14));
        book.setForeground(AppTheme.PURPLE);
        header.add(book, BorderLayout.CENTER);
        JLabel date = new JLabel(formatDate(mark.getDateCreated()));
        date.setFont(AppTheme.font(Font.PLAIN, 11));
        date.setForeground(AppTheme.MUTED_TEXT);
        header.add(date, BorderLayout.EAST);
        card.add(header, BorderLayout.NORTH);

        JTextArea quote = new JTextArea(mark.getText());
        quote.setEditable(false);
        quote.setLineWrap(true);
        quote.setWrapStyleWord(true);
        quote.setOpaque(false);
        quote.setForeground(AppTheme.TEXT);
        quote.setFont(AppTheme.font(Font.PLAIN, 14));
        quote.setColumns(1);
        quote.setRows(Math.min(4, Math.max(2, mark.getText().length() / 90 + 1)));
        quote.setCaretPosition(0);
        card.add(quote, BorderLayout.CENTER);

        String meta = fallback(mark.getBookAuthor(), "Autor desconocido");
        meta += " · Color " + mark.getColor();
        JLabel details = new JLabel(meta);
        details.setFont(AppTheme.font(Font.PLAIN, 11));
        details.setForeground(colorFor(mark.getColor()));
        card.add(details, BorderLayout.SOUTH);
        return card;
    }

    private boolean contains(String value, String query) {
        return query.isEmpty() || (value != null && value.toLowerCase().contains(query));
    }

    private String fallback(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private String formatDate(String value) {
        if (value == null || value.isBlank()) return "Sin fecha";
        return value.length() >= 10 ? value.substring(0, 10) : value;
    }

    private Color colorFor(int color) {
        return switch (color) {
            case 1 -> AppTheme.BLUE;
            case 3 -> AppTheme.ORANGE;
            default -> AppTheme.MUTED_TEXT;
        };
    }

    /** Obliga a las tarjetas a utilizar el ancho del viewport, sin desbordar. */
    private static class ScrollableCardsPanel extends JPanel implements Scrollable {
        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        @Override
        public int getScrollableUnitIncrement(Rectangle visibleRect,
                                              int orientation, int direction) {
            return 18;
        }

        @Override
        public int getScrollableBlockIncrement(Rectangle visibleRect,
                                               int orientation, int direction) {
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
