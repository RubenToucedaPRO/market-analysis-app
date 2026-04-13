package com.market.analysis.presentation.dto;

/**
 * UI-layer notification object used to convey a single flash message to the user.
 * Exclusive to the presentation layer; never crosses into Application or Domain.
 *
 * <p>Use the factory methods {@link #success(String)} and {@link #error(String)} to
 * construct instances with the correct Bootstrap alert type.</p>
 *
 * @param text the user-facing message text
 * @param type Bootstrap alert suffix: {@code "success"} or {@code "danger"}
 */
public record UiNotification(String text, String type) {

    /**
     * Creates a success notification.
     *
     * @param message the user-facing message
     * @return a {@code UiNotification} with type {@code "success"}
     */
    public static UiNotification success(String message) {
        return new UiNotification(message, "success");
    }

    /**
     * Creates an error notification.
     *
     * @param message the user-facing message
     * @return a {@code UiNotification} with type {@code "danger"}
     */
    public static UiNotification error(String message) {
        return new UiNotification(message, "danger");
    }
}
