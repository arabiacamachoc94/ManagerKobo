
package com.arcac.managerkobo.model;

/**
 * Clase que representa notas y subrayados 
 */

public class Bookmark {
    private String bookmarkId;
    private String volumeId; // Este es el ID del libro al que pertenece (el contentId del Book)
    private String text; // El fragmento exacto que el usuario subrayó del libro
    private String userNote; // La nota escrita por el usuario con el teclado (si la hay)
    private String dateCreated; // Fecha de creación

    public Bookmark() {
    }

    public Bookmark(String bookmarkId, String volumeId, String text, String userNote, String dateCreated) {
        this.bookmarkId = bookmarkId;
        this.volumeId = volumeId;
        this.text = text;
        this.userNote = userNote;
        this.dateCreated = dateCreated;
    }

    // --- GETTERS Y SETTERS ---

    public String getBookmarkId() {
        return bookmarkId;
    }

    public void setBookmarkId(String bookmarkId) {
        this.bookmarkId = bookmarkId;
    }

    public String getVolumeId() {
        return volumeId;
    }

    public void setVolumeId(String volumeId) {
        this.volumeId = volumeId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getUserNote() {
        return userNote;
    }

    public void setUserNote(String userNote) {
        this.userNote = userNote;
    }

    public String getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(String dateCreated) {
        this.dateCreated = dateCreated;
    }

    @Override
    public String toString() {
        
        String preview = text != null && text.length() > 30 ? text.substring(0, 30) + "..." : text;
        return "Subrayado: [" + preview + "] | Nota: " + (userNote != null ? userNote : "Ninguna");
    }
}