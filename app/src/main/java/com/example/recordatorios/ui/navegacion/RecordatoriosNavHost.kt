package com.example.recordatorios.ui.navegacion

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.dropUnlessResumed
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.recordatorios.ui.crear.CrearScreen
import com.example.recordatorios.ui.detalle.DetalleScreen
import com.example.recordatorios.ui.lista.ListaScreen
import kotlinx.serialization.Serializable

// Rutas tipadas: los destinos sin argumentos son objects; el detalle transporta solo el id.
@Serializable
data object ListaRoute

@Serializable
data object CrearRoute

@Serializable
data class DetalleRoute(val id: Long)

/**
 * Grafo de navegación. El NavController vive aquí, en el nivel más alto que lo necesita;
 * las pantallas solo reciben lambdas y nunca el controlador.
 */
@Composable
fun RecordatoriosNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    NavHost(navController = navController, startDestination = ListaRoute, modifier = modifier) {
        composable<ListaRoute> {
            ListaScreen(
                onCrearRecordatorio = dropUnlessResumed { navController.navigate(CrearRoute) },
                onRecordatorioClick = { id -> navController.navigate(DetalleRoute(id)) { launchSingleTop = true } },
            )
        }
        composable<CrearRoute> {
            // popBackStack() retira el formulario y vuelve al listado existente,
            // sin agregar otra copia del listado al historial.
            CrearScreen(
                onGuardado = { navController.popBackStack() },
                onCancelar = dropUnlessResumed { navController.popBackStack() },
            )
        }
        composable<DetalleRoute> { entrada ->
            val ruta = entrada.toRoute<DetalleRoute>()
            DetalleScreen(
                id = ruta.id,
                onVolver = dropUnlessResumed { navController.popBackStack() },
            )
        }
    }
}
