package com.market.analysis.unit.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
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
import com.market.analysis.domain.model.PageResult;
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

    @Test
    @DisplayName("Should return paginated prohibited tickers")
    void shouldReturnPaginatedProhibitedTickers() {
        ProhibitedTicker domainTicker = ProhibitedTicker.builder().ticker("AAPL").reason("Inappropriate").build();
        ProhibitedTickerDTO dtoTicker = ProhibitedTickerDTO.builder().ticker("AAPL").reason("Inappropriate").build();
        PageResult<ProhibitedTicker> page = new PageResult<>(List.of(domainTicker), 0, 10, 1L, 1);
        when(prohibitedTickerRepository.findAll(0, 10)).thenReturn(page);
        when(prohibitedTickerDTOMapper.toDTO(domainTicker)).thenReturn(dtoTicker);

        PageResult<ProhibitedTickerDTO> result = manageProhibitedTickerService.getProhibitedTickers(0, 10);

        assertEquals(1, result.content().size());
        assertEquals("AAPL", result.content().get(0).getTicker());
        assertEquals(0, result.pageNumber());
        assertEquals(10, result.pageSize());
        assertEquals(1L, result.totalElements());
        assertEquals(1, result.totalPages());
    }

    @Test
    @DisplayName("Should return empty page when no prohibited tickers exist")
    void shouldReturnEmptyPageWhenNoTickersExist() {
        PageResult<ProhibitedTicker> page = new PageResult<>(List.of(), 0, 10, 0L, 0);
        when(prohibitedTickerRepository.findAll(0, 10)).thenReturn(page);

        PageResult<ProhibitedTickerDTO> result = manageProhibitedTickerService.getProhibitedTickers(0, 10);

        assertTrue(result.content().isEmpty());
    }

    @Test
    @DisplayName("Should return multiple pages of prohibited tickers")
    void shouldReturnMultiplePagesOfProhibitedTickers() {
        ProhibitedTicker d1 = ProhibitedTicker.builder().ticker("AAPL").build();
        ProhibitedTicker d2 = ProhibitedTicker.builder().ticker("GOOGL").build();
        ProhibitedTickerDTO dto1 = ProhibitedTickerDTO.builder().ticker("AAPL").build();
        ProhibitedTickerDTO dto2 = ProhibitedTickerDTO.builder().ticker("GOOGL").build();
        PageResult<ProhibitedTicker> page = new PageResult<>(List.of(d1, d2), 1, 10, 25L, 3);
        when(prohibitedTickerRepository.findAll(1, 10)).thenReturn(page);
        when(prohibitedTickerDTOMapper.toDTO(d1)).thenReturn(dto1);
        when(prohibitedTickerDTOMapper.toDTO(d2)).thenReturn(dto2);

        PageResult<ProhibitedTickerDTO> result = manageProhibitedTickerService.getProhibitedTickers(1, 10);

        assertEquals(2, result.content().size());
        assertEquals(1, result.pageNumber());
        assertEquals(25L, result.totalElements());
        assertEquals(3, result.totalPages());
    }
}
