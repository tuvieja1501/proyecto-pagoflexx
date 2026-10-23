package cl.duoc.pagoflex.data.seed

import cl.duoc.pagoflex.data.seed.SeedBase.dt
import cl.duoc.pagoflex.data.seed.SeedBase.idEmpresa
import cl.duoc.pagoflex.data.seed.SeedBase.idUsuario
import cl.duoc.pagoflex.domain.CanalIds
import cl.duoc.pagoflex.domain.EstadoCompromiso
import cl.duoc.pagoflex.domain.Formatos
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth

object SeedCompromisos {

    private val PENDIENTE = EstadoCompromiso.PENDIENTE
    private val VENCIDO = EstadoCompromiso.VENCIDO
    private val PAGADO = EstadoCompromiso.PAGADO
    private val EN_REVISION = EstadoCompromiso.EN_REVISION
    private val ANULADO = EstadoCompromiso.ANULADO

    private class Explicito(
        val folio: String, val usuario: String, val empresa: String, val concepto: String,
        val cuota: Int?, val total: Int?, val monto: Int, val venc: LocalDate, val estado: Int,
        val recargo: Int, val cambio: LocalDateTime, val ejecutivo: String?, val registro: LocalDateTime
    )

    private val explicitos: List<Explicito> = listOf(

        Explicito("CP-2026-0064", "UF-01", "EC-08", "Arriendo departamento julio", 7, 12, 520000,
            LocalDate.of(2026, 7, 10), PAGADO, 0, dt(2026, 7, 9, 12), "EJ-07", dt(2026, 7, 1)),
        Explicito("CP-2026-0072", "UF-02", "EC-01", "Cuota pie departamento", 13, 36, 310000,
            LocalDate.of(2026, 8, 31), PAGADO, 8500, dt(2026, 9, 2, 21, 14), "EJ-01", dt(2026, 8, 1)),
        Explicito("CP-2026-0081", "UF-05", "EC-02", "Matrícula 2026", null, null, 120000,
            LocalDate.of(2026, 9, 4), ANULADO, 0, dt(2026, 9, 5, 11), "EJ-02", dt(2026, 8, 25)),
        Explicito("CP-2026-0089", "UF-01", "EC-08", "Arriendo departamento septiembre", 9, 12, 520000,
            LocalDate.of(2026, 9, 10), PAGADO, 0, dt(2026, 9, 9, 13, 5), "EJ-07", dt(2026, 9, 1)),
        Explicito("CP-2026-0090", "UF-01", "EC-06", "Cuota social septiembre", 9, 12, 15000,
            LocalDate.of(2026, 9, 5), PAGADO, 2000, dt(2026, 9, 7, 18, 40), "EJ-05", dt(2026, 9, 1)),
        Explicito("CP-2026-0091", "UF-01", "EC-05", "Aporte mensual septiembre", null, null, 10000,
            LocalDate.of(2026, 9, 15), PAGADO, 0, dt(2026, 9, 15, 8, 22), null, dt(2026, 9, 1)),
        Explicito("CP-2026-0095", "UF-08", "EC-06", "Cuota social octubre", 10, 12, 15000,
            LocalDate.of(2026, 10, 5), EN_REVISION, 0, dt(2026, 9, 30, 10), "EJ-05", dt(2026, 9, 28)),
        Explicito("CP-2026-0098", "UF-05", "EC-02", "Mensualidad septiembre", 7, 10, 185000,
            LocalDate.of(2026, 9, 10), PAGADO, 0, dt(2026, 9, 10, 22, 31), "EJ-02", dt(2026, 9, 1)),

        Explicito("CP-2026-0101", "UF-01", "EC-08", "Arriendo departamento octubre", 10, 12, 520000,
            LocalDate.of(2026, 10, 10), PENDIENTE, 0, dt(2026, 10, 1), "EJ-07", dt(2026, 10, 1)),
        Explicito("CP-2026-0102", "UF-01", "EC-06", "Cuota social octubre", 10, 12, 15000,
            LocalDate.of(2026, 10, 5), VENCIDO, 2000, dt(2026, 10, 6, 0), "EJ-05", dt(2026, 10, 1)),
        Explicito("CP-2026-0103", "UF-01", "EC-05", "Aporte mensual octubre", null, null, 10000,
            LocalDate.of(2026, 10, 15), PENDIENTE, 0, dt(2026, 10, 1), null, dt(2026, 10, 1)),
        Explicito("CP-2026-0104", "UF-01", "EC-03", "Crédito de consumo", 7, 24, 86400,
            LocalDate.of(2026, 9, 25), PAGADO, 0, dt(2026, 9, 24, 10, 42), "EJ-03", dt(2026, 9, 1)),
        Explicito("CP-2026-0105", "UF-01", "EC-03", "Crédito de consumo", 8, 24, 86400,
            LocalDate.of(2026, 10, 25), PENDIENTE, 0, dt(2026, 10, 1), "EJ-03", dt(2026, 10, 1)),
        Explicito("CP-2026-0106", "UF-02", "EC-01", "Cuota pie departamento", 14, 36, 310000,
            LocalDate.of(2026, 9, 30), VENCIDO, 8500, dt(2026, 10, 1, 0), "EJ-01", dt(2026, 9, 1)),
        Explicito("CP-2026-0107", "UF-02", "EC-01", "Cuota pie departamento", 15, 36, 310000,
            LocalDate.of(2026, 10, 30), PENDIENTE, 0, dt(2026, 10, 1), "EJ-01", dt(2026, 10, 1)),
        Explicito("CP-2026-0108", "UF-03", "EC-04", "Seguro de hogar octubre", 10, 12, 18900,
            LocalDate.of(2026, 10, 1), PAGADO, 0, dt(2026, 9, 30, 19, 5), null, dt(2026, 9, 15)),
        Explicito("CP-2026-0109", "UF-04", "EC-07", "Arancel octubre", 8, 10, 142000,
            LocalDate.of(2026, 10, 5), EN_REVISION, 0, dt(2026, 10, 3, 10), "EJ-06", dt(2026, 10, 1)),
        Explicito("CP-2026-0110", "UF-05", "EC-02", "Mensualidad octubre", 8, 10, 185000,
            LocalDate.of(2026, 10, 10), PENDIENTE, 0, dt(2026, 10, 1), "EJ-02", dt(2026, 10, 1)),
        Explicito("CP-2026-0111", "UF-06", "EC-03", "Crédito de consumo", 19, 36, 54200,
            LocalDate.of(2026, 9, 20), ANULADO, 0, dt(2026, 9, 22, 10), "EJ-04", dt(2026, 9, 1)),
        Explicito("CP-2026-0112", "UF-07", "EC-06", "Cuota social octubre", 10, 12, 15000,
            LocalDate.of(2026, 10, 5), PAGADO, 0, dt(2026, 10, 4, 8, 15), "EJ-05", dt(2026, 10, 1))
    )

