package com.example.recordatorios.ui.componentes

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.recordatorios.data.Categoria
import com.example.recordatorios.data.EstadoRecordatorio
import com.example.recordatorios.data.Prioridad
import com.example.recordatorios.data.RepositorioSimulado
import com.example.recordatorios.data.etiqueta
import java.time.format.DateTimeFormatter

/** Etiqueta de texto sobre un fondo suave del mismo color. El texto siempre acompaña al color. */
@Composable
fun EtiquetaColor(texto: String, color: Color, modifier: Modifier = Modifier) {
    Surface(color = color.copy(alpha = 0.15f), shape = MaterialTheme.shapes.small, modifier = modifier) {
        Text(
            texto,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelMedium,
            color = color,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
fun EtiquetaCategoria(categoria: Categoria, modifier: Modifier = Modifier) {
    val estilo = RepositorioSimulado.estilosPorCategoria.getValue(categoria)
    EtiquetaColor(estilo.etiqueta, Color(estilo.colorArgb), modifier)
}

@Composable
fun EtiquetaPrioridad(prioridad: Prioridad, modifier: Modifier = Modifier) {
    EtiquetaColor("Prioridad ${prioridad.nombre}", colorPrioridad(prioridad), modifier)
}

fun colorPrioridad(prioridad: Prioridad): Color = when (prioridad) {
    Prioridad.Alta -> Color(0xFFD32F2F)
    Prioridad.Media -> Color(0xFFE65100)
    Prioridad.Baja -> Color(0xFF388E3C)
}

private val formatoFecha = DateTimeFormatter.ofPattern("dd/MM/yyyy")

/** Texto legible del estado, incluida la información propia de cada variante. */
fun EstadoRecordatorio.descripcionCompleta(): String = when (this) {
    EstadoRecordatorio.Pendiente, EstadoRecordatorio.EnProgreso -> etiqueta()
    is EstadoRecordatorio.Completado -> "${etiqueta()} el ${fecha.format(formatoFecha)}"
    is EstadoRecordatorio.Cancelado -> motivo?.let { "${etiqueta()} ($it)" } ?: etiqueta()
}

fun java.time.LocalDate.formatoCorto(): String = format(formatoFecha)
