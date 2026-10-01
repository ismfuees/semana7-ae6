# UEES UCOM0310 — Diseño de Software
## Semana 7 | AE6 — Suite de Pruebas, Cobertura y Pull Request Documentado

| Campo | Detalle |
|---|---|
| **Universidad** | Universidad Espíritu Santo |
| **Asignatura** | Diseño de Software — UCOM0310 |
| **Período** | PEL 4 – 2026 · Semana 7 |
| **Estudiante** | Iván Stalyn Muela Flor |
| **Docente** | Ph.D. Jaime Paul Sayago Heredia |

---

# Reporte Técnico Ae6

## 1. Objetivo de Ae6
El objetivo de la actividad evaluativa integradora Ae6 es consolidar las competencias de diseño y automatización de pruebas unitarias en Java, aplicando:
- Estructuración rigurosa de pruebas unitarias bajo el patrón **AAA** (Arrange, Act, Assert) con **JUnit 5**.
- Aislamiento controlado de colaboradores externos mediante **dobles de prueba** (Stubs y Mocks con **Mockito**).
- Medición, análisis e interpretación técnica de la cobertura de código (instrucciones y ramas) con **JaCoCo**.
- Trazabilidad del ciclo de vida de desarrollo empleando ramas y commits semánticos en **Git**, culminando en un **Pull Request** documentado y autorrevisado.

---

## 2. Reglas de Negocio y Matriz de Casos

### 2.1 Reglas de Negocio Analizadas
1. **Políticas de Cancelación (`puedeCancelar`):**
   - Se permite cancelar una reserva únicamente si la anticipación es mayor o igual a 2 horas (`horasAnticipacion >= 2`).
2. **Cálculo de Descuentos (`calcularTotal`):**
   - El monto base no puede ser negativo (`totalBase < 0` lanza `IllegalArgumentException`).
   - Categoría `VIP`: 15% de descuento (`totalBase * 0.85`).
   - Categoría `ESTUDIANTE`: 10% de descuento (`totalBase * 0.90`).
   - Categoría `NORMAL` u otras: precio total sin descuento (`totalBase`).
3. **Confirmación de Reserva (`confirmar`):**
   - Si la reserva es `null`, se rechaza con `IllegalArgumentException` inmediatamente (*fail-fast*).
   - Se consulta la disponibilidad mediante `DisponibilidadClient`. Si no está disponible, se aborta con `IllegalStateException` sin persistir ni notificar.
   - Si está disponible, la reserva cambia a estado `CONFIRMADA`, se persiste en `ReservaRepository` y se despacha la confirmación mediante `Notificador`.
4. **Ciclo de Vida de Dominio (`Reserva`):**
   - Una reserva puede ser cancelada mediante el método `cancelar()`, transitando su estado interno a `CANCELADA`.

### 2.2 Matriz de Casos de Prueba (13 Casos)

