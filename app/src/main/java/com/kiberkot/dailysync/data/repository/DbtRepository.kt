package com.kiberkot.dailysync.data.repository

import com.kiberkot.dailysync.data.local.dao.DailySyncDao
import com.kiberkot.dailysync.model.HabilidadDbt
import com.kiberkot.dailysync.model.FormularioDbt
import com.kiberkot.dailysync.model.RegistroDbt
import com.kiberkot.dailysync.data.local.AppDatabase
import com.kiberkot.dailysync.data.local.entities.RegistroDbtEntity
import com.kiberkot.dailysync.data.local.entities.UsuarioPruebaEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class DbtRepository(
    private val dao: DailySyncDao
) {

    companion object {
        // Coincide con Ánimo y el historial actuales. Sustituir al integrar la sesión.
        const val USUARIO_DEMO = "usr_demo_01"
    }

    suspend fun prepararCatalogo() {
        dao.asegurarCatalogoDbt(AppDatabase.catalogoDbtInicial)
    }

    suspend fun prepararUsuario(usuarioId: String) {
        // Solo se crea el usuario académico conocido; no se inventan cuentas de sesión.
        if (usuarioId == USUARIO_DEMO) {
            dao.asegurarUsuarioDbt(UsuarioPruebaEntity(usuarioId, "UsuarioPrueba"))
        }
        check(dao.obtenerUsuario(usuarioId) != null) { "El usuario no está disponible. Vuelve a iniciar sesión." }
    }

    fun observarRegistro(id: String, usuarioId: String): Flow<RegistroDbt?> = combine(
        dao.observarPracticaDbt(id, usuarioId), dao.obtenerCatalogoDbt()
    ) { entidad, catalogo ->
        entidad?.let {
            val habilidad = catalogo.find { habilidad -> habilidad.idHabilidad == it.idHabilidad }
            RegistroDbt(it.idRegistro, it.idUsuario, it.idHabilidad,
                habilidad?.nombreHabilidad ?: "Habilidad DBT", habilidad?.moduloDbt.orEmpty(),
                it.fechaHora, it.situacionAsociada, it.nivelIntensidad, it.utilidadPercibida, it.pendienteSync)
        }
    }

    suspend fun guardar(usuarioId: String, id: String?, formulario: FormularioDbt): String {
        prepararUsuario(usuarioId)
        val habilidad = formulario.habilidadId?.let { dao.buscarHabilidadDbt(it) }
        require(formulario.errores(setOfNotNull(habilidad?.idHabilidad)).isEmpty()) { "Revisa los datos del formulario." }
        val habilidadId = requireNotNull(formulario.habilidadId)
        if (id != null) {
            check(dao.actualizarPracticaDbt(id, usuarioId, habilidadId, formulario.situacion.trim(),
                formulario.intensidad, formulario.utilidad) == 1) { "El registro ya no está disponible." }
            return id
        }
        val nuevoId = UUID.randomUUID().toString()
        dao.crearPracticaDbt(RegistroDbtEntity(
            idRegistro = nuevoId,
            idUsuario = usuarioId,
            idHabilidad = habilidadId,
            fechaHora = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.ROOT).format(Date()),
            situacionAsociada = formulario.situacion.trim(),
            nivelIntensidad = formulario.intensidad,
            utilidadPercibida = formulario.utilidad,
            pendienteSync = true
        ))
        return nuevoId
    }

    suspend fun eliminar(usuarioId: String, id: String) {
        check(dao.eliminarDbtDelUsuario(id, usuarioId) == 1) { "El registro ya no está disponible." }
    }

    fun observarCatalogo(): Flow<List<HabilidadDbt>> {
        return dao.obtenerCatalogoDbt()
            .map { entidades ->
                entidades.map { entidad ->
                    HabilidadDbt(
                        id = entidad.idHabilidad,
                        nombre = entidad.nombreHabilidad,
                        modulo = entidad.moduloDbt,
                        contenido = entidad.contenidoPsicoeducativo
                    )
                }
            }
    }
}
