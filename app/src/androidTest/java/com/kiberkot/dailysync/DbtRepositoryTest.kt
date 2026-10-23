package com.kiberkot.dailysync

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.kiberkot.dailysync.data.local.AppDatabase
import com.kiberkot.dailysync.data.local.entities.UsuarioPruebaEntity
import com.kiberkot.dailysync.data.repository.DbtRepository
import com.kiberkot.dailysync.data.repository.HistorialRepository
import com.kiberkot.dailysync.model.FormularioDbt
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class DbtRepositoryTest {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private lateinit var db: AppDatabase
    private lateinit var repository: DbtRepository
    private var archivoPrueba: String? = null

    @Before fun preparar() = runBlocking {
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        repository = DbtRepository(db.dailySyncDao())
        repository.prepararCatalogo()
        db.dailySyncDao().asegurarUsuarioDbt(UsuarioPruebaEntity("demo_a", "Alias A"))
        db.dailySyncDao().asegurarUsuarioDbt(UsuarioPruebaEntity("demo_b", "Alias B"))
    }

    @After fun cerrar() {
        db.close()
        archivoPrueba?.let { context.deleteDatabase(it) }
    }

    @Test fun creaEditaIntegraHistorialYEliminaSinPerderIdentidad() = runBlocking {
        val id = repository.guardar("demo_a", null, FormularioDbt(2, "  Situación ficticia  ", 4, 2))
        val creado = requireNotNull(repository.observarRegistro(id, "demo_a").first())
        assertEquals("STOP", creado.habilidadNombre)
        assertEquals("Situación ficticia", creado.situacion)
        assertTrue(creado.pendienteSync)
        repository.prepararCatalogo()
        assertNotNull(repository.observarRegistro(id, "demo_a").first())
        repository.guardar("demo_a", id, FormularioDbt(1, "Situación corregida", 1, 5))
        val editado = requireNotNull(repository.observarRegistro(id, "demo_a").first())
        assertEquals(creado.id, editado.id)
        assertEquals(creado.fechaHora, editado.fechaHora)
        assertEquals(5, editado.utilidad)
        assertEquals("Mente Sabia", editado.habilidadNombre)
        assertEquals(id, HistorialRepository(db.dailySyncDao()).observarHistorial("demo_a").first().single().id)
        repository.eliminar("demo_a", id)
        assertNull(repository.observarRegistro(id, "demo_a").first())
    }

    @Test fun impideConsultarEditarYEliminarRegistrosAjenos() = runBlocking {
        val formulario = FormularioDbt(1, "Contexto ficticio")
        val id = repository.guardar("demo_a", null, formulario)
        assertNull(repository.observarRegistro(id, "demo_b").first())
        try {
            repository.guardar("demo_b", id, formulario)
            fail("No debe editar registros ajenos")
        } catch (_: IllegalStateException) { }
        try {
            repository.eliminar("demo_b", id)
            fail("No debe eliminar registros ajenos")
        } catch (_: IllegalStateException) { }
        assertNotNull(repository.observarRegistro(id, "demo_a").first())
    }

    @Test fun actualizaTextosDelCatalogoSinBorrarPracticasExistentes() = runBlocking {
        val id = repository.guardar("demo_a", null, FormularioDbt(2, "Práctica que debe conservarse"))
        val habilidad = requireNotNull(db.dailySyncDao().buscarHabilidadDbt(2))
        db.dailySyncDao().asegurarCatalogoDbt(listOf(habilidad.copy(contenidoPsicoeducativo = "Texto anterior")))

        repository.prepararCatalogo()

        val catalogo = repository.observarCatalogo().first()
        assertEquals(4, catalogo.size)
        catalogo.forEach {
            assertTrue(it.contenido.contains("Para practicar:"))
            assertTrue(it.contenido.contains("Ejemplo ficticio:"))
        }
        assertEquals("Práctica que debe conservarse", repository.observarRegistro(id, "demo_a").first()?.situacion)
    }

    @Test fun conservaLaPracticaAlCerrarYReabrirSQLite() = runBlocking {
        db.close()
        val nombre = "dbt-prueba-${UUID.randomUUID()}.db"
        archivoPrueba = nombre
        db = Room.databaseBuilder(context, AppDatabase::class.java, nombre).build()
        repository = DbtRepository(db.dailySyncDao())
        repository.prepararCatalogo()
        val id = repository.guardar(DbtRepository.USUARIO_DEMO, null, FormularioDbt(2, "Prueba persistencia"))
        db.close()
        db = Room.databaseBuilder(context, AppDatabase::class.java, nombre).build()
        repository = DbtRepository(db.dailySyncDao())
        assertEquals("Prueba persistencia", repository.observarRegistro(id, DbtRepository.USUARIO_DEMO).first()?.situacion)
    }
}
