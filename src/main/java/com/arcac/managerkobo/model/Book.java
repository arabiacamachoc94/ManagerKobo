package com.arcac.managerkobo.model;

/** Representa un libro y los datos de lectura almacenados por Kobo. */
public class Book {
    private String contentId;
    private String title;
    private String subtitle;
    private String author;
    private String publisher;
    private String description;
    private String isbn;
    private String language;
    private String series;
    private Double seriesNumber;
    private String imageId;
    private String imageUrl;

    private int readStatus;
    private int percentRead;
    private int secondsRead;
    private int timesStartedReading;

    private String dateAdded;
    private String dateLastRead;
    private String lastTimeStartedReading;
    private String lastTimeFinishedReading;

    private String currentChapterId;
    private double currentChapterProgress;
    private int restOfBookEstimate;
    private int currentChapterEstimate;

    public Book() { }


    public boolean isFinished() {
        return readStatus == 2 || percentRead >= 100;
    }

    public boolean isInProgress() {
        return !isFinished() && (readStatus == 1 || percentRead > 0);
    }

    public boolean isNotStarted() {
        return !isFinished() && !isInProgress();
    }

    public int getMinutesRead() { return secondsRead / 60; }
    public double getHoursRead() { return secondsRead / 3600.0; }

    public void setMinutesRead(int minutesRead) { this.secondsRead = Math.max(0, minutesRead) * 60; }

    public String getContentId() { return contentId; }
    public void setContentId(String contentId) { this.contentId = contentId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getSubtitle() { return subtitle; }
    public void setSubtitle(String subtitle) { this.subtitle = subtitle; }
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    public String getPublisher() { return publisher; }
    public void setPublisher(String publisher) { this.publisher = publisher; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }
    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }
    public String getSeries() { return series; }
    public void setSeries(String series) { this.series = series; }
    public Double getSeriesNumber() { return seriesNumber; }
    public void setSeriesNumber(Double seriesNumber) { this.seriesNumber = seriesNumber; }
    public String getImageId() { return imageId; }
    public void setImageId(String imageId) { this.imageId = imageId; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public int getReadStatus() { return readStatus; }
    public void setReadStatus(int readStatus) { this.readStatus = readStatus; }
    public int getPercentRead() { return percentRead; }
    public void setPercentRead(int percentRead) { this.percentRead = percentRead; }
    public int getSecondsRead() { return secondsRead; }
    public void setSecondsRead(int secondsRead) { this.secondsRead = Math.max(0, secondsRead); }
    public int getTimesStartedReading() { return timesStartedReading; }
    public void setTimesStartedReading(int timesStartedReading) { this.timesStartedReading = timesStartedReading; }
    public String getDateAdded() { return dateAdded; }
    public void setDateAdded(String dateAdded) { this.dateAdded = dateAdded; }
    public String getDateLastRead() { return dateLastRead; }
    public void setDateLastRead(String dateLastRead) { this.dateLastRead = dateLastRead; }
    public String getLastTimeStartedReading() { return lastTimeStartedReading; }
    public void setLastTimeStartedReading(String value) { this.lastTimeStartedReading = value; }
    public String getLastTimeFinishedReading() { return lastTimeFinishedReading; }
    public void setLastTimeFinishedReading(String value) { this.lastTimeFinishedReading = value; }
    public String getCurrentChapterId() { return currentChapterId; }
    public void setCurrentChapterId(String currentChapterId) { this.currentChapterId = currentChapterId; }
    public double getCurrentChapterProgress() { return currentChapterProgress; }
    public void setCurrentChapterProgress(double value) { this.currentChapterProgress = value; }
    public int getRestOfBookEstimate() { return restOfBookEstimate; }
    public void setRestOfBookEstimate(int value) { this.restOfBookEstimate = value; }
    public int getCurrentChapterEstimate() { return currentChapterEstimate; }
    public void setCurrentChapterEstimate(int value) { this.currentChapterEstimate = value; }

    @Override
    public String toString() {
        return "Libro: " + title + " | Autor: " + author + " | Progreso: "
                + percentRead + "% | Tiempo: " + getMinutesRead() + " min";
    }
}