| ID | Método / Regla | Escenario | Entrada | Esperado | Tipo | Riesgo / Justificación |
|---|---|---|---|---|---|---|
| **CP-01** | `puedeCancelar` | Anticipación habitual | `horasAnticipacion = 5` | `true` | Normal | Valida la regla general de cancelación con antelación suficiente. |
| **CP-02** | `puedeCancelar` | Límite exacto permitido | `horasAnticipacion = 2` | `true` | Límite | Detecta regresiones por operador estricto (`>`) en vez de inclusión (`>=`). |
| **CP-03** | `puedeCancelar` | Justo bajo el límite | `horasAnticipacion = 1` | `false` | Límite | Protege la frontera inferior no permitida (`< 2`). |
| **CP-04** | `puedeCancelar` | Sin anticipación | `horasAnticipacion = 0` | `false` | Extremo | Garantiza que no se admita cancelación inmediata sin aviso previo. |
| **CP-05** | `calcularTotal` | Tarifa NORMAL | `tipo = "NORMAL"`, `totalBase = 100.0` | `100.0` | Normal | Verifica que no se apliquen descuentos indebidos a clientes estándar. |
| **CP-06** | `calcularTotal` | Cliente VIP (15%) | `tipo = "VIP"`, `totalBase = 100.0` | `85.0` | Alternativo | Valida el cálculo del 15% de descuento para clientes preferenciales. |
| **CP-07** | `calcularTotal` | Cliente ESTUDIANTE (10%) | `tipo = "ESTUDIANTE"`, `totalBase = 100.0` | `90.0` | Alternativo | Valida el cálculo del 10% de descuento educativo. |
| **CP-08** | `calcularTotal` | Monto base cero | `tipo = "VIP"`, `totalBase = 0.0` | `0.0` | Límite | Valida la frontera inferior monetaria válida. |
| **CP-09** | `calcularTotal` | Total base negativo | `tipo = "NORMAL"`, `totalBase = -1.0` | `IllegalArgumentException` | Inválido | Detecta y rechaza valores monetarios negativos o incongruentes. |
| **CP-10** | `confirmar` | Reserva disponible | `Reserva("R-001", "NORMAL")`, Disp = `true` | Estado `CONFIRMADA`, guarda y notifica | Normal (Stub/Mock) | Verifica el flujo feliz completo: mutación de dominio, persistencia y despacho. |
| **CP-11** | `confirmar` | Horario no disponible | `Reserva("R-002", "NORMAL")`, Disp = `false` | `IllegalStateException`, NO guarda, NO notifica | Alternativo (Stub/Mock) | Protege contra sobreventa (*overbooking*) y notificaciones erróneas. |
| **CP-12** | `confirmar` | Reserva nula | `reserva = null` | `IllegalArgumentException`, NO interactúa | Inválido (Mock) | Comprueba *fail-fast* inicial sin consumo de dependencias externas. |
| **CP-13** | `cancelar` | Cancelar reserva | `Reserva("R-003", "NORMAL")`, `cancelar()` | Estado `CANCELADA` | Dominio / JaCoCo | Ejercita la transición de estado descubierta como hueco en el reporte JaCoCo. |

---

## 3. Implementación JUnit 5 y Patrón AAA
La suite de pruebas fue estructurada en [`ReservaServiceTest.java`](src/test/java/edu/uees/testing/service/ReservaServiceTest.java) aplicando:
- **Separación AAA Explícita:** Cada método delimita la preparación del contexto (**Arrange**), la invocación del método bajo prueba (**Act**) y las aserciones (**Assert**).
- **Anotaciones Semánticas `@Nested` y `@DisplayName`:** Organización de las pruebas por áreas de negocio:
  - `PoliticasCancelacionTest`
  - `CalculoDescuentosTest`
  - `ConfirmacionReservaTest`
  - `DominioReservaTest`
- **Aserciones Robustas:** `assertEquals`, `assertTrue`, `assertFalse` y `assertThrows` con verificación del mensaje de error exacto.

---

## 4. Dobles de Prueba (Stub / Mock) Utilizados y Justificación

Para aislar unitariamente el método `confirmar()` se empleó **Mockito**:

```
+-------------------------------------------------------------+
|                      ReservaService                         |
|                                                             |
|   confirmar(reserva)                                        |
|         |                                                   |
|         +---> DisponibilidadClient (STUB) ---> true / false  |
|         |                                                   |
|         +---> Reserva.confirmar()                           |
|         |                                                   |
|         +---> ReservaRepository   (MOCK) ---> verify guardar|
|         |                                                   |
|         +---> Notificador         (MOCK) ---> verify enviar |
+-------------------------------------------------------------+
```

| Colaborador | Doble Empleado | Justificación Técnica |
|---|---|---|
| `DisponibilidadClient` | **Stub (Indirect Input)** | Permite controlar de forma determinista la respuesta del servicio de disponibilidad (`when(...).thenReturn(true/false)`) sin depender de llamadas remotas ni de latencia de red. |
| `ReservaRepository` | **Mock (Indirect Output)** | Permite verificar (`verify(repository).guardar(reserva)`) que el registro sólo se guarde en caso de éxito. En caso de indisponibilidad o fallo, se asegura que jamás se invoque (`verify(..., never()).guardar(any())`). |
| `Notificador` | **Mock (Indirect Output)** | Permite comprobar (`verify(notificador).enviarConfirmacion(reserva)`) que la notificación al cliente se envíe únicamente cuando la reserva ha sido confirmada y persistida, evitando falsas confirmaciones. |

