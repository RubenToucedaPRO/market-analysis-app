/**
 * Strategy Manager - JavaScript for Dynamic Rule Management
 *
 * This script handles adding and removing rules dynamically in the strategy form.
 * Written to be simple and easy to understand for junior developers.
 */

// Keep track of how many rules we have
let ruleIndex = 0;

/**
 * Initialize the rule index when page loads
 * Count existing rules on the page
 */
document.addEventListener("DOMContentLoaded", function () {
  // Count how many rules already exist on the page
  const existingRules = document.querySelectorAll(".rule-row");
  ruleIndex = existingRules.length;

  // Initialize visibility for existing rules
  existingRules.forEach((rule, index) => {
    const subjectSelect = rule.querySelector('select[name*="subjectCode"]');
    if (subjectSelect) {
      toggleSubjectParameter(subjectSelect, index);
    }

    const targetSelect = rule.querySelector('select[name*="targetCode"]');
    if (targetSelect) {
      toggleTargetParameter(targetSelect, index);
    }
  });

  // If there are no rules, add one automatically
  if (ruleIndex === 0) {
    addRuleRow();
  }
});

/**
 * Add a new rule row to the form
 * Creates a new card with all the necessary input fields
 */
function addRuleRow() {
  const container = document.getElementById("rules-container");
  const template = document.getElementById("rule-template");

  const clone = template.content.cloneNode(true);
  const newIndex = container.querySelectorAll(".rule-row").length;

  clone.querySelectorAll("[name]").forEach((el) => {
    el.name = el.name.replace("999", newIndex);
  });

  clone.querySelectorAll("[id]").forEach((el) => {
    el.id = el.id.replace("999", newIndex);
  });

  clone.querySelectorAll("[onchange]").forEach((el) => {
    el.setAttribute(
      "onchange",
      el.getAttribute("onchange").replace("999", newIndex),
    );
  });

  clone.querySelectorAll("label[for]").forEach((el) => {
    el.htmlFor = el.htmlFor.replace("999", newIndex);
  });

  container.appendChild(clone);
}

/**
 * Remove a rule row from the form
 * @param {HTMLElement} button - The delete button that was clicked
 */
function removeRuleRow(button) {
  // Find the rule card that contains this button
  const ruleCard = button.closest(".rule-row");

  // Remove the rule from the page
  if (ruleCard) {
    ruleCard.remove();

    // Re-index all remaining rules to keep numbering correct
    reindexRules();
  }
}

/**
 * Re-index all rules after one is removed
 * This ensures the form field names stay sequential (rules[0], rules[1], etc.)
 */
function reindexRules() {
  // Get all remaining rule rows
  const rules = document.querySelectorAll(".rule-row");

  if (rules.length === 0) {
    // If no rules remain, add a new empty rule
    addRuleRow();
    return;
  }

  // Reset the index counter
  ruleIndex = 0;

  // Loop through each rule and update its input names
  rules.forEach(function (rule) {
    // Find all inputs and selects in this rule
    const inputs = rule.querySelectorAll("input, select");

    // Update each input's name attribute
    inputs.forEach(function (input) {
      const name = input.getAttribute("name");
      if (name) {
        // Replace the old index with the new one
        // Example: rules[2].name becomes rules[0].name
        input.setAttribute("name", name.replace(/\[\d+\]/, `[${ruleIndex}]`));
      }
    });

    // Update the IDs of the parameter containers and sub-elements
    rule.querySelectorAll("[id]").forEach((el) => {
      el.id = el.id.replace(/(-\d+)$/, `-${ruleIndex}`);
    });

    // Update the onchange attribute of the subject and target selects
    const subjectSelect = rule.querySelector('select[name*="subjectCode"]');
    if (subjectSelect) {
      subjectSelect.setAttribute(
        "onchange",
        `toggleSubjectParameter(this, ${ruleIndex})`,
      );
    }
    const targetSelect = rule.querySelector('select[name*="targetCode"]');
    if (targetSelect) {
      targetSelect.setAttribute(
        "onchange",
        `toggleTargetParameter(this, ${ruleIndex})`,
      );
    }

    // Move to next index
    ruleIndex++;
  });
}
/*
 * Toggle the visibility and required status of parameter inputs based on the selected rule code
 */

function toggleParameter(
  selectElement,
  containerId,
  paramSelectId,
  paramInputId,
) {
  const selectedOption = selectElement.options[selectElement.selectedIndex];
  const requiresParam = String(selectedOption.dataset.requiresParam) === "true";
  const paramContainer = document.getElementById(containerId);
  const sel = document.getElementById(paramSelectId);
  const inp = document.getElementById(paramInputId);

  if (!paramContainer) return;

  if (!requiresParam) {
    hideParameterControls(paramContainer, paramSelectId, paramInputId);
    return;
  }

  showParameterContainer(paramContainer);

  const currentValue = sel?.value || inp?.value || "";
  const allowedValues = getAllowedValues(selectedOption.value);

  if (allowedValues.length > 0) {
    showAllowedValuesSelect(sel, inp, allowedValues, currentValue);
  } else {
    showFreeValueInput(inp, sel, currentValue);
  }
}

function hideParameterControls(paramContainer, paramSelectId, paramInputId) {
  paramContainer.style.setProperty("display", "none", "important");
  const sel = document.getElementById(paramSelectId);
  const inp = document.getElementById(paramInputId);

  if (sel) {
    sel.disabled = true;
    sel.removeAttribute("required");
  }

  if (inp) {
    inp.disabled = true;
    inp.removeAttribute("required");
    inp.value = "";
  }
}

function showParameterContainer(paramContainer) {
  paramContainer.style.setProperty("display", "block", "important");
}

function getAllowedValues(code) {
  const rdEntry = (globalThis.ruleDefinitions || []).find(
    (d) => d.code === code,
  );
  return rdEntry && Array.isArray(rdEntry.allowedParams)
    ? rdEntry.allowedParams
    : [];
}

function showAllowedValuesSelect(sel, inp, allowedValues, currentValue) {
  if (sel) {
    sel.innerHTML =
      '<option value="">-- Valor --</option>' +
      [...allowedValues]
        .sort((a, b) => a - b)
        .map((value) => `<option value="${value}">${value}</option>`)
        .join("");
    sel.style.display = "";
    sel.disabled = false;
    sel.setAttribute("required", "required");
    sel.value = normalizeParameterValue(currentValue);
  }

  if (inp) {
    inp.style.display = "none";
    inp.disabled = true;
    inp.removeAttribute("required");
    inp.value = "";
  }
}

function normalizeParameterValue(value) {
  if (value === null || value === undefined || value === "") {
    return "";
  }

  const numericValue = Number(value);
  return Number.isNaN(numericValue) ? String(value) : String(numericValue);
}

function showFreeValueInput(inp, sel, currentValue) {
  if (inp) {
    inp.style.display = "";
    inp.disabled = false;
    inp.setAttribute("required", "required");
    inp.value = currentValue;
  }

  if (sel) {
    sel.style.display = "none";
    sel.disabled = true;
    sel.removeAttribute("required");
  }
}

// These functions are called when the subject or target select changes
function toggleSubjectParameter(selectElement, index) {
  toggleParameter(
    selectElement,
    `subject-container-${index}`,
    `subject-param-select-${index}`,
    `subject-param-input-${index}`,
  );
}

function toggleTargetParameter(selectElement, index) {
  toggleParameter(
    selectElement,
    `target-container-${index}`,
    `target-param-select-${index}`,
    `target-param-input-${index}`,
  );
}
