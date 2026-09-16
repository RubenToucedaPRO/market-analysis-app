/**
 * Rule definition form helpers.
 * The requiresParam checkbox is disabled in create mode (visual only);
 * the backend aligns it from the canonical catalog on save.
 */
function syncRequiresParam(selectEl) {
  var selectedOption = selectEl.options[selectEl.selectedIndex];
  var requiresParam = selectedOption.dataset.requiresParam === 'true';
  var checkbox = document.getElementById('requiresParam');
  if (checkbox) {
    checkbox.checked = requiresParam;
  }
}

document.addEventListener('DOMContentLoaded', function () {
  document.querySelectorAll('[data-action="sync-requires-param"]').forEach(function (el) {
    // Sync once on load so a preselected value (e.g. after a validation
    // error) is reflected even before the first change event.
    syncRequiresParam(el);
    el.addEventListener('change', function () {
      syncRequiresParam(el);
    });
  });
});
