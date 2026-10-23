package com.example.recordatorios

import android.app.Application
import com.example.recordatorios.data.RepositorioEnMemoria
import com.example.recordatorios.data.RepositorioRecordatorios
import com.example.recordatorios.data.SimuladorEstados

/**
 * Contenedor de dependencias de la aplicación. Una única instancia del repositorio en
 * memoria se comparte entre pantallas y se expone tipada por sus interfaces.
 */
class RecordatoriosApp : Application() {
    private val repositorioEnMemoria by lazy { RepositorioEnMemoria() }
    val repositorio: RepositorioRecordatorios get() = repositorioEnMemoria
    val simulador: SimuladorEstados get() = repositorioEnMemoria
}
