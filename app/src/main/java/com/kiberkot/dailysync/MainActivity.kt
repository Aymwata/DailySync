package com.kiberkot.dailysync

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.ViewModelProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.kiberkot.dailysync.viewmodel.LoginViewModel
import com.kiberkot.dailysync.navigation.AppNavigation
import com.kiberkot.dailysync.ui.theme.DailySyncTheme

class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ -> }

    override fun onCreate(savedInstanceState: Bundle?) {
        val splash = installSplashScreen()
        super.onCreate(savedInstanceState)
        val login = ViewModelProvider(this)[LoginViewModel::class.java]
        splash.setKeepOnScreenCondition { login.uiState.value.restaurandoSesion }
        splash.setOnExitAnimationListener { it.remove() }
        enableEdgeToEdge()
        setContent {
            DailySyncTheme {
                val sesion by login.uiState.collectAsState()
                LaunchedEffect(sesion.restaurandoSesion, sesion.errorSesion) {
                    if (!sesion.restaurandoSesion && sesion.errorSesion == null) solicitarPermisoNotificaciones()
                }
                AppNavigation(login)
            }
        }
    }

    private fun solicitarPermisoNotificaciones() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permisoConcedido = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

            if (!permisoConcedido) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}
