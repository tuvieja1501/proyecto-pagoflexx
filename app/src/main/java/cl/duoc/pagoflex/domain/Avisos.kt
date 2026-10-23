package cl.duoc.pagoflex.domain

import java.time.LocalDate
import java.time.LocalTime
import java.time.temporal.ChronoUnit

enum class TipoAviso { PROXIMO, HOY, VENCIDO }

object Avisos {
    val INICIO_SILENCIO: LocalTime = LocalTime.of(22, 0)
    val FIN_SILENCIO: LocalTime = LocalTime.of(8, 0)

    fun enHorarioSilencioso(hora: LocalTime): Boolean =
        !hora.isBefore(INICIO_SILENCIO) || hora.isBefore(FIN_SILENCIO)

    fun diasRestantes(vencimiento: LocalDate, hoy: LocalDate): Int =
        ChronoUnit.DAYS.between(hoy, vencimiento).toInt()

    fun tipoDeAviso(idEstado: Int, vencimiento: LocalDate, hoy: LocalDate, diasAnticipacion: Int): TipoAviso? {
        val restantes = diasRestantes(vencimiento, hoy)
        return when (idEstado) {
            EstadoCompromiso.PENDIENTE -> when {
                restantes == 0 -> TipoAviso.HOY
                restantes in 1..diasAnticipacion -> TipoAviso.PROXIMO
                else -> null
            }
            EstadoCompromiso.VENCIDO -> TipoAviso.VENCIDO
            else -> null
        }
    }

    fun titulo(tipo: TipoAviso): String = when (tipo) {
        TipoAviso.PROXIMO -> "Se acerca un vencimiento"
        TipoAviso.HOY -> "Hoy es el día de tu pago"
        TipoAviso.VENCIDO -> "Ponte al día cuando quieras"
    }

    fun mensaje(tipo: TipoAviso, nombre: String, concepto: String, empresa: String, restantes: Int, total: Int): String =
        when (tipo) {
            TipoAviso.PROXIMO -> {
                val cuando = if (restantes == 1) "mañana" else "en $restantes días"
                "Hola $nombre: $cuando vence «$concepto» de $empresa (${Formatos.pesos(total)}). " +
                    "Puedes dejarlo listo desde la app."
            }
            TipoAviso.HOY ->
                "Hola $nombre: hoy vence «$concepto» de $empresa (${Formatos.pesos(total)}). " +
                    "Es el último día sin recargo y puedes pagarlo en pocos pasos."
            TipoAviso.VENCIDO ->
                "Hola $nombre: te falta «$concepto» de $empresa para quedar al día (${Formatos.pesos(total)}). " +
                    "Puedes ponerte al día cuando quieras desde la app."
        }
}
