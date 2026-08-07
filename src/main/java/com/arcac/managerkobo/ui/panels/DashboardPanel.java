package com.arcac.managerkobo.ui.panels;

import com.arcac.managerkobo.model.Book;
import com.arcac.managerkobo.ai.ReadingInsightsAiService;
import com.arcac.managerkobo.service.ReadingStatistics;
import com.arcac.managerkobo.service.ReadingReportPdfService;
import com.arcac.managerkobo.service.SummaryExportService;
import com.arcac.managerkobo.ui.components.DonutChartPanel;
import com.arcac.managerkobo.ui.components.DonutChartPanel.Segment;
import com.arcac.managerkobo.ui.components.BookCard;
import com.arcac.managerkobo.ui.components.LibraryOverviewPanel;
import com.arcac.managerkobo.ui.components.RoundedButton;
import com.arcac.managerkobo.ui.components.RoundedPanel;
import com.arcac.managerkobo.ui.theme.AppTheme;
import com.arcac.managerkobo.ui.util.IconLoader;
import com.arcac.managerkobo.ui.util.I18n;
import com.arcac.managerkobo.ui.util.AppPreferences;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JFileChooser;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

/** Resumen principal de la actividad de lectura. */
public class DashboardPanel extends JPanel {
    private final ReadingStatistics statistics;
    private final Consumer<Book> openBookAction;
    private final LocalDateTime lastSynchronization;
    private final SummaryExportService summaryExportService =
            new SummaryExportService();
    private final ReadingReportPdfService pdfReportService =
            new ReadingReportPdfService();
    private final ReadingInsightsAiService readingInsightsAiService =
            new ReadingInsightsAiService();
    private JButton imageExportButton;
    private JButton pdfExportButton;

