package cl.duoc.pagoflex.data.seed

import java.time.LocalDate
import java.time.LocalDateTime

data class SeedUsuario(
    val id: Int,

    val codigo: String,
    val idRol: Int,
    val rut: String,
    val nombres: String,
    val apellidos: String,
    val correo: String,
    val telefono: String?,
    val activo: Boolean,
    val fechaCreacion: LocalDateTime,
    val idComuna: Int? = null,
    val fechaIncorporacion: LocalDate? = null,
    val avisosActivos: Boolean? = null,
    val diasAnticipacion: Int? = null,
    val idArea: Int? = null,
    val idEmpresa: Int? = null,
    val cargo: String? = null
)

data class SeedEmpresa(
    val id: Int,
    val codigo: String,
    val razonSocial: String,
    val nombreFantasia: String,
    val rut: String,
    val idRubro: Int,
    val correo: String,
    val telefono: String,
    val recargo: Int
)

data class SeedCompromiso(
    val id: Int,
    val folio: String,
    val idUsuario: Int,
    val idEmpresa: Int,
    val concepto: String,
    val cuota: Int?,
    val totalCuotas: Int?,
    val monto: Int,
    val vencimiento: LocalDate,
    val idEstado: Int,
    val recargo: Int,
    val fechaCambioEstado: LocalDateTime,
    val idEjecutivoRegistro: Int?,
    val fechaRegistro: LocalDateTime
)

data class SeedPago(
    val numero: String,
    val folio: String,
    val fechaHora: LocalDateTime,
    val monto: Int,
    val idCanal: Int,
    val idMedioPago: Int
)

data class SeedAnulacion(
    val folio: String,
    val idEjecutivo: Int,
    val idMotivo: Int,
    val comentario: String?,
    val fecha: LocalDateTime
)

data class SeedReporte(
    val numero: String,
    val folio: String,
    val idMotivo: Int,
    val comentario: String?,
    val fechaCreacion: LocalDateTime,
    val idEstado: Int,
    val idAgente: Int?,
    val fechaToma: LocalDateTime?,
    val respuesta: String?,
    val fechaResolucion: LocalDateTime?
)

data class SeedRecomendacion(
    val numero: String,
    val idUsuario: Int,
    val organizacion: String,
    val idRubro: Int,
    val idComuna: Int,
    val fecha: LocalDateTime,
    val idEstado: Int,
    val contactoNombre: String?,
    val contactoCorreo: String?,
    val contactoTelefono: String?,
    val idAgenteUltimoCambio: Int?,
    val fechaUltimoCambio: LocalDateTime?,
    val motivoDescarte: String?
)
