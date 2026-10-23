package cl.duoc.pagoflex.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalDateTime

@Entity(
    tableName = "compromiso",
    foreignKeys = [
        ForeignKey(entity = Usuario::class, parentColumns = ["id_usuario"], childColumns = ["id_usuario_final"]),
        ForeignKey(entity = EmpresaCliente::class, parentColumns = ["id_empresa"], childColumns = ["id_empresa"]),
        ForeignKey(
            entity = EstadoCompromisoEntidad::class,
            parentColumns = ["id_estado_compromiso"],
            childColumns = ["id_estado_compromiso"]
        ),
        ForeignKey(entity = Usuario::class, parentColumns = ["id_usuario"], childColumns = ["id_ejecutivo_registro"])
    ],
    indices = [
        Index(value = ["folio"], unique = true),
        Index(value = ["id_usuario_final", "id_empresa", "concepto", "numero_cuota"], unique = true),
        Index(value = ["id_usuario_final"]),
        Index(value = ["id_empresa"]),
        Index(value = ["id_estado_compromiso"]),
        Index(value = ["id_ejecutivo_registro"])
    ]
)
data class Compromiso(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id_compromiso") val idCompromiso: Int = 0,
    @ColumnInfo(name = "folio") val folio: String,
    @ColumnInfo(name = "id_usuario_final") val idUsuarioFinal: Int,
    @ColumnInfo(name = "id_empresa") val idEmpresa: Int,
    @ColumnInfo(name = "concepto") val concepto: String,
    @ColumnInfo(name = "numero_cuota") val numeroCuota: Int? = null,
    @ColumnInfo(name = "total_cuotas") val totalCuotas: Int? = null,

    @ColumnInfo(name = "monto") val monto: Int,
    @ColumnInfo(name = "fecha_vencimiento") val fechaVencimiento: LocalDate,
    @ColumnInfo(name = "id_estado_compromiso") val idEstadoCompromiso: Int,

    @ColumnInfo(name = "recargo_aplicado") val recargoAplicado: Int = 0,
    @ColumnInfo(name = "fecha_cambio_estado") val fechaCambioEstado: LocalDateTime,

    @ColumnInfo(name = "id_ejecutivo_registro") val idEjecutivoRegistro: Int? = null,
    @ColumnInfo(name = "fecha_registro") val fechaRegistro: LocalDateTime
)

@Entity(
    tableName = "anulacion",
    foreignKeys = [
        ForeignKey(entity = Compromiso::class, parentColumns = ["id_compromiso"], childColumns = ["id_compromiso"]),
        ForeignKey(entity = Usuario::class, parentColumns = ["id_usuario"], childColumns = ["id_ejecutivo"]),
        ForeignKey(
            entity = MotivoAnulacion::class,
            parentColumns = ["id_motivo_anulacion"],
            childColumns = ["id_motivo_anulacion"]
        )
    ],
    indices = [Index(value = ["id_ejecutivo"]), Index(value = ["id_motivo_anulacion"])]
)
data class Anulacion(
    @PrimaryKey @ColumnInfo(name = "id_compromiso") val idCompromiso: Int,
    @ColumnInfo(name = "id_ejecutivo") val idEjecutivo: Int,
    @ColumnInfo(name = "id_motivo_anulacion") val idMotivoAnulacion: Int,

    @ColumnInfo(name = "comentario") val comentario: String?,
    @ColumnInfo(name = "fecha") val fecha: LocalDateTime
)

@Entity(
    tableName = "pago",
    foreignKeys = [
        ForeignKey(entity = Compromiso::class, parentColumns = ["id_compromiso"], childColumns = ["id_compromiso"]),
        ForeignKey(entity = Canal::class, parentColumns = ["id_canal"], childColumns = ["id_canal"]),
        ForeignKey(entity = MedioPago::class, parentColumns = ["id_medio_pago"], childColumns = ["id_medio_pago"])
    ],
    indices = [
        Index(value = ["numero_comprobante"], unique = true),
        Index(value = ["id_compromiso"], unique = true),
        Index(value = ["id_canal"]),
        Index(value = ["id_medio_pago"])
    ]
)
data class Pago(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id_pago") val idPago: Int = 0,
    @ColumnInfo(name = "numero_comprobante") val numeroComprobante: String,
    @ColumnInfo(name = "id_compromiso") val idCompromiso: Int,
    @ColumnInfo(name = "fecha_hora") val fechaHora: LocalDateTime,

    @ColumnInfo(name = "monto_pagado") val montoPagado: Int,
    @ColumnInfo(name = "id_canal") val idCanal: Int,
    @ColumnInfo(name = "id_medio_pago") val idMedioPago: Int
)

@Entity(
    tableName = "reporte",
    foreignKeys = [
        ForeignKey(entity = Compromiso::class, parentColumns = ["id_compromiso"], childColumns = ["id_compromiso"]),
        ForeignKey(
            entity = MotivoReporte::class,
            parentColumns = ["id_motivo_reporte"],
            childColumns = ["id_motivo_reporte"]
        ),
        ForeignKey(
            entity = EstadoReporteEntidad::class,
            parentColumns = ["id_estado_reporte"],
            childColumns = ["id_estado_reporte"]
        ),
        ForeignKey(entity = Usuario::class, parentColumns = ["id_usuario"], childColumns = ["id_agente"])
    ],
    indices = [
        Index(value = ["numero"], unique = true),
        Index(value = ["id_compromiso"]),
        Index(value = ["id_motivo_reporte"]),
        Index(value = ["id_estado_reporte"]),
        Index(value = ["id_agente"])
    ]
)
data class Reporte(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id_reporte") val idReporte: Int = 0,
    @ColumnInfo(name = "numero") val numero: String,
    @ColumnInfo(name = "id_compromiso") val idCompromiso: Int,
    @ColumnInfo(name = "id_motivo_reporte") val idMotivoReporte: Int,
    @ColumnInfo(name = "comentario") val comentario: String?,
    @ColumnInfo(name = "fecha_creacion") val fechaCreacion: LocalDateTime,
    @ColumnInfo(name = "id_estado_reporte") val idEstadoReporte: Int,
    @ColumnInfo(name = "id_agente") val idAgente: Int? = null,
    @ColumnInfo(name = "fecha_toma") val fechaToma: LocalDateTime? = null,
    @ColumnInfo(name = "respuesta") val respuesta: String? = null,
    @ColumnInfo(name = "fecha_resolucion") val fechaResolucion: LocalDateTime? = null
)

