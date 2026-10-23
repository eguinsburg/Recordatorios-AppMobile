package com.example.recordatorios.ui.crear

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.recordatorios.RecordatoriosApp
import com.example.recordatorios.data.Categoria
import com.example.recordatorios.data.NuevoRecordatorio
import com.example.recordatorios.data.Prioridad
import com.example.recordatorios.data.RepositorioRecordatorios
import com.example.recordatorios.domain.ErroresFormulario
import com.example.recordatorios.domain.ValidadorFormulario
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Estado del envío: mutuamente excluyente, por eso es una jerarquía sellada. */
sealed interface EstadoEnvio {
    data object Inactivo : EstadoEnvio
    data object Enviando : EstadoEnvio
    data class Exito(val idCreado: Long) : EstadoEnvio
    data class Error(val mensaje: String) : EstadoEnvio
}

enum class Campo { Titulo, Descripcion, Categoria, Prioridad }

/**
 * Estado inmutable del formulario: valores actuales, mensajes de validación visibles,
 * estado del envío (con su resultado de éxito o el error general de guardado).
 */
data class CrearUiState(
    val titulo: String = "",
    val descripcion: String = "",
    val categoria: Categoria? = null,
    val prioridad: Prioridad? = null,
    val camposModificados: Set<Campo> = emptySet(),
    val envio: EstadoEnvio = EstadoEnvio.Inactivo,
) {
    /** Validación completa, aplicada sobre los valores actuales. */
    private val validacion: ErroresFormulario
        get() = ValidadorFormulario.validar(titulo, descripcion, categoria, prioridad)

    /** Solo se muestran los mensajes de los campos que el usuario ya modificó. */
    val errores: ErroresFormulario
        get() = validacion.let { v ->
            ErroresFormulario(
                titulo = v.titulo.takeIf { Campo.Titulo in camposModificados },
                descripcion = v.descripcion.takeIf { Campo.Descripcion in camposModificados },
                categoria = v.categoria.takeIf { Campo.Categoria in camposModificados },
                prioridad = v.prioridad.takeIf { Campo.Prioridad in camposModificados },
            )
        }

    val esValido: Boolean get() = !validacion.hayErrores
    val enviando: Boolean get() = envio is EstadoEnvio.Enviando

    /** El botón Guardar se deshabilita si el formulario es inválido o hay un envío en curso. */
    val puedeGuardar: Boolean get() = esValido && !enviando
}

/**
 * Recibe los eventos del formulario, aplica las validaciones y actualiza el estado con
 * nuevas instancias mediante update/copy. Las funciones componibles nunca lo modifican.
 */
class CrearViewModel(
    private val repositorio: RepositorioRecordatorios,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CrearUiState())
    val uiState: StateFlow<CrearUiState> = _uiState.asStateFlow()

    fun onTituloChange(valor: String) = _uiState.update {
        it.copy(titulo = valor, camposModificados = it.camposModificados + Campo.Titulo)
    }

    fun onDescripcionChange(valor: String) = _uiState.update {
        it.copy(descripcion = valor, camposModificados = it.camposModificados + Campo.Descripcion)
    }

    fun onCategoriaSeleccionada(categoria: Categoria) = _uiState.update {
        it.copy(categoria = categoria, camposModificados = it.camposModificados + Campo.Categoria)
    }

    fun onPrioridadSeleccionada(prioridad: Prioridad) = _uiState.update {
        it.copy(prioridad = prioridad, camposModificados = it.camposModificados + Campo.Prioridad)
    }

    fun guardar() {
        val actual = _uiState.value
        if (!actual.puedeGuardar) {
            // Ante un intento inválido se muestran todos los mensajes pendientes.
            _uiState.update { it.copy(camposModificados = Campo.entries.toSet()) }
            return
        }
        _uiState.update { it.copy(envio = EstadoEnvio.Enviando) }
        viewModelScope.launch {
            try {
                val creado = repositorio.guardar(
                    NuevoRecordatorio(
                        titulo = actual.titulo.trim(),
                        descripcion = actual.descripcion.trim().ifBlank { null },
                        categoria = requireNotNull(actual.categoria),
                        prioridad = requireNotNull(actual.prioridad),
                    )
                )
                _uiState.update { it.copy(envio = EstadoEnvio.Exito(creado.id)) }
            } catch (e: CancellationException) {
                throw e // la cancelación del alcance no se oculta
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(envio = EstadoEnvio.Error(e.message ?: "No se pudo guardar el recordatorio."))
                }
            }
        }
    }

    /** La navegación tras el éxito es un evento de una sola vez: se consume y se restablece. */
    fun exitoProcesado() = _uiState.update { it.copy(envio = EstadoEnvio.Inactivo) }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                CrearViewModel((this[APPLICATION_KEY] as RecordatoriosApp).repositorio)
            }
        }
    }
}
