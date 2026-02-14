package com.market.analysis.presentation.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.market.analysis.domain.exception.RuleDefinitionNotFoundException;
import com.market.analysis.domain.exception.StockDataNotFoundException;
import com.market.analysis.infrastructure.exception.FinnhubException;
import com.market.analysis.infrastructure.exception.PolygonException;
import com.market.analysis.infrastructure.exception.StockException;

/**
 * Global exception handler for the application.
 * This class captures exceptions thrown by controllers and provides
 * a centralized error handling mechanism following the Clean Architecture pattern.
 * 
 * Handles domain, infrastructure, and generic exceptions,
 * providing user-friendly error pages with navigation capabilities.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    private static final String ERROR_VIEW = "error";
    private static final String ATTR_ERROR_MESSAGE = "errorMessage";
    private static final String ATTR_ERROR_DETAILS = "errorDetails";
    private static final String ATTR_ERROR_TYPE = "errorType";

    /**
     * Handles RuleDefinitionNotFoundException - domain exception.
     * Thrown when a requested rule definition is not found in the system.
     * 
     * @param ex the exception
     * @param model the model to add attributes
     * @return the error view name
     */
    @ExceptionHandler(RuleDefinitionNotFoundException.class)
    public String handleRuleDefinitionNotFoundException(RuleDefinitionNotFoundException ex, Model model) {
        log.error("Rule definition not found: {}", ex.getMessage(), ex);
        
        model.addAttribute(ATTR_ERROR_TYPE, "Rule Definition Not Found");
        model.addAttribute(ATTR_ERROR_MESSAGE, "The requested rule definition could not be found.");
        model.addAttribute(ATTR_ERROR_DETAILS, ex.getMessage());
        
        return ERROR_VIEW;
    }

    /**
     * Handles StockDataNotFoundException - domain exception.
     * Thrown when stock data is not available for the requested ticker.
     * 
     * @param ex the exception
     * @param model the model to add attributes
     * @return the error view name
     */
    @ExceptionHandler(StockDataNotFoundException.class)
    public String handleStockDataNotFoundException(StockDataNotFoundException ex, Model model) {
        log.error("Stock data not found: {}", ex.getMessage(), ex);
        
        model.addAttribute(ATTR_ERROR_TYPE, "Stock Data Not Found");
        model.addAttribute(ATTR_ERROR_MESSAGE, "The requested stock data could not be found.");
        model.addAttribute(ATTR_ERROR_DETAILS, ex.getMessage());
        
        return ERROR_VIEW;
    }

    /**
     * Handles StockException - infrastructure exception.
     * General exception for stock-related operations.
     * 
     * @param ex the exception
     * @param model the model to add attributes
     * @return the error view name
     */
    @ExceptionHandler(StockException.class)
    public String handleStockException(StockException ex, Model model) {
        log.error("Stock exception occurred: {}", ex.getMessage(), ex);
        
        model.addAttribute(ATTR_ERROR_TYPE, "Stock Processing Error");
        model.addAttribute(ATTR_ERROR_MESSAGE, "An error occurred while processing stock data.");
        model.addAttribute(ATTR_ERROR_DETAILS, ex.getMessage());
        
        return ERROR_VIEW;
    }

    /**
     * Handles FinnhubException - infrastructure exception.
     * Thrown when there's an error communicating with the Finnhub API.
     * 
     * @param ex the exception
     * @param model the model to add attributes
     * @return the error view name
     */
    @ExceptionHandler(FinnhubException.class)
    public String handleFinnhubException(FinnhubException ex, Model model) {
        log.error("Finnhub API error: {}", ex.getMessage(), ex);
        
        model.addAttribute(ATTR_ERROR_TYPE, "External Service Error");
        model.addAttribute(ATTR_ERROR_MESSAGE, "Unable to retrieve data from the market data service.");
        model.addAttribute(ATTR_ERROR_DETAILS, ex.getMessage());
        
        return ERROR_VIEW;
    }

    /**
     * Handles PolygonException - infrastructure exception.
     * Thrown when there's an error communicating with the Polygon.io API.
     * 
     * @param ex the exception
     * @param model the model to add attributes
     * @return the error view name
     */
    @ExceptionHandler(PolygonException.class)
    public String handlePolygonException(PolygonException ex, Model model) {
        log.error("Polygon API error: {}", ex.getMessage(), ex);
        
        model.addAttribute(ATTR_ERROR_TYPE, "External Service Error");
        model.addAttribute(ATTR_ERROR_MESSAGE, "Unable to retrieve historical data from the market data service.");
        model.addAttribute(ATTR_ERROR_DETAILS, ex.getMessage());
        
        return ERROR_VIEW;
    }

    /**
     * Handles generic RuntimeException.
     * Catches any runtime exception not specifically handled above.
     * 
     * @param ex the exception
     * @param model the model to add attributes
     * @return the error view name
     */
    @ExceptionHandler(RuntimeException.class)
    public String handleRuntimeException(RuntimeException ex, Model model) {
        log.error("Runtime exception occurred: {}", ex.getMessage(), ex);
        
        model.addAttribute(ATTR_ERROR_TYPE, "Runtime Error");
        model.addAttribute(ATTR_ERROR_MESSAGE, "An unexpected error occurred while processing your request.");
        model.addAttribute(ATTR_ERROR_DETAILS, ex.getMessage());
        
        return ERROR_VIEW;
    }

    /**
     * Handles generic Exception.
     * Fallback handler for any exception not caught by more specific handlers.
     * 
     * @param ex the exception
     * @param model the model to add attributes
     * @return the error view name
     */
    @ExceptionHandler(Exception.class)
    public String handleGenericException(Exception ex, Model model) {
        log.error("Unexpected exception occurred: {}", ex.getMessage(), ex);
        
        model.addAttribute(ATTR_ERROR_TYPE, "System Error");
        model.addAttribute(ATTR_ERROR_MESSAGE, "An unexpected system error occurred. Please try again later.");
        model.addAttribute(ATTR_ERROR_DETAILS, ex.getMessage());
        
        return ERROR_VIEW;
    }
}
