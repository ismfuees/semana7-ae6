# Justificación Técnica de Dobles de Prueba (Stub / Mock) — Ae6

Este documento detalla el uso, configuración y justificación de dobles de prueba para aislar y validar el método `confirmar(Reserva reserva)` en [`ReservaService`](../src/main/java/edu/uees/testing/service/ReservaService.java).

---

## 1. Dependencias y Clasificación de Dobles

Para probar `ReservaService.confirmar()`, el servicio interactúa con tres interfaces externas:

| Dependencia | Rol de Doble | Propósito Técnico |
|---|---|---|
| [`DisponibilidadClient`](../src/main/java/edu/uees/testing/availability/DisponibilidadClient.java) | **Stub (Indirect Input)** | Controla y simula la respuesta de disponibilidad del horario (`true` o `false`) sin realizar llamadas remotas/externas. Permite guiar el flujo hacia el caso exitoso o alternativo de forma determinista. |
| [`ReservaRepository`](../src/main/java/edu/uees/testing/repository/ReservaRepository.java) | **Mock (Indirect Output)** | Verifica la interacción de persistencia (`guardar()`), asegurando que solo se guarde la reserva si todas las reglas previas fueron satisfechas y nunca en caso de error. |
| [`Notificador`](../src/main/java/edu/uees/testing/notification/Notificador.java) | **Mock (Indirect Output)** | Verifica la interacción de notificación al cliente (`enviarConfirmacion()`), garantizando que los mensajes salientes no se disparen ante reservas inválidas o no disponibles. |

---

## 2. Escenarios Probados y Verificaciones

### Escenario 1: Reserva disponible (Flujo Feliz) — `CP-10`
- **Stub**: `when(disponibilidad.estaDisponible(any(Reserva.class))).thenReturn(true);`
- **Estado verificado**: `assertEquals(EstadoReserva.CONFIRMADA, reserva.getEstado());`
- **Interacciones verificadas (Mocks)**:
  - `verify(disponibilidad).estaDisponible(reserva);` (Consulta de disponibilidad realizada).
  - `verify(repository).guardar(reserva);` (Persistencia ejecutada exactamente con la reserva confirmada).
  - `verify(notificador).enviarConfirmacion(reserva);` (Notificación enviada al cliente).

### Escenario 2: Horario no disponible (Fallo de regla) — `CP-11`
- **Stub**: `when(disponibilidad.estaDisponible(any(Reserva.class))).thenReturn(false);`
- **Excepción verificada**: `assertThrows(IllegalStateException.class, () -> servicio.confirmar(reserva));`
- **Interacciones verificadas (Mocks)**:
  - `verify(repository, never()).guardar(any());` (Se garantiza que NO hubo escritura en base de datos).
  - `verify(notificador, never()).enviarConfirmacion(any());` (Se garantiza que NO se envió correo/notificación falsa).

### Escenario 3: Reserva nula (Validación de entrada) — `CP-12`
- **Excepción verificada**: `assertThrows(IllegalArgumentException.class, () -> servicio.confirmar(null));`
- **Interacciones verificadas (Mocks)**:
  - `verify(disponibilidad, never()).estaDisponible(any());`
  - `verify(repository, never()).guardar(any());`
  - `verify(notificador, never()).enviarConfirmacion(any());`
  - *Justificación*: Ante un argumento nulo, el servicio debe fallar inmediatamente por guarda rápida (*fail-fast*) sin consumir recursos de colaboradores externos.

---

## 3. ¿Por qué estas interacciones son críticas para el negocio?

1. **Prevención de sobreventa e inconsistencia:** Si el repositorio guardara reservas sin verificar disponibilidad, se produciría *overbooking*.
2. **Evitar efectos secundarios no deseados:** Enviar notificaciones de confirmación cuando una reserva falló confunde al cliente y daña la reputación del servicio.
3. **Aislamiento unitario:** Las pruebas se ejecutan en milisegundos sin requerir bases de datos reales ni servicios de mensajería activos, garantizando reproducibilidad y determinismo.
