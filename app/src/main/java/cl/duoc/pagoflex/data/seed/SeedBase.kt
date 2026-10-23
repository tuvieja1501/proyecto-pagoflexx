package cl.duoc.pagoflex.data.seed

import cl.duoc.pagoflex.domain.RolIds
import java.time.LocalDate
import java.time.LocalDateTime

object SeedBase {

    fun dt(anio: Int, mes: Int, dia: Int, hora: Int = 9, minuto: Int = 0): LocalDateTime =
        LocalDateTime.of(anio, mes, dia, hora, minuto)

    val roles = listOf(
        Triple(RolIds.USUARIO_FINAL, "USUARIO_FINAL", "Usuario final"),
        Triple(RolIds.AGENTE, "AGENTE", "Agente PagoFlex"),
        Triple(RolIds.EJECUTIVO, "EJECUTIVO", "Ejecutivo de empresa cliente")
    )

    val rolDescripcion = mapOf(
        RolIds.USUARIO_FINAL to "Persona que paga compromisos a empresas clientes de PagoFlex.",
        RolIds.AGENTE to "Integrante de soporte o comercial: gestiona reportes de problema.",
        RolIds.EJECUTIVO to "Personal de cobranza de una empresa cliente: registra y anula compromisos."
    )

    val comunas = listOf(
        1 to "Maipú", 2 to "Puente Alto", 3 to "San Miguel", 4 to "La Florida",
        5 to "Ñuñoa", 6 to "Quilicura", 7 to "San Bernardo", 8 to "Estación Central"
    )

    val areas = listOf(1 to "Soporte", 2 to "Comercial")

    val rubros = listOf(
        1 to "Inmobiliaria", 2 to "Educación escolar", 3 to "Crédito de consumo", 4 to "Seguros",
        5 to "ONG", 6 to "Club deportivo", 7 to "Educación superior", 8 to "Administración de arriendos",
        9 to "Educación parvularia", 10 to "Administración de condominio", 11 to "Deporte",
        12 to "Educación artística"
    )

    val estadosCompromiso = listOf(
        1 to "Pendiente", 2 to "Vencido", 3 to "Pagado", 4 to "En revisión", 5 to "Anulado"
    )

    val motivosAnulacion = listOf(
        1 to "Repactación", 2 to "Error de registro", 3 to "Pagado directamente a la empresa", 4 to "Otro"
    )

    val canales = listOf(1 to "App", 2 to "Portal de pagos", 3 to "Agente conversacional")

    val mediosPago = listOf(
        1 to "Tarjeta de débito", 2 to "Tarjeta de crédito", 3 to "Transferencia", 4 to "Pago simulado"
    )

    val motivosReporte = listOf(
        1 to "Ya pagué por otro medio", 2 to "No reconozco este cobro", 3 to "El monto no corresponde", 4 to "Otro"
    )

    val estadosReporte = listOf(1 to "Abierto", 2 to "En gestión", 3 to "Resuelto")

    val estadosRecomendacion = listOf(
        Triple(1, "Recibida", 1), Triple(2, "Contactada", 2), Triple(3, "En evaluación", 3),
        Triple(4, "Se unió", 4), Triple(5, "Descartada", 4)
    )

