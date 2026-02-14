package com.market.analysis.unit.presentation.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;

import com.market.analysis.domain.exception.RuleDefinitionNotFoundException;
import com.market.analysis.domain.exception.StockDataNotFoundException;
import com.market.analysis.infrastructure.exception.FinnhubException;
import com.market.analysis.infrastructure.exception.PolygonException;
import com.market.analysis.infrastructure.exception.StockException;
import com.market.analysis.presentation.exception.GlobalExceptionHandler;

/**
 * Unit tests for GlobalExceptionHandler.
 * Tests the centralized exception handling mechanism for the application.
 */
@DisplayName("GlobalExceptionHandler Unit Tests")
@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @Mock
    private Model model;

    @InjectMocks
    private GlobalExceptionHandler globalExceptionHandler;

    private static final String ERROR_VIEW = "error";
    private static final String ATTR_ERROR_MESSAGE = "errorMessage";
    private static final String ATTR_ERROR_DETAILS = "errorDetails";
    private static final String ATTR_ERROR_TYPE = "errorType";

    @BeforeEach
    void setUp() {
        // No additional setup needed
    }

    @Test
    @DisplayName("Should handle RuleDefinitionNotFoundException correctly")
    void testHandleRuleDefinitionNotFoundException() {
        // Arrange
        String errorMessage = "Rule definition with id 123 not found";
        RuleDefinitionNotFoundException exception = new RuleDefinitionNotFoundException(errorMessage);

        // Act
        String viewName = globalExceptionHandler.handleRuleDefinitionNotFoundException(exception, model);

        // Assert
        assertEquals(ERROR_VIEW, viewName);
        verify(model, times(1)).addAttribute(ATTR_ERROR_TYPE, "Rule Definition Not Found");
        verify(model, times(1)).addAttribute(ATTR_ERROR_MESSAGE, "The requested rule definition could not be found.");
        verify(model, times(1)).addAttribute(ATTR_ERROR_DETAILS, errorMessage);
    }

    @Test
    @DisplayName("Should handle StockDataNotFoundException correctly")
    void testHandleStockDataNotFoundException() {
        // Arrange
        String errorMessage = "Stock data for AAPL not found";
        StockDataNotFoundException exception = new StockDataNotFoundException(errorMessage);

        // Act
        String viewName = globalExceptionHandler.handleStockDataNotFoundException(exception, model);

        // Assert
        assertEquals(ERROR_VIEW, viewName);
        verify(model, times(1)).addAttribute(ATTR_ERROR_TYPE, "Stock Data Not Found");
        verify(model, times(1)).addAttribute(ATTR_ERROR_MESSAGE, "The requested stock data could not be found.");
        verify(model, times(1)).addAttribute(ATTR_ERROR_DETAILS, errorMessage);
    }

    @Test
    @DisplayName("Should handle StockException correctly")
    void testHandleStockException() {
        // Arrange
        String errorMessage = "Error processing stock data";
        StockException exception = new StockException(errorMessage);

        // Act
        String viewName = globalExceptionHandler.handleStockException(exception, model);

        // Assert
        assertEquals(ERROR_VIEW, viewName);
        verify(model, times(1)).addAttribute(ATTR_ERROR_TYPE, "Stock Processing Error");
        verify(model, times(1)).addAttribute(ATTR_ERROR_MESSAGE, "An error occurred while processing stock data.");
        verify(model, times(1)).addAttribute(ATTR_ERROR_DETAILS, errorMessage);
    }

    @Test
    @DisplayName("Should handle FinnhubException correctly")
    void testHandleFinnhubException() {
        // Arrange
        String errorMessage = "Finnhub API returned 503";
        FinnhubException exception = new FinnhubException(errorMessage);

        // Act
        String viewName = globalExceptionHandler.handleFinnhubException(exception, model);

        // Assert
        assertEquals(ERROR_VIEW, viewName);
        verify(model, times(1)).addAttribute(ATTR_ERROR_TYPE, "External Service Error");
        verify(model, times(1)).addAttribute(ATTR_ERROR_MESSAGE, "Unable to retrieve data from the market data service.");
        verify(model, times(1)).addAttribute(ATTR_ERROR_DETAILS, errorMessage);
    }

    @Test
    @DisplayName("Should handle PolygonException correctly")
    void testHandlePolygonException() {
        // Arrange
        String errorMessage = "Polygon API rate limit exceeded";
        PolygonException exception = new PolygonException(errorMessage);

        // Act
        String viewName = globalExceptionHandler.handlePolygonException(exception, model);

        // Assert
        assertEquals(ERROR_VIEW, viewName);
        verify(model, times(1)).addAttribute(ATTR_ERROR_TYPE, "External Service Error");
        verify(model, times(1)).addAttribute(ATTR_ERROR_MESSAGE, "Unable to retrieve historical data from the market data service.");
        verify(model, times(1)).addAttribute(ATTR_ERROR_DETAILS, errorMessage);
    }

    @Test
    @DisplayName("Should handle generic RuntimeException correctly")
    void testHandleRuntimeException() {
        // Arrange
        String errorMessage = "Unexpected runtime error";
        RuntimeException exception = new RuntimeException(errorMessage);

        // Act
        String viewName = globalExceptionHandler.handleRuntimeException(exception, model);

        // Assert
        assertEquals(ERROR_VIEW, viewName);
        verify(model, times(1)).addAttribute(ATTR_ERROR_TYPE, "Runtime Error");
        verify(model, times(1)).addAttribute(ATTR_ERROR_MESSAGE, "An unexpected error occurred while processing your request.");
        verify(model, times(1)).addAttribute(ATTR_ERROR_DETAILS, errorMessage);
    }

    @Test
    @DisplayName("Should handle generic Exception correctly")
    void testHandleGenericException() {
        // Arrange
        String errorMessage = "Unexpected system error";
        Exception exception = new Exception(errorMessage);

        // Act
        String viewName = globalExceptionHandler.handleGenericException(exception, model);

        // Assert
        assertEquals(ERROR_VIEW, viewName);
        verify(model, times(1)).addAttribute(ATTR_ERROR_TYPE, "System Error");
        verify(model, times(1)).addAttribute(ATTR_ERROR_MESSAGE, "An unexpected system error occurred. Please try again later.");
        verify(model, times(1)).addAttribute(ATTR_ERROR_DETAILS, errorMessage);
    }

    @Test
    @DisplayName("Should handle exception with cause correctly")
    void testHandleExceptionWithCause() {
        // Arrange
        Throwable cause = new IllegalStateException("Root cause");
        String errorMessage = "Stock data not available";
        StockDataNotFoundException exception = new StockDataNotFoundException(errorMessage, cause);

        // Act
        String viewName = globalExceptionHandler.handleStockDataNotFoundException(exception, model);

        // Assert
        assertEquals(ERROR_VIEW, viewName);
        verify(model, times(1)).addAttribute(ATTR_ERROR_TYPE, "Stock Data Not Found");
        verify(model, times(1)).addAttribute(ATTR_ERROR_MESSAGE, "The requested stock data could not be found.");
        verify(model, times(1)).addAttribute(ATTR_ERROR_DETAILS, errorMessage);
    }

    @Test
    @DisplayName("Should handle null message in exception")
    void testHandleExceptionWithNullMessage() {
        // Arrange
        RuleDefinitionNotFoundException exception = new RuleDefinitionNotFoundException(null);

        // Act
        String viewName = globalExceptionHandler.handleRuleDefinitionNotFoundException(exception, model);

        // Assert
        assertEquals(ERROR_VIEW, viewName);
        verify(model, times(1)).addAttribute(ATTR_ERROR_TYPE, "Rule Definition Not Found");
        verify(model, times(1)).addAttribute(ATTR_ERROR_MESSAGE, "The requested rule definition could not be found.");
        verify(model, times(1)).addAttribute(ATTR_ERROR_DETAILS, null);
    }
}
