package cl.duoc.pagoflex.data

import androidx.room.withTransaction
import cl.duoc.pagoflex.data.db.Compromiso
import cl.duoc.pagoflex.data.db.Pago
import cl.duoc.pagoflex.data.db.PagoFlexDatabase
import cl.duoc.pagoflex.data.db.Reporte
import cl.duoc.pagoflex.domain.AppClock
import cl.duoc.pagoflex.domain.CanalIds
import cl.duoc.pagoflex.domain.EstadoCompromiso
import cl.duoc.pagoflex.domain.EstadoReporte
import cl.duoc.pagoflex.domain.MotivoReporteIds
import cl.duoc.pagoflex.domain.Rules
import cl.duoc.pagoflex.domain.RolIds
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

class RepositorioUsuarioFinal(private val db: PagoFlexDatabase, private val nucleo: Nucleo) {

    companion object {
        const val MAX_COMENTARIO_REPORTE = 500

        val OPCIONES_ANTICIPACION = listOf(1, 3, 5)
    }

    fun observarCompromisos(idUsuario: Int): Flow<List<CompromisoVista>> =
        db.compromisoDao().observarPorUsuario(idUsuario).map { lista -> lista.map(nucleo::vista) }

    suspend fun listarCompromisos(idUsuario: Int): List<CompromisoVista> =
        db.compromisoDao().listarPorUsuario(idUsuario).map(nucleo::vista)

    fun observarCompromiso(idCompromiso: Int, idUsuario: Int): Flow<CompromisoVista?> =
        db.compromisoDao().observarPorId(idCompromiso).map { c ->
            c?.takeIf { it.idUsuarioFinal == idUsuario }?.let(nucleo::vista)
        }

    fun observarComprobante(idCompromiso: Int, idUsuario: Int): Flow<ComprobanteVista?> =
        combine(
            db.pagoDao().observarPorCompromiso(idCompromiso),
            db.compromisoDao().observarPorId(idCompromiso),
            db.usuarioDao().observarPorId(idUsuario)
        ) { pago, compromiso, persona ->
            if (pago != null && compromiso != null && persona != null && compromiso.idUsuarioFinal == idUsuario) {
                nucleo.comprobante(pago, compromiso, persona)
            } else null
        }

    fun observarHistorial(idUsuario: Int): Flow<List<ItemHistorial>> =
        combine(
            db.pagoDao().observarPorUsuario(idUsuario),
            db.anulacionDao().observarPorUsuario(idUsuario),
            db.compromisoDao().observarPorUsuario(idUsuario),
            db.usuarioDao().observarPorId(idUsuario)
        ) { pagos, anulaciones, compromisos, persona ->
            if (persona == null) return@combine emptyList<ItemHistorial>()
            val porId = compromisos.associateBy { it.idCompromiso }
            val dePagos = pagos.mapNotNull { p ->
                porId[p.idCompromiso]?.let { c ->
                    ItemHistorial.DePago(nucleo.comprobante(p, c, persona), c.idEmpresa)
                }
            }
            val deAnulaciones = anulaciones.mapNotNull { a ->
                porId[a.idCompromiso]?.let { c ->
                    ItemHistorial.DeAnulacion(
                        nucleo.vista(c), nucleo.cat.nombreMotivoAnulacion.getValue(a.idMotivoAnulacion), a.fecha
                    )
                }
            }
            (dePagos + deAnulaciones).sortedByDescending { it.fecha }
        }

    fun observarCumplimiento(idUsuario: Int): Flow<Rules.Cumplimiento> =
        combine(
            db.compromisoDao().observarPorUsuario(idUsuario),
            db.pagoDao().observarPorUsuario(idUsuario)
        ) { compromisos, pagos ->
            val fechaPago = pagos.associate { it.idCompromiso to it.fechaHora.toLocalDate() }
            Rules.cumplimiento(
                compromisos.map { Rules.ItemCumplimiento(it.fechaVencimiento, it.idEstadoCompromiso, fechaPago[it.idCompromiso]) },
                AppClock.hoy()
            )
        }

