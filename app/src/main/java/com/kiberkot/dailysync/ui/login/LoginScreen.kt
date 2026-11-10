package com.kiberkot.dailysync.ui.login

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kiberkot.dailysync.R
import com.kiberkot.dailysync.ui.components.DailySyncLogo
import com.kiberkot.dailysync.ui.theme.DailySyncBrand as Brand
import com.kiberkot.dailysync.ui.theme.DailySyncTheme
import com.kiberkot.dailysync.viewmodel.LoginUiState

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun LoginScreen(
    state: LoginUiState,
    onCambioUsuario: (String) -> Unit,
    onCambioPassword: (String) -> Unit,
    onTogglePasswordVisibility: () -> Unit,
    onIniciarSesion: () -> Unit,
    onIrARegistro: () -> Unit,
    modifier: Modifier = Modifier,
    onAccesoPrueba: () -> Unit = {}
) {
    val focus = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current
    val iniciarSesion = {
        if (!state.isLoading) {
            keyboard?.hide()
            focus.clearFocus()
            onIniciarSesion()
        }
    }
    Box(modifier.fillMaxSize().background(Brand.Background)) {
        FondoAcceso(Modifier.matchParentSize())
        BoxWithConstraints(Modifier.fillMaxSize().safeDrawingPadding().imePadding()) {
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())
                    .heightIn(min = maxHeight)
                    .padding(horizontal = 20.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Surface(
                    modifier = Modifier.size(72.dp).shadow(20.dp, RoundedCornerShape(24.dp),
                        ambientColor = Brand.Teal.copy(alpha = 0.13f), spotColor = Brand.Teal.copy(alpha = 0.18f)),
                    shape = RoundedCornerShape(24.dp), color = Color.White
                ) {
                    Box(contentAlignment = Alignment.Center) { DailySyncLogo() }
                }
                Spacer(Modifier.height(20.dp))
                TextoAcceso("TU ESPACIO DIARIO", 10, Brand.Teal, FontWeight.SemiBold,
                    letterSpacing = 2.6f)
                Spacer(Modifier.height(12.dp))
                TextoAcceso("DailySync", 28, Brand.Ink, FontWeight.Bold, letterSpacing = -1.1f)
                Spacer(Modifier.height(8.dp))
                TextoAcceso("Un momento para registrar cómo estás.", 13, Brand.Muted,
                    modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                Spacer(Modifier.height(26.dp))
                Surface(
                    modifier = Modifier.widthIn(max = 420.dp).fillMaxWidth()
                        .shadow(28.dp, RoundedCornerShape(28.dp),
                            ambientColor = Brand.Teal.copy(alpha = 0.05f), spotColor = Brand.Teal.copy(alpha = 0.08f)),
                    shape = RoundedCornerShape(28.dp), color = Color.White.copy(alpha = 0.97f)
                ) {
                    Column(Modifier.padding(24.dp)) {
                        TextoAcceso("Qué gusto verte", 18, Brand.Ink, FontWeight.SemiBold)
                        state.avisoSesion?.let {
                            Spacer(Modifier.height(12.dp))
                            TextoAcceso(it, 12, Brand.Muted,
                                modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite })
                        }
                        Spacer(Modifier.height(22.dp))
                        CampoAcceso(
                            etiqueta = "ID o Correo electrónico", valor = state.usuarioOrEmail,
                            onCambio = onCambioUsuario, placeholder = "tu@correo.com",
                            icono = R.drawable.ic_login_mail, error = state.errorUsuario,
                            enabled = !state.isLoading, tag = "login_usuario",
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                            keyboardActions = KeyboardActions(onNext = { focus.moveFocus(FocusDirection.Down) })
                        )
                        Spacer(Modifier.height(20.dp))
                        CampoAcceso(
                            etiqueta = "Contraseña", valor = state.password, onCambio = onCambioPassword,
                            placeholder = "••••••••", icono = R.drawable.ic_login_lock,
                            error = state.errorPassword, enabled = !state.isLoading, tag = "login_password",
                            transformation = if (state.isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = { iniciarSesion() }),
                            trailingIcon = {
                                IconButton(onClick = onTogglePasswordVisibility, enabled = !state.isLoading) {
                                    Icon(painterResource(if (state.isPasswordVisible) R.drawable.ic_login_eye_off else R.drawable.ic_login_eye),
                                        contentDescription = if (state.isPasswordVisible) "Ocultar contraseña" else "Mostrar contraseña",
                                        modifier = Modifier.size(18.dp), tint = Brand.Muted.copy(alpha = 0.8f))
                                }
                            }
                        )
                        state.error?.let {
                            Spacer(Modifier.height(12.dp))
                            TextoAcceso(it, 12, Brand.Error, modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite })
                        }
                        Spacer(Modifier.height(18.dp))
                        val buttonShape = RoundedCornerShape(16.dp)
                        Button(
                            onClick = iniciarSesion, enabled = !state.isLoading, shape = buttonShape,
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent,
                                disabledContainerColor = Color.Transparent, contentColor = Color.White),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
                            modifier = Modifier.fillMaxWidth().heightIn(min = 50.dp)
                                .background(Brush.horizontalGradient(listOf(Brand.Teal, Brand.Emerald)), buttonShape)
                        ) {
                            if (state.isLoading) {
                                CircularProgressIndicator(Modifier.size(20.dp).semantics { contentDescription = "Iniciando sesión" },
                                    color = Color.White, strokeWidth = 2.dp)
                            } else TextoAcceso("Iniciar sesión", 14, Color.White, FontWeight.SemiBold)
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                TextButton(onClick = onIrARegistro, enabled = !state.isLoading,
                    shape = RoundedCornerShape(16.dp), contentPadding = PaddingValues(horizontal = 8.dp, vertical = 12.dp)) {
                    Text(buildAnnotatedString {
                        withStyle(SpanStyle(color = Brand.Muted)) { append("¿Aún no tienes cuenta?  ") }
                        withStyle(SpanStyle(color = Color(0xFF0F766E), fontWeight = FontWeight.SemiBold)) { append("Crear cuenta") }
                    }, style = estiloAcceso(12), textAlign = TextAlign.Center)
                }
                TextButton(onClick = {
                    keyboard?.hide()
                    focus.clearFocus()
                    onAccesoPrueba()
                }, enabled = !state.isLoading, shape = RoundedCornerShape(16.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)) {
                    TextoAcceso("Usar acceso de prueba", 12, Brand.Muted, FontWeight.Medium)
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun LoginPreview() {
    DailySyncTheme { LoginScreen(LoginUiState(), {}, {}, {}, {}, {}) }
}

@Preview(showBackground = true, widthDp = 320, heightDp = 640, fontScale = 1.3f)
@Composable
private fun LoginCompactoPreview() {
    DailySyncTheme { LoginScreen(LoginUiState(errorPassword = "La contraseña es incorrecta."), {}, {}, {}, {}, {}) }
}
