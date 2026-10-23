# PagoFlex – MVP móvil (DSY1105)

**Integrantes:** _(escribir nombre y apellido de cada integrante de la pareja)_  
**Asignatura:** DSY1105 Desarrollo de Aplicaciones Móviles – Evaluación Parcial 2

App Android nativa en Kotlin: Jetpack Compose + Material 3, MVVM, Coroutines/Flow y Room (SQLite local).
Todos los datos son ficticios (Anexo 9). Sesión simulada (RF-01), sin servidor.

## Cómo abrirla
1. Android Studio (Ladybug o más nuevo) → *Open* → carpeta `PagoFlex`.
2. Esperar el *Gradle Sync* (necesita internet la primera vez).
3. Ejecutar en un emulador o teléfono con Android 8.0+ (API 26).

## Roles (se elige la cuenta al entrar)
- Usuario final (ej. Camila): inicio, detalle, pago simulado, comprobante, historial con filtros, cumplimiento, reportar problema, avisos de vencimiento (RF-02..RF-11).
- Agente (ej. Andrés): bandeja de reportes, tomar y resolver (RF-14, RF-15).
- Ejecutivo (ej. Paula): compromisos de su empresa, registrar, anular, cobranza (RF-17..RF-20).

## Requisitos de la evaluación
- Material 3: `ui/theme`, `ui/componentes`.
- Formularios validados: registrar compromiso, reportar, anular, resolver, pago.
- Navegación: Navigation Compose por rol, con transiciones.
- Estado: ViewModel + StateFlow.
- Almacenamiento local: Room (22 tablas del MER) + SharedPreferences.
- Recursos nativos: notificaciones, WorkManager, BiometricPrompt (huella/PIN), ConnectivityManager, vibración al pagar.
- Animaciones: transiciones, anillo de progreso, total animado, check del comprobante, banners y listas.

## Fecha de referencia
`domain/AppClock.kt`: `USAR_FECHA_REFERENCIA = true` fija "hoy" en 06-10-2026 para que coincida con el Anexo 9. Cámbialo a `false` para usar la fecha real.

## Pruebas
`./gradlew test` (reglas de negocio, RUT y formatos).
