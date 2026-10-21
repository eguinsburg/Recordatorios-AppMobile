package com.example.recordatorios

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.recordatorios.data.RepositorioSimulado
import com.example.recordatorios.ui.RecordatoriosScreen
import com.example.recordatorios.ui.theme.RecordatoriosTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Evidencia en Logcat (filtrar por "Recordatorios").
        Log.d(TAG, "Recordatorios válidos: ${RepositorioSimulado.recordatorios.size}")
        RepositorioSimulado.descartados.forEach {
            Log.w(TAG, "Descartado '${it.titulo}': ${it.errores.joinToString()}")
        }
        enableEdgeToEdge()
        setContent {
            RecordatoriosTheme {
                RecordatoriosScreen()
            }
        }
    }

    private companion object {
        const val TAG = "Recordatorios"
    }
}
