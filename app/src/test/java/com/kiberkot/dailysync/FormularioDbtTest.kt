package com.kiberkot.dailysync

import com.kiberkot.dailysync.model.FormularioDbt
import org.junit.Assert.*
import org.junit.Test

class FormularioDbtTest {
    private val habilidades = setOf(1, 2)

    @Test fun aceptaFormularioValido() {
        assertTrue(FormularioDbt(1, "Situación ficticia", 1, 5).errores(habilidades).isEmpty())
    }

    @Test fun exigeHabilidadDelCatalogo() {
        listOf(null, 99).forEach { id ->
            assertTrue(FormularioDbt(id, "Prueba").errores(habilidades).containsKey("habilidad"))
        }
    }

    @Test fun rechazaSituacionVaciaCortaYLarga() {
        listOf("   ", " ab ", "x".repeat(251)).forEach { texto ->
            assertTrue(FormularioDbt(1, texto).errores(habilidades).containsKey("situacion"))
        }
    }

    @Test fun aceptaLimitesDeSituacionRecortandoEspacios() {
        listOf(" abc ", "x".repeat(250)).forEach { texto ->
            assertTrue(FormularioDbt(1, texto).errores(habilidades).isEmpty())
        }
    }

    @Test fun validaAmbasEscalas() {
        listOf(0, 6).forEach { valor ->
            val errores = FormularioDbt(1, "Prueba", valor, valor).errores(habilidades)
            assertTrue(errores.containsKey("intensidad"))
            assertTrue(errores.containsKey("utilidad"))
        }
    }
}
