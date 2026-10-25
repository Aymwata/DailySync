package com.kiberkot.dailysync.data.local.dao

import androidx.room.*
import com.kiberkot.dailysync.data.local.entities.*
import kotlinx.coroutines.flow.Flow

@Dao
interface DailySyncDao {
    // --- CONSULTAS Y ACTUALIZACIÓN NEURODESARROLLO ---
    @Query("SELECT * FROM REGISTRO_NEURO WHERE id_registro = :id AND id_usuario = :usuarioId LIMIT 1")
    fun observarRegistroNeuro(id: String, usuarioId: String): Flow<RegistroNeuroEntity?>

    @Update
    suspend fun actualizarRegistroNeuro(registro: RegistroNeuroEntity)

    // --- CONSULTAS Y ACTUALIZACIÓN ADICCIÓN / CRAVING ---
    @Query("SELECT * FROM REGISTRO_ADICCION WHERE id_registro = :id AND id_usuario = :usuarioId LIMIT 1")
    fun observarRegistroAdiccion(id: String, usuarioId: String): Flow<RegistroAdiccionEntity?>

    @Update
    suspend fun actualizarRegistroAdiccion(registro: RegistroAdiccionEntity)
    @Query("SELECT * FROM USUARIO_PRUEBA WHERE id_usuario = :busqueda OR correo = :busqueda LIMIT 1")
    suspend fun obtenerUsuarioPorIdOCorreo(busqueda: String): UsuarioPruebaEntity?
    // --- USUARIO Y SESIÓN ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarUsuario(usuario: UsuarioPruebaEntity)

    @Query("SELECT * FROM USUARIO_PRUEBA WHERE id_usuario = :idUsuario LIMIT 1")
    suspend fun obtenerUsuario(idUsuario: String): UsuarioPruebaEntity?

    // --- CATÁLOGOS ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarCatalogoAnimo(lista: List<CatalogoAnimoEntity>)

    @Query("SELECT * FROM CATALOGO_ANIMO")
    fun obtenerCatalogoAnimo(): Flow<List<CatalogoAnimoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarCatalogoDbt(lista: List<CatalogoDbtEntity>)

    @Query("SELECT * FROM CATALOGO_DBT")
    fun obtenerCatalogoDbt(): Flow<List<CatalogoDbtEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarCatalogoNeuro(lista: List<CatalogoNeuroEntity>)

    @Query("SELECT * FROM CATALOGO_NEURO")
    fun obtenerCatalogoNeuro(): Flow<List<CatalogoNeuroEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarCatalogoAdiccion(lista: List<CatalogoAdiccionEntity>)

    @Query("SELECT * FROM CATALOGO_ADICCION")
    fun obtenerCatalogoAdiccion(): Flow<List<CatalogoAdiccionEntity>>

    // --- REGISTROS DE ÁNIMO ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarRegistroAnimo(registro: RegistroAnimoEntity)

    @Query("SELECT * FROM REGISTRO_ANIMO ORDER BY fecha_hora DESC")
    fun obtenerRegistrosAnimo(): Flow<List<RegistroAnimoEntity>>

    @Query("SELECT * FROM REGISTRO_ANIMO WHERE pendiente_sync = 1")
    suspend fun obtenerAnimoPendientesSync(): List<RegistroAnimoEntity>

    @Query("DELETE FROM REGISTRO_ANIMO WHERE id_registro = :idRegistro")
    suspend fun eliminarRegistroAnimoPorId(idRegistro: String)

    @Update
    suspend fun actualizarRegistroAnimo(registro: RegistroAnimoEntity)

    // --- REGISTROS DE DBT ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarRegistroDbt(registro: RegistroDbtEntity)

    @Query("SELECT * FROM REGISTRO_DBT ORDER BY fecha_hora DESC")
    fun obtenerRegistrosDbt(): Flow<List<RegistroDbtEntity>>

