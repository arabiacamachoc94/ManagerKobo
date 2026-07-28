package com.arcac.managerkobo.ui.components;

import com.arcac.managerkobo.ui.components.HighlightListItem.BookGroup;
import com.arcac.managerkobo.ui.components.HighlightListItem.Highlight;
import com.arcac.managerkobo.ui.theme.AppTheme;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import javax.swing.JLabel;
import javax.swing.JCheckBox;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.SwingConstants;
import javax.swing.BoxLayout;
import javax.swing.border.EmptyBorder;

/** Alterna entre una cabecera de libro y una tarjeta de subrayado. */
public class GroupedHighlightCellRenderer
        implements ListCellRenderer<HighlightListItem> {

    private final RoundedPanel groupPanel = new RoundedPanel(14, AppTheme.PANEL_ALT);
    private final JLabel arrow = new JLabel();
    private final JLabel title = new JLabel();
    private final JLabel author = new JLabel();
    private final JLabel count = new JLabel();
    private final JCheckBox groupCheck = new JCheckBox();
    private final HighlightCellRenderer flatHighlightRenderer =
            new HighlightCellRenderer(0);
    private final HighlightCellRenderer indentedHighlightRenderer =
            new HighlightCellRenderer(24, false);

    public GroupedHighlightCellRenderer() {
        groupPanel.setLayout(new BorderLayout(12, 0));
        groupPanel.setBorder(new EmptyBorder(14, 16, 14, 16));
        groupPanel.setPreferredSize(new Dimension(400, 66));

        arrow.setFont(AppTheme.font(Font.BOLD, 16));
        arrow.setForeground(AppTheme.PURPLE);
        arrow.setHorizontalAlignment(SwingConstants.CENTER);
        arrow.setPreferredSize(new Dimension(18, 0));
        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.X_AXIS));
        groupCheck.setOpaque(false);
        groupCheck.setFocusable(false);
        groupCheck.setToolTipText("Seleccionar todos los subrayados de este libro");
        left.add(groupCheck);
        left.add(arrow);
        groupPanel.add(left, BorderLayout.WEST);

        JPanel text = new JPanel(new BorderLayout(0, 3));
        text.setOpaque(false);
        title.setFont(AppTheme.font(Font.BOLD, 14));
        title.setForeground(AppTheme.TEXT);
        author.setFont(AppTheme.font(Font.PLAIN, 11));
        author.setForeground(AppTheme.MUTED_TEXT);
        text.add(title, BorderLayout.CENTER);
        text.add(author, BorderLayout.SOUTH);
        groupPanel.add(text, BorderLayout.CENTER);

        JPanel right = new JPanel(new BorderLayout(8, 0));
        right.setOpaque(false);
        count.setFont(AppTheme.font(Font.BOLD, 12));
        count.setForeground(AppTheme.MUTED_TEXT);
        right.add(count, BorderLayout.CENTER);
        groupPanel.add(right, BorderLayout.EAST);
    }

    @Override
    public Component getListCellRendererComponent(
            JList<? extends HighlightListItem> list,
            HighlightListItem value,
            int index,
            boolean selected,
            boolean hasFocus) {
        if (value instanceof BookGroup group) {
            arrow.setText(group.expanded() ? "▾" : "▸");
            title.setText(group.title());
            author.setText(group.author());
            count.setText(group.highlightCount() + " subr."
                    + (group.selectedCount() > 0
                            ? " · " + group.selectedCount() + " ✓"
                            : ""));
            groupCheck.setVisible(group.selectionMode());
            groupCheck.setSelected(group.selectedCount() == group.highlightCount()
                    && group.highlightCount() > 0);
            return groupPanel;
        }

        Highlight highlight = (Highlight) value;
        HighlightCellRenderer renderer = highlight.indented()
                ? indentedHighlightRenderer
                : flatHighlightRenderer;
        renderer.configureSelection(highlight.selectionMode(), highlight.marked());
        renderer.setSize(Math.max(1, list.getWidth()), 1);
        return renderer.getListCellRendererComponent(
                null, highlight.bookmark(), index, highlight.marked(), hasFocus);
    }
}
