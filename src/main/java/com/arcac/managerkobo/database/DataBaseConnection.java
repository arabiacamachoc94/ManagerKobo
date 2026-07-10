
package com.arcac.managerkobo.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 *Clase para abrir y cerrar la conexión
 */

public class DataBaseConnection {

    // 1. La variable estática para el Singleton y el objeto Connection
    private static DataBaseConnection instance;
    private Connection connection;

    
    private DataBaseConnection() { }


    // 1. getInstance(): Devuelve la única instancia de la clase
    public static DataBaseConnection getInstance() {
        if (instance == null) {
            instance = new DataBaseConnection();
        }
        return instance;
    }

    // 2. connect(): Abre la conexión con el archivo SQLite
    public void connect(String databasePath) throws SQLException {
        if (connection != null && !connection.isClosed()) {
            return; 
        }
        // Le indicamos al driver de JDBC que use SQLite
        String url = "jdbc:sqlite:" + databasePath;
        connection = DriverManager.getConnection(url);
        System.out.println("Conexión a la base de datos abierta.");
    }

    // 3. disconnect(): Cierra la conexión de forma segura
    public void disconnect() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("Conexión a la base de datos cerrada.");
            }
        } catch (SQLException e) {
            System.err.println("Error al intentar cerrar la conexión: " + e.getMessage());
        }
    }

    // 4. getConnection(): Necesario para que otras clases puedan hacer los SELECT
    public Connection getConnection() {
        return connection;
    }
}