package com.market.analysis.unit.infrastructure.external.finnhub;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClient;

import com.market.analysis.domain.model.CompanyProfile;
import com.market.analysis.domain.model.Stock;
import com.market.analysis.infrastructure.exception.FinnhubException;
import com.market.analysis.infrastructure.external.finnhub.FinnhubAdapter;
import com.market.analysis.infrastructure.external.finnhub.FinnhubMapper;
import com.market.analysis.infrastructure.external.finnhub.dto.CompanyData;
import com.market.analysis.infrastructure.external.finnhub.dto.QuoteData;

@ExtendWith(MockitoExtension.class)
class FinnhubAdapterTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    RestClient restClient;

    @Mock
    FinnhubMapper mapper;

    FinnhubAdapter adapter;

    @BeforeEach
    void setup() {
        adapter = new FinnhubAdapter(restClient, mapper);
        ReflectionTestUtils.setField(adapter, "apiToken", "test-token");
    }

    @Test
    void shouldReturnStockWhenQuoteValid() {
        QuoteData dto = QuoteData.builder()
                .symbol("AAPL")
                .c(BigDecimal.valueOf(100.0))
                .d(1.5)
                .dp(BigDecimal.valueOf(1.5))
                .h(BigDecimal.valueOf(150.0))
                .l(BigDecimal.valueOf(90.0))
                .o(BigDecimal.valueOf(95.0))
                .pc(BigDecimal.valueOf(98.0))
                .t(System.currentTimeMillis())
                .build();

        Stock expected = Stock.builder()
                .ticker("AAPL")
                .currentPrice(BigDecimal.valueOf(100.0))
                .highOfDay(BigDecimal.valueOf(150.0))
                .lowOfDay(BigDecimal.valueOf(90.0))
                .openPrice(BigDecimal.valueOf(95.0))
                .previousClose(BigDecimal.valueOf(98.0))
                .lastUpdated(LocalDateTime.ofEpochSecond(dto.getT() / 1000, 0, java.time.ZoneOffset.UTC))
                .build();

        when(restClient.get()
                .uri(org.mockito.ArgumentMatchers.<java.util.function.Function<org.springframework.web.util.UriBuilder, java.net.URI>>any())
                .retrieve()
                .onStatus(any(), any())
                .body(QuoteData.class))
                .thenReturn(dto);

        when(mapper.toDomain(dto)).thenReturn(expected);

        Stock result = adapter.getQuote("AAPL");

        assertEquals(expected, result);
    }

    @Test
    void shouldThrowFinnhubExceptionWhenQuoteIsNull() {
        when(restClient.get()
                .uri(org.mockito.ArgumentMatchers.<java.util.function.Function<org.springframework.web.util.UriBuilder, java.net.URI>>any())
                .retrieve()
                .onStatus(any(), any())
                .body(QuoteData.class))
                .thenReturn(null);

        FinnhubException exception = assertThrows(FinnhubException.class, () -> {
            adapter.getQuote("INVALID");
        });

        assertEquals("Error fetching quote for INVALID: No valid data found for: INVALID", exception.getMessage());
    }

    @Test
    void shouldThrowFinnhubExceptionWhenQuoteIsInvalid() {
        QuoteData invalidDto = QuoteData.builder()
                .symbol("INVALID")
                .c(null)
                .h(null)
                .l(null)
                .o(null)
                .pc(null)
                .t(0L)
                .build();

        when(restClient.get()
                .uri(org.mockito.ArgumentMatchers.<java.util.function.Function<org.springframework.web.util.UriBuilder, java.net.URI>>any())
                .retrieve()
                .onStatus(any(), any())
                .body(QuoteData.class))
                .thenReturn(invalidDto);

        FinnhubException exception = assertThrows(FinnhubException.class, () -> {
            adapter.getQuote("INVALID");
        });

        assertEquals("Error fetching quote for INVALID: No valid data found for: INVALID", exception.getMessage());
    }

    @Test
    void shouldReturnCompanyProfileWhenValid() {
        CompanyData companyData = CompanyData.builder()
                .ticker("AAPL")
                .name("Apple Inc.")
                .country("US")
                .exchange("NASDAQ")
                .finnhubIndustry("Technology")
                .marketCapitalization(20000000.0)
                .build();

        CompanyProfile expected = CompanyProfile.builder()
                .ticker("AAPL")
                .name("Apple Inc.")
                .country("US")
                .exchange("NASDAQ")
                .industry("Technology")
                .marketCapitalization(20000000.0)
                .build();

        when(restClient.get()
                .uri(org.mockito.ArgumentMatchers.<java.util.function.Function<org.springframework.web.util.UriBuilder, java.net.URI>>any())
                .retrieve()
                .onStatus(any(), any())
                .body(CompanyData.class))
                .thenReturn(companyData);

        when(mapper.toDomain(companyData)).thenReturn(expected);

        CompanyProfile result = adapter.getCompanyProfile("AAPL");

        assertNotNull(result);
        assertEquals(expected, result);
        assertEquals("Apple Inc.", result.getName());
    }

    @Test
    void shouldReturnNullWhenCompanyProfileNotFound() {
        when(restClient.get()
                .uri(org.mockito.ArgumentMatchers.<java.util.function.Function<org.springframework.web.util.UriBuilder, java.net.URI>>any())
                .retrieve()
                .onStatus(any(), any())
                .body(CompanyData.class))
                .thenReturn(null);

        CompanyProfile result = adapter.getCompanyProfile("NONEXISTENT");

        assertNull(result);
    }

    @Test
    void shouldReturnNullWhenCompanyProfileIsInvalid() {
        CompanyData invalidProfile = CompanyData.builder()
                .ticker("INVALID")
                .name(null)
                .country(null)
                .build();

        when(restClient.get()
                .uri(org.mockito.ArgumentMatchers.<java.util.function.Function<org.springframework.web.util.UriBuilder, java.net.URI>>any())
                .retrieve()
                .onStatus(any(), any())
                .body(CompanyData.class))
                .thenReturn(invalidProfile);

        CompanyProfile result = adapter.getCompanyProfile("INVALID");

        assertNull(result);
    }

    @Test
    void shouldReturnFalseForUpComingEarnings() {
        boolean result = adapter.hasUpComingEarnings("AAPL");

        assertFalse(result);
    }
}