    val empresas = listOf(
        SeedEmpresa(1, "EC-01", "Inmobiliaria Altos del Maipo SpA", "Altos del Maipo", "76.845.213-K", 1,
            "atencion@altosmaipo.ejemplo", "+56 2 0000 0201", 8500),
        SeedEmpresa(2, "EC-02", "Corporación Educacional Bosque Nativo", "Colegio Bosque Nativo", "65.132.874-8", 2,
            "pagos@bosquenativo.ejemplo", "+56 2 0000 0202", 5000),
        SeedEmpresa(3, "EC-03", "Crédito Andino S.A.", "Crédito Andino", "96.571.438-3", 3,
            "clientes@creditoandino.ejemplo", "+56 2 0000 0203", 12000),
        SeedEmpresa(4, "EC-04", "Seguros Horizonte Sur S.A.", "Horizonte Sur", "96.814.205-4", 4,
            "contacto@horizontesur.ejemplo", "+56 2 0000 0204", 3000),
        SeedEmpresa(5, "EC-05", "Fundación Manos Abiertas", "Manos Abiertas", "65.247.190-0", 5,
            "socios@manosabiertas.ejemplo", "+56 2 0000 0205", 0),
        SeedEmpresa(6, "EC-06", "Club Deportivo Los Halcones", "Club Los Halcones", "65.398.412-K", 6,
            "tesoreria@loshalcones.ejemplo", "+56 2 0000 0206", 2000),
        SeedEmpresa(7, "EC-07", "Instituto Técnico Cordillera SpA", "Instituto Cordillera", "77.214.583-7", 7,
            "finanzas@itcordillera.ejemplo", "+56 2 0000 0207", 6000),
        SeedEmpresa(8, "EC-08", "Arriendos Plaza Oriente Ltda.", "Plaza Oriente", "76.432.871-K", 8,
            "cobranza@plazaoriente.ejemplo", "+56 2 0000 0208", 7500)
    )

    private fun final(
        id: Int, codigo: String, rut: String, nombres: String, apellidos: String, correo: String,
        telefono: String, idComuna: Int, incorporacion: LocalDate, avisos: Boolean, dias: Int?
    ) = SeedUsuario(
        id = id, codigo = codigo, idRol = RolIds.USUARIO_FINAL, rut = rut, nombres = nombres,
        apellidos = apellidos, correo = correo, telefono = telefono, activo = true,
        fechaCreacion = incorporacion.atStartOfDay(), idComuna = idComuna, fechaIncorporacion = incorporacion,
        avisosActivos = avisos, diasAnticipacion = dias
    )

    private fun agente(
        id: Int, codigo: String, rut: String, nombres: String, apellidos: String, correo: String,
        idArea: Int, activo: Boolean
    ) = SeedUsuario(
        id = id, codigo = codigo, idRol = RolIds.AGENTE, rut = rut, nombres = nombres, apellidos = apellidos,
        correo = correo, telefono = null, activo = activo, fechaCreacion = dt(2025, 1, 2), idArea = idArea
    )

    private fun ejecutivo(
        id: Int, codigo: String, rut: String, nombres: String, apellidos: String, correo: String,
        telefono: String, idEmpresa: Int, cargo: String
    ) = SeedUsuario(
        id = id, codigo = codigo, idRol = RolIds.EJECUTIVO, rut = rut, nombres = nombres, apellidos = apellidos,
        correo = correo, telefono = telefono, activo = true, fechaCreacion = dt(2025, 1, 2),
        idEmpresa = idEmpresa, cargo = cargo
    )

