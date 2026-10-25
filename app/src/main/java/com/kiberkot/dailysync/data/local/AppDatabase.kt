package com.kiberkot.dailysync.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.kiberkot.dailysync.data.local.dao.DailySyncDao

// --- IMPORTS EXPLÍCITOS DE ENTIDADES ---
import com.kiberkot.dailysync.data.local.entities.UsuarioPruebaEntity
import com.kiberkot.dailysync.data.local.entities.CatalogoAnimoEntity
import com.kiberkot.dailysync.data.local.entities.RegistroAnimoEntity
import com.kiberkot.dailysync.data.local.entities.CatalogoDbtEntity
import com.kiberkot.dailysync.data.local.entities.RegistroDbtEntity
import com.kiberkot.dailysync.data.local.entities.CatalogoNeuroEntity
import com.kiberkot.dailysync.data.local.entities.RegistroNeuroEntity
import com.kiberkot.dailysync.data.local.entities.CatalogoAdiccionEntity
import com.kiberkot.dailysync.data.local.entities.RegistroAdiccionEntity

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
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun dailySyncDao(): DailySyncDao

    companion object {

        val catalogoAnimoInicial = listOf(
            CatalogoAnimoEntity(1, "Tranquilo / Calmo"),
            CatalogoAnimoEntity(2, "Motivado / Con energía"),
            CatalogoAnimoEntity(3, "Ansioso / Inquieto"),
            CatalogoAnimoEntity(4, "Abrumado / Estresado"),
            CatalogoAnimoEntity(5, "Desanimado / Triste")
        )

        val catalogoDbtInicial = listOf(
            CatalogoDbtEntity(
                idHabilidad = 1,
                nombreHabilidad = "Mente Sabia",
                moduloDbt = "Mindfulness / Atención plena",
                contenidoPsicoeducativo = """
                    Consiste en tomar una decisión considerando tanto lo que sientes como los hechos y tus objetivos.

                    Para practicar:
                    1. Pon nombre a tu emoción y al impulso que aparece.
                    2. Separa los hechos comprobables de tus suposiciones.
                    3. Elige una respuesta que tenga en cuenta tus necesidades y las consecuencias.

                    Ejemplo ficticio:
                    Un compañero tarda en responder. Sientes molestia y quieres reclamar. Reconoces que no sabes por qué tarda y decides preguntar con calma antes de sacar conclusiones.
                """.trimIndent()
            ),
            CatalogoDbtEntity(
                idHabilidad = 2,
                nombreHabilidad = "STOP",
                moduloDbt = "Tolerancia al Malestar",
                contenidoPsicoeducativo = """
                    Es una pausa breve antes de actuar por impulso. Su nombre recuerda cuatro pasos.

                    Para practicar:
                    1. S — Detente: deja pasar un momento antes de responder.
                    2. T — Toma distancia: haz una pausa y respira.
                    3. O — Observa: identifica qué ocurre, qué piensas y qué sientes.
                    4. P — Procede con atención: elige una acción acorde con tu objetivo.

                    Ejemplo ficticio:
                    Recibes un mensaje que te molesta. Antes de contestar, apartas el teléfono, reconoces tu enojo y decides responder cuando puedas expresar tu idea con claridad.
                """.trimIndent()
            ),
            CatalogoDbtEntity(
                idHabilidad = 3,
                nombreHabilidad = "DEAR MAN",
                moduloDbt = "Efectividad Interpersonal",
                contenidoPsicoeducativo = """
                    Es una guía para pedir algo o decir que no de forma clara y respetuosa. Cada letra recuerda una parte de la conversación.

                    Para practicar:
                    D — Describe los hechos sin acusaciones.
                    E — Expresa cómo te afecta la situación.
                    A — Pide claramente lo que necesitas o comunica tu límite.
                    R — Explica el beneficio de llegar a un acuerdo.
                    M — Mantén el foco en tu objetivo.
                    A — Adopta un tono seguro y respetuoso.
                    N — Negocia alternativas posibles.

                    Ejemplo ficticio:
                    «Aún no recibo tu parte del trabajo. Me preocupa el plazo. ¿Podrías enviarla a las seis? Así podremos revisarla juntos. Si no puedes, acordemos otro reparto».
                """.trimIndent()
            ),
            CatalogoDbtEntity(
                idHabilidad = 4,
                nombreHabilidad = "Acción Opuesta",
                moduloDbt = "Regulación Emocional",
                contenidoPsicoeducativo = """
                    Consiste en elegir una conducta distinta al impulso de una emoción cuando ese impulso no ayuda a tus objetivos o la emoción no se ajusta a los hechos.

                    Para practicar:
                    1. Identifica la emoción y lo que te impulsa a hacer.
                    2. Revisa los hechos y comprueba que la situación es segura.
                    3. Si seguir el impulso no te ayuda, elige una acción opuesta, pequeña y concreta.

                    Ejemplo ficticio:
                    Te da vergüenza hacer una pregunta en clase, aunque el profesor invita a preguntar. En vez de callarte, levantas la mano y haces una pregunta breve.
                """.trimIndent()
            )
        )

        val catalogoNeuroInicial = listOf(
            CatalogoNeuroEntity(1, "Pausa sensorial / Aislamiento acústico"),
            CatalogoNeuroEntity(2, "Desglose en microtareas (Pomodoro)"),
            CatalogoNeuroEntity(3, "Respiración cuadrada 4x4"),
            CatalogoNeuroEntity(4, "Uso de apoyos visuales / Checklist")
        )

        val catalogoAdiccionInicial = listOf(
            CatalogoAdiccionEntity(idAccionProtectora = 1, nombreAccion = "Surfear el impulso (Urge Surfing - 10 min)"),
            CatalogoAdiccionEntity(idAccionProtectora = 2, nombreAccion = "Contacto con red de apoyo"),
            CatalogoAdiccionEntity(idAccionProtectora = 3, nombreAccion = "Cambio de entorno / Actividad física breve"),
            CatalogoAdiccionEntity(idAccionProtectora = 4, nombreAccion = "Distracción cognitiva inmediata")
        )
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "dailysync_offline_db"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(PrepopulateCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class PrepopulateCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        val dao = database.dailySyncDao()

                        // Usuario Ficticio de Prueba Inicial (Contraseña: 1234)
                        dao.insertarUsuario(
                            UsuarioPruebaEntity(
                                idUsuario = "usr_demo_01",
                                aliasFicticio = "UsuarioPrueba",
                                correo = "demo@dailysync.app",
                                password = "1234",
                                tokenSesion = "token_ficticio_123",
                                recordatorioActivo = true,
                                horaRecordatorio = "20:00"
                            )
                        )

                        dao.insertarCatalogoAnimo(catalogoAnimoInicial)
                        dao.asegurarCatalogoDbt(catalogoDbtInicial)
                        dao.insertarCatalogoNeuro(catalogoNeuroInicial)
                        dao.insertarCatalogoAdiccion(catalogoAdiccionInicial)
                    }
                }
            }
        }
    }
}
