package com.example.recordatorios.ui.detalle

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.recordatorios.R
import com.example.recordatorios.data.Recordatorio
import com.example.recordatorios.data.RepositorioSimulado
import com.example.recordatorios.ui.componentes.EtiquetaCategoria
import com.example.recordatorios.ui.componentes.EtiquetaPrioridad
import com.example.recordatorios.ui.componentes.MensajeEstado
import com.example.recordatorios.ui.componentes.TipoMensaje
import com.example.recordatorios.ui.componentes.descripcionCompleta
import com.example.recordatorios.ui.componentes.formatoCorto
import com.example.recordatorios.ui.theme.RecordatoriosTheme

@Composable
fun DetalleScreen(
    id: Long,
    onVolver: () -> Unit,
    viewModel: DetalleViewModel = viewModel(factory = DetalleViewModel.factory(id)),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    DetalleContenido(uiState = uiState, onVolver = onVolver, onReintentar = viewModel::reintentar)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleContenido(
    uiState: DetalleUiState,
    onVolver: () -> Unit,
    onReintentar: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.detalle_titulo)) },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.volver))
                    }
                },
            )
        },
    ) { innerPadding ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter,
        ) {
            val contenidoModifier = Modifier.widthIn(max = 640.dp)
            when (uiState) {
                DetalleUiState.Cargando -> MensajeEstado(
                    TipoMensaje.Carga(stringResource(R.string.detalle_cargando)),
                    modifier = contenidoModifier,
                )
                is DetalleUiState.Encontrado -> DetalleRecordatorio(
                    uiState.recordatorio,
                    modifier = contenidoModifier
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                )
                is DetalleUiState.NoEncontrado -> MensajeEstado(
                    TipoMensaje.Vacio(
                        titulo = stringResource(R.string.detalle_titulo),
                        texto = stringResource(R.string.detalle_no_encontrado, uiState.id),
                        textoAccion = stringResource(R.string.volver),
                    ),
                    modifier = contenidoModifier,
                    onAccion = onVolver,
                )
                is DetalleUiState.Error -> MensajeEstado(
                    TipoMensaje.Error(
                        titulo = stringResource(R.string.detalle_titulo),
                        texto = uiState.mensaje,
                        textoAccion = stringResource(R.string.reintentar),
                    ),
                    modifier = contenidoModifier,
                    onAccion = onReintentar,
                )
            }
        }
    }
}

/** Información completa del registro recibido mediante su identificador. */
@Composable
fun DetalleRecordatorio(recordatorio: Recordatorio, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            recordatorio.titulo,
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.semantics { heading() },
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            EtiquetaCategoria(recordatorio.categoria)
            EtiquetaPrioridad(recordatorio.prioridad)
        }
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                FilaDetalle("Estado", recordatorio.estado.descripcionCompleta())
                HorizontalDivider()
                FilaDetalle(
                    "Descripción",
                    recordatorio.descripcion ?: stringResource(R.string.detalle_sin_descripcion),
                )
                HorizontalDivider()
                FilaDetalle(
                    "Fecha límite",
                    recordatorio.fechaLimite?.formatoCorto() ?: stringResource(R.string.detalle_sin_fecha),
                )
                HorizontalDivider()
                FilaDetalle("Ubicación", recordatorio.ubicacion ?: stringResource(R.string.detalle_sin_ubicacion))
                HorizontalDivider()
                FilaDetalle(
                    "Etiquetas",
                    recordatorio.etiquetas.takeIf { it.isNotEmpty() }?.joinToString(", ")
                        ?: stringResource(R.string.detalle_sin_etiquetas),
                )
                HorizontalDivider()
                FilaDetalle("Identificador", "#${recordatorio.id}")
            }
        }
    }
}

@Composable
fun FilaDetalle(etiqueta: String, valor: String, modifier: Modifier = Modifier) {
    Column(modifier) {
        Text(etiqueta, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(valor, style = MaterialTheme.typography.bodyLarge)
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun DetallePreview() {
    RecordatoriosTheme(dynamicColor = false) {
        DetalleContenido(DetalleUiState.Encontrado(RepositorioSimulado.recordatorios.first()), {}, {})
    }
}
