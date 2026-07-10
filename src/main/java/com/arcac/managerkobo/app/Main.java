
package com.arcac.managerkobo.app;

import com.arcac.managerkobo.database.DataBaseConnection;
import java.sql.SQLException;


public class Main {
    public static void main(String[] args) {
        
        String rutaTest = "C:\\Users\\arcac\\OneDrive\\Escritorio\\BBDD_Kobo\\KoboReader.sqlite";
        
        try {
            
            DataBaseConnection db = DataBaseConnection.getInstance();
            db.connect(rutaTest);
            System.out.println("¡Éxito! Podemos leer el Kobo.");
            
            
            db.disconnect();
            
        } catch (SQLException e) {
            System.err.println("Error al conectar: " + e.getMessage());
        }
    }
}