    private val pagosExplicitos: List<SeedPago> = listOf(
        SeedPago("CPR-000330", "CP-2026-0064", dt(2026, 7, 9, 12), 520000, CanalIds.PORTAL, 1),
        SeedPago("CPR-000377", "CP-2026-0072", dt(2026, 9, 2, 21, 14), 318500, CanalIds.AGENTE_CONVERSACIONAL, 3),
        SeedPago("CPR-000398", "CP-2026-0089", dt(2026, 9, 9, 13, 5), 520000, CanalIds.PORTAL, 1),
        SeedPago("CPR-000402", "CP-2026-0090", dt(2026, 9, 7, 18, 40), 17000, CanalIds.PORTAL, 2),
        SeedPago("CPR-000405", "CP-2026-0091", dt(2026, 9, 15, 8, 22), 10000, CanalIds.AGENTE_CONVERSACIONAL, 3),
        SeedPago("CPR-000447", "CP-2026-0098", dt(2026, 9, 10, 22, 31), 185000, CanalIds.PORTAL, 1),
        SeedPago("CPR-000451", "CP-2026-0104", dt(2026, 9, 24, 10, 42), 86400, CanalIds.PORTAL, 1),
        SeedPago("CPR-000452", "CP-2026-0108", dt(2026, 9, 30, 19, 5), 18900, CanalIds.AGENTE_CONVERSACIONAL, 3),
        SeedPago("CPR-000453", "CP-2026-0112", dt(2026, 10, 4, 8, 15), 15000, CanalIds.APP, 1)
    )

