# Recordatorios — Actividad 1 (Programación Mobile)

App Android (Kotlin + Jetpack Compose + Material 3) que modela recordatorios con data classes
y sealed classes, valida datos simulados y los muestra en una lista filtrable.

## Estructura
- `data/Modelos.kt` — data class `Recordatorio` y sealed classes (categoría, prioridad, estado, resultados).
- `data/RepositorioSimulado.kt` — datos simulados con List, Set y Map; filtros por estado/prioridad.
- `domain/ValidadorRecordatorio.kt` — reglas de validación.
- `ui/RecordatoriosScreen.kt`, `ui/theme/Theme.kt` — pantalla Compose y tema Material 3.

## Cómo ejecutarlo
1. Android Studio → **File › Open** → seleccionar esta carpeta.
2. Esperar la sincronización de Gradle (AGP 9.0.1, Gradle 9.1, compileSdk 36).
3. Elegir un emulador (por ejemplo, API 36) y presionar **Run ▶**.
