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
import com.kiberkot.dailysync.model.RegistroAnimo
import com.kiberkot.dailysync.ui.detalle.DetalleScreen
import com.kiberkot.dailysync.ui.login.LoginScreen
import com.kiberkot.dailysync.ui.login.RegisterScreen
import com.kiberkot.dailysync.ui.registro.EditarAnimoScreen
import com.kiberkot.dailysync.ui.registro.RegistroAnimoScreen
import com.kiberkot.dailysync.viewmodel.AnimoViewModel
import com.kiberkot.dailysync.viewmodel.LoginViewModel
import com.kiberkot.dailysync.viewmodel.RegisterViewModel
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import com.kiberkot.dailysync.ui.home.HomeScreen
import com.kiberkot.dailysync.viewmodel.ModuloHome
import com.kiberkot.dailysync.viewmodel.SeccionHome
import com.kiberkot.dailysync.ui.historial.HistorialScreen
import com.kiberkot.dailysync.viewmodel.HomeViewModel
import com.kiberkot.dailysync.model.AmbitoRegistro
import com.kiberkot.dailysync.viewmodel.HistorialViewModel
import com.kiberkot.dailysync.viewmodel.DbtViewModel
import com.kiberkot.dailysync.data.repository.DbtRepository
import com.kiberkot.dailysync.viewmodel.RegistroDbtViewModel
import com.kiberkot.dailysync.ui.dbt.CatalogoDbtScreen
import com.kiberkot.dailysync.ui.dbt.DetalleHabilidadDbtScreen
import com.kiberkot.dailysync.ui.dbt.RegistroDbtScreen
import com.kiberkot.dailysync.ui.dbt.DetalleRegistroDbtScreen

object Destinos {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val HOME = "home"
    const val REGISTRO = "registro"
    const val DETALLE = "detalle/{registroId}"
    const val EDITAR = "editar/{registroId}"
    const val HISTORIAL = "historial"
    fun crearRutaDetalle(id: String) = "detalle/$id"
    fun crearRutaEditar(id: String) = "editar/$id"

