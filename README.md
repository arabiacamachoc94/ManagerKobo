# Kobo Manager

## Descripción

Kobo Manager es una aplicación de escritorio desarrollada en Java para extraer, explorar y analizar la información almacenada por dispositivos Kobo eReader.

La aplicación detecta el dispositivo conectado, crea una copia local de su base de datos SQLite y presenta de forma visual la biblioteca y las estadísticas personales de lectura. El objetivo final es facilitar la consulta, organización y exportación de libros, notas, subrayados y otros datos registrados por Kobo.

## Estado del proyecto

🚧 **En desarrollo**

Actualmente, el proyecto dispone de una primera interfaz funcional conectada a datos reales de Kobo. La arquitectura se encuentra dividida en modelos, acceso a datos, servicios estadísticos y componentes de interfaz.

## Funcionalidades implementadas

- Detección automática de dispositivos Kobo conectados por USB.
- Copia local y respaldo de `KoboReader.sqlite`.
- Sincronización manual desde la interfaz sin bloquear la aplicación.
- Diferenciación entre un Kobo conectado y el uso de datos locales.
- Consulta de libros y subrayados mediante JDBC.
- Dashboard con estadísticas reales de lectura.
- Visualización de libros totales, terminados, tiempo leído y subrayados.
- Información sobre la lectura actual, el último libro leído y el libro con mayor tiempo de lectura.
- Biblioteca con tabla y búsqueda por título o autor.
- Interfaz gráfica oscura desarrollada con Java Swing y FlatLaf.
- Navegación entre secciones mediante `CardLayout`.
- Capa reutilizable para calcular estadísticas de lectura.

## Funcionalidades previstas

- Consulta, búsqueda y filtrado de subrayados y notas.
- Edición y organización de notas personales.
- Exportación de libros y subrayados a CSV, Markdown y otros formatos.
- Diccionario personal con las palabras consultadas en el Kobo.
- Análisis de capítulos y distribución de subrayados.
- Estadísticas y patrones personales de lectura más avanzados.
- Visualización de logros y actividad reciente.
- Integración con herramientas de análisis como Power BI.
- Compatibilidad adaptativa con diferentes versiones de la base de datos de Kobo.

## Tecnologías utilizadas

- Java 17
- Java Swing
- FlatLaf
- JDBC
- SQLite
- Maven
- Git y GitHub
- Power BI (integración prevista)

## Estructura principal

```text
src/main/java/com/arcac/managerkobo/
├── app/        Punto de entrada de la aplicación
├── database/   Conexión SQLite y consultas DAO
├── model/      Modelos de libros y subrayados
├── service/    Cálculo de estadísticas de lectura
├── ui/         Ventana, paneles y componentes Swing
└── util/       Detección y sincronización del Kobo
```

## Privacidad de los datos

La base de datos de Kobo puede contener información personal y credenciales de la cuenta. Por este motivo, los archivos SQLite y el directorio local `data/` están excluidos del repositorio mediante `.gitignore`.

No deben publicarse archivos como:

```text
KoboReader.sqlite
KoboReader.sqlite-wal
KoboReader.sqlite-shm
```