    private class Slot(
        val idEmpresa: Int, val concepto: String, val cuota: Int?, val total: Int?, val monto: Int,
        val venc: LocalDate
    )

    private class Plan(val usuario: String, val slots: List<Slot>, val cantidad: Int, val atrasados: Set<Int>)

    private val INICIO_VENTANA = LocalDate.of(2025, 10, 7)

    private val MESES: List<YearMonth> = (0..11).map { YearMonth.of(2025, 10).plusMonths(it.toLong()) }

    private fun mes(ym: YearMonth) = Formatos.nombreMes(ym.monthValue) + if (ym.year == 2025) " 2025" else ""

    private fun mensual(empresa: String, base: String, monto: Int, dia: Int, conCuota: Boolean): List<Slot> =
        MESES.map { ym ->
            Slot(idEmpresa(empresa), "$base ${mes(ym)}", if (conCuota) ym.monthValue else null,
                if (conCuota) 12 else null, monto, ym.atDay(dia))
        }

    private fun escolar(empresa: String, base: String, monto: Int, dia: Int): List<Slot> =
        MESES.filter { it.monthValue in 3..12 }.map { ym ->
            Slot(idEmpresa(empresa), "$base ${mes(ym)}", ym.monthValue - 2, 10, monto, ym.atDay(dia))
        }

    private val planes: List<Plan> = listOf(
        Plan("UF-01",
            mensual("EC-08", "Arriendo departamento", 520000, 10, true) +
                mensual("EC-06", "Cuota social", 15000, 5, true) +
                mensual("EC-05", "Aporte mensual", 10000, 15, false) +
                (3..8).map { m ->
                    Slot(idEmpresa("EC-03"), "Crédito de consumo", m - 2, 24, 86400, LocalDate.of(2026, m, 25))
                },
            cantidad = 30, atrasados = setOf(7)),
        Plan("UF-02",
            (3..12).map { n ->
                Slot(idEmpresa("EC-01"), "Cuota pie departamento", n, 36, 310000,
                    YearMonth.of(2026, 9).minusMonths((14 - n).toLong()).atEndOfMonth())
            },
            cantidad = 10, atrasados = setOf(3, 8)),
        Plan("UF-03",
            mensual("EC-04", "Seguro de hogar", 18900, 1, true) +
                mensual("EC-04", "Seguro de vida", 12500, 1, true) +
                mensual("EC-04", "Seguro de salud complementario", 24300, 1, true),
            cantidad = 27, atrasados = emptySet()),
        Plan("UF-04",
            (1..7).map { n ->
                val ym = YearMonth.of(2026, 2 + n)
                Slot(idEmpresa("EC-07"), "Arancel ${mes(ym)}", n, 10, 142000, ym.atDay(5))
            },
            cantidad = 7, atrasados = setOf(4)),
        Plan("UF-05",
            escolar("EC-02", "Mensualidad", 185000, 10) +
                escolar("EC-02", "Mensualidad hijo/a menor", 160000, 10),
            cantidad = 19, atrasados = setOf(6)),
        Plan("UF-06",
            (8..18).map { n ->
                Slot(idEmpresa("EC-03"), "Crédito de consumo", n, 36, 54200,
                    YearMonth.of(2026, 9).minusMonths((19 - n).toLong()).atDay(20))
            } + MESES.mapIndexed { i, ym ->
                Slot(idEmpresa("EC-03"), "Crédito automotriz", i + 1, 48, 98000, ym.atDay(15))
            },
            cantidad = 14, atrasados = setOf(5, 11)),
        Plan("UF-07",
            mensual("EC-06", "Cuota social", 15000, 5, true),
            cantidad = 4, atrasados = emptySet())
    )

    private val ejecutivoDeEmpresa = mapOf(
        "EC-01" to "EJ-01", "EC-02" to "EJ-02", "EC-03" to "EJ-03",
        "EC-06" to "EJ-05", "EC-07" to "EJ-06", "EC-08" to "EJ-07"
    )

    private class Generado(
        val idUsuario: Int, val slot: Slot, val tarde: Boolean, val orden: Int
    )

    private fun clave(idUsuario: Int, idEmpresa: Int, concepto: String, cuota: Int?) =
        "$idUsuario|$idEmpresa|${concepto.lowercase()}|$cuota"

