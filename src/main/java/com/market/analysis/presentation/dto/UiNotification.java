package com.market.analysis.presentation.dto;

/**
 * UI-layer notification object used to convey a single flash message to the user.
 * Exclusive to the presentation layer; never crosses into Application or Domain.
 *
 * <p>Use the factory methods {@link #success(String)}, {@link #warning(String)} and {@link #error(String)} to
 * construct instances with the correct Bootstrap alert type.</p>
 *
 * @param text the user-facing message text
 * @param type Bootstrap alert suffix: {@code "success"}, {@code "warning"} or {@code "danger"}
 */
public record UiNotification(String text, String type) {

    public static final String TYPE_SUCCESS = "success";
    public static final String TYPE_DANGER = "danger";
    public static final String TYPE_WARNING = "warning";

    /**
     * Creates a success notification.
     *
     * @param message the user-facing message
     * @return a {@code UiNotification} with type {@code "success"}
     */
    public static UiNotification success(String message) {
        return new UiNotification(message, TYPE_SUCCESS);
    }

    /**
     * Creates an error notification.
     *
     * @param message the user-facing message
     * @return a {@code UiNotification} with type {@code "danger"}
     */
    public static UiNotification error(String message) {
        return new UiNotification(message, TYPE_DANGER);
    }

    /**
     * Creates a warning notification.
     *
     * @param message the user-facing message
     * @return a {@code UiNotification} with type {@code "warning"}
     */
    public static UiNotification warning(String message) {
        return new UiNotification(message, TYPE_WARNING);
    }
}
