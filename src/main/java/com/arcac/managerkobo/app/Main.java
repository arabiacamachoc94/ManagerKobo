package com.arcac.managerkobo.app;

import com.arcac.managerkobo.service.KoboLibraryData;
import com.arcac.managerkobo.service.KoboLibraryService;
import com.arcac.managerkobo.service.LibraryStatisticsService;
import com.arcac.managerkobo.util.KoboSyncResult;
import com.formdev.flatlaf.FlatDarkLaf;
import com.arcac.managerkobo.ui.MainFrame;
import java.util.List;
import javax.swing.SwingUtilities;

public class Main {

    public static void main(String[] args) {
        FlatDarkLaf.setup();
        KoboLibraryService libraryService = new KoboLibraryService();
        KoboLibraryData libraryData;
        try {
            libraryData = libraryService.synchronizeAndLoad();
        } catch (Exception exception) {
            exception.printStackTrace();
            KoboSyncResult failedResult = new KoboSyncResult(
                    false, false, false, null,
                    "No se pudo cargar la biblioteca: " + rootMessage(exception));
            libraryData = new KoboLibraryData(
                    failedResult,
                    List.of(),
                    List.of(),
                    new LibraryStatisticsService().calculate(List.of(), List.of()));
        }

        KoboLibraryData initialData = libraryData;
        SwingUtilities.invokeLater(()
                -> new MainFrame(libraryService, initialData).setVisible(true));
    }

    private static String rootMessage(Throwable throwable) {
        Throwable current = throwable;
        while (current.getCause() != null) {
            current = current.getCause();
        }
        return current.getMessage() == null
                ? current.getClass().getSimpleName()
                : current.getMessage();
    }
}
