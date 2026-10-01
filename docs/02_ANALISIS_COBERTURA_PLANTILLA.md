# Análisis e Interpretación de Cobertura con JaCoCo — Ae6

## 1. Resultado Observado

- **Cobertura de instrucciones global:** 90 % (125 / 138 instrucciones ejecutadas)
- **Cobertura de ramas global:** 83 % (15 / 18 ramas cubiertas)
- **Clase principal (`ReservaService`):**
  - **Instrucciones:** 100 % (74 / 74)
  - **Ramas:** 100 % (12 / 12)
  - **Líneas:** 100 % (15 / 15)
- **Clase de dominio (`Reserva`):**
  - **Instrucciones:** 69 % (30 / 43)
  - **Ramas:** 50 % (3 / 6)
  - **Líneas:** 75 % (9 / 12)
- **Enumeración (`EstadoReserva`):**
  - **Instrucciones:** 100 % (21 / 21)

---

## 2. Análisis Técnico de Huecos Relevantes

1. **`Reserva` — Ramas de validación en constructor no ejercitadas:**
   - La condición de guarda `id == null || id.isBlank()` posee ramas no transitadas: pasar `id = null` o un `id` compuesto de espacios en blanco `""` / `"   "`.
   - La expresión ternaria `this.tipo = tipo == null ? "NORMAL" : tipo;` tiene la rama de fallback (`tipo == null`) sin probar directamente en los tests unitarios de servicio.
   - **Riesgo:** La regla de negocio que prohíbe crear instancias inconsistentes de reserva sin identificador válido no estaba siendo probada de manera aislada a nivel de dominio.

2. **`Reserva.cancelar()` — Transición de estado (`CANCELADA`):**
   - En la suite inicial de laboratorios, el método `cancelar()` de la entidad `Reserva` no era invocado por ningún test, dejando el estado `CANCELADA` inalcanzable.
   - **Corrección:** Se añadió la prueba `cancelarReservaCambiaEstadoACancelada()` en `DominioReservaTest`, elevando la cobertura de instrucciones y garantizando la transición del ciclo de vida del objeto.

---

## 3. Decisiones y Hallazgos Clave

- **¿Qué prueba nueva se añadió a partir del análisis?**
  Se incorporó `cancelarReservaCambiaEstadoACancelada()` en el grupo `DominioReservaTest` de [`ReservaServiceTest.java`](../src/test/java/edu/uees/testing/service/ReservaServiceTest.java). Se verifica que al invocar `reserva.cancelar()`, el estado pase explícitamente a `EstadoReserva.CANCELADA`.

- **¿Qué riesgo protege esta prueba?**
  Protege la integridad del ciclo de vida de la entidad de dominio. Garantiza que las cancelaciones de reserva modifiquen efectivamente el estado interno del objeto, evitando que permanezca erróneamente en `PENDIENTE` o `CONFIRMADA`.

- **¿Por qué una cobertura alta (incluso 100%) no garantiza la corrección del software?**
  1. **Cobertura de ejecución vs. Verificación de comportamiento:** JaCoCo registra qué líneas de bytecode fueron transitadas por el hilo de ejecución, pero **no valida la calidad ni la existencia de las aserciones (`assert`)**. Un test sin aserciones puede marcar 100% de cobertura sin verificar nada.
  2. **Interacciones con colaboradores:** En `ReservaService.confirmar()`, se alcanzaba cobertura de líneas al ejecutarse, pero sin la configuración de **Mocks y Stubs** (`verify(repository).guardar()`, `verify(notificador).enviarConfirmacion()`), no se comprobaba si el sistema producía los efectos secundarios correctos (guardar en base de datos o enviar notificaciones a clientes).
  3. **Comportamientos no especificados y casos omitidos:** JaCoCo mide el código existente, pero no detecta reglas de negocio faltantes, condiciones de concurrencia, problemas de rendimiento ni entradas no contempladas en el diseño.
