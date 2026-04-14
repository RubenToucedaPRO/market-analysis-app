/* * This script automatically closes Bootstrap alerts after a specified duration (10 seconds).
 * If the user hovers over the alert, the auto-close is paused, and it resumes when the mouse leaves.
 * It uses Bootstrap's Alert component to handle the closing of alerts.
 *
 * Expected DOM: Any element with the class "alert-dismissible" will be targeted for auto-closing.
 */
document.addEventListener("DOMContentLoaded", function () {
  const alerts = document.querySelectorAll(".alert-dismissible");

  alerts.forEach(function (alert) {
    let autoCloseTimeout;
    const duration = 3000; // 3 seconds

    // Function for starting the auto-close timer
    const startTimer = () => {
      autoCloseTimeout = setTimeout(() => {
        const bsAlert = new bootstrap.Alert(alert);
        if (alert) bsAlert.close();
      }, duration);
    };

    // Start the timer when the page loads
    startTimer();

    // On mouse enter: Cancel the auto-close
    alert.addEventListener("mouseenter", () => {
      clearTimeout(autoCloseTimeout);
    });

    // On mouse leave: Restart the auto-close timer
    alert.addEventListener("mouseleave", () => {
      startTimer();
    });
  });
});
