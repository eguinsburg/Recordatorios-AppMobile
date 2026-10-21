package com.example.recordatorios.data

import com.example.recordatorios.domain.ValidadorRecordatorio
import java.time.LocalDate

/**
 * Fuente de datos simulados. Usa las tres colecciones de Kotlin:
 *  - List: los recordatorios, en orden y de solo lectura.
 *  - Set: las categorías en uso, sin repetidos.
 *  - Map: cada categoría asociada a su etiqueta y color.
 */
object RepositorioSimulado {

    private val hoy: LocalDate = LocalDate.now()

    /** Map: categoría -> etiqueta visible y color. */
    val estilosPorCategoria: Map<Categoria, EstiloCategoria> = mapOf(
        Categoria.Trabajo to EstiloCategoria("💼 Trabajo", 0xFF1E88E5),
        Categoria.Personal to EstiloCategoria("🙂 Personal", 0xFF8E24AA),
        Categoria.Salud to EstiloCategoria("❤️ Salud", 0xFFE53935),
        Categoria.Estudio to EstiloCategoria("📚 Estudio", 0xFF43A047),
        Categoria.Hogar to EstiloCategoria("🏠 Hogar", 0xFFFB8C00),
    )

    /** Borradores tal como llegarían de un formulario: algunos no cumplen las reglas. */
    private val borradores: List<Recordatorio> = listOf(
        Recordatorio(
            id = 1, titulo = "Entregar informe trimestral", categoria = Categoria.Trabajo,
            prioridad = Prioridad.Alta, fechaLimite = hoy.plusDays(2),
            descripcion = "Enviar al equipo de gestión", etiquetas = setOf("oficina", "urgente"),
        ),
        Recordatorio(
            id = 2, titulo = "Turno con el dentista", categoria = Categoria.Salud,
            prioridad = Prioridad.Media, fechaLimite = hoy.plusDays(5),
            ubicacion = "Clínica Centro",
        ),
        Recordatorio(
            id = 3, titulo = "Repasar sealed classes", categoria = Categoria.Estudio,
            prioridad = Prioridad.Alta, estado = EstadoRecordatorio.EnProgreso,
            descripcion = "Módulo 1 - Tema 2",
        ),
        Recordatorio(
            id = 4, titulo = "Comprar regalo de cumpleaños", categoria = Categoria.Personal,
            prioridad = Prioridad.Baja, estado = EstadoRecordatorio.Completado(hoy.minusDays(1)),
        ),
        Recordatorio(
            id = 5, titulo = "Pagar factura de luz", categoria = Categoria.Hogar,
            prioridad = Prioridad.Alta, fechaLimite = hoy.plusDays(1), etiquetas = setOf("pagos"),
        ),
        Recordatorio(
            id = 6, titulo = "Reunión de planificación", categoria = Categoria.Trabajo,
            prioridad = Prioridad.Media, estado = EstadoRecordatorio.Completado(hoy),
        ),
        Recordatorio(
            id = 7, titulo = "Configurar emulador Pixel", categoria = Categoria.Estudio,
            prioridad = Prioridad.Baja, estado = EstadoRecordatorio.Cancelado(motivo = null),
        ),
        Recordatorio(
            id = 8, titulo = "Salir a correr 30 minutos", categoria = Categoria.Salud,
            prioridad = Prioridad.Baja, ubicacion = "Parque",
        ),
        // --- Borradores inválidos: la validación los descarta ---
        Recordatorio(id = 9, titulo = "  ab ", categoria = Categoria.Personal, prioridad = Prioridad.Media),
        Recordatorio(
            id = 10, titulo = "Renovar licencia", categoria = Categoria.Personal,
            prioridad = Prioridad.Alta, descripcion = "   ", fechaLimite = hoy.minusDays(3),
        ),
        Recordatorio(id = 0, titulo = "", categoria = Categoria.Trabajo, prioridad = Prioridad.Baja),
    )

    private val resultados: List<ResultadoValidacion> =
        borradores.map { ValidadorRecordatorio.validar(it, hoy) }

    /** List de solo lectura con los recordatorios que superaron la validación. */
    val recordatorios: List<Recordatorio> = resultados
        .filterIsInstance<ResultadoValidacion.Valido>()
        .map { it.recordatorio }
        .sortedByDescending { it.prioridad.nivel }

    /** Borradores rechazados, con sus errores, para mostrarlos como evidencia. */
    val descartados: List<ResultadoValidacion.Invalido> =
        resultados.filterIsInstance<ResultadoValidacion.Invalido>()

    /** Set: categorías utilizadas, sin repetidos (Trabajo/Salud/Estudio aparecen dos veces). */
    val categoriasEnUso: Set<Categoria> = recordatorios.map { it.categoria }.toSet()

    /** Map: cantidad de recordatorios por categoría. */
    val cantidadPorCategoria: Map<Categoria, Int> =
        recordatorios.groupingBy { it.categoria }.eachCount()
}

/** Filtrado por estado o prioridad: devuelve una nueva lista, no modifica la original. */
fun List<Recordatorio>.filtrar(filtro: Filtro): List<Recordatorio> = when (filtro) {
    Filtro.Todos -> this
    Filtro.Pendientes -> filter {
        it.estado == EstadoRecordatorio.Pendiente || it.estado == EstadoRecordatorio.EnProgreso
    }
    Filtro.Completados -> filter { it.estado is EstadoRecordatorio.Completado }
    Filtro.AltaPrioridad -> filter { it.prioridad == Prioridad.Alta }
}