@Entity(
    tableName = "recomendacion",
    foreignKeys = [
        ForeignKey(entity = Usuario::class, parentColumns = ["id_usuario"], childColumns = ["id_usuario_final"]),
        ForeignKey(entity = Rubro::class, parentColumns = ["id_rubro"], childColumns = ["id_rubro"]),
        ForeignKey(entity = Comuna::class, parentColumns = ["id_comuna"], childColumns = ["id_comuna"]),
        ForeignKey(
            entity = EstadoRecomendacionEntidad::class,
            parentColumns = ["id_estado_recomendacion"],
            childColumns = ["id_estado_recomendacion"]
        )
    ],
    indices = [
        Index(value = ["numero"], unique = true),
        Index(value = ["id_usuario_final", "nombre_organizacion"], unique = true),
        Index(value = ["id_rubro"]),
        Index(value = ["id_comuna"]),
        Index(value = ["id_estado_recomendacion"])
    ]
)
data class Recomendacion(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id_recomendacion") val idRecomendacion: Int = 0,
    @ColumnInfo(name = "numero") val numero: String,
    @ColumnInfo(name = "id_usuario_final") val idUsuarioFinal: Int,
    @ColumnInfo(name = "nombre_organizacion") val nombreOrganizacion: String,
    @ColumnInfo(name = "rut_organizacion") val rutOrganizacion: String?,
    @ColumnInfo(name = "id_rubro") val idRubro: Int,
    @ColumnInfo(name = "id_comuna") val idComuna: Int,
    @ColumnInfo(name = "fecha") val fecha: LocalDateTime,
    @ColumnInfo(name = "id_estado_recomendacion") val idEstadoRecomendacion: Int
)

@Entity(
    tableName = "contacto_recomendacion",
    foreignKeys = [
        ForeignKey(
            entity = Recomendacion::class,
            parentColumns = ["id_recomendacion"],
            childColumns = ["id_recomendacion"]
        )
    ]
)
data class ContactoRecomendacion(
    @PrimaryKey @ColumnInfo(name = "id_recomendacion") val idRecomendacion: Int,
    @ColumnInfo(name = "nombre") val nombre: String,
    @ColumnInfo(name = "correo") val correo: String?,
    @ColumnInfo(name = "telefono") val telefono: String?,
    @ColumnInfo(name = "autoriza_contacto") val autorizaContacto: Boolean
)

@Entity(
    tableName = "historial_recomendacion",
    foreignKeys = [
        ForeignKey(
            entity = Recomendacion::class,
            parentColumns = ["id_recomendacion"],
            childColumns = ["id_recomendacion"]
        ),
        ForeignKey(
            entity = EstadoRecomendacionEntidad::class,
            parentColumns = ["id_estado_recomendacion"],
            childColumns = ["id_estado_recomendacion"]
        ),
        ForeignKey(entity = Usuario::class, parentColumns = ["id_usuario"], childColumns = ["id_agente"])
    ],
    indices = [
        Index(value = ["id_recomendacion"]),
        Index(value = ["id_estado_recomendacion"]),
        Index(value = ["id_agente"])
    ]
)
data class HistorialRecomendacion(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id_historial") val idHistorial: Int = 0,
    @ColumnInfo(name = "id_recomendacion") val idRecomendacion: Int,
    @ColumnInfo(name = "id_estado_recomendacion") val idEstadoRecomendacion: Int,
    @ColumnInfo(name = "id_agente") val idAgente: Int,
    @ColumnInfo(name = "fecha") val fecha: LocalDateTime,
    @ColumnInfo(name = "motivo") val motivo: String?
)

@Entity(tableName = "reconocimiento", indices = [Index(value = ["nombre"], unique = true)])
data class Reconocimiento(
    @PrimaryKey @ColumnInfo(name = "id_reconocimiento") val idReconocimiento: Int,
    @ColumnInfo(name = "nombre") val nombre: String,
    @ColumnInfo(name = "descripcion") val descripcion: String
)

@Entity(
    tableName = "usuario_reconocimiento",
    primaryKeys = ["id_usuario_final", "id_reconocimiento"],
    foreignKeys = [
        ForeignKey(entity = Usuario::class, parentColumns = ["id_usuario"], childColumns = ["id_usuario_final"]),
        ForeignKey(
            entity = Reconocimiento::class,
            parentColumns = ["id_reconocimiento"],
            childColumns = ["id_reconocimiento"]
        )
    ],
    indices = [Index(value = ["id_reconocimiento"])]
)
data class UsuarioReconocimiento(
    @ColumnInfo(name = "id_usuario_final") val idUsuarioFinal: Int,
    @ColumnInfo(name = "id_reconocimiento") val idReconocimiento: Int,
    @ColumnInfo(name = "fecha_obtencion") val fechaObtencion: LocalDateTime,
    @ColumnInfo(name = "motivo") val motivo: String
)
