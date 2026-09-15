/**
 * Rule definition form helpers
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
    el.addEventListener('change', function () {
      syncRequiresParam(el);
    });
  });
});
