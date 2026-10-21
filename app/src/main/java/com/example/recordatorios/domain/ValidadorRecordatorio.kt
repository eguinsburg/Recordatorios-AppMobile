package com.example.recordatorios.domain

import com.example.recordatorios.data.EstadoRecordatorio
import com.example.recordatorios.data.Recordatorio
import com.example.recordatorios.data.ResultadoValidacion
import java.time.LocalDate

/**
 * Reglas iniciales del producto. Los datos incorrectos son una situación
 * esperable: no se lanzan excepciones, se devuelve un ResultadoValidacion.
 */
object ValidadorRecordatorio {

    const val LARGO_MINIMO_TITULO = 4
    const val LARGO_MAXIMO_TITULO = 60
    const val LARGO_MAXIMO_DESCRIPCION = 200

    fun validar(recordatorio: Recordatorio, hoy: LocalDate = LocalDate.now()): ResultadoValidacion {
        val errores = mutableListOf<String>()
        val titulo = recordatorio.titulo.trim()

        if (recordatorio.id <= 0) errores += "El id debe ser positivo"

        when {
            titulo.isEmpty() -> errores += "El título es obligatorio"
            titulo.length < LARGO_MINIMO_TITULO ->
                errores += "El título debe tener al menos $LARGO_MINIMO_TITULO caracteres"
            titulo.length > LARGO_MAXIMO_TITULO ->
                errores += "El título no puede superar $LARGO_MAXIMO_TITULO caracteres"
        }

        // La descripción es opcional, pero si existe no puede estar en blanco.
        recordatorio.descripcion?.let { descripcion ->
            if (descripcion.isBlank()) errores += "La descripción no puede estar vacía (usar null)"
            if (descripcion.length > LARGO_MAXIMO_DESCRIPCION) errores += "La descripción es demasiado larga"
        }

        // Un recordatorio pendiente no puede tener una fecha límite pasada.
        val fecha = recordatorio.fechaLimite
        if (fecha != null && recordatorio.estado == EstadoRecordatorio.Pendiente && fecha.isBefore(hoy)) {
            errores += "Un recordatorio pendiente no puede vencer en el pasado"
        }

        // No se puede completar en el futuro.
        val estado = recordatorio.estado
        if (estado is EstadoRecordatorio.Completado && estado.fecha.isAfter(hoy)) {
            errores += "La fecha de completado no puede ser futura"
        }

        if (recordatorio.etiquetas.any { it.isBlank() }) errores += "Las etiquetas no pueden estar vacías"

        return if (errores.isEmpty()) {
            // Se devuelve una copia normalizada (título sin espacios sobrantes).
            ResultadoValidacion.Valido(recordatorio.copy(titulo = titulo))
        } else {
            ResultadoValidacion.Invalido(recordatorio.titulo, errores)
        }
    }
}
