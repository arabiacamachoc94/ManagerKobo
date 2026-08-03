package com.arcac.managerkobo.model;

/** Palabra consultada en el diccionario del Kobo. */
public record LookedUpWord(
        String text,
        String volumeId,
        String dictionarySuffix,
        String dateCreated,
        String bookTitle,
        String bookAuthor) {
}
