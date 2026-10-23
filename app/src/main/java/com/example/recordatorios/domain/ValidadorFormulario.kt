package com.example.recordatorios.domain

import com.example.recordatorios.data.Catalogo
import com.example.recordatorios.data.Categoria
import com.example.recordatorios.data.Prioridad

/** Mensajes de validación por campo; null significa que el campo es válido. */
data class ErroresFormulario(
    val titulo: String? = null,
    val descripcion: String? = null,
    val categoria: String? = null,
    val prioridad: String? = null,
) {
    val hayErrores: Boolean
        get() = listOf(titulo, descripcion, categoria, prioridad).any { it != null }
}

/** Reglas del formulario de creación (reutiliza los límites de la Actividad 1). */
object ValidadorFormulario {

    fun validar(
        titulo: String,
        descripcion: String,
        categoria: Categoria?,
        prioridad: Prioridad?,
    ) = ErroresFormulario(
        titulo = validarTitulo(titulo),
        descripcion = validarDescripcion(descripcion),
        categoria = validarCategoria(categoria),
        prioridad = if (prioridad == null) "Seleccioná una prioridad" else null,
    )

    fun validarTitulo(titulo: String): String? {
        val largo = titulo.trim().length
        return when {
            largo == 0 -> "El título es obligatorio"
            largo < ValidadorRecordatorio.LARGO_MINIMO_TITULO ->
                "El título debe tener al menos ${ValidadorRecordatorio.LARGO_MINIMO_TITULO} caracteres, sin contar espacios al inicio y al final"
            largo > ValidadorRecordatorio.LARGO_MAXIMO_TITULO ->
                "El título no puede superar ${ValidadorRecordatorio.LARGO_MAXIMO_TITULO} caracteres"
            else -> null
        }
    }

    fun validarDescripcion(descripcion: String): String? =
        if (descripcion.length > ValidadorRecordatorio.LARGO_MAXIMO_DESCRIPCION) {
            "La descripción no puede superar ${ValidadorRecordatorio.LARGO_MAXIMO_DESCRIPCION} caracteres"
        } else {
            null
        }

    fun validarCategoria(categoria: Categoria?): String? = when {
        categoria == null -> "Seleccioná una categoría"
        categoria !in Catalogo.categorias -> "La categoría no pertenece a las opciones definidas"
        else -> null
    }
}
