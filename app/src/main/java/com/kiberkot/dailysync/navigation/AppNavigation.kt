package com.kiberkot.dailysync.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.kiberkot.dailysync.ui.login.LoginScreen
import com.kiberkot.dailysync.ui.login.RegisterScreen
import com.kiberkot.dailysync.ui.registro.EditarAnimoScreen
import com.kiberkot.dailysync.ui.registro.RegistroAnimoScreen
import com.kiberkot.dailysync.viewmodel.AnimoViewModel
import com.kiberkot.dailysync.viewmodel.LoginViewModel
import com.kiberkot.dailysync.viewmodel.RegisterViewModel

object Destinos {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val HOME = "home"
    const val REGISTRO = "registro"
    const val DETALLE = "detalle/{registroId}"
    const val EDITAR = "editar/{registroId}"
    fun crearRutaDetalle(id: String) = "detalle/$id"
    fun crearRutaEditar(id: String) = "editar/$id"
}

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    animoViewModel: AnimoViewModel = viewModel(),
    loginViewModel: LoginViewModel = viewModel(),
    registerViewModel: RegisterViewModel = viewModel()
) {
    val animoUiState by animoViewModel.uiState.collectAsState()
    val loginUiState by loginViewModel.uiState.collectAsState()
    val registerUiState by registerViewModel.uiState.collectAsState()

    // Redirección al Home al autenticarse correctamente
    LaunchedEffect(loginUiState.loginExitoso) {
        if (loginUiState.loginExitoso) {
            navController.navigate(Destinos.HOME) {
                popUpTo(Destinos.LOGIN) { inclusive = true }
            }
            loginViewModel.resetearEstadoLogin()
        }
    }

    // Redirección al Login al registrarse correctamente
    LaunchedEffect(registerUiState.registroExitoso) {
        if (registerUiState.registroExitoso) {
            navController.popBackStack()
            registerViewModel.resetearEstado()
        }
    }

    NavHost(
        navController = navController,
        startDestination = Destinos.LOGIN
    ) {
        composable(route = Destinos.LOGIN) {
            LoginScreen(
                state = loginUiState,
                onCambioUsuario = loginViewModel::cambiarUsuario,
                onCambioPassword = loginViewModel::cambiarPassword,
                onTogglePasswordVisibility = loginViewModel::conmutarVisibilidadPassword,
                onIniciarSesion = { loginViewModel.ejecutarLogin() },
                onIrARegistro = { navController.navigate(Destinos.REGISTER) }
            )
        }

        composable(route = Destinos.REGISTER) {
            RegisterScreen(
                state = registerUiState,
                onCambioId = registerViewModel::cambiarIdUsuario,
                onCambioAlias = registerViewModel::cambiarAlias,
                onCambioCorreo = registerViewModel::cambiarCorreo,
                onCambioPassword = registerViewModel::cambiarPassword,
                onTogglePasswordVisibility = registerViewModel::conmutarVisibilidadPassword,
                onRegistrar = { registerViewModel.registrarUsuario() },
                onVolverLogin = { navController.popBackStack() }
            )
        }

        composable(route = Destinos.HOME) {
            HomeScreen(
                historial = animoUiState.historial,
                onNuevoRegistro = { navController.navigate(Destinos.REGISTRO) },
                onVerDetalle = { id: String -> navController.navigate(Destinos.crearRutaDetalle(id)) }
            )
        }

        composable(route = Destinos.REGISTRO) {
            RegistroAnimoScreen(
                state = animoUiState,
                onSeleccionarCategoria = animoViewModel::seleccionarCategoria,
                onCambioIntensidad = animoViewModel::cambiarIntensidad,
                onCambioContexto = animoViewModel::cambiarContexto,
                onCambioNota = animoViewModel::cambiarNota,
                onGuardar = {
                    animoViewModel.guardarRegistro(
                        onExito = { navController.popBackStack() }
                    )
                },
                onOcultarBanner = animoViewModel::ocultarBanner,
                onVolver = { navController.popBackStack() }
            )
        }

        composable(
            route = Destinos.DETALLE,
            arguments = listOf(navArgument("registroId") { type = NavType.StringType })
        ) { backStackEntry ->
            val registroId = backStackEntry.arguments?.getString("registroId")
            val registroEncontrado: RegistroAnimo? =
                animoUiState.historial.find { reg: RegistroAnimo -> reg.id == registroId }

            DetalleScreen(
                registro = registroEncontrado,
                onVolver = { navController.popBackStack() },
                onEliminar = { id ->
                    animoViewModel.eliminarRegistro(id) {
                        navController.popBackStack()
                    }
                },
                onEditar = { id ->
                    navController.navigate(Destinos.crearRutaEditar(id))
                }
            )
        }

        composable(
            route = Destinos.EDITAR,
            arguments = listOf(navArgument("registroId") { type = NavType.StringType })
        ) { backStackEntry ->
            val registroId = backStackEntry.arguments?.getString("registroId")
            val registroEncontrado = animoUiState.historial.find { it.id == registroId }

            EditarAnimoScreen(
                registro = registroEncontrado,
                categorias = animoUiState.categorias,
                onVolver = { navController.popBackStack() },
                onGuardarCambios = { cat, intensidad, contexto, nota ->
                    if (registroEncontrado != null) {
                        animoViewModel.actualizarRegistro(
                            idRegistro = registroEncontrado.id,
                            fechaHoraOriginal = registroEncontrado.fechaHora,
                            categoria = cat,
                            intensidad = intensidad,
                            contexto = contexto,
                            nota = nota,
                            onExito = {
                                navController.popBackStack()
                            }
                        )
                    } else false
                }
            )
        }
    }
}
