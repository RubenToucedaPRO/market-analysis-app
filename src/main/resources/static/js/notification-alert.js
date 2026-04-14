/*
 * This script handles the auto-closing of alert notifications with a progress bar.
 * It starts a timer to close the alert after 3 seconds and updates the progress bar accordingly.
 * If the user hovers over the alert, the auto-close timer is paused and the progress bar is frozen.
 * When the user leaves the alert, the timer resumes and the progress bar continues to update.
 */

document.addEventListener("DOMContentLoaded", () => {
  // Usamos delegación de eventos o un observador si las alertas fueran dinámicas,
  // pero para FlashAttributes, este selector es suficiente:
  const notifications = document.querySelectorAll(".js-notification-alert");

  notifications.forEach((alertElement) => {
    let autoCloseTimeout;
    const duration = 3000; // 3 seconds
    const progressBar = alertElement.querySelector(".js-notification-progress");

    const closeAlert = () => {
      const bsAlert = bootstrap.Alert.getOrCreateInstance(alertElement);
      if (alertElement) bsAlert.close();
    };

    const startTimer = () => {
      autoCloseTimeout = setTimeout(closeAlert, duration);
      if (progressBar) {
        progressBar.style.transition = `width ${duration}ms linear`;
        progressBar.style.width = "0%";
      }
    };

    const stopTimer = () => {
      clearTimeout(autoCloseTimeout);
      if (progressBar) {
        progressBar.style.transition = "none";
        // Calculate the current width based on elapsed time
        const currentWidth = progressBar.getBoundingClientRect().width;
        progressBar.style.width = `${currentWidth}px`;
      }
    };

    // Start the auto-close timer when the alert is shown
    startTimer();

    // Event listeners to pause/resume the timer on hover
    alertElement.addEventListener("mouseenter", stopTimer);
    alertElement.addEventListener("mouseleave", startTimer);
  });
});
