package com.market.analysis.unit.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.market.analysis.application.usecase.ManageProhibitedTickerService;
import com.market.analysis.domain.model.ProhibitedTicker;
import com.market.analysis.domain.port.out.ProhibitedTickerRepository;

/**
 * Unit tests for ManageProhibitedTickerService.
 */
@DisplayName("ManageProhibitedTickerService Unit Tests")
@ExtendWith(MockitoExtension.class)
class ManageProhibitedTickerServiceTest {

    @Mock
    private ProhibitedTickerRepository prohibitedTickerRepository;

    @InjectMocks
    private ManageProhibitedTickerService manageProhibitedTickerService;

    private ProhibitedTicker testProhibitedTicker;

    @BeforeEach
    void setUp() {
        testProhibitedTicker = new ProhibitedTicker("AAPL");
    }

    @Test
    @DisplayName("Should get all prohibited tickers")
    void testGetAllProhibitedTickers() {
        // Arrange
        ProhibitedTicker ticker1 = new ProhibitedTicker("AAPL");
        ProhibitedTicker ticker2 = new ProhibitedTicker("GOOGL");
        List<ProhibitedTicker> tickers = Arrays.asList(ticker1, ticker2);
        when(prohibitedTickerRepository.findAll()).thenReturn(tickers);

        // Act
        List<ProhibitedTicker> result = manageProhibitedTickerService.getAllProhibitedTickers();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("AAPL", result.get(0).getTicker());
        assertEquals("GOOGL", result.get(1).getTicker());
        verify(prohibitedTickerRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should return empty list when no prohibited tickers exist")
    void testGetAllProhibitedTickersEmpty() {
        // Arrange
        when(prohibitedTickerRepository.findAll()).thenReturn(Arrays.asList());

        // Act
        List<ProhibitedTicker> result = manageProhibitedTickerService.getAllProhibitedTickers();

        // Assert
        assertNotNull(result);
        assertEquals(0, result.size());
        verify(prohibitedTickerRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should return true when ticker is prohibited")
    void testIsTickerProhibited() {
        // Arrange
        when(prohibitedTickerRepository.existsByTicker("AAPL")).thenReturn(true);

        // Act
        boolean result = manageProhibitedTickerService.isTickerProhibited("AAPL");

        // Assert
        assertTrue(result);
        verify(prohibitedTickerRepository, times(1)).existsByTicker("AAPL");
    }

    @Test
    @DisplayName("Should return false when ticker is not prohibited")
    void testIsTickerNotProhibited() {
        // Arrange
        when(prohibitedTickerRepository.existsByTicker("MSFT")).thenReturn(false);

        // Act
        boolean result = manageProhibitedTickerService.isTickerProhibited("MSFT");

        // Assert
        assertFalse(result);
        verify(prohibitedTickerRepository, times(1)).existsByTicker("MSFT");
    }

    @Test
    @DisplayName("Should add prohibited ticker successfully")
    void testAddProhibitedTicker() {
        // Arrange
        when(prohibitedTickerRepository.save(any(ProhibitedTicker.class))).thenReturn(testProhibitedTicker);

        // Act
        manageProhibitedTickerService.addProhibitedTicker(testProhibitedTicker);

        // Assert
        verify(prohibitedTickerRepository, times(1)).save(testProhibitedTicker);
    }

    @Test
    @DisplayName("Should remove prohibited ticker successfully")
    void testRemoveProhibitedTicker() {
        // Arrange
        Long tickerId = 1L;

        // Act
        manageProhibitedTickerService.removeProhibitedTicker(tickerId);

        // Assert
        verify(prohibitedTickerRepository, times(1)).deleteById(tickerId);
    }
}