    private val generados: List<Generado> by lazy {
        val existentes = explicitos.map {
            clave(idUsuario(it.usuario), idEmpresa(it.empresa), it.concepto, it.cuota)
        }.toSet()
        val lista = mutableListOf<Generado>()
        for (plan in planes) {
            val uid = idUsuario(plan.usuario)
            val elegidos = plan.slots
                .filter { !it.venc.isBefore(INICIO_VENTANA) }
                .filter { clave(uid, it.idEmpresa, it.concepto, it.cuota) !in existentes }
                .sortedByDescending { it.venc }
                .take(plan.cantidad)
            check(elegidos.size == plan.cantidad) { "Faltan datos para ${plan.usuario}: ${elegidos.size}" }
            elegidos.forEachIndexed { i, slot ->
                lista += Generado(uid, slot, tarde = (i + 1) in plan.atrasados, orden = lista.size)
            }
        }
        lista.sortedWith(compareBy({ it.slot.venc }, { it.idUsuario }, { it.slot.concepto }))
    }

    private val foliosGenerados: Map<Generado, String> by lazy {
        val usados = explicitos.map { it.folio }.toMutableSet()
        val contadores = mutableMapOf<Int, Int>()
        val mapa = LinkedHashMap<Generado, String>()
        for (g in generados) {
            val anio = g.slot.venc.minusMonths(1).year
            var n = contadores.getOrDefault(anio, 0)
            var folio: String
            do {
                n++
                folio = "CP-$anio-%04d".format(n)
            } while (folio in usados)
            contadores[anio] = n
            usados += folio
            mapa[g] = folio
        }
        mapa
    }

    private fun fechaPagoGenerado(g: Generado, i: Int): LocalDateTime =
        if (g.tarde) g.slot.venc.plusDays(2).atTime(12, 0)
        else g.slot.venc.minusDays((2 + i % 3).toLong()).atTime(listOf(8, 10, 13, 18, 20)[i % 5], (i * 7) % 60)

    val compromisos: List<SeedCompromiso> by lazy {
        val lista = mutableListOf<SeedCompromiso>()
        var id = 0
        for (e in explicitos) {
            id++
            lista += SeedCompromiso(
                id = id, folio = e.folio, idUsuario = idUsuario(e.usuario), idEmpresa = idEmpresa(e.empresa),
                concepto = e.concepto, cuota = e.cuota, totalCuotas = e.total, monto = e.monto,
                vencimiento = e.venc, idEstado = e.estado, recargo = e.recargo, fechaCambioEstado = e.cambio,
                idEjecutivoRegistro = e.ejecutivo?.let { idUsuario(it) }, fechaRegistro = e.registro
            )
        }
        generados.forEachIndexed { i, g ->
            id++
            val recargo = if (g.tarde) SeedBase.recargoDe(g.slot.idEmpresa) else 0
            val codigoEmpresa = SeedBase.empresas.first { it.id == g.slot.idEmpresa }.codigo
            lista += SeedCompromiso(
                id = id, folio = foliosGenerados.getValue(g), idUsuario = g.idUsuario,
                idEmpresa = g.slot.idEmpresa, concepto = g.slot.concepto, cuota = g.slot.cuota,
                totalCuotas = g.slot.total, monto = g.slot.monto, vencimiento = g.slot.venc,
                idEstado = PAGADO, recargo = recargo, fechaCambioEstado = fechaPagoGenerado(g, i),
                idEjecutivoRegistro = ejecutivoDeEmpresa[codigoEmpresa]?.let { idUsuario(it) },
                fechaRegistro = g.slot.venc.minusMonths(1).withDayOfMonth(25).atTime(9, 0)
            )
        }
        lista
    }