    suspend fun motivoNoPagable(idUsuario: Int, idCompromiso: Int): String? {
        val c = db.compromisoDao().porId(idCompromiso) ?: return "No encontramos este compromiso."
        if (c.idUsuarioFinal != idUsuario) return "Este compromiso no corresponde a tu cuenta."
        return motivoNoPagable(c, db.compromisoDao().listarPorUsuario(idUsuario))
    }

    private fun motivoNoPagable(c: Compromiso, delUsuario: List<Compromiso>): String? {
        when (c.idEstadoCompromiso) {
            EstadoCompromiso.PAGADO ->
                return "Este compromiso ya está pagado, así que no hay nada más que hacer."
            EstadoCompromiso.ANULADO ->
                return "La empresa dejó este compromiso sin efecto, por eso no tienes nada que pagar."
            EstadoCompromiso.EN_REVISION ->
                return "Este compromiso está en revisión por el reporte que hiciste. " +
                    "Cuando se resuelva, podrás pagarlo si corresponde."
        }
        val orden = delUsuario.map { Rules.ItemOrden(it.idCompromiso, it.idEmpresa, it.idEstadoCompromiso, it.fechaVencimiento) }
        val previo = Rules.compromisoPrevioAPagar(
            Rules.ItemOrden(c.idCompromiso, c.idEmpresa, c.idEstadoCompromiso, c.fechaVencimiento), orden
        ) ?: return null
        val anterior = delUsuario.first { it.idCompromiso == previo.idCompromiso }
        val empresa = nucleo.cat.empresas.getValue(anterior.idEmpresa).nombreFantasia
        val cuota = if (anterior.numeroCuota != null && anterior.totalCuotas != null) {
            "la cuota ${anterior.numeroCuota}/${anterior.totalCuotas}"
        } else "«${anterior.concepto}»"
        return "Primero debes pagar $cuota de $empresa, que es la más antigua. Después podrás pagar este compromiso."
    }

    suspend fun pagar(idUsuario: Int, idCompromiso: Int, idMedioPago: Int): Resultado<ComprobanteVista> =
        db.withTransaction<Resultado<ComprobanteVista>> {
            val persona = db.usuarioDao().porId(idUsuario)
            if (persona == null || persona.idRol != RolIds.USUARIO_FINAL) {
                return@withTransaction Resultado.Fallo("Solo una persona con cuenta de pagos puede pagar.")
            }
            val c = db.compromisoDao().porId(idCompromiso)
                ?: return@withTransaction Resultado.Fallo("No encontramos este compromiso.")
            if (c.idUsuarioFinal != idUsuario) {
                return@withTransaction Resultado.Fallo("Este compromiso no corresponde a tu cuenta.")
            }
            motivoNoPagable(c, db.compromisoDao().listarPorUsuario(idUsuario))?.let {
                return@withTransaction Resultado.Fallo(it)
            }
            if (db.pagoDao().porCompromiso(idCompromiso) != null) {
                return@withTransaction Resultado.Fallo("Este compromiso ya tiene un pago registrado.")
            }
            if (idMedioPago !in nucleo.cat.nombreMedio) {
                return@withTransaction Resultado.Fallo("Elige un medio de pago para continuar.")
            }
            val ahora = AppClock.ahora()
            val nuevo = Pago(
                numeroComprobante = siguienteComprobante(), idCompromiso = idCompromiso, fechaHora = ahora,
                montoPagado = Rules.totalAPagar(c.monto, c.recargoAplicado),
                idCanal = CanalIds.APP, idMedioPago = idMedioPago
            )
            val idPago = db.pagoDao().insertar(nuevo).toInt()
            db.compromisoDao().actualizar(c.copy(idEstadoCompromiso = EstadoCompromiso.PAGADO, fechaCambioEstado = ahora))
            Resultado.Exito(nucleo.comprobante(nuevo.copy(idPago = idPago), c, persona))
        }

    private suspend fun siguienteComprobante(): String {
        val ultimo = db.pagoDao().ultimoNumeroComprobante()
        val n = ultimo?.substringAfter("CPR-")?.toIntOrNull() ?: 0
        return "CPR-%06d".format(n + 1)
    }

