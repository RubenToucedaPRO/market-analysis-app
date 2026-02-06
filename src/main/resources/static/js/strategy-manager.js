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

  // Clonas el contenido que Thymeleaf ya procesó y dejó en el template
  const clone = template.content.cloneNode(true);
  const newIndex = document.querySelectorAll(".rule-row").length;

  // Reemplazas el índice 999 por el real
  clone.querySelectorAll("[name]").forEach((el) => {
    el.name = el.name.replace("999", newIndex);
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

/**
 * Toggle visibility of Subject Parameter field based on selected indicator
 * @param {HTMLSelectElement} selectElement - The select element that was changed
 * @param {number} index - The index of the rule row
 */
function toggleSubjectParameter(selectElement, index) {
  const selectedOption = selectElement.options[selectElement.selectedIndex];
  const requiresParam = selectedOption.dataset.requiresParam === "true";
  const paramContainer = document.getElementById(`rule-container-${index}`);

  if (paramContainer) {
    paramContainer.style.display = requiresParam ? "" : "none";

    // Toggle required attribute on the input field
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
