package com.arcac.managerkobo.ui.dialogs;

import com.arcac.managerkobo.ai.HighlightAiService;
import com.arcac.managerkobo.ai.HighlightAiService.Operation;
import com.arcac.managerkobo.ai.GeminiApiKeyStore;
import com.arcac.managerkobo.model.Bookmark;
import com.arcac.managerkobo.ui.theme.AppTheme;
import com.arcac.managerkobo.ui.util.I18n;
import com.arcac.managerkobo.ui.util.UiStyles;
import com.arcac.managerkobo.ui.components.RoundedButton;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.datatransfer.StringSelection;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;

/** Diálogo de prueba para resumir subrayados mediante Gemini. */
public class AiSummaryDialog extends JDialog {
    private final List<Bookmark> highlights;
    private final Operation operation;
    private final String question;
    private final HighlightAiService aiService = new HighlightAiService();
    private final JTextArea response = new JTextArea();
    private final JLabel status = new JLabel();
    private final JButton generate = new RoundedButton("");
    private final JProgressBar progress = new JProgressBar();

    public AiSummaryDialog(Window owner, List<Bookmark> highlights,
            Operation operation, String question) {
        super(owner, I18n.text(dialogTitle(operation)), ModalityType.APPLICATION_MODAL);
        this.highlights = List.copyOf(highlights);
        this.operation = operation;
        this.question = question;
        generate.setText(I18n.text(actionText(operation)));
        configureWindow();
        setContentPane(createContent());
        I18n.translateTree(getContentPane());
    }

    private void configureWindow() {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(720, 560);
        setMinimumSize(new Dimension(560, 440));
        setLocationRelativeTo(getOwner());
    }

    private JPanel createContent() {
        JPanel content = new JPanel(new BorderLayout(0, 14));
        content.setBackground(AppTheme.BACKGROUND);
        content.setBorder(new EmptyBorder(20, 22, 18, 22));

        JPanel header = new JPanel(new BorderLayout(0, 7));
        header.setOpaque(false);
        JLabel title = label(dialogTitle(operation),
                20, Font.BOLD, AppTheme.TEXT);
        header.add(title, BorderLayout.NORTH);
        header.add(label(I18n.text("Se enviarán ") + highlights.size()
                        + I18n.text(" subrayados seleccionados a la API de Gemini."),
                12, Font.PLAIN, AppTheme.MUTED_TEXT), BorderLayout.CENTER);
        status.setFont(AppTheme.font(Font.PLAIN, 12));
        status.setForeground(AppTheme.MUTED_TEXT);
        updateConfigurationState();
        JPanel state = new JPanel();
        state.setOpaque(false);
        state.setLayout(new javax.swing.BoxLayout(
                state, javax.swing.BoxLayout.Y_AXIS));
        status.setAlignmentX(LEFT_ALIGNMENT);
        state.add(status);
        state.add(javax.swing.Box.createVerticalStrut(5));
        progress.setIndeterminate(true);
        progress.setVisible(false);
        progress.setBorder(null);
        progress.setMaximumSize(new Dimension(Integer.MAX_VALUE, 4));
        progress.setPreferredSize(new Dimension(0, 4));
        progress.setAlignmentX(LEFT_ALIGNMENT);
        state.add(progress);
        header.add(state, BorderLayout.SOUTH);
        content.add(header, BorderLayout.NORTH);

        response.setEditable(false);
        response.setLineWrap(true);
        response.setWrapStyleWord(true);
        response.setFont(AppTheme.font(Font.PLAIN, 14));
        response.setBackground(AppTheme.PANEL);
        response.setForeground(AppTheme.TEXT);
        response.setBorder(new EmptyBorder(14, 14, 14, 14));
        response.setText("La respuesta aparecerá aquí.");
        JScrollPane scroll = new JScrollPane(response);
        scroll.setBorder(null);
        content.add(scroll, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        JButton copy = button("Copiar", AppTheme.PANEL_ALT);
        copy.addActionListener(event -> copyResponse());
        JButton close = button("Cerrar", AppTheme.PANEL_ALT);
        close.addActionListener(event -> dispose());
        styleButton(generate, AppTheme.PURPLE);
        generate.addActionListener(event -> generateResponse());
        actions.add(copy);
        actions.add(close);
        actions.add(generate);
        content.add(actions, BorderLayout.SOUTH);
        return content;
    }

    private void updateConfigurationState() {
        boolean configured = aiService.isConfigured();
        generate.setEnabled(configured);
        if (!configured) {
            status.setText(I18n.text("Configura tu clave API para comenzar."));
        } else if (GeminiApiKeyStore.comesFromEnvironment()) {
            status.setText(I18n.text("Preparado: clave cargada desde GEMINI_API_KEY."));
        } else {
            status.setText(I18n.text("Preparado: clave guardada en la aplicación."));
        }
    }

    private void generateResponse() {
        generate.setEnabled(false);
        generate.setText(I18n.text("Generando..."));
        status.setText(I18n.text("Consultando a Gemini..."));
        progress.setVisible(true);
        setCursor(java.awt.Cursor.getPredefinedCursor(
                java.awt.Cursor.WAIT_CURSOR));
        response.setText("");
        new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() {
                return aiService.execute(operation, highlights, question);
            }

            @Override
            protected void done() {
                try {
                    response.setText(get());
                    response.setCaretPosition(0);
                    status.setText(I18n.text("Respuesta generada correctamente."));
                } catch (Exception exception) {
                    response.setText(I18n.text("No se pudo generar la respuesta.") + "\n\n"
                            + I18n.text(rootMessage(exception)));
                    status.setText(I18n.text("La petición ha fallado."));
                } finally {
                    progress.setVisible(false);
                    setCursor(java.awt.Cursor.getDefaultCursor());
                    generate.setText(I18n.text(actionText(operation)));
                    generate.setEnabled(aiService.isConfigured());
                }
            }
        }.execute();
    }

    private static String dialogTitle(Operation operation) {
        return switch (operation) {
            case SUMMARY -> "Resumir subrayados";
            case KEY_IDEAS -> "Extraer ideas clave";
            case QUESTION -> "Preguntar sobre los subrayados";
        };
    }

    private static String actionText(Operation operation) {
        return switch (operation) {
            case SUMMARY -> "Generar resumen";
            case KEY_IDEAS -> "Extraer ideas";
            case QUESTION -> "Responder";
        };
    }

    private void copyResponse() {
        String text = response.getText();
        if (text == null || text.isBlank()) return;
        Toolkit.getDefaultToolkit().getSystemClipboard()
                .setContents(new StringSelection(text), null);
        status.setText(I18n.text("Respuesta copiada al portapapeles."));
    }

    private JButton button(String text, Color color) {
        JButton button = new RoundedButton(text);
        styleButton(button, color);
        return button;
    }

    private void styleButton(JButton button, Color color) {
        if (color.equals(AppTheme.PANEL_ALT)) {
            UiStyles.secondaryButton(button);
        } else {
            UiStyles.actionButton(button, color);
        }
    }

    private JLabel label(String text, int size, int style, Color color) {
        JLabel label = new JLabel(text);
        label.setFont(AppTheme.font(style, size));
        label.setForeground(color);
        return label;
    }

    private String rootMessage(Throwable throwable) {
        Throwable current = throwable;
        while (current.getCause() != null) current = current.getCause();
        return current.getMessage() == null
                ? current.getClass().getSimpleName() : current.getMessage();
    }
}
