package com.market.analysis.presentation.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.market.analysis.domain.exception.EntityInUseException;
import com.market.analysis.domain.exception.MissingIndicatorException;
import com.market.analysis.domain.exception.RuleDefinitionNotFoundException;
import com.market.analysis.domain.exception.RuleNotEvaluableException;
import com.market.analysis.domain.exception.StockDataNotFoundException;
import com.market.analysis.infrastructure.exception.AIServiceException;
import com.market.analysis.infrastructure.exception.FinnhubException;
import com.market.analysis.infrastructure.exception.PersistenceException;
import com.market.analysis.infrastructure.exception.PolygonException;
import com.market.analysis.infrastructure.exception.StockException;
import com.market.analysis.presentation.dto.UiNotification;
import com.market.analysis.presentation.util.WebConstants;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Global exception handler for the application.
 * This class captures exceptions thrown by controllers and provides
 * a centralized error handling mechanism following the Clean Architecture pattern.
 *
 * <p>Exceptions are grouped by behaviour:</p>
 * <ul>
 *   <li>Domain/Navigation errors → redirect to referer with original message (log.warn)</li>
 *   <li>Integrity errors        → redirect to referer with a fixed user-friendly message</li>
 *   <li>External-service errors → redirect to referer with a fixed user-friendly message (log.error)</li>
 *   <li>Critical errors         → render error.html with technical details</li>
 * </ul>
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private static final String ERROR_VIEW = "error";
    private static final String ATTR_ERROR_MESSAGE = "errorMessage";
    private static final String ATTR_ERROR_DETAILS = "errorDetails";
    private static final String ATTR_ERROR_TYPE = "errorType";
    private static final String DEFAULT_REFERER = "/";

    private static final String EXTERNAL_SERVICE_MSG =
            "El servicio de datos de mercado no está disponible temporalmente";
    private static final String ENTITY_IN_USE_MSG =
            "No se puede eliminar el recurso porque tiene dependencias asociadas";

    // -------------------------------------------------------------------------
    // Domain / Navigation errors – redirect with warn + original exception message
    // -------------------------------------------------------------------------

    /**
     * Handles RuleDefinitionNotFoundException – domain exception.
     */
    @ExceptionHandler(RuleDefinitionNotFoundException.class)
    public String handleRuleDefinitionNotFoundException(
            RuleDefinitionNotFoundException ex,
            RedirectAttributes ra,
            HttpServletRequest req) {
        log.warn("Rule definition not found: {}", ex.getMessage());
        return redirectWithError(ex.getMessage(), ra, req);
    }

    /**
     * Handles StockDataNotFoundException – domain exception.
     */
    @ExceptionHandler(StockDataNotFoundException.class)
    public String handleStockDataNotFoundException(
            StockDataNotFoundException ex,
            RedirectAttributes ra,
            HttpServletRequest req) {
        log.warn("Stock data not found: {}", ex.getMessage());
        return redirectWithError(ex.getMessage(), ra, req);
    }

    /**
     * Handles MissingIndicatorException – domain exception for missing technical indicators.
     */
    @ExceptionHandler(MissingIndicatorException.class)
    public String handleMissingIndicatorException(
            MissingIndicatorException ex,
            RedirectAttributes ra,
            HttpServletRequest req) {
        log.warn("Missing technical indicator: {}", ex.getMessage());
        return redirectWithError("Faltan datos de indicadores técnicos para realizar el análisis.", ra, req);
    }

    /**
     * Handles RuleNotEvaluableException – domain exception for invalid rules.
     */
    @ExceptionHandler(RuleNotEvaluableException.class)
    public String handleRuleNotEvaluableException(
            RuleNotEvaluableException ex,
            RedirectAttributes ra,
            HttpServletRequest req) {
        log.warn("Rule not evaluable: {}", ex.getMessage());
        return redirectWithError("La regla configurada no puede ser evaluada. Verifica la configuración.", ra, req);
    }

    // -------------------------------------------------------------------------
    // Integrity errors – redirect with fixed user-friendly message
    // -------------------------------------------------------------------------

    /**
     * Handles EntityInUseException – thrown when deleting an entity that has dependencies.
     */
    @ExceptionHandler(EntityInUseException.class)
    public String handleEntityInUseException(
            EntityInUseException ex,
            RedirectAttributes ra,
            HttpServletRequest req) {
        log.warn("Entity in use, cannot be deleted: {}", ex.getMessage());
        return redirectWithError(ENTITY_IN_USE_MSG, ra, req);
    }

    // -------------------------------------------------------------------------
    // External-service errors – redirect with fixed user-friendly message
    // -------------------------------------------------------------------------

    /**
     * Handles IllegalArgumentException – thrown when invalid parameters are provided.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public String handleIllegalArgumentException(
            IllegalArgumentException ex,
            RedirectAttributes ra,
            HttpServletRequest req) {
        log.error("Invalid argument provided: {}", ex.getMessage(), ex);
        return redirectWithError("Se proporcionaron parámetros inválidos. Por favor, verifica e intenta de nuevo.", ra, req);
    }

    /**
     * Handles IllegalStateException – thrown when entity state is inconsistent.
     */
    @ExceptionHandler(IllegalStateException.class)
    public String handleIllegalStateException(
            IllegalStateException ex,
            RedirectAttributes ra,
            HttpServletRequest req) {
        log.error("Illegal state: {}", ex.getMessage(), ex);
        return redirectWithError("Error de estado interno. Por favor, contacta con soporte.", ra, req);
    }

    /**
     * Handles FinnhubException – infrastructure exception for Finnhub API errors.
     */
    @ExceptionHandler(FinnhubException.class)
    public String handleFinnhubException(
            FinnhubException ex,
            RedirectAttributes ra,
            HttpServletRequest req) {
        log.error("Finnhub API error: {}", ex.getMessage(), ex);
        return redirectWithError(EXTERNAL_SERVICE_MSG, ra, req);
    }

    /**
     * Handles PolygonException – infrastructure exception for Polygon.io API errors.
     */
    @ExceptionHandler(PolygonException.class)
    public String handlePolygonException(
            PolygonException ex,
            RedirectAttributes ra,
            HttpServletRequest req) {
        log.error("Polygon API error: {}", ex.getMessage(), ex);
        return redirectWithError(EXTERNAL_SERVICE_MSG, ra, req);
    }

    /**
     * Handles AIServiceException – infrastructure exception for AI service errors.
     */
    @ExceptionHandler(AIServiceException.class)
    public String handleAIServiceException(
            AIServiceException ex,
            RedirectAttributes ra,
            HttpServletRequest req) {
        log.error("AI Service error: {}", ex.getMessage(), ex);
        return redirectWithError(EXTERNAL_SERVICE_MSG, ra, req);
    }

    /**
     * Handles StockException – general infrastructure exception for stock operations.
     */
    @ExceptionHandler(StockException.class)
    public String handleStockException(
            StockException ex,
            RedirectAttributes ra,
            HttpServletRequest req) {
        log.error("Stock exception occurred: {}", ex.getMessage(), ex);
        return redirectWithError(EXTERNAL_SERVICE_MSG, ra, req);
    }

    // -------------------------------------------------------------------------
    // Critical errors – render error.html with technical details
    // -------------------------------------------------------------------------

    /**
     * Handles PersistenceException – infrastructure exception for database errors.
     * Renders the error view because the application cannot continue normally.
     */
    @ExceptionHandler(PersistenceException.class)
    public String handlePersistenceException(PersistenceException ex, Model model) {
        log.error("Database error: {}", ex.getMessage(), ex);

        model.addAttribute(ATTR_ERROR_TYPE, "Database Error");
        model.addAttribute(ATTR_ERROR_MESSAGE, "A database error occurred while processing your request.");
        model.addAttribute(ATTR_ERROR_DETAILS, ex.getMessage());

        return ERROR_VIEW;
    }

    /**
     * Handles generic Exception.
     * Fallback handler for any exception not caught by more specific handlers.
     * Renders the error view because the application cannot continue normally.
     */
    @ExceptionHandler(Exception.class)
    public String handleGenericException(Exception ex, Model model) {
        log.error("Unexpected exception occurred: {}", ex.getMessage(), ex);

        model.addAttribute(ATTR_ERROR_TYPE, "System Error");
        model.addAttribute(ATTR_ERROR_MESSAGE, "An unexpected system error occurred. Please try again later.");
        model.addAttribute(ATTR_ERROR_DETAILS, ex.getMessage());

        return ERROR_VIEW;
    }

    // -------------------------------------------------------------------------
    // DRY helper
    // -------------------------------------------------------------------------

    /**
     * Centralises redirect-with-error logic: adds the error flash attribute and
     * builds a redirect back to the HTTP Referer header, falling back to {@code /}.
     *
     * @param message the user-facing error message to flash
     * @param ra      the RedirectAttributes used to pass flash attributes
     * @param req     the current HTTP request (used to read the Referer header)
     * @return a Spring MVC redirect string
     */
    private String redirectWithError(String message, RedirectAttributes ra, HttpServletRequest req) {
        ra.addFlashAttribute(WebConstants.UI_NOTIFICATION_KEY, UiNotification.error(message));
        String referer = req.getHeader("Referer");
        return "redirect:" + (referer != null ? referer : DEFAULT_REFERER);
    }
}

