package com.example.recordatorios.data

import java.time.LocalDate

/**
 * Categoría de un recordatorio. Es una jerarquía sellada: el compilador conoce
 * todas las categorías posibles y los `when` no necesitan rama `else`.
 * Cada categoría es un `object` porque no transporta datos propios.
 */
sealed class Categoria(val nombre: String) {
    object Trabajo : Categoria("Trabajo")
    object Personal : Categoria("Personal")
    object Salud : Categoria("Salud")
    object Estudio : Categoria("Estudio")
    object Hogar : Categoria("Hogar")

    override fun toString(): String = nombre
}

/** Prioridad con un nivel numérico para poder ordenar recordatorios. */
sealed class Prioridad(val nombre: String, val nivel: Int) {
    object Alta : Prioridad("Alta", 3)
    object Media : Prioridad("Media", 2)
    object Baja : Prioridad("Baja", 1)

    override fun toString(): String = nombre
}

/**
 * Estado de un recordatorio. A diferencia de un enum, cada variante puede
 * transportar su propia información (por ejemplo, la fecha de completado).
 */
sealed class EstadoRecordatorio {
    object Pendiente : EstadoRecordatorio()
    object EnProgreso : EstadoRecordatorio()
    data class Completado(val fecha: LocalDate) : EstadoRecordatorio()
    data class Cancelado(val motivo: String?) : EstadoRecordatorio()
}

fun EstadoRecordatorio.etiqueta(): String = when (this) {
    EstadoRecordatorio.Pendiente -> "Pendiente"
    EstadoRecordatorio.EnProgreso -> "En progreso"
    is EstadoRecordatorio.Completado -> "Completado"
    is EstadoRecordatorio.Cancelado -> "Cancelado"
}

/**
 * Entidad principal. Todas las propiedades son `val` (inmutable): un cambio se
 * expresa creando una copia con `copy()`.
 * Los datos opcionales se modelan como tipos anulables (`?`), sin confundir
 * "dato ausente" con "cadena vacía".
 */
data class Recordatorio(
    val id: Long,
    val titulo: String,
    val categoria: Categoria,
    val prioridad: Prioridad,
    val estado: EstadoRecordatorio = EstadoRecordatorio.Pendiente,
    val descripcion: String? = null,
    val fechaLimite: LocalDate? = null,
    val ubicacion: String? = null,   // se usará en módulos posteriores (aviso por ubicación)
    val etiquetas: Set<String> = emptySet(),
)

/** Transición inmutable: devuelve un nuevo recordatorio en estado Completado. */
fun Recordatorio.completar(fecha: LocalDate): Recordatorio =
    copy(estado = EstadoRecordatorio.Completado(fecha))

/** Etiqueta visible y color (ARGB) asociados a cada categoría mediante un Map. */
data class EstiloCategoria(val etiqueta: String, val colorArgb: Long)

/** Resultado de validar un recordatorio: válido o inválido con la lista de errores. */
sealed class ResultadoValidacion {
    data class Valido(val recordatorio: Recordatorio) : ResultadoValidacion()
    data class Invalido(val titulo: String, val errores: List<String>) : ResultadoValidacion()
}

/** Estados posibles de la pantalla principal; evita combinar booleanos contradictorios. */
sealed class EstadoPantalla {
    object Cargando : EstadoPantalla()
    data class ConDatos(val recordatorios: List<Recordatorio>) : EstadoPantalla()
    object SinResultados : EstadoPantalla()
    data class Error(val mensaje: String) : EstadoPantalla()
}

/** Filtros que ofrece la pantalla. */
sealed class Filtro {
    object Todos : Filtro()
    object Pendientes : Filtro()
    object Completados : Filtro()
    object AltaPrioridad : Filtro()
}
