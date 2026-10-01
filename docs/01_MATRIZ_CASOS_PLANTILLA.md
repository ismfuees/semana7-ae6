# Matriz de Casos de Prueba — Ae6

Esta matriz define los casos de prueba diseñados para validar el comportamiento del módulo `ReservaService` y sus entidades relacionadas, cubriendo las tres áreas funcionales clave: **Cancelación**, **Descuentos** y **Confirmación con dobles de prueba (Stub/Mock)**, además del ciclo de vida del dominio.

## Matriz de Casos

| ID | Método / Regla | Escenario | Entrada | Esperado | Tipo | Riesgo / Justificación |
|---|---|---|---|---|---|---|
| **CP-01** | `puedeCancelar` | Anticipación habitual | `horasAnticipacion = 5` | `true` | Normal | Comprueba la regla general de cancelación con suficiente margen de tiempo. |
| **CP-02** | `puedeCancelar` | Límite permitido (frontera exacta) | `horasAnticipacion = 2` | `true` | Límite | Detecta regresiones por uso incorrecto de `>` en lugar del operador `>=`. |
| **CP-03** | `puedeCancelar` | Justo debajo del límite | `horasAnticipacion = 1` | `false` | Límite | Protege la frontera estricta inferior (`< 2 horas`). |
| **CP-04** | `puedeCancelar` | Sin anticipación (inmediato) | `horasAnticipacion = 0` | `false` | Extremo | Garantiza que no se admita cancelación inmediata sin antelación. |
| **CP-05** | `calcularTotal` | Tarifa NORMAL (sin descuento) | `tipo = "NORMAL"`, `totalBase = 100.0` | `100.0` | Normal | Comprueba que el tipo estándar mantenga el 100% del valor base sin modificaciones. |
| **CP-06** | `calcularTotal` | Cliente VIP (15% descuento) | `tipo = "VIP"`, `totalBase = 100.0` | `85.0` | Alternativo | Verifica la correcta aplicación del 15% de beneficio promocional/categoría. |
| **CP-07** | `calcularTotal` | Cliente ESTUDIANTE (10% descuento) | `tipo = "ESTUDIANTE"`, `totalBase = 100.0` | `90.0` | Alternativo | Verifica la correcta aplicación del 10% de beneficio estudiantil. |
| **CP-08** | `calcularTotal` | Total base en cero | `tipo = "VIP"`, `totalBase = 0.0` | `0.0` | Límite | Comprueba el cálculo válido en la frontera monetaria inferior permitida. |
| **CP-09** | `calcularTotal` | Total base negativo | `tipo = "NORMAL"`, `totalBase = -1.0` | `IllegalArgumentException` ("Total base inválido") | Inválido | Detecta y rechaza valores monetarios incongruentes o negativos. |
| **CP-10** | `confirmar` | Reserva disponible | `Reserva("R-001", "NORMAL")`, `DisponibilidadClient` retorna `true` | Estado `CONFIRMADA`, invoca `repository.guardar()` y `notificador.enviarConfirmacion()` | Normal (Stub/Mock) | Verifica el flujo feliz completo: cambio de estado en dominio, persistencia y notificación externa. |
| **CP-11** | `confirmar` | Horario no disponible | `Reserva("R-002", "NORMAL")`, `DisponibilidadClient` retorna `false` | Lanza `IllegalStateException` ("Horario no disponible"), NO invoca `guardar()` ni `enviarConfirmacion()` | Alternativo (Stub/Mock) | Verifica la protección contra sobreventa: aborta operación sin persistir ni enviar notificaciones erróneas. |
| **CP-12** | `confirmar` | Reserva nula | `reserva = null` | Lanza `IllegalArgumentException` ("Reserva obligatoria"), NO interactúa con colaboradores | Inválido (Mock) | Protege contra `NullPointerException` inicial y asegura que ninguna dependencia sea consultada. |
| **CP-13** | `cancelar` (Dominio) | Cancelación de reserva existente | `Reserva("R-003", "NORMAL")`, invoca `cancelar()` | Estado `CANCELADA` | Dominio / JaCoCo | Ejercita la transición de estado a `CANCELADA` detectada como hueco en el análisis de cobertura de dominio. |
