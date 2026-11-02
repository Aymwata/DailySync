package com.kiberkot.dailysync

import android.app.Application
import androidx.lifecycle.ViewModelStore
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.kiberkot.dailysync.data.local.AppDatabase
import com.kiberkot.dailysync.data.local.entities.*
import com.kiberkot.dailysync.data.repository.HistorialRepository
import com.kiberkot.dailysync.model.AmbitoRegistro
import com.kiberkot.dailysync.viewmodel.DbtViewModel
import com.kiberkot.dailysync.viewmodel.RegistroDbtViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import java.util.UUID

/** Prueba el ViewModel real con un DAO de pruebas, sin modificar la base de la demostración. */
@RunWith(AndroidJUnit4::class)
class DbtViewModelTest {
    private val application = InstrumentationRegistry.getInstrumentation()
        .targetContext.applicationContext as Application
    private lateinit var db: AppDatabase
    private lateinit var almacen: ViewModelStore
    private var archivoPrueba: String? = null

    @Before fun preparar() = runBlocking {
        almacen = ViewModelStore()
        db = Room.inMemoryDatabaseBuilder(application, AppDatabase::class.java)
            .addCallback(AppDatabase.PrepopulateCallback()).build()
        // La primera consulta espera a que onOpen complete todos los datos iniciales.
        assertNotNull(db.dailySyncDao().obtenerUsuario(AppDatabase.USUARIO_DEMO))
        db.dailySyncDao().asegurarUsuarioDbt(UsuarioPruebaEntity("demo_a", "Alias A"))
        db.dailySyncDao().asegurarUsuarioDbt(UsuarioPruebaEntity("demo_b", "Alias B"))
    }

    @After fun cerrar() {
        InstrumentationRegistry.getInstrumentation().runOnMainSync { almacen.clear() }
        db.close()
        archivoPrueba?.let { application.deleteDatabase(it) }
    }

    private suspend fun formulario(
        usuario: String = "demo_a", id: String? = null, habilidadId: Int? = 2
    ): RegistroDbtViewModel {
        val vm = withContext(Dispatchers.Main) {
            RegistroDbtViewModel(application, db.dailySyncDao()).also {
                almacen.put(UUID.randomUUID().toString(), it)
                it.cargar(usuario, id, habilidadId)
            }
        }
        withTimeout(10_000) { vm.uiState.first { !it.cargando } }
        return vm
    }

    private suspend fun guardar(vm: RegistroDbtViewModel): String {
        val resultado = CompletableDeferred<String>()
        withContext(Dispatchers.Main) { vm.guardar { resultado.complete(it) } }
        return withTimeout(10_000) { resultado.await() }
    }

    @Test fun creaEditaIntegraHistorialYEliminaConElMismoIdYFecha() = runBlocking {
        val vm = formulario()
        withContext(Dispatchers.Main) {
            vm.cambiarSituacion("  Situación ficticia  ")
            vm.cambiarIntensidad(4)
            vm.cambiarUtilidad(2)
        }
        val id = guardar(vm)
        val creado = requireNotNull(db.dailySyncDao().observarPracticaDbt(id, "demo_a").first())
        assertEquals("Situación ficticia", creado.situacionAsociada)
        assertTrue(creado.pendienteSync)

        val edicion = formulario(id = id)
        withContext(Dispatchers.Main) {
            edicion.seleccionarHabilidad(1)
            edicion.cambiarSituacion("Situación corregida")
            edicion.cambiarUtilidad(5)
        }
        assertEquals(id, guardar(edicion))
        val editado = requireNotNull(db.dailySyncDao().observarPracticaDbt(id, "demo_a").first())
        assertEquals(creado.fechaHora, editado.fechaHora)
        assertEquals(1, editado.idHabilidad)
        assertEquals(5, editado.utilidadPercibida)
        val historial = HistorialRepository(db.dailySyncDao()).observarHistorial("demo_a").first()
        assertEquals(id, historial.single().id)
        assertEquals("Mente Sabia", historial.single().titulo)
        assertEquals(AmbitoRegistro.DBT, historial.single().ambito)

        // El borrado de la app ocurre desde HistorialViewModel mediante este Repository.
        HistorialRepository(db.dailySyncDao()).eliminar(historial.single(), "demo_a")
        withTimeout(10_000) { edicion.uiState.first { !it.cargando && it.registro == null } }
        assertNull(db.dailySyncDao().observarPracticaDbt(id, "demo_a").first())
    }

