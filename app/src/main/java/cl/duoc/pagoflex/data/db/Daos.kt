package cl.duoc.pagoflex.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface UsuarioDao {
    @Query("SELECT * FROM usuario ORDER BY id_rol, id_usuario")
    fun observarTodos(): Flow<List<Usuario>>

    @Query("SELECT * FROM usuario WHERE id_usuario = :id")
    fun observarPorId(id: Int): Flow<Usuario?>

    @Query("SELECT * FROM usuario WHERE id_usuario = :id")
    suspend fun porId(id: Int): Usuario?

    @Query("SELECT * FROM usuario WHERE rut = :rut LIMIT 1")
    suspend fun porRut(rut: String): Usuario?

    @Query("SELECT COUNT(*) FROM usuario")
    suspend fun contar(): Int

    @Insert
    suspend fun insertar(usuario: Usuario): Long

    @Insert
    suspend fun insertarTodos(usuarios: List<Usuario>)

    @Query("UPDATE usuario SET avisos_activos = :activos, dias_anticipacion = :dias WHERE id_usuario = :id")
    suspend fun actualizarAvisos(id: Int, activos: Boolean, dias: Int?)
}

@Dao
interface EmpresaDao {
    @Query("SELECT * FROM empresa_cliente ORDER BY codigo")
    fun observarTodas(): Flow<List<EmpresaCliente>>

    @Query("SELECT * FROM empresa_cliente WHERE id_empresa = :id")
    suspend fun porId(id: Int): EmpresaCliente?

    @Insert
    suspend fun insertarTodas(empresas: List<EmpresaCliente>)
}

@Dao
interface CatalogoDao {
    @Insert
    suspend fun insertarRoles(items: List<Rol>)

    @Insert
    suspend fun insertarComunas(items: List<Comuna>)

    @Insert
    suspend fun insertarAreas(items: List<Area>)

    @Insert
    suspend fun insertarRubros(items: List<Rubro>)

    @Insert
    suspend fun insertarEstadosCompromiso(items: List<EstadoCompromisoEntidad>)

    @Insert
    suspend fun insertarMotivosAnulacion(items: List<MotivoAnulacion>)

    @Insert
    suspend fun insertarCanales(items: List<Canal>)

    @Insert
    suspend fun insertarMediosPago(items: List<MedioPago>)

    @Insert
    suspend fun insertarMotivosReporte(items: List<MotivoReporte>)

    @Insert
    suspend fun insertarEstadosReporte(items: List<EstadoReporteEntidad>)

    @Insert
    suspend fun insertarEstadosRecomendacion(items: List<EstadoRecomendacionEntidad>)

    @Query("SELECT * FROM comuna ORDER BY nombre")
    fun observarComunas(): Flow<List<Comuna>>

    @Query("SELECT * FROM estado_compromiso ORDER BY id_estado_compromiso")
    fun observarEstadosCompromiso(): Flow<List<EstadoCompromisoEntidad>>

    @Query("SELECT * FROM motivo_anulacion ORDER BY id_motivo_anulacion")
    fun observarMotivosAnulacion(): Flow<List<MotivoAnulacion>>

    @Query("SELECT * FROM canal ORDER BY id_canal")
    fun observarCanales(): Flow<List<Canal>>

    @Query("SELECT * FROM medio_pago ORDER BY id_medio_pago")
    fun observarMediosPago(): Flow<List<MedioPago>>

    @Query("SELECT * FROM motivo_reporte ORDER BY id_motivo_reporte")
    fun observarMotivosReporte(): Flow<List<MotivoReporte>>

    @Query("SELECT * FROM estado_reporte ORDER BY id_estado_reporte")
    fun observarEstadosReporte(): Flow<List<EstadoReporteEntidad>>

    @Query("SELECT * FROM rubro ORDER BY nombre")
    fun observarRubros(): Flow<List<Rubro>>

    @Query("SELECT * FROM area ORDER BY nombre")
    fun observarAreas(): Flow<List<Area>>
}

@Dao
interface CompromisoDao {
    @Query("SELECT * FROM compromiso WHERE id_usuario_final = :idUsuario ORDER BY fecha_vencimiento, id_compromiso")
    fun observarPorUsuario(idUsuario: Int): Flow<List<Compromiso>>

    @Query("SELECT * FROM compromiso WHERE id_empresa = :idEmpresa ORDER BY fecha_vencimiento, id_compromiso")
    fun observarPorEmpresa(idEmpresa: Int): Flow<List<Compromiso>>

    @Query("SELECT * FROM compromiso")
    fun observarTodos(): Flow<List<Compromiso>>

    @Query("SELECT * FROM compromiso WHERE id_compromiso = :id")
    fun observarPorId(id: Int): Flow<Compromiso?>

    @Query("SELECT * FROM compromiso WHERE id_compromiso = :id")
    suspend fun porId(id: Int): Compromiso?

    @Query("SELECT * FROM compromiso WHERE id_usuario_final = :idUsuario")
    suspend fun listarPorUsuario(idUsuario: Int): List<Compromiso>

    @Query("SELECT * FROM compromiso WHERE id_estado_compromiso = 1 AND fecha_vencimiento < :hoy")
    suspend fun pendientesYaVencidos(hoy: LocalDate): List<Compromiso>

    @Query(
        "SELECT COUNT(*) FROM compromiso WHERE id_usuario_final = :idUsuario AND id_empresa = :idEmpresa " +
            "AND LOWER(concepto) = LOWER(:concepto) AND numero_cuota IS :numeroCuota"
    )
    suspend fun contarDuplicados(idUsuario: Int, idEmpresa: Int, concepto: String, numeroCuota: Int?): Int

