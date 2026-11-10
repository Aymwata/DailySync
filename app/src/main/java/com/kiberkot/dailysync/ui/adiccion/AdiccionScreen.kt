package com.kiberkot.dailysync.ui.adiccion

import androidx.activity.compose.BackHandler
import com.kiberkot.dailysync.ui.components.RegistroRapidoConfig
import com.kiberkot.dailysync.ui.components.ControlesRegistroRapido
import com.kiberkot.dailysync.ui.components.PieRegistroRapido
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.ui.platform.testTag
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kiberkot.dailysync.R
import com.kiberkot.dailysync.data.local.entities.CatalogoAdiccionEntity
import com.kiberkot.dailysync.ui.components.EstadoRegistro
import com.kiberkot.dailysync.ui.theme.DailySyncBrand as Brand
import com.kiberkot.dailysync.viewmodel.AdiccionUiState

private val AdiccionAccent = Color(0xFFA56150)
private val AdiccionSoft = Color(0xFFFBECE6)
private val AdiccionRed = Color(0xFFA56150)

private fun textoAdiccion(size: Int, weight: FontWeight = FontWeight.Normal) = TextStyle(
    fontFamily = Brand.Font,
    fontSize = size.sp,
    lineHeight = (size * 1.35f).sp,
    fontWeight = weight
)

