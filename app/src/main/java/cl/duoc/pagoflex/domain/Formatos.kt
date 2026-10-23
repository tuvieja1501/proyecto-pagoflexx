package cl.duoc.pagoflex.domain

import java.time.DateTimeException
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.ResolverStyle

object Formatos {
    private val FECHA = DateTimeFormatter.ofPattern("dd-MM-uuuu")
    private val FECHA_ESTRICTA = DateTimeFormatter.ofPattern("dd-MM-uuuu").withResolverStyle(ResolverStyle.STRICT)
    private val FECHA_HORA = DateTimeFormatter.ofPattern("dd-MM-uuuu HH:mm")

    private val MESES = listOf(
        "enero", "febrero", "marzo", "abril", "mayo", "junio",
        "julio", "agosto", "septiembre", "octubre", "noviembre", "diciembre"
    )

    fun pesos(monto: Int): String = "$ " + agrupar(monto.toLong())

    fun pesos(monto: Long): String = "$ " + agrupar(monto)

    private fun agrupar(n: Long): String {
        val signo = if (n < 0) "-" else ""
        val digitos = kotlin.math.abs(n).toString()
        return signo + digitos.reversed().chunked(3).joinToString(".").reversed()
    }

    fun fecha(d: LocalDate): String = d.format(FECHA)

    fun fechaHora(dt: LocalDateTime): String = dt.format(FECHA_HORA)

    fun fechaIso(iso: String): String = fecha(LocalDate.parse(iso))

    fun fechaHoraIso(iso: String): String = fechaHora(LocalDateTime.parse(iso))

    fun parseFecha(texto: String): LocalDate? =
        try {
            LocalDate.parse(texto.trim(), FECHA_ESTRICTA)
        } catch (e: DateTimeException) {
            null
        }

    fun porcentaje(valor: Double): String {
        val redondeado = Math.round(valor * 10.0)
        return "${redondeado / 10},${redondeado % 10}%"
    }

    fun nombreMes(mes: Int): String = MESES[mes - 1]

    fun mesAnio(anio: Int, mes: Int): String = "${nombreMes(mes)} $anio"
}