    @Test fun rechazaFormularioInvalidoSinEscribirYPermiteCorregirlo() = runBlocking {
        val vm = formulario()
        withContext(Dispatchers.Main) { vm.guardar { fail("No debe guardar una situación vacía") } }
        assertTrue(vm.uiState.value.errores.containsKey("situacion"))
        assertTrue(db.dailySyncDao().observarDbtPorUsuario("demo_a").first().isEmpty())
        assertFalse(vm.uiState.value.procesando)

        withContext(Dispatchers.Main) { vm.cambiarSituacion("Situación válida") }
        assertTrue(vm.uiState.value.errores.isEmpty())
        assertNotNull(guardar(vm))
    }

    @Test fun noSobrescribeBorradorCuandoRoomEmiteYCatalogaLosFiltros() = runBlocking {
        val vm = formulario()
        withContext(Dispatchers.Main) { vm.cambiarSituacion("Borrador sin guardar") }
        val habilidad = requireNotNull(db.dailySyncDao().buscarHabilidadDbt(2))
        db.dailySyncDao().asegurarCatalogoDbt(listOf(habilidad.copy(nombreHabilidad = "STOP revisado")))
        withTimeout(10_000) { vm.uiState.first { it.catalogo.any { h -> h.nombre == "STOP revisado" } } }
        assertEquals("Borrador sin guardar", vm.uiState.value.formulario.situacion)

        val catalogo = withContext(Dispatchers.Main) {
            DbtViewModel(application, db.dailySyncDao()).also { almacen.put("catalogo", it) }
        }
        withTimeout(10_000) { catalogo.uiState.first { !it.cargando } }
        assertEquals(4, catalogo.uiState.value.catalogo.size)
        withContext(Dispatchers.Main) { catalogo.seleccionarModulo("Tolerancia al Malestar") }
        assertEquals(listOf(2), catalogo.uiState.value.habilidadesVisibles.map { it.id })
    }

    @Test fun exigeUnaHabilidadExistenteEnElCatalogo() = runBlocking {
        for (habilidadId in listOf(null, 99)) {
            val vm = formulario(habilidadId = habilidadId)
            withContext(Dispatchers.Main) {
                vm.cambiarSituacion("Situación válida")
                vm.guardar { fail("No debe guardar una habilidad inválida") }
            }
            assertEquals(setOf("habilidad"), vm.uiState.value.errores.keys)
        }
        assertTrue(db.dailySyncDao().observarDbtPorUsuario("demo_a").first().isEmpty())
    }

    @Test fun rechazaSituacionesVaciasCortasYLargas() = runBlocking {
        val vm = formulario()
        for (texto in listOf("   ", " ab ", "x".repeat(251))) {
            withContext(Dispatchers.Main) {
                vm.cambiarSituacion(texto)
                vm.guardar { fail("No debe guardar una situación inválida") }
            }
            assertEquals(setOf("situacion"), vm.uiState.value.errores.keys)
        }
        assertTrue(db.dailySyncDao().observarDbtPorUsuario("demo_a").first().isEmpty())
    }

    @Test fun aceptaLosLimitesYRecortaEspaciosAntesDeGuardar() = runBlocking {
        for ((texto, intensidad, utilidad) in listOf(
            Triple(" abc ", 1, 5), Triple("x".repeat(250), 5, 1)
        )) {
            val vm = formulario()
            withContext(Dispatchers.Main) {
                vm.cambiarSituacion(texto)
                vm.cambiarIntensidad(intensidad)
                vm.cambiarUtilidad(utilidad)
            }
            val id = guardar(vm)
            val registro = requireNotNull(db.dailySyncDao().observarPracticaDbt(id, "demo_a").first())
            assertEquals(texto.trim(), registro.situacionAsociada)
            assertEquals(intensidad, registro.nivelIntensidad)
            assertEquals(utilidad, registro.utilidadPercibida)
            assertTrue(vm.uiState.value.errores.isEmpty())
        }
    }

