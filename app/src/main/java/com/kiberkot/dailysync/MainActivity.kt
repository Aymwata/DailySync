package com.kiberkot.dailysync

import android.os.Bundle
import android.view.MotionEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import com.kiberkot.dailysync.viewmodel.LoginViewModel
import com.kiberkot.dailysync.navigation.AppNavigation
import com.kiberkot.dailysync.ui.theme.DailySyncTheme

class MainActivity : ComponentActivity() {
    private lateinit var login: LoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        val splash = installSplashScreen()
        super.onCreate(savedInstanceState)
        login = ViewModelProvider(this)[LoginViewModel::class.java]
        splash.setKeepOnScreenCondition { login.uiState.value.restaurandoSesion }
        splash.setOnExitAnimationListener { it.remove() }
        com.kiberkot.dailysync.utils.NotificationHelper(this).eliminarCanalAnterior()
        enableEdgeToEdge()
        setContent {
            DailySyncTheme {
                AppNavigation(login)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        if (::login.isInitialized) login.verificarSesion()
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                withTimeout(8000) { com.kiberkot.dailysync.reminders.RecordatoriosDiarios(applicationContext).reprogramarTodos() }
            } catch (e: Exception) {
                android.util.Log.w("DailySync", "No se pudo actualizar la programación de recordatorios", e)
            }
        }
    }

    override fun onStop() {
        if (::login.isInitialized) login.guardarActividadActual()
        super.onStop()
    }

    override fun onUserInteraction() {
        super.onUserInteraction()
        if (::login.isInitialized) login.registrarInteraccion()
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (::login.isInitialized) {
            val permitido = if (event.actionMasked == MotionEvent.ACTION_CANCEL) login.verificarSesion()
                else login.registrarInteraccion()
            if (!permitido) return true
        }
        return super.dispatchTouchEvent(event)
    }

    override fun dispatchGenericMotionEvent(event: MotionEvent): Boolean {
        if (::login.isInitialized && !login.registrarInteraccion()) return true
        return super.dispatchGenericMotionEvent(event)
    }

}
