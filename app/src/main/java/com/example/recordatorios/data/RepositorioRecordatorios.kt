package com.example.recordatorios.data

import kotlinx.coroutines.flow.Flow

/**
 * Operaciones que necesita la aplicación, sin revelar de dónde salen los datos.
 * Los ViewModel dependen de esta interfaz: más adelante la implementación en memoria
 * podrá reemplazarse por Room o por un servicio remoto sin modificar las pantallas.
 */
interface RepositorioRecordatorios {
    /** Emite la lista actual y cada nueva versión cuando cambia. Puede fallar al cargar. */
    fun observarRecordatorios(): Flow<List<Recordatorio>>

    /** Devuelve el recordatorio con ese id, o null si no existe. */
    suspend fun obtenerPorId(id: Long): Recordatorio?

    /** Guarda un recordatorio nuevo, le asigna un id y lo devuelve. Puede fallar. */
    suspend fun guardar(nuevo: NuevoRecordatorio): Recordatorio
}

/** Datos que provienen del formulario: todavía no tienen id ni estado. */
data class NuevoRecordatorio(
    val titulo: String,
    val descripcion: String?,
    val categoria: Categoria,
    val prioridad: Prioridad,
)

/**
 * Mecanismo de simulación para comprobar todos los estados de la interfaz sin una
 * base de datos ni un servicio remoto. Es una interfaz aparte para que los ViewModel
 * tampoco dependan de la implementación concreta.
 */
interface SimuladorEstados {
    fun restaurarDatosDeEjemplo()
    fun vaciar()
    fun fallarProximaCarga()
    fun fallarProximoGuardado()
}
