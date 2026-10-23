package com.kiberkot.dailysync

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.filters.SdkSuppress
import com.kiberkot.dailysync.data.local.AppDatabase
import com.kiberkot.dailysync.data.repository.DbtRepository
import com.kiberkot.dailysync.navigation.AppNavigation
import com.kiberkot.dailysync.ui.theme.DailySyncTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith

/** Recorre las pantallas reales y limpia únicamente el registro ficticio creado por la prueba. */
@RunWith(AndroidJUnit4::class)
// Espresso 3.6.1 accede a InputManager.getInstance, eliminado en Android 17.
// Ejecutar esta prueba de interfaz en un emulador API 35; Room también se prueba en API 37.
@SdkSuppress(maxSdkVersion = 35)
class DbtFlujoTest {
    @get:Rule val compose = createComposeRule()
    private val contexto = "DBT prueba UI ${System.currentTimeMillis()}"
    private val corregido = "$contexto corregido"

    private fun esperar(texto: String) {
        compose.waitUntil(15_000) { compose.onAllNodesWithText(texto).fetchSemanticsNodes().isNotEmpty() }
    }

    private fun pulsarDbt(texto: String) {
        compose.onNodeWithTag("dbt_contenido").performScrollToNode(hasText(texto))
        compose.onNodeWithText(texto).performClick()
    }

    @After fun limpiar() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val dao = AppDatabase.getDatabase(context).dailySyncDao()
        dao.observarDbtPorUsuario(DbtRepository.USUARIO_DEMO).first()
            .filter { it.situacionAsociada == contexto || it.situacionAsociada == corregido }
            .forEach { dao.eliminarDbtDelUsuario(it.idRegistro, DbtRepository.USUARIO_DEMO) }
    }

    @Test fun loginCatalogoValidacionGuardarEditarHistorialYEliminar() {
        compose.setContent { DailySyncTheme { AppNavigation() } }
        compose.onNodeWithText("Usuario o Correo").performTextInput(DbtRepository.USUARIO_DEMO)
        compose.onNodeWithText("Contraseña").performTextInput("demo")
        compose.onNodeWithText("Iniciar Sesión").performScrollTo().performClick()
        esperar("Ámbitos de autorreporte")
        compose.onNodeWithText("Habilidades DBT").performScrollTo().performClick()
        esperar("STOP")
        pulsarDbt("STOP")
        pulsarDbt("Registrar práctica de esta habilidad")
        esperar("Registrar práctica")
        pulsarDbt("Guardar práctica")
        compose.onNodeWithTag("dbt_contenido").performScrollToNode(hasText("Escribe entre 3 y 250 caracteres."))
        compose.onNodeWithText("Escribe entre 3 y 250 caracteres.").assertIsDisplayed()
        compose.onNodeWithTag("dbt_contenido").performScrollToNode(hasTestTag("dbt_situacion"))
        compose.onNodeWithTag("dbt_situacion").performTextInput(contexto)
        pulsarDbt("Guardar práctica")
        esperar("Detalle de práctica")
        compose.onNodeWithText(contexto).assertExists()
        pulsarDbt("Editar práctica")
        esperar("Editar práctica")
        compose.onNodeWithTag("dbt_contenido").performScrollToNode(hasTestTag("dbt_situacion"))
        compose.onNodeWithTag("dbt_situacion").performTextReplacement(corregido)
        pulsarDbt("Guardar cambios")
        esperar("Detalle de práctica")
        esperar(corregido)
        pulsarDbt("Ver historial")
        esperar("Mi historial")
        esperar(corregido)
        compose.onAllNodesWithText("Ver detalle").onFirst().performScrollTo().performClick()
        esperar("Detalle de práctica")
        pulsarDbt("Eliminar práctica")
        compose.onNodeWithText("Cancelar").performClick()
        compose.onNodeWithText("Detalle de práctica").assertExists()
        pulsarDbt("Eliminar práctica")
        compose.onNodeWithText("Eliminar").performClick()
        esperar("Mi historial")
        compose.waitUntil(15_000) { compose.onAllNodesWithText(corregido).fetchSemanticsNodes().isEmpty() }
    }
}