    @Test fun rechazaEscalasFueraDeRangoSinEscribir() = runBlocking {
        val vm = formulario()
        for (valor in listOf(0, 6)) {
            withContext(Dispatchers.Main) {
                vm.cambiarSituacion("Situación válida")
                vm.cambiarIntensidad(valor)
                vm.cambiarUtilidad(valor)
                vm.guardar { fail("No debe guardar escalas inválidas") }
            }
            assertEquals(setOf("intensidad", "utilidad"), vm.uiState.value.errores.keys)
        }
        assertTrue(db.dailySyncDao().observarDbtPorUsuario("demo_a").first().isEmpty())
    }

    @Test fun noPermiteEditarNiEliminarUnaPracticaDeOtroUsuario() = runBlocking {
        val vm = formulario()
        withContext(Dispatchers.Main) { vm.cambiarSituacion("Práctica de A") }
        val id = guardar(vm)
        val ajeno = formulario("demo_b", id)
        assertNull(ajeno.uiState.value.registro)
        assertNotNull(ajeno.uiState.value.error)
        withContext(Dispatchers.Main) {
            ajeno.seleccionarHabilidad(2)
            ajeno.cambiarSituacion("Cambio no permitido")
            ajeno.guardar { fail("No debe editar una práctica ajena") }
        }
        withTimeout(10_000) { ajeno.uiState.first { !it.procesando && it.error != null } }
        assertEquals(0, db.dailySyncDao().eliminarDbtDelUsuario(id, "demo_b"))
        assertEquals("Práctica de A", db.dailySyncDao().observarPracticaDbt(id, "demo_a").first()?.situacionAsociada)
    }

    @Test fun reabrirBaseConservaLosCuatroAmbitosYActualizaCatalogosSinBorrarHijos() = runBlocking {
        db.close()
        val nombre = "persistencia-modulos-${UUID.randomUUID()}.db"
        archivoPrueba = nombre
        fun abrir() = Room.databaseBuilder(application, AppDatabase::class.java, nombre)
            .addCallback(AppDatabase.PrepopulateCallback()).build()
        db = abrir()
        val usuario = AppDatabase.USUARIO_DEMO
        assertNotNull(db.dailySyncDao().obtenerUsuario(usuario))
        val dao = db.dailySyncDao()
        dao.insertarRegistroAnimo(RegistroAnimoEntity("animo", usuario, 1, "2026-11-01 10:00", 3, "Ensayo", null))
        dao.crearPracticaDbt(RegistroDbtEntity("dbt", usuario, 2, "2026-11-01 10:01", "Ensayo", 3, 4))
        dao.insertarRegistroNeuro(RegistroNeuroEntity("neuro", usuario, 1, "2026-11-01 10:02", "Ensayo", 3, "Aula", 4))
        dao.insertarRegistroAdiccion(RegistroAdiccionEntity("adiccion", usuario, 1, "2026-11-01 10:03", 4, "Ensayo", "Logrado"))
        dao.asegurarCatalogoDbt(listOf(requireNotNull(dao.buscarHabilidadDbt(2)).copy(contenidoPsicoeducativo = "Texto antiguo")))

        repeat(2) {
            db.close()
            db = abrir()
            val historial = HistorialRepository(db.dailySyncDao()).observarHistorial(usuario).first()
            assertEquals(listOf("adiccion", "neuro", "dbt", "animo"), historial.map { it.id })
            assertTrue(requireNotNull(db.dailySyncDao().buscarHabilidadDbt(2)).contenidoPsicoeducativo.contains("Para practicar:"))
            assertEquals("1234", db.dailySyncDao().obtenerUsuario(usuario)?.password)
        }
    }
}
