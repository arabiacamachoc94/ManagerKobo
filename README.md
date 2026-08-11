# Kobo Manager

Aplicación de escritorio en Java para explorar, analizar y exportar la información de lectura almacenada por un eReader Kobo.

> **Estado:** beta en desarrollo. Actualmente trabaja con un único Kobo Clara Colour y ha sido probada principalmente en Windows.

## Motivación

La motivación inicial del proyecto fue poder extraer los subrayados del Kobo para conservarlos, consultarlos y manipularlos con más facilidad fuera del dispositivo.

A partir de esa necesidad, el proyecto creció hacia el análisis de datos de lectura: biblioteca, progreso, tiempo leído, palabras consultadas y patrones personales. Esta parte resulta especialmente útil para aprender y aplicar tratamiento, visualización y exportación de datos reales.

Kobo Manager detecta el dispositivo conectado, crea una copia local de `KoboReader.sqlite` y trabaja sobre ella. La base de datos original del Kobo no se modifica.

## Funcionalidades principales

- Detección y sincronización del Kobo conectado por USB.
- Uso de la última copia local cuando el dispositivo está desconectado.
- Biblioteca visual con portadas, búsqueda y filtros combinables.
- Detalle de libros con progreso, tiempo, ritmo y subrayados.
- Subrayados agrupados por libro, selección múltiple y copia.
- Exportación de subrayados en TXT y PDF.
- Palabras consultadas en el diccionario y exportación en TXT.
- Resumen con métricas, lecturas activas y gráficas estadísticas.
- Exportación del resumen como JPEG e informe PDF.
- Funciones opcionales con Gemini: resumen, ideas clave, preguntas y análisis del informe.
- Tema oscuro y claro, interfaz en español e inglés e iconos SVG HiDPI.
- 43 pruebas automatizadas y ejecución continua mediante GitHub Actions.

Algunas métricas son estimaciones y dependen de la información que cada modelo y firmware de Kobo guarde en SQLite.

## Uso básico

1. Conecta el Kobo por USB y permite el acceso desde el dispositivo.
2. Abre Kobo Manager.
3. La aplicación copiará `.kobo/KoboReader.sqlite` dentro del directorio local `data/`.
4. Utiliza el botón de sincronización para actualizar los datos.
5. Sin el Kobo conectado se utilizará la última copia local disponible.

## Inteligencia artificial y privacidad

Gemini es opcional y requiere que cada usuario introduzca su propia API key desde Ajustes. Cuando se utiliza, solo se envían los subrayados seleccionados o las estadísticas agregadas necesarias, no la SQLite completa.

La base de datos de Kobo puede contener información personal. Por ello, `data/`, `*.sqlite`, las claves y los archivos generados localmente no deben publicarse. Estos elementos están excluidos mediante `.gitignore`.

## Desarrollo

Requisitos:

- JDK 17
- Maven
- Un Kobo o una copia compatible de `KoboReader.sqlite`

Ejecutar la aplicación:

```bash
mvn exec:java
```

Ejecutar las pruebas:

```bash
mvn test
```

Generar la versión portable de Windows:

```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\build-portable.ps1
```

El resultado se crea en `dist/KoboManager/` y debe compartirse como una carpeta completa.

## Tecnologías

Java 17 · Swing · FlatLaf · JDBC · SQLite · PDFBox · Gemini API · Maven · JUnit 5 · GitHub Actions · jpackage

## Estructura

```text
src/main/java/com/arcac/managerkobo/
├── ai/          Integración opcional con Gemini
├── app/         Inicio de la aplicación
├── database/    Conexión y consultas SQLite
├── model/       Libros, subrayados y palabras
├── service/     Carga, estadísticas, portadas y exportaciones
├── ui/          Ventana, pantallas y componentes Swing
└── util/        Detección, sincronización y formatos
```


## Limitaciones y próximas mejoras

- Perfiles separados para utilizar varios Kobo.
- Mayor compatibilidad entre modelos y versiones de firmware.
- Histórico de lectura entre sincronizaciones.
- Más opciones de IA, informes y exportaciones para análisis con Power BI.
- Mejor registro y diagnóstico de errores en versiones distribuidas.

Kobo Manager es un proyecto personal e independiente. No está afiliado, patrocinado ni respaldado por Rakuten Kobo.
