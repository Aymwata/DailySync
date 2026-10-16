package com.kiberkot.dailysync.ui.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kiberkot.dailysync.viewmodel.RegisterUiState

@Composable
fun RegisterScreen(
    state: RegisterUiState,
    onCambioId: (String) -> Unit,
    onCambioAlias: (String) -> Unit,
    onCambioCorreo: (String) -> Unit,
    onCambioPassword: (String) -> Unit,
    onTogglePasswordVisibility: () -> Unit,
    onRegistrar: () -> Unit,
    onVolverLogin: () -> Unit,
    modifier: Modifier = Modifier
) {
    val gradientColors = listOf(Color(0xFF006C59), Color(0xFF02927A))

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(90.dp)
                .clip(CircleShape)
                .background(Brush.horizontalGradient(gradientColors)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "📝", fontSize = 42.sp)
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Crear Cuenta",
            style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Registro local seguro con Room DB",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(36.dp))

        OutlinedTextField(
            value = state.idUsuario,
            onValueChange = onCambioId,
            label = { Text("ID de Usuario") },
            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
            isError = state.errorId != null,
            supportingText = { state.errorId?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
            shape = RoundedCornerShape(18.dp),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = state.aliasFicticio,
            onValueChange = onCambioAlias,
            label = { Text("Alias Ficticio") },
            leadingIcon = { Icon(Icons.Default.AccountCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
            isError = state.errorAlias != null,
            supportingText = { state.errorAlias?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
            shape = RoundedCornerShape(18.dp),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Campo de Correo Electrónico
        OutlinedTextField(
            value = state.correo,
            onValueChange = onCambioCorreo,
            label = { Text("Correo Electrónico") },
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            isError = state.errorCorreo != null,
            supportingText = { state.errorCorreo?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
            shape = RoundedCornerShape(18.dp),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = state.password,
            onValueChange = onCambioPassword,
            label = { Text("Contraseña") },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
            trailingIcon = {
                TextButton(onClick = onTogglePasswordVisibility) {
                    Text(if (state.isPasswordVisible) "Ocultar" else "Mostrar")
                }
            },
            visualTransformation = if (state.isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            isError = state.errorPassword != null,
            supportingText = { state.errorPassword?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
            shape = RoundedCornerShape(18.dp),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onRegistrar,
            enabled = !state.isLoading,
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Text("Registrarse", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        TextButton(onClick = onVolverLogin) {
            Text("¿Ya tienes cuenta? Inicia sesión")
        }
    }
}