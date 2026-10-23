package cl.duoc.pagoflex.data

import cl.duoc.pagoflex.domain.EstadoCompromiso
import cl.duoc.pagoflex.domain.Rules
import java.time.LocalDate
import java.time.LocalDateTime

sealed interface Resultado<out T> {
    data class Exito<T>(val valor: T) : Resultado<T>
    data class Fallo(val mensaje: String) : Resultado<Nothing>
}

data class CompromisoVista(
    val id: Int,
    val folio: String,
    val idUsuario: Int,
    val idEmpresa: Int,
    val empresa: String,
    val concepto: String,
    val cuota: Int?,
    val totalCuotas: Int?,
    val monto: Int,
    val vencimiento: LocalDate,
    val idEstado: Int,
    val estado: String,
    val recargo: Int,
    val fechaCambioEstado: LocalDateTime
) {

    val totalAPagar: Int get() = Rules.totalAPagar(monto, recargo)

    val cuotaTexto: String? get() = if (cuota != null && totalCuotas != null) "$cuota/$totalCuotas" else null

    val estaVencido: Boolean get() = idEstado == EstadoCompromiso.VENCIDO
}

data class ComprobanteVista(
    val idPago: Int,
    val numero: String,
    val idCompromiso: Int,
    val folio: String,
    val fechaHora: LocalDateTime,
    val pagadoPor: String,
    val rutEnmascarado: String,
    val empresa: String,
    val razonSocial: String,
    val concepto: String,
    val cuotaTexto: String?,
    val vencimiento: LocalDate,
    val montoCuota: Int,
    val recargo: Int,
    val totalPagado: Int,
    val medioPago: String,
    val canal: String,

    val aTiempo: Boolean,
    val diasDeAtraso: Int
)

sealed interface ItemHistorial {
    val fecha: LocalDateTime
    val idEmpresa: Int
    val empresa: String

    data class DePago(
        val comprobante: ComprobanteVista,
        override val idEmpresa: Int
    ) : ItemHistorial {
        override val fecha: LocalDateTime get() = comprobante.fechaHora
        override val empresa: String get() = comprobante.empresa
    }

    data class DeAnulacion(
        val compromiso: CompromisoVista,
        val motivo: String,
        override val fecha: LocalDateTime
    ) : ItemHistorial {
        override val idEmpresa: Int get() = compromiso.idEmpresa
        override val empresa: String get() = compromiso.empresa
    }
}

data class ReporteVista(
    val id: Int,
    val numero: String,
    val idCompromiso: Int,
    val folio: String,
    val empresa: String,
    val concepto: String,
    val cuotaTexto: String?,
    val monto: Int,
    val idEstadoCompromiso: Int,
    val estadoCompromiso: String,
    val idUsuarioFinal: Int,
    val nombrePersona: String,
    val rutPersona: String,
    val motivo: String,
    val comentario: String?,
    val fechaCreacion: LocalDateTime,
    val idEstado: Int,
    val estado: String,
    val idAgente: Int?,
    val nombreAgente: String?,
    val fechaToma: LocalDateTime?,
    val respuesta: String?,
    val fechaResolucion: LocalDateTime?,

    val atrasado: Boolean
)

data class DetalleEjecutivo(
    val compromiso: CompromisoVista,
    val nombrePersona: String,
    val rutPersona: String,
    val correoPersona: String?,
    val telefonoPersona: String?,
    val reporte: ReporteVista?,
    val comprobante: ComprobanteVista?,
    val anulacion: AnulacionVista?,
    val registradoPor: String
)

data class AnulacionVista(
    val motivo: String,
    val comentario: String?,
    val ejecutivo: String,
    val fecha: LocalDateTime
)

data class DatosCompromiso(
    val rut: String,
    val nombres: String,
    val apellidos: String,
    val correo: String,
    val telefono: String,
    val concepto: String,
    val monto: Int,
    val vencimiento: LocalDate,
    val cuota: Int?,
    val totalCuotas: Int?
)

data class FilaEjecutivo(
    val compromiso: CompromisoVista,
    val nombrePersona: String,
    val rutPersona: String
)
