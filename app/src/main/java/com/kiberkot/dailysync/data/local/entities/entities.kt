package com.kiberkot.dailysync.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

// 1. USUARIO_PRUEBA
@Entity(tableName = "USUARIO_PRUEBA")
data class UsuarioPruebaEntity(
    @PrimaryKey @ColumnInfo(name = "id_usuario") val idUsuario: String,
    @ColumnInfo(name = "alias_ficticio") val aliasFicticio: String,
    @ColumnInfo(name = "token_sesion") val tokenSesion: String? = null,
    @ColumnInfo(name = "recordatorio_activo") val recordatorioActivo: Boolean = false,
    @ColumnInfo(name = "hora_recordatorio") val horaRecordatorio: String? = null
)

// --- MÓDULO ESTADO DE ÁNIMO ---
// 2. CATALOGO_ANIMO[cite: 6]
@Entity(tableName = "CATALOGO_ANIMO")
data class CatalogoAnimoEntity(
    @PrimaryKey @ColumnInfo(name = "id_categoria") val idCategoria: Int,
    @ColumnInfo(name = "nombre_categoria") val nombreCategoria: String
)

// 3. REGISTRO_ANIMO[cite: 6]
@Entity(
    tableName = "REGISTRO_ANIMO",
    foreignKeys = [
        ForeignKey(entity = UsuarioPruebaEntity::class, parentColumns = ["id_usuario"], childColumns = ["id_usuario"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = CatalogoAnimoEntity::class, parentColumns = ["id_categoria"], childColumns = ["id_categoria"], onDelete = ForeignKey.CASCADE)
    ]
)
data class RegistroAnimoEntity(
    @PrimaryKey @ColumnInfo(name = "id_registro") val idRegistro: String,
    @ColumnInfo(name = "id_usuario") val idUsuario: String,
    @ColumnInfo(name = "id_categoria") val idCategoria: Int,
    @ColumnInfo(name = "fecha_hora") val fechaHora: String,
    @ColumnInfo(name = "nivel_intensidad") val nivelIntensidad: Int,
    @ColumnInfo(name = "contexto") val contexto: String,
    @ColumnInfo(name = "nota_opcional") val notaOpcional: String? = null,
    @ColumnInfo(name = "pendiente_sync") val pendienteSync: Boolean = true
)

// --- MÓDULO HABILIDADES DBT ---
// 4. CATALOGO_DBT[cite: 6]
@Entity(tableName = "CATALOGO_DBT")
data class CatalogoDbtEntity(
    @PrimaryKey @ColumnInfo(name = "id_habilidad") val idHabilidad: Int,
    @ColumnInfo(name = "nombre_habilidad") val nombreHabilidad: String,
    @ColumnInfo(name = "modulo_dbt") val moduloDbt: String,
    @ColumnInfo(name = "contenido_psicoeducativo") val contenidoPsicoeducativo: String
)

// 5. REGISTRO_DBT[cite: 6]
@Entity(
    tableName = "REGISTRO_DBT",
    foreignKeys = [
        ForeignKey(entity = UsuarioPruebaEntity::class, parentColumns = ["id_usuario"], childColumns = ["id_usuario"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = CatalogoDbtEntity::class, parentColumns = ["id_habilidad"], childColumns = ["id_habilidad"], onDelete = ForeignKey.CASCADE)
    ]
)
data class RegistroDbtEntity(
    @PrimaryKey @ColumnInfo(name = "id_registro") val idRegistro: String,
    @ColumnInfo(name = "id_usuario") val idUsuario: String,
    @ColumnInfo(name = "id_habilidad") val idHabilidad: Int,
    @ColumnInfo(name = "fecha_hora") val fechaHora: String,
    @ColumnInfo(name = "situacion_asociada") val situacionAsociada: String,
    @ColumnInfo(name = "nivel_intensidad") val nivelIntensidad: Int,
    @ColumnInfo(name = "utilidad_percibida") val utilidadPercibida: Int,
    @ColumnInfo(name = "pendiente_sync") val pendienteSync: Boolean = true
)

// --- MÓDULO NEURODESARROLLO ---
// 6. CATALOGO_NEURO[cite: 6]
@Entity(tableName = "CATALOGO_NEURO")
data class CatalogoNeuroEntity(
    @PrimaryKey @ColumnInfo(name = "id_estrategia") val idEstrategia: Int,
    @ColumnInfo(name = "nombre_estrategia") val nombreEstrategia: String
)

// 7. REGISTRO_NEURO[cite: 6]
@Entity(
    tableName = "REGISTRO_NEURO",
    foreignKeys = [
        ForeignKey(entity = UsuarioPruebaEntity::class, parentColumns = ["id_usuario"], childColumns = ["id_usuario"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = CatalogoNeuroEntity::class, parentColumns = ["id_estrategia"], childColumns = ["id_estrategia"], onDelete = ForeignKey.CASCADE)
    ]
)
data class RegistroNeuroEntity(
    @PrimaryKey @ColumnInfo(name = "id_registro") val idRegistro: String,
    @ColumnInfo(name = "id_usuario") val idUsuario: String,
    @ColumnInfo(name = "id_estrategia") val idEstrategia: Int,
    @ColumnInfo(name = "fecha_hora") val fechaHora: String,
    @ColumnInfo(name = "tipo_actividad") val tipoActividad: String,
    @ColumnInfo(name = "nivel_sobrecarga") val nivelSobrecarga: Int,
    @ColumnInfo(name = "contexto") val contexto: String,
    @ColumnInfo(name = "resultado_percibido") val resultadoPercibido: Int,
    @ColumnInfo(name = "pendiente_sync") val pendienteSync: Boolean = true
)

// --- MÓDULO ADICCIONES ---
// 8. CATALOGO_ADICCION[cite: 6]
@Entity(tableName = "CATALOGO_ADICCION")
data class CatalogoAdiccionEntity(
    @PrimaryKey @ColumnInfo(name = "id_accion_protectora") val idAccionProtectora: Int,
    @ColumnInfo(name = "nombre_accion") val nombreAccion: String
)

// 9. REGISTRO_ADICCION[cite: 6]
@Entity(
    tableName = "REGISTRO_ADICCION",
    foreignKeys = [
        ForeignKey(entity = UsuarioPruebaEntity::class, parentColumns = ["id_usuario"], childColumns = ["id_usuario"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = CatalogoAdiccionEntity::class, parentColumns = ["id_accion_protectora"], childColumns = ["id_accion_protectora"], onDelete = ForeignKey.CASCADE)
    ]
)
data class RegistroAdiccionEntity(
    @PrimaryKey @ColumnInfo(name = "id_registro") val idRegistro: String,
    @ColumnInfo(name = "id_usuario") val idUsuario: String,
    @ColumnInfo(name = "id_accion_protectora") val idAccionProtectora: Int,
    @ColumnInfo(name = "fecha_hora") val fechaHora: String,
    @ColumnInfo(name = "nivel_impulso") val nivelImpulso: Int,
    @ColumnInfo(name = "desencadenante") val desencadenante: String,
    @ColumnInfo(name = "estado_objetivo") val estadoObjetivo: String,
    @ColumnInfo(name = "pendiente_sync") val pendienteSync: Boolean = true
)