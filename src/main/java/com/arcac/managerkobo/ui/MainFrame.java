package com.arcac.managerkobo.ui;

import com.arcac.managerkobo.model.Book;
import com.arcac.managerkobo.model.Bookmark;
import com.arcac.managerkobo.model.LookedUpWord;
import com.arcac.managerkobo.service.KoboLibraryData;
import com.arcac.managerkobo.service.KoboLibraryService;
import com.arcac.managerkobo.service.ReadingStatistics;
import com.arcac.managerkobo.ui.panels.DashboardPanel;
import com.arcac.managerkobo.ui.panels.BookDetailPanel;
import com.arcac.managerkobo.ui.panels.HighlightsPanel;
import com.arcac.managerkobo.ui.panels.LibraryPanel;
import com.arcac.managerkobo.ui.panels.WordsPanel;
import com.arcac.managerkobo.ui.theme.AppTheme;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

/**
 * Ventana principal: contiene la navegación y coordina las pantallas.
 */
public class MainFrame extends JFrame {

    private final CardLayout navigation = new CardLayout();
    private final JPanel contentPanel = new JPanel(navigation);
    private final KoboLibraryService libraryService;
    private SidebarPanel sidebarPanel;
    private DashboardPanel dashboardPanel;
    private JPanel bookDetailPanel;
    private List<Bookmark> currentHighlights = List.of();
    private String currentPage = SidebarPanel.DASHBOARD;
    private String bookDetailReturnPage = SidebarPanel.LIBRARY;
    private LocalDateTime lastSynchronization;
    private static final String BOOK_DETAIL = "book-detail";


    public MainFrame(KoboLibraryService libraryService, KoboLibraryData initialData) {
        this.libraryService = libraryService;
        this.lastSynchronization = databaseModificationDate(
                initialData.syncResult().databasePath());
        configureWindow();
        createLayout(initialData.syncResult().koboConnected());
        createPages(initialData.books(), initialData.highlights(),
                initialData.words(), initialData.statistics());
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
            List<LookedUpWord> words, ReadingStatistics statistics) {
        contentPanel.removeAll();
        currentHighlights = new ArrayList<>(highlights);
        bookDetailPanel = null;
        contentPanel.setBackground(AppTheme.BACKGROUND);
        dashboardPanel = new DashboardPanel(
                books, statistics, this::showBookDetail,
                lastSynchronization);
        contentPanel.add(dashboardPanel, SidebarPanel.DASHBOARD);
        contentPanel.add(new LibraryPanel(books, highlights, this::showBookDetail), SidebarPanel.LIBRARY);
        contentPanel.add(new HighlightsPanel(books, highlights), SidebarPanel.HIGHLIGHTS);
        contentPanel.add(new WordsPanel(words), SidebarPanel.WORDS);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private void createLayout(boolean koboConnected) {
        setLayout(new BorderLayout());
        sidebarPanel = new SidebarPanel(
                this::showPage, this::synchronizeDatabase, koboConnected);
        add(sidebarPanel, BorderLayout.WEST);
        add(contentPanel, BorderLayout.CENTER);
    }

    private void showPage(String page) {
        currentPage = page;
        navigation.show(contentPanel, page);
    }

    private void showBookDetail(Book book) {
        if (!BOOK_DETAIL.equals(currentPage)) {
            bookDetailReturnPage = currentPage;
        }
        List<Bookmark> bookHighlights = currentHighlights.stream()
                .filter(mark -> book.getContentId() != null
                && book.getContentId().equals(mark.getVolumeId()))
                .toList();
        if (bookDetailPanel != null) {
            contentPanel.remove(bookDetailPanel);
        }
        bookDetailPanel = new BookDetailPanel(book, bookHighlights,
                () -> showPage(bookDetailReturnPage));
        contentPanel.add(bookDetailPanel, BOOK_DETAIL);
        showPage(BOOK_DETAIL);
    }

    /**
     * Ejecuta copia, reconexión y recarga sin bloquear el hilo de Swing.
     */
    private void synchronizeDatabase() {
        sidebarPanel.setSyncing(true);

        new SwingWorker<KoboLibraryData, Void>() {
            @Override
            protected KoboLibraryData doInBackground() {
                return libraryService.synchronizeAndLoad();
            }

            @Override
            protected void done() {
                try {
                    KoboLibraryData data = get();
                    sidebarPanel.setKoboConnected(data.syncResult().koboConnected());

                    if (data.syncResult().databaseAvailable()) {
                        if (data.syncResult().databaseUpdated()) {
                            lastSynchronization = LocalDateTime.now();
                        }
                        createPages(data.books(), data.highlights(),
                                data.words(), data.statistics());
                        showPage(currentPage);
                    }
                    sidebarPanel.setSyncing(false);

                    int messageType = data.syncResult().koboConnected()
                            ? JOptionPane.INFORMATION_MESSAGE
                            : JOptionPane.WARNING_MESSAGE;
                    JOptionPane.showMessageDialog(MainFrame.this,
                            data.syncResult().message(), "Sincronización", messageType);
                } catch (Exception exception) {
                    sidebarPanel.setSyncing(false);
                    JOptionPane.showMessageDialog(MainFrame.this,
                            "No se pudo sincronizar la base de datos: "
                            + rootMessage(exception),
                            "Error de sincronización", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private LocalDateTime databaseModificationDate(String databasePath) {
        if (databasePath == null || databasePath.isBlank()) {
            return null;
        }
        try {
            Instant modified = Files.getLastModifiedTime(Path.of(databasePath)).toInstant();
            return LocalDateTime.ofInstant(modified, ZoneId.systemDefault());
        } catch (Exception ignored) {
            return null;
        }
    }

    private String rootMessage(Throwable throwable) {
        Throwable current = throwable;
        while (current.getCause() != null) {
            current = current.getCause();
        }
        return current.getMessage() == null ? current.getClass().getSimpleName() : current.getMessage();
    }

}
