package com.kiberkot.dailysync.data.repository

import com.kiberkot.dailysync.data.local.dao.DailySyncDao
import com.kiberkot.dailysync.model.AmbitoRegistro
import com.kiberkot.dailysync.model.RegistroHistorial
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class HistorialRepository(
    private val dao: DailySyncDao
) {

    fun observarHistorial(
        usuarioId: String
    ): Flow<List<RegistroHistorial>> {
        return combine(
            observarAnimo(usuarioId),
            observarDbt(usuarioId),
            observarNeuro(usuarioId),
            observarAdicciones(usuarioId)
        ) { animo, dbt, neuro, adicciones ->

            (animo + dbt + neuro + adicciones)
                .sortedByDescending { it.fechaHora }
        }
    }

    private fun observarAnimo(
        usuarioId: String
    ): Flow<List<RegistroHistorial>> {
        return combine(
            dao.observarAnimoPorUsuario(usuarioId),
            dao.obtenerCatalogoAnimo()
        ) { registros, catalogo ->

            val categorias = catalogo.associateBy {
                it.idCategoria
            }

            registros.map { registro ->
                RegistroHistorial(
                    id = registro.idRegistro,
                    ambito = AmbitoRegistro.ANIMO,
                    titulo = categorias[registro.idCategoria]
                        ?.nombreCategoria ?: "Estado de ánimo",
                    descripcion = registro.contexto,
                    fechaHora = registro.fechaHora,
                    intensidad = registro.nivelIntensidad,
                    pendienteSync = registro.pendienteSync
                )
            }
        }
    }

    private fun observarDbt(
        usuarioId: String
    ): Flow<List<RegistroHistorial>> {
        return combine(
            dao.observarDbtPorUsuario(usuarioId),
            dao.obtenerCatalogoDbt()
        ) { registros, catalogo ->

            val habilidades = catalogo.associateBy {
                it.idHabilidad
            }

            registros.map { registro ->
                RegistroHistorial(
                    id = registro.idRegistro,
                    ambito = AmbitoRegistro.DBT,
                    titulo = habilidades[registro.idHabilidad]
                        ?.nombreHabilidad ?: "Práctica DBT",
                    descripcion = registro.situacionAsociada,
                    fechaHora = registro.fechaHora,
                    intensidad = registro.nivelIntensidad,
                    pendienteSync = registro.pendienteSync
                )
            }
        }
    }

    private fun observarNeuro(
        usuarioId: String
    ): Flow<List<RegistroHistorial>> {
        return combine(
            dao.observarNeuroPorUsuario(usuarioId),
            dao.obtenerCatalogoNeuro()
        ) { registros, catalogo ->

            val estrategias = catalogo.associateBy {
                it.idEstrategia
            }

            registros.map { registro ->
                RegistroHistorial(
                    id = registro.idRegistro,
                    ambito = AmbitoRegistro.NEURODESARROLLO,
                    titulo = estrategias[registro.idEstrategia]
                        ?.nombreEstrategia ?: "Neurodesarrollo",
                    descripcion = registro.contexto,
                    fechaHora = registro.fechaHora,
                    intensidad = registro.nivelSobrecarga,
                    pendienteSync = registro.pendienteSync
                )
            }
        }
    }

    private fun observarAdicciones(
        usuarioId: String
    ): Flow<List<RegistroHistorial>> {
        return combine(
            dao.observarAdiccionesPorUsuario(usuarioId),
            dao.obtenerCatalogoAdiccion()
        ) { registros, catalogo ->

            val acciones = catalogo.associateBy {
                it.idAccionProtectora
            }

            registros.map { registro ->
                RegistroHistorial(
                    id = registro.idRegistro,
                    ambito = AmbitoRegistro.ADICCIONES,
                    titulo = acciones[registro.idAccionProtectora]
                        ?.nombreAccion ?: "Registro de impulso",
                    descripcion = registro.desencadenante,
                    fechaHora = registro.fechaHora,
                    intensidad = registro.nivelImpulso,
                    pendienteSync = registro.pendienteSync
                )
            }
        }
    }

    suspend fun eliminar(
        registro: RegistroHistorial,
        usuarioId: String
    ) {
        val filasEliminadas = when (registro.ambito) {
            AmbitoRegistro.ANIMO ->
                dao.eliminarAnimoDelUsuario(registro.id, usuarioId)

            AmbitoRegistro.DBT ->
                dao.eliminarDbtDelUsuario(registro.id, usuarioId)

            AmbitoRegistro.NEURODESARROLLO ->
                dao.eliminarNeuroDelUsuario(registro.id, usuarioId)

            AmbitoRegistro.ADICCIONES ->
                dao.eliminarAdiccionDelUsuario(registro.id, usuarioId)
        }

        check(filasEliminadas == 1) {
            "El registro ya no está disponible."
        }
    }
}