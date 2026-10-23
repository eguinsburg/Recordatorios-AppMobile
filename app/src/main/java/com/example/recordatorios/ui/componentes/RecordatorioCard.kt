package com.example.recordatorios.ui.componentes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.recordatorios.data.EstadoRecordatorio
import com.example.recordatorios.data.Recordatorio
import com.example.recordatorios.data.RepositorioSimulado
import com.example.recordatorios.ui.theme.RecordatoriosTheme

/**
 * Representa un recordatorio del listado: título, categoría, prioridad y estado.
 * No conoce la navegación: solo informa la selección mediante [onClick].
 */
@Composable
fun RecordatorioCard(
    recordatorio: Recordatorio,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val completado = recordatorio.estado is EstadoRecordatorio.Completado
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                recordatorio.titulo,
                style = MaterialTheme.typography.titleMedium,
                textDecoration = if (completado) TextDecoration.LineThrough else null,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                EtiquetaCategoria(recordatorio.categoria)
                EtiquetaPrioridad(recordatorio.prioridad)
            }
            Text(
                "Estado: ${recordatorio.estado.descripcionCompleta()}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun RecordatorioCardPreview() {
    RecordatoriosTheme(dynamicColor = false) {
        RecordatorioCard(RepositorioSimulado.recordatorios.first(), onClick = {}, modifier = Modifier.padding(16.dp))
    }
}
