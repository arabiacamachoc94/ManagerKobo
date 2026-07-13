/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.arcac.managerkobo.model;

/**
 * Clase que representa un libro y sus atributos
 */


public class Book {
    private String contentId;
    private String title;
    private String author;
    private int readStatus; // 0 = No leído, 1 = Leyendo, 2 = Terminado
    private int percentRead; // De 0 a 100
    private int minutesRead;

  
    public Book() {
    }


    public Book(String contentId, String title, String author, int readStatus, int percentRead, int minutesRead) {
        this.contentId = contentId;
        this.title = title;
        this.author = author;
        this.readStatus = readStatus;
        this.percentRead = percentRead;
        this.minutesRead = minutesRead;
    }

    // --- GETTERS Y SETTERS ---

    public String getContentId() {
        return contentId;
    }

    public void setContentId(String contentId) {
        this.contentId = contentId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public int getReadStatus() {
        return readStatus;
    }

    public void setReadStatus(int readStatus) {
        this.readStatus = readStatus;
    }

    public int getPercentRead() {
        return percentRead;
    }

    public void setPercentRead(int percentRead) {
        this.percentRead = percentRead;
    }

    public int getMinutesRead() {
        return minutesRead;
    }

    public void setMinutesRead(int minutesRead) {
        this.minutesRead = minutesRead;
    }

    
    @Override
    public String toString() {
        return "Libro: " + title + " | Autor: " + author + " | Progreso: " + percentRead + "% | Minutos: " + minutesRead;
    }
}