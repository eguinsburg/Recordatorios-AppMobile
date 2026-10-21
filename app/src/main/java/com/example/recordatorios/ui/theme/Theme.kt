package com.example.recordatorios.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val Azul = Color(0xFF3355CC)
private val AzulClaro = Color(0xFFB4C5FF)
private val Turquesa = Color(0xFF00897B)

private val EsquemaClaro = lightColorScheme(primary = Azul, secondary = Turquesa)
private val EsquemaOscuro = darkColorScheme(primary = AzulClaro, secondary = Turquesa)

/** Tema Material 3 de la app: claro, oscuro y color dinámico en Android 12+. */
@Composable
fun RecordatoriosTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> EsquemaOscuro
        else -> EsquemaClaro
    }
    MaterialTheme(colorScheme = colorScheme, content = content)
}
