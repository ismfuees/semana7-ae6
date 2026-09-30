# Análisis de cobertura

## Resultado observado

- **Cobertura de instrucciones (total):** 90 % (125 / 138)
- **Cobertura de ramas (total):** 83 % (15 / 18)
- **Clase analizada:** `Reserva` (paquete `edu.uees.testing.domain`)
  - Instrucciones: 69 % — Ramas: 50 %
- **Clase de referencia:** `ReservaService` (paquete `edu.uees.testing.service`)
  - Instrucciones: 100 % — Ramas: 100 %

## Huecos relevantes

1. **`Reserva` — constructor, ramas de validación no cubiertas**
   - La condición `id == null || id.isBlank()` tiene dos ramas no ejercitadas:
     pasar `null` como id y pasar una cadena vacía/en blanco.
   - El ternario `tipo == null ? "NORMAL" : tipo` tiene la rama `tipo == null`
     sin cubrir: nunca se construye una `Reserva` con tipo `null`.
   - Comportamiento no protegido: la regla de negocio que impide crear
     reservas sin identificador válido no tiene ningún test que la ejercite.

2. **`Reserva.cancelar()` — método sin cobertura (hueco corregido en este laboratorio)**
   - Antes del paso 6, el método `cancelar()` nunca era invocado, lo que
     dejaba el estado `CANCELADA` completamente inalcanzable para las pruebas.
   - Se añadió `cancelarReservaCambiaEstadoACancelada()`, que construye una
     reserva, llama a `cancelar()` y afirma que el estado es `CANCELADA`.
   - Impacto: la cobertura de instrucciones de `Reserva` subió de 60 % a 69 %
     y la cobertura total del proyecto pasó de 87 % a 90 %.

## Decisiones

- **¿Qué prueba nueva se añadió?**
  `cancelarReservaCambiaEstadoACancelada()` en `ReservaServiceTest`.
  Construye una `Reserva("R-003", "NORMAL")`, invoca `cancelar()` y verifica
  que `getEstado()` devuelve `EstadoReserva.CANCELADA`.

- **¿Qué riesgo protege?**
  Protege la regla de dominio de que una reserva puede ser cancelada y que
  ese cambio de estado queda correctamente registrado. Sin esta prueba, una
  regresión en `cancelar()` (por ejemplo, que asignara el estado equivocado)
  pasaría desapercibida.

- **¿Por qué no basta con el porcentaje?**
  `ReservaService` alcanzó el 100 % de instrucciones desde el principio, pero
  los tests del Laboratorio 1 usaban `new ReservaService(null, null, null)`.
  Eso significaba que las líneas de `confirmar()` nunca se ejecutaban en
  realidad; el porcentaje alto era engañoso porque no existía ninguna aserción
  sobre las interacciones con los colaboradores. Añadir los Mocks (pasos 3–4)
  convirtió esa cobertura superficial en verificación real de comportamiento:
  se comprueba que `repository.guardar()` y `notificador.enviarConfirmacion()`
  son invocados exactamente cuando corresponde y nunca cuando no corresponde.
  La cobertura de líneas no distingue entre "la línea se ejecutó" y "la línea
  se ejecutó y su efecto fue verificado".
