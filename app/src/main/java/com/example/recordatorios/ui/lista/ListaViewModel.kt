package com.example.recordatorios.ui.lista

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.recordatorios.RecordatoriosApp
import com.example.recordatorios.data.Recordatorio
import com.example.recordatorios.data.RepositorioRecordatorios
import com.example.recordatorios.data.SimuladorEstados
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Estados mutuamente excluyentes del listado: nunca hay carga, datos y error a la vez. */
sealed interface ListaUiState {
    data object Cargando : ListaUiState
    data object Vacio : ListaUiState
    data class ConDatos(val recordatorios: List<Recordatorio>) : ListaUiState
    data class Error(val mensaje: String) : ListaUiState
}

/** Opciones del mecanismo de simulación que ofrece la pantalla. */
enum class Simulacion { DatosDeEjemplo, ListaVacia, ErrorAlCargar, ErrorAlGuardar }

/**
 * Administra el estado del listado. Depende de las interfaces del repositorio y del
 * simulador, nunca de una lista concreta, y no guarda referencias a Activity ni a View.
 */
class ListaViewModel(
    private val repositorio: RepositorioRecordatorios,
    private val simulador: SimuladorEstados,
) : ViewModel() {

    private val _uiState = MutableStateFlow<ListaUiState>(ListaUiState.Cargando)
    val uiState: StateFlow<ListaUiState> = _uiState.asStateFlow()

    /** Aviso breve (por ejemplo, la simulación de error al guardar). Se consume una vez. */
    private val _aviso = MutableStateFlow<Simulacion?>(null)
    val aviso: StateFlow<Simulacion?> = _aviso.asStateFlow()

    private var cargaJob: Job? = null

    init {
        cargar()
    }

    /** Evento de la interfaz: el usuario pulsa «Reintentar». */
    fun reintentar() = cargar()

    /** Evento de la interfaz: el usuario elige una opción del menú de simulación. */
    fun simular(opcion: Simulacion) {
        when (opcion) {
            Simulacion.DatosDeEjemplo -> simulador.restaurarDatosDeEjemplo().also { cargar() }
            Simulacion.ListaVacia -> simulador.vaciar().also { cargar() }
            Simulacion.ErrorAlCargar -> simulador.fallarProximaCarga().also { cargar() }
            Simulacion.ErrorAlGuardar -> {
                simulador.fallarProximoGuardado()
                _aviso.update { Simulacion.ErrorAlGuardar }
            }
        }
    }

    fun avisoMostrado() = _aviso.update { null }

    /**
     * Observa el repositorio dentro de viewModelScope: la demora simulada no bloquea la
     * interfaz y cada cambio en los datos (por ejemplo, un guardado) produce un nuevo estado.
     */
    private fun cargar() {
        cargaJob?.cancel()
        cargaJob = viewModelScope.launch {
            repositorio.observarRecordatorios()
                .onStart { _uiState.update { ListaUiState.Cargando } }
                .catch { error ->
                    _uiState.update { ListaUiState.Error(error.message ?: "Ocurrió un error inesperado.") }
                }
                .collect { lista ->
                    _uiState.update {
                        if (lista.isEmpty()) ListaUiState.Vacio
                        else ListaUiState.ConDatos(lista.sortedByDescending { r -> r.prioridad.nivel })
                    }
                }
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as RecordatoriosApp
                ListaViewModel(app.repositorio, app.simulador)
            }
        }
    }
}
