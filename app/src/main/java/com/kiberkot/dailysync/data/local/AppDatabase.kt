package com.kiberkot.dailysync.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.kiberkot.dailysync.data.local.dao.DailySyncDao
import com.kiberkot.dailysync.data.local.entities.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        UsuarioPruebaEntity::class,
        CatalogoAnimoEntity::class,
        RegistroAnimoEntity::class,
        CatalogoDbtEntity::class,
        RegistroDbtEntity::class,
        CatalogoNeuroEntity::class,
        RegistroNeuroEntity::class,
        CatalogoAdiccionEntity::class,
        RegistroAdiccionEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun dailySyncDao(): DailySyncDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "dailysync_offline_db"
                )
                    .addCallback(PrepopulateCallback(context))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        // Carga inicial de datos ficticios y catálogos sintéticos[cite: 1, 6]
        private class PrepopulateCallback(private val context: Context) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                CoroutineScope(Dispatchers.IO).launch {
                    val dao = getDatabase(context).dailySyncDao()

                    // Usuario Ficticio de Prueba[cite: 1, 6]
                    dao.insertarUsuario(
                        UsuarioPruebaEntity(
                            idUsuario = "usr_demo_01",
                            aliasFicticio = "UsuarioPrueba",
                            tokenSesion = "token_ficticio_123",
                            recordatorioActivo = true,
                            horaRecordatorio = "20:00"
                        )
                    )

                    // Catálogo de Estado de Ánimo[cite: 6]
                    dao.insertarCatalogoAnimo(
                        listOf(
                            CatalogoAnimoEntity(1, "Tranquilo / Calmo"),
                            CatalogoAnimoEntity(2, "Motivado / Con energía"),
                            CatalogoAnimoEntity(3, "Ansioso / Inquieto"),
                            CatalogoAnimoEntity(4, "Abrumado / Estresado"),
                            CatalogoAnimoEntity(5, "Desanimado / Triste")
                        )
                    )

                    // Catálogo DBT[cite: 6]
                    dao.insertarCatalogoDbt(
                        listOf(
                            CatalogoDbtEntity(1, "Mente Sabia", "Mindfulness", "Aprende a integrar emoción y lógica."),
                            CatalogoDbtEntity(2, "STOP", "Tolerancia al Malestar", "Pausa antes de reaccionar impulsivamente."),
                            CatalogoDbtEntity(3, "DEAR MAN", "Efectividad Interpersonal", "Expresa tus necesidades con firmeza.")
                        )
                    )
                }
            }
        }
    }
}