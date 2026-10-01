package edu.uees.testing.service;

import edu.uees.testing.availability.DisponibilidadClient;
import edu.uees.testing.domain.EstadoReserva;
import edu.uees.testing.domain.Reserva;
import edu.uees.testing.notification.Notificador;
import edu.uees.testing.repository.ReservaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Suite de pruebas unitarias para ReservaService y reglas de negocio asociadas.
 * Estructura AAA (Arrange - Act - Assert), pruebas de estado, excepciones e interacciones.
 */
@DisplayName("Suite de Pruebas Unitarias - ReservaService")
class ReservaServiceTest {

    private final ReservaService servicio = new ReservaService(null, null, null);

    @Test
    @DisplayName("CP-00: Verificación de entorno de ejecución JUnit 5")
    void entornoJUnitFunciona() {
        assertTrue(true);
    }

    @Nested
    @DisplayName("Regla 1: Políticas de Cancelación (puedeCancelar)")
    class PoliticasCancelacionTest {

        @Test
        @DisplayName("CP-01: Cinco horas de anticipación permiten cancelar la reserva")
        void cincoHorasPermitenCancelar() {
            // Arrange
            int horas = 5;

            // Act
            boolean resultado = servicio.puedeCancelar(horas);

            // Assert
            assertTrue(resultado, "Con 5 horas de anticipación debería ser posible cancelar");
        }

        @Test
        @DisplayName("CP-02: Dos horas de anticipación es el límite exacto permitido para cancelar")
        void dosHorasEsElLimitePermitido() {
            // Arrange
            int horas = 2;

            // Act
            boolean resultado = servicio.puedeCancelar(horas);

            // Assert
            assertTrue(resultado, "El límite exacto de 2 horas debe permitir la cancelación");
        }

        @Test
        @DisplayName("CP-03: Una hora de anticipación está por debajo del límite y no permite cancelar")
        void unaHoraNoPermiteCancelar() {
            // Arrange
            int horas = 1;

            // Act
            boolean resultado = servicio.puedeCancelar(horas);

            // Assert
            assertFalse(resultado, "Con 1 hora no debe permitirse la cancelación");
        }

        @Test
        @DisplayName("CP-04: Cero horas de anticipación no permite cancelar de forma inmediata")
        void ceroHorasNoPermiteCancelar() {
            // Arrange
            int horas = 0;

            // Act
            boolean resultado = servicio.puedeCancelar(horas);

            // Assert
            assertFalse(resultado, "Con 0 horas de antelación no se debe admitir cancelación");
        }
    }

    @Nested
    @DisplayName("Regla 2: Cálculo de Total y Descuentos (calcularTotal)")
    class CalculoDescuentosTest {

        @Test
        @DisplayName("CP-05: Cliente NORMAL no recibe descuento sobre el total base")
        void normalNoRecibeDescuento() {
            // Arrange & Act
            double total = servicio.calcularTotal("NORMAL", 100.0);

            // Assert
            assertEquals(100.0, total, 0.001, "Tarifa NORMAL debe conservar el 100% del total base");
        }

        @Test
        @DisplayName("CP-06: Cliente VIP recibe un 15% de descuento sobre el total base")
        void vipRecibeQuincePorCiento() {
            // Arrange & Act
            double total = servicio.calcularTotal("VIP", 100.0);

            // Assert
            assertEquals(85.0, total, 0.001, "Tarifa VIP debe aplicar un descuento del 15%");
        }

        @Test
        @DisplayName("CP-07: Cliente ESTUDIANTE recibe un 10% de descuento sobre el total base")
        void estudianteRecibeDiezPorCiento() {
            // Arrange & Act
            double total = servicio.calcularTotal("ESTUDIANTE", 100.0);

            // Assert
            assertEquals(90.0, total, 0.001, "Tarifa ESTUDIANTE debe aplicar un descuento del 10%");
        }

        @Test
        @DisplayName("CP-08: Cálculo de total con monto base cero devuelve cero")
        void totalBaseCeroDevuelveCero() {
            // Arrange & Act
            double total = servicio.calcularTotal("VIP", 0.0);

            // Assert
            assertEquals(0.0, total, 0.001, "Monto base cero debe calcularse como 0.0");
        }

