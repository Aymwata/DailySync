package com.kiberkot.dailysync.ui.registro

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kiberkot.dailysync.R
import com.kiberkot.dailysync.model.CategoriaAnimo
import com.kiberkot.dailysync.ui.components.EstadoRegistro
import com.kiberkot.dailysync.ui.theme.BarrasClarasMarca
import com.kiberkot.dailysync.ui.theme.DailySyncBrand as Brand
import com.kiberkot.dailysync.ui.theme.DailySyncTheme
import com.kiberkot.dailysync.viewmodel.AnimoUiState

/** Creación y edición comparten controles, validación y bloqueo de escritura. */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RegistroAnimoScreen(
    state: AnimoUiState,
    editando: Boolean = false,
    onSeleccionarCategoria: (CategoriaAnimo) -> Unit,
    onCambioIntensidad: (Float) -> Unit,
    onCambioContexto: (String) -> Unit,
    onCambioNota: (String) -> Unit,
    onGuardar: () -> Unit,
    onVolver: () -> Unit,
    onReintentar: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val focus = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current
    var mostrarNota by rememberSaveable { mutableStateOf(false) }
    val categoriaVisible = remember { BringIntoViewRequester() }
    val contextoVisible = remember { BringIntoViewRequester() }
    var intentosGuardar by remember { mutableIntStateOf(0) }
    var validacionMostrada by remember { mutableIntStateOf(0) }
    LaunchedEffect(intentosGuardar, state.errorCategoria, state.errorContexto) {
        if (intentosGuardar > validacionMostrada) {
            when {
                state.errorCategoria != null -> {
                    validacionMostrada = intentosGuardar
                    categoriaVisible.bringIntoView()
                }
                state.errorContexto != null -> {
                    validacionMostrada = intentosGuardar
                    contextoVisible.bringIntoView()
                }
            }
        }
    }
    LaunchedEffect(state.idRegistroExistente) {
        if (state.notaOpcional.isNotBlank()) mostrarNota = true
    }
    BackHandler(enabled = state.guardando) { }
    BarrasClarasMarca()
    val disponible = !state.cargando && !state.cargaFallida && !state.noDisponible
    val guardar = {
        intentosGuardar++
        keyboard?.hide()
        focus.clearFocus()
        onGuardar()
    }
    Scaffold(
        modifier = modifier.fillMaxSize().imePadding(),
        containerColor = Color(0xFFF6FAF7), contentColor = Brand.Ink,
        topBar = {
            Row(Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Surface(shape = CircleShape, color = Color.White) {
                    IconButton(onClick = onVolver, enabled = !state.guardando, modifier = Modifier.size(44.dp)) {
                        Icon(painterResource(R.drawable.ic_form_back), "Volver", tint = Brand.Ink, modifier = Modifier.size(21.dp))
                    }
                }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(1.dp)) {
                    Text("Estado de ánimo", style = textoAnimo(12, FontWeight.Medium), color = AnimoAccent)
                    Text(if (editando) "Editar Check-in" else "Nuevo Check-in",
                        style = textoAnimo(20, FontWeight.SemiBold), color = Brand.Ink)
                }
                Surface(shape = RoundedCornerShape(14.dp), color = AnimoSoft) {
                    Box(Modifier.size(42.dp), contentAlignment = Alignment.Center) {
                        Image(painterResource(R.drawable.ic_module_animo), null, Modifier.size(34.dp))
                    }
                }
            }
        },
        bottomBar = {
            if (disponible) {
                Surface(shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp), color = Color.White,
                    border = BorderStroke(1.dp, Brand.Border)) {
                    Column(Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 20.dp, vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        state.error?.let { ErrorAnimo(it) }
                        val shape = RoundedCornerShape(18.dp)
                        Button(onClick = guardar, enabled = !state.guardando, shape = shape,
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent,
                                disabledContainerColor = Color.Transparent, contentColor = Color.White,
                                disabledContentColor = Color.White),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
                            modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp).testTag("animo_guardar")
                                .background(Brush.horizontalGradient(listOf(Brand.Teal, Brand.Emerald)), shape)
                                .semantics { liveRegion = LiveRegionMode.Polite }) {
                            when {
                                state.bannerExitoVisible -> Icon(painterResource(R.drawable.ic_home_check), null, Modifier.size(20.dp))
                                state.guardando -> CircularProgressIndicator(Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                                else -> Icon(painterResource(R.drawable.ic_home_check), null, Modifier.size(20.dp))
                            }
                            Spacer(Modifier.width(10.dp))
                            Text(when {
                                state.bannerExitoVisible -> if (editando) "Cambios guardados" else "Registro guardado"
                                state.guardando -> "Guardando…"
                                editando -> "Guardar cambios"
                                else -> "Guardar registro"
                            }, style = textoAnimo(16, FontWeight.SemiBold))
                        }
                        Text("Se guarda en este dispositivo", style = textoAnimo(12), color = Brand.Muted)
                    }
                }
            }
        }
    ) { padding ->
        if (!disponible) {
            EstadoRegistro(state.cargando, state.error, onReintentar, Modifier.padding(padding))
            return@Scaffold
        }
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(if (editando) "Revisa cómo te sentías" else "¿Cómo te sientes hoy?",
                    style = textoAnimo(25, FontWeight.Bold), color = Brand.Ink)
                Text(if (editando) "Puedes ajustar tu registro con calma." else "Elige lo que más se acerca a este momento.",
                    style = textoAnimo(14), color = Brand.Muted)
            }
            Column(Modifier.bringIntoViewRequester(categoriaVisible), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                PasoAnimo("1", "Elige tu estado")
                Column(Modifier.fillMaxWidth().selectableGroup(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    state.categorias.chunked(2).forEach { fila ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            fila.forEach { categoria ->
                                OpcionAnimo(categoria, state.categoriaSeleccionada?.id == categoria.id,
                                    !state.guardando, { onSeleccionarCategoria(categoria) }, Modifier.weight(1f))
                            }
                        }
                    }
                }
                state.errorCategoria?.let { ErrorAnimo(it) }
            }
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                PasoAnimo("2", "Ajusta la intensidad")
                Surface(shape = RoundedCornerShape(24.dp), color = Color.White,
                    border = BorderStroke(1.dp, Brand.Border)) {
                    Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically) {
                            Text("¿Cuánto lo sientes?", style = textoAnimo(14, FontWeight.Medium), color = Brand.Ink)
                            Text(descripcionIntensidad(state.nivelIntensidad.toInt()), style = textoAnimo(12, FontWeight.SemiBold),
                                color = AnimoAccent)
                        }
                        Row(Modifier.fillMaxWidth().selectableGroup(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            (1..5).forEach { nivel ->
                                val seleccionada = state.nivelIntensidad.toInt() == nivel
                                Surface(shape = RoundedCornerShape(14.dp),
                                    color = if (seleccionada) AnimoAccent else Color(0xFFF5F7F3),
                                    modifier = Modifier.weight(1f).heightIn(min = 48.dp)
                                        .selectable(selected = seleccionada, enabled = !state.guardando, role = Role.RadioButton,
                                            onClick = { onCambioIntensidad(nivel.toFloat()) })
                                        .testTag("animo_intensidad_$nivel").semantics {
                                            contentDescription = "Intensidad $nivel de 5: ${descripcionIntensidad(nivel)}"
                                        }) {
                                    Box(Modifier.padding(vertical = 10.dp).clearAndSetSemantics {
                                        contentDescription = "Intensidad $nivel de 5: ${descripcionIntensidad(nivel)}"
                                    }, contentAlignment = Alignment.Center) {
                                        Text("$nivel", style = textoAnimo(18, FontWeight.SemiBold),
                                            color = if (seleccionada) Color.White else Brand.Muted)
                                    }
                                }
                            }
                        }
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Suave", style = textoAnimo(12), color = Brand.Muted)
                            Text("Muy intensa", style = textoAnimo(12), color = Brand.Muted)
                        }
                    }
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                PasoAnimo("3", "Añade contexto")
                Surface(shape = RoundedCornerShape(24.dp), color = Color.White,
                    border = BorderStroke(1.dp, Brand.Border)) {
                    Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Contexto", style = textoAnimo(14, FontWeight.SemiBold), color = Brand.Ink,
                                modifier = Modifier.clearAndSetSemantics {})
                            Text("Obligatorio", style = textoAnimo(12), color = Brand.Muted)
                        }
                        Text("¿Qué estaba pasando?", style = textoAnimo(13), color = Brand.Muted)
                        CampoAnimo(state.contexto, onCambioContexto, "Ej. Terminando una tarea en casa",
                            "Contexto", "animo_contexto", !state.guardando, state.errorContexto,
                            KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Next),
                            KeyboardActions(onNext = { focus.moveFocus(FocusDirection.Down) }),
                            modifier = Modifier.bringIntoViewRequester(contextoVisible))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Entre 3 y 60 caracteres", style = textoAnimo(12), color = Brand.Muted)
                            Text("${state.contexto.trim().length}/60", style = textoAnimo(12),
                                color = if (state.contexto.trim().length > 60) Brand.Error else Brand.Muted)
                        }
                        HorizontalDivider(color = Brand.Border)
                        TextButton(onClick = { mostrarNota = !mostrarNota }, enabled = !state.guardando,
                            contentPadding = PaddingValues(horizontal = 0.dp, vertical = 6.dp)) {
                            Icon(painterResource(R.drawable.ic_form_note), null, Modifier.size(19.dp), tint = Brand.Teal)
                            Spacer(Modifier.width(8.dp))
                            Text(if (mostrarNota) "Ocultar nota opcional" else if (state.notaOpcional.isNotBlank()) "Editar nota opcional"
                                else "Añadir nota opcional", style = textoAnimo(14, FontWeight.Medium), color = Brand.Teal)
                        }
                        if (mostrarNota) {
                            CampoAnimo(state.notaOpcional, onCambioNota, "Algo que quieras recordar…",
                                "Nota opcional", "animo_nota", !state.guardando, null,
                                KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Default),
                                KeyboardActions.Default)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PasoAnimo(numero: String, titulo: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Surface(shape = CircleShape, color = AnimoSoft) {
            Box(Modifier.size(28.dp), contentAlignment = Alignment.Center) {
                Text(numero, style = textoAnimo(12, FontWeight.SemiBold), color = AnimoAccent)
            }
        }
        Text(titulo, style = textoAnimo(17, FontWeight.SemiBold), color = Brand.Ink)
    }
}

