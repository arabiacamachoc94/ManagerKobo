package com.arcac.managerkobo.ui.panels;

import com.arcac.managerkobo.model.LookedUpWord;
import com.arcac.managerkobo.ui.table.WordTableModel;
import com.arcac.managerkobo.ui.theme.AppTheme;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.text.Collator;
import java.util.List;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableRowSorter;

/** Pantalla básica de palabras consultadas en el diccionario del Kobo. */
public class WordsPanel extends JPanel {
    private final WordTableModel tableModel;

    public WordsPanel(List<LookedUpWord> words) {
        List<LookedUpWord> safeWords = words == null ? List.of() : words;
        tableModel = new WordTableModel(safeWords);
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

        JPanel header = verticalPanel();
        header.setBorder(new EmptyBorder(30, 32, 22, 32));
        header.add(label("Palabras", 29, Font.BOLD, AppTheme.TEXT));
        header.add(Box.createVerticalStrut(5));
        header.add(label(words.size() + " palabras consultadas en "
                        + books + (books == 1 ? " libro" : " libros"),
                14, Font.PLAIN, AppTheme.MUTED_TEXT));
        return header;
    }

    private JPanel createContent() {
        JPanel body = new JPanel(new BorderLayout(0, 14));
        body.setOpaque(false);
        body.setBorder(new EmptyBorder(4, 32, 30, 32));

        JTextField search = new JTextField();
        search.putClientProperty("JTextField.placeholderText",
                "Buscar palabra, libro o autor...");
        search.setPreferredSize(new Dimension(400, 40));
        search.getDocument().addDocumentListener(new DocumentListener() {
            private void update() {
                tableModel.filter(search.getText());
            }
            @Override public void insertUpdate(DocumentEvent event) { update(); }
            @Override public void removeUpdate(DocumentEvent event) { update(); }
            @Override public void changedUpdate(DocumentEvent event) { update(); }
        });
        body.add(search, BorderLayout.NORTH);
        body.add(createTable(), BorderLayout.CENTER);
        return body;
    }

    private JScrollPane createTable() {
        JTable table = new JTable(tableModel);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        table.setRowHeight(54);
        table.setBackground(AppTheme.PANEL);
        table.setForeground(AppTheme.TEXT);
        table.setSelectionBackground(AppTheme.NAV_SELECTED);
        table.setGridColor(AppTheme.BORDER);
        table.setShowVerticalLines(false);
        table.setFont(AppTheme.font(Font.PLAIN, 14));
        table.getTableHeader().setBackground(AppTheme.PANEL_ALT);
        table.getTableHeader().setForeground(AppTheme.MUTED_TEXT);
        table.getTableHeader().setFont(AppTheme.font(Font.BOLD, 13));
        table.getTableHeader().setPreferredSize(new Dimension(0, 46));
        table.getTableHeader().setReorderingAllowed(false);

        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
        renderer.setBorder(new EmptyBorder(8, 14, 8, 14));
        renderer.setBackground(AppTheme.PANEL);
        renderer.setForeground(AppTheme.TEXT);
        for (int index = 0; index < table.getColumnCount(); index++) {
            table.getColumnModel().getColumn(index).setCellRenderer(renderer);
        }

        table.getColumnModel().getColumn(0)
                .setCellRenderer(new BoldWordRenderer());

        table.getColumnModel().getColumn(0).setPreferredWidth(150);
        table.getColumnModel().getColumn(1).setPreferredWidth(250);
        table.getColumnModel().getColumn(2).setPreferredWidth(170);
        table.getColumnModel().getColumn(3).setPreferredWidth(100);
        table.getColumnModel().getColumn(4).setPreferredWidth(105);

        TableRowSorter<WordTableModel> sorter = new TableRowSorter<>(tableModel);
        Collator comparator = Collator.getInstance(Locale.forLanguageTag("es"));
        comparator.setStrength(Collator.PRIMARY);
        sorter.setComparator(0, comparator);
        sorter.setComparator(1, comparator);
        sorter.setComparator(2, comparator);
        table.setRowSorter(sorter);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(AppTheme.BORDER));
        scroll.getViewport().setBackground(AppTheme.PANEL);
        scroll.getVerticalScrollBar().setUnitIncrement(18);
        return scroll;
    }

    private JPanel verticalPanel() {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        return panel;
    }

    private static final class BoldWordRenderer
            extends DefaultTableCellRenderer {
        BoldWordRenderer() {
            setBorder(new EmptyBorder(8, 14, 8, 14));
            setBackground(AppTheme.PANEL);
            setForeground(AppTheme.TEXT);
        }

        @Override
        public java.awt.Component getTableCellRendererComponent(
                JTable table, Object value, boolean selected,
                boolean focused, int row, int column) {
            java.awt.Component component = super.getTableCellRendererComponent(
                    table, value, selected, focused, row, column);
            component.setFont(AppTheme.font(Font.BOLD, 14));
            return component;
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
