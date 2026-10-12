package com.kiberkot.dailysync.data.local.dao

import androidx.room.*
import com.kiberkot.dailysync.data.local.entities.*
import kotlinx.coroutines.flow.Flow

@Dao
interface DailySyncDao {

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

    // --- REGISTROS DE ÁNIMO ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarRegistroAnimo(registro: RegistroAnimoEntity)

    @Query("SELECT * FROM REGISTRO_ANIMO ORDER BY fecha_hora DESC")
    fun obtenerRegistrosAnimo(): Flow<List<RegistroAnimoEntity>>

    @Query("SELECT * FROM REGISTRO_ANIMO WHERE pendiente_sync = 1")
    suspend fun obtenerAnimoPendientesSync(): List<RegistroAnimoEntity>

    // --- REGISTROS DE DBT ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarRegistroDbt(registro: RegistroDbtEntity)

    @Query("SELECT * FROM REGISTRO_DBT ORDER BY fecha_hora DESC")
    fun obtenerRegistrosDbt(): Flow<List<RegistroDbtEntity>>

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
}