package edu.uees.testing.service;

import edu.uees.testing.availability.DisponibilidadClient;
import edu.uees.testing.domain.EstadoReserva;
import edu.uees.testing.domain.Reserva;
import edu.uees.testing.notification.Notificador;
import edu.uees.testing.repository.ReservaRepository;
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
 * Suite de pruebas unitarias para ReservaService.
 * Implementa la estructura AAA (Arrange - Act - Assert).
 */
class ReservaServiceTest {

    private final ReservaService servicio = new ReservaService(null, null, null);

    @Test
    void entornoJUnitFunciona() {
        assertTrue(true);
    }

    // 4.1 Caso normal
    @Test
    void cincoHorasPermitenCancelar() {
        // Arrange
        int horas = 5;

        // Act
        boolean resultado = servicio.puedeCancelar(horas);

        // Assert
        assertTrue(resultado);
    }

    // 4.2 Caso límite
    @Test
    void dosHorasEsElLimitePermitido() {
        // Arrange & Act & Assert
        assertTrue(servicio.puedeCancelar(2));
    }

    @Test
    void unaHoraNoPermiteCancelar() {
        // Arrange & Act & Assert
        assertFalse(servicio.puedeCancelar(1));
    }

    // 4.3 Excepción
    @Test
    void totalNegativoEsInvalido() {
        // Arrange & Act
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> servicio.calcularTotal("NORMAL", -1)
        );

        // Assert
        assertEquals("Total base inválido", ex.getMessage());
    }

    // 5. Pruebas de descuento
    @Test
    void vipRecibeQuincePorCiento() {
        assertEquals(
                85.0,
                servicio.calcularTotal("VIP", 100),
                0.001
        );
    }

    @Test
    void estudianteRecibeDiezPorCiento() {
        assertEquals(
                90.0,
                servicio.calcularTotal(
                        "ESTUDIANTE",
                        100
                ),
                0.001
        );
    }

    @Test
    void normalNoRecibeDescuento() {
        assertEquals(
                100.0,
                servicio.calcularTotal(
                        "NORMAL",
                        100
                ),
                0.001
        );
    }
    // --- Casos con Stub y Mock para confirmar() ---

    // Caso 1 | Reserva disponible
    @Test
    void reservaDisponibleSeConfirmaGuardaYNotifica() {
        // Arrange
        DisponibilidadClient disponibilidad = mock(DisponibilidadClient.class);
        ReservaRepository repository = mock(ReservaRepository.class);
        Notificador notificador = mock(Notificador.class);

        // Stub: controla la respuesta de disponibilidad → siempre disponible
        when(disponibilidad.estaDisponible(any())).thenReturn(true);

        ReservaService servicio = new ReservaService(disponibilidad, repository, notificador);
        Reserva reserva = new Reserva("R-001", "NORMAL");

        // Act
        servicio.confirmar(reserva);

        // Assert: estado de dominio correcto
        assertEquals(EstadoReserva.CONFIRMADA, reserva.getEstado());
        // Mock: verifica que las interacciones esperadas ocurrieron
        verify(repository).guardar(reserva);
        verify(notificador).enviarConfirmacion(reserva);
    }

    // Caso 2 | Sin disponibilidad
    @Test
    void reservaNoDisponibleNoSeGuardaNiNotifica() {
        // Arrange
        DisponibilidadClient disponibilidad = mock(DisponibilidadClient.class);
        ReservaRepository repository = mock(ReservaRepository.class);
        Notificador notificador = mock(Notificador.class);

        // Stub: simula horario no disponible
        when(disponibilidad.estaDisponible(any())).thenReturn(false);

        ReservaService servicio = new ReservaService(disponibilidad, repository, notificador);
        Reserva reserva = new Reserva("R-002", "NORMAL");

        // Act & Assert
        assertThrows(
                IllegalStateException.class,
                () -> servicio.confirmar(reserva)
        );
        // Mock: verifica que nunca se persistió ni notificó
        verify(repository, never()).guardar(any());
        verify(notificador, never()).enviarConfirmacion(any());
    }

    // Caso 3 | Reserva nula
    @Test
    void reservaNulaNoConsultaDependencias() {
        // Arrange
        DisponibilidadClient disponibilidad = mock(DisponibilidadClient.class);
        ReservaRepository repository = mock(ReservaRepository.class);
        Notificador notificador = mock(Notificador.class);

        ReservaService servicio = new ReservaService(disponibilidad, repository, notificador);

        // Act & Assert
        assertThrows(
                IllegalArgumentException.class,
                () -> servicio.confirmar(null)
        );
        // Mock: ningún colaborador debe ser invocado cuando la reserva es nula
        verify(disponibilidad, never()).estaDisponible(any());
        verify(repository, never()).guardar(any());
        verify(notificador, never()).enviarConfirmacion(any());
    }

    // --- Prueba añadida a partir del reporte JaCoCo ---

    // Hueco detectado: Reserva.cancelar() nunca ejercitado → estado CANCELADA no alcanzado
    @Test
    void cancelarReservaCambiaEstadoACancelada() {
        // Arrange
        Reserva reserva = new Reserva("R-003", "NORMAL");

        // Act
        reserva.cancelar();

        // Assert
        assertEquals(EstadoReserva.CANCELADA, reserva.getEstado());
    }
}
