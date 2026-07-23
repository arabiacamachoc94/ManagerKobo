package com.arcac.managerkobo.ui.panels;

import com.arcac.managerkobo.model.Book;
import com.arcac.managerkobo.model.Bookmark;
import com.arcac.managerkobo.ui.table.BookTableModel;
import com.arcac.managerkobo.ui.theme.AppTheme;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.util.List;
import java.util.function.Consumer;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
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
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableRowSorter;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;
import java.awt.Component;
import java.text.Collator;
import java.util.Comparator;
import java.util.Locale;

/** Pantalla de biblioteca: buscador y tabla de libros. */
public class LibraryPanel extends JPanel {
    private final BookTableModel tableModel;
    private final Consumer<Book> openBookAction;

    public LibraryPanel(List<Book> books, List<Bookmark> highlights,
                        Consumer<Book> openBookAction) {
        tableModel = new BookTableModel(books, highlights);
        this.openBookAction = openBookAction;
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
        header.add(label(totalBooks + " libros encontrados · Doble clic para abrir un libro",
                14, Font.PLAIN, AppTheme.MUTED_TEXT));
        return header;
    }

    private JPanel createContent() {
        JPanel body = new JPanel(new BorderLayout(0, 16));
        body.setOpaque(false);
        body.setBorder(new EmptyBorder(8, 36, 32, 36));
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
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        table.setRowHeight(70);
        table.setBackground(AppTheme.PANEL);
        table.setForeground(AppTheme.TEXT);
        table.setSelectionBackground(AppTheme.NAV_SELECTED);
        table.setGridColor(AppTheme.BORDER);
        table.setShowVerticalLines(false);
        table.setFont(AppTheme.font(Font.PLAIN, 14));
        table.getTableHeader().setBackground(AppTheme.PANEL_ALT);
        table.getTableHeader().setForeground(AppTheme.MUTED_TEXT);
        table.getTableHeader().setFont(AppTheme.font(Font.BOLD, 13));
        table.getTableHeader().setPreferredSize(new Dimension(0, 48));
        TableRowSorter<BookTableModel> sorter = new TableRowSorter<>(tableModel);
        Collator textComparator = Collator.getInstance(Locale.forLanguageTag("es"));
        textComparator.setStrength(Collator.PRIMARY);
        sorter.setComparator(0, textComparator);
        sorter.setComparator(1, textComparator);
        sorter.setComparator(3, Comparator.comparingInt(LibraryPanel::statusOrder));
        table.setRowSorter(sorter);
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                if (event.getClickCount() == 2 && table.getSelectedRow() >= 0) {
                    int modelRow = table.convertRowIndexToModel(table.getSelectedRow());
                    Book selectedBook = tableModel.getBookAt(modelRow);
                    if (selectedBook != null) openBookAction.accept(selectedBook);
                }
            }
        });
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(AppTheme.BORDER));
        scroll.getViewport().setBackground(AppTheme.PANEL);
        scroll.getViewport().addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent event) {
                configureResponsiveColumns(table, scroll.getViewport().getWidth());
            }
        });
        SwingUtilities.invokeLater(() ->
                configureResponsiveColumns(table, scroll.getViewport().getWidth()));
        return scroll;
    }

    private void configureResponsiveColumns(JTable table, int availableWidth) {
        int mode = availableWidth < 560 ? 2 : availableWidth < 780 ? 1 : 0;
        Object currentMode = table.getClientProperty("responsiveColumnMode");
        if (currentMode instanceof Integer && (Integer) currentMode == mode) return;
        table.putClientProperty("responsiveColumnMode", mode);

        table.createDefaultColumnsFromModel();
        for (int viewIndex = table.getColumnCount() - 1; viewIndex >= 0; viewIndex--) {
            int modelIndex = table.getColumnModel().getColumn(viewIndex).getModelIndex();
            boolean hide = (mode >= 1 && (modelIndex == 3 || modelIndex == 4))
                    || (mode == 2 && modelIndex == 1);
            if (hide) table.removeColumn(table.getColumnModel().getColumn(viewIndex));
        }

        DefaultTableCellRenderer textRenderer = new DefaultTableCellRenderer();
        textRenderer.setBorder(new EmptyBorder(8, mode == 2 ? 10 : 16, 8, mode == 2 ? 10 : 16));
        textRenderer.setBackground(AppTheme.PANEL);
        textRenderer.setForeground(AppTheme.TEXT);

        for (int viewIndex = 0; viewIndex < table.getColumnCount(); viewIndex++) {
            javax.swing.table.TableColumn column = table.getColumnModel().getColumn(viewIndex);
            int modelIndex = column.getModelIndex();
            column.setCellRenderer(switch (modelIndex) {
                case 2 -> new ProgressRenderer();
                case 4 -> new TimeRenderer();
                default -> textRenderer;
            });
            int preferredWidth = switch (modelIndex) {
                case 0 -> mode == 2 ? 250 : 270;
                case 1 -> 185;
                case 2 -> mode == 2 ? 155 : 175;
                case 3, 4 -> 110;
                case 5 -> 105;
                default -> 100;
            };
            column.setPreferredWidth(preferredWidth);
        }
        table.revalidate();
        table.repaint();
    }

    private JLabel label(String text, int size, int style, java.awt.Color color) {
        JLabel label = new JLabel(text);
        label.setFont(AppTheme.font(style, size));
        label.setForeground(color);
        return label;
    }

    /** Renderiza el porcentaje mediante una barra, conservando el valor numérico. */
    private static int statusOrder(String status) {
        return switch (status) {
            case "Sin empezar" -> 0;
            case "Leyendo" -> 1;
            case "Terminado" -> 2;
            default -> 3;
        };
    }

    /** Muestra horas y minutos, conservando segundos en el modelo para ordenar. */
    private static class TimeRenderer extends DefaultTableCellRenderer {
        TimeRenderer() {
            setBorder(new EmptyBorder(8, 16, 8, 16));
            setBackground(AppTheme.PANEL);
            setForeground(AppTheme.TEXT);
        }

        @Override
        protected void setValue(Object value) {
            int seconds = value instanceof Number ? ((Number) value).intValue() : 0;
            setText(String.format("%dh %02dmin", seconds / 3600, (seconds % 3600) / 60));
        }
    }

    private static class ProgressRenderer extends JPanel implements TableCellRenderer {
        private final JProgressBar progress = new JProgressBar(0, 100);

        ProgressRenderer() {
            setLayout(new BorderLayout());
            setBorder(new EmptyBorder(18, 16, 18, 16));
            progress.setStringPainted(true);
            progress.setBorderPainted(false);
            progress.setForeground(AppTheme.PURPLE);
            progress.setBackground(AppTheme.BORDER);
            progress.setFont(AppTheme.font(Font.BOLD, 11));
            add(progress, BorderLayout.CENTER);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean selected, boolean focused, int row, int column) {
            int percentage = value instanceof Number ? ((Number) value).intValue() : 0;
            progress.setValue(Math.max(0, Math.min(100, percentage)));
            progress.setString(percentage + "%");
            setBackground(selected ? table.getSelectionBackground() : AppTheme.PANEL);
            progress.setForeground(percentage >= 100 ? AppTheme.GREEN : AppTheme.PURPLE);
            return this;
        }
    }
}
