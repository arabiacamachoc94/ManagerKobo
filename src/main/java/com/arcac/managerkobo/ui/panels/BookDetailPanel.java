package com.arcac.managerkobo.ui.panels;

import com.arcac.managerkobo.model.Book;
import com.arcac.managerkobo.model.Bookmark;
import com.arcac.managerkobo.ui.components.HighlightListPanel;
import com.arcac.managerkobo.ui.components.RoundedPanel;
import com.arcac.managerkobo.ui.theme.AppTheme;
import com.arcac.managerkobo.ui.util.IconLoader;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Dimension;
import java.util.List;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.border.EmptyBorder;

/** Vista sencilla de un libro y de los subrayados que le pertenecen. */
public class BookDetailPanel extends JPanel {

    public BookDetailPanel(Book book, List<Bookmark> highlights, Runnable backAction) {
        setLayout(new BorderLayout(0, 16));
        setBackground(AppTheme.BACKGROUND);
        add(createHeader(book, backAction), BorderLayout.NORTH);

        JPanel content = new JPanel(new BorderLayout(0, 18));
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(0, 32, 28, 32));
        content.add(createSummary(book, highlights.size()), BorderLayout.NORTH);
        content.add(new HighlightListPanel(highlights, false), BorderLayout.CENTER);
        add(content, BorderLayout.CENTER);
    }

    private JPanel createHeader(Book book, Runnable backAction) {
        JPanel header = new JPanel(new BorderLayout(16, 0));
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(25, 32, 10, 32));
        JButton back = new JButton();
        back.setIcon(IconLoader.loadTinted("/icons/back.png", 18, AppTheme.TEXT));
        back.setToolTipText("Volver a Biblioteca");
        back.getAccessibleContext().setAccessibleName("Volver a Biblioteca");
        back.setPreferredSize(new Dimension(42, 42));
        back.setFont(AppTheme.font(Font.BOLD, 13));
        back.setForeground(AppTheme.TEXT);
        back.setBackground(AppTheme.PANEL_ALT);
        back.setFocusPainted(false);
        back.setBorder(new EmptyBorder(10, 14, 10, 14));
        back.addActionListener(event -> backAction.run());
        header.add(back, BorderLayout.WEST);

        JLabel title = new JLabel(fallback(book.getTitle(), "Sin título"));
        title.setFont(AppTheme.font(Font.BOLD, 24));
        title.setForeground(AppTheme.TEXT);
        header.add(title, BorderLayout.CENTER);
        return header;
    }

    private JPanel createSummary(Book book, int highlightCount) {
        RoundedPanel card = new RoundedPanel(18, AppTheme.PANEL);
        card.setLayout(new BorderLayout(22, 12));
        card.setBorder(new EmptyBorder(20, 22, 20, 22));

        JLabel cover = new JLabel("▥");
        cover.setFont(AppTheme.font(Font.PLAIN, 48));
        cover.setForeground(AppTheme.PURPLE);
        card.add(cover, BorderLayout.WEST);

        JPanel info = new JPanel();
        info.setOpaque(false);
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        JLabel author = new JLabel(fallback(book.getAuthor(), "Autor desconocido"));
        author.setFont(AppTheme.font(Font.BOLD, 16));
        author.setForeground(AppTheme.TEXT);
        info.add(author);
        info.add(Box.createVerticalStrut(5));
        JLabel metadata = new JLabel(metadata(book));
        metadata.setFont(AppTheme.font(Font.PLAIN, 12));
        metadata.setForeground(AppTheme.MUTED_TEXT);
        info.add(metadata);
        info.add(Box.createVerticalStrut(13));
        JProgressBar progress = new JProgressBar(0, 100);
        progress.setValue(book.getPercentRead());
        progress.setStringPainted(true);
        progress.setForeground(AppTheme.PURPLE);
        progress.setBackground(AppTheme.BORDER);
        progress.setBorderPainted(false);
        info.add(progress);
        card.add(info, BorderLayout.CENTER);

        JPanel metrics = new JPanel(new GridLayout(2, 1, 0, 6));
        metrics.setOpaque(false);
        metrics.add(metric(formatTime(book.getSecondsRead()), "Tiempo leído"));
        metrics.add(metric(String.valueOf(highlightCount), "Subrayados"));
        card.add(metrics, BorderLayout.EAST);
        return card;
    }

    private JPanel metric(String value, String caption) {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(AppTheme.font(Font.BOLD, 17));
        valueLabel.setForeground(AppTheme.TEXT);
        JLabel captionLabel = new JLabel(caption);
        captionLabel.setFont(AppTheme.font(Font.PLAIN, 11));
        captionLabel.setForeground(AppTheme.MUTED_TEXT);
        panel.add(valueLabel);
        panel.add(captionLabel);
        return panel;
    }

    private String metadata(Book book) {
        String publisher = fallback(book.getPublisher(), "Editorial desconocida");
        String language = fallback(book.getLanguage(), "Idioma desconocido").toUpperCase();
        return publisher + " · " + language + " · " + status(book);
    }

    private String status(Book book) {
        if (book.isFinished()) return "Terminado";
        if (book.isInProgress()) return "Leyendo";
        return "Sin empezar";
    }

    private String formatTime(int seconds) {
        return (seconds / 3600) + " h " + ((seconds % 3600) / 60) + " min";
    }

    private String fallback(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}