    // Actualiza los textos existentes sin borrar la habilidad ni sus prácticas asociadas.
    @Upsert
    suspend fun asegurarCatalogoDbt(habilidades: List<CatalogoDbtEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun asegurarUsuarioDbt(usuario: UsuarioPruebaEntity)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun crearPracticaDbt(registro: RegistroDbtEntity)

    @Query("SELECT * FROM REGISTRO_DBT WHERE id_registro = :id AND id_usuario = :usuarioId LIMIT 1")
    fun observarPracticaDbt(id: String, usuarioId: String): Flow<RegistroDbtEntity?>

    @Query("SELECT * FROM CATALOGO_DBT WHERE id_habilidad = :id LIMIT 1")
    suspend fun buscarHabilidadDbt(id: Int): CatalogoDbtEntity?

    @Query("""
        UPDATE REGISTRO_DBT SET id_habilidad = :habilidadId,
            situacion_asociada = :situacion, nivel_intensidad = :intensidad,
            utilidad_percibida = :utilidad, pendiente_sync = 1
        WHERE id_registro = :id AND id_usuario = :usuarioId
    """)
    suspend fun actualizarPracticaDbt(
        id: String, usuarioId: String, habilidadId: Int,
        situacion: String, intensidad: Int, utilidad: Int
    ): Int

    // --- REGISTROS DE NEURODESARROLLO ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarRegistroNeuro(registro: RegistroNeuroEntity)

    @Query("SELECT * FROM REGISTRO_NEURO ORDER BY fecha_hora DESC")
    fun obtenerRegistrosNeuro(): Flow<List<RegistroNeuroEntity>>

    // --- REGISTROS DE ADICCIÓN / CRAVING ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarRegistroAdiccion(registro: RegistroAdiccionEntity)

    @Query("SELECT * FROM REGISTRO_ADICCION ORDER BY fecha_hora DESC")
    fun obtenerRegistrosAdiccion(): Flow<List<RegistroAdiccionEntity>>

    // --- HISTORIAL POR USUARIO ---

    @Query("""
    SELECT * FROM REGISTRO_ANIMO
    WHERE id_usuario = :usuarioId
    ORDER BY fecha_hora DESC
""")
    fun observarAnimoPorUsuario(
        usuarioId: String
    ): Flow<List<RegistroAnimoEntity>>

    @Query("""
    SELECT * FROM REGISTRO_DBT
    WHERE id_usuario = :usuarioId
    ORDER BY fecha_hora DESC
""")
    fun observarDbtPorUsuario(
        usuarioId: String
    ): Flow<List<RegistroDbtEntity>>

    @Query("""
    SELECT * FROM REGISTRO_NEURO
    WHERE id_usuario = :usuarioId
    ORDER BY fecha_hora DESC
""")
    fun observarNeuroPorUsuario(
        usuarioId: String
    ): Flow<List<RegistroNeuroEntity>>

    @Query("""
    SELECT * FROM REGISTRO_ADICCION
    WHERE id_usuario = :usuarioId
    ORDER BY fecha_hora DESC
""")
    fun observarAdiccionesPorUsuario(
        usuarioId: String
    ): Flow<List<RegistroAdiccionEntity>>

    // --- ELIMINACIÓN DESDE EL HISTORIAL ---

    @Query("""
    DELETE FROM REGISTRO_ANIMO
    WHERE id_registro = :id AND id_usuario = :usuarioId
""")
    suspend fun eliminarAnimoDelUsuario(
        id: String,
        usuarioId: String
    ): Int

    @Query("""
    DELETE FROM REGISTRO_DBT
    WHERE id_registro = :id AND id_usuario = :usuarioId
""")
    suspend fun eliminarDbtDelUsuario(
        id: String,
        usuarioId: String
    ): Int

    @Query("""
    DELETE FROM REGISTRO_NEURO
    WHERE id_registro = :id AND id_usuario = :usuarioId
""")
    suspend fun eliminarNeuroDelUsuario(
        id: String,
        usuarioId: String
    ): Int

    @Query("""
    DELETE FROM REGISTRO_ADICCION
    WHERE id_registro = :id AND id_usuario = :usuarioId
""")
    suspend fun eliminarAdiccionDelUsuario(
        id: String,
        usuarioId: String
    ): Int
}
