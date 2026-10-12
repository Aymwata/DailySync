package com.kiberkot.dailysync.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kiberkot.dailysync.model.RegistroAnimo
import kotlinx.coroutines.flow.Flow

@Dao
interface AnimoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(registro: RegistroAnimo)

    @Query("SELECT * FROM registro_animo ORDER BY fechaHora DESC")
    fun obtenerTodos(): Flow<List<RegistroAnimo>>

    @Query("SELECT * FROM registro_animo WHERE id = :id LIMIT 1")
    suspend fun obtenerPorId(id: String): RegistroAnimo?
}