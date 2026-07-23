package com.arcac.managerkobo.ui.panels;

import com.arcac.managerkobo.model.Bookmark;
import com.arcac.managerkobo.ui.components.HighlightListPanel;
import com.arcac.managerkobo.ui.theme.AppTheme;
import java.awt.BorderLayout;
import java.awt.Font;
import java.util.List;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

/** Pantalla global para consultar todos los subrayados. */
public class HighlightsPanel extends JPanel {

    public HighlightsPanel(List<Bookmark> highlights) {
        List<Bookmark> safeHighlights = highlights == null ? List.of() : highlights;
        setLayout(new BorderLayout());
        setBackground(AppTheme.BACKGROUND);
        add(createHeader(safeHighlights.size()), BorderLayout.NORTH);
        HighlightListPanel list = new HighlightListPanel(safeHighlights, true);
        list.setBorder(new EmptyBorder(0, 32, 28, 32));
        add(list, BorderLayout.CENTER);
    }

    private JPanel createHeader(int total) {
        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBorder(new EmptyBorder(30, 32, 22, 32));
        JLabel title = new JLabel("Subrayados");
        title.setFont(AppTheme.font(Font.BOLD, 29));
        title.setForeground(AppTheme.TEXT);
        JLabel subtitle = new JLabel(total + " fragmentos guardados en tu Kobo");
        subtitle.setFont(AppTheme.font(Font.PLAIN, 14));
        subtitle.setForeground(AppTheme.MUTED_TEXT);
        header.add(title);
        header.add(Box.createVerticalStrut(5));
        header.add(subtitle);
        return header;
    }
}
