
package com.arcac.managerkobo.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Clase que se encarga de rastrear las unidades, encontrar la carpeta kobo, y
 * copiar el archivo .sqlite al PC.
 */
public class KoboDetector {

    /**
     * Detecta el kobo en el pc y extrae el sqlite.
     */
    public static String detectAndCopyDatabase() {
        // Carpeta local donde guardaremos los datos 
        File appDataDir = new File("data");
        if (!appDataDir.exists()) {
            appDataDir.mkdir(); 
        }
        File localDbFile = new File(appDataDir, "KoboReader.sqlite");

        File[] roots = File.listRoots();
        boolean koboFound = false;

        for (File root : roots) {
            File koboFolder = new File(root, ".kobo");
            File sqliteFile = new File(koboFolder, "KoboReader.sqlite");

            if (koboFolder.exists() && koboFolder.isDirectory() && sqliteFile.exists()) {
                koboFound = true;
                System.out.println("¡Kobo detectado en la unidad: " + root.getAbsolutePath() + "!");

                try {
                    // Si ya existe una base de datos local de una conexión anterior
                    if (localDbFile.exists()) {

                        // Comparamos tamaño o fecha para ver si el usuario ha leído o subrayado algo nuevo
                        if (sqliteFile.lastModified() != localDbFile.lastModified() || sqliteFile.length() != localDbFile.length()) {

                            // ** En el futuro pondremos un JOptionPane pidiendo confirmación al usuario **
                            System.out.println("Se han detectado cambios en el Kobo.");

                            // 1. Hagcemos un backup 
                            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                            File backupFile = new File(appDataDir, "KoboReader_backup_" + timestamp + ".sqlite");
                            localDbFile.renameTo(backupFile);
                            System.out.println("Guardada copia de seguridad antigua en: " + backupFile.getName());

                            // 2. Copiamos la nueva
                            Files.copy(sqliteFile.toPath(), localDbFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                            localDbFile.setLastModified(sqliteFile.lastModified()); // Sincronizar fechas

                        } else {
                            System.out.println("La base de datos local ya está totalmente sincronizada con el Kobo.");
                        }
                    } else {
                        // Es la primera vez que se ejecuta el programa
                        Files.copy(sqliteFile.toPath(), localDbFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                        localDbFile.setLastModified(sqliteFile.lastModified());
                        System.out.println("Base de datos importada por primera vez.");
                    }

                    return localDbFile.getAbsolutePath();

                } catch (IOException e) {
                    System.err.println("Error al copiar la base de datos: " + e.getMessage());
                    return null;
                }
            }
        }

        // Si no hemos encontrado el Kobo enchufado, comprobamos si tenemos datos locales 
        if (!koboFound) {
            System.out.println("No se ha detectado el Kobo por USB...");
            if (localDbFile.exists()) {
                System.out.println("...usamos los datos guardados en la carpeta local.");
                return localDbFile.getAbsolutePath();
            } else {
                System.out.println("...no hay datos locales. Conecta el Kobo para empezar.");
                return null;
            }
        }

        return null;
    }
}