---

## 5. Resultado de Ejecución Limpia

### Comando de Verificación:
```bash
JAVA_HOME=/usr/lib/jvm/java-21-openjdk-21.0.10.0.7-1.el8.x86_64 mvn clean test
```

### Evidencia de Ejecución:
```text
[INFO] Scanning for projects...
[INFO] ------------------------< edu.uees:semana7-ae6 >------------------------
[INFO] Building semana7-ae6 1.0.0
[INFO] --------------------------------[ jar ]---------------------------------
[INFO] --- maven-clean-plugin:2.5:clean (default-clean) @ semana7-ae6 ---
[INFO] --- jacoco-maven-plugin:0.8.12:prepare-agent (default) @ semana7-ae6 ---
[INFO] --- maven-compiler-plugin:3.1:compile (default-compile) @ semana7-ae6 ---
[INFO] Compiling 6 source files to target/classes
[INFO] --- maven-compiler-plugin:3.1:testCompile (default-testCompile) @ semana7-ae6 ---
[INFO] Compiling 1 source file to target/test-classes
[INFO] --- maven-surefire-plugin:3.2.5:test (default-test) @ semana7-ae6 ---
[INFO] Running edu.uees.testing.service.ReservaServiceTest
[INFO] Running edu.uees.testing.service.ReservaServiceTest$DominioReservaTest
[INFO] Tests run: 2, Failures: 0, Errors: 0, Skipped: 0
[INFO] Running edu.uees.testing.service.ReservaServiceTest$ConfirmacionReservaTest
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0
[INFO] Running edu.uees.testing.service.ReservaServiceTest$CalculoDescuentosTest
[INFO] Tests run: 5, Failures: 0, Errors: 0, Skipped: 0
[INFO] Running edu.uees.testing.service.ReservaServiceTest$PoliticasCancelacionTest
[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0
[INFO] Results:
[INFO] Tests run: 14, Failures: 0, Errors: 0, Skipped: 0
[INFO] --- jacoco-maven-plugin:0.8.12:report (report) @ semana7-ae6 ---
[INFO] Loading execution data file target/jacoco.exec
[INFO] Analyzed bundle 'semana7-ae6' with 3 classes
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
```

---

## 6. Cobertura JaCoCo e Interpretación

### 6.1 Métricas Obtenidas (`target/site/jacoco/index.html`)

| Elemento | Instrucciones | Cobertura Inst. | Ramas | Cobertura Ramas | Líneas |
|---|---|---|---|---|---|
| **Total Proyecto** | **125 / 138** | **90 %** | **15 / 18** | **83 %** | **25 / 28 (89 %)** |
| `ReservaService` | 74 / 74 | **100 %** | 12 / 12 | **100 %** | 15 / 15 (100 %) |
| `Reserva` (Dominio) | 30 / 43 | **69 %** | 3 / 6 | **50 %** | 9 / 12 (75 %) |
| `EstadoReserva` | 21 / 21 | **100 %** | 0 / 0 | **100 %** | 1 / 1 (100 %) |

### 6.2 Análisis de Huecos y Pruebas Derivadas
1. **Hueco Identificado y Corregido:** En la suite inicial de los laboratorios, el método `cancelar()` de la clase `Reserva` nunca era ejecutado. Se implementó la prueba `CP-13` (`cancelarReservaCambiaEstadoACancelada()`), garantizando la mutación al estado `CANCELADA`.
2. **Hueco Remanente Documentado:** En el constructor de `Reserva`, las ramas de validación para `id == null || id.isBlank()` y el fallback ternario `tipo == null` no se evalúan de forma exhaustiva ya que no se diseñaron pruebas directas para el constructor de la entidad.

