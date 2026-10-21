package com.example.recordatorios.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.recordatorios.R
import com.example.recordatorios.data.Categoria
import com.example.recordatorios.data.EstadoPantalla
import com.example.recordatorios.data.EstadoRecordatorio
import com.example.recordatorios.data.EstiloCategoria
import com.example.recordatorios.data.Filtro
import com.example.recordatorios.data.Prioridad
import com.example.recordatorios.data.Recordatorio
import com.example.recordatorios.data.RepositorioSimulado
import com.example.recordatorios.data.ResultadoValidacion
import com.example.recordatorios.data.etiqueta
import com.example.recordatorios.data.filtrar
import com.example.recordatorios.ui.theme.RecordatoriosTheme
import java.time.format.DateTimeFormatter

private val formatoFecha = DateTimeFormatter.ofPattern("dd/MM/yyyy")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordatoriosScreen(
    recordatorios: List<Recordatorio> = RepositorioSimulado.recordatorios,
    estilos: Map<Categoria, EstiloCategoria> = RepositorioSimulado.estilosPorCategoria,
    descartados: List<ResultadoValidacion.Invalido> = RepositorioSimulado.descartados,
) {
    // Estado simple de la pantalla: el filtro seleccionado.
    var filtro by remember { mutableStateOf<Filtro>(Filtro.Todos) }

    val visibles = recordatorios.filtrar(filtro)
    val estado: EstadoPantalla =
        if (visibles.isEmpty()) EstadoPantalla.SinResultados else EstadoPantalla.ConDatos(visibles)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(stringResource(R.string.titulo_pantalla), fontWeight = FontWeight.Bold)
                        Text(
                            stringResource(R.string.subtitulo_datos_simulados),
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                ),
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item { Resumen(recordatorios) }
            item { SeccionCategorias(RepositorioSimulado.categoriasEnUso, estilos) }
            item { Filtros(filtro, onFiltroChange = { filtro = it }) }

            when (estado) {
                EstadoPantalla.Cargando -> item {
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is EstadoPantalla.ConDatos -> items(estado.recordatorios, key = { it.id }) { r ->
                    TarjetaRecordatorio(r, estilos.getValue(r.categoria))
                }
                EstadoPantalla.SinResultados -> item {
                    Text(stringResource(R.string.sin_resultados), Modifier.padding(16.dp))
                }
                is EstadoPantalla.Error -> item {
                    Text(estado.mensaje, color = MaterialTheme.colorScheme.error)
                }
            }

            if (descartados.isNotEmpty()) {
                item { TarjetaDescartados(descartados) }
            }
        }
    }
}

@Composable
private fun Resumen(recordatorios: List<Recordatorio>) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        DatoResumen(
            stringResource(R.string.resumen_pendientes),
            recordatorios.filtrar(Filtro.Pendientes).size,
            Modifier.weight(1f),
        )
        DatoResumen(
            stringResource(R.string.resumen_completados),
            recordatorios.filtrar(Filtro.Completados).size,
            Modifier.weight(1f),
        )
        DatoResumen(
            stringResource(R.string.resumen_alta),
            recordatorios.filtrar(Filtro.AltaPrioridad).size,
            Modifier.weight(1f),
        )
    }
}

@Composable
private fun DatoResumen(texto: String, valor: Int, modifier: Modifier = Modifier) {
    Card(modifier) {
        Column(Modifier.padding(12.dp)) {
            Text("$valor", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text(texto, style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
private fun SeccionCategorias(categorias: Set<Categoria>, estilos: Map<Categoria, EstiloCategoria>) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(stringResource(R.string.seccion_categorias), style = MaterialTheme.typography.titleSmall)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            items(categorias.toList()) { categoria ->
                val estilo = estilos.getValue(categoria)
                val cantidad = RepositorioSimulado.cantidadPorCategoria[categoria] ?: 0
                EtiquetaColor("${estilo.etiqueta} ($cantidad)", Color(estilo.colorArgb))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Filtros(seleccionado: Filtro, onFiltroChange: (Filtro) -> Unit) {
    val opciones = listOf(
        Filtro.Todos to R.string.filtro_todos,
        Filtro.Pendientes to R.string.filtro_pendientes,
        Filtro.Completados to R.string.filtro_completados,
        Filtro.AltaPrioridad to R.string.filtro_alta,
    )
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(opciones) { (filtro, texto) ->
            FilterChip(
                selected = filtro == seleccionado,
                onClick = { onFiltroChange(filtro) },
                label = { Text(stringResource(texto)) },
            )
        }
    }
}

@Composable
fun TarjetaRecordatorio(recordatorio: Recordatorio, estilo: EstiloCategoria, modifier: Modifier = Modifier) {
    val completado = recordatorio.estado is EstadoRecordatorio.Completado
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(
                modifier = Modifier.size(12.dp),
                shape = CircleShape,
                color = colorPrioridad(recordatorio.prioridad),
            ) {}
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    recordatorio.titulo,
                    style = MaterialTheme.typography.titleMedium,
                    textDecoration = if (completado) TextDecoration.LineThrough else null,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                recordatorio.descripcion?.let {
                    Text(it, style = MaterialTheme.typography.bodySmall)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    EtiquetaColor(estilo.etiqueta, Color(estilo.colorArgb))
                    EtiquetaColor("Prioridad ${recordatorio.prioridad}", colorPrioridad(recordatorio.prioridad))
                }
                Text(
                    textoEstado(recordatorio),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun textoEstado(r: Recordatorio): String {
    val fecha = r.fechaLimite?.let { "Vence ${it.format(formatoFecha)}" } ?: stringResource(R.string.sin_fecha)
    val detalle = when (val estado = r.estado) {
        is EstadoRecordatorio.Completado -> " el ${estado.fecha.format(formatoFecha)}"
        is EstadoRecordatorio.Cancelado -> estado.motivo?.let { " ($it)" } ?: ""
        EstadoRecordatorio.Pendiente, EstadoRecordatorio.EnProgreso -> " · $fecha"
    }
    val lugar = r.ubicacion?.let { " · 📍 $it" } ?: ""
    return "Estado: ${r.estado.etiqueta()}$detalle$lugar"
}

@Composable
private fun EtiquetaColor(texto: String, color: Color) {
    Surface(color = color.copy(alpha = 0.15f), shape = MaterialTheme.shapes.small) {
        Text(
            texto,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun TarjetaDescartados(descartados: List<ResultadoValidacion.Invalido>) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                stringResource(R.string.seccion_descartados),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onErrorContainer,
            )
            descartados.forEach { d ->
                val titulo = d.titulo.trim().ifEmpty { "(sin título)" }
                Text(
                    "• \"$titulo\": ${d.errores.joinToString("; ")}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                )
            }
        }
    }
}

private fun colorPrioridad(prioridad: Prioridad): Color = when (prioridad) {
    Prioridad.Alta -> Color(0xFFD32F2F)
    Prioridad.Media -> Color(0xFFF9A825)
    Prioridad.Baja -> Color(0xFF388E3C)
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun RecordatoriosScreenPreview() {
    RecordatoriosTheme(dynamicColor = false) {
        RecordatoriosScreen()
    }
}
