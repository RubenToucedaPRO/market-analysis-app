package com.market.analysis.unit.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
                .createdAt(java.time.LocalDateTime.parse("2024-06-01T12:00:00"))
                .build();
    }

    @Test
    @DisplayName("Should get all prohibited tickers")
    void testGetAllProhibitedTickers() {
        // Arrange
        ProhibitedTicker ticker1 = new ProhibitedTicker("AAPL", "Inappropriate content",
                java.time.LocalDateTime.parse("2024-06-01T12:00:00"));
        ProhibitedTicker ticker2 = new ProhibitedTicker("GOOGL", "Inappropriate content",
                java.time.LocalDateTime.parse("2024-06-01T12:00:00"));

        ProhibitedTickerDTO dto1 = ProhibitedTickerDTO.builder().ticker("AAPL").build();
        ProhibitedTickerDTO dto2 = ProhibitedTickerDTO.builder().ticker("GOOGL").build();

        List<ProhibitedTicker> tickers = Arrays.asList(ticker1, ticker2);
        when(prohibitedTickerRepository.findAll()).thenReturn(tickers);
        when(prohibitedTickerDTOMapper.toDTO(ticker1)).thenReturn(dto1);
        when(prohibitedTickerDTOMapper.toDTO(ticker2)).thenReturn(dto2);

        // Act
        List<ProhibitedTickerDTO> result = manageProhibitedTickerService.getAllProhibitedTickers();

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
        List<ProhibitedTickerDTO> result = manageProhibitedTickerService.getAllProhibitedTickers();

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
