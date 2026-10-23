package cl.duoc.pagoflex.data

import androidx.room.withTransaction
import cl.duoc.pagoflex.data.db.Anulacion
import cl.duoc.pagoflex.data.db.Compromiso
import cl.duoc.pagoflex.data.db.EmpresaCliente
import cl.duoc.pagoflex.data.db.PagoFlexDatabase
import cl.duoc.pagoflex.data.db.Usuario
import cl.duoc.pagoflex.domain.AppClock
import cl.duoc.pagoflex.domain.EstadoCompromiso
import cl.duoc.pagoflex.domain.MotivoAnulacionIds
import cl.duoc.pagoflex.domain.RolIds
import cl.duoc.pagoflex.domain.Rules
import cl.duoc.pagoflex.domain.Rut
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.YearMonth

class RepositorioEjecutivo(private val db: PagoFlexDatabase, private val nucleo: Nucleo) {

    companion object {
        const val MAX_CONCEPTO = 120
        const val MAX_COMENTARIO_ANULACION = 255
        private val CORREO = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
        private val TELEFONO = Regex("^\\+?[0-9 ]{8,16}$")
        fun correoValido(texto: String) = CORREO.matches(texto.trim())
        fun telefonoValido(texto: String) = TELEFONO.matches(texto.trim())
    }

    suspend fun empresaDe(idEjecutivo: Int): EmpresaCliente? {
        val u = db.usuarioDao().porId(idEjecutivo) ?: return null
        if (u.idRol != RolIds.EJECUTIVO) return null
        return u.idEmpresa?.let { nucleo.cat.empresas[it] }
    }

    fun observarFilas(idEmpresa: Int): Flow<List<FilaEjecutivo>> =
        combine(
            db.compromisoDao().observarPorEmpresa(idEmpresa),
            db.usuarioDao().observarTodos()
        ) { compromisos, usuarios ->
            val porId = usuarios.associateBy { it.idUsuario }
            compromisos.mapNotNull { c ->
                porId[c.idUsuarioFinal]?.let { FilaEjecutivo(nucleo.vista(c), it.nombreCompleto, Rut.enmascarar(it.rut)) }
            }
        }

    fun observarDetalle(idCompromiso: Int, idEmpresa: Int): Flow<DetalleEjecutivo?> =
        combine(
            db.compromisoDao().observarPorId(idCompromiso),
            db.reporteDao().observarPorCompromiso(idCompromiso),
            db.pagoDao().observarPorCompromiso(idCompromiso),
            db.anulacionDao().observarPorCompromiso(idCompromiso),
            db.usuarioDao().observarTodos()
        ) { c, reportes, pago, anulacion, usuarios ->
            if (c == null || c.idEmpresa != idEmpresa) return@combine null
            val porId = usuarios.associateBy { it.idUsuario }
            val persona = porId[c.idUsuarioFinal] ?: return@combine null
            DetalleEjecutivo(
                compromiso = nucleo.vista(c),
                nombrePersona = persona.nombreCompleto,
                rutPersona = Rut.enmascarar(persona.rut),
                correoPersona = persona.correo,
                telefonoPersona = persona.telefono,
                reporte = reportes.firstOrNull()?.let { r -> nucleo.reporte(r, c, persona, r.idAgente?.let(porId::get)) },
                comprobante = pago?.let { nucleo.comprobante(it, c, persona) },
                anulacion = anulacion?.let {
                    AnulacionVista(
                        motivo = nucleo.cat.nombreMotivoAnulacion.getValue(it.idMotivoAnulacion),
                        comentario = it.comentario,
                        ejecutivo = porId[it.idEjecutivo]?.nombreCompleto ?: "Ejecutivo",
                        fecha = it.fecha
                    )
                },
                registradoPor = c.idEjecutivoRegistro?.let { porId[it]?.nombreCompleto } ?: "Equipo PagoFlex"
            )
        }

    fun observarCobranza(idEmpresa: Int, mes: YearMonth): Flow<Rules.Cobranza> =
        combine(
            db.compromisoDao().observarPorEmpresa(idEmpresa),
            db.pagoDao().observarPorEmpresa(idEmpresa)
        ) { compromisos, pagos ->
            val pagado = pagos.associate { it.idCompromiso to it.montoPagado }
            Rules.cobranza(
                compromisos
                    .filter { YearMonth.from(it.fechaVencimiento) == mes }
                    .map { Rules.ItemCobranza(it.idEstadoCompromiso, pagado[it.idCompromiso]) }
            )
        }

    suspend fun buscarPersona(rut: String): Usuario? {
        if (!Rut.esValido(rut)) return null
        return db.usuarioDao().porRut(Rut.formatear(rut))?.takeIf { it.idRol == RolIds.USUARIO_FINAL }
    }

