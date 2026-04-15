/**
 * Risk Management - Dynamic SMA selector for target and stop-loss fields.
 *
 * Reuses globalThis.ruleDefinitions (populated by the server from
 * RuleCapabilityCatalog) so that allowed SMA periods are never
 * hardcoded in the view or in this script.
 *
 * When a type selector changes:
 *   - SMA        → show <select> with allowed periods, hide <input>
 *   - PERCENTAGE → show <input> with % placeholder, hide <select>
 *   - FIXED_PRICE→ show <input> with $ placeholder, hide <select>
 */

/**
 * Returns the sorted list of allowed SMA periods from the catalog.
 */
function getSmaAllowedPeriods() {
  const smaEntry = (globalThis.ruleDefinitions || []).find(
    (d) => d.code === "SMA",
  );
  if (smaEntry && Array.isArray(smaEntry.allowedParams)) {
    return [...smaEntry.allowedParams].sort((a, b) => a - b);
  }
  return [];
}

/**
 * Populates a <select> element with SMA period options.
 *
 * @param {HTMLSelectElement} selectEl  the <select> to populate
 * @param {Array<number>}    periods   sorted allowed SMA periods
 * @param {string}           currentValue value to pre-select (if editing)
 */
function populateSmaPeriodSelect(selectEl, periods, currentValue) {
  selectEl.innerHTML =
    '<option value="">-- Período --</option>' +
    periods
      .map((p) => {
        const intVal = Number.isInteger(p) ? p : Math.round(p);
        return `<option value="${intVal}">${intVal}</option>`;
      })
      .join("");

  if (currentValue !== null && currentValue !== undefined && currentValue !== "") {
    const normalized = String(Math.round(Number(currentValue)));
    selectEl.value = normalized;
  }
}

/**
 * Handles the toggle between <select> and <input> for a given value field.
 *
 * @param {string} typeSelectId   id of the type <select> (e.g. objectiveTargetType)
 * @param {string} inputId        id of the numeric <input>
 * @param {string} selectId       id of the SMA period <select>
 */
function handleObjectiveTypeChange(typeSelectId, inputId, selectId) {
  const typeSelect = document.getElementById(typeSelectId);
  const input = document.getElementById(inputId);
  const select = document.getElementById(selectId);

  if (!typeSelect || !input || !select) {
    return;
  }

  const selectedType = typeSelect.value;
  const currentInputValue = input.value;
  const currentSelectValue = select.value;

  if (selectedType === "SMA") {
    const periods = getSmaAllowedPeriods();
    populateSmaPeriodSelect(select, periods, currentInputValue || currentSelectValue);

    select.style.display = "";
    select.disabled = false;
    select.setAttribute("required", "required");

    input.style.display = "none";
    input.disabled = true;
    input.removeAttribute("required");
  } else {
    select.style.display = "none";
    select.disabled = true;
    select.removeAttribute("required");

    input.style.display = "";
    input.disabled = false;
    input.setAttribute("required", "required");

    if (selectedType === "PERCENTAGE") {
      input.placeholder = "ej., 5.00 (%)";
    } else if (selectedType === "FIXED_PRICE") {
      input.placeholder = "ej., 150.00 ($)";
    } else {
      input.placeholder = "ej., 5.00";
    }
  }
}

/**
 * Synchronises the hidden <input> value from the active <select> before
 * the form is submitted, so that Thymeleaf binding works correctly.
 */
function syncSelectToInput(selectId, inputId) {
  const select = document.getElementById(selectId);
  const input = document.getElementById(inputId);
  if (select && input && !select.disabled) {
    input.value = select.value;
  }
}

// ──────────────────────────────────────────────
// Initialise on page load and wire up events
// ──────────────────────────────────────────────
document.addEventListener("DOMContentLoaded", function () {
  // Apply initial state for both fields
  handleObjectiveTypeChange(
    "objectiveTargetType",
    "objectiveTargetValue",
    "objectiveTargetSmaSelect",
  );
  handleObjectiveTypeChange(
    "objectiveStopLossType",
    "objectiveStopLossValue",
    "objectiveStopLossSmaSelect",
  );

  // Wire change events on type selectors
  var targetTypeEl = document.getElementById("objectiveTargetType");
  if (targetTypeEl) {
    targetTypeEl.addEventListener("change", function () {
      handleObjectiveTypeChange(
        "objectiveTargetType",
        "objectiveTargetValue",
        "objectiveTargetSmaSelect",
      );
    });
  }

  var stopLossTypeEl = document.getElementById("objectiveStopLossType");
  if (stopLossTypeEl) {
    stopLossTypeEl.addEventListener("change", function () {
      handleObjectiveTypeChange(
        "objectiveStopLossType",
        "objectiveStopLossValue",
        "objectiveStopLossSmaSelect",
      );
    });
  }

  // Sync select values to hidden inputs before form submit
  var form = document.getElementById("strategyForm");
  if (form) {
    form.addEventListener("submit", function () {
      syncSelectToInput("objectiveTargetSmaSelect", "objectiveTargetValue");
      syncSelectToInput("objectiveStopLossSmaSelect", "objectiveStopLossValue");
    });
  }
});
