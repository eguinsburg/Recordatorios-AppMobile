package com.example.recordatorios.ui.lista

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.recordatorios.R
import com.example.recordatorios.data.RepositorioSimulado
import com.example.recordatorios.ui.componentes.MensajeEstado
import com.example.recordatorios.ui.componentes.RecordatorioCard
import com.example.recordatorios.ui.componentes.TipoMensaje
import com.example.recordatorios.ui.theme.RecordatoriosTheme

/** Pantalla con estado: obtiene el ViewModel y observa su StateFlow según el ciclo de vida. */
@Composable
fun ListaScreen(
    onCrearRecordatorio: () -> Unit,
    onRecordatorioClick: (Long) -> Unit,
    viewModel: ListaViewModel = viewModel(factory = ListaViewModel.Factory),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val aviso by viewModel.aviso.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val textoAviso = stringResource(R.string.simular_aviso_guardado)

    LaunchedEffect(aviso) {
        if (aviso != null) {
            snackbarHostState.showSnackbar(textoAviso)
            viewModel.avisoMostrado()
        }
    }

    ListaContenido(
        uiState = uiState,
        onCrearRecordatorio = onCrearRecordatorio,
        onRecordatorioClick = onRecordatorioClick,
        onReintentar = viewModel::reintentar,
        onSimular = viewModel::simular,
        snackbarHostState = snackbarHostState,
    )
}

/** Pantalla sin estado: dibuja el UiState recibido y comunica las acciones con lambdas. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListaContenido(
    uiState: ListaUiState,
    onCrearRecordatorio: () -> Unit,
    onRecordatorioClick: (Long) -> Unit,
    onReintentar: () -> Unit,
    onSimular: (Simulacion) -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.lista_titulo)) },
                actions = { MenuSimulacion(onSimular) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                ),
            )
        },
        floatingActionButton = {
            // El FAB solo aparece con datos; el estado vacío ya ofrece su propia acción.
            if (uiState is ListaUiState.ConDatos) {
                ExtendedFloatingActionButton(
                    onClick = onCrearRecordatorio,
                    icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                    text = { Text(stringResource(R.string.lista_nuevo)) },
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        // innerPadding evita que el contenido quede debajo de la barra superior o del sistema.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter,
        ) {
            val contenidoModifier = Modifier.widthIn(max = 640.dp)
            when (uiState) {
                ListaUiState.Cargando -> MensajeEstado(
                    TipoMensaje.Carga(stringResource(R.string.lista_cargando)),
                    modifier = contenidoModifier,
                )
                ListaUiState.Vacio -> MensajeEstado(
                    TipoMensaje.Vacio(
                        titulo = stringResource(R.string.lista_vacia_titulo),
                        texto = stringResource(R.string.lista_vacia_mensaje),
                        textoAccion = stringResource(R.string.lista_vacia_accion),
                    ),
                    modifier = contenidoModifier,
                    onAccion = onCrearRecordatorio,
                )
                is ListaUiState.Error -> MensajeEstado(
                    TipoMensaje.Error(
                        titulo = stringResource(R.string.lista_error_titulo),
                        texto = uiState.mensaje,
                        textoAccion = stringResource(R.string.reintentar),
                    ),
                    modifier = contenidoModifier,
                    onAccion = onReintentar,
                )
                is ListaUiState.ConDatos -> LazyColumn(
                    modifier = contenidoModifier.fillMaxWidth(),
                    // Espacio inferior extra para que el FAB no tape la última tarjeta.
                    contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    item {
                        Text(
                            stringResource(R.string.lista_cantidad, uiState.recordatorios.size),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    items(uiState.recordatorios, key = { it.id }) { recordatorio ->
                        RecordatorioCard(
                            recordatorio = recordatorio,
                            onClick = { onRecordatorioClick(recordatorio.id) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MenuSimulacion(onSimular: (Simulacion) -> Unit) {
    var abierto by rememberSaveable { mutableStateOf(false) }
    IconButton(onClick = { abierto = true }) {
        Icon(Icons.Filled.MoreVert, contentDescription = stringResource(R.string.simular_menu))
    }
    DropdownMenu(expanded = abierto, onDismissRequest = { abierto = false }) {
        listOf(
            Simulacion.DatosDeEjemplo to R.string.simular_datos,
            Simulacion.ListaVacia to R.string.simular_vacio,
            Simulacion.ErrorAlCargar to R.string.simular_error_carga,
            Simulacion.ErrorAlGuardar to R.string.simular_error_guardado,
        ).forEach { (opcion, texto) ->
            DropdownMenuItem(
                text = { Text(stringResource(texto)) },
                onClick = {
                    abierto = false
                    onSimular(opcion)
                },
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun ListaConDatosPreview() {
    RecordatoriosTheme(dynamicColor = false) {
        ListaContenido(
            uiState = ListaUiState.ConDatos(RepositorioSimulado.recordatorios),
            onCrearRecordatorio = {}, onRecordatorioClick = {}, onReintentar = {}, onSimular = {},
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun ListaVaciaPreview() {
    RecordatoriosTheme(dynamicColor = false) {
        ListaContenido(ListaUiState.Vacio, {}, {}, {}, {})
    }
}
