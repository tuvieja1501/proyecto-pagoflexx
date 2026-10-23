package cl.duoc.pagoflex.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalDateTime

@Entity(tableName = "rol", indices = [Index(value = ["codigo"], unique = true)])
data class Rol(
    @PrimaryKey @ColumnInfo(name = "id_rol") val idRol: Int,
    @ColumnInfo(name = "codigo") val codigo: String,
    @ColumnInfo(name = "nombre") val nombre: String,
    @ColumnInfo(name = "descripcion") val descripcion: String?
)

@Entity(tableName = "comuna", indices = [Index(value = ["nombre"], unique = true)])
data class Comuna(
    @PrimaryKey @ColumnInfo(name = "id_comuna") val idComuna: Int,
    @ColumnInfo(name = "nombre") val nombre: String
)

@Entity(tableName = "area", indices = [Index(value = ["nombre"], unique = true)])
data class Area(
    @PrimaryKey @ColumnInfo(name = "id_area") val idArea: Int,
    @ColumnInfo(name = "nombre") val nombre: String
)

@Entity(tableName = "rubro", indices = [Index(value = ["nombre"], unique = true)])
data class Rubro(
    @PrimaryKey @ColumnInfo(name = "id_rubro") val idRubro: Int,
    @ColumnInfo(name = "nombre") val nombre: String
)

@Entity(tableName = "estado_compromiso", indices = [Index(value = ["nombre"], unique = true)])
data class EstadoCompromisoEntidad(
    @PrimaryKey @ColumnInfo(name = "id_estado_compromiso") val idEstadoCompromiso: Int,
    @ColumnInfo(name = "nombre") val nombre: String
)

@Entity(tableName = "motivo_anulacion", indices = [Index(value = ["nombre"], unique = true)])
data class MotivoAnulacion(
    @PrimaryKey @ColumnInfo(name = "id_motivo_anulacion") val idMotivoAnulacion: Int,
    @ColumnInfo(name = "nombre") val nombre: String
)

@Entity(tableName = "canal", indices = [Index(value = ["nombre"], unique = true)])
data class Canal(
    @PrimaryKey @ColumnInfo(name = "id_canal") val idCanal: Int,
    @ColumnInfo(name = "nombre") val nombre: String
)

@Entity(tableName = "medio_pago", indices = [Index(value = ["nombre"], unique = true)])
data class MedioPago(
    @PrimaryKey @ColumnInfo(name = "id_medio_pago") val idMedioPago: Int,
    @ColumnInfo(name = "nombre") val nombre: String
)

@Entity(tableName = "motivo_reporte", indices = [Index(value = ["nombre"], unique = true)])
data class MotivoReporte(
    @PrimaryKey @ColumnInfo(name = "id_motivo_reporte") val idMotivoReporte: Int,
    @ColumnInfo(name = "nombre") val nombre: String
)

@Entity(tableName = "estado_reporte", indices = [Index(value = ["nombre"], unique = true)])
data class EstadoReporteEntidad(
    @PrimaryKey @ColumnInfo(name = "id_estado_reporte") val idEstadoReporte: Int,
    @ColumnInfo(name = "nombre") val nombre: String
)

@Entity(tableName = "estado_recomendacion", indices = [Index(value = ["nombre"], unique = true)])
data class EstadoRecomendacionEntidad(
    @PrimaryKey @ColumnInfo(name = "id_estado_recomendacion") val idEstadoRecomendacion: Int,
    @ColumnInfo(name = "nombre") val nombre: String,
    @ColumnInfo(name = "orden") val orden: Int
)

@Entity(
    tableName = "empresa_cliente",
    foreignKeys = [ForeignKey(entity = Rubro::class, parentColumns = ["id_rubro"], childColumns = ["id_rubro"])],
    indices = [
        Index(value = ["codigo"], unique = true),
        Index(value = ["rut"], unique = true),
        Index(value = ["id_rubro"])
    ]
)
data class EmpresaCliente(
    @PrimaryKey @ColumnInfo(name = "id_empresa") val idEmpresa: Int,
    @ColumnInfo(name = "codigo") val codigo: String,
    @ColumnInfo(name = "razon_social") val razonSocial: String,
    @ColumnInfo(name = "nombre_fantasia") val nombreFantasia: String,
    @ColumnInfo(name = "rut") val rut: String,
    @ColumnInfo(name = "id_rubro") val idRubro: Int,
    @ColumnInfo(name = "correo_atencion") val correoAtencion: String,
    @ColumnInfo(name = "telefono_atencion") val telefonoAtencion: String,

    @ColumnInfo(name = "recargo_atraso") val recargoAtraso: Int,
    @ColumnInfo(name = "activa") val activa: Boolean
)

@Entity(
    tableName = "usuario",
    foreignKeys = [
        ForeignKey(entity = Rol::class, parentColumns = ["id_rol"], childColumns = ["id_rol"]),
        ForeignKey(entity = Comuna::class, parentColumns = ["id_comuna"], childColumns = ["id_comuna"]),
        ForeignKey(entity = Area::class, parentColumns = ["id_area"], childColumns = ["id_area"]),
        ForeignKey(entity = EmpresaCliente::class, parentColumns = ["id_empresa"], childColumns = ["id_empresa"])
    ],
    indices = [
        Index(value = ["rut"], unique = true),
        Index(value = ["correo"], unique = true),
        Index(value = ["id_rol"]),
        Index(value = ["id_comuna"]),
        Index(value = ["id_area"]),
        Index(value = ["id_empresa"])
    ]
)
data class Usuario(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id_usuario") val idUsuario: Int = 0,
    @ColumnInfo(name = "id_rol") val idRol: Int,
    @ColumnInfo(name = "rut") val rut: String,
    @ColumnInfo(name = "nombres") val nombres: String,
    @ColumnInfo(name = "apellidos") val apellidos: String,
    @ColumnInfo(name = "correo") val correo: String,
    @ColumnInfo(name = "telefono") val telefono: String?,
    @ColumnInfo(name = "activo") val activo: Boolean = true,
    @ColumnInfo(name = "fecha_creacion") val fechaCreacion: LocalDateTime,

    @ColumnInfo(name = "id_comuna") val idComuna: Int? = null,
    @ColumnInfo(name = "fecha_incorporacion") val fechaIncorporacion: LocalDate? = null,
    @ColumnInfo(name = "avisos_activos") val avisosActivos: Boolean? = null,
    @ColumnInfo(name = "dias_anticipacion") val diasAnticipacion: Int? = null,

    @ColumnInfo(name = "id_area") val idArea: Int? = null,

    @ColumnInfo(name = "id_empresa") val idEmpresa: Int? = null,
    @ColumnInfo(name = "cargo") val cargo: String? = null
) {
    val nombreCompleto: String get() = "$nombres $apellidos"
}
