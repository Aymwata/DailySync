package com.kiberkot.dailysync.navigation

import kotlinx.coroutines.delay
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.kiberkot.dailysync.model.AmbitoRegistro
import com.kiberkot.dailysync.ui.adiccion.AdiccionScreen
import com.kiberkot.dailysync.ui.adiccion.DetalleAdiccionScreen
import com.kiberkot.dailysync.ui.dbt.CatalogoDbtScreen
import com.kiberkot.dailysync.ui.dbt.DetalleHabilidadDbtScreen
import com.kiberkot.dailysync.ui.dbt.DetalleRegistroDbtScreen
import com.kiberkot.dailysync.ui.dbt.RegistroDbtScreen
import com.kiberkot.dailysync.ui.historial.HistorialScreen
import com.kiberkot.dailysync.ui.home.HomeScreen
import com.kiberkot.dailysync.ui.login.LoginScreen
import com.kiberkot.dailysync.ui.login.InicioSesionScreen
import com.kiberkot.dailysync.ui.login.RegisterScreen
import com.kiberkot.dailysync.ui.neuro.DetalleNeuroScreen
import com.kiberkot.dailysync.ui.neuro.NeuroScreen
import com.kiberkot.dailysync.ui.registro.DetalleAnimoScreen
import com.kiberkot.dailysync.ui.registro.RegistroAnimoScreen
import com.kiberkot.dailysync.viewmodel.*

object Destinos {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val HOME = "home"
    const val REGISTRO = "registro"
    const val HISTORIAL = "historial"

    const val NEURO = "neuro"
    const val NEURO_EDITAR = "neuro/editar/{registroId}"
    const val NEURO_DETALLE = "neuro/detalle/{registroId}"
    fun crearRutaEditarNeuro(id: String) = "neuro/editar/$id"
    fun crearRutaDetalleNeuro(id: String) = "neuro/detalle/$id"

    const val ADICCION = "adiccion"
    const val ADICCION_EDITAR = "adiccion/editar/{registroId}"
    const val ADICCION_DETALLE = "adiccion/detalle/{registroId}"
    fun crearRutaEditarAdiccion(id: String) = "adiccion/editar/$id"
    fun crearRutaDetalleAdiccion(id: String) = "adiccion/detalle/$id"

