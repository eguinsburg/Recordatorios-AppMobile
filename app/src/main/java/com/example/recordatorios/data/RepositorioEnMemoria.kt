package com.example.recordatorios.data

import com.example.recordatorios.domain.ValidadorRecordatorio
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update
import java.io.IOException
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong

/**
 * Implementación en memoria del repositorio. Simula la demora de una fuente real con
 * delay(), que suspende la coroutine sin bloquear el hilo principal, y permite forzar errores.
 */
class RepositorioEnMemoria(
    private val demoraCargaMs: Long = 1_200,
    private val demoraGuardadoMs: Long = 1_500,
    datosIniciales: List<Recordatorio> = RepositorioSimulado.recordatorios,
) : RepositorioRecordatorios, SimuladorEstados {

    private val datos = MutableStateFlow(datosIniciales)
    private val ultimoId = AtomicLong(datosIniciales.maxOfOrNull { it.id } ?: 0)
    private val errorEnCarga = AtomicBoolean(false)
    private val errorEnGuardado = AtomicBoolean(false)

    override fun observarRecordatorios(): Flow<List<Recordatorio>> = flow {
        delay(demoraCargaMs)
        if (errorEnCarga.getAndSet(false)) {
            throw IOException("No se pudieron obtener los recordatorios. Revisá la conexión e intentá de nuevo.")
        }
        emitAll(datos)
    }

    override suspend fun obtenerPorId(id: Long): Recordatorio? {
        delay(demoraCargaMs / 2)
        return datos.value.firstOrNull { it.id == id }
    }

    override suspend fun guardar(nuevo: NuevoRecordatorio): Recordatorio {
        delay(demoraGuardadoMs)
        if (errorEnGuardado.getAndSet(false)) {
            throw IOException("No se pudo guardar el recordatorio. Intentá nuevamente.")
        }
        val recordatorio = Recordatorio(
            id = ultimoId.incrementAndGet(),
            titulo = nuevo.titulo,
            descripcion = nuevo.descripcion,
            categoria = nuevo.categoria,
            prioridad = nuevo.prioridad,
        )
        // Se conservan las reglas de la Actividad 1 como última barrera antes de guardar.
        return when (val resultado = ValidadorRecordatorio.validar(recordatorio)) {
            is ResultadoValidacion.Valido -> resultado.recordatorio.also { valido ->
                datos.update { lista -> lista + valido }
            }
            is ResultadoValidacion.Invalido -> throw IllegalArgumentException(resultado.errores.joinToString())
        }
    }

    // --- SimuladorEstados ---
    override fun restaurarDatosDeEjemplo() = datos.update { RepositorioSimulado.recordatorios }
    override fun vaciar() = datos.update { emptyList() }
    override fun fallarProximaCarga() = errorEnCarga.set(true)
    override fun fallarProximoGuardado() = errorEnGuardado.set(true)
}
