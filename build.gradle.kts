// Archivo de construcción de nivel superior: declara los plugins (sin aplicarlos).
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.kotlin.serialization) apply false
}
