package cl.duoc.pagoflex.data.db

import androidx.room.TypeConverter
import java.time.LocalDate
import java.time.LocalDateTime

class Converters {
    @TypeConverter
    fun fechaATexto(valor: LocalDate?): String? = valor?.toString()

    @TypeConverter
    fun textoAFecha(texto: String?): LocalDate? = texto?.let { LocalDate.parse(it) }

    @TypeConverter
    fun fechaHoraATexto(valor: LocalDateTime?): String? = valor?.toString()

    @TypeConverter
    fun textoAFechaHora(texto: String?): LocalDateTime? = texto?.let { LocalDateTime.parse(it) }
}
