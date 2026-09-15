/**
 * Bootstrap tooltip initialization
 * Activates all elements with data-bs-toggle="tooltip"
 */
document.addEventListener('DOMContentLoaded', function () {
  document.querySelectorAll('[data-bs-toggle="tooltip"]').forEach(function (el) {
    new bootstrap.Tooltip(el);
  });
});
