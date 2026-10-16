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
    version = 2, // 1. Se incrementa la versión por el cambio en la tabla
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
                    .fallbackToDestructiveMigration() // 2. Permite recrear la base de datos limpiamente
                    .addCallback(PrepopulateCallback(context))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        // Carga inicial de datos ficticios y catálogos sintéticos (Lemac DataLab)
        private class PrepopulateCallback(private val context: Context) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                CoroutineScope(Dispatchers.IO).launch {
                    val dao = getDatabase(context).dailySyncDao()

                    // 1. Usuario Ficticio de Prueba (con campo correo incluido)
                    dao.insertarUsuario(
                        UsuarioPruebaEntity(
                            idUsuario = "usr_demo_01",
                            aliasFicticio = "UsuarioPrueba",
                            correo = "demo@dailysync.app",
                            tokenSesion = "token_ficticio_123",
                            recordatorioActivo = true,
                            horaRecordatorio = "20:00"
                        )
                    )

                    // 2. Catálogo de Estado de Ánimo
                    dao.insertarCatalogoAnimo(
                        listOf(
                            CatalogoAnimoEntity(1, "Tranquilo / Calmo"),
                            CatalogoAnimoEntity(2, "Motivado / Con energía"),
                            CatalogoAnimoEntity(3, "Ansioso / Inquieto"),
                            CatalogoAnimoEntity(4, "Abrumado / Estresado"),
                            CatalogoAnimoEntity(5, "Desanimado / Triste")
                        )
                    )

                    // 3. Catálogo de Habilidades DBT
                    dao.insertarCatalogoDbt(
                        listOf(
                            CatalogoDbtEntity(
                                1,
                                "Mente Sabia",
                                "Mindfulness",
                                "Integra emoción y razón antes de actuar."
                            ),
                            CatalogoDbtEntity(
                                2,
                                "STOP",
                                "Tolerancia al Malestar",
                                "Detente, da un paso atrás y observa."
                            ),
                            CatalogoDbtEntity(
                                3,
                                "DEAR MAN",
                                "Efectividad Interpersonal",
                                "Comunica necesidades de forma asertiva."
                            ),
                            CatalogoDbtEntity(
                                4,
                                "Acción Opuesta",
                                "Regulación Emocional",
                                "Actúa en dirección opuesta al impulso emocional."
                            )
                        )
                    )

                    // 4. Catálogo de Estrategias de Neurodesarrollo
                    dao.insertarCatalogoNeuro(
                        listOf(
                            CatalogoNeuroEntity(1, "Pausa sensorial / Aislamiento acústico"),
                            CatalogoNeuroEntity(2, "Desglose en microtareas (Pomodoro)"),
                            CatalogoNeuroEntity(3, "Respiración cuadrada 4x4"),
                            CatalogoNeuroEntity(4, "Uso de apoyos visuales / Checklist")
                        )
                    )

                    // 5. Catálogo de Acciones Protectoras (Adicciones)
                    dao.insertarCatalogoAdiccion(
                        listOf(
                            CatalogoAdiccionEntity(1, "Surfear el impulso (Urge Surfing - 10 min)"),
                            CatalogoAdiccionEntity(2, "Contacto con red de apoyo"),
                            CatalogoAdiccionEntity(3, "Cambio de entorno / Actividad física breve"),
                            CatalogoAdiccionEntity(4, "Distracción cognitiva inmediata")
                        )
                    )
                }
            }
        }
    }
}