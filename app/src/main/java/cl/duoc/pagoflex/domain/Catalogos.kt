package cl.duoc.pagoflex.domain

object RolIds {
    const val USUARIO_FINAL = 1
    const val AGENTE = 2
    const val EJECUTIVO = 3
}

object EstadoCompromiso {
    const val PENDIENTE = 1
    const val VENCIDO = 2
    const val PAGADO = 3
    const val EN_REVISION = 4
    const val ANULADO = 5
}

object EstadoReporte {
    const val ABIERTO = 1
    const val EN_GESTION = 2
    const val RESUELTO = 3
}

object CanalIds {
    const val APP = 1
    const val PORTAL = 2
    const val AGENTE_CONVERSACIONAL = 3
}

object MedioPagoIds {
    const val TARJETA_DEBITO = 1
    const val TARJETA_CREDITO = 2
    const val TRANSFERENCIA = 3
    const val PAGO_SIMULADO = 4
}

object MotivoReporteIds {
    const val YA_PAGUE = 1
    const val NO_RECONOZCO = 2
    const val MONTO_NO_CORRESPONDE = 3
    const val OTRO = 4
}

object MotivoAnulacionIds {
    const val REPACTACION = 1
    const val ERROR_DE_REGISTRO = 2
    const val PAGADO_DIRECTAMENTE = 3
    const val OTRO = 4
}

object EstadoRecomendacionIds {
    const val RECIBIDA = 1
    const val CONTACTADA = 2
    const val EN_EVALUACION = 3
    const val SE_UNIO = 4
    const val DESCARTADA = 5
}