### 6.3 ¿Por qué una cobertura alta no garantiza corrección?
- **Ejecución vs. Verificación:** JaCoCo registra qué líneas de bytecode se ejecutaron, pero no evalúa la presencia ni calidad de los `assert`. Un método sin aserciones puede tener 100% de cobertura sin validar nada.
- **Efectos Secundarios:** La cobertura de líneas no comprueba si se llamó a un repositorio o a un servicio de mensajería; para ello es imprescindible verificar interacciones mediante Mocks.
- **Lógica No Implementada:** JaCoCo no puede alertar sobre casos de negocio o validaciones que fueron omitidas en el código fuente.

---

## 7. Flujo Git y Trazabilidad de Commits

El trabajo se desarrolló en la rama específica `ae6/suite-pruebas` derivada de `main`, aplicando commits atómicos y semánticos:

```text
* 534cdf0 (HEAD -> ae6/suite-pruebas) docs: completar plantilla y documentacion de pull request para ae6
* f7d09e7 docs: analizar e interpretar reporte de cobertura jacoco
* 714bb72 test: completar suite junit 5 con dobles stub y mock para confirmacion
* 40a3acb docs: definir matriz de casos de prueba para Ae6
* 29d21c4 (origin/main, main) test(service): implementar pruebas unitarias para reglas de descuento en ReservaServiceTest (Paso 5)
* 941ae21 cambio de nombre del proyecto en pom.xml
* 133cacd feat(testing): diseño de matriz de casos y suite inicial JUnit 5 (Pasos 3 y 4)
* 6aaa0ec Inicial
```

---

## 8. Pull Request y Autorrevisión

- **Rama Base:** `main`
- **Rama Comparada:** `ae6/suite-pruebas`
- **Título:** `test: completar suite, cobertura y evidencia de Ae6`
- **Documentación:** Registrada en [`docs/03_PULL_REQUEST_PLANTILLA.md`](docs/03_PULL_REQUEST_PLANTILLA.md).

### Checklist de Autorrevisión:
- [x] Repositorio limpio, compilación limpia (`mvn clean test`).
- [x] Directorio `target/` excluido en `.gitignore`.
- [x] Nombres de pruebas descriptivos con `@DisplayName` y estructura AAA.
- [x] Casos normales, de frontera, inválidos e interacciones cubiertos.
- [x] Mocks y Stubs utilizados con justificación técnica.
- [x] Análisis e interpretación de JaCoCo completado.

---

## 9. Conclusiones, Limitaciones y Trabajo Futuro

### Conclusiones:
1. La combinación de la matriz de casos y el patrón AAA permite transformar requerimientos de negocio en pruebas reproducibles, expresivas y mantenibles.
2. El uso de Stubs y Mocks desacopla el componente de lógica de negocio de la infraestructura externa, permitiendo validar tanto mutaciones de estado como interacciones críticas (persistencia y notificaciones) de forma determinista y veloz.
3. El análisis de cobertura con JaCoCo es una herramienta diagnóstica para descubrir código no ejercitado, pero debe complementarse siempre con aserciones rigurosas de comportamiento.

### Limitaciones:
- La suite actual valida principalmente la capa de servicio (`ReservaService`); las validaciones del constructor de `Reserva` (`id` en blanco o nulo) no cuentan con una clase de prueba unitaria dedicada.
- No se evalúan escenarios de concurrencia ni persistencia real en base de datos.

### Trabajo Futuro:
- Crear `ReservaTest` para validar exhaustivamente el constructor y encapsulamiento de la entidad de dominio.
- Implementar pruebas de integración con `@SpringBootTest` / `Testcontainers` para verificar la base de datos real y brokers de mensajería.

---

## 10. Declaración de Uso de Inteligencia Artificial
- **Herramienta:** IBM Bob (asistente de ingeniería de software).
- **Finalidad y Alcance:** Asistencia técnica en la estructuración de la matriz de casos, diseño de la jerarquía de pruebas con anotaciones JUnit 5 (`@Nested` / `@DisplayName`), redacción de la justificación técnica de dobles de prueba y síntesis del reporte técnico.
