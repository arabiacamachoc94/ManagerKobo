package com.arcac.managerkobo.model;

/** Representa un marcador, una nota o un subrayado, junto con su libro. */
public class Bookmark {
    private String bookmarkId;
    private String volumeId;
    private String contentId;
    private String text;
    private String userNote;
    private String dateCreated;
    private String dateModified;
    private double chapterProgress;
    private String type;
    private int color;
    private String contextString;

    // Datos obtenidos al relacionar Bookmark con content.
    private String bookTitle;
    private String bookAuthor;
    private String chapterTitle;

    public Bookmark() { }

    public Bookmark(String bookmarkId, String volumeId, String text,
                    String userNote, String dateCreated) {
        this.bookmarkId = bookmarkId;
        this.volumeId = volumeId;
        this.text = text;
        this.userNote = userNote;
        this.dateCreated = dateCreated;
    }

    public boolean isHighlight() { return "highlight".equalsIgnoreCase(type); }
    public boolean hasUserNote() { return userNote != null && !userNote.isBlank(); }

    public String getBookmarkId() { return bookmarkId; }
    public void setBookmarkId(String value) { bookmarkId = value; }
    public String getVolumeId() { return volumeId; }
    public void setVolumeId(String value) { volumeId = value; }
    public String getContentId() { return contentId; }
    public void setContentId(String value) { contentId = value; }
    public String getText() { return text; }
    public void setText(String value) { text = value; }
    public String getUserNote() { return userNote; }
    public void setUserNote(String value) { userNote = value; }
    public String getDateCreated() { return dateCreated; }
    public void setDateCreated(String value) { dateCreated = value; }
    public String getDateModified() { return dateModified; }
    public void setDateModified(String value) { dateModified = value; }
    public double getChapterProgress() { return chapterProgress; }
    public void setChapterProgress(double value) { chapterProgress = value; }
    public String getType() { return type; }
    public void setType(String value) { type = value; }
    public int getColor() { return color; }
    public void setColor(int value) { color = value; }
    public String getContextString() { return contextString; }
    public void setContextString(String value) { contextString = value; }
    public String getBookTitle() { return bookTitle; }
    public void setBookTitle(String value) { bookTitle = value; }
    public String getBookAuthor() { return bookAuthor; }
    public void setBookAuthor(String value) { bookAuthor = value; }
    public String getChapterTitle() { return chapterTitle; }
    public void setChapterTitle(String value) { chapterTitle = value; }

    @Override
    public String toString() {
        String preview = text != null && text.length() > 50 ? text.substring(0, 50) + "..." : text;
        return "Subrayado de " + (bookTitle == null ? "libro desconocido" : bookTitle)
                + ": [" + preview + "]";
    }
}
