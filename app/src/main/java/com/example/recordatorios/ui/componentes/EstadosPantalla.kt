package com.example.recordatorios.ui.componentes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.recordatorios.ui.theme.RecordatoriosTheme

/** Variantes que puede comunicar [MensajeEstado]. */
sealed interface TipoMensaje {
    data class Carga(val texto: String) : TipoMensaje
    data class Vacio(val titulo: String, val texto: String, val textoAccion: String) : TipoMensaje
    data class Error(val titulo: String, val texto: String, val textoAccion: String) : TipoMensaje
}

/**
 * Componente único para comunicar carga, vacío o error. El vacío y el error ofrecen una
 * acción ([onAccion]): crear un recordatorio o reintentar.
 */
@Composable
fun MensajeEstado(
    tipo: TipoMensaje,
    modifier: Modifier = Modifier,
    onAccion: () -> Unit = {},
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp)
            // Se anuncia a los lectores de pantalla cuando cambia el estado.
            .semantics { liveRegion = LiveRegionMode.Polite },
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        when (tipo) {
            is TipoMensaje.Carga -> {
                CircularProgressIndicator()
                Text(tipo.texto, style = MaterialTheme.typography.bodyLarge)
            }
            is TipoMensaje.Vacio -> {
                Contenido(Icons.Filled.Info, tipo.titulo, tipo.texto, MaterialTheme.colorScheme.primary)
                Button(onClick = onAccion) {
                    Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(ButtonDefaults.IconSize))
                    Text(tipo.textoAccion, Modifier.padding(start = 8.dp))
                }
            }
            is TipoMensaje.Error -> {
                Contenido(Icons.Filled.Warning, tipo.titulo, tipo.texto, MaterialTheme.colorScheme.error)
                Button(onClick = onAccion) {
                    Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(ButtonDefaults.IconSize))
                    Text(tipo.textoAccion, Modifier.padding(start = 8.dp))
                }
            }
        }
    }
}

@Composable
private fun Contenido(icono: ImageVector, titulo: String, texto: String, color: androidx.compose.ui.graphics.Color) {
    Icon(icono, contentDescription = null, tint = color, modifier = Modifier.size(56.dp))
    Text(titulo, style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
    Text(
        texto,
        style = MaterialTheme.typography.bodyLarge,
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Preview(showBackground = true)
@Composable
private fun MensajeEstadoErrorPreview() {
    RecordatoriosTheme(dynamicColor = false) {
        MensajeEstado(TipoMensaje.Error("No pudimos cargar", "Revisá la conexión.", "Reintentar"))
    }
}