        @Test
        @DisplayName("CP-09: Total base negativo lanza IllegalArgumentException")
        void totalNegativoEsInvalido() {
            // Arrange & Act
            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> servicio.calcularTotal("NORMAL", -1.0)
            );

            // Assert
            assertEquals("Total base inválido", ex.getMessage());
        }
    }

    @Nested
    @DisplayName("Regla 3: Confirmación de Reserva con Dobles de Prueba (confirmar)")
    class ConfirmacionReservaTest {

        @Test
        @DisplayName("CP-10: Reserva disponible se confirma, se persiste y se envía notificación")
        void reservaDisponibleSeConfirmaGuardaYNotifica() {
            // Arrange
            DisponibilidadClient disponibilidad = mock(DisponibilidadClient.class);
            ReservaRepository repository = mock(ReservaRepository.class);
            Notificador notificador = mock(Notificador.class);

            // Stub: simula disponibilidad exitosa
            when(disponibilidad.estaDisponible(any(Reserva.class))).thenReturn(true);

            ReservaService servicioConColaboradores = new ReservaService(disponibilidad, repository, notificador);
            Reserva reserva = new Reserva("R-001", "NORMAL");

            // Act
            servicioConColaboradores.confirmar(reserva);

            // Assert: verificación de estado del dominio
            assertEquals(EstadoReserva.CONFIRMADA, reserva.getEstado(), "La reserva debe quedar en estado CONFIRMADA");

            // Assert: verificación de interacciones con Mocks
            verify(disponibilidad).estaDisponible(reserva);
            verify(repository).guardar(reserva);
            verify(notificador).enviarConfirmacion(reserva);
        }

        @Test
        @DisplayName("CP-11: Reserva no disponible lanza IllegalStateException y no persiste ni notifica")
        void reservaNoDisponibleNoSeGuardaNiNotifica() {
            // Arrange
            DisponibilidadClient disponibilidad = mock(DisponibilidadClient.class);
            ReservaRepository repository = mock(ReservaRepository.class);
            Notificador notificador = mock(Notificador.class);

            // Stub: simula indisponibilidad de horario
            when(disponibilidad.estaDisponible(any(Reserva.class))).thenReturn(false);

            ReservaService servicioConColaboradores = new ReservaService(disponibilidad, repository, notificador);
            Reserva reserva = new Reserva("R-002", "NORMAL");

            // Act & Assert
            IllegalStateException ex = assertThrows(
                    IllegalStateException.class,
                    () -> servicioConColaboradores.confirmar(reserva)
            );
            assertEquals("Horario no disponible", ex.getMessage());

            // Assert: verificar que NUNCA se persistió ni notificó
            verify(repository, never()).guardar(any());
            verify(notificador, never()).enviarConfirmacion(any());
        }

        @Test
        @DisplayName("CP-12: Reserva nula lanza IllegalArgumentException sin invocar colaboradores")
        void reservaNulaNoConsultaDependencias() {
            // Arrange
            DisponibilidadClient disponibilidad = mock(DisponibilidadClient.class);
            ReservaRepository repository = mock(ReservaRepository.class);
            Notificador notificador = mock(Notificador.class);

            ReservaService servicioConColaboradores = new ReservaService(disponibilidad, repository, notificador);

            // Act & Assert
            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> servicioConColaboradores.confirmar(null)
            );
            assertEquals("Reserva obligatoria", ex.getMessage());

            // Assert: verificar que ningún colaborador fue invocado
            verify(disponibilidad, never()).estaDisponible(any());
            verify(repository, never()).guardar(any());
            verify(notificador, never()).enviarConfirmacion(any());
        }
    }

    @Nested
    @DisplayName("Regla 4: Ciclo de Vida y Transición de Estados en Dominio (Reserva)")
    class DominioReservaTest {

        @Test
        @DisplayName("CP-13: Cancelar reserva cambia su estado a CANCELADA")
        void cancelarReservaCambiaEstadoACancelada() {
            // Arrange
            Reserva reserva = new Reserva("R-003", "NORMAL");

            // Act
            reserva.cancelar();

            // Assert
            assertEquals(EstadoReserva.CANCELADA, reserva.getEstado(), "El estado de la reserva debe ser CANCELADA");
        }
    }
}
