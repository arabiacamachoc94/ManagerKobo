package com.arcac.managerkobo.ui;

import com.arcac.managerkobo.ui.theme.AppTheme;
import com.arcac.managerkobo.ui.util.IconLoader;
import com.arcac.managerkobo.ui.util.AppPreferences;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
    public static final String SETTINGS = "settings";

    private final Consumer<String> navigationAction;
    private final List<JButton> buttons = new ArrayList<>();
    private final JLabel connectionStatus = new JLabel();
    private final JLabel synchronizationStatus = new JLabel();
    private final JButton syncButton = new CircularButton();

    public SidebarPanel(Consumer<String> navigationAction,
            Runnable syncAction, boolean koboConnected) {
        this.navigationAction = navigationAction;
        setLayout(new BorderLayout());
        setBackground(AppTheme.SIDEBAR);
        setPreferredSize(new Dimension(220, 0));
        setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, AppTheme.BORDER));
        add(createBrand(), BorderLayout.NORTH);
        add(createMenu(), BorderLayout.CENTER);
        configureStatus();
        configureSyncButton(syncAction);
        setKoboConnected(koboConnected);
        add(createFooter(), BorderLayout.SOUTH);
        select(DASHBOARD);
    }

    private JLabel createBrand() {
        JLabel brand = new JLabel("Kobo Manager");
        brand.setIcon(IconLoader.load("/icons/logo2-transparent.png", 30));
        brand.setIconTextGap(10);
        brand.setForeground(AppTheme.TEXT);
        brand.setFont(AppTheme.font(Font.BOLD, 20));
        brand.setBorder(new EmptyBorder(28, 23, 25, 15));
        return brand;
    }

    private JPanel createMenu() {
        JPanel menu = new JPanel();
        menu.setOpaque(false);
        menu.setLayout(new BoxLayout(menu, BoxLayout.Y_AXIS));
        boolean english = AppPreferences.isEnglish();
        addButton(menu, "/icons/dashboard.png", english ? "Overview" : "Resumen", DASHBOARD);
        addButton(menu, "/icons/library.png", english ? "Library" : "Biblioteca", LIBRARY);
        addButton(menu, "/icons/lapiz.png", english ? "Highlights" : "Subrayados", HIGHLIGHTS);
        addButton(menu, "/icons/palabras.png", english ? "Words" : "Palabras", WORDS);
        addButton(menu, "/icons/settings.png", english ? "Settings" : "Ajustes", SETTINGS);
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
        connectionStatus.setBorder(new EmptyBorder(0, 9, 0, 0));
    }

    private void configureSyncButton(Runnable syncAction) {
        syncButton.setIcon(IconLoader.loadTinted(
                "/icons/actualizar.png", 19, AppTheme.MUTED_TEXT));
        syncButton.setToolTipText(AppPreferences.isEnglish()
                ? "Sync Kobo" : "Sincronizar Kobo");
        syncButton.setContentAreaFilled(false);
        syncButton.setFocusPainted(false);
        syncButton.setBorderPainted(false);
        syncButton.setBorder(new EmptyBorder(0, 0, 0, 0));
        syncButton.setHorizontalAlignment(SwingConstants.CENTER);
        syncButton.setVerticalAlignment(SwingConstants.CENTER);
        syncButton.setPreferredSize(new Dimension(36, 36));
        syncButton.addActionListener(event -> syncAction.run());
    }

    private JPanel createFooter() {
        JPanel footer = new JPanel();
        footer.setLayout(new BoxLayout(footer, BoxLayout.Y_AXIS));
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(14, 22, 22, 10));
        JPanel statusRow = new JPanel(new BorderLayout());
        statusRow.setOpaque(false);
        statusRow.setAlignmentX(LEFT_ALIGNMENT);
        statusRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        statusRow.add(syncButton, BorderLayout.WEST);
        statusRow.add(connectionStatus, BorderLayout.CENTER);
        footer.add(statusRow);
        footer.add(Box.createVerticalStrut(5));
        synchronizationStatus.setFont(AppTheme.font(Font.PLAIN, 11));
        synchronizationStatus.setForeground(AppTheme.MUTED_TEXT);
        synchronizationStatus.setAlignmentX(LEFT_ALIGNMENT);
        footer.add(synchronizationStatus);
        return footer;
    }

    public void setLastSynchronization(LocalDateTime date) {
        synchronizationStatus.setText(date == null
                ? (AppPreferences.isEnglish()
                        ? "Last sync: unavailable"
                        : "Última sincronización: no disponible")
                : (AppPreferences.isEnglish() ? "Last sync: " : "Última: ")
                        + date.format(DateTimeFormatter.ofPattern(
                                "dd/MM/yyyy · HH:mm")));
    }

    public void setSyncing(boolean syncing) {
        syncButton.setEnabled(!syncing);
        syncButton.setToolTipText(syncing
                ? (AppPreferences.isEnglish() ? "Syncing..." : "Sincronizando...")
                : (AppPreferences.isEnglish() ? "Sync Kobo" : "Sincronizar Kobo"));
    }

    public void setKoboConnected(boolean connected) {
        connectionStatus.setText(connected
                ? (AppPreferences.isEnglish() ? "●  Kobo connected" : "●  Kobo conectado")
                : (AppPreferences.isEnglish() ? "●  Local mode" : "●  Modo local"));
        connectionStatus.setForeground(connected ? AppTheme.GREEN : AppTheme.MUTED_TEXT);
    }

    private static final class CircularButton extends JButton {
        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D copy = (Graphics2D) graphics.create();
            copy.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            copy.setColor(getModel().isPressed()
                    ? AppTheme.NAV_SELECTED : AppTheme.PANEL_ALT);
            copy.fillOval(0, 0, getWidth() - 1, getHeight() - 1);
            copy.dispose();
            super.paintComponent(graphics);
        }
    }
}
