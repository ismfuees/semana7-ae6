# Matriz de casos

| ID | Regla | Escenario | Entrada | Esperado | Tipo | Riesgo / Justificación |
|---|---|---|---|---|---|---|
| CP-01 | Cancelación | Anticipación habitual | `horasAnticipacion = 5` | `true` | Normal | Comprueba la regla general de cancelación. |
| CP-02 | Cancelación | Límite permitido | `horasAnticipacion = 2` | `true` | Límite | Detecta uso incorrecto de `>` en vez de `>=`. |
| CP-03 | Cancelación | Debajo del límite | `horasAnticipacion = 1` | `false` | Límite | Protege la frontera inferior (`< 2`). |
| CP-04 | Cancelación | Sin anticipación | `horasAnticipacion = 0` | `false` | Extremo | No debe permitir la cancelación inmediata. |
| CP-05 | Descuentos | Tarifa NORMAL | `tipo = "NORMAL"`, `totalBase = 100.0` | `100.0` | Normal | Comprueba que no se aplique descuento. |
| CP-06 | Descuentos | Cliente VIP (15%) | `tipo = "VIP"`, `totalBase = 100.0` | `85.0` | Alternativo | Verifica la aplicación del 15% de descuento. |
| CP-07 | Descuentos | Cliente ESTUDIANTE (10%) | `tipo = "ESTUDIANTE"`, `totalBase = 100.0` | `90.0` | Alternativo | Verifica la aplicación del 10% de descuento. |
| CP-08 | Descuentos | Total base cero | `tipo = "VIP"`, `totalBase = 0.0` | `0.0` | Límite | Frontera inferior permitida para monto base. |
| CP-09 | Descuentos | Total base negativo | `tipo = "NORMAL"`, `totalBase = -1.0` | `IllegalArgumentException` ("Total base inválido") | Inválido | Detecta y rechaza valores monetarios negativos. |
