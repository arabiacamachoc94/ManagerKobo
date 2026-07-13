
package com.arcac.managerkobo.database;

/**
 * Clase que usa el DatabaseConnection para mandar las consultas SQL.
 */


import com.arcac.managerkobo.model.Bookmark; 
import com.arcac.managerkobo.model.Book;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class KoboDAO {

    /**
     * Extrae todos los libros de la base de datos.
     */
        public List<Book> getAllBooks() {
        List<Book> books = new ArrayList<>();
        
        String sql = "SELECT ContentID, Title, Attribution, ReadStatus, ___PercentRead, (TimeSpentReading / 60) AS MinutosLeidos " +
                     "FROM content WHERE ContentType = 6 AND Title IS NOT NULL";

        try {
            Connection conn = DataBaseConnection.getInstance().getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Book b = new Book();
                b.setContentId(rs.getString("ContentID"));
                b.setTitle(rs.getString("Title"));                         
                b.setAuthor(rs.getString("Attribution"));              
                b.setReadStatus(rs.getInt("ReadStatus"));
                b.setPercentRead(rs.getInt("___PercentRead"));
                b.setMinutesRead(rs.getInt("MinutosLeidos"));
                
                books.add(b);
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener los libros: " + e.getMessage());
        }

        return books;
    }

    /**
     * Extrae todos los subrayados y notas.
     */
    public List<Bookmark> getAllBookmarks() { 
        List<Bookmark> bookmarks = new ArrayList<>();
        String sql = "SELECT BookmarkID, VolumeID, Text, Annotation, DateCreated FROM Bookmark WHERE Text IS NOT NULL";

        try {
            Connection conn = DataBaseConnection.getInstance().getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Bookmark mark = new Bookmark();
                mark.setBookmarkId(rs.getString("BookmarkID"));
                mark.setVolumeId(rs.getString("VolumeID"));
                mark.setText(rs.getString("Text"));
                mark.setUserNote(rs.getString("Annotation"));
                mark.setDateCreated(rs.getString("DateCreated"));
                
                bookmarks.add(mark);
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener los subrayados: " + e.getMessage());
        }

        return bookmarks;
    }
}