    fun observarReportes(idUsuario: Int): Flow<List<ReporteVista>> =
        combine(
            db.reporteDao().observarPorUsuario(idUsuario),
            db.compromisoDao().observarPorUsuario(idUsuario),
            db.usuarioDao().observarTodos()
        ) { reportes, compromisos, usuarios ->
            val porId = compromisos.associateBy { it.idCompromiso }
            val usuarioPorId = usuarios.associateBy { it.idUsuario }
            val persona = usuarioPorId[idUsuario]
            if (persona == null) emptyList()
            else reportes.mapNotNull { r ->
                porId[r.idCompromiso]?.let { c -> nucleo.reporte(r, c, persona, r.idAgente?.let(usuarioPorId::get)) }
            }
        }

    fun observarReporteDeCompromiso(idCompromiso: Int, idUsuario: Int): Flow<ReporteVista?> =
        observarReportes(idUsuario).map { lista -> lista.firstOrNull { it.idCompromiso == idCompromiso } }

    suspend fun reportarProblema(
        idUsuario: Int, idCompromiso: Int, idMotivo: Int, comentario: String
    ): Resultado<ReporteVista> = db.withTransaction<Resultado<ReporteVista>> {
        val texto = comentario.trim()
        if (idMotivo !in nucleo.cat.nombreMotivoReporte) {
            return@withTransaction Resultado.Fallo("Elige el motivo de tu reporte.")
        }
        if (idMotivo == MotivoReporteIds.OTRO && texto.isEmpty()) {
            return@withTransaction Resultado.Fallo("Cuéntanos qué pasó para poder ayudarte.")
        }
        if (texto.length > MAX_COMENTARIO_REPORTE) {
            return@withTransaction Resultado.Fallo("El comentario puede tener hasta $MAX_COMENTARIO_REPORTE letras.")
        }
        val persona = db.usuarioDao().porId(idUsuario)
        val c = db.compromisoDao().porId(idCompromiso)
        if (persona == null || c == null || c.idUsuarioFinal != idUsuario) {
            return@withTransaction Resultado.Fallo("Este compromiso no corresponde a tu cuenta.")
        }
        db.reporteDao().reporteVigenteDe(idCompromiso)?.let {
            return@withTransaction Resultado.Fallo("Este compromiso ya tiene un reporte en curso (${it.numero}).")
        }
        if (!Rules.puedeReportar(c.idEstadoCompromiso)) {
            return@withTransaction Resultado.Fallo("Solo se pueden reportar compromisos pendientes o vencidos.")
        }
        val ahora = AppClock.ahora()
        val nuevo = Reporte(
            numero = siguienteNumeroReporte(), idCompromiso = idCompromiso, idMotivoReporte = idMotivo,
            comentario = texto.ifEmpty { null }, fechaCreacion = ahora, idEstadoReporte = EstadoReporte.ABIERTO
        )
        val id = db.reporteDao().insertar(nuevo).toInt()
        val enRevision = c.copy(idEstadoCompromiso = EstadoCompromiso.EN_REVISION, fechaCambioEstado = ahora)
        db.compromisoDao().actualizar(enRevision)
        Resultado.Exito(nucleo.reporte(nuevo.copy(idReporte = id), enRevision, persona, null))
    }

    private suspend fun siguienteNumeroReporte(): String {
        val ultimo = db.reporteDao().ultimoNumero()
        val n = ultimo?.substringAfter("REP-")?.toIntOrNull() ?: 0
        return "REP-%04d".format(n + 1)
    }

    suspend fun guardarAvisos(idUsuario: Int, activos: Boolean, dias: Int): Resultado<Unit> {
        if (activos && dias !in OPCIONES_ANTICIPACION) {
            return Resultado.Fallo("Elige 1, 3 o 5 días de anticipación.")
        }
        val persona = db.usuarioDao().porId(idUsuario)
        if (persona == null || persona.idRol != RolIds.USUARIO_FINAL) {
            return Resultado.Fallo("Solo las personas con cuenta de pagos pueden configurar avisos.")
        }
        db.usuarioDao().actualizarAvisos(idUsuario, activos, if (activos) dias else persona.diasAnticipacion)
        return Resultado.Exito(Unit)
    }
}