@OptIn(ExperimentalFoundationApi::class, ExperimentalLayoutApi::class)
@Composable
fun AdiccionScreen(
    state: AdiccionUiState,
    editando: Boolean = false,
    onCambioImpulso: (Int) -> Unit,
    onCambioDesencadenante: (String) -> Unit,
    onSeleccionarAccion: (Int) -> Unit,
    onCambioEstadoObjetivo: (String) -> Unit,
    onGuardar: () -> Unit,
    onVolver: () -> Unit,
    onReintentar: () -> Unit = {},
    modifier: Modifier = Modifier,
    onSeleccionarDesencadenante: (String) -> Unit = {},
    onCambioOtraAccion: (String) -> Unit = {},
    onCerrarApoyo: () -> Unit = {},
    registroRapido: RegistroRapidoConfig? = null
) {
    val lista = rememberLazyListState()
    val focus = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current
    var intentos by remember { mutableIntStateOf(0) }
    var errorMostrado by remember { mutableIntStateOf(0) }
    LaunchedEffect(intentos, state.errorMsg) {
        if (intentos > errorMostrado && state.errorMsg != null) {
            errorMostrado = intentos
            when {
                state.desencadenante.isBlank() -> lista.animateScrollToItem(2)
                state.idAccionSeleccionada == null -> lista.animateScrollToItem(3)
            }
        }
    }
    val opcionesObjetivo = com.kiberkot.dailysync.viewmodel.ESTADOS_OBJETIVO

    if (state.mostrarApoyo) AvisoApoyoAdiccion(onCerrarApoyo)

    BackHandler(enabled = state.guardando || state.guardadoExitoso) { }

    val disponible = !state.isLoading && !state.cargaFallida && !state.noDisponible
    val guardar = {
        intentos++
        keyboard?.hide()
        focus.clearFocus()
        onGuardar()
    }

    Scaffold(
        modifier = modifier.fillMaxSize().imePadding(),
        containerColor = Color(0xFFF6FAF7),
        contentColor = Brand.Ink,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(shape = CircleShape, color = Color.White) {
                    IconButton(
                        onClick = onVolver,
                        enabled = !state.guardando && !state.guardadoExitoso,
                        modifier = Modifier.size(44.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_form_back),
                            contentDescription = "Volver",
                            tint = Brand.Ink,
                            modifier = Modifier.size(21.dp)
                        )
                    }
                }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(1.dp)) {
                    Text("Adicciones y Craving", style = textoAdiccion(12, FontWeight.Medium), color = AdiccionAccent)
                    Text(
                        if (editando) "Editar registro" else "Nuevo registro",
                        style = textoAdiccion(20, FontWeight.SemiBold),
                        color = Brand.Ink
                    )
                }
                Surface(shape = RoundedCornerShape(14.dp), color = AdiccionSoft) {
                    Box(Modifier.size(42.dp), contentAlignment = Alignment.Center) {
                        Image(
                            painter = painterResource(R.drawable.ic_module_adiccion),
                            contentDescription = null,
                            modifier = Modifier.size(34.dp)
                        )
                    }
                }
            }
        },
        bottomBar = {
            if (disponible) {
                Surface(
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, Brand.Border)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(horizontal = 20.dp, vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ControlesRegistroRapido(registroRapido, habilitado = !state.guardando && !state.guardadoExitoso)
                        state.errorMsg?.let { ErrorAdiccion(it) }


                        val shape = RoundedCornerShape(18.dp)
                        Button(
                            onClick = guardar,
                            enabled = !state.guardando && !state.guardadoExitoso,
                            shape = shape,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Transparent,
                                disabledContainerColor = Color.Transparent,
                                contentColor = Color.White,
                                disabledContentColor = Color.White
                            ),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 52.dp)
                                .background(Brush.horizontalGradient(listOf(Brand.Teal, Brand.Emerald)), shape)
                        ) {
                            when {
                                state.guardadoExitoso -> Icon(painterResource(R.drawable.ic_home_check), null, Modifier.size(20.dp))
                                state.guardando -> CircularProgressIndicator(Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                                else -> Icon(painterResource(R.drawable.ic_home_check), null, Modifier.size(20.dp))
                            }
                            Spacer(Modifier.width(10.dp))
                            Text(
                                text = when {
                                    state.guardadoExitoso -> if (editando) "Cambios guardados" else "Registro guardado"
                                    state.guardando -> "Guardando…"
                                    editando -> "Guardar cambios"
                                    else -> "Guardar registro"
                                },
                                style = textoAdiccion(16, FontWeight.SemiBold)
                            )
                        }
                        Text("Se guarda en este dispositivo", style = textoAdiccion(12), color = Brand.Muted)
                    }
                }
            } else PieRegistroRapido(registroRapido)
        }
    ) { padding ->
        if (!disponible) {
            EstadoRegistro(state.isLoading, state.errorMsg, onReintentar, Modifier.padding(padding))
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            state = lista, contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            item(key = "seccion_0") {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        if (editando) "Revisa tu registro de impulsos" else "Registro de Impulsos",
                        style = textoAdiccion(25, FontWeight.Bold),
                        color = Brand.Ink
                    )
                    Text(
                        "Monitorea el craving y registra las acciones protectoras aplicadas.",
                        style = textoAdiccion(14),
                        color = Brand.Muted
                    )
                }
            }
            item(key = "seccion_1") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    PasoAdiccion("1", "Ajusta el nivel de impulso")
                    if (state.versionEscala == 1) Text(com.kiberkot.dailysync.model.AVISO_ESCALA_ANTERIOR,
                        style = textoAdiccion(13), color = Brand.Muted)
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = Color.White,
                        border = BorderStroke(1.dp, Brand.Border)
                    ) {
                        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Intensidad del impulso", style = textoAdiccion(14, FontWeight.Medium), color = Brand.Ink, modifier = Modifier.weight(1f).padding(end = 8.dp))
                                Text(
                                    com.kiberkot.dailysync.model.textoIntensidad(state.nivelImpulso, 2),
                                    style = textoAdiccion(12, FontWeight.SemiBold),
                                    color = AdiccionRed
                                )
                            }
                            Column(Modifier.fillMaxWidth().selectableGroup(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                (0..4).chunked(5).forEach { fila ->
                                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        fila.forEach { nivel ->
                                            val seleccionada = state.nivelImpulso == nivel
                                            Surface(shape = RoundedCornerShape(14.dp),
                                                color = if (seleccionada) AdiccionRed else AdiccionSoft,
                                                modifier = Modifier.weight(1f).heightIn(min = 48.dp)
                                                    .selectable(selected = seleccionada,
                                                        enabled = !state.guardando && !state.guardadoExitoso,
                                                        role = Role.RadioButton, onClick = { onCambioImpulso(nivel) })
                                                    .testTag("adiccion_impulso_$nivel")) {
                                                Box(Modifier.padding(vertical = 10.dp).clearAndSetSemantics {
                                                    contentDescription = "Impulso: $nivel de 4: ${com.kiberkot.dailysync.model.etiquetaIntensidad(nivel)}"
                                                }, contentAlignment = Alignment.Center) {
                                                    Text("$nivel", style = textoAdiccion(18, FontWeight.SemiBold),
                                                        color = if (seleccionada) Color.White else AdiccionAccent)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Sin síntomas (0)", style = textoAdiccion(12), color = Brand.Muted)
                                Text("Severa (4)", style = textoAdiccion(12), color = Brand.Muted)
                            }
                        }
                    }
                }
            }
            item(key = "seccion_2") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    PasoAdiccion("2", "Identifica el desencadenante")
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = Color.White,
                        border = BorderStroke(1.dp, Brand.Border)
                    ) {
                        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("¿Qué factor o situación disparó la urgencia?", style = textoAdiccion(13), color = Brand.Muted)
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                com.kiberkot.dailysync.viewmodel.DESENCADENANTES_ADICCION.forEach { opcion ->
                                    FilterChip(selected = if (opcion == "Otro (especificar)") state.otroDesencadenante
                                        else !state.otroDesencadenante && state.desencadenante == opcion,
                                        onClick = { onSeleccionarDesencadenante(opcion) },
                                        enabled = !state.guardando && !state.guardadoExitoso,
                                        label = { Text(opcion, style = textoAdiccion(13)) })
                                }
                            }
                            if (state.otroDesencadenante) OutlinedTextField(
                                enabled = !state.guardando && !state.guardadoExitoso,
                                value = state.desencadenante,
                                onValueChange = com.kiberkot.dailysync.ui.components.conActividadSesion(onCambioDesencadenante),
                                textStyle = textoAdiccion(16),
                                placeholder = { Text("Especifica el otro desencadenante", style = textoAdiccion(14), color = Color(0xFF7A9389)) },
                                shape = RoundedCornerShape(18.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Brand.Field, unfocusedContainerColor = Brand.Field,
                                    focusedBorderColor = Brand.Teal, unfocusedBorderColor = Brand.Border,
                                    focusedTextColor = Brand.Ink, unfocusedTextColor = Brand.Ink
                                ),
                                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Next),
                                keyboardActions = KeyboardActions(onNext = { focus.moveFocus(androidx.compose.ui.focus.FocusDirection.Down) }),
                                modifier = Modifier.fillMaxWidth().testTag("adiccion_desencadenante")
                                    .semantics { contentDescription = "Desencadenante" }
                            )
                        }
                    }
                }
            }
            item(key = "seccion_3") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    PasoAdiccion("3", "Elige una acción protectora")
                    Column(Modifier.fillMaxWidth().selectableGroup(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        state.catalogoAcciones.forEach { acc ->
                            OpcionAccionAdiccion(
                                accion = acc,
                                seleccionada = state.idAccionSeleccionada == acc.idAccionProtectora,
                                enabled = !state.guardando && !state.guardadoExitoso,
                                onSelect = { onSeleccionarAccion(acc.idAccionProtectora) }
                            )
                        }
                    }
                    if (state.idAccionSeleccionada == 5) OutlinedTextField(
                        value = state.otraAccion, onValueChange = com.kiberkot.dailysync.ui.components.conActividadSesion(onCambioOtraAccion),
                        enabled = !state.guardando && !state.guardadoExitoso,
                        label = { Text("Otra acción protectora") },
                        supportingText = { Text("Especifica entre 3 y 250 caracteres") },
                        shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences))
                }
            }
            item(key = "seccion_4") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    PasoAdiccion("4", "Cumplimiento de objetivo")
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = Color.White,
                        border = BorderStroke(1.dp, Brand.Border)
                    ) {
                        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("Objetivo de prueba de tu perfil", style = textoAdiccion(13), color = Brand.Muted)
                            Text(state.objetivoPersonal, style = textoAdiccion(15, FontWeight.SemiBold), color = Brand.Ink)
                            Text("¿Cómo estuvo hoy?", style = textoAdiccion(13), color = Brand.Muted)
                            Column(
                                Modifier.fillMaxWidth().selectableGroup(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                opcionesObjetivo.forEach { opcion ->
                                    val seleccionada = state.estadoObjetivo == opcion
                                    Surface(
                                        shape = RoundedCornerShape(14.dp),
                                        color = if (seleccionada) Brand.Emerald else Color(0xFFF5F7F3),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .heightIn(min = 46.dp)
                                            .selectable(
                                                selected = seleccionada,
                                                enabled = !state.guardando && !state.guardadoExitoso,
                                                role = Role.RadioButton,
                                                onClick = { onCambioEstadoObjetivo(opcion) }
                                            )
                                    ) {
                                        Box(Modifier.padding(vertical = 10.dp).clearAndSetSemantics { contentDescription = "Objetivo: $opcion" }, contentAlignment = Alignment.Center) {
                                            Text(
                                                opcion,
                                                style = textoAdiccion(14, FontWeight.SemiBold),
                                                color = if (seleccionada) Color.White else Brand.Muted
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PasoAdiccion(numero: String, titulo: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Surface(shape = CircleShape, color = AdiccionSoft) {
            Box(Modifier.size(28.dp), contentAlignment = Alignment.Center) {
                Text(numero, style = textoAdiccion(12, FontWeight.SemiBold), color = AdiccionAccent)
            }
        }
        Text(titulo, style = textoAdiccion(17, FontWeight.SemiBold), color = Brand.Ink)
    }
}

@Composable
private fun OpcionAccionAdiccion(
    accion: CatalogoAdiccionEntity,
    seleccionada: Boolean,
    enabled: Boolean,
    onSelect: () -> Unit
) {
    val descripcion = when (accion.idAccionProtectora) {
        1 -> "Observa el impulso y registra cómo cambió."
        2 -> "Busca compañía o conversación en tu red de apoyo."
        3 -> "Registra si cambiar de lugar o actividad te ayudó."
        4 -> "Lleva la atención a una tarea distinta durante un momento."
        else -> "Registra cómo te ayudó esta acción."
    }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = if (seleccionada) AdiccionSoft else Color.White,
        border = BorderStroke(if (seleccionada) 1.5.dp else 1.dp, if (seleccionada) AdiccionAccent else Brand.Border),
        modifier = Modifier
            .fillMaxWidth()
            .selectable(selected = seleccionada, enabled = enabled, role = Role.RadioButton, onClick = onSelect)
            .testTag("adiccion_accion_${accion.idAccionProtectora}")
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(shape = RoundedCornerShape(14.dp), color = Color(0xFFFBE4DC)) {
                Box(Modifier.size(42.dp), contentAlignment = Alignment.Center) {
                    Image(
                        painter = painterResource(R.drawable.ic_module_adiccion),
                        contentDescription = null,
                        modifier = Modifier.size(30.dp)
                    )
                }
            }

            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "Acción protectora",
                    style = textoAdiccion(12, FontWeight.SemiBold),
                    color = AdiccionAccent
                )
                Text(
                    text = accion.nombreAccion,
                    style = textoAdiccion(15, FontWeight.SemiBold),
                    color = if (seleccionada) AdiccionAccent else Brand.Ink
                )
                Text(
                    text = descripcion,
                    style = textoAdiccion(12),
                    color = Brand.Muted
                )
            }

            Box(
                modifier = Modifier
                    .size(20.dp)
                    .background(if (seleccionada) AdiccionAccent else Color.White, CircleShape)
                    .border(1.dp, if (seleccionada) AdiccionAccent else Color(0xFFCADCD2), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (seleccionada) {
                    Icon(
                        painter = painterResource(R.drawable.ic_home_check),
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = Color.White
                    )
                }
            }
        }
    }
}
@Composable
private fun ErrorAdiccion(mensaje: String) {
    Text(mensaje, style = textoAdiccion(12), color = Brand.Error,
        modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite })
}
