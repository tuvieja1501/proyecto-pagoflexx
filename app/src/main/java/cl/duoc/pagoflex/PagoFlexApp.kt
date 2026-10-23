package cl.duoc.pagoflex

import android.app.Application
import android.content.Context
import cl.duoc.pagoflex.data.Nucleo
import cl.duoc.pagoflex.data.RepositorioAgente
import cl.duoc.pagoflex.data.RepositorioBase
import cl.duoc.pagoflex.data.RepositorioEjecutivo
import cl.duoc.pagoflex.data.RepositorioUsuarioFinal
import cl.duoc.pagoflex.data.db.PagoFlexDatabase
import cl.duoc.pagoflex.util.Conectividad
import cl.duoc.pagoflex.util.Notificador
import cl.duoc.pagoflex.util.Preferencias
import cl.duoc.pagoflex.util.ProgramadorAvisos
import cl.duoc.pagoflex.util.ServicioAvisos
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class AppContainer(contexto: Context) {
    val db = PagoFlexDatabase.crear(contexto)
    val nucleo = Nucleo(db)
    val base = RepositorioBase(db, nucleo)
    val usuarioFinal = RepositorioUsuarioFinal(db, nucleo)
    val agente = RepositorioAgente(db, nucleo)
    val ejecutivo = RepositorioEjecutivo(db, nucleo)

    val preferencias = Preferencias(contexto)
    val conectividad = Conectividad(contexto)
    val notificador = Notificador(contexto)
    val servicioAvisos = ServicioAvisos(base, usuarioFinal, preferencias, notificador)

    private val candado = Mutex()

    @Volatile
    private var listo = false

    suspend fun asegurarInicializado() {
        if (listo) return
        candado.withLock {
            if (!listo) {
                base.inicializar()
                listo = true
            }
        }
    }
}

class PagoFlexApp : Application() {

    lateinit var contenedor: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        contenedor = AppContainer(this)
        contenedor.notificador.crearCanal()
        ProgramadorAvisos.programar(this)
    }
}
