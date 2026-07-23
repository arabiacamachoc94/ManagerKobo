package com.arcac.managerkobo.ui;

import com.arcac.managerkobo.model.Book;
import com.arcac.managerkobo.model.Bookmark;
import com.arcac.managerkobo.database.DataBaseConnection;
import com.arcac.managerkobo.database.KoboDAO;
import com.arcac.managerkobo.service.LibraryStatisticsService;
import com.arcac.managerkobo.service.ReadingStatistics;
import com.arcac.managerkobo.ui.panels.DashboardPanel;
import com.arcac.managerkobo.ui.panels.BookDetailPanel;
import com.arcac.managerkobo.ui.panels.HighlightsPanel;
import com.arcac.managerkobo.ui.panels.LibraryPanel;
import com.arcac.managerkobo.ui.theme.AppTheme;
import com.arcac.managerkobo.util.KoboDetector;
import com.arcac.managerkobo.util.KoboSyncResult;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import javax.swing.SwingConstants;

/**
 * Ventana principal: contiene la navegación y coordina las pantallas.
 */
public class MainFrame extends JFrame {

    private final CardLayout navigation = new CardLayout();
    private final JPanel contentPanel = new JPanel(navigation);
    private SidebarPanel sidebarPanel;
    private DashboardPanel dashboardPanel;
    private JPanel bookDetailPanel;
    private List<Bookmark> currentHighlights = List.of();
    private String currentPage = SidebarPanel.DASHBOARD;
    private static final String BOOK_DETAIL = "book-detail";


    public MainFrame(List<Book> books, List<Bookmark> highlights,
            ReadingStatistics statistics, boolean koboConnected) {
        List<Book> safeBooks = books == null ? List.of() : new ArrayList<>(books);
        List<Bookmark> safeHighlights = highlights == null
                ? List.of() : new ArrayList<>(highlights);
        ReadingStatistics safeStatistics = statistics == null
                ? new LibraryStatisticsService().calculate(safeBooks, safeHighlights)
                : statistics;
        configureWindow();
        createLayout(koboConnected);
        createPages(safeBooks, safeHighlights, safeStatistics);
        showPage(SidebarPanel.DASHBOARD);
    }

    private void configureWindow() {
        setTitle("Kobo Manager");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(820, 600));
        setSize(1180, 760);
        setLocationRelativeTo(null);
        getContentPane().setBackground(AppTheme.BACKGROUND);
    }

    private void createPages(List<Book> books, List<Bookmark> highlights,
            ReadingStatistics statistics) {
        contentPanel.removeAll();
        currentHighlights = new ArrayList<>(highlights);
        bookDetailPanel = null;
        contentPanel.setBackground(AppTheme.BACKGROUND);
        dashboardPanel = new DashboardPanel(books, statistics, this::synchronizeDatabase);
        contentPanel.add(dashboardPanel, SidebarPanel.DASHBOARD);
        contentPanel.add(new LibraryPanel(books, highlights, this::showBookDetail), SidebarPanel.LIBRARY);
        contentPanel.add(new HighlightsPanel(highlights), SidebarPanel.HIGHLIGHTS);
        contentPanel.add(placeholder("Palabras", "Tu diccionario personal aparecerá aquí."), SidebarPanel.WORDS);
        contentPanel.add(placeholder("Logros", "Tus hitos de lectura aparecerán aquí."), SidebarPanel.ACHIEVEMENTS);
        contentPanel.add(placeholder("Ajustes", "Preferencias de Kobo Manager."), SidebarPanel.SETTINGS);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private void createLayout(boolean koboConnected) {
        setLayout(new BorderLayout());
        sidebarPanel = new SidebarPanel(this::showPage, koboConnected);
        add(sidebarPanel, BorderLayout.WEST);
        add(contentPanel, BorderLayout.CENTER);
    }

    private void showPage(String page) {
        currentPage = page;
        navigation.show(contentPanel, page);
    }

    private void showBookDetail(Book book) {
        List<Bookmark> bookHighlights = currentHighlights.stream()
                .filter(mark -> book.getContentId() != null
                && book.getContentId().equals(mark.getVolumeId()))
                .toList();
        if (bookDetailPanel != null) {
            contentPanel.remove(bookDetailPanel);
        }
        bookDetailPanel = new BookDetailPanel(book, bookHighlights,
                () -> showPage(SidebarPanel.LIBRARY));
        contentPanel.add(bookDetailPanel, BOOK_DETAIL);
        showPage(BOOK_DETAIL);
    }

    /**
     * Ejecuta copia, reconexión y recarga sin bloquear el hilo de Swing.
     */
    private void synchronizeDatabase() {
        dashboardPanel.setSyncing(true);

        new SwingWorker<ReloadedData, Void>() {
            @Override
            protected ReloadedData doInBackground() throws Exception {
                DataBaseConnection database = DataBaseConnection.getInstance();
                database.disconnect();

                KoboSyncResult result = KoboDetector.synchronize();
                if (!result.databaseAvailable()) {
                    return new ReloadedData(result, List.of(), List.of(),
                            new LibraryStatisticsService().calculate(List.of()));
                }

                database.connect(result.databasePath());
                KoboDAO dao = new KoboDAO();
                List<Book> books = dao.getAllBooks();
                List<Bookmark> highlights = dao.getAllHighlightsWithBook();
                ReadingStatistics statistics = new LibraryStatisticsService()
                        .calculate(books, highlights);
                return new ReloadedData(result, books, highlights, statistics);
            }

            @Override
            protected void done() {
                try {
                    ReloadedData data = get();
                    sidebarPanel.setKoboConnected(data.syncResult().koboConnected());

                    if (data.syncResult().databaseAvailable()) {
                        createPages(data.books(), data.highlights(), data.statistics());
                        showPage(currentPage);
                    } else {
                        dashboardPanel.setSyncing(false);
                    }

                    int messageType = data.syncResult().koboConnected()
                            ? JOptionPane.INFORMATION_MESSAGE
                            : JOptionPane.WARNING_MESSAGE;
                    JOptionPane.showMessageDialog(MainFrame.this,
                            data.syncResult().message(), "Sincronización", messageType);
                } catch (Exception exception) {
                    dashboardPanel.setSyncing(false);
                    JOptionPane.showMessageDialog(MainFrame.this,
                            "No se pudo sincronizar la base de datos: "
                            + rootMessage(exception),
                            "Error de sincronización", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private String rootMessage(Throwable throwable) {
        Throwable current = throwable;
        while (current.getCause() != null) {
            current = current.getCause();
        }
        return current.getMessage() == null ? current.getClass().getSimpleName() : current.getMessage();
    }

    private record ReloadedData(
            KoboSyncResult syncResult,
            List<Book> books,
            List<Bookmark> highlights,
            ReadingStatistics statistics) {

    }

    /* Panel provisional. Próximo desarrollo*/
    private JPanel placeholder(String title, String description) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(AppTheme.BACKGROUND);
        JLabel text = new JLabel("<html><h1>" + title + "</h1><p>" + description + "</p></html>", SwingConstants.CENTER);
        text.setForeground(AppTheme.TEXT);
        panel.add(text);
        return panel;
    }
}
