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
    const select = rule.querySelector('select[name*="subjectCode"]');
    if (select) {
      const selectedOption = select.options[select.selectedIndex];
      const requiresParam = selectedOption.dataset.requiresParam === "true";
      const paramContainer = rule.querySelector(`[id^="rule-container-"]`);

      if (paramContainer) {
        paramContainer.style.display = requiresParam ? "" : "none";

        // Set required attribute correctly on initialization
        const input = paramContainer.querySelector("input");
        if (input) {
          if (requiresParam) {
            input.setAttribute("required", "required");
          } else {
            input.removeAttribute("required");
          }
        }
      }
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
    rule.querySelectorAll('[id]').forEach((el) => {
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

function toggleParameter(selectElement, containerId, paramSelectId, paramInputId) {
  const selectedOption = selectElement.options[selectElement.selectedIndex];
  // Convertir explícitamente a string para comparar, manejando undefined
  const requiresParam = String(selectedOption.dataset.requiresParam) === "true";
  const paramContainer = document.getElementById(containerId);

  if (!paramContainer) return;

  if (!requiresParam) {
    paramContainer.style.setProperty("display", "none", "important");
    // Disable and clear both controls so they are not submitted
    const sel = document.getElementById(paramSelectId);
    const inp = document.getElementById(paramInputId);
    if (sel) { sel.disabled = true; sel.removeAttribute("required"); }
    if (inp) { inp.disabled = true; inp.removeAttribute("required"); inp.value = ""; }
    return;
  }

  paramContainer.style.setProperty("display", "block", "important");

  // Determine whether to show a constrained-value select or a free number input.
  // allowedParams comes from the JSON-serialised ruleDefinitions array injected by
  // Thymeleaf – this avoids fragile Set.toString() parsing of data attributes.
  const code = selectedOption.value;
  const rdEntry = (globalThis.ruleDefinitions || []).find((d) => d.code === code);
  const allowedValues = rdEntry && Array.isArray(rdEntry.allowedParams)
    ? rdEntry.allowedParams
    : [];

  const sel = document.getElementById(paramSelectId);
  const inp = document.getElementById(paramInputId);

  if (allowedValues.length > 0) {
    // Populate the select with the allowed values and show it
    if (sel) {
      sel.innerHTML =
        '<option value="">-- Valor --</option>' +
        [...allowedValues]
          .sort((a, b) => a - b)
          .map((v) => `<option value="${v}">${v}</option>`)
          .join("");
      sel.style.display = "";
      sel.disabled = false;
      sel.setAttribute("required", "required");
    }
    if (inp) {
      inp.style.display = "none";
      inp.disabled = true;
      inp.removeAttribute("required");
      inp.value = "";
    }
  } else {
    // Free-value number input (anyParamAllowed indicators like CONSTANT/VALUE)
    if (inp) {
      inp.style.display = "";
      inp.disabled = false;
      inp.setAttribute("required", "required");
    }
    if (sel) {
      sel.style.display = "none";
      sel.disabled = true;
      sel.removeAttribute("required");
    }
  }
}

// Simplificación de tus funciones:
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
