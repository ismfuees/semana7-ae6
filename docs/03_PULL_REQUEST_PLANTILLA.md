# Pull Request — Ae6: Suite de Pruebas, Cobertura y Dobles de Prueba

**Título del Pull Request:**
`test: completar suite, cobertura y evidencia de Ae6`

**Rama Base:** `main`  
**Rama a Integrar (Compare):** `ae6/suite-pruebas`

---

## ## Objetivo
Proteger las reglas críticas de negocio del módulo de reservas (`ReservaService` y entidad `Reserva`), garantizar la correcta interacción con colaboradores externos mediante el uso justificado de dobles de prueba (Stubs y Mocks), documentar el análisis técnico de cobertura con JaCoCo y asegurar la trazabilidad del desarrollo mediante commits semánticos e incrementales.

---

## ## Cambios Realizados
- **Matriz de Casos de Prueba (`docs/01_MATRIZ_CASOS_PLANTILLA.md`):** Definición detallada de 13 casos de prueba cubriendo flujos normales, valores de borde/límite, condiciones de error/excepción y colaboraciones.
- **Suite JUnit 5 (`src/test/java/edu/uees/testing/service/ReservaServiceTest.java`):**
  - Implementación bajo patrón AAA (Arrange - Act - Assert).
  - Estructuración semántica mediante anotaciones `@Nested` y `@DisplayName` para organizar las reglas de cancelación, descuentos, confirmación y dominio.
  - Validación de políticas de cancelación (`puedeCancelar` con 5h, 2h límite, 1h bajo límite y 0h extremo).
  - Validación de descuentos (`calcularTotal` con tarifas `NORMAL`, `VIP` al 15%, `ESTUDIANTE` al 10%, base cero y rechazo de montos negativos con `IllegalArgumentException`).
  - Dobles de prueba con Mockito para aislar y verificar `confirmar(reserva)` ante reservas disponibles, no disponibles y nulas.
  - Prueba de ciclo de vida de dominio `cancelar()` para cubrir la transición de estado a `CANCELADA`.
- **Justificación de Dobles de Prueba (`docs/04_JUSTIFICACION_DOBLES_PRUEBA.md`):** Documentación técnica del uso de `DisponibilidadClient` como Stub y `ReservaRepository`/`Notificador` como Mocks.
- **Análisis de Cobertura JaCoCo (`docs/02_ANALISIS_COBERTURA_PLANTILLA.md`):** Interpretación de métricas de instrucciones y ramas, identificación de huecos y justificación de por qué la cobertura alta no equivale a corrección sin aserciones pertinentes.

---

## ## Casos de Prueba Implementados
- `CP-01`: Cancelación con 5 horas de anticipación (permitida, caso normal).
- `CP-02`: Cancelación con 2 horas exactas de anticipación (frontera límite permitida).
- `CP-03`: Cancelación con 1 hora de anticipación (frontera inferior rechazada).
- `CP-04`: Cancelación con 0 horas de anticipación (rechazo inmediato).
- `CP-05`: Tarifa NORMAL mantiene el 100% del monto base.
- `CP-06`: Tarifa VIP aplica el 15% de descuento.
- `CP-07`: Tarifa ESTUDIANTE aplica el 10% de descuento.
- `CP-08`: Monto base cero retorna 0.0 sin errores.
- `CP-09`: Monto base negativo lanza `IllegalArgumentException` ("Total base inválido").
- `CP-10`: Reserva disponible se confirma (`CONFIRMADA`), se guarda en repositorio y se notifica (Stub + Mock verify).
- `CP-11`: Horario no disponible lanza `IllegalStateException` y garantiza no-persistencia y no-notificación (`never()`).
- `CP-12`: Reserva nula lanza `IllegalArgumentException` ("Reserva obligatoria") sin invocar a ningún colaborador externo.
- `CP-13`: Cancelación de reserva cambia su estado de dominio a `CANCELADA`.

---

## ## Cómo Verificar

Ejecutar la suite completa y generación de reportes desde la raíz del proyecto:

```bash
JAVA_HOME=/usr/lib/jvm/java-21-openjdk-21.0.10.0.7-1.el8.x86_64 mvn clean test
```

Verificar reporte de cobertura HTML generado en:
```text
target/site/jacoco/index.html
```

---

## ## Cobertura JaCoCo
- **Instrucciones totales del proyecto:** 90 % (125 / 138 instrucciones).
- **Ramas totales del proyecto:** 83 % (15 / 18 ramas).
- **`ReservaService`:** 100 % de cobertura de instrucciones y 100 % de ramas.
- **`Reserva` (Dominio):** 69 % de instrucciones y 50 % de ramas (hueco restante: ramas de validación en constructor para `id` nulo/en blanco no probado en aislamiento).
- **Interpretación:** La cobertura de líneas se complementó con la verificación rigurosa de efectos secundarios usando `verify()` y `verify(..., never())`, asegurando que el código no solo se ejecute, sino que cumpla fielmente con las reglas del negocio.

---

## ## Limitaciones
- La validación del constructor de `Reserva` (`id == null || id.isBlank()`) no cuenta con pruebas unitarias aisladas en una clase `ReservaTest` dedicada, ya que el alcance de la actividad se centró en `ReservaServiceTest`.
- No se realizan pruebas de concurrencia en la disponibilidad ni pruebas de integración con persistencia real (base de datos o API remota).

---

## ## Autorrevisión
- [x] Compila limpiamente sin advertencias ni errores.
- [x] 14 pruebas unitarias en verde (`BUILD SUCCESS`).
- [x] Sin archivos temporales o generados (`target/` excluido en `.gitignore`).
- [x] Commits semánticos y descriptivos con trazabilidad clara.
- [x] Documentación técnica y matrices completamente actualizadas.
- [x] Instrucciones de verificación claras y reproducibles.

---

## ## Uso de IA
- **Herramienta:** IBM Bob (asistente de ingeniería de software).
- **Alcance de uso:** Asistencia en la estructuración de la matriz de casos, redacción de pruebas JUnit 5 bajo formato `@Nested` y `@DisplayName`, generación de documentación técnica de dobles de prueba y síntesis del análisis de cobertura de JaCoCo.