    @Query("SELECT folio FROM compromiso WHERE folio LIKE :prefijo || '%' ORDER BY folio DESC LIMIT 1")
    suspend fun ultimoFolio(prefijo: String): String?

    @Insert
    suspend fun insertar(compromiso: Compromiso): Long

    @Insert
    suspend fun insertarTodos(compromisos: List<Compromiso>)

    @Update
    suspend fun actualizar(compromiso: Compromiso)
}

@Dao
interface AnulacionDao {
    @Insert
    suspend fun insertar(anulacion: Anulacion)

    @Insert
    suspend fun insertarTodas(anulaciones: List<Anulacion>)

    @Query("SELECT * FROM anulacion WHERE id_compromiso = :idCompromiso")
    suspend fun porCompromiso(idCompromiso: Int): Anulacion?

    @Query("SELECT * FROM anulacion WHERE id_compromiso = :idCompromiso")
    fun observarPorCompromiso(idCompromiso: Int): Flow<Anulacion?>

    @Query(
        "SELECT a.* FROM anulacion a JOIN compromiso c ON c.id_compromiso = a.id_compromiso " +
            "WHERE c.id_usuario_final = :idUsuario"
    )
    fun observarPorUsuario(idUsuario: Int): Flow<List<Anulacion>>

    @Query(
        "SELECT a.* FROM anulacion a JOIN compromiso c ON c.id_compromiso = a.id_compromiso " +
            "WHERE c.id_empresa = :idEmpresa"
    )
    fun observarPorEmpresa(idEmpresa: Int): Flow<List<Anulacion>>
}

@Dao
interface PagoDao {
    @Insert
    suspend fun insertar(pago: Pago): Long

    @Insert
    suspend fun insertarTodos(pagos: List<Pago>)

    @Query("SELECT * FROM pago WHERE id_compromiso = :idCompromiso")
    suspend fun porCompromiso(idCompromiso: Int): Pago?

    @Query("SELECT * FROM pago WHERE id_compromiso = :idCompromiso")
    fun observarPorCompromiso(idCompromiso: Int): Flow<Pago?>

    @Query(
        "SELECT p.* FROM pago p JOIN compromiso c ON c.id_compromiso = p.id_compromiso " +
            "WHERE c.id_usuario_final = :idUsuario ORDER BY p.fecha_hora DESC"
    )
    fun observarPorUsuario(idUsuario: Int): Flow<List<Pago>>

    @Query(
        "SELECT p.* FROM pago p JOIN compromiso c ON c.id_compromiso = p.id_compromiso " +
            "WHERE c.id_empresa = :idEmpresa ORDER BY p.fecha_hora DESC"
    )
    fun observarPorEmpresa(idEmpresa: Int): Flow<List<Pago>>

    @Query("SELECT numero_comprobante FROM pago ORDER BY numero_comprobante DESC LIMIT 1")
    suspend fun ultimoNumeroComprobante(): String?
}

@Dao
interface ReporteDao {
    @Insert
    suspend fun insertar(reporte: Reporte): Long

    @Insert
    suspend fun insertarTodos(reportes: List<Reporte>)

    @Update
    suspend fun actualizar(reporte: Reporte)

    @Query("SELECT * FROM reporte WHERE id_reporte = :id")
    suspend fun porId(id: Int): Reporte?

    @Query("SELECT * FROM reporte WHERE id_reporte = :id")
    fun observarPorId(id: Int): Flow<Reporte?>

    @Query("SELECT * FROM reporte WHERE id_compromiso = :idCompromiso AND id_estado_reporte <> 3 LIMIT 1")
    suspend fun reporteVigenteDe(idCompromiso: Int): Reporte?

    @Query("SELECT * FROM reporte WHERE id_compromiso = :idCompromiso ORDER BY fecha_creacion DESC")
    fun observarPorCompromiso(idCompromiso: Int): Flow<List<Reporte>>

    @Query("SELECT * FROM reporte ORDER BY fecha_creacion")
    fun observarTodos(): Flow<List<Reporte>>

    @Query(
        "SELECT r.* FROM reporte r JOIN compromiso c ON c.id_compromiso = r.id_compromiso " +
            "WHERE c.id_usuario_final = :idUsuario ORDER BY r.fecha_creacion DESC"
    )
    fun observarPorUsuario(idUsuario: Int): Flow<List<Reporte>>

    @Query(
        "SELECT r.* FROM reporte r JOIN compromiso c ON c.id_compromiso = r.id_compromiso " +
            "WHERE c.id_empresa = :idEmpresa ORDER BY r.fecha_creacion DESC"
    )
    fun observarPorEmpresa(idEmpresa: Int): Flow<List<Reporte>>

    @Query("SELECT numero FROM reporte ORDER BY numero DESC LIMIT 1")
    suspend fun ultimoNumero(): String?
}

@Dao
interface RecomendacionDao {
    @Insert
    suspend fun insertarRecomendaciones(items: List<Recomendacion>)

    @Insert
    suspend fun insertarContactos(items: List<ContactoRecomendacion>)

    @Insert
    suspend fun insertarHistorial(items: List<HistorialRecomendacion>)

    @Insert
    suspend fun insertarReconocimientos(items: List<Reconocimiento>)

    @Insert
    suspend fun insertarUsuarioReconocimientos(items: List<UsuarioReconocimiento>)
}
