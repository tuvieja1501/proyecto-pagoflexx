package cl.duoc.pagoflex.data

import cl.duoc.pagoflex.data.db.Compromiso
import cl.duoc.pagoflex.data.db.EmpresaCliente
import cl.duoc.pagoflex.data.db.MedioPago
import cl.duoc.pagoflex.data.db.MotivoAnulacion
import cl.duoc.pagoflex.data.db.MotivoReporte
import cl.duoc.pagoflex.data.db.Pago
import cl.duoc.pagoflex.data.db.PagoFlexDatabase
import cl.duoc.pagoflex.data.db.Reporte
import cl.duoc.pagoflex.data.db.Usuario
import cl.duoc.pagoflex.domain.AppClock
import cl.duoc.pagoflex.domain.Rules
import cl.duoc.pagoflex.domain.Rut
import kotlinx.coroutines.flow.first
import java.time.temporal.ChronoUnit

class CatalogosCargados(
    val estadosCompromiso: Map<Int, String>,
    val estadosReporte: Map<Int, String>,
    val canales: Map<Int, String>,
    val medios: List<MedioPago>,
    val motivosReporte: List<MotivoReporte>,
    val motivosAnulacion: List<MotivoAnulacion>,
    val empresas: Map<Int, EmpresaCliente>
) {
    val nombreMedio: Map<Int, String> = medios.associate { it.idMedioPago to it.nombre }
    val nombreMotivoReporte: Map<Int, String> = motivosReporte.associate { it.idMotivoReporte to it.nombre }
    val nombreMotivoAnulacion: Map<Int, String> = motivosAnulacion.associate { it.idMotivoAnulacion to it.nombre }
}

class Nucleo(val db: PagoFlexDatabase) {

    lateinit var cat: CatalogosCargados
        private set

    suspend fun cargarCatalogos() {
        val dao = db.catalogoDao()
        cat = CatalogosCargados(
            estadosCompromiso = dao.observarEstadosCompromiso().first().associate { it.idEstadoCompromiso to it.nombre },
            estadosReporte = dao.observarEstadosReporte().first().associate { it.idEstadoReporte to it.nombre },
            canales = dao.observarCanales().first().associate { it.idCanal to it.nombre },
            medios = dao.observarMediosPago().first(),
            motivosReporte = dao.observarMotivosReporte().first(),
            motivosAnulacion = dao.observarMotivosAnulacion().first(),
            empresas = db.empresaDao().observarTodas().first().associateBy { it.idEmpresa }
        )
    }

    fun vista(c: Compromiso): CompromisoVista {
        val empresa = cat.empresas.getValue(c.idEmpresa)
        return CompromisoVista(
            id = c.idCompromiso, folio = c.folio, idUsuario = c.idUsuarioFinal, idEmpresa = c.idEmpresa,
            empresa = empresa.nombreFantasia, concepto = c.concepto, cuota = c.numeroCuota,
            totalCuotas = c.totalCuotas, monto = c.monto, vencimiento = c.fechaVencimiento,
            idEstado = c.idEstadoCompromiso, estado = cat.estadosCompromiso.getValue(c.idEstadoCompromiso),
            recargo = c.recargoAplicado, fechaCambioEstado = c.fechaCambioEstado
        )
    }

    fun comprobante(p: Pago, c: Compromiso, persona: Usuario): ComprobanteVista {
        val empresa = cat.empresas.getValue(c.idEmpresa)
        val fechaPago = p.fechaHora.toLocalDate()
        val aTiempo = !fechaPago.isAfter(c.fechaVencimiento)
        return ComprobanteVista(
            idPago = p.idPago, numero = p.numeroComprobante, idCompromiso = c.idCompromiso, folio = c.folio,
            fechaHora = p.fechaHora, pagadoPor = persona.nombreCompleto, rutEnmascarado = Rut.enmascarar(persona.rut),
            empresa = empresa.nombreFantasia, razonSocial = empresa.razonSocial, concepto = c.concepto,
            cuotaTexto = if (c.numeroCuota != null && c.totalCuotas != null) "${c.numeroCuota}/${c.totalCuotas}" else null,
            vencimiento = c.fechaVencimiento, montoCuota = c.monto,
            recargo = (p.montoPagado - c.monto).coerceAtLeast(0), totalPagado = p.montoPagado,
            medioPago = cat.nombreMedio.getValue(p.idMedioPago), canal = cat.canales.getValue(p.idCanal),
            aTiempo = aTiempo,
            diasDeAtraso = if (aTiempo) 0 else ChronoUnit.DAYS.between(c.fechaVencimiento, fechaPago).toInt()
        )
    }

    fun reporte(r: Reporte, c: Compromiso, persona: Usuario, agente: Usuario?): ReporteVista {
        val empresa = cat.empresas.getValue(c.idEmpresa)
        return ReporteVista(
            id = r.idReporte, numero = r.numero, idCompromiso = c.idCompromiso, folio = c.folio,
            empresa = empresa.nombreFantasia, concepto = c.concepto,
            cuotaTexto = if (c.numeroCuota != null && c.totalCuotas != null) "${c.numeroCuota}/${c.totalCuotas}" else null,
            monto = c.monto, idEstadoCompromiso = c.idEstadoCompromiso,
            estadoCompromiso = cat.estadosCompromiso.getValue(c.idEstadoCompromiso),
            idUsuarioFinal = persona.idUsuario, nombrePersona = persona.nombreCompleto,
            rutPersona = Rut.enmascarar(persona.rut), motivo = cat.nombreMotivoReporte.getValue(r.idMotivoReporte),
            comentario = r.comentario, fechaCreacion = r.fechaCreacion, idEstado = r.idEstadoReporte,
            estado = cat.estadosReporte.getValue(r.idEstadoReporte), idAgente = r.idAgente,
            nombreAgente = agente?.nombreCompleto, fechaToma = r.fechaToma, respuesta = r.respuesta,
            fechaResolucion = r.fechaResolucion,
            atrasado = Rules.reporteAtrasado(r.idEstadoReporte, r.fechaCreacion.toLocalDate(), AppClock.hoy())
        )
    }
}
