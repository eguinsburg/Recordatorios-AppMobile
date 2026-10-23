package com.example.recordatorios.ui.crear

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.recordatorios.R
import com.example.recordatorios.data.Catalogo
import com.example.recordatorios.data.Categoria
import com.example.recordatorios.data.Prioridad
import com.example.recordatorios.domain.ValidadorRecordatorio
import com.example.recordatorios.ui.componentes.CampoTextoValidado
import com.example.recordatorios.ui.componentes.SelectorOpciones
import com.example.recordatorios.ui.theme.RecordatoriosTheme

@Composable
fun CrearScreen(
    onGuardado: () -> Unit,
    onCancelar: () -> Unit,
    viewModel: CrearViewModel = viewModel(factory = CrearViewModel.Factory),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // El éxito se procesa una sola vez: se regresa al listado y se restablece el estado.
    LaunchedEffect(uiState.envio) {
        if (uiState.envio is EstadoEnvio.Exito) {
            onGuardado()
            viewModel.exitoProcesado()
        }
    }

    CrearContenido(
        uiState = uiState,
        onTituloChange = viewModel::onTituloChange,
        onDescripcionChange = viewModel::onDescripcionChange,
        onCategoriaSeleccionada = viewModel::onCategoriaSeleccionada,
        onPrioridadSeleccionada = viewModel::onPrioridadSeleccionada,
        onGuardar = viewModel::guardar,
        onCancelar = onCancelar,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrearContenido(
    uiState: CrearUiState,
    onTituloChange: (String) -> Unit,
    onDescripcionChange: (String) -> Unit,
    onCategoriaSeleccionada: (Categoria) -> Unit,
    onPrioridadSeleccionada: (Prioridad) -> Unit,
    onGuardar: () -> Unit,
    onCancelar: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.crear_titulo)) },
                navigationIcon = {
                    IconButton(onClick = onCancelar) {
                        Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.crear_cancelar))
                    }
                },
            )
        },
    ) { innerPadding ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding(),
            contentAlignment = Alignment.TopCenter,
        ) {
            FormularioRecordatorio(
                uiState = uiState,
                onTituloChange = onTituloChange,
                onDescripcionChange = onDescripcionChange,
                onCategoriaSeleccionada = onCategoriaSeleccionada,
                onPrioridadSeleccionada = onPrioridadSeleccionada,
                onGuardar = onGuardar,
                onCancelar = onCancelar,
                modifier = Modifier
                    .widthIn(max = 640.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
            )
        }
    }
}

/** Formulario reutilizable y sin estado propio: todos sus campos son controlados. */
@Composable
fun FormularioRecordatorio(
    uiState: CrearUiState,
    onTituloChange: (String) -> Unit,
    onDescripcionChange: (String) -> Unit,
    onCategoriaSeleccionada: (Categoria) -> Unit,
    onPrioridadSeleccionada: (Prioridad) -> Unit,
    onGuardar: () -> Unit,
    onCancelar: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val habilitado = !uiState.enviando
    val errores = uiState.errores
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        CampoTextoValidado(
            valor = uiState.titulo,
            onValorChange = onTituloChange,
            etiqueta = stringResource(R.string.campo_titulo),
            error = errores.titulo,
            maxCaracteres = ValidadorRecordatorio.LARGO_MAXIMO_TITULO,
            habilitado = habilitado,
        )
        CampoTextoValidado(
            valor = uiState.descripcion,
            onValorChange = onDescripcionChange,
            etiqueta = stringResource(R.string.campo_descripcion),
            error = errores.descripcion,
            maxCaracteres = ValidadorRecordatorio.LARGO_MAXIMO_DESCRIPCION,
            singleLine = false,
            habilitado = habilitado,
        )
        SelectorOpciones(
            etiqueta = stringResource(R.string.campo_categoria),
            opciones = Catalogo.categorias,
            seleccionada = uiState.categoria,
            onSeleccion = onCategoriaSeleccionada,
            textoOpcion = { it.nombre },
            error = errores.categoria,
            ayuda = stringResource(R.string.campo_obligatorio).takeIf { uiState.categoria == null },
            habilitado = habilitado,
        )
        SelectorOpciones(
            etiqueta = stringResource(R.string.campo_prioridad),
            opciones = Catalogo.prioridades,
            seleccionada = uiState.prioridad,
            onSeleccion = onPrioridadSeleccionada,
            textoOpcion = { it.nombre },
            error = errores.prioridad,
            ayuda = stringResource(R.string.campo_obligatorio).takeIf { uiState.prioridad == null },
            habilitado = habilitado,
        )

        val envio = uiState.envio
        if (envio is EstadoEnvio.Error) ErrorGeneral(envio.mensaje)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End),
        ) {
            OutlinedButton(onClick = onCancelar) { Text(stringResource(R.string.crear_cancelar)) }
            Button(onClick = onGuardar, enabled = uiState.puedeGuardar) {
                if (uiState.enviando) {
                    CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                    Text(stringResource(R.string.crear_guardando), Modifier.padding(start = 8.dp))
                } else {
                    Text(stringResource(R.string.crear_guardar))
                }
            }
        }
    }
}

@Composable
private fun ErrorGeneral(mensaje: String, modifier: Modifier = Modifier) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
        modifier = modifier
            .fillMaxWidth()
            .semantics { liveRegion = LiveRegionMode.Assertive },
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.onErrorContainer)
            Column(Modifier.padding(start = 12.dp)) {
                Text(
                    stringResource(R.string.crear_error_titulo),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                )
                Text(mensaje, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onErrorContainer)
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun CrearConErroresPreview() {
    RecordatoriosTheme(dynamicColor = false) {
        CrearContenido(
            uiState = CrearUiState(titulo = "ab", camposModificados = setOf(Campo.Titulo)),
            onTituloChange = {}, onDescripcionChange = {}, onCategoriaSeleccionada = {},
            onPrioridadSeleccionada = {}, onGuardar = {}, onCancelar = {},
        )
    }
}
