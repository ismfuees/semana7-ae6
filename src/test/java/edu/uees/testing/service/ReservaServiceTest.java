package edu.uees.testing.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
}
