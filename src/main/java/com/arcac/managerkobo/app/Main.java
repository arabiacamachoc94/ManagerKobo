package com.arcac.managerkobo.app;

import com.arcac.managerkobo.database.DataBaseConnection;
import com.arcac.managerkobo.database.KoboDAO;
import com.arcac.managerkobo.model.Book;
import com.arcac.managerkobo.model.Bookmark;
import com.arcac.managerkobo.service.LibraryStatisticsService;
import com.arcac.managerkobo.service.ReadingStatistics;
import com.arcac.managerkobo.util.KoboDetector;
import com.arcac.managerkobo.util.KoboSyncResult;
import java.util.List;
import com.formdev.flatlaf.FlatDarkLaf;
import com.arcac.managerkobo.ui.MainFrame;
import java.util.ArrayList;
import javax.swing.SwingUtilities;

public class Main {

    public static void main(String[] args) {
        FlatDarkLaf.setup();
        KoboSyncResult syncResult = KoboDetector.synchronize();
        String rutaSegura = syncResult.databasePath();

        List<Book> misLibros = new ArrayList<>();
        List<Bookmark> misSubrayados = new ArrayList<>();
        boolean baseDisponible = syncResult.databaseAvailable();
        if (baseDisponible) {
            try {
                
                DataBaseConnection db = DataBaseConnection.getInstance();
                db.connect(rutaSegura);

                System.out.println("¡Estamos dentro y listos para extraer libros!");

                KoboDAO dao = new KoboDAO();
                misLibros = dao.getAllBooks();
                misSubrayados = dao.getAllHighlightsWithBook();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        List<Book> librosParaLaVista = misLibros;
        ReadingStatistics estadisticas = new LibraryStatisticsService()
                .calculate(misLibros, misSubrayados);
        SwingUtilities.invokeLater(() ->
                new MainFrame(librosParaLaVista, estadisticas,
                        syncResult.koboConnected()).setVisible(true));
    }
}
