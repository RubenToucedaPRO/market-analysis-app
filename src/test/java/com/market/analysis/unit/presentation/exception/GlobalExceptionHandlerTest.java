package com.market.analysis.unit.presentation.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.market.analysis.domain.exception.EntityInUseException;
import com.market.analysis.domain.exception.RuleDefinitionNotFoundException;
import com.market.analysis.domain.exception.StockDataNotFoundException;
import com.market.analysis.infrastructure.exception.AIServiceException;
import com.market.analysis.infrastructure.exception.FinnhubException;
import com.market.analysis.infrastructure.exception.PersistenceException;
import com.market.analysis.infrastructure.exception.PolygonException;
import com.market.analysis.infrastructure.exception.StockException;
import com.market.analysis.presentation.exception.GlobalExceptionHandler;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Unit tests for GlobalExceptionHandler.
 * Tests the centralized exception handling mechanism for the application.
 */
@DisplayName("GlobalExceptionHandler Unit Tests")
@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @Mock
    private Model model;

    @Mock
    private RedirectAttributes redirectAttributes;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private GlobalExceptionHandler globalExceptionHandler;

    private static final String ERROR_VIEW = "error";
    private static final String ATTR_ERROR_MESSAGE = "errorMessage";
    private static final String ATTR_ERROR_DETAILS = "errorDetails";
    private static final String ATTR_ERROR_TYPE = "errorType";
    private static final String EXTERNAL_SERVICE_MSG =
            "El servicio de datos de mercado no está disponible temporalmente";
    private static final String ENTITY_IN_USE_MSG =
            "No se puede eliminar el recurso porque tiene dependencias asociadas";

    private static final String REFERER = "/some-page";

    @BeforeEach
    void setUp() {
        // individual tests set up request stubs as needed
    }

    // -------------------------------------------------------------------------
    // Domain / Navigation errors – redirect with original message
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Should redirect with original message for RuleDefinitionNotFoundException")
    void testHandleRuleDefinitionNotFoundException() {
        when(request.getHeader("Referer")).thenReturn(REFERER);
        String errorMessage = "Rule definition with id 123 not found";
        RuleDefinitionNotFoundException exception = new RuleDefinitionNotFoundException(errorMessage);

        String viewName = globalExceptionHandler.handleRuleDefinitionNotFoundException(
                exception, redirectAttributes, request);

        assertTrue(viewName.startsWith("redirect:"));
        verify(redirectAttributes, times(1)).addFlashAttribute(ATTR_ERROR_MESSAGE, errorMessage);
    }

    @Test
    @DisplayName("Should redirect with original message for StockDataNotFoundException")
    void testHandleStockDataNotFoundException() {
        when(request.getHeader("Referer")).thenReturn(REFERER);
        String errorMessage = "Stock data for AAPL not found";
        StockDataNotFoundException exception = new StockDataNotFoundException(errorMessage);

        String viewName = globalExceptionHandler.handleStockDataNotFoundException(
                exception, redirectAttributes, request);

        assertTrue(viewName.startsWith("redirect:"));
        verify(redirectAttributes, times(1)).addFlashAttribute(ATTR_ERROR_MESSAGE, errorMessage);
    }

    @Test
    @DisplayName("Should use Referer header for redirect when present")
    void testRedirectUsesRefererHeader() {
        when(request.getHeader("Referer")).thenReturn("/rule-definitions");
        RuleDefinitionNotFoundException exception = new RuleDefinitionNotFoundException("not found");

        String viewName = globalExceptionHandler.handleRuleDefinitionNotFoundException(
                exception, redirectAttributes, request);

        assertEquals("redirect:/rule-definitions", viewName);
    }

    @Test
    @DisplayName("Should fall back to '/' when Referer header is absent")
    void testRedirectFallsBackToRootWhenNoReferer() {
        when(request.getHeader("Referer")).thenReturn(null);
        RuleDefinitionNotFoundException exception = new RuleDefinitionNotFoundException("not found");

        String viewName = globalExceptionHandler.handleRuleDefinitionNotFoundException(
                exception, redirectAttributes, request);

        assertEquals("redirect:/", viewName);
    }

    // -------------------------------------------------------------------------
    // Integrity errors
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Should redirect with fixed message for EntityInUseException")
    void testHandleEntityInUseException() {
        when(request.getHeader("Referer")).thenReturn(REFERER);
        EntityInUseException exception = new EntityInUseException("rule used in strategy");

        String viewName = globalExceptionHandler.handleEntityInUseException(
                exception, redirectAttributes, request);

        assertTrue(viewName.startsWith("redirect:"));
        verify(redirectAttributes, times(1)).addFlashAttribute(ATTR_ERROR_MESSAGE, ENTITY_IN_USE_MSG);
    }

    // -------------------------------------------------------------------------
    // External-service errors – redirect with fixed friendly message
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Should redirect with friendly message for FinnhubException")
    void testHandleFinnhubException() {
        when(request.getHeader("Referer")).thenReturn(REFERER);
        FinnhubException exception = new FinnhubException("Finnhub API returned 503");

        String viewName = globalExceptionHandler.handleFinnhubException(
                exception, redirectAttributes, request);

        assertTrue(viewName.startsWith("redirect:"));
        verify(redirectAttributes, times(1)).addFlashAttribute(ATTR_ERROR_MESSAGE, EXTERNAL_SERVICE_MSG);
    }

    @Test
    @DisplayName("Should redirect with friendly message for PolygonException")
    void testHandlePolygonException() {
        when(request.getHeader("Referer")).thenReturn(REFERER);
        PolygonException exception = new PolygonException("Polygon API rate limit exceeded");

        String viewName = globalExceptionHandler.handlePolygonException(
                exception, redirectAttributes, request);

        assertTrue(viewName.startsWith("redirect:"));
        verify(redirectAttributes, times(1)).addFlashAttribute(ATTR_ERROR_MESSAGE, EXTERNAL_SERVICE_MSG);
    }

    @Test
    @DisplayName("Should redirect with friendly message for AIServiceException")
    void testHandleAIServiceException() {
        when(request.getHeader("Referer")).thenReturn(REFERER);
        AIServiceException exception = new AIServiceException("AI service timeout");

        String viewName = globalExceptionHandler.handleAIServiceException(
                exception, redirectAttributes, request);

        assertTrue(viewName.startsWith("redirect:"));
        verify(redirectAttributes, times(1)).addFlashAttribute(ATTR_ERROR_MESSAGE, EXTERNAL_SERVICE_MSG);
    }

    @Test
    @DisplayName("Should redirect with friendly message for StockException")
    void testHandleStockException() {
        when(request.getHeader("Referer")).thenReturn(REFERER);
        StockException exception = new StockException("Error processing stock data");

        String viewName = globalExceptionHandler.handleStockException(
                exception, redirectAttributes, request);

        assertTrue(viewName.startsWith("redirect:"));
        verify(redirectAttributes, times(1)).addFlashAttribute(ATTR_ERROR_MESSAGE, EXTERNAL_SERVICE_MSG);
    }

    // -------------------------------------------------------------------------
    // Critical errors – render error.html
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Should render error view for PersistenceException")
    void testHandlePersistenceException() {
        String errorMessage = "DB connection refused";
        PersistenceException exception = new PersistenceException(errorMessage);

        String viewName = globalExceptionHandler.handlePersistenceException(exception, model);

        assertEquals(ERROR_VIEW, viewName);
        verify(model, times(1)).addAttribute(ATTR_ERROR_TYPE, "Database Error");
        verify(model, times(1)).addAttribute(eq(ATTR_ERROR_MESSAGE), eq("A database error occurred while processing your request."));
        verify(model, times(1)).addAttribute(ATTR_ERROR_DETAILS, errorMessage);
    }

    @Test
    @DisplayName("Should render error view for generic Exception")
    void testHandleGenericException() {
        String errorMessage = "Unexpected system error";
        Exception exception = new Exception(errorMessage);

        String viewName = globalExceptionHandler.handleGenericException(exception, model);

        assertEquals(ERROR_VIEW, viewName);
        verify(model, times(1)).addAttribute(ATTR_ERROR_TYPE, "System Error");
        verify(model, times(1)).addAttribute(eq(ATTR_ERROR_MESSAGE), eq("An unexpected system error occurred. Please try again later."));
        verify(model, times(1)).addAttribute(ATTR_ERROR_DETAILS, errorMessage);
    }

    // -------------------------------------------------------------------------
    // Edge cases
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Should handle StockDataNotFoundException with cause correctly")
    void testHandleExceptionWithCause() {
        when(request.getHeader("Referer")).thenReturn(REFERER);
        Throwable cause = new IllegalStateException("Root cause");
        String errorMessage = "Stock data not available";
        StockDataNotFoundException exception = new StockDataNotFoundException(errorMessage, cause);

        String viewName = globalExceptionHandler.handleStockDataNotFoundException(
                exception, redirectAttributes, request);

        assertTrue(viewName.startsWith("redirect:"));
        verify(redirectAttributes, times(1)).addFlashAttribute(ATTR_ERROR_MESSAGE, errorMessage);
    }

    @Test
    @DisplayName("Should handle null message in RuleDefinitionNotFoundException")
    void testHandleExceptionWithNullMessage() {
        when(request.getHeader("Referer")).thenReturn(REFERER);
        RuleDefinitionNotFoundException exception = new RuleDefinitionNotFoundException(null);

        String viewName = globalExceptionHandler.handleRuleDefinitionNotFoundException(
                exception, redirectAttributes, request);

        assertTrue(viewName.startsWith("redirect:"));
        verify(redirectAttributes, times(1)).addFlashAttribute(ATTR_ERROR_MESSAGE, null);
    }
}

