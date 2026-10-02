package com.kiberkot.dailysync.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.kiberkot.dailysync.ui.detalle.DetalleScreen
import com.kiberkot.dailysync.ui.home.HomeScreen
import com.kiberkot.dailysync.ui.registro.RegistroAnimoScreen
import com.kiberkot.dailysync.viewmodel.AnimoViewModel

// Rutas estables de navegación
object Destinos {
    const val HOME = "home"
    const val REGISTRO = "registro"
    const val DETALLE = "detalle/{registroId}"
    fun crearRutaDetalle(id: String) = "detalle/$id"
}

@Composable
fun DailySyncApp(
    viewModel: AnimoViewModel = viewModel()
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Destinos.HOME
    ) {
        // PANTALLA 1: Home / Historial
        composable(Destinos.HOME) {
            HomeScreen(
                historial = viewModel.historialAnimo,
                onNuevoRegistro = { navController.navigate(Destinos.REGISTRO) },
                onVerDetalle = { id -> navController.navigate(Destinos.crearRutaDetalle(id)) }
            )
        }

        // PANTALLA 2: Formulario RF12
        composable(Destinos.REGISTRO) {
            RegistroAnimoScreen(
                state = viewModel.uiState,
                onSeleccionarCategoria = viewModel::seleccionarCategoria,
                onCambioIntensidad = viewModel::actualizarIntensidad,
                onCambioContexto = viewModel::actualizarContexto,
                onCambioNota = viewModel::actualizarNota,
                onGuardar = {
                    val nuevoId = viewModel.guardarRegistro()
                    if (nuevoId != null) {
                        // Navega al detalle del registro recién creado
                        navController.navigate(Destinos.crearRutaDetalle(nuevoId)) {
                            popUpTo(Destinos.HOME)
                        }
                    }
                }
            )
        }

        // PANTALLA 3: Detalle por ID
        composable(
            route = Destinos.DETALLE,
            arguments = listOf(navArgument("registroId") { type = NavType.StringType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("registroId") ?: ""
            DetalleScreen(
                registro = viewModel.buscarPorId(id),
                onVolver = { navController.popBackStack() }
            )
        }
    }
}