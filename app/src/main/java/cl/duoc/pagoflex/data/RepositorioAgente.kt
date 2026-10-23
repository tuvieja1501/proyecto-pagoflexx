package cl.duoc.pagoflex.data

import androidx.room.withTransaction
import cl.duoc.pagoflex.data.db.PagoFlexDatabase
import cl.duoc.pagoflex.domain.AppClock
import cl.duoc.pagoflex.domain.EstadoCompromiso
import cl.duoc.pagoflex.domain.EstadoReporte
import cl.duoc.pagoflex.domain.RolIds
import cl.duoc.pagoflex.domain.Rules
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

class RepositorioAgente(private val db: PagoFlexDatabase, private val nucleo: Nucleo) {

    companion object {
        const val MAX_RESPUESTA = 500
    }

    fun observarReportes(): Flow<List<ReporteVista>> =
        combine(
            db.reporteDao().observarTodos(),
            db.compromisoDao().observarTodos(),
            db.usuarioDao().observarTodos()
        ) { reportes, compromisos, usuarios ->
            val compromisoPorId = compromisos.associateBy { it.idCompromiso }
            val usuarioPorId = usuarios.associateBy { it.idUsuario }
            reportes.mapNotNull { r ->
                val c = compromisoPorId[r.idCompromiso] ?: return@mapNotNull null
                val persona = usuarioPorId[c.idUsuarioFinal] ?: return@mapNotNull null
                nucleo.reporte(r, c, persona, r.idAgente?.let(usuarioPorId::get))
            }
        }

    fun observarReporte(idReporte: Int): Flow<ReporteVista?> =
        observarReportes().map { lista -> lista.firstOrNull { it.id == idReporte } }

    suspend fun tomar(idAgente: Int, idReporte: Int): Resultado<Unit> =
        db.withTransaction<Resultado<Unit>> {
            val agente = db.usuarioDao().porId(idAgente)
            if (agente == null || agente.idRol != RolIds.AGENTE || !agente.activo) {
                return@withTransaction Resultado.Fallo("Solo un agente PagoFlex activo puede gestionar reportes.")
            }
            val r = db.reporteDao().porId(idReporte)
                ?: return@withTransaction Resultado.Fallo("No encontramos el reporte.")
            if (r.idEstadoReporte != EstadoReporte.ABIERTO) {
                return@withTransaction Resultado.Fallo(
                    "Este reporte ya está ${nucleo.cat.estadosReporte.getValue(r.idEstadoReporte).lowercase()}."
                )
            }
            db.reporteDao().actualizar(
                r.copy(
                    idEstadoReporte = EstadoReporte.EN_GESTION, idAgente = idAgente,
                    fechaToma = AppClock.ahora()
                )
            )
            Resultado.Exito(Unit)
        }

    suspend fun resolver(idAgente: Int, idReporte: Int, respuesta: String): Resultado<String> =
        db.withTransaction<Resultado<String>> {
            val agente = db.usuarioDao().porId(idAgente)
            if (agente == null || agente.idRol != RolIds.AGENTE || !agente.activo) {
                return@withTransaction Resultado.Fallo("Solo un agente PagoFlex activo puede gestionar reportes.")
            }
            val r = db.reporteDao().porId(idReporte)
                ?: return@withTransaction Resultado.Fallo("No encontramos el reporte.")
            if (r.idEstadoReporte != EstadoReporte.EN_GESTION) {
                return@withTransaction Resultado.Fallo("Primero toma el reporte para poder resolverlo.")
            }
            if (r.idAgente != idAgente) {
                val responsable = r.idAgente?.let { db.usuarioDao().porId(it)?.nombreCompleto } ?: "otro agente"
                return@withTransaction Resultado.Fallo("Este reporte lo está gestionando $responsable.")
            }
            val texto = respuesta.trim()
            if (texto.isEmpty()) {
                return@withTransaction Resultado.Fallo("Escribe la respuesta para la persona antes de resolver el reporte.")
            }
            if (texto.length > MAX_RESPUESTA) {
                return@withTransaction Resultado.Fallo("La respuesta puede tener hasta $MAX_RESPUESTA letras.")
            }
            val c = db.compromisoDao().porId(r.idCompromiso)
                ?: return@withTransaction Resultado.Fallo("No encontramos el compromiso del reporte.")
            val ahora = AppClock.ahora()
            val nuevoEstado = Rules.estadoTrasResolver(c.idEstadoCompromiso, c.fechaVencimiento, AppClock.hoy())
            val recargo = if (nuevoEstado == EstadoCompromiso.VENCIDO && c.recargoAplicado == 0) {
                nucleo.cat.empresas.getValue(c.idEmpresa).recargoAtraso
            } else c.recargoAplicado
            if (nuevoEstado != c.idEstadoCompromiso || recargo != c.recargoAplicado) {
                db.compromisoDao().actualizar(
                    c.copy(idEstadoCompromiso = nuevoEstado, recargoAplicado = recargo, fechaCambioEstado = ahora)
                )
            }
            db.reporteDao().actualizar(
                r.copy(idEstadoReporte = EstadoReporte.RESUELTO, respuesta = texto, fechaResolucion = ahora)
            )
            Resultado.Exito(nucleo.cat.estadosCompromiso.getValue(nuevoEstado))
        }
}