    const val DETALLE = "detalle/{registroId}"
    const val EDITAR = "editar/{registroId}"
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
fun AppNavigation(loginViewModel: LoginViewModel = viewModel()) {
    val loginUiState by loginViewModel.uiState.collectAsState()
    if (loginUiState.restaurandoSesion || loginUiState.errorSesion != null) {
        InicioSesionScreen(loginUiState.errorSesion, loginViewModel::restaurarSesion)
        return
    }
    val context = LocalContext.current
    LaunchedEffect(loginUiState.error, loginUiState.usuarioId) {
        if (loginUiState.usuarioId != null && loginUiState.error != null) {
            Toast.makeText(context, loginUiState.error, Toast.LENGTH_LONG).show()
        }
    }
    val usuarioId = loginUiState.usuarioId
    if (usuarioId == null) {
        val navController = rememberNavController()
        NavHost(navController, startDestination = Destinos.LOGIN,
            enterTransition = { entradaRapida() }, exitTransition = { ExitTransition.None },
            popEnterTransition = { EnterTransition.None }, popExitTransition = { ExitTransition.None }) {
            composable(Destinos.LOGIN) { entrada ->
                LoginScreen(state = loginUiState,
                    onCambioUsuario = loginViewModel::cambiarUsuario,
                    onCambioPassword = loginViewModel::cambiarPassword,
                    onTogglePasswordVisibility = loginViewModel::conmutarVisibilidadPassword,
                    onIniciarSesion = loginViewModel::ejecutarLogin,
                    onAccesoPrueba = {
                        loginViewModel.cambiarUsuario(com.kiberkot.dailysync.data.local.AppDatabase.USUARIO_DEMO)
                        loginViewModel.cambiarPassword("1234")
                        loginViewModel.ejecutarLogin()
                    },
                    onIrARegistro = { navController.navegarDesde(entrada, Destinos.REGISTER) })
            }
            composable(Destinos.REGISTER) { entrada ->
                val vm: RegisterViewModel = viewModel()
                val state by vm.uiState.collectAsState()
                LaunchedEffect(state.registroExitoso) {
                    if (state.registroExitoso && navController.currentBackStackEntry == entrada) navController.popBackStack()
                }
                RegisterScreen(state = state, onCambioId = vm::cambiarIdUsuario,
                    onCambioAlias = vm::cambiarAlias, onCambioCorreo = vm::cambiarCorreo,
                    onCambioPassword = vm::cambiarPassword, onTogglePasswordVisibility = vm::conmutarVisibilidadPassword,
                    onRegistrar = vm::registrarUsuario, onVolverLogin = { navController.volverDesde(entrada) })
            }
        }
    } else {
        // Las rutas privadas solo se componen mientras existe una cuenta autenticada.
        key(usuarioId) { NavegacionSesion(usuarioId, loginViewModel::cerrarSesion) }
    }
}

@Composable
private fun NavegacionSesion(usuarioId: String, onCerrarSesion: () -> Unit) {
    val navController = rememberNavController()
    val context = LocalContext.current
    NavHost(navController = navController, startDestination = Destinos.HOME,
        enterTransition = { entradaRapida() }, exitTransition = { ExitTransition.None },
        // Regreso inmediato: una pantalla que sale no debe tapar los toques del Home.
        popEnterTransition = { EnterTransition.None }, popExitTransition = { ExitTransition.None }) {
        composable(route = Destinos.HOME) { entrada ->
            val homeViewModel: HomeViewModel = viewModel()
            val homeUiState by homeViewModel.uiState.collectAsState()
            LaunchedEffect(usuarioId) {
                homeViewModel.cargar(usuarioId)
            }
            HomeScreen(
                state = homeUiState,
                onCerrarSesion = {
                    navController.popBackStack(navController.graph.id, inclusive = true)
                    onCerrarSesion()
                },
                onReintentar = { homeViewModel.cargar(usuarioId) },
                onAbrirModulo = { modulo ->
                    when (modulo) {
                        ModuloHome.ANIMO -> navController.navegarDesde(entrada, Destinos.REGISTRO)
                        ModuloHome.DBT -> navController.navegarDesde(entrada, Destinos.DBT_CATALOGO)
                        ModuloHome.NEURODESARROLLO -> navController.navegarDesde(entrada, Destinos.NEURO)
                        ModuloHome.ADICCIONES -> navController.navegarDesde(entrada, Destinos.ADICCION)
                    }
                },
                onAbrirSeccion = { seccion ->
                    when (seccion) {
                        SeccionHome.INICIO -> Unit
                        SeccionHome.HISTORIAL -> navController.navegarDesde(entrada, Destinos.HISTORIAL)
                        SeccionHome.CONTENIDOS,
                        SeccionHome.AJUSTES -> {
                            Toast.makeText(context, "Sección en preparación", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            )
        }

        listOf(Destinos.REGISTRO, Destinos.EDITAR).forEach { ruta ->
            val editando = ruta == Destinos.EDITAR
            composable(ruta, arguments = if (editando) listOf(navArgument("registroId") { type = NavType.StringType }) else emptyList()) { entrada ->
                val vm: AnimoViewModel = viewModel()
                val state by vm.uiState.collectAsState()
                val id = entrada.arguments?.getString("registroId")
                LaunchedEffect(usuarioId, id) { vm.cargar(usuarioId, id) }
                RegistroAnimoScreen(state = state, editando = editando,
                    onSeleccionarCategoria = vm::seleccionarCategoria,
                    onCambioIntensidad = vm::cambiarIntensidad, onCambioContexto = vm::cambiarContexto,
                    onCambioNota = vm::cambiarNota,
                    onGuardar = { vm.guardarRegistro {
                        if (navController.currentBackStackEntry == entrada) navController.popBackStack()
                    } },
                    onReintentar = { vm.cargar(usuarioId, id) },
                    onVolver = { navController.volverDesde(entrada) })
            }
        }
        composable(Destinos.DETALLE, arguments = listOf(navArgument("registroId") { type = NavType.StringType })) { entrada ->
            val vm: AnimoViewModel = viewModel()
            val state by vm.uiState.collectAsState()
            val id = entrada.arguments?.getString("registroId").orEmpty()
            LaunchedEffect(usuarioId, id) { vm.cargar(usuarioId, id) }
            DetalleAnimoScreen(state = state, registroId = id,
                onEditar = { navController.navegarDesde(entrada, Destinos.crearRutaEditar(id)) },
                onReintentar = { vm.cargar(usuarioId, id) }, onVolver = { navController.volverDesde(entrada) })
        }

        // --- NEURODESARROLLO (CREAR / EDITAR / DETALLE) ---
        listOf(Destinos.NEURO, Destinos.NEURO_EDITAR).forEach { ruta ->
            val editando = ruta == Destinos.NEURO_EDITAR
            composable(
                route = ruta,
                arguments = if (editando) listOf(navArgument("registroId") { type = NavType.StringType }) else emptyList()
            ) { entrada ->
                val neuroViewModel: NeuroViewModel = viewModel()
                val neuroUiState by neuroViewModel.uiState.collectAsState()
                val id = entrada.arguments?.getString("registroId")
                LaunchedEffect(neuroUiState.guardadoExitoso) {
                    if (neuroUiState.guardadoExitoso) {
                        delay(1000L)
                        if (navController.currentBackStackEntry == entrada) navController.popBackStack()
                    }
                }
                LaunchedEffect(usuarioId, id) {
                    neuroViewModel.cargar(usuarioId, id)
                }

                NeuroScreen(
                    state = neuroUiState,
                    editando = editando,
                    onCambioTipoActividad = neuroViewModel::cambiarTipoActividad,
                    onCambioSobrecarga = neuroViewModel::cambiarNivelSobrecarga,
                    onSeleccionarEstrategia = neuroViewModel::seleccionarEstrategia,
                    onCambioContexto = neuroViewModel::cambiarContexto,
                    onCambioResultado = neuroViewModel::cambiarResultadoPercibido,
                    onGuardar = { neuroViewModel.guardarRegistro(usuarioId) },
                    onReintentar = { neuroViewModel.cargar(usuarioId, id) },
                    onVolver = { navController.volverDesde(entrada) }
                )
            }
        }

        composable(
            route = Destinos.NEURO_DETALLE,
            arguments = listOf(navArgument("registroId") { type = NavType.StringType })
        ) { entrada ->
            val neuroViewModel: NeuroViewModel = viewModel()
            val neuroUiState by neuroViewModel.uiState.collectAsState()
            val id = entrada.arguments?.getString("registroId") ?: ""
            LaunchedEffect(usuarioId, id) {
                neuroViewModel.cargar(usuarioId, id, soloLectura = true)
            }

            DetalleNeuroScreen(
                state = neuroUiState,
                onReintentar = { neuroViewModel.cargar(usuarioId, id, soloLectura = true) },
                onEditar = { navController.navegarDesde(entrada, Destinos.crearRutaEditarNeuro(id)) },
                onVolver = { navController.volverDesde(entrada) }
            )
        }

        // --- ADICCIONES / CRAVING (CREAR / EDITAR / DETALLE) ---
        listOf(Destinos.ADICCION, Destinos.ADICCION_EDITAR).forEach { ruta ->
            val editando = ruta == Destinos.ADICCION_EDITAR
            composable(
                route = ruta,
                arguments = if (editando) listOf(navArgument("registroId") { type = NavType.StringType }) else emptyList()
            ) { entrada ->
                val adiccionViewModel: AdiccionViewModel = viewModel()
                val adiccionUiState by adiccionViewModel.uiState.collectAsState()
                val id = entrada.arguments?.getString("registroId")
                LaunchedEffect(adiccionUiState.guardadoExitoso) {
                    if (adiccionUiState.guardadoExitoso) {
                        delay(1000L)
                        if (navController.currentBackStackEntry == entrada) navController.popBackStack()
                    }
                }
                LaunchedEffect(usuarioId, id) {
                    adiccionViewModel.cargar(usuarioId, id)
                }

                AdiccionScreen(
                    state = adiccionUiState,
                    editando = editando,
                    onCambioImpulso = adiccionViewModel::cambiarNivelImpulso,
                    onCambioDesencadenante = adiccionViewModel::cambiarDesencadenante,
                    onSeleccionarAccion = adiccionViewModel::seleccionarAccion,
                    onCambioEstadoObjetivo = adiccionViewModel::cambiarEstadoObjetivo,
                    onGuardar = { adiccionViewModel.guardarRegistro(usuarioId) },
                    onReintentar = { adiccionViewModel.cargar(usuarioId, id) },
                    onVolver = { navController.volverDesde(entrada) }
                )
            }
        }

        composable(
            route = Destinos.ADICCION_DETALLE,
            arguments = listOf(navArgument("registroId") { type = NavType.StringType })
        ) { entrada ->
            val adiccionViewModel: AdiccionViewModel = viewModel()
            val adiccionUiState by adiccionViewModel.uiState.collectAsState()
            val id = entrada.arguments?.getString("registroId") ?: ""
            LaunchedEffect(usuarioId, id) {
                adiccionViewModel.cargar(usuarioId, id, soloLectura = true)
            }

            DetalleAdiccionScreen(
                state = adiccionUiState,
                onReintentar = { adiccionViewModel.cargar(usuarioId, id, soloLectura = true) },
                onEditar = { navController.navegarDesde(entrada, Destinos.crearRutaEditarAdiccion(id)) },
                onVolver = { navController.volverDesde(entrada) }
            )
        }

        composable(route = Destinos.HISTORIAL) { entrada ->
            val historialViewModel: HistorialViewModel = viewModel()
            val historialUiState by historialViewModel.uiState.collectAsState()

            LaunchedEffect(usuarioId) {
                historialViewModel.cargar(usuarioId)
            }

            HistorialScreen(
                state = historialUiState,
                onSeleccionarFiltro = historialViewModel::seleccionarFiltro,
                onVerDetalle = { registro ->
                    when (registro.ambito) {
                        AmbitoRegistro.ANIMO -> navController.navegarDesde(entrada, Destinos.crearRutaDetalle(registro.id))
                        AmbitoRegistro.DBT -> navController.navegarDesde(entrada, Destinos.crearRutaDetalleDbt(registro.id))
                        AmbitoRegistro.NEURODESARROLLO -> navController.navegarDesde(entrada, Destinos.crearRutaDetalleNeuro(registro.id))
                        AmbitoRegistro.ADICCIONES -> navController.navegarDesde(entrada, Destinos.crearRutaDetalleAdiccion(registro.id))
                    }
                },
                onEditar = { registro ->
                    when (registro.ambito) {
                        AmbitoRegistro.ANIMO -> navController.navegarDesde(entrada, Destinos.crearRutaEditar(registro.id))
                        AmbitoRegistro.DBT -> navController.navegarDesde(entrada, Destinos.crearRutaEditarDbt(registro.id))
                        AmbitoRegistro.NEURODESARROLLO -> navController.navegarDesde(entrada, Destinos.crearRutaEditarNeuro(registro.id))
                        AmbitoRegistro.ADICCIONES -> navController.navegarDesde(entrada, Destinos.crearRutaEditarAdiccion(registro.id))
                    }
                },
                onSolicitarEliminar = historialViewModel::solicitarEliminacion,
                onCancelarEliminar = historialViewModel::cancelarEliminacion,
                onConfirmarEliminar = historialViewModel::confirmarEliminacion,
                onReintentar = { historialViewModel.cargar(usuarioId) },
                onVolver = { navController.popBackStack(route = Destinos.HOME, inclusive = false) }
            )
        }

        composable(Destinos.DBT_CATALOGO) { entrada ->
            val dbtViewModel: DbtViewModel = viewModel()
            val state by dbtViewModel.uiState.collectAsState()
            CatalogoDbtScreen(
                state = state,
                onSeleccionarModulo = dbtViewModel::seleccionarModulo,
                onVerHabilidad = { navController.navegarDesde(entrada, Destinos.crearRutaHabilidadDbt(it)) },
                onReintentar = dbtViewModel::cargarCatalogo,
                onVolver = { navController.volverDesde(entrada) }
            )
        }

        composable(
            route = Destinos.DBT_HABILIDAD,
            arguments = listOf(navArgument("habilidadId") { type = NavType.IntType })
        ) { entrada ->
            val dbtViewModel: DbtViewModel = viewModel()
            val state by dbtViewModel.uiState.collectAsState()
            DetalleHabilidadDbtScreen(
                state = state,
                habilidadId = entrada.arguments!!.getInt("habilidadId"),
                onRegistrarPractica = { navController.navegarDesde(entrada, Destinos.crearRutaNuevaPracticaDbt(it)) },
                onReintentar = dbtViewModel::cargarCatalogo,
                onVolver = { navController.volverDesde(entrada) }
            )
        }

        composable(
            route = Destinos.DBT_DETALLE,
            arguments = listOf(navArgument("registroId") { type = NavType.StringType })
        ) { entrada ->
            val vm: RegistroDbtViewModel = viewModel()
            val state by vm.uiState.collectAsState()
            val id = entrada.arguments?.getString("registroId") ?: ""

            LaunchedEffect(usuarioId, id) {
                vm.cargar(usuarioId, id, null)
            }

            DetalleRegistroDbtScreen(
                state = state,
                onEditar = { navController.navegarDesde(entrada, Destinos.crearRutaEditarDbt(id)) },
                onVolver = { navController.volverDesde(entrada) },
                onEliminar = { navController.popBackStack() },
                onVerHistorial = { navController.navegarDesde(entrada, Destinos.HISTORIAL) },
                onReintentar = { vm.cargar(usuarioId, id, null) }
            )
        }

        listOf(Destinos.DBT_NUEVA, Destinos.DBT_EDITAR).forEach { ruta ->
            val editando = ruta == Destinos.DBT_EDITAR
            composable(
                route = ruta,
                arguments = if (editando) listOf(navArgument("registroId") { type = NavType.StringType })
                else listOf(navArgument("habilidadId") { type = NavType.IntType })
            ) { entrada ->
                val vm: RegistroDbtViewModel = viewModel()
                val state by vm.uiState.collectAsState()
                val id = entrada.arguments?.getString("registroId")
                val habilidadId = if (editando) null else entrada.arguments?.getInt("habilidadId")

                LaunchedEffect(usuarioId, id, habilidadId) {
                    vm.cargar(usuarioId, id, habilidadId)
                }

                RegistroDbtScreen(
                    state = state,
                    editando = editando,
                    onSeleccionarHabilidad = vm::seleccionarHabilidad,
                    onCambioSituacion = vm::cambiarSituacion,
                    onCambioIntensidad = vm::cambiarIntensidad,
                    onCambioUtilidad = vm::cambiarUtilidad,
                    onGuardar = {
                        vm.guardar {
                            if (navController.currentBackStackEntry == entrada) {
                                if (editando) navController.popBackStack()
                                else navController.popBackStack(Destinos.HOME, inclusive = false)
                            }
                        }
                    },
                    onReintentar = {
                        vm.cargar(usuarioId, id, habilidadId)
                    },
                    onVolver = { navController.volverDesde(entrada) }
                )
            }
        }
    }
}