@Composable
private fun OpcionAnimo(categoria: CategoriaAnimo, seleccionada: Boolean, enabled: Boolean,
    onSelect: () -> Unit, modifier: Modifier = Modifier) {
    val estilo = estiloAnimo(categoria.id, categoria.nombre)
    Surface(shape = RoundedCornerShape(20.dp), color = if (seleccionada) estilo.fondo else Color.White,
        border = BorderStroke(if (seleccionada) 1.5.dp else 1.dp, if (seleccionada) estilo.acento else Brand.Border),
        modifier = modifier.heightIn(min = 72.dp)
            .selectable(selected = seleccionada, enabled = enabled, role = Role.RadioButton, onClick = onSelect)
            .testTag("animo_categoria_${categoria.id}").semantics { contentDescription = categoria.nombre }) {
        Row(Modifier.fillMaxWidth().padding(12.dp).clearAndSetSemantics { contentDescription = categoria.nombre },
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Image(painterResource(estilo.icono), null, Modifier.size(36.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(estilo.titulo, style = textoAnimo(15, FontWeight.SemiBold), color = if (seleccionada) estilo.acento else Brand.Ink)
                if (estilo.matiz.isNotEmpty()) Text(estilo.matiz, style = textoAnimo(12), color = Brand.Muted)
            }
            Box(Modifier.size(18.dp).background(if (seleccionada) estilo.acento else Color.White, CircleShape)
                .border(1.dp, if (seleccionada) estilo.acento else Color(0xFFCADCD2), CircleShape), contentAlignment = Alignment.Center) {
                if (seleccionada) Icon(painterResource(R.drawable.ic_home_check), null, Modifier.size(12.dp), tint = Color.White)
            }
        }
    }
}

@Composable
private fun CampoAnimo(value: String, onChange: (String) -> Unit, placeholder: String,
    label: String, tag: String, enabled: Boolean, error: String?,
    options: KeyboardOptions, actions: KeyboardActions, modifier: Modifier = Modifier) {
    OutlinedTextField(value = value, onValueChange = onChange, enabled = enabled,
        textStyle = textoAnimo(16), minLines = 2, maxLines = 5,
        placeholder = { Text(placeholder, style = textoAnimo(14), color = Color(0xFF7A9389)) },
        shape = RoundedCornerShape(18.dp), isError = error != null,
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Brand.Field, unfocusedContainerColor = Brand.Field, disabledContainerColor = Brand.Field,
            errorContainerColor = Brand.Field, focusedBorderColor = Brand.Teal, unfocusedBorderColor = Brand.Border,
            focusedTextColor = Brand.Ink, unfocusedTextColor = Brand.Ink, disabledTextColor = Brand.Muted,
            cursorColor = Brand.Teal, errorBorderColor = Brand.Error),
        keyboardOptions = options, keyboardActions = actions,
        modifier = modifier.fillMaxWidth().testTag(tag).semantics { contentDescription = label },
        supportingText = if (error == null) null else ({ ErrorAnimo(error) }))
}

@Composable
private fun ErrorAnimo(mensaje: String) {
    Text(mensaje, style = textoAnimo(12), color = Brand.Error,
        modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite })
}

private fun descripcionIntensidad(valor: Int) = when (valor) {
    1 -> "Muy suave"
    2 -> "Suave"
    3 -> "Moderada"
    4 -> "Intensa"
    5 -> "Muy intensa"
    else -> "Elige un nivel"
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun AnimoPreview() {
    DailySyncTheme { RegistroAnimoScreen(AnimoUiState(cargando = false), false, {}, {}, {}, {}, {}, {}) }
}

@Preview(showBackground = true, widthDp = 320, heightDp = 640, fontScale = 1.3f)
@Composable
private fun AnimoCompactoPreview() {
    DailySyncTheme { RegistroAnimoScreen(AnimoUiState(cargando = false, errorContexto = "Escribe entre 3 y 60 caracteres."),
        false, {}, {}, {}, {}, {}, {}) }
}
