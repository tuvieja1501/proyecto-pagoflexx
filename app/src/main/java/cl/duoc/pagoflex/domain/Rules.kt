package cl.duoc.pagoflex.domain

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.ChronoUnit

object Rules {

    fun diasAtraso(vencimiento: LocalDate, hoy: LocalDate): Int =
        ChronoUnit.DAYS.between(vencimiento, hoy).toInt().coerceAtLeast(0)

    fun totalAPagar(monto: Int, recargoAplicado: Int): Int = monto + recargoAplicado

    fun puedePagar(idEstado: Int): Boolean =
        idEstado == EstadoCompromiso.PENDIENTE || idEstado == EstadoCompromiso.VENCIDO

    fun puedeReportar(idEstado: Int): Boolean = puedePagar(idEstado)

    fun puedeAnular(idEstado: Int): Boolean =
        idEstado == EstadoCompromiso.PENDIENTE ||
            idEstado == EstadoCompromiso.VENCIDO ||
            idEstado == EstadoCompromiso.EN_REVISION

    fun estadoTrasResolver(idEstadoActual: Int, vencimiento: LocalDate, hoy: LocalDate): Int =
        when {
            idEstadoActual == EstadoCompromiso.ANULADO -> EstadoCompromiso.ANULADO
            idEstadoActual == EstadoCompromiso.PAGADO -> EstadoCompromiso.PAGADO
            vencimiento.isBefore(hoy) -> EstadoCompromiso.VENCIDO
            else -> EstadoCompromiso.PENDIENTE
        }

    data class ItemOrden(val idCompromiso: Int, val idEmpresa: Int, val idEstado: Int, val vencimiento: LocalDate)

    fun compromisoPrevioAPagar(objetivo: ItemOrden, delUsuario: List<ItemOrden>): ItemOrden? =
        delUsuario
            .filter {
                it.idEmpresa == objetivo.idEmpresa &&
                    it.idCompromiso != objetivo.idCompromiso &&
                    puedePagar(it.idEstado) &&
                    it.vencimiento.isBefore(objetivo.vencimiento)
            }
            .minByOrNull { it.vencimiento }

    data class ItemCumplimiento(val vencimiento: LocalDate, val idEstado: Int, val fechaPago: LocalDate?)

    data class Cumplimiento(
        val considerados: Int,
        val aTiempo: Int,

        val porcentaje: Double?,
        val categoria: String?
    )

    fun cumplimiento(items: List<ItemCumplimiento>, hoy: LocalDate): Cumplimiento {
        val desde = hoy.minusMonths(12)
        val considerados = items.filter {
            it.idEstado != EstadoCompromiso.ANULADO &&
                it.idEstado != EstadoCompromiso.EN_REVISION &&
                it.vencimiento.isBefore(hoy) &&
                !it.vencimiento.isBefore(desde)
        }
        val aTiempo = considerados.count { it.fechaPago != null && !it.fechaPago.isAfter(it.vencimiento) }
        if (considerados.size < 3) return Cumplimiento(considerados.size, aTiempo, null, null)
        val pct = redondear1(aTiempo * 100.0 / considerados.size)
        return Cumplimiento(considerados.size, aTiempo, pct, categoria(pct))
    }

    fun categoria(porcentaje: Double): String = when {
        porcentaje >= 95.0 -> "Ejemplar"
        porcentaje >= 80.0 -> "Al día"
        else -> "En camino"
    }

    fun diasHabiles(desde: LocalDate, hasta: LocalDate): Int {
        var cuenta = 0
        var dia = desde.plusDays(1)
        while (!dia.isAfter(hasta)) {
            if (dia.dayOfWeek != DayOfWeek.SATURDAY && dia.dayOfWeek != DayOfWeek.SUNDAY) cuenta++
            dia = dia.plusDays(1)
        }
        return cuenta
    }

    fun reporteAtrasado(idEstadoReporte: Int, fechaCreacion: LocalDate, hoy: LocalDate): Boolean =
        idEstadoReporte == EstadoReporte.ABIERTO && diasHabiles(fechaCreacion, hoy) > 2

    data class ItemCobranza(val idEstado: Int, val montoPagado: Int?)

    data class Cobranza(
        val total: Int,
        val anulados: Int,
        val pagados: Int,
        val pendientes: Int,
        val vencidos: Int,
        val enRevision: Int,
        val montoRecaudado: Long,

        val tasa: Double?
    )

    fun cobranza(items: List<ItemCobranza>): Cobranza {
        val anulados = items.count { it.idEstado == EstadoCompromiso.ANULADO }
        val pagados = items.count { it.idEstado == EstadoCompromiso.PAGADO }
        val base = items.size - anulados
        val tasa = if (base == 0) null else redondear1(pagados * 100.0 / base)
        return Cobranza(
            total = items.size,
            anulados = anulados,
            pagados = pagados,
            pendientes = items.count { it.idEstado == EstadoCompromiso.PENDIENTE },
            vencidos = items.count { it.idEstado == EstadoCompromiso.VENCIDO },
            enRevision = items.count { it.idEstado == EstadoCompromiso.EN_REVISION },
            montoRecaudado = items.filter { it.idEstado == EstadoCompromiso.PAGADO }
                .sumOf { (it.montoPagado ?: 0).toLong() },
            tasa = tasa
        )
    }

    fun redondear1(valor: Double): Double = Math.round(valor * 10.0) / 10.0
}
