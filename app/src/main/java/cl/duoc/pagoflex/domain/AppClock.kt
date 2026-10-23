package cl.duoc.pagoflex.domain

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

object AppClock {
    const val USAR_FECHA_REFERENCIA = true

    val FECHA_REFERENCIA: LocalDate = LocalDate.of(2026, 10, 6)

    fun hoy(): LocalDate =
        if (USAR_FECHA_REFERENCIA) FECHA_REFERENCIA else LocalDate.now()

    fun ahora(): LocalDateTime {
        val hora = LocalTime.now().withSecond(0).withNano(0)
        return if (USAR_FECHA_REFERENCIA) LocalDateTime.of(FECHA_REFERENCIA, hora)
        else LocalDateTime.of(LocalDate.now(), hora)
    }
}
