package com.arcac.managerkobo.ui.components;

import com.arcac.managerkobo.model.Book;
import com.arcac.managerkobo.service.BookCoverService;
import com.arcac.managerkobo.ui.theme.AppTheme;
import com.arcac.managerkobo.ui.util.I18n;
import com.arcac.managerkobo.ui.util.IconLoader;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import static com.arcac.managerkobo.util.ReadingFormat.textOr;

/** Tarjeta visual y pulsable de un libro. */
public class BookCard extends RoundedPanel {
    public static final int CARD_WIDTH = 174;
    public static final int CARD_HEIGHT = 306;
    private static final int COVER_WIDTH = 132;
    private static final int COVER_HEIGHT = 184;
    private static final BookCoverService COVER_SERVICE = new BookCoverService();

    public BookCard(Book book, Consumer<Book> openBookAction) {
        this(book, openBookAction, false);
    }

    public BookCard(Book book, Consumer<Book> openBookAction,
            boolean compact) {
        super(18, AppTheme.PANEL);
        int cardWidth = compact ? 148 : CARD_WIDTH;
        int cardHeight = compact ? 230 : CARD_HEIGHT;
        int coverWidth = compact ? 82 : COVER_WIDTH;
        int coverHeight = compact ? 112 : COVER_HEIGHT;
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(12, 12, 12, 12));
        setPreferredSize(new Dimension(cardWidth, cardHeight));
        setMinimumSize(getPreferredSize());
        setMaximumSize(getPreferredSize());
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        add(createCover(book, coverWidth, coverHeight), BorderLayout.NORTH);
        add(createInformation(book, compact,
                compact ? coverWidth : 142), BorderLayout.CENTER);
        installClickAction(this, book, openBookAction);
    }

    private JPanel createCover(Book book, int coverWidth, int coverHeight) {
        RoundedPanel holder = new RoundedPanel(13, AppTheme.PANEL_ALT);
        holder.setLayout(new BorderLayout());
        holder.setPreferredSize(new Dimension(coverWidth, coverHeight));

        JLabel cover = new JLabel();
        cover.setHorizontalAlignment(SwingConstants.CENTER);
        cover.setVerticalAlignment(SwingConstants.CENTER);
        cover.setIcon(IconLoader.loadTinted(
                "/icons/libro.png", 46, AppTheme.PURPLE));
        holder.add(cover, BorderLayout.CENTER);

        COVER_SERVICE.loadAsync(book, coverWidth, coverHeight, image -> {
            if (image != null) cover.setIcon(image);
        });
        return holder;
    }

    private JPanel createInformation(
            Book book, boolean compact, int contentWidth) {
        JPanel information = new JPanel();
        information.setOpaque(false);
        information.setLayout(new BoxLayout(information, BoxLayout.Y_AXIS));
        information.setBorder(new EmptyBorder(compact ? 7 : 10, 1, 0, 1));

        JLabel title = label(twoLineText(
                        textOr(book.getTitle(), "Sin título"), contentWidth),
                compact ? 11 : 13, Font.BOLD, AppTheme.TEXT);
        title.setToolTipText(textOr(book.getTitle(), "Sin título"));
        title.setAlignmentX(LEFT_ALIGNMENT);
        title.setMaximumSize(new Dimension(
                Integer.MAX_VALUE, compact ? 31 : 38));
        information.add(title);
        information.add(Box.createVerticalStrut(3));

        JLabel author = label(textOr(book.getAuthor(), "Autor desconocido"),
                compact ? 9 : 11, Font.PLAIN, AppTheme.MUTED_TEXT);
        author.setToolTipText(author.getText());
        author.setAlignmentX(LEFT_ALIGNMENT);
        author.setMaximumSize(new Dimension(Integer.MAX_VALUE, 18));
        information.add(author);
        information.add(Box.createVerticalGlue());

        JPanel status = new JPanel(new BorderLayout());
        status.setOpaque(false);
        status.setAlignmentX(LEFT_ALIGNMENT);
        status.setMaximumSize(new Dimension(Integer.MAX_VALUE, 18));
        status.add(label(statusOf(book), 10, Font.BOLD, statusColor(book)),
                BorderLayout.WEST);
        status.add(label(book.getPercentRead() + "%", 10, Font.BOLD,
                AppTheme.TEXT), BorderLayout.EAST);
        information.add(status);
        information.add(Box.createVerticalStrut(5));

        JProgressBar progress = new JProgressBar(0, 100);
        progress.setValue(Math.max(0, Math.min(100, book.getPercentRead())));
        progress.setForeground(book.isFinished()
                ? AppTheme.GREEN : AppTheme.PURPLE);
        progress.setBackground(AppTheme.BORDER);
        progress.setBorderPainted(false);
        progress.setAlignmentX(LEFT_ALIGNMENT);
        progress.setMaximumSize(new Dimension(Integer.MAX_VALUE, 7));
        progress.setPreferredSize(new Dimension(130, 7));
        information.add(progress);
        return information;
    }

    private void installClickAction(
            Component component, Book book, Consumer<Book> action) {
        component.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        component.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent event) {
                if (javax.swing.SwingUtilities.isLeftMouseButton(event)) {
                    event.consume();
                    action.accept(book);
                }
            }
        });
        if (component instanceof JPanel panel) {
            for (Component child : panel.getComponents()) {
                installClickAction(child, book, action);
            }
        }
    }

    private JLabel label(String text, int size, int style, Color color) {
        JLabel label = new JLabel(text);
        label.setFont(AppTheme.font(style, size));
        label.setForeground(color);
        return label;
    }

    private String statusOf(Book book) {
        if (book.isFinished()) return I18n.text("Terminado");
        if (book.isInProgress()) return I18n.text("Leyendo");
        return I18n.text("Sin empezar");
    }

    private Color statusColor(Book book) {
        if (book.isFinished()) return AppTheme.GREEN;
        if (book.isInProgress()) return AppTheme.PURPLE;
        return AppTheme.MUTED_TEXT;
    }

    private String twoLineText(String text, int contentWidth) {
        String escaped = text.replace("&", "&amp;")
                .replace("<", "&lt;").replace(">", "&gt;");
        return "<html><div style='width:" + Math.max(60, contentWidth - 4)
                + "px'>" + escaped + "</div></html>";
    }

}
