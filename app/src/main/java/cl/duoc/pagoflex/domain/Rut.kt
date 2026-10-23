package cl.duoc.pagoflex.domain

object Rut {

    fun limpiar(texto: String): String =
        texto.replace(".", "").replace("-", "").replace(" ", "").uppercase()

    fun esValido(texto: String): Boolean {
        val limpio = limpiar(texto)
        if (limpio.length < 2) return false
        val cuerpo = limpio.dropLast(1)
        val dv = limpio.last()
        if (cuerpo.length > 8 || !cuerpo.all { it in '0'..'9' }) return false
        return dvEsperado(cuerpo) == dv
    }

    fun dvEsperado(cuerpo: String): Char {
        var suma = 0
        var multiplicador = 2
        for (c in cuerpo.reversed()) {
            suma += (c - '0') * multiplicador
            multiplicador = if (multiplicador == 7) 2 else multiplicador + 1
        }
        return when (val resto = 11 - (suma % 11)) {
            11 -> '0'
            10 -> 'K'
            else -> '0' + resto
        }
    }

    fun formatear(texto: String): String {
        val limpio = limpiar(texto)
        if (limpio.length < 2) return texto
        val cuerpo = limpio.dropLast(1)
        val dv = limpio.last()
        val conPuntos = cuerpo.reversed().chunked(3).joinToString(".").reversed()
        return "$conPuntos-$dv"
    }

    fun enmascarar(texto: String): String {
        val formateado = formatear(texto)
        val primerGrupo = formateado.substringBefore('.')
        val dv = formateado.substringAfterLast('-')
        return "$primerGrupo.*.*-$dv"
    }
}
