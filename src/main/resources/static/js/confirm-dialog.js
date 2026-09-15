/**
 * Confirm dialog and navigation handler
 * - Elements with data-confirm attribute trigger a confirm() before their action.
 * - Elements with data-action="back" trigger window.history.back()
 */
document.addEventListener('DOMContentLoaded', function () {
  document.querySelectorAll('[data-confirm]').forEach(function (el) {
    el.addEventListener('click', function (e) {
      if (!confirm(el.getAttribute('data-confirm'))) {
        e.preventDefault();
        e.stopPropagation();
      }
    });
  });

  document.querySelectorAll('[data-action="back"]').forEach(function (el) {
    el.addEventListener('click', function () {
      window.history.back();
    });
  });
});
