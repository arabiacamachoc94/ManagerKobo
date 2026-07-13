package com.arcac.managerkobo.app;

import com.arcac.managerkobo.database.DataBaseConnection;
import com.arcac.managerkobo.database.KoboDAO;
import com.arcac.managerkobo.model.Book;
import com.arcac.managerkobo.util.KoboDetector;
import java.sql.SQLException;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        // 1. Busca y copia (nos devuelve un String con la ruta, ej: "data/KoboReader.sqlite")
        String rutaSegura = KoboDetector.detectAndCopyDatabase();

        // 2. Comprobamos si la encontró
        if (rutaSegura != null) {
            try {
                // 3. Le pasamos esa ruta a la conexión 
                DataBaseConnection db = DataBaseConnection.getInstance();
                db.connect(rutaSegura);

                System.out.println("¡Estamos dentro y listos para extraer libros!");

                KoboDAO dao = new KoboDAO();
                List<Book> misLibros = dao.getAllBooks();

                System.out.println("\n--- MI BIBLIOTECA KOBO ---");
                for (Book libro : misLibros) {
                    System.out.println(libro.toString());
                }

                db.disconnect();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