    suspend fun registrarCompromiso(idEjecutivo: Int, datos: DatosCompromiso): Resultado<CompromisoVista> =
        db.withTransaction<Resultado<CompromisoVista>> {
            val ejecutivo = db.usuarioDao().porId(idEjecutivo)
            val idEmpresa = ejecutivo?.idEmpresa
            if (ejecutivo == null || ejecutivo.idRol != RolIds.EJECUTIVO || !ejecutivo.activo || idEmpresa == null) {
                return@withTransaction Resultado.Fallo("Solo un ejecutivo activo de una empresa cliente puede registrar compromisos.")
            }
            val hoy = AppClock.hoy()
            val ahora = AppClock.ahora()
            val concepto = datos.concepto.trim()

            if (!Rut.esValido(datos.rut)) return@withTransaction Resultado.Fallo("El RUT no es válido.")
            if (concepto.isEmpty() || concepto.length > MAX_CONCEPTO) {
                return@withTransaction Resultado.Fallo("Escribe el concepto (hasta $MAX_CONCEPTO letras).")
            }
            if (datos.monto <= 0) return@withTransaction Resultado.Fallo("El monto debe ser mayor que cero.")
            if (datos.vencimiento.isBefore(hoy)) {
                return@withTransaction Resultado.Fallo("La fecha de vencimiento no puede ser anterior a hoy.")
            }
            if ((datos.cuota == null) != (datos.totalCuotas == null)) {
                return@withTransaction Resultado.Fallo("Indica el número de cuota y el total de cuotas, o deja ambos vacíos.")
            }
            if (datos.cuota != null && datos.totalCuotas != null &&
                (datos.cuota < 1 || datos.totalCuotas < 1 || datos.cuota > datos.totalCuotas)
            ) {
                return@withTransaction Resultado.Fallo("La cuota no puede ser mayor que el total de cuotas.")
            }

            val rutFormateado = Rut.formatear(datos.rut)
            var persona = db.usuarioDao().porRut(rutFormateado)
            if (persona != null && (persona.idRol != RolIds.USUARIO_FINAL || !persona.activo)) {
                return@withTransaction Resultado.Fallo("Ese RUT no corresponde a un usuario final activo.")
            }
            if (persona == null) {

                if (datos.nombres.isBlank() || datos.apellidos.isBlank()) {
                    return@withTransaction Resultado.Fallo("Escribe el nombre y los apellidos de la persona.")
                }
                if (!correoValido(datos.correo)) return@withTransaction Resultado.Fallo("El correo no es válido.")
                if (!telefonoValido(datos.telefono)) return@withTransaction Resultado.Fallo("El teléfono no es válido.")
                val nueva = Usuario(
                    idRol = RolIds.USUARIO_FINAL, rut = rutFormateado, nombres = datos.nombres.trim(),
                    apellidos = datos.apellidos.trim(), correo = datos.correo.trim(), telefono = datos.telefono.trim(),
                    fechaCreacion = ahora, fechaIncorporacion = hoy, avisosActivos = true, diasAnticipacion = 3
                )
                val id = try {
                    db.usuarioDao().insertar(nueva).toInt()
                } catch (e: android.database.sqlite.SQLiteConstraintException) {
                    return@withTransaction Resultado.Fallo("Ya existe una cuenta con ese correo.")
                }
                persona = nueva.copy(idUsuario = id)
            }

            if (db.compromisoDao().contarDuplicados(persona.idUsuario, idEmpresa, concepto, datos.cuota) > 0) {
                return@withTransaction Resultado.Fallo("Este compromiso ya está registrado para esa persona.")
            }
            val nuevo = Compromiso(
                folio = siguienteFolio(hoy.year), idUsuarioFinal = persona.idUsuario, idEmpresa = idEmpresa,
                concepto = concepto, numeroCuota = datos.cuota, totalCuotas = datos.totalCuotas, monto = datos.monto,
                fechaVencimiento = datos.vencimiento, idEstadoCompromiso = EstadoCompromiso.PENDIENTE,
                recargoAplicado = 0, fechaCambioEstado = ahora, idEjecutivoRegistro = idEjecutivo, fechaRegistro = ahora
            )
            val id = db.compromisoDao().insertar(nuevo).toInt()
            Resultado.Exito(nucleo.vista(nuevo.copy(idCompromiso = id)))
        }

    private suspend fun siguienteFolio(anio: Int): String {
        val prefijo = "CP-$anio-"
        val n = db.compromisoDao().ultimoFolio(prefijo)?.substringAfterLast('-')?.toIntOrNull() ?: 0
        return prefijo + "%04d".format(n + 1)
    }

    suspend fun anularCompromiso(
        idEjecutivo: Int, idCompromiso: Int, idMotivo: Int, comentario: String
    ): Resultado<Unit> = db.withTransaction<Resultado<Unit>> {
        val ejecutivo = db.usuarioDao().porId(idEjecutivo)
        if (ejecutivo == null || ejecutivo.idRol != RolIds.EJECUTIVO || !ejecutivo.activo || ejecutivo.idEmpresa == null) {
            return@withTransaction Resultado.Fallo("Solo un ejecutivo activo de una empresa cliente puede anular compromisos.")
        }
        val c = db.compromisoDao().porId(idCompromiso)
        if (c == null || c.idEmpresa != ejecutivo.idEmpresa) {
            return@withTransaction Resultado.Fallo("Solo puedes anular compromisos de tu empresa.")
        }
        if (!Rules.puedeAnular(c.idEstadoCompromiso)) {
            val motivo = if (c.idEstadoCompromiso == EstadoCompromiso.PAGADO) "Un compromiso pagado no se puede anular."
            else "Este compromiso ya está anulado."
            return@withTransaction Resultado.Fallo(motivo)
        }
        if (idMotivo !in nucleo.cat.nombreMotivoAnulacion) {
            return@withTransaction Resultado.Fallo("Elige el motivo de la anulación.")
        }
        val texto = comentario.trim()
        if (idMotivo == MotivoAnulacionIds.OTRO && texto.isEmpty()) {
            return@withTransaction Resultado.Fallo("Cuando el motivo es «Otro», escribe un comentario.")
        }
        if (texto.length > MAX_COMENTARIO_ANULACION) {
            return@withTransaction Resultado.Fallo("El comentario puede tener hasta $MAX_COMENTARIO_ANULACION letras.")
        }
        val ahora = AppClock.ahora()
        db.anulacionDao().insertar(Anulacion(idCompromiso, idEjecutivo, idMotivo, texto.ifEmpty { null }, ahora))
        db.compromisoDao().actualizar(c.copy(idEstadoCompromiso = EstadoCompromiso.ANULADO, fechaCambioEstado = ahora))
        Resultado.Exito(Unit)
    }
}
