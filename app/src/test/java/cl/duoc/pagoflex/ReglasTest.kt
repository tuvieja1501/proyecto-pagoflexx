package cl.duoc.pagoflex

import cl.duoc.pagoflex.domain.EstadoCompromiso
import cl.duoc.pagoflex.domain.Formatos
import cl.duoc.pagoflex.domain.Rules
import cl.duoc.pagoflex.domain.Rut
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class ReglasTest {

    @Test fun rutValidoEInvalido() {
        assertTrue(Rut.esValido("12.345.678-5"))
        assertFalse(Rut.esValido("12.345.678-9"))
    }

    @Test fun formatoDePesosYFechas() {
        assertEquals("$ 520.000", Formatos.pesos(520000))
        assertEquals("06-10-2026", Formatos.fecha(LocalDate.of(2026, 10, 6)))
        assertNull(Formatos.parseFecha("2026-10-06"))
    }

    @Test fun totalIncluyeRecargo() {
        assertEquals(525000, Rules.totalAPagar(520000, 5000))
    }

    @Test fun soloPendienteOVencidoSePuedePagar() {
        assertTrue(Rules.puedePagar(EstadoCompromiso.PENDIENTE))
        assertTrue(Rules.puedePagar(EstadoCompromiso.VENCIDO))
        assertFalse(Rules.puedePagar(EstadoCompromiso.PAGADO))
        assertFalse(Rules.puedePagar(EstadoCompromiso.EN_REVISION))
        assertFalse(Rules.puedePagar(EstadoCompromiso.ANULADO))
    }

    @Test fun cumplimientoNecesitaTresCompromisos() {
        val hoy = LocalDate.of(2026, 10, 6)
        val dos = listOf(
            Rules.ItemCumplimiento(LocalDate.of(2026, 9, 1), EstadoCompromiso.PAGADO, LocalDate.of(2026, 9, 1)),
            Rules.ItemCumplimiento(LocalDate.of(2026, 8, 1), EstadoCompromiso.PAGADO, LocalDate.of(2026, 8, 1))
        )
        assertNull(Rules.cumplimiento(dos, hoy).porcentaje)
    }
}
