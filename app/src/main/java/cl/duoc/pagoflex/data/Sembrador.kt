package cl.duoc.pagoflex.data

import androidx.room.withTransaction
import cl.duoc.pagoflex.data.db.Anulacion
import cl.duoc.pagoflex.data.db.Area
import cl.duoc.pagoflex.data.db.Canal
import cl.duoc.pagoflex.data.db.Comuna
import cl.duoc.pagoflex.data.db.Compromiso
import cl.duoc.pagoflex.data.db.ContactoRecomendacion
import cl.duoc.pagoflex.data.db.EmpresaCliente
import cl.duoc.pagoflex.data.db.EstadoCompromisoEntidad
import cl.duoc.pagoflex.data.db.EstadoRecomendacionEntidad
import cl.duoc.pagoflex.data.db.EstadoReporteEntidad
import cl.duoc.pagoflex.data.db.HistorialRecomendacion
import cl.duoc.pagoflex.data.db.MedioPago
import cl.duoc.pagoflex.data.db.MotivoAnulacion
import cl.duoc.pagoflex.data.db.MotivoReporte
import cl.duoc.pagoflex.data.db.Pago
import cl.duoc.pagoflex.data.db.PagoFlexDatabase
import cl.duoc.pagoflex.data.db.Recomendacion
import cl.duoc.pagoflex.data.db.Reporte
import cl.duoc.pagoflex.data.db.Rol
import cl.duoc.pagoflex.data.db.Rubro
import cl.duoc.pagoflex.data.db.Usuario
import cl.duoc.pagoflex.data.seed.SeedBase
import cl.duoc.pagoflex.data.seed.SeedCompromisos

class Sembrador(private val db: PagoFlexDatabase) {

    suspend fun sembrarSiHaceFalta(): Boolean {
        if (db.usuarioDao().contar() > 0) return false
        db.withTransaction { sembrar() }
        return true
    }

    private suspend fun sembrar() {
        val cat = db.catalogoDao()
        cat.insertarRoles(SeedBase.roles.map { (id, codigo, nombre) ->
            Rol(id, codigo, nombre, SeedBase.rolDescripcion[id])
        })
        cat.insertarComunas(SeedBase.comunas.map { (id, nombre) -> Comuna(id, nombre) })
        cat.insertarAreas(SeedBase.areas.map { (id, nombre) -> Area(id, nombre) })
        cat.insertarRubros(SeedBase.rubros.map { (id, nombre) -> Rubro(id, nombre) })
        cat.insertarEstadosCompromiso(SeedBase.estadosCompromiso.map { (id, n) -> EstadoCompromisoEntidad(id, n) })
        cat.insertarMotivosAnulacion(SeedBase.motivosAnulacion.map { (id, n) -> MotivoAnulacion(id, n) })
        cat.insertarCanales(SeedBase.canales.map { (id, n) -> Canal(id, n) })
        cat.insertarMediosPago(SeedBase.mediosPago.map { (id, n) -> MedioPago(id, n) })
        cat.insertarMotivosReporte(SeedBase.motivosReporte.map { (id, n) -> MotivoReporte(id, n) })
        cat.insertarEstadosReporte(SeedBase.estadosReporte.map { (id, n) -> EstadoReporteEntidad(id, n) })
        cat.insertarEstadosRecomendacion(SeedBase.estadosRecomendacion.map { (id, n, orden) ->
            EstadoRecomendacionEntidad(id, n, orden)
        })

        db.empresaDao().insertarTodas(SeedBase.empresas.map {
            EmpresaCliente(
                idEmpresa = it.id, codigo = it.codigo, razonSocial = it.razonSocial,
                nombreFantasia = it.nombreFantasia, rut = it.rut, idRubro = it.idRubro,
                correoAtencion = it.correo, telefonoAtencion = it.telefono,
                recargoAtraso = it.recargo, activa = true
            )
        })

        db.usuarioDao().insertarTodos(SeedBase.usuarios.map {
            Usuario(
                idUsuario = it.id, idRol = it.idRol, rut = it.rut, nombres = it.nombres,
                apellidos = it.apellidos, correo = it.correo, telefono = it.telefono, activo = it.activo,
                fechaCreacion = it.fechaCreacion, idComuna = it.idComuna,
                fechaIncorporacion = it.fechaIncorporacion, avisosActivos = it.avisosActivos,
                diasAnticipacion = it.diasAnticipacion, idArea = it.idArea, idEmpresa = it.idEmpresa,
                cargo = it.cargo
            )
        })

        val compromisos = SeedCompromisos.compromisos
        val idPorFolio = compromisos.associate { it.folio to it.id }
        db.compromisoDao().insertarTodos(compromisos.map {
            Compromiso(
                idCompromiso = it.id, folio = it.folio, idUsuarioFinal = it.idUsuario, idEmpresa = it.idEmpresa,
                concepto = it.concepto, numeroCuota = it.cuota, totalCuotas = it.totalCuotas, monto = it.monto,
                fechaVencimiento = it.vencimiento, idEstadoCompromiso = it.idEstado,
                recargoAplicado = it.recargo, fechaCambioEstado = it.fechaCambioEstado,
                idEjecutivoRegistro = it.idEjecutivoRegistro, fechaRegistro = it.fechaRegistro
            )
        })

        db.anulacionDao().insertarTodas(SeedCompromisos.anulaciones.map {
            Anulacion(idPorFolio.getValue(it.folio), it.idEjecutivo, it.idMotivo, it.comentario, it.fecha)
        })

        db.pagoDao().insertarTodos(SeedCompromisos.pagos.map {
            Pago(
                numeroComprobante = it.numero, idCompromiso = idPorFolio.getValue(it.folio),
                fechaHora = it.fechaHora, montoPagado = it.monto, idCanal = it.idCanal,
                idMedioPago = it.idMedioPago
            )
        })

        db.reporteDao().insertarTodos(SeedCompromisos.reportes.map {
            Reporte(
                numero = it.numero, idCompromiso = idPorFolio.getValue(it.folio), idMotivoReporte = it.idMotivo,
                comentario = it.comentario, fechaCreacion = it.fechaCreacion, idEstadoReporte = it.idEstado,
                idAgente = it.idAgente, fechaToma = it.fechaToma, respuesta = it.respuesta,
                fechaResolucion = it.fechaResolucion
            )
        })

        val recs = SeedCompromisos.recomendaciones
        db.recomendacionDao().insertarRecomendaciones(recs.mapIndexed { i, r ->
            Recomendacion(
                idRecomendacion = i + 1, numero = r.numero, idUsuarioFinal = r.idUsuario,
                nombreOrganizacion = r.organizacion, rutOrganizacion = null, idRubro = r.idRubro,
                idComuna = r.idComuna, fecha = r.fecha, idEstadoRecomendacion = r.idEstado
            )
        })
        db.recomendacionDao().insertarContactos(recs.mapIndexedNotNull { i, r ->
            r.contactoNombre?.let {
                ContactoRecomendacion(i + 1, it, r.contactoCorreo, r.contactoTelefono, autorizaContacto = true)
            }
        })
        db.recomendacionDao().insertarHistorial(recs.mapIndexedNotNull { i, r ->
            val agente = r.idAgenteUltimoCambio
            val fecha = r.fechaUltimoCambio
            if (agente != null && fecha != null) {
                HistorialRecomendacion(
                    idRecomendacion = i + 1, idEstadoRecomendacion = r.idEstado, idAgente = agente,
                    fecha = fecha, motivo = r.motivoDescarte
                )
            } else null
        })
    }
}
