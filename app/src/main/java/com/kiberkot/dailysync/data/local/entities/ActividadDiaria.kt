package com.kiberkot.dailysync.data.local.entities

/** Lightweight projection: the Home does not need notes or entire history records. */
data class ActividadDiaria(val fecha: String, val cantidad: Int)
