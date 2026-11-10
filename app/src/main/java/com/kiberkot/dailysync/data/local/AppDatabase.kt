package com.kiberkot.dailysync.data.local

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
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
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun dailySyncDao(): DailySyncDao

    companion object {
        val MIGRATION_4_5 = object : androidx.room.migration.Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Antes eran datos de ejemplo sin programación real. La nueva función requiere activación explícita.
                db.execSQL("UPDATE USUARIO_PRUEBA SET recordatorio_activo = 0")
            }
        }

        val MIGRATION_3_4 = object : androidx.room.migration.Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                listOf("REGISTRO_ANIMO", "REGISTRO_DBT", "REGISTRO_NEURO", "REGISTRO_ADICCION").forEach { tabla ->
                    db.execSQL("ALTER TABLE $tabla ADD COLUMN version_escala INTEGER NOT NULL DEFAULT 1")
                    db.execSQL("ALTER TABLE $tabla ADD COLUMN editado INTEGER NOT NULL DEFAULT 0")
                }
                db.execSQL("ALTER TABLE REGISTRO_ADICCION ADD COLUMN otra_accion TEXT")
                db.execSQL("ALTER TABLE USUARIO_PRUEBA ADD COLUMN objetivo_adiccion TEXT NOT NULL DEFAULT 'Aplicar una acción protectora ante un impulso'")
            }
        }

        const val USUARIO_DEMO = "usr_demo_01"

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
            CatalogoAdiccionEntity(idAccionProtectora = 4, nombreAccion = "Distracción cognitiva inmediata"),
            CatalogoAdiccionEntity(idAccionProtectora = 5, nombreAccion = "Otra (especificar)")
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
                    .addMigrations(MIGRATION_3_4, MIGRATION_4_5)
                    .addCallback(PrepopulateCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    // Se ejecuta antes de entregar la base abierta al DAO: no hay cargas en paralelo.
    internal class PrepopulateCallback : RoomDatabase.Callback() {
        override fun onOpen(db: SupportSQLiteDatabase) {
            super.onOpen(db)
            db.beginTransaction()
            try {
                poblarDatosIniciales(db)
                db.setTransactionSuccessful()
            } finally {
                db.endTransaction()
            }
        }

        private fun poblarDatosIniciales(db: SupportSQLiteDatabase) {
            val usuario = ContentValues().apply {
                put("id_usuario", USUARIO_DEMO)
                put("alias_ficticio", "UsuarioPrueba")
                put("correo", "demo@dailysync.app")
                put("password", "1234")
                put("token_sesion", "token_ficticio_123")
                put("recordatorio_activo", false)
                put("hora_recordatorio", "20:00")
            }
            db.insert("USUARIO_PRUEBA", SQLiteDatabase.CONFLICT_IGNORE, usuario)
            // Completar instalaciones antiguas sin reemplazar el usuario ni borrar sus registros.
            db.execSQL("""
                UPDATE USUARIO_PRUEBA SET
                    correo = CASE WHEN correo = '' THEN ? ELSE correo END,
                    password = CASE WHEN password = '' THEN ? ELSE password END
                WHERE id_usuario = ?
            """, arrayOf("demo@dailysync.app", "1234", USUARIO_DEMO))

            catalogoAnimoInicial.forEach { categoria ->
                actualizarCatalogo(db, "CATALOGO_ANIMO", "id_categoria", categoria.idCategoria,
                    ContentValues().apply { put("nombre_categoria", categoria.nombreCategoria) })
            }
            catalogoDbtInicial.forEach { habilidad ->
                actualizarCatalogo(db, "CATALOGO_DBT", "id_habilidad", habilidad.idHabilidad,
                    ContentValues().apply {
                        put("nombre_habilidad", habilidad.nombreHabilidad)
                        put("modulo_dbt", habilidad.moduloDbt)
                        put("contenido_psicoeducativo", habilidad.contenidoPsicoeducativo)
                    })
            }
            catalogoNeuroInicial.forEach { estrategia ->
                actualizarCatalogo(db, "CATALOGO_NEURO", "id_estrategia", estrategia.idEstrategia,
                    ContentValues().apply { put("nombre_estrategia", estrategia.nombreEstrategia) })
            }
            catalogoAdiccionInicial.forEach { accion ->
                actualizarCatalogo(db, "CATALOGO_ADICCION", "id_accion_protectora", accion.idAccionProtectora,
                    ContentValues().apply { put("nombre_accion", accion.nombreAccion) })
            }
        }

        private fun actualizarCatalogo(
            db: SupportSQLiteDatabase, tabla: String, columnaId: String,
            id: Int, valores: ContentValues
        ) {
            valores.put(columnaId, id)
            db.insert(tabla, SQLiteDatabase.CONFLICT_IGNORE, valores)
            // Actualizar la fila existente evita el DELETE implícito de REPLACE y su CASCADE.
            db.update(tabla, SQLiteDatabase.CONFLICT_ABORT, valores,
                "$columnaId = ?", arrayOf(id))
        }
    }
}
