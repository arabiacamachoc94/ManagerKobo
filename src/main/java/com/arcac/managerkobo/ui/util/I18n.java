package com.arcac.managerkobo.ui.util;

import java.awt.Component;
import java.awt.Container;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.AbstractButton;
import javax.swing.JLabel;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.text.JTextComponent;

/** Traduce de forma central los controles visibles de la interfaz. */
public final class I18n {
    private static final Map<String, String> TEXTS = new LinkedHashMap<>();

    static {
        add("Ajustes", "Settings"); add("Preferencias de Kobo Manager", "Kobo Manager preferences");
        add("Acerca de", "About");
        add("Aplicación para explorar, analizar y exportar la información de lectura de Kobo.",
                "An application for exploring, analyzing and exporting Kobo reading data.");
        add("Nace como respuesta a una necesidad personal como lectora: comprender mejor mis hábitos",
                "It began as a response to a personal need as a reader: to better understand my habits");
        add("y aprovechar los libros, palabras y subrayados que almacena el dispositivo.",
                "and make better use of the books, words and highlights stored on the device.");
        add("Diseñada y desarrollada por Arabia como proyecto personal en Java.",
                "Designed and developed by Arabia as a personal Java project.");
        add("Versión 1.0 · En desarrollo", "Version 1.0 · In development");
        add("Próximamente", "Coming next");
        add("Histórico de lectura · Compatibilidad Kobo · Mejoras de IA · Informes",
                "Reading history · Kobo compatibility · AI improvements · Reports");
        add("API de Gemini", "Gemini API"); add("No hay API key", "No API key configured");
        add("Introducir", "Add key"); add("Modificar", "Change");
        add("Idioma", "Language"); add("Tema", "Theme");
        add("Mi Biblioteca", "My Library"); add("Biblioteca", "Library");
        add("Resumen", "Overview");
        add("Subrayados", "Highlights"); add("Palabras", "Words");
        add("palabra", "word"); add("palabras", "words");
        add("No se encontraron palabras.", "No words were found.");
        add("Estadísticas", "Statistics");
        add("Explora los patrones encontrados en tu biblioteca", "Explore the patterns found in your library");
        add("Explora tus estadísticas", "Explore your statistics");
        add("Tu biblioteca de un vistazo", "Your library at a glance");
        add("Terminados este año", "Finished this year");
        add("Ritmo mensual", "Monthly pace");
        add("Ritmo de lectura", "Reading pace");
        add("Ritmo de lectura (ppm)", "Reading pace (wpm)");
        add("ppm", "wpm");
        add("Proyección anual", "Annual projection");
        add("autor con más tiempo", "author with the most reading time");
        add("libro más subrayado", "most-highlighted book");
        add("media por libro", "average per book");
        add("lectura activa", "current read");
        add("lecturas activas", "current reads");
        add("libro que has leído más rápido", "book you read the fastest");
        add("libro leído más lento", "slowest-read book");
        add("Aún no hay datos suficientes para calcular el libro leído más rápido.",
                "There is not enough data yet to calculate the fastest-read book.");
        add("Aún no hay datos suficientes para calcular el libro leído más lento.",
                "There is not enough data yet to calculate the slowest-read book.");
        add("Aún no hay datos suficientes para comparar tu ritmo de lectura.",
                "There is not enough data yet to compare your reading pace.");
        add("ritmo de lectura", "reading pace");
        add("ritmo de lectura estimado es lento", "estimated reading pace is slow");
        add("ritmo de lectura estimado es normal", "estimated reading pace is typical");
        add("ritmo de lectura estimado es rápido", "estimated reading pace is fast");
        add("Estimación basada en los libros terminados que tienen fecha registrada.",
                "Estimate based on finished books with a recorded date.");
        add("Lecturas en curso", "Current reads"); add("Lectura actual", "Current read");
        add("Lecturas anteriores", "Previous reads"); add("Lecturas siguientes", "Next reads");
        add("No tienes otras lecturas en curso", "You have no other current reads");
        add("No hay libros en progreso", "No books in progress");
        add("Empieza un libro y sincroniza el Kobo para verlo aquí.", "Start a book and sync your Kobo to see it here.");
        add("Abrir libro", "Open book"); add("Detalle del libro", "Book details");
        add("Volver", "Back"); add("Sin título", "Untitled");
        add("Autor desconocido", "Unknown author"); add("Editorial desconocida", "Unknown publisher");
        add("Idioma desconocido", "Unknown language"); add("Libro desconocido", "Unknown book");
        add("Desconocido", "Unknown"); add("Sin fecha", "No date");
        add("Español", "Spanish"); add("Inglés", "English");
        add("Francés", "French"); add("Alemán", "German");
        add("Italiano", "Italian"); add("Portugués", "Portuguese");
        add("Libro", "Book"); add("Diccionario", "Dictionary"); add("Fecha", "Date");
        add("Estado de la biblioteca", "Library status");
        add("Progreso de libros en curso", "Current book progress");
        add("Progreso de lecturas activas", "Current reading progress");
        add("Libros finalizados por mes (estimado)", "Books finished per month (estimated)");
        add("Tiempo de lectura por libro", "Reading time by book");
        add("Velocidad de lectura por libro", "Reading speed by book");
        add("Tiempo de lectura por autor", "Reading time by author");
        add("Subrayados por libro", "Highlights by book"); add("Subrayados por autor", "Highlights by author");
        add("Subrayados por mes", "Highlights by month"); add("Notas por mes", "Notes by month");
        add("Palabras consultadas por mes", "Looked-up words per month");
        add("Libros por idioma", "Books by language");
        add("Densidad de subrayados por libro", "Highlight density by book");
        add("Mostrar", "Show"); add("Finalización", "Completion");
        add("Horas leídas", "Hours read"); add("Tiempo leído", "Reading time");
        add("Tiempo restante estimado", "Estimated time remaining");
        add("Última lectura", "Last read"); add("Último inicio registrado", "Last recorded start");
        add("Primer subrayado", "First highlight"); add("Último subrayado", "Last highlight");
        add("Subrayados por hora", "Highlights per hour");
        add("Velocidad de lectura", "Reading speed");
        add("Notas", "Notes"); add("% leído", "% read");
        add("Menos del 25%", "Under 25%"); add("75% o más", "75% or more");
        add("Formato de exportación", "Export format");
        add("Exportar subrayados", "Export highlights");
        add("Confirmar exportación", "Confirm export");
        add("Exportación completada", "Export completed");
        add("Error de exportación", "Export error");
        add("Selecciona el formato del archivo:", "Select the file format:");
        add("Resumir subrayados con Gemini", "Summarize highlights with Gemini");
        add("Resumen con Gemini", "Gemini summary");
        add("Resumir", "Summarize"); add("Ideas clave", "Key ideas");
        add("Preguntar...", "Ask..."); add("✨ Acciones IA ▾", "✨ AI actions ▾");
        add("Aplicar una acción inteligente a la selección", "Apply an AI action to the selection");
        add("Resumir subrayados", "Summarize highlights");
        add("Extraer ideas clave", "Extract key ideas");
        add("Preguntar sobre los subrayados", "Ask about the highlights");
        add("Consultando a Gemini...", "Asking Gemini...");
        add("Respuesta generada correctamente.", "Response generated successfully.");
        add("Preguntar a Gemini", "Ask Gemini");
        add("¿Qué quieres preguntar sobre los subrayados seleccionados?",
                "What would you like to ask about the selected highlights?");
        add("Cerrar", "Close"); add("Guardar", "Save"); add("Eliminar", "Delete");
        add("La respuesta aparecerá aquí.", "The response will appear here.");
        add("Preparado para generar.", "Ready to generate.");
        add("Configura tu clave API para comenzar.", "Configure your API key to begin.");
        add("Generando resumen...", "Generating summary...");
        add("Resumen generado correctamente.", "Summary generated successfully.");
        add("La petición ha fallado.", "The request failed.");
        add("No se pudo generar la respuesta.", "The response could not be generated.");
        add("Se enviarán ", "The selected ");
        add(" subrayados seleccionados a la API de Gemini.",
                " highlights will be sent to the Gemini API.");
        add("Preparado: clave cargada desde GEMINI_API_KEY.",
                "Ready: key loaded from GEMINI_API_KEY.");
        add("Preparado: clave guardada en la aplicación.",
                "Ready: key saved in the application.");
        add("Respuesta copiada al portapapeles.", "Response copied to the clipboard.");
        add("Sin datos", "No data"); add("Sin empezar", "Not started");
        add("Leyendo", "Reading"); add("Terminado", "Finished"); add("Terminados", "Finished");
        add("Título", "Title"); add("Autor", "Author"); add("Progreso", "Progress");
        add("Estado", "Status"); add("Tiempo", "Time"); add("Palabra", "Word");
        add("Filtrar ▾", "Filter ▾"); add("Seleccionar", "Select");
        add("Filtrar", "Filter");
        add("Cancelar", "Cancel"); add("Limpiar", "Clear"); add("Copiar", "Copy");
        add("Seleccionar todos", "Select all");
        add("Seleccionar todos los subrayados", "Select all highlights");
        add("Seleccionar todos los subrayados de este libro",
                "Select all highlights from this book");
        add("Texto", "Text"); add("Copiar subrayados", "Copy highlights");
        add(" subrayados copiados al portapapeles.", " highlights copied to the clipboard.");
        add(" subrayados exportados en ", " highlights exported as ");
        add(" correctamente.", " successfully.");
        add("Exportar", "Export"); add("Aplicar filtros", "Apply filters");
        add("Desmarcar todos los subrayados", "Clear all selected highlights");
        add("Exportar los subrayados seleccionados", "Export selected highlights");
        add("Exportar imagen", "Export image");
        add("Exportar TXT", "Export TXT");
        add("Exportar palabras", "Export words");
        add("Exportar resumen", "Export overview");
        add("Exportar una lista simple de palabras", "Export a simple word list");
        add("Archivo de texto", "Text file");
        add("Documento PDF", "PDF document");
        add("Generar informe PDF", "Generate PDF report");
        add("No hay datos para exportar.", "There is no data to export.");
        add("Exportación no disponible", "Export unavailable");
        add("No hay una base de datos con información de lectura para exportar.",
                "There is no database with reading data to export.");
        add("El archivo ya existe. ¿Quieres reemplazarlo?",
                "The file already exists. Do you want to replace it?");
        add("Resumen exportado correctamente en:", "Overview exported successfully to:");
        add("Informe PDF generado correctamente en:", "PDF report generated successfully to:");
        add("Palabras exportadas correctamente en:", "Words exported successfully to:");
        add("No se pudo exportar el resumen: ", "The overview could not be exported: ");
        add("No se pudo generar el informe: ", "The report could not be generated: ");
        add("No se pudo crear el archivo: ", "The file could not be created: ");
        add("El PDF se ha guardado, pero sin el análisis de IA.",
                "The PDF was saved, but without the AI analysis.");
        add("Guardado en:", "Saved to:");
        add("Informe generado sin IA", "Report generated without AI");
        add("Informe PDF", "PDF report");
        add("Exportando...", "Exporting...");
        add("Generando...", "Generating...");
        add("Generando PDF...", "Generating PDF...");
        add("✓ API key guardada", "✓ API key saved");
        add("Incluir análisis final generado con Gemini",
                "Include a final analysis generated with Gemini");
        add("El informe incluirá estadísticas agregadas de lectura.",
                "The report will include aggregated reading statistics.");
        add("Configura tu API key en Ajustes para activar el análisis.",
                "Configure your API key in Settings to enable the analysis.");
        add("Opciones del informe PDF", "PDF report options");
        add("Imagen JPEG", "JPEG image");
        add("Limpiar filtros", "Clear filters"); add("Con subrayados", "With highlights");
        add("No se encontraron subrayados.", "No highlights found.");
        add("Selecciona uno o varios subrayados para usar Gemini.",
                "Select one or more highlights to use Gemini.");
        add("Escribe una pregunta antes de continuar.",
                "Enter a question before continuing.");
        add("Selecciona uno o varios subrayados para copiarlos.",
                "Select one or more highlights to copy them.");
        add("Selecciona uno o varios subrayados para exportarlos.",
                "Select one or more highlights to export them.");
        add("Cargando...", "Loading...");
        add("Buscar título o autor...", "Search by title or author...");
        add("Buscar libro, autor o texto subrayado...", "Search book, author or highlighted text...");
        add("Buscar en los subrayados...", "Search highlights...");
        add("Buscar palabra, libro o autor...", "Search word, book or author...");
        add("No hay datos suficientes para esta gráfica.", "Not enough data for this chart.");
        add("No hay información de idiomas", "No language information");
        add("Sincronización", "Synchronization");
        add("Error de sincronización", "Synchronization error");
        add("La clave procede de GEMINI_API_KEY y debe modificarse desde Windows.",
                "The key comes from GEMINI_API_KEY and must be changed in Windows.");
        add("Modificar API key", "Change API key");
        add("Introducir API key", "Enter API key");
        add("Introduce una clave API.", "Enter an API key.");
        add("Clave vacía", "Empty key");
        add("No se pudo sincronizar la base de datos: ",
                "The database could not be synchronized: ");
        add("subrayados", "highlights"); add("notas", "notes");
        add("palabras", "words"); add("libros", "books");
        add("Sin datos de lectura por autor", "No reading data by author");
        add("Sin subrayados asociados a autores", "No highlights linked to authors");
        add("No hay subrayados asociados a libros", "No highlights linked to books");
    }

