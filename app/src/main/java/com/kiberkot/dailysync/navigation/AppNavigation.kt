package com.kiberkot.dailysync.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.kiberkot.dailysync.model.CategoriaAnimo
import com.kiberkot.dailysync.model.RegistroAnimo
import com.kiberkot.dailysync.ui.detalle.DetalleScreen
import com.kiberkot.dailysync.ui.home.HomeScreen
import com.kiberkot.dailysync.ui.registro.RegistroAnimoScreen
import com.kiberkot.dailysync.viewmodel.AnimoViewModel

object Destinos {
    const val HOME = "home"
    const val REGISTRO = "registro"
    const val DETALLE = "detalle/{registroId}"
    fun crearRutaDetalle(registroId: String): String = "detalle/$registroId"
}

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    viewModel: AnimoViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    NavHost(
        navController = navController,
        startDestination = Destinos.HOME
    ) {
        composable(Destinos.HOME) {
            HomeScreen(
                historial = uiState.historial,
                onNuevoRegistro = { navController.navigate(Destinos.REGISTRO) },
                onVerDetalle = { id: String -> navController.navigate(Destinos.crearRutaDetalle(id)) }
            )
        }

        composable(Destinos.REGISTRO) {
            RegistroAnimoScreen(
                state = uiState,
                onSeleccionarCategoria = viewModel::seleccionarCategoria,
                onCambioIntensidad = viewModel::cambiarIntensidad,
                onCambioContexto = viewModel::cambiarContexto,
                onCambioNota = viewModel::cambiarNota,
                onGuardar = {
                    viewModel.guardarRegistro(
                        onExito = {
                            navController.popBackStack()
                        }
                    )
                },
                onOcultarBanner = viewModel::ocultarBanner,
                onVolver = { navController.popBackStack() }
            )
        }

        composable(
            route = Destinos.DETALLE,
            arguments = listOf(navArgument("registroId") { type = NavType.StringType })
        ) { backStackEntry: NavBackStackEntry ->
            val registroId = backStackEntry.arguments?.getString("registroId")
            val registroEncontrado: RegistroAnimo? = uiState.historial.find { reg: RegistroAnimo -> reg.id == registroId }

            DetalleScreen(
                registro = registroEncontrado,
                onVolver = { navController.popBackStack() }
            )
        }
    }
}