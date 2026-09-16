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
 *
 * Labels, placeholders and SMA horizon names are resolved from
 * data-* attributes rendered by Thymeleaf (messages.properties),
 * never from hardcoded Spanish strings.
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
 * Resolves the horizon label (corto/medio/largo plazo) for a period.
 *
 * @param {number} period integer SMA period
 * @param {HTMLSelectElement} selectEl the <select> holding i18n labels
 */
function getSmaHorizonLabel(period, selectEl) {
  const labels = selectEl?.dataset ?? {};
  if (period === 20) {
    return labels.short || "";
  }
  if (period === 50) {
    return labels.medium || "";
  }
  if (period === 200) {
    return labels.long || "";
  }
  return "";
}

/**
 * Builds the visible label for an SMA option (e.g. "SMA 20 - Corto plazo").
 *
 * @param {number} period integer SMA period
 * @param {HTMLSelectElement} selectEl the <select> holding i18n labels
 */
function formatSmaOptionLabel(period, selectEl) {
  const horizon = getSmaHorizonLabel(period, selectEl);
  return horizon ? `SMA ${period} - ${horizon}` : `SMA ${period}`;
}

/**
 * Populates a <select> element with SMA period options.
 *
 * @param {HTMLSelectElement} selectEl  the <select> to populate
 * @param {Array<number>}    periods   sorted allowed SMA periods
 * @param {string}           currentValue value to pre-select (if editing)
 */
function populateSmaPeriodSelect(selectEl, periods, currentValue) {
  const defaultOption =
    selectEl.dataset?.defaultOption || "-- Periodo SMA --";
  selectEl.innerHTML =
    `<option value="">${defaultOption}</option>` +
    periods
      .map((p) => {
        const intVal = Number.isInteger(p) ? p : Math.round(p);
        return `<option value="${intVal}">${formatSmaOptionLabel(intVal, selectEl)}</option>`;
      })
      .join("");

  if (currentValue != null && currentValue !== "") {
    const normalized = String(Math.round(Number(currentValue)));
    selectEl.value = normalized;
  }
}

/**
 * Updates label text, placeholder and SMA help tooltip for a value field.
 *
 * @param {HTMLSelectElement} typeSelect the type <select> with data-* i18n
 * @param {HTMLInputElement} input the numeric <input>
 * @param {string} selectedType current objective type
 * @param {string} labelId id of the label <span> to update
 * @param {string} helpId id of the SMA help tooltip icon
 */
function updateValueFieldHints(typeSelect, input, selectedType, labelId, helpId) {
  const data = typeSelect.dataset || {};
  const labelEl = document.getElementById(labelId);
  const helpEl = document.getElementById(helpId);

  if (labelEl) {
    if (selectedType === "SMA" && data.labelSma) {
      labelEl.textContent = data.labelSma;
    } else if (selectedType === "PERCENTAGE" && data.labelPercentage) {
      labelEl.textContent = data.labelPercentage;
    } else if (selectedType === "FIXED_PRICE" && data.labelFixed) {
      labelEl.textContent = data.labelFixed;
    } else if (data.labelDefault) {
      labelEl.textContent = data.labelDefault;
    }
  }

  if (helpEl) {
    helpEl.classList.toggle("d-none", selectedType !== "SMA");
  }

  if (selectedType === "PERCENTAGE") {
    input.placeholder = data.placeholderPercentage || "ej., 5.00 (%)";
  } else if (selectedType === "FIXED_PRICE") {
    input.placeholder = data.placeholderFixed || "ej., 150.00 ($)";
  } else if (selectedType !== "SMA") {
    input.placeholder = data.placeholderDefault || "ej., 5.00";
  }
}

/**
 * Handles the toggle between <select> and <input> for a given value field.
 *
 * @param {string} typeSelectId   id of the type <select> (e.g. objectiveTargetType)
 * @param {string} inputId        id of the numeric <input>
 * @param {string} selectId       id of the SMA period <select>
 * @param {string} labelId        id of the label <span> to update
 * @param {string} helpId         id of the SMA help tooltip icon
 */
function handleObjectiveTypeChange(typeSelectId, inputId, selectId, labelId, helpId) {
  const typeSelect = document.getElementById(typeSelectId);
  const input = document.getElementById(inputId);
  const select = document.getElementById(selectId);

  if (!typeSelect || !input || !select) {
    return;
  }

  const selectedType = typeSelect.value;
  const currentInputValue = input.value;
  const currentSelectValue = select.value;

  updateValueFieldHints(typeSelect, input, selectedType, labelId, helpId);

  if (selectedType === "SMA") {
    const periods = getSmaAllowedPeriods();
    populateSmaPeriodSelect(
      select,
      periods,
      currentInputValue || currentSelectValue,
    );

    select.classList.remove("d-none");
    select.disabled = false;
    select.setAttribute("required", "required");

    input.classList.add("d-none");
    input.disabled = true;
    input.removeAttribute("required");
  } else {
    select.classList.add("d-none");
    select.disabled = true;
    select.removeAttribute("required");

    input.classList.remove("d-none");
    input.disabled = false;
    input.setAttribute("required", "required");
  }
}

/**
 * Syncs the value from a <select> to a hidden <input> before form submission, if the select is enabled.
 *
 * @param {string} selectId id of the <select> element
 * @param {string} inputId  id of the hidden <input> element
 */
function syncSelectToInput(selectId, inputId) {
  const select = document.getElementById(selectId);
  const input = document.getElementById(inputId);
  if (select && input && !select.disabled) {
    input.value = select.value;
    input.disabled = false;
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
    "objectiveTargetValueLabel",
    "objectiveTargetSmaHelp",
  );
  handleObjectiveTypeChange(
    "objectiveStopLossType",
    "objectiveStopLossValue",
    "objectiveStopLossSmaSelect",
    "objectiveStopLossValueLabel",
    "objectiveStopLossSmaHelp",
  );

  // Wire change events on type selectors
  const targetTypeEl = document.getElementById("objectiveTargetType");
  if (targetTypeEl) {
    targetTypeEl.addEventListener("change", function () {
      handleObjectiveTypeChange(
        "objectiveTargetType",
        "objectiveTargetValue",
        "objectiveTargetSmaSelect",
        "objectiveTargetValueLabel",
        "objectiveTargetSmaHelp",
      );
    });
  }

  const stopLossTypeEl = document.getElementById("objectiveStopLossType");
  if (stopLossTypeEl) {
    stopLossTypeEl.addEventListener("change", function () {
      handleObjectiveTypeChange(
        "objectiveStopLossType",
        "objectiveStopLossValue",
        "objectiveStopLossSmaSelect",
        "objectiveStopLossValueLabel",
        "objectiveStopLossSmaHelp",
      );
    });
  }

  // Sync select values to hidden inputs before form submit
  const form = document.getElementById("strategyForm");
  if (form) {
    form.addEventListener("submit", function () {
      syncSelectToInput("objectiveTargetSmaSelect", "objectiveTargetValue");
      syncSelectToInput("objectiveStopLossSmaSelect", "objectiveStopLossValue");
    });
  }
});
