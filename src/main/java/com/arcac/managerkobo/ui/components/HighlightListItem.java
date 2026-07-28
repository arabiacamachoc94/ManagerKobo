package com.arcac.managerkobo.ui.components;

import com.arcac.managerkobo.model.Bookmark;

/** Tipos de fila que pueden aparecer en la lista agrupada de subrayados. */
public sealed interface HighlightListItem
        permits HighlightListItem.BookGroup, HighlightListItem.Highlight {

    record BookGroup(
            String groupId,
            String title,
            String author,
            int highlightCount,
            int selectedCount,
            boolean selectionMode,
            boolean expanded) implements HighlightListItem {
    }

    record Highlight(
            Bookmark bookmark,
            boolean indented,
            boolean selectionMode,
            boolean marked) implements HighlightListItem {
    }
}
