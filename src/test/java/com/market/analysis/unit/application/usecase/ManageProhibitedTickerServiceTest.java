package com.market.analysis.unit.application.usecase;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.market.analysis.application.dto.ProhibitedTickerDTO;
import com.market.analysis.application.mapper.ProhibitedTickerDTOMapper;
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

    @Mock
    private ProhibitedTickerDTOMapper prohibitedTickerDTOMapper;

    @InjectMocks
    private ManageProhibitedTickerService manageProhibitedTickerService;

    private ProhibitedTickerDTO testProhibitedTickerDTO;

    @BeforeEach
    void setUp() {

        testProhibitedTickerDTO = ProhibitedTickerDTO.builder()
                .ticker("AAPL")
                .reason("Inappropriate content")
                .createdAt(Instant.now().minus(40, ChronoUnit.DAYS))
                .build();
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
        ProhibitedTicker domainTicker = ProhibitedTicker.builder().ticker("AAPL").build();
        when(prohibitedTickerDTOMapper.toDomain(testProhibitedTickerDTO)).thenReturn(domainTicker);

        // Act
        manageProhibitedTickerService.addProhibitedTicker(testProhibitedTickerDTO);

        // Assert
        verify(prohibitedTickerRepository, times(1)).save(domainTicker);
    }

    @Test
    @DisplayName("Should remove prohibited ticker successfully")
    void testRemoveProhibitedTicker() {
        // Arrange
        String ticker = "AAPL";

        // Act
        manageProhibitedTickerService.removeProhibitedTicker(ticker);

        // Assert
        verify(prohibitedTickerRepository, times(1)).deleteByTicker(ticker);
    }
}