    const val DBT_CATALOGO = "dbt/catalogo"
    const val DBT_HABILIDAD = "dbt/habilidad/{habilidadId}"
    const val DBT_NUEVA = "dbt/practica/nueva/{habilidadId}"
    const val DBT_DETALLE = "dbt/practica/detalle/{registroId}"
    const val DBT_EDITAR = "dbt/practica/editar/{registroId}"
    fun crearRutaHabilidadDbt(id: Int) = "dbt/habilidad/$id"
    fun crearRutaNuevaPracticaDbt(id: Int) = "dbt/practica/nueva/$id"
    fun crearRutaDetalleDbt(id: String) = "dbt/practica/detalle/$id"
    fun crearRutaEditarDbt(id: String) = "dbt/practica/editar/$id"
}

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    animoViewModel: AnimoViewModel = viewModel(),
    loginViewModel: LoginViewModel = viewModel(),
    registerViewModel: RegisterViewModel = viewModel(),
    homeViewModel: HomeViewModel = viewModel(),
    dbtViewModel: DbtViewModel = viewModel()
) {
    val animoUiState by animoViewModel.uiState.collectAsState()
    val loginUiState by loginViewModel.uiState.collectAsState()
    val registerUiState by registerViewModel.uiState.collectAsState()
    val homeUiState by homeViewModel.uiState.collectAsState()
    val context = LocalContext.current
    val usuarioDbtId = DbtRepository.USUARIO_DEMO

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

        composable(route = Destinos.HISTORIAL) {
            val historialViewModel: HistorialViewModel = viewModel()
            val historialUiState by
            historialViewModel.uiState.collectAsState()

            LaunchedEffect(Unit) {
                historialViewModel.cargar("usr_demo_01")
            }

            HistorialScreen(
                state = historialUiState,

                onSeleccionarFiltro =
                    historialViewModel::seleccionarFiltro,

                onVerDetalle = { registro ->
                    if (registro.ambito == AmbitoRegistro.ANIMO) {
                        navController.navigate(
                            Destinos.crearRutaDetalle(registro.id)
                        )
                    } else if (registro.ambito == AmbitoRegistro.DBT) {
                        navController.navigate(Destinos.crearRutaDetalleDbt(registro.id))
                    } else {
                        Toast.makeText(
                            context,
                            "El detalle de este módulo está en preparación",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                },

                onEditar = { registro ->
                    if (registro.ambito == AmbitoRegistro.ANIMO) {
                        navController.navigate(
                            Destinos.crearRutaEditar(registro.id)
                        )
                    } else if (registro.ambito == AmbitoRegistro.DBT) {
                        navController.navigate(Destinos.crearRutaEditarDbt(registro.id))
                    }
                },

                onSolicitarEliminar =
                    historialViewModel::solicitarEliminacion,

                onCancelarEliminar =
                    historialViewModel::cancelarEliminacion,

                onConfirmarEliminar =
                    historialViewModel::confirmarEliminacion,

                onReintentar = {
                    historialViewModel.cargar("usr_demo_01")
                },

                onVolver = {
                    navController.popBackStack(
                        route = Destinos.HOME,
                        inclusive = false
                    )
                }
            )
        }

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
            LaunchedEffect(usuarioDbtId) {
                homeViewModel.cargar(usuarioDbtId)
            }
            HomeScreen(
                state = homeUiState,
                onReintentar = { homeViewModel.cargar(usuarioDbtId) },

                onAbrirModulo = { modulo ->
                    when (modulo) {
                        ModuloHome.ANIMO -> {
                            navController.navigate(Destinos.REGISTRO)
                        }

                        ModuloHome.DBT -> {
                            navController.navigate(Destinos.DBT_CATALOGO)
                        }

                        else -> {
                            Toast.makeText(
                                context,
                                "${modulo.titulo}: módulo en preparación",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                },

                onAbrirSeccion = { seccion ->
                    when (seccion) {
                        SeccionHome.INICIO -> Unit

                        SeccionHome.HISTORIAL -> {
                            navController.navigate(
                                Destinos.HISTORIAL
                            )
                        }

                        SeccionHome.CONTENIDOS,
                        SeccionHome.AJUSTES -> {
                            Toast.makeText(
                                context,
                                "Sección en preparación",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            )
        }

        composable(Destinos.DBT_CATALOGO) {
            val state by dbtViewModel.uiState.collectAsState()
            CatalogoDbtScreen(state, dbtViewModel::seleccionarModulo,
                onVerHabilidad = { navController.navigate(Destinos.crearRutaHabilidadDbt(it)) },
                onReintentar = dbtViewModel::cargarCatalogo, onVolver = { navController.popBackStack() })
        }
        composable(Destinos.DBT_HABILIDAD, arguments = listOf(navArgument("habilidadId") { type = NavType.IntType })) { entrada ->
            val state by dbtViewModel.uiState.collectAsState()
            DetalleHabilidadDbtScreen(state, entrada.arguments!!.getInt("habilidadId"),
                onRegistrarPractica = { navController.navigate(Destinos.crearRutaNuevaPracticaDbt(it)) },
                onReintentar = dbtViewModel::cargarCatalogo, onVolver = { navController.popBackStack() })
        }
        // Cada entrada de formulario tiene su propio ViewModel: sus campos sobreviven a rotación.
        listOf(Destinos.DBT_NUEVA, Destinos.DBT_EDITAR).forEach { ruta ->
            val editando = ruta == Destinos.DBT_EDITAR
            composable(ruta, arguments = if (editando) listOf(navArgument("registroId") { type = NavType.StringType })
                else listOf(navArgument("habilidadId") { type = NavType.IntType })) { entrada ->
                val vm: RegistroDbtViewModel = viewModel()
                val state by vm.uiState.collectAsState()
                val context = LocalContext.current
                val id = entrada.arguments?.getString("registroId")
                val habilidadId = if (editando) null else entrada.arguments?.getInt("habilidadId")
                LaunchedEffect(usuarioDbtId, id, habilidadId) { vm.cargar(usuarioDbtId, id, habilidadId) }
                RegistroDbtScreen(state, editando, vm::seleccionarHabilidad, vm::cambiarSituacion,
                    vm::cambiarIntensidad, vm::cambiarUtilidad,
                    onGuardar = {
                        vm.guardar { guardadoId ->
                            if (navController.currentBackStackEntry == entrada) {
                                Toast.makeText(context, "Práctica guardada localmente", Toast.LENGTH_SHORT).show()
                                if (editando) navController.popBackStack()
                                else navController.navigate(Destinos.crearRutaDetalleDbt(guardadoId)) {
                                    popUpTo(entrada.destination.id) { inclusive = true }
                                }
                            }
                        }
                    }, onReintentar = vm::reintentar, onVolver = { navController.popBackStack() })
            }
        }
        composable(Destinos.DBT_DETALLE, arguments = listOf(navArgument("registroId") { type = NavType.StringType })) { entrada ->
            val vm: RegistroDbtViewModel = viewModel()
            val state by vm.uiState.collectAsState()
            val id = requireNotNull(entrada.arguments?.getString("registroId"))
            LaunchedEffect(usuarioDbtId, id) { vm.cargar(usuarioDbtId, registroId = id) }
            DetalleRegistroDbtScreen(state, onEditar = { navController.navigate(Destinos.crearRutaEditarDbt(id)) },
                onEliminar = { vm.eliminar { if (navController.currentBackStackEntry == entrada) navController.popBackStack() } },
                onVerHistorial = {
                    navController.navigate(Destinos.HISTORIAL) { popUpTo(Destinos.HOME); launchSingleTop = true }
                }, onReintentar = vm::reintentar, onVolver = { navController.popBackStack() })
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
