package com.arcac.managerkobo.ui.panels;

import com.arcac.managerkobo.model.Book;
import com.arcac.managerkobo.ui.table.BookTableModel;
import com.arcac.managerkobo.ui.theme.AppTheme;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;

/** Pantalla de biblioteca: buscador y tabla de libros. */
public class LibraryPanel extends JPanel {
    private final BookTableModel tableModel;

    public LibraryPanel(List<Book> books) {
        tableModel = new BookTableModel(books);
        setLayout(new BorderLayout());
        setBackground(AppTheme.BACKGROUND);
        add(createHeader(books.size()), BorderLayout.NORTH);
        add(createContent(), BorderLayout.CENTER);
    }

    private JPanel createHeader(int totalBooks) {
        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBorder(new EmptyBorder(30, 32, 25, 32));
        header.add(label("Mi Biblioteca", 29, Font.BOLD, AppTheme.TEXT));
        header.add(Box.createVerticalStrut(5));
        header.add(label(totalBooks + " libros encontrados en tu Kobo", 14, Font.PLAIN, AppTheme.MUTED_TEXT));
        return header;
    }

    private JPanel createContent() {
        JPanel body = new JPanel(new BorderLayout(0, 16));
        body.setOpaque(false);
        body.setBorder(new EmptyBorder(4, 32, 28, 32));
        body.add(createToolbar(), BorderLayout.NORTH);
        body.add(createTable(), BorderLayout.CENTER);
        return body;
    }

    private JPanel createToolbar() {
        JPanel toolbar = new JPanel(new BorderLayout(12, 0));
        toolbar.setOpaque(false);
        JTextField search = new JTextField();
        search.putClientProperty("JTextField.placeholderText", "Buscar título o autor...");
        search.setPreferredSize(new Dimension(360, 40));
        search.getDocument().addDocumentListener(new DocumentListener() {
            private void update() { tableModel.filter(search.getText()); }
            @Override public void insertUpdate(DocumentEvent e) { update(); }
            @Override public void removeUpdate(DocumentEvent e) { update(); }
            @Override public void changedUpdate(DocumentEvent e) { update(); }
        });
        JButton export = new JButton("Exportar CSV");
        export.setBackground(AppTheme.GREEN);
        export.setForeground(java.awt.Color.WHITE);
        export.setFont(AppTheme.font(Font.BOLD, 13));
        export.setBorder(new EmptyBorder(11, 17, 11, 17));
        export.addActionListener(e -> JOptionPane.showMessageDialog(this, "La exportación CSV se implementará en el siguiente módulo."));
        toolbar.add(search, BorderLayout.CENTER);
        toolbar.add(export, BorderLayout.EAST);
        return toolbar;
    }

    private JScrollPane createTable() {
        JTable table = new JTable(tableModel);
        table.setRowHeight(58);
        table.setBackground(AppTheme.PANEL);
        table.setForeground(AppTheme.TEXT);
        table.setSelectionBackground(AppTheme.NAV_SELECTED);
        table.setGridColor(AppTheme.BORDER);
        table.setShowVerticalLines(false);
        table.setFont(AppTheme.font(Font.PLAIN, 14));
        table.getTableHeader().setBackground(AppTheme.PANEL_ALT);
        table.getTableHeader().setForeground(AppTheme.MUTED_TEXT);
        table.getTableHeader().setFont(AppTheme.font(Font.BOLD, 13));
        table.getTableHeader().setPreferredSize(new Dimension(0, 40));
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
        renderer.setBorder(new EmptyBorder(0, 10, 0, 10));
        renderer.setBackground(AppTheme.PANEL);
        renderer.setForeground(AppTheme.TEXT);
        for (int column = 0; column < table.getColumnCount(); column++) {
            table.getColumnModel().getColumn(column).setCellRenderer(renderer);
        }
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(AppTheme.BORDER));
        scroll.getViewport().setBackground(AppTheme.PANEL);
        return scroll;
    }

    private JLabel label(String text, int size, int style, java.awt.Color color) {
        JLabel label = new JLabel(text);
        label.setFont(AppTheme.font(style, size));
        label.setForeground(color);
        return label;
    }
}