    private I18n() { }
    private static void add(String spanish, String english) { TEXTS.put(spanish, english); }

    public static String text(String value) {
        if (!AppPreferences.isEnglish() || value == null || value.isBlank()) return value;
        String exact = TEXTS.get(value);
        if (exact != null) return exact;
        return value
                .replace("Ene ", "Jan ").replace("Abr ", "Apr ")
                .replace("Ago ", "Aug ").replace("Sept ", "Sep ")
                .replace("Dic ", "Dec ")
                .replace("Última sincronización: todavía no disponible", "Last sync: not available yet")
                .replace("Última sincronización: ", "Last sync: ")
                .replace(" No se pudieron cargar subrayados ni palabras consultadas porque esta base de datos no contiene la estructura esperada.",
                        " Highlights and looked-up words could not be loaded because this database has a different structure.")
                .replace(" No se pudieron cargar subrayados porque esta base de datos no contiene la estructura esperada.",
                        " Highlights could not be loaded because this database has a different structure.")
                .replace(" No se pudieron cargar palabras consultadas porque esta base de datos no contiene la estructura esperada.",
                        " Looked-up words could not be loaded because this database has a different structure.")
                .replace("Tiempo leído: ", "Reading time: ")
                .replace("Autor con más tiempo de lectura: ", "Author with most reading time: ")
                .replace("Autor más subrayado: ", "Most-highlighted author: ")
                .replace("Libro más subrayado: ", "Most-highlighted book: ")
                .replace("Media por libro empezado: ", "Average per started book: ")
                .replace("Media por libro terminado: ", "Average per finished book: ")
                .replace("Progreso medio de las lecturas actuales: ", "Average current reading progress: ")
                .replace("% leído", "% read")
                .replace("Editorial desconocida", "Unknown publisher")
                .replace("Autor desconocido", "Unknown author")
                .replace("Idioma desconocido", "Unknown language")
                .replace(" · Terminado", " · Finished")
                .replace(" · Leyendo", " · Reading")
                .replace(" · Sin empezar", " · Not started")
                .replace("El autor con más tiempo de lectura es ", "The author with the most reading time is ")
                .replace("El libro más subrayado es ", "The most-highlighted book is ")
                .replace("El libro que has leído más rápido es ",
                        "The book you read the fastest is ")
                .replace("El libro leído más lento es ",
                        "The slowest-read book is ")
                .replace("Tu ritmo de lectura estimado es lento frente al rango adulto de referencia.",
                        "Your estimated reading pace is slow compared with the adult reference range.")
                .replace("Tu ritmo de lectura estimado es normal frente al rango adulto de referencia.",
                        "Your estimated reading pace is typical compared with the adult reference range.")
                .replace("Tu ritmo de lectura estimado es rápido frente al rango adulto de referencia.",
                        "Your estimated reading pace is fast compared with the adult reference range.")
                .replace(" palabras por minuto.", " words per minute.")
                .replace("La media por libro terminado es de ", "The average per finished book is ")
                .replace("Aún no hay datos sobre el tiempo de lectura por autor.",
                        "There is no reading-time data by author yet.")
                .replace("Todavía no hay un libro más subrayado.",
                        "There is no most-highlighted book yet.")
                .replace("Tienes ", "You have ")
                .replace(" fragmentos guardados en tu Kobo", " saved excerpts on your Kobo")
                .replace(" palabras consultadas en ", " words looked up in ")
                .replace(" libros encontrados · Pulsa una portada para abrir el libro",
                        " books found · Click a cover to open the book")
                .replace(" lecturas activas en este momento.", " current reads.")
                .replace(" lectura activa en este momento.", " current read.")
                .replace(" seleccionados", " selected")
                .replace(" terminados", " finished")
                .replace(" en progreso", " in progress")
                .replace(" libros", " books").replace(" libro", " book")
                .replace(" subrayados", " highlights");
    }

    public static void translateTree(Component component) {
        if (!AppPreferences.isEnglish() || component == null) return;
        if (component instanceof JComponent swingComponent) {
            swingComponent.setToolTipText(text(swingComponent.getToolTipText()));
        }
        if (component instanceof JLabel label) {
            label.setText(text(label.getText()));
        } else if (component instanceof AbstractButton button) {
            button.setText(text(button.getText()));
        } else if (component instanceof JTextComponent field) {
            Object placeholder = field.getClientProperty("JTextField.placeholderText");
            if (placeholder instanceof String string) {
                field.putClientProperty("JTextField.placeholderText", text(string));
            }
        } else if (component instanceof JTable table) {
            for (int index = 0; index < table.getColumnCount(); index++) {
                Object header = table.getColumnModel().getColumn(index).getHeaderValue();
                if (header instanceof String string) {
                    table.getColumnModel().getColumn(index).setHeaderValue(text(string));
                }
            }
        }
        if (component instanceof Container container) {
            for (Component child : container.getComponents()) translateTree(child);
        }
    }
}
