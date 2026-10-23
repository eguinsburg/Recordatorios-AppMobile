package com.example.recordatorios.ui

import com.example.recordatorios.data.Categoria
import com.example.recordatorios.data.NuevoRecordatorio
import com.example.recordatorios.data.Prioridad
import com.example.recordatorios.data.Recordatorio
import com.example.recordatorios.data.RepositorioRecordatorios
import com.example.recordatorios.data.RepositorioSimulado
import com.example.recordatorios.data.SimuladorEstados
import com.example.recordatorios.ui.crear.CrearViewModel
import com.example.recordatorios.ui.crear.EstadoEnvio
import com.example.recordatorios.ui.lista.ListaUiState
import com.example.recordatorios.ui.lista.ListaViewModel
import com.example.recordatorios.ui.lista.Simulacion
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException

/** Repositorio falso: los ViewModel solo conocen la interfaz, así que se puede reemplazar. */
private class RepositorioFalso : RepositorioRecordatorios, SimuladorEstados {
    val datos = MutableStateFlow(RepositorioSimulado.recordatorios)
    var fallarCarga = false
    var fallarGuardado = false

    override fun observarRecordatorios(): Flow<List<Recordatorio>> = flow {
        delay(100)
        if (fallarCarga) { fallarCarga = false; throw IOException("sin conexión") }
        emitAll(datos)
    }

    override suspend fun obtenerPorId(id: Long) = datos.value.firstOrNull { it.id == id }

    override suspend fun guardar(nuevo: NuevoRecordatorio): Recordatorio {
        delay(100)
        if (fallarGuardado) throw IOException("falló el guardado")
        val r = Recordatorio(99, nuevo.titulo, nuevo.categoria, nuevo.prioridad, descripcion = nuevo.descripcion)
        datos.update { it + r }
        return r
    }

    override fun restaurarDatosDeEjemplo() = datos.update { RepositorioSimulado.recordatorios }
    override fun vaciar() = datos.update { emptyList() }
    override fun fallarProximaCarga() { fallarCarga = true }
    override fun fallarProximoGuardado() { fallarGuardado = true }
}

@OptIn(ExperimentalCoroutinesApi::class)
class ViewModelsTest {

    private val dispatcher = StandardTestDispatcher()
    private lateinit var repo: RepositorioFalso

    @Before fun setUp() {
        Dispatchers.setMain(dispatcher)
        repo = RepositorioFalso()
    }

    @After fun tearDown() = Dispatchers.resetMain()

    @Test fun listado_pasaDeCargaADatos() = runTest(dispatcher) {
        val vm = ListaViewModel(repo, repo)
        assertEquals(ListaUiState.Cargando, vm.uiState.value)
        advanceUntilIdle()
        assertTrue(vm.uiState.value is ListaUiState.ConDatos)
    }

    @Test fun listado_vacioYErrorConReintento() = runTest(dispatcher) {
        val vm = ListaViewModel(repo, repo)
        vm.simular(Simulacion.ListaVacia); advanceUntilIdle()
        assertEquals(ListaUiState.Vacio, vm.uiState.value)

        vm.simular(Simulacion.DatosDeEjemplo)
        vm.simular(Simulacion.ErrorAlCargar); advanceUntilIdle()
        assertTrue(vm.uiState.value is ListaUiState.Error)

        vm.reintentar(); advanceUntilIdle()
        assertTrue(vm.uiState.value is ListaUiState.ConDatos)
    }

    @Test fun formulario_validaTituloYDeshabilitaGuardado() {
        val vm = CrearViewModel(repo)
        vm.onTituloChange("  ab  ")
        assertNotNull(vm.uiState.value.errores.titulo)
        assertFalse(vm.uiState.value.puedeGuardar)

        vm.onTituloChange("Comprar pan")
        vm.onDescripcionChange("x".repeat(201))
        assertNotNull(vm.uiState.value.errores.descripcion)

        vm.onDescripcionChange("Antes de las 18")
        vm.onCategoriaSeleccionada(Categoria.Hogar)
        assertFalse("falta la prioridad", vm.uiState.value.puedeGuardar)
        vm.onPrioridadSeleccionada(Prioridad.Media)
        assertNull(vm.uiState.value.errores.titulo)
        assertTrue(vm.uiState.value.puedeGuardar)
    }

    @Test fun formulario_guardaConEstadoDeEnvioYExito() = runTest(dispatcher) {
        val vm = completarFormulario(CrearViewModel(repo))
        vm.guardar()
        assertEquals(EstadoEnvio.Enviando, vm.uiState.value.envio)
        assertFalse("botón deshabilitado durante el envío", vm.uiState.value.puedeGuardar)
        advanceUntilIdle()
        assertEquals(EstadoEnvio.Exito(99), vm.uiState.value.envio)
        assertTrue(repo.datos.value.any { it.titulo == "Comprar pan" })
    }

    @Test fun formulario_errorGeneralDeGuardado() = runTest(dispatcher) {
        repo.fallarGuardado = true
        val vm = completarFormulario(CrearViewModel(repo))
        vm.guardar(); advanceUntilIdle()
        assertTrue(vm.uiState.value.envio is EstadoEnvio.Error)
        assertTrue("se puede volver a intentar", vm.uiState.value.puedeGuardar)
    }

    private fun completarFormulario(vm: CrearViewModel) = vm.apply {
        onTituloChange("  Comprar pan ")
        onCategoriaSeleccionada(Categoria.Hogar)
        onPrioridadSeleccionada(Prioridad.Media)
    }
}