    val pagos: List<SeedPago> by lazy {
        val usados = pagosExplicitos.map { it.numero }.toSet()
        var correlativo = 100
        val lista = pagosExplicitos.toMutableList()
        val sinteticos = generados.mapIndexed { i, g -> Triple(i, g, fechaPagoGenerado(g, i)) }
            .sortedBy { it.third }
        for ((i, g, fecha) in sinteticos) {
            var numero: String
            do {
                correlativo++
                numero = "CPR-%06d".format(correlativo)
            } while (numero in usados)
            val recargo = if (g.tarde) SeedBase.recargoDe(g.slot.idEmpresa) else 0
            lista += SeedPago(
                numero = numero, folio = foliosGenerados.getValue(g), fechaHora = fecha,
                monto = g.slot.monto + recargo,
                idCanal = if (i % 2 == 0) CanalIds.PORTAL else CanalIds.AGENTE_CONVERSACIONAL,
                idMedioPago = 1 + i % 3
            )
        }
        lista
    }

    val anulaciones: List<SeedAnulacion> = listOf(
        SeedAnulacion("CP-2026-0081", idUsuario("EJ-02"), 2, "Cobro duplicado de una matrícula ya pagada en marzo",
            dt(2026, 9, 5, 11)),
        SeedAnulacion("CP-2026-0111", idUsuario("EJ-04"), 1, null, dt(2026, 9, 22, 10))
    )

    val reportes: List<SeedReporte> = listOf(
        SeedReporte("REP-0005", "CP-2026-0064", 4, "El concepto dice \"agosto\" y es la cuota de julio.",
            dt(2026, 7, 28), 3, idUsuario("AG-01"), dt(2026, 7, 29, 10),
            "Se corrigió el concepto; el monto era correcto (30-07-2026).", dt(2026, 7, 30, 11)),
        SeedReporte("REP-0007", "CP-2026-0081", 2, "Me aparece una matrícula que ya pagué en marzo.",
            dt(2026, 9, 2), 3, idUsuario("AG-02"), dt(2026, 9, 3, 9),
            "Cobro duplicado anulado por el colegio (05-09-2026).", dt(2026, 9, 5, 12)),
        SeedReporte("REP-0009", "CP-2026-0111", 3, "Repacté el crédito y me siguen cobrando la cuota antigua.",
            dt(2026, 9, 18), 3, idUsuario("AG-01"), dt(2026, 9, 19, 9),
            "La empresa anuló la cuota por repactación (22-09-2026).", dt(2026, 9, 22, 11)),
        SeedReporte("REP-0010", "CP-2026-0095", 2, "Me llegó un cobro de un club al que no pertenezco.",
            dt(2026, 9, 30), 1, null, null, null, null),
        SeedReporte("REP-0011", "CP-2026-0109", 1, "Pagué por transferencia directa al instituto el 03-10.",
            dt(2026, 10, 3), 2, idUsuario("AG-02"), dt(2026, 10, 5, 9, 30), null, null)
    )

    val recomendaciones: List<SeedRecomendacion> = listOf(
        SeedRecomendacion("REC-0001", idUsuario("UF-01"), "Jardín Infantil Pequeños Exploradores", 9, 1,
            dt(2026, 8, 12), 4, "Paula Herrera", "pherrera@pexploradores.ejemplo", null,
            idUsuario("AG-04"), dt(2026, 9, 25), null),
        SeedRecomendacion("REC-0002", idUsuario("UF-01"), "Comunidad Edificio Parque Los Olmos", 10, 1,
            dt(2026, 9, 2), 2, null, null, null, idUsuario("AG-05"), dt(2026, 9, 10), null),
        SeedRecomendacion("REC-0003", idUsuario("UF-03"), "Gimnasio Fuerza Sur", 11, 3,
            dt(2026, 9, 15), 3, "Raúl Ibáñez", null, "+56 9 0000 0301", idUsuario("AG-04"), dt(2026, 9, 28), null),
        SeedRecomendacion("REC-0004", idUsuario("UF-05"), "Academia de Música Clave de Sol", 12, 5,
            dt(2026, 9, 20), 1, null, null, null, null, null, null),
        SeedRecomendacion("REC-0005", idUsuario("UF-04"), "Colegio Bosque Nativo", 2, 4,
            dt(2026, 9, 22), 5, null, null, null, idUsuario("AG-05"), dt(2026, 9, 23),
            "La organización ya es cliente de PagoFlex"),
        SeedRecomendacion("REC-0006", idUsuario("UF-07"), "Corporación Deportiva Cordillera", 6, 7,
            dt(2026, 10, 1), 1, "Ana Valdés", "avaldes@cdcordillera.ejemplo", null, null, null, null)
    )
}
