package com.market.analysis.unit.presentation.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Locale;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.market.analysis.domain.exception.DomainValidationException;
import com.market.analysis.domain.exception.EntityInUseException;
import com.market.analysis.domain.exception.EvaluateStrategyException;
import com.market.analysis.domain.exception.MissingIndicatorException;
import com.market.analysis.domain.exception.RuleNotEvaluableException;
import com.market.analysis.domain.exception.StockDataNotFoundException;
import com.market.analysis.infrastructure.exception.AIServiceException;
import com.market.analysis.infrastructure.exception.FinnhubException;
import com.market.analysis.infrastructure.exception.PersistenceException;
import com.market.analysis.infrastructure.exception.PolygonException;
import com.market.analysis.infrastructure.exception.StockException;
import com.market.analysis.presentation.dto.UiNotification;
import com.market.analysis.presentation.exception.GlobalExceptionHandler;
import com.market.analysis.presentation.util.WebConstants;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Unit tests for GlobalExceptionHandler.
 * Tests the centralized exception handling mechanism for the application.
 */
@DisplayName("GlobalExceptionHandler Unit Tests")
@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @Mock
    private MessageSource messageSource;

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

    private static final String REFERER = "/some-page";

    // -------------------------------------------------------------------------
    // Domain / Navigation errors – redirect with resolved message
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Should redirect with resolved message for StockDataNotFoundException")
    void testHandleStockDataNotFoundException() {
        when(request.getHeader("Referer")).thenReturn(REFERER);
        String errorCode = "ticker.not_found";
        Object[] params = new Object[]{42L};
        String resolvedMessage = "Ticker data not found for: 42";
        StockDataNotFoundException exception = new StockDataNotFoundException(errorCode, params);

        when(messageSource.getMessage(eq(errorCode), eq(params), any(Locale.class)))
                .thenReturn(resolvedMessage);

        String viewName = globalExceptionHandler.handleStockDataNotFoundException(
                exception, redirectAttributes, request);

        assertTrue(viewName.startsWith("redirect:"));
        verify(messageSource, times(1)).getMessage(eq(errorCode), eq(params), any(Locale.class));
        verify(redirectAttributes, times(1)).addFlashAttribute(
                WebConstants.UI_NOTIFICATION_KEY, UiNotification.error(resolvedMessage));
    }

    @Test
    @DisplayName("Should handle StockDataNotFoundException without params correctly")
    void testHandleStockDataNotFoundExceptionWithoutParams() {
        when(request.getHeader("Referer")).thenReturn(REFERER);
        String errorCode = "ticker.not_found";
        Object[] params = new Object[]{};
        String resolvedMessage = "Ticker data not found";
        StockDataNotFoundException exception = new StockDataNotFoundException(errorCode);

        when(messageSource.getMessage(eq(errorCode), eq(params), any(Locale.class)))
                .thenReturn(resolvedMessage);

        String viewName = globalExceptionHandler.handleStockDataNotFoundException(
                exception, redirectAttributes, request);

        assertTrue(viewName.startsWith("redirect:"));
        verify(messageSource, times(1)).getMessage(eq(errorCode), eq(params), any(Locale.class));
        verify(redirectAttributes, times(1)).addFlashAttribute(
                WebConstants.UI_NOTIFICATION_KEY, UiNotification.error(resolvedMessage));
    }

    // -------------------------------------------------------------------------
    // Domain validation errors
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Should redirect with resolved message for DomainValidationException")
    void testHandleDomainValidationException() {
        when(request.getHeader("Referer")).thenReturn(REFERER);
        String errorCode = "validation.target_price_null";
        Object[] params = new Object[]{};
        String resolvedMessage = "Target price cannot be null";
        DomainValidationException exception = new DomainValidationException(errorCode);

        when(messageSource.getMessage(eq(errorCode), eq(params), any(Locale.class)))
                .thenReturn(resolvedMessage);

        String viewName = globalExceptionHandler.handleDomainValidationException(
                exception, redirectAttributes, request);

        assertTrue(viewName.startsWith("redirect:"));
        verify(messageSource, times(1)).getMessage(eq(errorCode), eq(params), any(Locale.class));
        verify(redirectAttributes, times(1)).addFlashAttribute(
                WebConstants.UI_NOTIFICATION_KEY, UiNotification.error(resolvedMessage));
    }

    @Test
    @DisplayName("Should redirect with resolved message for DomainValidationException with params")
    void testHandleDomainValidationExceptionWithParams() {
        when(request.getHeader("Referer")).thenReturn(REFERER);
        String errorCode = "validation.rd_exists";
        Object[] params = new Object[]{"SMA_CROSS"};
        String resolvedMessage = "RuleDefinition with code 'SMA_CROSS' already exists";
        DomainValidationException exception = new DomainValidationException(errorCode, params);

        when(messageSource.getMessage(eq(errorCode), eq(params), any(Locale.class)))
                .thenReturn(resolvedMessage);

        String viewName = globalExceptionHandler.handleDomainValidationException(
                exception, redirectAttributes, request);

        assertTrue(viewName.startsWith("redirect:"));
        verify(messageSource, times(1)).getMessage(eq(errorCode), eq(params), any(Locale.class));
        verify(redirectAttributes, times(1)).addFlashAttribute(
                WebConstants.UI_NOTIFICATION_KEY, UiNotification.error(resolvedMessage));
    }

    // -------------------------------------------------------------------------
    // MissingIndicatorException
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Should redirect with resolved message for MissingIndicatorException")
    void testHandleMissingIndicatorException() {
        when(request.getHeader("Referer")).thenReturn(REFERER);
        String errorCode = "rule.missing_indicator";
        Object[] params = new Object[]{};
        String resolvedMessage = "Faltan datos de indicadores tecnicos para realizar el analisis.";
        MissingIndicatorException exception = new MissingIndicatorException(errorCode);

        when(messageSource.getMessage(eq(errorCode), eq(params), any(Locale.class)))
                .thenReturn(resolvedMessage);

        String viewName = globalExceptionHandler.handleMissingIndicatorException(
                exception, redirectAttributes, request);

        assertTrue(viewName.startsWith("redirect:"));
        verify(messageSource, times(1)).getMessage(eq(errorCode), eq(params), any(Locale.class));
        verify(redirectAttributes, times(1)).addFlashAttribute(
                WebConstants.UI_NOTIFICATION_KEY, UiNotification.error(resolvedMessage));
    }

    // -------------------------------------------------------------------------
    // RuleNotEvaluableException
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Should redirect with resolved message for RuleNotEvaluableException")
    void testHandleRuleNotEvaluableException() {
        when(request.getHeader("Referer")).thenReturn(REFERER);
        String errorCode = "rule.not_evaluable";
        Object[] params = new Object[]{};
        String resolvedMessage = "La regla configurada no puede ser evaluada.";
        RuleNotEvaluableException exception = new RuleNotEvaluableException(errorCode);

        when(messageSource.getMessage(eq(errorCode), eq(params), any(Locale.class)))
                .thenReturn(resolvedMessage);

        String viewName = globalExceptionHandler.handleRuleNotEvaluableException(
                exception, redirectAttributes, request);

        assertTrue(viewName.startsWith("redirect:"));
        verify(messageSource, times(1)).getMessage(eq(errorCode), eq(params), any(Locale.class));
        verify(redirectAttributes, times(1)).addFlashAttribute(
                WebConstants.UI_NOTIFICATION_KEY, UiNotification.error(resolvedMessage));
    }

    // -------------------------------------------------------------------------
    // Integrity errors
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Should redirect with resolved message for EntityInUseException")
    void testHandleEntityInUseException() {
        when(request.getHeader("Referer")).thenReturn(REFERER);
        String errorCode = "entity.in_use";
        Object[] params = new Object[]{"SMA_CROSS"};
        String resolvedMessage = "No se puede eliminar el recurso 'SMA_CROSS' porque tiene dependencias asociadas";
        EntityInUseException exception = new EntityInUseException(errorCode, params);

        when(messageSource.getMessage(eq(errorCode), eq(params), any(Locale.class)))
                .thenReturn(resolvedMessage);

        String viewName = globalExceptionHandler.handleEntityInUseException(
                exception, redirectAttributes, request);

        assertTrue(viewName.startsWith("redirect:"));
        verify(messageSource, times(1)).getMessage(eq(errorCode), eq(params), any(Locale.class));
        verify(redirectAttributes, times(1)).addFlashAttribute(
                WebConstants.UI_NOTIFICATION_KEY, UiNotification.error(resolvedMessage));
    }

    // -------------------------------------------------------------------------
    // EvaluateStrategyException
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Should redirect with resolved message for EvaluateStrategyException")
    void testHandleEvaluateStrategyException() {
        when(request.getHeader("Referer")).thenReturn(REFERER);
        String errorCode = "strategy.evaluation_failed";
        Object[] params = new Object[]{};
        String resolvedMessage = "No se pudo generar una evaluacion determinista valida.";
        EvaluateStrategyException exception = new EvaluateStrategyException(errorCode);

        when(messageSource.getMessage(eq(errorCode), eq(params), any(Locale.class)))
                .thenReturn(resolvedMessage);

        String viewName = globalExceptionHandler.handleEvaluateStrategyException(
                exception, redirectAttributes, request);

        assertTrue(viewName.startsWith("redirect:"));
        verify(messageSource, times(1)).getMessage(eq(errorCode), eq(params), any(Locale.class));
        verify(redirectAttributes, times(1)).addFlashAttribute(
                WebConstants.UI_NOTIFICATION_KEY, UiNotification.error(resolvedMessage));
    }

    // -------------------------------------------------------------------------
    // External-service errors – redirect with resolved friendly message
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Should redirect with resolved message for FinnhubException")
    void testHandleFinnhubException() {
        when(request.getHeader("Referer")).thenReturn(REFERER);
        String resolvedMessage = "El servicio de datos de mercado no esta disponible temporalmente";
        FinnhubException exception = new FinnhubException("Finnhub API returned 503");

        when(messageSource.getMessage(eq("error.external_service"), eq(null), any(Locale.class)))
                .thenReturn(resolvedMessage);

        String viewName = globalExceptionHandler.handleFinnhubException(
                exception, redirectAttributes, request);

        assertTrue(viewName.startsWith("redirect:"));
        verify(messageSource, times(1)).getMessage(eq("error.external_service"), eq(null), any(Locale.class));
        verify(redirectAttributes, times(1)).addFlashAttribute(
                WebConstants.UI_NOTIFICATION_KEY, UiNotification.error(resolvedMessage));
    }

    @Test
    @DisplayName("Should redirect with resolved message for PolygonException")
    void testHandlePolygonException() {
        when(request.getHeader("Referer")).thenReturn(REFERER);
        String resolvedMessage = "El servicio de datos de mercado no esta disponible temporalmente";
        PolygonException exception = new PolygonException("Polygon API rate limit exceeded");

        when(messageSource.getMessage(eq("error.external_service"), eq(null), any(Locale.class)))
                .thenReturn(resolvedMessage);

        String viewName = globalExceptionHandler.handlePolygonException(
                exception, redirectAttributes, request);

        assertTrue(viewName.startsWith("redirect:"));
        verify(messageSource, times(1)).getMessage(eq("error.external_service"), eq(null), any(Locale.class));
        verify(redirectAttributes, times(1)).addFlashAttribute(
                WebConstants.UI_NOTIFICATION_KEY, UiNotification.error(resolvedMessage));
    }

    @Test
    @DisplayName("Should redirect with resolved message for AIServiceException")
    void testHandleAIServiceException() {
        when(request.getHeader("Referer")).thenReturn(REFERER);
        String resolvedMessage = "El servicio de datos de mercado no esta disponible temporalmente";
        AIServiceException exception = new AIServiceException("AI service timeout");

        when(messageSource.getMessage(eq("error.external_service"), eq(null), any(Locale.class)))
                .thenReturn(resolvedMessage);

        String viewName = globalExceptionHandler.handleAIServiceException(
                exception, redirectAttributes, request);

        assertTrue(viewName.startsWith("redirect:"));
        verify(messageSource, times(1)).getMessage(eq("error.external_service"), eq(null), any(Locale.class));
        verify(redirectAttributes, times(1)).addFlashAttribute(
                WebConstants.UI_NOTIFICATION_KEY, UiNotification.error(resolvedMessage));
    }

    @Test
    @DisplayName("Should redirect with resolved message for StockException")
    void testHandleStockException() {
        when(request.getHeader("Referer")).thenReturn(REFERER);
        String resolvedMessage = "El servicio de datos de mercado no esta disponible temporalmente";
        StockException exception = new StockException("Error processing stock data");

        when(messageSource.getMessage(eq("error.external_service"), eq(null), any(Locale.class)))
                .thenReturn(resolvedMessage);

        String viewName = globalExceptionHandler.handleStockException(
                exception, redirectAttributes, request);

        assertTrue(viewName.startsWith("redirect:"));
        verify(messageSource, times(1)).getMessage(eq("error.external_service"), eq(null), any(Locale.class));
        verify(redirectAttributes, times(1)).addFlashAttribute(
                WebConstants.UI_NOTIFICATION_KEY, UiNotification.error(resolvedMessage));
    }

    // -------------------------------------------------------------------------
    // Critical errors – render error.html
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Should render error view for PersistenceException")
    void testHandlePersistenceException() {
        String errorMessage = "DB connection refused";
        PersistenceException exception = new PersistenceException(errorMessage);
        String resolvedMessage = "A database error occurred while processing your request.";

        when(messageSource.getMessage(eq("error.database"), eq(null), any(Locale.class)))
                .thenReturn(resolvedMessage);

        String viewName = globalExceptionHandler.handlePersistenceException(exception, model);

        assertEquals(ERROR_VIEW, viewName);
        verify(model, times(1)).addAttribute(ATTR_ERROR_TYPE, "Database Error");
        verify(model, times(1)).addAttribute(ATTR_ERROR_MESSAGE, resolvedMessage);
        verify(model, times(1)).addAttribute(ATTR_ERROR_DETAILS, errorMessage);
    }

    @Test
    @DisplayName("Should render error view for generic Exception")
    void testHandleGenericException() {
        String errorMessage = "Unexpected system error";
        Exception exception = new Exception(errorMessage);
        String resolvedMessage = "An unexpected system error occurred. Please try again later.";

        when(messageSource.getMessage(eq("error.unexpected"), eq(null), any(Locale.class)))
                .thenReturn(resolvedMessage);

        String viewName = globalExceptionHandler.handleGenericException(exception, model);

        assertEquals(ERROR_VIEW, viewName);
        verify(model, times(1)).addAttribute(ATTR_ERROR_TYPE, "System Error");
        verify(model, times(1)).addAttribute(ATTR_ERROR_MESSAGE, resolvedMessage);
        verify(model, times(1)).addAttribute(ATTR_ERROR_DETAILS, errorMessage);
    }

    // -------------------------------------------------------------------------
    // Edge cases
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Should handle StockDataNotFoundException with cause correctly")
    void testHandleExceptionWithCause() {
        when(request.getHeader("Referer")).thenReturn(REFERER);
        String errorCode = "ticker.not_found";
        Object[] params = new Object[]{42L};
        String resolvedMessage = "Ticker data not found for: 42";
        StockDataNotFoundException exception = new StockDataNotFoundException(errorCode, params);

        when(messageSource.getMessage(eq(errorCode), eq(params), any(Locale.class)))
                .thenReturn(resolvedMessage);

        String viewName = globalExceptionHandler.handleStockDataNotFoundException(
                exception, redirectAttributes, request);

        assertTrue(viewName.startsWith("redirect:"));
        verify(redirectAttributes, times(1)).addFlashAttribute(
                WebConstants.UI_NOTIFICATION_KEY, UiNotification.error(resolvedMessage));
    }

}
