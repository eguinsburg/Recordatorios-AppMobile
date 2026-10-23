package com.example.recordatorios.ui.detalle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.recordatorios.RecordatoriosApp
import com.example.recordatorios.data.Recordatorio
import com.example.recordatorios.data.RepositorioRecordatorios
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface DetalleUiState {
    data object Cargando : DetalleUiState
    data class Encontrado(val recordatorio: Recordatorio) : DetalleUiState
    data class NoEncontrado(val id: Long) : DetalleUiState
    data class Error(val mensaje: String) : DetalleUiState
}

/**
 * Recibe solo el identificador (el argumento de la ruta) y solicita el registro al
 * repositorio, así el detalle siempre muestra la versión actual del recordatorio.
 */
class DetalleViewModel(
    private val id: Long,
    private val repositorio: RepositorioRecordatorios,
) : ViewModel() {

    private val _uiState = MutableStateFlow<DetalleUiState>(DetalleUiState.Cargando)
    val uiState: StateFlow<DetalleUiState> = _uiState.asStateFlow()

    init {
        cargar()
    }

    fun reintentar() = cargar()

    private fun cargar() {
        _uiState.update { DetalleUiState.Cargando }
        viewModelScope.launch {
            val nuevoEstado = try {
                repositorio.obtenerPorId(id)
                    ?.let { DetalleUiState.Encontrado(it) }
                    ?: DetalleUiState.NoEncontrado(id)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                DetalleUiState.Error(e.message ?: "No se pudo cargar el recordatorio.")
            }
            _uiState.update { nuevoEstado }
        }
    }

    companion object {
        fun factory(id: Long) = viewModelFactory {
            initializer {
                DetalleViewModel(id, (this[APPLICATION_KEY] as RecordatoriosApp).repositorio)
            }
        }
    }
}