    val usuarios: List<SeedUsuario> = listOf(
        final(1, "UF-01", "15.834.207-3", "Camila Andrea", "Rojas Peña", "crojas@correo.ejemplo",
            "+56 9 0000 0101", 1, LocalDate.of(2023, 3, 14), true, 3),
        final(2, "UF-02", "12.476.318-5", "Luis Alberto", "Muñoz Tapia", "lmunoz@correo.ejemplo",
            "+56 9 0000 0102", 2, LocalDate.of(2023, 11, 2), true, 1),
        final(3, "UF-03", "6.893.541-5", "Rosa Elena", "Contreras Díaz", "rcontreras@correo.ejemplo",
            "+56 9 0000 0103", 3, LocalDate.of(2024, 6, 20), true, 5),
        final(4, "UF-04", "21.045.873-5", "Matías Ignacio", "Soto Vergara", "msoto@correo.ejemplo",
            "+56 9 0000 0104", 4, LocalDate.of(2025, 3, 7), false, null),
        final(5, "UF-05", "17.562.904-1", "Fernanda Paz", "Godoy Lagos", "fgodoy@correo.ejemplo",
            "+56 9 0000 0105", 5, LocalDate.of(2024, 1, 15), true, 3),
        final(6, "UF-06", "10.318.265-4", "Héctor Manuel", "Pizarro Campos", "hpizarro@correo.ejemplo",
            "+56 9 0000 0106", 6, LocalDate.of(2023, 8, 28), true, 3),
        final(7, "UF-07", "19.287.430-0", "Javiera Constanza", "Reyes Molina", "jreyes@correo.ejemplo",
            "+56 9 0000 0107", 7, LocalDate.of(2025, 5, 3), true, 1),
        final(8, "UF-08", "18.704.396-4", "Benjamín Esteban", "Araya Fuentes", "baraya@correo.ejemplo",
            "+56 9 0000 0108", 8, LocalDate.of(2026, 9, 11), true, 3),

        agente(9, "AG-01", "16.248.317-K", "Andrés Felipe", "Lagos Meza", "alagos@pagoflex.ejemplo", 1, true),
        agente(10, "AG-02", "18.352.091-1", "Daniela Sofía", "Ortiz Bravo", "dortiz@pagoflex.ejemplo", 1, true),
        agente(11, "AG-03", "13.907.264-2", "Ricardo José", "Salinas Cáceres", "rsalinas@pagoflex.ejemplo", 1, false),
        agente(12, "AG-04", "17.781.435-0", "Valentina Ignacia", "Paredes Silva", "vparedes@pagoflex.ejemplo", 2, true),
        agente(13, "AG-05", "19.564.028-9", "Tomás Alejandro", "Vidal Rojas", "tvidal@pagoflex.ejemplo", 2, true),

        ejecutivo(14, "EJ-01", "14.629.853-2", "Paula Andrea", "Saavedra Núñez", "psaavedra@altosmaipo.ejemplo",
            "+56 9 0000 0401", 1, "Ejecutiva de cobranza"),
        ejecutivo(15, "EJ-02", "11.853.720-3", "Marco Antonio", "Fuentes Riquelme", "mfuentes@bosquenativo.ejemplo",
            "+56 9 0000 0402", 2, "Encargado de finanzas"),
        ejecutivo(16, "EJ-03", "15.370.946-7", "Claudia Beatriz", "Henríquez Soto", "chenriquez@creditoandino.ejemplo",
            "+56 9 0000 0403", 3, "Jefa de cobranza"),
        ejecutivo(17, "EJ-04", "17.095.482-3", "Felipe Ignacio", "Morales Tapia", "fmorales@creditoandino.ejemplo",
            "+56 9 0000 0404", 3, "Ejecutivo de cobranza"),
        ejecutivo(18, "EJ-05", "9.846.217-1", "Gloria Inés", "Carrasco Vega", "gcarrasco@loshalcones.ejemplo",
            "+56 9 0000 0405", 6, "Tesorera"),
        ejecutivo(19, "EJ-06", "18.930.571-0", "Sebastián Andrés", "Núñez Olivares", "snunez@itcordillera.ejemplo",
            "+56 9 0000 0406", 7, "Analista de finanzas"),
        ejecutivo(20, "EJ-07", "12.584.396-4", "Mónica Patricia", "Leiva Castro", "mleiva@plazaoriente.ejemplo",
            "+56 9 0000 0407", 8, "Administradora de cobranza")
    )

    private val idPorCodigo: Map<String, Int> = usuarios.associate { it.codigo to it.id }
    private val idEmpresaPorCodigo: Map<String, Int> = empresas.associate { it.codigo to it.id }

    fun idUsuario(codigo: String): Int = idPorCodigo.getValue(codigo)
    fun idEmpresa(codigo: String): Int = idEmpresaPorCodigo.getValue(codigo)
    fun recargoDe(idEmpresa: Int): Int = empresas.first { it.id == idEmpresa }.recargo
}
