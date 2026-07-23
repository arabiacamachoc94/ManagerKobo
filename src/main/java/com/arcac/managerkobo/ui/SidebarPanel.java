package com.arcac.managerkobo.ui;

import com.arcac.managerkobo.ui.theme.AppTheme;
import com.arcac.managerkobo.ui.util.IconLoader;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

/** Menú lateral. Comunica la página elegida mediante un Consumer. */
public class SidebarPanel extends JPanel {

    public static final String DASHBOARD = "dashboard";
    public static final String LIBRARY = "library";
    public static final String HIGHLIGHTS = "highlights";
    public static final String WORDS = "words";
    public static final String ACHIEVEMENTS = "achievements";
    public static final String SETTINGS = "settings";

    private final Consumer<String> navigationAction;
    private final List<JButton> buttons = new ArrayList<>();
    private final JLabel connectionStatus = new JLabel();

    public SidebarPanel(Consumer<String> navigationAction, boolean koboConnected) {
        this.navigationAction = navigationAction;
        setLayout(new BorderLayout());
        setBackground(AppTheme.SIDEBAR);
        setPreferredSize(new Dimension(220, 0));
        setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, AppTheme.BORDER));
        add(createBrand(), BorderLayout.NORTH);
        add(createMenu(), BorderLayout.CENTER);
        configureStatus();
        setKoboConnected(koboConnected);
        add(connectionStatus, BorderLayout.SOUTH);
        select(DASHBOARD);
    }

    private JLabel createBrand() {
        JLabel brand = new JLabel("Kobo Manager");
        brand.setForeground(AppTheme.TEXT);
        brand.setFont(AppTheme.font(Font.BOLD, 20));
        brand.setBorder(new EmptyBorder(28, 23, 25, 15));
        return brand;
    }

    private JPanel createMenu() {
        JPanel menu = new JPanel();
        menu.setOpaque(false);
        menu.setLayout(new BoxLayout(menu, BoxLayout.Y_AXIS));
        addButton(menu, "/icons/dashboard.png", "Dashboard", DASHBOARD);
        addButton(menu, "/icons/library.png", "Biblioteca", LIBRARY);
        addButton(menu, "/icons/lapiz.png", "Subrayados", HIGHLIGHTS);
        addButton(menu, "/icons/palabras.png", "Palabras", WORDS);
        addButton(menu, "/icons/logros.png", "Logros", ACHIEVEMENTS);
        addButton(menu, "/icons/settings.png", "Ajustes", SETTINGS);
        return menu;
    }

    private void addButton(JPanel menu, String iconPath, String text, String page) {
        JButton button = new JButton(text);
        button.setName(page);
        button.putClientProperty("iconPath", iconPath);
        button.putClientProperty("label", text);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setIconTextGap(14);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 47));
        button.setFont(AppTheme.font(Font.PLAIN, 14));
        button.setForeground(AppTheme.MUTED_TEXT);
        button.setBackground(AppTheme.SIDEBAR);
        button.setBorder(new EmptyBorder(12, 23, 12, 15));
        button.setFocusPainted(false);
        button.addActionListener(event -> {
            select(page);
            navigationAction.accept(page);
        });
        buttons.add(button);
        menu.add(button);
        menu.add(Box.createVerticalStrut(6));
    }

    private void select(String page) {
        for (JButton button : buttons) {
            boolean selected = page.equals(button.getName());
            button.setBackground(selected ? AppTheme.NAV_SELECTED : AppTheme.SIDEBAR);
            button.setForeground(selected ? Color.WHITE : AppTheme.MUTED_TEXT);
            Color iconColor = selected ? Color.WHITE : AppTheme.MUTED_TEXT;
            String iconPath = (String) button.getClientProperty("iconPath");
            javax.swing.ImageIcon icon = IconLoader.loadTinted(iconPath, 20, iconColor);
            button.setIcon(icon);
            String label = (String) button.getClientProperty("label");
            button.setText(label);
        }
    }

    private void configureStatus() {
        connectionStatus.setFont(AppTheme.font(Font.PLAIN, 13));
        connectionStatus.setBorder(new EmptyBorder(18, 24, 24, 10));
    }

    public void setKoboConnected(boolean connected) {
        connectionStatus.setText(connected ? "●  Kobo conectado" : "●  Modo local");
        connectionStatus.setForeground(connected ? AppTheme.GREEN : AppTheme.MUTED_TEXT);
    }
}