    public DashboardPanel(ReadingStatistics statistics,
            Consumer<Book> openBookAction,
            LocalDateTime lastSynchronization) {
        this.statistics = statistics;
        this.openBookAction = openBookAction;
        this.lastSynchronization = lastSynchronization;
        setLayout(new BorderLayout());
        setBackground(AppTheme.BACKGROUND);
        add(createHeader(), BorderLayout.NORTH);
        add(createContent(), BorderLayout.CENTER);
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout(20, 0));
        header.setOpaque(false);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(
                        0, 0, 1, 0, AppTheme.BORDER),
                new EmptyBorder(30, 32, 22, 32)));

        JPanel titles = verticalPanel();
        titles.add(label("Resumen", 29, Font.BOLD, AppTheme.TEXT));
        titles.add(Box.createVerticalStrut(5));
        titles.add(label(librarySummary(), 14, Font.PLAIN, AppTheme.MUTED_TEXT));
        titles.add(Box.createVerticalStrut(4));
        titles.add(label(lastSynchronizationText(),
                12, Font.PLAIN, AppTheme.MUTED_TEXT));
        header.add(titles, BorderLayout.CENTER);

        JLabel logo = new JLabel();
        logo.setIcon(IconLoader.load("/icons/logo2-transparent.png", 58));
        logo.setHorizontalAlignment(SwingConstants.CENTER);
        logo.setPreferredSize(new Dimension(64, 64));
        imageExportButton = new RoundedButton("Exportar imagen");
        styleSecondaryButton(imageExportButton);
        imageExportButton.addActionListener(event -> exportSummary());
        pdfExportButton = new RoundedButton("Informe PDF");
        styleSecondaryButton(pdfExportButton);
        pdfExportButton.addActionListener(event -> exportPdfReport());

        JPanel headerActions = new JPanel(new java.awt.FlowLayout(
                java.awt.FlowLayout.RIGHT, 12, 10));
        headerActions.setOpaque(false);
        headerActions.add(imageExportButton);
        headerActions.add(pdfExportButton);
        headerActions.add(logo);
        header.add(headerActions, BorderLayout.EAST);

        return header;
    }

    private void exportSummary() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Exportar resumen");
        chooser.setSelectedFile(new java.io.File(
                "resumen_kobo.jpg"));
        chooser.setFileFilter(new FileNameExtensionFilter("Imagen JPEG", "jpg", "jpeg"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;

        Path destination = ensureExtension(
                chooser.getSelectedFile().toPath(), ".jpg");
        if (destination.toFile().exists()) {
            int answer = JOptionPane.showConfirmDialog(this,
                    "El archivo ya existe. ¿Quieres reemplazarlo?",
                    "Confirmar exportación", JOptionPane.YES_NO_OPTION);
            if (answer != JOptionPane.YES_OPTION) return;
        }

        setExportInProgress(true, imageExportButton, "Exportando...");
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                summaryExportService.export(
                        statistics, lastSynchronization, destination,
                        AppPreferences.isEnglish());
                verifyExport(destination);
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    JOptionPane.showMessageDialog(DashboardPanel.this,
                            "Resumen exportado correctamente en:\n"
                                    + destination.toAbsolutePath(),
                            "Exportación completada",
                            JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception exception) {
                    JOptionPane.showMessageDialog(DashboardPanel.this,
                            "No se pudo exportar el resumen: "
                                    + rootMessage(exception),
                            "Error de exportación", JOptionPane.ERROR_MESSAGE);
                } finally {
                    setExportInProgress(false, null, null);
                }
            }
        }.execute();
    }

    private void exportPdfReport() {
        JCheckBox includeAi = new JCheckBox(
                "Incluir análisis final generado con Gemini",
                readingInsightsAiService.isConfigured());
        includeAi.setEnabled(readingInsightsAiService.isConfigured());
        JPanel options = new JPanel();
        options.setLayout(new BoxLayout(options, BoxLayout.Y_AXIS));
        options.add(new JLabel("El informe incluirá estadísticas agregadas de lectura."));
        options.add(Box.createVerticalStrut(8));
        options.add(includeAi);
        if (!readingInsightsAiService.isConfigured()) {
            options.add(Box.createVerticalStrut(6));
            options.add(new JLabel(
                    "Configura tu API key en Ajustes para activar el análisis."));
        }
        I18n.translateTree(options);
        int option = JOptionPane.showConfirmDialog(this, options,
                I18n.text("Opciones del informe PDF"),
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (option != JOptionPane.OK_OPTION) return;
        boolean includeAiAnalysis = includeAi.isSelected();

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Generar informe PDF");
        chooser.setSelectedFile(new java.io.File("informe_lectura_kobo.pdf"));
        chooser.setFileFilter(new FileNameExtensionFilter("Documento PDF", "pdf"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;

        Path destination = ensureExtension(
                chooser.getSelectedFile().toPath(), ".pdf");
        if (destination.toFile().exists()) {
            int answer = JOptionPane.showConfirmDialog(this,
                    "El archivo ya existe. ¿Quieres reemplazarlo?",
                    "Confirmar exportación", JOptionPane.YES_NO_OPTION);
            if (answer != JOptionPane.YES_OPTION) return;
        }

        setExportInProgress(true, pdfExportButton, "Generando PDF...");
        new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() throws Exception {
                String analysis = null;
                String aiWarning = null;
                if (includeAiAnalysis) {
                    try {
                        analysis = readingInsightsAiService.analyze(statistics);
                    } catch (Exception exception) {
                        aiWarning = rootMessage(exception);
                    }
                }
                pdfReportService.export(
                        statistics, lastSynchronization, destination, analysis,
                        AppPreferences.isEnglish());
                verifyExport(destination);
                return aiWarning;
            }

            @Override
            protected void done() {
                try {
                    String warning = get();
                    String message = warning == null
                            ? "Informe PDF generado correctamente en:\n"
                                    + destination.toAbsolutePath()
                            : "El PDF se ha guardado, pero sin el análisis de IA.\n\n"
                                    + warning + "\n\n"
                                    + "Guardado en:\n"
                                    + destination.toAbsolutePath();
                    JOptionPane.showMessageDialog(DashboardPanel.this, message,
                            warning == null ? "Exportación completada"
                                    : "Informe generado sin IA",
                            warning == null ? JOptionPane.INFORMATION_MESSAGE
                                    : JOptionPane.WARNING_MESSAGE);
                } catch (Exception exception) {
                    JOptionPane.showMessageDialog(DashboardPanel.this,
                            "No se pudo generar el informe: "
                                    + rootMessage(exception),
                            "Error de exportación", JOptionPane.ERROR_MESSAGE);
                } finally {
                    setExportInProgress(false, null, null);
                }
            }
        }.execute();
    }

    private Path ensureExtension(Path path, String extension) {
        return path.toString().toLowerCase().endsWith(extension)
                ? path : Path.of(path + extension);
    }

    private void verifyExport(Path destination) throws IOException {
        if (!Files.isRegularFile(destination) || Files.size(destination) == 0) {
            throw new IOException("El archivo no se ha podido guardar.");
        }
    }

    private void setExportInProgress(boolean exporting, JButton active,
            String progressText) {
        imageExportButton.setEnabled(!exporting);
        pdfExportButton.setEnabled(!exporting);
        imageExportButton.setText(active == imageExportButton
                ? I18n.text(progressText) : I18n.text("Exportar imagen"));
        pdfExportButton.setText(active == pdfExportButton
                ? I18n.text(progressText) : I18n.text("Informe PDF"));
        setCursor(exporting
                ? java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.WAIT_CURSOR)
                : java.awt.Cursor.getDefaultCursor());
    }

    private String rootMessage(Throwable throwable) {
        Throwable current = throwable;
        while (current.getCause() != null) current = current.getCause();
        return current.getMessage() == null
                ? current.getClass().getSimpleName() : current.getMessage();
    }

    private JPanel createContent() {
        ScrollableVerticalPanel body = new ScrollableVerticalPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBorder(new EmptyBorder(22, 32, 32, 32));

        body.add(titledSection("Lecturas en curso",
                createCurrentBooksSummary()));
        body.add(Box.createVerticalStrut(22));

        body.add(sectionTitle("Tu biblioteca de un vistazo"));
        body.add(Box.createVerticalStrut(13));
        body.add(fullWidth(new LibraryOverviewPanel(statistics), 335));
        body.add(Box.createVerticalStrut(24));

        body.add(createDonutCharts());
        body.add(Box.createVerticalStrut(24));

        body.add(sectionTitle("Explora tus estadísticas"));
        body.add(Box.createVerticalStrut(12));
        AdvancedStatisticsPanel advancedPanel =
                new AdvancedStatisticsPanel(statistics, false);
        advancedPanel.setPreferredSize(new Dimension(600, 384));
        advancedPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 384));
        body.add(advancedPanel);

        JScrollPane scroll = new JScrollPane(body);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUnitIncrement(18);
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(scroll, BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel createDonutCharts() {
        JPanel charts = responsiveGrid(2, 360, 245, 16);
        charts.add(titledSection("Estado de la biblioteca",
                createLibraryStatusDonut()));
        charts.add(titledSection("Progreso de lecturas activas",
                createReadingProgressDonut()));
        return charts;
    }

    private JPanel createLibraryStatusDonut() {
        return new DonutChartPanel(List.of(
                new Segment("Terminados", statistics.finishedBooks(),
                        AppTheme.GREEN),
                new Segment("Leyendo", statistics.readingBooks(),
                        AppTheme.PURPLE),
                new Segment("Sin empezar", statistics.unreadBooks(),
                        AppTheme.BLUE)));
    }

    private JPanel createReadingProgressDonut() {
        int[] ranges = new int[4];
        for (Book book : statistics.inProgressBooks()) {
            int progress = book.getPercentRead();
            if (progress < 25) ranges[0]++;
            else if (progress < 50) ranges[1]++;
            else if (progress < 75) ranges[2]++;
            else ranges[3]++;
        }
        return new DonutChartPanel(List.of(
                new Segment("Menos del 25%", ranges[0], AppTheme.BLUE),
                new Segment("25–49%", ranges[1], AppTheme.PURPLE),
                new Segment("50–74%", ranges[2], AppTheme.ORANGE),
                new Segment("75% o más", ranges[3], AppTheme.GREEN)));
    }

    private JPanel createCurrentBooksSummary() {
        List<Book> currentBooks = statistics.inProgressBooks();
        if (currentBooks.isEmpty()) {
            RoundedPanel empty = new RoundedPanel(20, AppTheme.PANEL);
            empty.setLayout(new BorderLayout());
            empty.setBorder(new EmptyBorder(24, 24, 24, 24));
            empty.add(label(
                    "No hay libros en progreso. Sincroniza el Kobo para actualizarlo.",
                    13, Font.PLAIN, AppTheme.MUTED_TEXT), BorderLayout.CENTER);
            return empty;
        }

        JPanel books = responsiveGrid(
                4, BookCard.CARD_WIDTH, BookCard.CARD_HEIGHT, 14);
        for (Book book : currentBooks) {
            books.add(new BookCard(book, openBookAction));
        }
        return books;
    }

    private JPanel titledSection(String title, JPanel content) {
        JPanel section = new JPanel(new BorderLayout(0, 11));
        section.setOpaque(false);
        section.add(sectionTitle(title), BorderLayout.NORTH);
        section.add(content, BorderLayout.CENTER);
        return section;
    }

    private String librarySummary() {
        return statistics.totalBooks() + " libros · "
                + statistics.readingBooks() + " en progreso · "
                + statistics.finishedBooks() + " terminados";
    }

    private String lastSynchronizationText() {
        if (lastSynchronization == null) {
            return "Última sincronización: todavía no disponible";
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy · HH:mm");
        return "Última sincronización: " + lastSynchronization.format(formatter);
    }

    private void styleSecondaryButton(JButton button) {
        button.setBackground(AppTheme.PURPLE);
        button.setForeground(Color.WHITE);
        button.setFont(AppTheme.font(Font.BOLD, 12));
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(8, 14, 8, 14));
    }

    private JPanel verticalPanel() {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        return panel;
    }

    private JPanel responsiveGrid(
            int maxColumns, int minimumCardWidth, int rowHeight, int gap) {
        JPanel panel = new JPanel(new GridLayout(0, maxColumns, gap, gap));
        panel.setOpaque(false);
        panel.putClientProperty("responsiveColumns", maxColumns);
        panel.setPreferredSize(
                new Dimension(maxColumns * minimumCardWidth, rowHeight));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, rowHeight));
        panel.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent event) {
                updateResponsiveGrid(
                        panel, maxColumns, minimumCardWidth, rowHeight, gap);
            }
        });
        return panel;
    }

    private void updateResponsiveGrid(
            JPanel panel, int maxColumns, int minimumCardWidth,
            int rowHeight, int gap) {
        int availableWidth = Math.max(minimumCardWidth, panel.getWidth());
        int columns = Math.max(1, Math.min(maxColumns,
                (availableWidth + gap) / (minimumCardWidth + gap)));
        int previousColumns =
                (int) panel.getClientProperty("responsiveColumns");
        int rows = Math.max(1,
                (int) Math.ceil(panel.getComponentCount() / (double) columns));
        int requiredHeight = rows * rowHeight + (rows - 1) * gap;

        if (columns != previousColumns
                || panel.getPreferredSize().height != requiredHeight) {
            ((GridLayout) panel.getLayout()).setColumns(columns);
            panel.putClientProperty("responsiveColumns", columns);
            panel.setPreferredSize(new Dimension(
                    Math.max(minimumCardWidth, availableWidth), requiredHeight));
            panel.setMaximumSize(
                    new Dimension(Integer.MAX_VALUE, requiredHeight));
            panel.revalidate();
        }
    }

    /**
     * Evita que BoxLayout reduzca una tarjeta a su ancho preferido.
     */
    private JPanel fullWidth(JPanel content, int height) {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(content, BorderLayout.CENTER);
        wrapper.setPreferredSize(new Dimension(600, height));
        wrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, height));
        wrapper.setAlignmentX(LEFT_ALIGNMENT);
        return wrapper;
    }

    private JLabel sectionTitle(String text) {
        return label(text, 18, Font.BOLD, AppTheme.TEXT);
    }

    private JLabel label(String text, int size, int style, Color color) {
        JLabel result = new JLabel(text);
        result.setFont(AppTheme.font(style, size));
        result.setForeground(color);
        return result;
    }

    private static class ScrollableVerticalPanel extends JPanel implements Scrollable {
        @Override
        public void doLayout() {
            super.doLayout();
            Insets insets = getInsets();
            int availableWidth = Math.max(0,
                    getWidth() - insets.left - insets.right);

            /*
             * BoxLayout puede conservar el ancho preferido de algunos paneles.
             * Aquí todas las secciones reciben explícitamente el ancho útil.
             */
            for (Component component : getComponents()) {
                component.setBounds(insets.left, component.getY(),
                        availableWidth, component.getHeight());
            }
        }

        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        @Override
        public int getScrollableUnitIncrement(
                Rectangle visibleRect, int orientation, int direction) {
            return 18;
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
