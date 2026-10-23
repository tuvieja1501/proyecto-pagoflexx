package cl.duoc.pagoflex.data

import androidx.room.withTransaction
import cl.duoc.pagoflex.data.db.PagoFlexDatabase
import cl.duoc.pagoflex.data.db.Usuario
import cl.duoc.pagoflex.domain.AppClock
import cl.duoc.pagoflex.domain.EstadoCompromiso
import kotlinx.coroutines.flow.Flow

class RepositorioBase(private val db: PagoFlexDatabase, private val nucleo: Nucleo) {

    val usuarios: Flow<List<Usuario>> = db.usuarioDao().observarTodos()

    fun observarUsuario(id: Int): Flow<Usuario?> = db.usuarioDao().observarPorId(id)

    suspend fun usuario(id: Int): Usuario? = db.usuarioDao().porId(id)

    suspend fun inicializar() {
        Sembrador(db).sembrarSiHaceFalta()
        nucleo.cargarCatalogos()
        actualizarVencimientos()
    }

    suspend fun actualizarVencimientos(): Int = db.withTransaction {
        val hoy = AppClock.hoy()
        val ahora = AppClock.ahora()
        val vencidos = db.compromisoDao().pendientesYaVencidos(hoy)
        vencidos.forEach { c ->
            val recargo = nucleo.cat.empresas.getValue(c.idEmpresa).recargoAtraso
            db.compromisoDao().actualizar(
                c.copy(
                    idEstadoCompromiso = EstadoCompromiso.VENCIDO,
                    recargoAplicado = recargo,
                    fechaCambioEstado = ahora
                )
            )
        }
        vencidos.size
    }
}
