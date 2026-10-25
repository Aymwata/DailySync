package com.kiberkot.dailysync.navigation

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.kiberkot.dailysync.data.repository.DbtRepository
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
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    animoViewModel: AnimoViewModel = viewModel(),
    loginViewModel: LoginViewModel = viewModel(),
    registerViewModel: RegisterViewModel = viewModel(),
    homeViewModel: HomeViewModel = viewModel(),
    dbtViewModel: DbtViewModel = viewModel(),
    neuroViewModel: NeuroViewModel = viewModel(),
    adiccionViewModel: AdiccionViewModel = viewModel()
) {
    val animoUiState by animoViewModel.uiState.collectAsState()
    val loginUiState by loginViewModel.uiState.collectAsState()
    val registerUiState by registerViewModel.uiState.collectAsState()
    val homeUiState by homeViewModel.uiState.collectAsState()
    val neuroUiState by neuroViewModel.uiState.collectAsState()
    val adiccionUiState by adiccionViewModel.uiState.collectAsState()

    val context = LocalContext.current
    val usuarioDbtId = DbtRepository.USUARIO_DEMO

    LaunchedEffect(loginUiState.loginExitoso) {
        if (loginUiState.loginExitoso) {
            navController.navigate(Destinos.HOME) {
                popUpTo(Destinos.LOGIN) { inclusive = true }
            }
            loginViewModel.resetearEstadoLogin()
        }
    }

    LaunchedEffect(registerUiState.registroExitoso) {
        if (registerUiState.registroExitoso) {
            navController.popBackStack()
            registerViewModel.resetearEstado()
        }
    }

    LaunchedEffect(neuroUiState.guardadoExitoso) {
        if (neuroUiState.guardadoExitoso) {
            navController.popBackStack()
            neuroViewModel.resetearEstado()
        }
    }

    LaunchedEffect(adiccionUiState.guardadoExitoso) {
        if (adiccionUiState.guardadoExitoso) {
            navController.popBackStack()
            adiccionViewModel.resetearEstado()
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
            LaunchedEffect(usuarioDbtId) {
                homeViewModel.cargar(usuarioDbtId)
            }
            HomeScreen(
                state = homeUiState,
                onReintentar = { homeViewModel.cargar(usuarioDbtId) },
                onAbrirModulo = { modulo ->
                    when (modulo) {
                        ModuloHome.ANIMO -> navController.navigate(Destinos.REGISTRO)
                        ModuloHome.DBT -> navController.navigate(Destinos.DBT_CATALOGO)
                        ModuloHome.NEURODESARROLLO -> navController.navigate(Destinos.NEURO)
                        ModuloHome.ADICCIONES -> navController.navigate(Destinos.ADICCION)
                    }
                },
                onAbrirSeccion = { seccion ->
                    when (seccion) {
                        SeccionHome.INICIO -> Unit
                        SeccionHome.HISTORIAL -> navController.navigate(Destinos.HISTORIAL)
                        SeccionHome.CONTENIDOS,
                        SeccionHome.AJUSTES -> {
                            Toast.makeText(context, "Sección en preparación", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            )
        }

        composable(route = Destinos.REGISTRO) {
            RegistroAnimoScreen(
                state = animoUiState,
                editando = false,
                onSeleccionarCategoria = animoViewModel::seleccionarCategoria,
                onCambioIntensidad = animoViewModel::cambiarIntensidad,
                onCambioContexto = animoViewModel::cambiarContexto,
                onCambioNota = animoViewModel::cambiarNota,
                onGuardar = { animoViewModel.guardarRegistro(onExito = { navController.popBackStack() }) },
                onOcultarBanner = animoViewModel::ocultarBanner,
                onVolver = { navController.popBackStack() }
            )
        }

        composable(
            route = Destinos.DETALLE,
            arguments = listOf(navArgument("registroId") { type = NavType.StringType })
        ) { entrada ->
            val id = entrada.arguments?.getString("registroId") ?: ""
            DetalleAnimoScreen(
                state = animoUiState,
                registroId = id,
                onEditar = { navController.navigate(Destinos.crearRutaEditar(id)) },
                onVolver = { navController.popBackStack() }
            )
        }

        composable(
            route = Destinos.EDITAR,
            arguments = listOf(navArgument("registroId") { type = NavType.StringType })
        ) { entrada ->
            val id = entrada.arguments?.getString("registroId") ?: ""
            LaunchedEffect(id) {
                animoViewModel.cargarParaEditar(id)
            }

            RegistroAnimoScreen(
                state = animoUiState,
                editando = true,
                onSeleccionarCategoria = animoViewModel::seleccionarCategoria,
                onCambioIntensidad = animoViewModel::cambiarIntensidad,
                onCambioContexto = animoViewModel::cambiarContexto,
                onCambioNota = animoViewModel::cambiarNota,
                onGuardar = {
                    animoViewModel.guardarRegistro(onExito = { navController.popBackStack() })
                },
                onOcultarBanner = animoViewModel::ocultarBanner,
                onVolver = { navController.popBackStack() }
            )
        }

        // --- NEURODESARROLLO (CREAR / EDITAR / DETALLE) ---
        listOf(Destinos.NEURO, Destinos.NEURO_EDITAR).forEach { ruta ->
            val editando = ruta == Destinos.NEURO_EDITAR
            composable(
                route = ruta,
                arguments = if (editando) listOf(navArgument("registroId") { type = NavType.StringType }) else emptyList()
            ) { entrada ->
                val id = entrada.arguments?.getString("registroId")
                LaunchedEffect(usuarioDbtId, id) {
                    neuroViewModel.cargar(usuarioDbtId, id)
                }

                NeuroScreen(
                    state = neuroUiState,
                    editando = editando,
                    onCambioTipoActividad = neuroViewModel::cambiarTipoActividad,
                    onCambioSobrecarga = neuroViewModel::cambiarNivelSobrecarga,
                    onSeleccionarEstrategia = neuroViewModel::seleccionarEstrategia,
                    onCambioContexto = neuroViewModel::cambiarContexto,
                    onCambioResultado = neuroViewModel::cambiarResultadoPercibido,
                    onGuardar = { neuroViewModel.guardarRegistro(usuarioDbtId) },
                    onVolver = { navController.popBackStack() }
                )
            }
        }

        composable(
            route = Destinos.NEURO_DETALLE,
            arguments = listOf(navArgument("registroId") { type = NavType.StringType })
        ) { entrada ->
            val id = entrada.arguments?.getString("registroId") ?: ""
            LaunchedEffect(usuarioDbtId, id) {
                neuroViewModel.cargar(usuarioDbtId, id)
            }

            DetalleNeuroScreen(
                state = neuroUiState,
                onEditar = { navController.navigate(Destinos.crearRutaEditarNeuro(id)) },
                onVolver = { navController.popBackStack() }
            )
        }

        // --- ADICCIONES / CRAVING (CREAR / EDITAR / DETALLE) ---
        listOf(Destinos.ADICCION, Destinos.ADICCION_EDITAR).forEach { ruta ->
            val editando = ruta == Destinos.ADICCION_EDITAR
            composable(
                route = ruta,
                arguments = if (editando) listOf(navArgument("registroId") { type = NavType.StringType }) else emptyList()
            ) { entrada ->
                val id = entrada.arguments?.getString("registroId")
                LaunchedEffect(usuarioDbtId, id) {
                    adiccionViewModel.cargar(usuarioDbtId, id)
                }

                AdiccionScreen(
                    state = adiccionUiState,
                    editando = editando,
                    onCambioImpulso = adiccionViewModel::cambiarNivelImpulso,
                    onCambioDesencadenante = adiccionViewModel::cambiarDesencadenante,
                    onSeleccionarAccion = adiccionViewModel::seleccionarAccion,
                    onCambioEstadoObjetivo = adiccionViewModel::cambiarEstadoObjetivo,
                    onGuardar = { adiccionViewModel.guardarRegistro(usuarioDbtId) },
                    onVolver = { navController.popBackStack() }
                )
            }
        }

        composable(
            route = Destinos.ADICCION_DETALLE,
            arguments = listOf(navArgument("registroId") { type = NavType.StringType })
        ) { entrada ->
            val id = entrada.arguments?.getString("registroId") ?: ""
            LaunchedEffect(usuarioDbtId, id) {
                adiccionViewModel.cargar(usuarioDbtId, id)
            }

            DetalleAdiccionScreen(
                state = adiccionUiState,
                onEditar = { navController.navigate(Destinos.crearRutaEditarAdiccion(id)) },
                onVolver = { navController.popBackStack() }
            )
        }

        composable(route = Destinos.HISTORIAL) {
            val historialViewModel: HistorialViewModel = viewModel()
            val historialUiState by historialViewModel.uiState.collectAsState()

            LaunchedEffect(usuarioDbtId) {
                historialViewModel.cargar(usuarioDbtId)
            }

            HistorialScreen(
                state = historialUiState,
                onSeleccionarFiltro = historialViewModel::seleccionarFiltro,
                onVerDetalle = { registro ->
                    when (registro.ambito) {
                        AmbitoRegistro.ANIMO -> navController.navigate(Destinos.crearRutaDetalle(registro.id))
                        AmbitoRegistro.DBT -> navController.navigate(Destinos.crearRutaDetalleDbt(registro.id))
                        AmbitoRegistro.NEURODESARROLLO -> navController.navigate(Destinos.crearRutaDetalleNeuro(registro.id))
                        AmbitoRegistro.ADICCIONES -> navController.navigate(Destinos.crearRutaDetalleAdiccion(registro.id))
                    }
                },
                onEditar = { registro ->
                    when (registro.ambito) {
                        AmbitoRegistro.ANIMO -> navController.navigate(Destinos.crearRutaEditar(registro.id))
                        AmbitoRegistro.DBT -> navController.navigate(Destinos.crearRutaEditarDbt(registro.id))
                        AmbitoRegistro.NEURODESARROLLO -> navController.navigate(Destinos.crearRutaEditarNeuro(registro.id))
                        AmbitoRegistro.ADICCIONES -> navController.navigate(Destinos.crearRutaEditarAdiccion(registro.id))
                    }
                },
                onSolicitarEliminar = historialViewModel::solicitarEliminacion,
                onCancelarEliminar = historialViewModel::cancelarEliminacion,
                onConfirmarEliminar = historialViewModel::confirmarEliminacion,
                onReintentar = { historialViewModel.cargar(usuarioDbtId) },
                onVolver = { navController.popBackStack(route = Destinos.HOME, inclusive = false) }
            )
        }

        composable(Destinos.DBT_CATALOGO) {
            val state by dbtViewModel.uiState.collectAsState()
            CatalogoDbtScreen(
                state = state,
                onSeleccionarModulo = dbtViewModel::seleccionarModulo,
                onVerHabilidad = { navController.navigate(Destinos.crearRutaHabilidadDbt(it)) },
                onReintentar = dbtViewModel::cargarCatalogo,
                onVolver = { navController.popBackStack() }
            )
        }

        composable(
            route = Destinos.DBT_HABILIDAD,
            arguments = listOf(navArgument("habilidadId") { type = NavType.IntType })
        ) { entrada ->
            val state by dbtViewModel.uiState.collectAsState()
            DetalleHabilidadDbtScreen(
                state = state,
                habilidadId = entrada.arguments!!.getInt("habilidadId"),
                onRegistrarPractica = { navController.navigate(Destinos.crearRutaNuevaPracticaDbt(it)) },
                onReintentar = dbtViewModel::cargarCatalogo,
                onVolver = { navController.popBackStack() }
            )
        }

        composable(
            route = Destinos.DBT_DETALLE,
            arguments = listOf(navArgument("registroId") { type = NavType.StringType })
        ) { entrada ->
            val vm: RegistroDbtViewModel = viewModel()
            val state by vm.uiState.collectAsState()
            val id = entrada.arguments?.getString("registroId") ?: ""

            LaunchedEffect(usuarioDbtId, id) {
                vm.cargar(usuarioDbtId, id, null)
            }

            DetalleRegistroDbtScreen(
                state = state,
                onEditar = { navController.navigate(Destinos.crearRutaEditarDbt(id)) },
                onVolver = { navController.popBackStack() },
                onEliminar = { navController.popBackStack() },
                onVerHistorial = { navController.navigate(Destinos.HISTORIAL) },
                onReintentar = { vm.cargar(usuarioDbtId, id, null) }
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

                LaunchedEffect(usuarioDbtId, id, habilidadId) {
                    vm.cargar(usuarioDbtId, id, habilidadId)
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
                                navController.popBackStack()
                            }
                        }
                    },
                    onReintentar = {
                        vm.cargar(usuarioDbtId, id, habilidadId)
                    },
                    onVolver = { navController.popBackStack() }
                )
            }
        }
    }
}