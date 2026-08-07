package com.arcac.managerkobo.ui.panels;

import com.arcac.managerkobo.ai.GeminiApiKeyStore;
import com.arcac.managerkobo.ui.theme.AppTheme;
import com.arcac.managerkobo.ui.components.RoundedButton;
import com.arcac.managerkobo.ui.util.AppPreferences;
import com.arcac.managerkobo.ui.util.I18n;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.JButton;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.border.EmptyBorder;

/** Pantalla minimalista de preferencias. */
public class SettingsPanel extends JPanel {
    private final Runnable applyAction;
    private final JLabel apiStatus = valueLabel();
    private final JButton apiButton = button("Introducir", AppTheme.PURPLE);
    private final JComboBox<String> language = new JComboBox<>(
            new String[]{"Español", "English"});
    private final JComboBox<String> theme = new JComboBox<>(
            AppPreferences.isEnglish()
                    ? new String[]{"Dark", "Light"}
                    : new String[]{"Oscuro", "Claro"});
    private final JButton applyButton = button("Aplicar", AppTheme.PURPLE);

    public SettingsPanel(Runnable applyAction) {
        this.applyAction = applyAction;
        setLayout(new BorderLayout());
        setBackground(AppTheme.BACKGROUND);
        add(createHeader(), BorderLayout.NORTH);
        add(createContent(), BorderLayout.CENTER);
        configureActions();
        refreshState();
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout(0, 5));
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(30, 32, 22, 32));
        header.add(label("Ajustes", 29, Font.BOLD, AppTheme.TEXT), BorderLayout.NORTH);
        header.add(label("Preferencias de Kobo Manager", 14, Font.PLAIN,
                AppTheme.MUTED_TEXT), BorderLayout.SOUTH);
        return header;
    }

    private JPanel createContent() {
        JPanel body = new JPanel(new BorderLayout());
        body.setOpaque(false);
        body.setBorder(new EmptyBorder(4, 32, 30, 32));

        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(AppTheme.PANEL);
        card.setBorder(new EmptyBorder(8, 22, 8, 22));
        addRow(card, 0, "API de Gemini", apiStatus, apiButton);
        addSeparator(card, 1);
        addRow(card, 2, "Idioma", valueLabel(""), language);
        addSeparator(card, 3);
        addRow(card, 4, "Tema", valueLabel(""), theme);

        JPanel holder = new JPanel(new BorderLayout());
        holder.setOpaque(false);
        JPanel cards = new JPanel();
        cards.setOpaque(false);
        cards.setLayout(new BoxLayout(cards, BoxLayout.Y_AXIS));
        card.setAlignmentX(LEFT_ALIGNMENT);
        cards.add(card);
        cards.add(Box.createVerticalStrut(18));
        JPanel about = createAboutPanel();
        about.setAlignmentX(LEFT_ALIGNMENT);
        cards.add(about);
        holder.add(cards, BorderLayout.NORTH);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 16));
        actions.setOpaque(false);
        actions.add(applyButton);
        holder.add(actions, BorderLayout.SOUTH);
        body.add(holder, BorderLayout.CENTER);
        return body;
    }

    private JPanel createAboutPanel() {
        JPanel about = new JPanel();
        about.setLayout(new BoxLayout(about, BoxLayout.Y_AXIS));
        about.setBackground(AppTheme.PANEL);
        about.setBorder(new EmptyBorder(22, 30, 22, 30));
        about.setMaximumSize(new Dimension(Integer.MAX_VALUE, 270));

        JLabel section = label("Acerca de", 14, Font.BOLD, AppTheme.TEXT);
        JLabel description = label(
                "Aplicación para explorar, analizar y exportar la información de lectura de Kobo.",
                14, Font.PLAIN, AppTheme.TEXT);
        JLabel origin = label(
                "Nace como respuesta a una necesidad personal como lectora: comprender mejor mis hábitos",
                13, Font.PLAIN, AppTheme.MUTED_TEXT);
        JLabel purpose = label(
                "y aprovechar los libros, palabras y subrayados que almacena el dispositivo.",
                13, Font.PLAIN, AppTheme.MUTED_TEXT);
        JLabel author = label(
                "Diseñada y desarrollada por Arabia como proyecto personal en Java.",
                13, Font.PLAIN, AppTheme.MUTED_TEXT);
        JLabel version = label("Versión 1.0 · En desarrollo",
                12, Font.PLAIN, AppTheme.MUTED_TEXT);
        JLabel technologies = label("Java 17 · Swing · SQLite · Gemini",
                12, Font.PLAIN, AppTheme.MUTED_TEXT);
        JLabel upcoming = label("Próximamente", 14, Font.BOLD, AppTheme.MUTED_TEXT);
        JLabel upcomingItems = label(
                "Histórico de lectura · Compatibilidad Kobo · Mejoras de IA · Informes",
                13, Font.PLAIN, AppTheme.TEXT);
        section.setAlignmentX(LEFT_ALIGNMENT);
        description.setAlignmentX(LEFT_ALIGNMENT);
        origin.setAlignmentX(LEFT_ALIGNMENT);
        purpose.setAlignmentX(LEFT_ALIGNMENT);
        author.setAlignmentX(LEFT_ALIGNMENT);
        version.setAlignmentX(LEFT_ALIGNMENT);
        technologies.setAlignmentX(LEFT_ALIGNMENT);
        upcoming.setAlignmentX(LEFT_ALIGNMENT);
        upcomingItems.setAlignmentX(LEFT_ALIGNMENT);

        about.add(section);
        about.add(Box.createVerticalStrut(12));
        about.add(description);
        about.add(Box.createVerticalStrut(7));
        about.add(origin);
        about.add(Box.createVerticalStrut(4));
        about.add(purpose);
        about.add(Box.createVerticalStrut(10));
        about.add(author);
        about.add(Box.createVerticalStrut(12));
        about.add(version);
        about.add(Box.createVerticalStrut(4));
        about.add(technologies);
        about.add(Box.createVerticalStrut(20));
        about.add(upcoming);
        about.add(Box.createVerticalStrut(10));
        about.add(upcomingItems);
        return about;
    }

    private void addRow(JPanel panel, int row, String title,
            JLabel description, java.awt.Component control) {
        GridBagConstraints left = constraints(0, row);
        left.weightx = 0.28;
        panel.add(label(title, 14, Font.BOLD, AppTheme.TEXT), left);

        GridBagConstraints center = constraints(1, row);
        center.weightx = 0.52;
        panel.add(description, center);

        GridBagConstraints right = constraints(2, row);
        right.weightx = 0.20;
        right.anchor = GridBagConstraints.EAST;
        panel.add(control, right);
    }

    private void addSeparator(JPanel panel, int row) {
        JPanel line = new JPanel();
        line.setBackground(AppTheme.BORDER);
        line.setPreferredSize(new Dimension(1, 1));
        GridBagConstraints constraints = constraints(0, row);
        constraints.gridwidth = 3;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(0, 0, 0, 0);
        panel.add(line, constraints);
    }

    private GridBagConstraints constraints(int column, int row) {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = column;
        constraints.gridy = row;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.insets = new Insets(18, 8, 18, 8);
        return constraints;
    }

    private void configureActions() {
        apiButton.addActionListener(event -> editApiKey());
        applyButton.addActionListener(event -> applyChanges());
    }

    private void editApiKey() {
        if (GeminiApiKeyStore.comesFromEnvironment()) {
            JOptionPane.showMessageDialog(this,
                    "La clave procede de GEMINI_API_KEY y debe modificarse desde Windows.",
                    "Gemini", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        boolean configured = !GeminiApiKeyStore.get().isBlank();
        JPasswordField field = new JPasswordField(30);
        String[] options = configured
                ? new String[]{"Guardar", "Eliminar", "Cancelar"}
                : new String[]{"Guardar", "Cancelar"};
        int result = JOptionPane.showOptionDialog(this, field,
                configured ? "Modificar API key" : "Introducir API key",
                JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE,
                null, options, options[0]);

        if (result == 0) {
            String key = new String(field.getPassword()).strip();
            if (key.isBlank()) {
                JOptionPane.showMessageDialog(this, "Introduce una clave API.",
                        "Clave vacía", JOptionPane.WARNING_MESSAGE);
                return;
            }
            GeminiApiKeyStore.save(key);
        } else if (configured && result == 1) {
            GeminiApiKeyStore.remove();
        }
        refreshState();
    }

    private void applyChanges() {
        AppPreferences.setLanguage((String) language.getSelectedItem());
        String selectedTheme = (String) theme.getSelectedItem();
        AppPreferences.setTheme("Light".equals(selectedTheme)
                ? "Claro" : "Dark".equals(selectedTheme)
                        ? "Oscuro" : selectedTheme);
        applyAction.run();
    }

    public void refreshState() {
        boolean configured = !GeminiApiKeyStore.get().isBlank();
        apiStatus.setText(I18n.text(configured
                ? "✓ API key guardada" : "No hay API key"));
        apiStatus.setForeground(configured ? AppTheme.GREEN : AppTheme.MUTED_TEXT);
        apiButton.setText(I18n.text(configured ? "Modificar" : "Introducir"));
        language.setSelectedItem(AppPreferences.language());
        String storedTheme = AppPreferences.theme();
        theme.setSelectedItem(AppPreferences.isEnglish()
                ? ("Claro".equals(storedTheme) ? "Light" : "Dark")
                : storedTheme);
    }

    private JLabel valueLabel() {
        return valueLabel("");
    }

    private JLabel valueLabel(String text) {
        return label(text, 13, Font.PLAIN, AppTheme.MUTED_TEXT);
    }

    private JLabel label(String text, int size, int style, Color color) {
        JLabel label = new JLabel(text);
        label.setFont(AppTheme.font(style, size));
        label.setForeground(color);
        return label;
    }

    private JButton button(String text, Color color) {
        JButton button = new RoundedButton(text);
        button.putClientProperty("JButton.buttonType", "roundRect");
        button.putClientProperty("JComponent.roundRect", true);
        button.setFont(AppTheme.font(Font.BOLD, 12));
        button.setForeground(Color.WHITE);
        button.setBackground(color);
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(9, 14, 9, 14));
        return button;
    }
}
