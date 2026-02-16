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

    // Update the ID of the parameter container
    const paramContainer = rule.querySelector('[id^="rule-container-"]');
    if (paramContainer) {
      paramContainer.id = `rule-container-${ruleIndex}`;
    }

    // Update the onchange attribute of the select
    const select = rule.querySelector('select[name*="subjectCode"]');
    if (select) {
      select.setAttribute(
        "onchange",
        `toggleSubjectParameter(this, ${ruleIndex})`,
      );
    }

    // Move to next index
    ruleIndex++;
  });
}

function toggleParameter(selectElement, containerId) {
  const selectedOption = selectElement.options[selectElement.selectedIndex];
  // Convertir explícitamente a string para comparar, manejando undefined
  const requiresParam = String(selectedOption.dataset.requiresParam) === "true";
  const paramContainer = document.getElementById(containerId);

  if (paramContainer) {
    paramContainer.style.setProperty(
      "display",
      requiresParam ? "block" : "none",
      "important",
    );

    const input = paramContainer.querySelector("input");
    if (input) {
      if (requiresParam) {
        input.setAttribute("required", "required");
      } else {
        input.removeAttribute("required");
        input.value = ""; // Limpiar valor si se oculta para evitar basura en el POST
      }
    }
  }
}

// Simplificación de tus funciones:
function toggleSubjectParameter(selectElement, index) {
  toggleParameter(selectElement, `subject-container-${index}`);
}

function toggleTargetParameter(selectElement, index) {
  toggleParameter(selectElement, `target-container-${index}`);
}
