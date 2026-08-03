package com.arcac.managerkobo.ui.components;

import com.arcac.managerkobo.model.Bookmark;
import com.arcac.managerkobo.ui.theme.AppTheme;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import javax.swing.JLabel;
import javax.swing.JCheckBox;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.ListCellRenderer;
import javax.swing.border.EmptyBorder;

/**
 * Dibuja un subrayado como una tarjeta reutilizable dentro de una JList.Solo
 * cambia el contenido sin tener que crear un conjunto nuevo de componentes por
 * cada subrayado.
 */
public class HighlightCellRenderer extends JPanel
        implements ListCellRenderer<Bookmark> {

    private final RoundedPanel card = new RoundedPanel(16, AppTheme.PANEL);
    private final JLabel bookLabel = new JLabel();
    private final JLabel dateLabel = new JLabel();
    private final JTextArea quoteArea = new JTextArea();
    private final JLabel detailsLabel = new JLabel();
    private final JPanel selectionIndicator = new JPanel();
    private final JCheckBox selectionCheck = new JCheckBox();
    private final boolean showBookTitle;

    public HighlightCellRenderer() {
        this(0, true);
    }

    public HighlightCellRenderer(int leftIndent) {
        this(leftIndent, true);
    }

    public HighlightCellRenderer(int leftIndent, boolean showBookTitle) {
        this.showBookTitle = showBookTitle;
        setLayout(new BorderLayout());
        setOpaque(false);
        setBorder(new EmptyBorder(0, leftIndent, 10, 0));
        selectionIndicator.setOpaque(false);
        selectionIndicator.setBackground(AppTheme.PURPLE);
        selectionIndicator.setPreferredSize(new Dimension(4, 0));
        add(selectionIndicator, BorderLayout.WEST);

        card.setLayout(new BorderLayout(12, 10));
        card.setBorder(new EmptyBorder(15, 18, 15, 18));
        selectionCheck.setOpaque(false);
        selectionCheck.setFocusable(false);
        selectionCheck.setVisible(false);
        card.add(selectionCheck, BorderLayout.WEST);

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        bookLabel.setFont(AppTheme.font(Font.BOLD, 14));
        bookLabel.setForeground(AppTheme.PURPLE);
        bookLabel.setVisible(showBookTitle);
        header.add(bookLabel, BorderLayout.CENTER);
        dateLabel.setFont(AppTheme.font(Font.PLAIN, 11));
        dateLabel.setForeground(AppTheme.MUTED_TEXT);
        header.add(dateLabel, BorderLayout.EAST);
        card.add(header, BorderLayout.NORTH);

        quoteArea.setEditable(false);
        quoteArea.setLineWrap(true);
        quoteArea.setWrapStyleWord(true);
        quoteArea.setOpaque(false);
        quoteArea.setForeground(AppTheme.TEXT);
        quoteArea.setFont(AppTheme.font(Font.PLAIN, 14));
        quoteArea.setColumns(1);
        quoteArea.setFocusable(false);
        card.add(quoteArea, BorderLayout.CENTER);

        detailsLabel.setFont(AppTheme.font(Font.PLAIN, 11));
        card.add(detailsLabel, BorderLayout.SOUTH);
        add(card, BorderLayout.CENTER);
    }

    public void configureSelection(boolean selectionMode, boolean marked) {
        selectionCheck.setVisible(selectionMode);
        selectionCheck.setSelected(marked);
    }

    @Override
    public Component getListCellRendererComponent(
            JList<? extends Bookmark> list,
            Bookmark mark,
            int index,
            boolean selected,
            boolean hasFocus) {

        String text = mark.getText() == null ? "" : mark.getText();
        if (showBookTitle) {
            bookLabel.setText(fallback(mark.getBookTitle(), "Libro desconocido"));
        }
        dateLabel.setText(formatDate(mark.getDateCreated()));
        quoteArea.setText(text);
        quoteArea.setCaretPosition(0);

        int listWidth = list == null ? getWidth() : list.getWidth();
        int textWidth = Math.max(180, listWidth - 70);
        quoteArea.setPreferredSize(null);
        quoteArea.setSize(new Dimension(textWidth, Short.MAX_VALUE));
        Dimension textSize = quoteArea.getPreferredSize();
        quoteArea.setPreferredSize(new Dimension(textWidth, textSize.height));

        detailsLabel.setText("Color " + mark.getColor());
        detailsLabel.setForeground(colorFor(mark.getColor()));

        Color selectionColor = selected ? AppTheme.NAV_SELECTED : AppTheme.BACKGROUND;
        setBackground(selectionColor);
        setOpaque(selected);
        selectionIndicator.setOpaque(selected);
        return this;
    }

    private String fallback(String value, String alternative) {
        return value == null || value.isBlank() ? alternative : value;
    }

    private String formatDate(String value) {
        if (value == null || value.isBlank()) {
            return "Sin fecha";
        }
        return value.length() >= 10 ? value.substring(0, 10) : value;
    }

    private Color colorFor(int color) {
        return switch (color) {
            case 1 ->
                AppTheme.BLUE;
            case 3 ->
                AppTheme.ORANGE;
            default ->
                AppTheme.MUTED_TEXT;
        };
    }
}
