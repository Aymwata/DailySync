package com.kiberkot.dailysync

import com.kiberkot.dailysync.utils.formatearFechaRegistro
import com.kiberkot.dailysync.viewmodel.construirActividadSemanal
import org.junit.Assert.*
import org.junit.Test
import java.util.Calendar

class ActividadSemanalTest {
    private val hoy = Calendar.getInstance().apply {
        set(2026, Calendar.OCTOBER, 23, 12, 0, 0)
        set(Calendar.MILLISECOND, 0)
    }

    @Test fun cuentaTodosLosRegistrosSoloDentroDeLosSieteDias() {
        val dias = construirActividadSemanal(listOf(
            "2026-10-16 23:59", // Fuera del intervalo.
            "2026-10-17 00:00", // Primer día incluido.
            "2026-10-22 10:20",
            "2026-10-23 03:17", // Ánimo.
            "2026-10-23 03:16:53.306", // DBT existente.
            "2026-10-24 00:00", // Día futuro.
            "fecha inválida",
            "2026-02-30 12:00"
        ), hoy)
        assertEquals(listOf(1, 0, 0, 0, 0, 1, 2), dias.map { it.cantidad })
        assertEquals(4, dias.sumOf { it.cantidad })
        assertEquals("Hoy", dias.last().etiqueta)
        assertEquals(listOf(false, false, false, false, false, false, true), dias.map { it.esHoy })
    }

    @Test fun sinRegistrosMuestraLosSieteDiasEnCero() {
        assertEquals(List(7) { 0 }, construirActividadSemanal(emptyList(), hoy).map { it.cantidad })
    }

    @Test fun alEliminarUnRegistroDisminuyeElTotal() {
        val fechas = listOf("2026-10-23 10:00", "2026-10-23 10:00:30.123")
        assertEquals(2, construirActividadSemanal(fechas, hoy).last().cantidad)
        assertEquals(1, construirActividadSemanal(fechas.dropLast(1), hoy).last().cantidad)
    }

    @Test fun presentaFechasAntiguasYNuevasSinSegundos() {
        assertEquals("23/10/2026 03:16", formatearFechaRegistro("2026-10-23 03:16:53.306"))
        assertEquals("23/10/2026 03:17", formatearFechaRegistro("2026-10-23 03:17"))
        assertEquals("fecha inválida", formatearFechaRegistro("fecha inválida"))
    }
}
