package com.market.analysis.presentation.exception;

import java.net.URI;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpHeaders;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
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

    private static final String ERROR_EXTERNAL_SERVICE = "error.external_service";

    private final MessageSource messageSource;

    public GlobalExceptionHandler(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    // -------------------------------------------------------------------------
    // Domain / Navigation errors – redirect with warn + resolved message
    // -------------------------------------------------------------------------

    /**
     * Handles StockDataNotFoundException – domain exception.
     * Resolves the message via {@link MessageSource} using the error code.
     */
    @ExceptionHandler(StockDataNotFoundException.class)
    public String handleStockDataNotFoundException(
            StockDataNotFoundException ex,
            RedirectAttributes ra,
            HttpServletRequest req) {
        log.warn("Stock data not found: {}", ex.getErrorCode());
        String message = messageSource.getMessage(
                ex.getErrorCode(), ex.getParams(), Locale.getDefault());
        return redirectWithError(message, ra, req);
    }

    /**
     * Handles MissingIndicatorException – domain exception for missing technical indicators.
     * Resolves the message via {@link MessageSource} using the error code.
     */
    @ExceptionHandler(MissingIndicatorException.class)
    public String handleMissingIndicatorException(
            MissingIndicatorException ex,
            RedirectAttributes ra,
            HttpServletRequest req) {
        log.warn("Missing technical indicator: {}", ex.getErrorCode());
        String message = messageSource.getMessage(
                ex.getErrorCode(), ex.getParams(), Locale.getDefault());
        return redirectWithError(message, ra, req);
    }

    /**
     * Handles RuleNotEvaluableException – domain exception for invalid rules.
     * Resolves the message via {@link MessageSource} using the error code.
     */
    @ExceptionHandler(RuleNotEvaluableException.class)
    public String handleRuleNotEvaluableException(
            RuleNotEvaluableException ex,
            RedirectAttributes ra,
            HttpServletRequest req) {
        log.warn("Rule not evaluable: {}", ex.getErrorCode());
        String message = messageSource.getMessage(
                ex.getErrorCode(), ex.getParams(), Locale.getDefault());
        return redirectWithError(message, ra, req);
    }

    // -------------------------------------------------------------------------
    // Domain validation errors – redirect with resolved error code message
    // -------------------------------------------------------------------------

    /**
     * Handles DomainValidationException – domain validation error.
     * Resolves the message via {@link MessageSource} using the error code.
     */
    @ExceptionHandler(DomainValidationException.class)
    public String handleDomainValidationException(
            DomainValidationException ex,
            RedirectAttributes ra,
            HttpServletRequest req) {
        log.warn("Domain validation error: {}", ex.getErrorCode());
        String message = messageSource.getMessage(
                ex.getErrorCode(), ex.getParams(), Locale.getDefault());
        return redirectWithError(message, ra, req);
    }

    /**
     * Handles EvaluateStrategyException – domain exception for strategy evaluation failures.
     * Resolves the message via {@link MessageSource} using the error code.
     */
    @ExceptionHandler(EvaluateStrategyException.class)
    public String handleEvaluateStrategyException(
            EvaluateStrategyException ex,
            RedirectAttributes ra,
            HttpServletRequest req) {
        log.warn("Strategy evaluation error: {}", ex.getErrorCode());
        String message = messageSource.getMessage(
                ex.getErrorCode(), ex.getParams(), Locale.getDefault());
        return redirectWithError(message, ra, req);
    }

    // -------------------------------------------------------------------------
    // Integrity errors – redirect with resolved error code message
    // -------------------------------------------------------------------------

    /**
     * Handles EntityInUseException – thrown when deleting an entity that has dependencies.
     * Resolves the message via {@link MessageSource} using the error code.
     */
    @ExceptionHandler(EntityInUseException.class)
    public String handleEntityInUseException(
            EntityInUseException ex,
            RedirectAttributes ra,
            HttpServletRequest req) {
        log.warn("Entity in use, cannot be deleted: {}", ex.getErrorCode());
        String message = messageSource.getMessage(
                ex.getErrorCode(), ex.getParams(), Locale.getDefault());
        return redirectWithError(message, ra, req);
    }

    // -------------------------------------------------------------------------
    // External-service errors – redirect with fixed user-friendly message
    // -------------------------------------------------------------------------

    /**
     * Handles IllegalArgumentException – thrown when invalid parameters are provided.
     * Resolves the message via {@link MessageSource} using the error code.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public String handleIllegalArgumentException(
            IllegalArgumentException ex,
            RedirectAttributes ra,
            HttpServletRequest req) {
        log.error("Invalid argument provided: {}", ex.getMessage(), ex);
        String message = messageSource.getMessage(
                "error.invalid_params", null, Locale.getDefault());
        return redirectWithError(message, ra, req);
    }

    /**
     * Handles IllegalStateException – thrown when entity state is inconsistent.
     * Resolves the message via {@link MessageSource} using the error code.
     */
    @ExceptionHandler(IllegalStateException.class)
    public String handleIllegalStateException(
            IllegalStateException ex,
            RedirectAttributes ra,
            HttpServletRequest req) {
        log.error("Illegal state: {}", ex.getMessage(), ex);
        String message = messageSource.getMessage(
                "error.illegal_state", null, Locale.getDefault());
        return redirectWithError(message, ra, req);
    }

    /**
     * Handles FinnhubException – infrastructure exception for Finnhub API errors.
     * Resolves the message via {@link MessageSource} using the error code.
     */
    @ExceptionHandler(FinnhubException.class)
    public String handleFinnhubException(
            FinnhubException ex,
            RedirectAttributes ra,
            HttpServletRequest req) {
        log.error("Finnhub API error: {}", ex.getMessage(), ex);
        String message = messageSource.getMessage(
                ERROR_EXTERNAL_SERVICE, null, Locale.getDefault());
        return redirectWithError(message, ra, req);
    }

    /**
     * Handles PolygonException – infrastructure exception for Polygon.io API errors.
     * Resolves the message via {@link MessageSource} using the error code.
     */
    @ExceptionHandler(PolygonException.class)
    public String handlePolygonException(
            PolygonException ex,
            RedirectAttributes ra,
            HttpServletRequest req) {
        log.error("Polygon API error: {}", ex.getMessage(), ex);
        String message = messageSource.getMessage(
                ERROR_EXTERNAL_SERVICE, null, Locale.getDefault());
        return redirectWithError(message, ra, req);
    }

    /**
     * Handles AIServiceException – infrastructure exception for AI service errors.
     * Resolves the message via {@link MessageSource} using the error code.
     */
    @ExceptionHandler(AIServiceException.class)
    public String handleAIServiceException(
            AIServiceException ex,
            RedirectAttributes ra,
            HttpServletRequest req) {
        log.error("AI Service error: {}", ex.getMessage(), ex);
        String message = messageSource.getMessage(
                ERROR_EXTERNAL_SERVICE, null, Locale.getDefault());
        return redirectWithError(message, ra, req);
    }

    /**
     * Handles StockException – general infrastructure exception for stock operations.
     * Resolves the message via {@link MessageSource} using the error code.
     */
    @ExceptionHandler(StockException.class)
    public String handleStockException(
            StockException ex,
            RedirectAttributes ra,
            HttpServletRequest req) {
        log.error("Stock exception occurred: {}", ex.getMessage(), ex);
        String message = messageSource.getMessage(
                ERROR_EXTERNAL_SERVICE, null, Locale.getDefault());
        return redirectWithError(message, ra, req);
    }

    // -------------------------------------------------------------------------
    // Critical errors – render error.html with technical details
    // -------------------------------------------------------------------------

    /**
     * Handles PersistenceException – infrastructure exception for database errors.
     * Renders the error view because the application cannot continue normally.
     * Resolves the message via {@link MessageSource} using the error code.
     */
    @ExceptionHandler(PersistenceException.class)
    public String handlePersistenceException(PersistenceException ex, Model model) {
        log.error("Database error: {}", ex.getMessage(), ex);

        String userMessage = messageSource.getMessage(
                "error.database", null, Locale.getDefault());
        model.addAttribute(WebConstants.ATTR_ERROR_TYPE, "Database Error");
        model.addAttribute(WebConstants.ATTR_ERROR_MESSAGE, userMessage);
        model.addAttribute(WebConstants.ATTR_ERROR_DETAILS, ex.getMessage());

        return WebConstants.TEMPLATE_ERROR;
    }

    /**
     * Handles generic Exception.
     * Fallback handler for any exception not caught by more specific handlers.
     * Renders the error view because the application cannot continue normally.
     * Resolves the message via {@link MessageSource} using the error code.
     */
    @ExceptionHandler(Exception.class)
    public String handleGenericException(Exception ex, Model model) {
        log.error("Unexpected exception occurred: {}", ex.getMessage(), ex);

        String userMessage = messageSource.getMessage(
                "error.unexpected", null, Locale.getDefault());
        model.addAttribute(WebConstants.ATTR_ERROR_TYPE, "System Error");
        model.addAttribute(WebConstants.ATTR_ERROR_MESSAGE, userMessage);
        model.addAttribute(WebConstants.ATTR_ERROR_DETAILS, ex.getMessage());

        return WebConstants.TEMPLATE_ERROR;
    }

    // -------------------------------------------------------------------------
    // DRY helper
    // -------------------------------------------------------------------------

    /**
     * Centralises redirect-with-error logic: adds the error flash attribute and
     * builds a redirect back to the HTTP Referer header, falling back to the
     * section of the failing request (or {@code /}).
     *
     * <p>Referers pointing to POST-only action URLs (e.g. {@code /strategies/edit},
     * which has no GET mapping) are sanitised to their parent section so the
     * redirect never lands on an unmapped URL.</p>
     *
     * <p>Referers pointing to POST-only action URLs (e.g. {@code /strategies/edit},
     * which has no GET mapping) are sanitised to their parent section so the
     * redirect never lands on an unmapped URL.</p>
     *
     * @param message the user-facing error message to flash
     * @param ra      the RedirectAttributes used to pass flash attributes
     * @param req     the current HTTP request (used to read the Referer header)
     * @return a Spring MVC redirect string
     */
    private String redirectWithError(String message, RedirectAttributes ra, HttpServletRequest req) {
        ra.addFlashAttribute(WebConstants.UI_NOTIFICATION_KEY, UiNotification.error(message));
        return "redirect:" + safeReferer(req);
    }

    /**
     * Resolves a safe redirect target from the HTTP Referer header.
     *
     * @param req the current HTTP request
     * @return the referer URL, its parent section when it targets a POST-only
     *         action, the section of the failing request when absent, or the
     *         default referer when unparseable
     */
    private String safeReferer(HttpServletRequest req) {
        String referer = req.getHeader(HttpHeaders.REFERER);
        if (referer == null || referer.isBlank()) {
            return sectionOf(req.getRequestURI());
        }
        try {
            String path = URI.create(referer).getPath();
            if (path != null && (path.endsWith("/edit") || path.endsWith("/delete"))) {
                String parent = path.substring(0, path.lastIndexOf('/'));
                return parent.isEmpty() ? WebConstants.DEFAULT_REFERER : parent;
            }
        } catch (IllegalArgumentException ex) {
            log.debug("Unparseable Referer header, falling back to default: {}", referer);
            return WebConstants.DEFAULT_REFERER;
        }
        return referer;
    }

    /**
     * Resolves the section root (first path segment) of a request URI so
     * error redirects keep the user in context instead of landing on {@code /}.
     *
     * @param requestUri the request URI (e.g. {@code /analysis/ticker/999})
     * @return the section root (e.g. {@code /analysis}) or the default referer
     */
    private String sectionOf(String requestUri) {
        if (requestUri == null || requestUri.isBlank()) {
            return WebConstants.DEFAULT_REFERER;
        }
        String[] segments = requestUri.split("/");
        for (String segment : segments) {
            if (!segment.isBlank()) {
                return "/" + segment;
            }
        }
        return WebConstants.DEFAULT_REFERER;
    }
}
