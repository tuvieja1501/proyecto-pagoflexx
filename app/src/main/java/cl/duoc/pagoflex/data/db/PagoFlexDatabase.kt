package cl.duoc.pagoflex.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        Rol::class, Comuna::class, Area::class, Rubro::class,
        EstadoCompromisoEntidad::class, MotivoAnulacion::class, Canal::class, MedioPago::class,
        MotivoReporte::class, EstadoReporteEntidad::class, EstadoRecomendacionEntidad::class,
        EmpresaCliente::class, Usuario::class,
        Compromiso::class, Anulacion::class, Pago::class, Reporte::class,
        Recomendacion::class, ContactoRecomendacion::class, HistorialRecomendacion::class,
        Reconocimiento::class, UsuarioReconocimiento::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class PagoFlexDatabase : RoomDatabase() {
    abstract fun usuarioDao(): UsuarioDao
    abstract fun empresaDao(): EmpresaDao
    abstract fun catalogoDao(): CatalogoDao
    abstract fun compromisoDao(): CompromisoDao
    abstract fun anulacionDao(): AnulacionDao
    abstract fun pagoDao(): PagoDao
    abstract fun reporteDao(): ReporteDao
    abstract fun recomendacionDao(): RecomendacionDao

    companion object {
        fun crear(contexto: Context): PagoFlexDatabase =
            Room.databaseBuilder(contexto.applicationContext, PagoFlexDatabase::class.java, "pagoflex.db")
                .build()
    }
}
