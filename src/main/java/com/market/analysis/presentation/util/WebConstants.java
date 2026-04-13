package com.market.analysis.presentation.util;

/**
 * Presentation-layer constants shared across controllers and exception handlers.
 * Centralises magic strings used as model/flash attribute keys.
 */
public final class WebConstants {

    /**
     * Flash attribute key under which a {@link com.market.analysis.presentation.dto.UiNotification}
     * is stored for rendering in Thymeleaf templates.
     */
    public static final String UI_NOTIFICATION_KEY = "uiNotification";

    private WebConstants() {
        // utility class – no instantiation
    }
}
