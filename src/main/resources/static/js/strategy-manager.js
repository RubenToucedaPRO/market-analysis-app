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
document.addEventListener('DOMContentLoaded', function() {
    // Count how many rules already exist on the page
    const existingRules = document.querySelectorAll('.rule-row');
    ruleIndex = existingRules.length;
    
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
    // Get the container where rules are displayed
    const container = document.getElementById('rules-container');
    
    // Create a new div element for the rule
    const newRule = document.createElement('div');
    newRule.className = 'rule-row card mb-3';
    
    // Set the HTML content of the new rule
    // Uses Bootstrap classes for styling
    newRule.innerHTML = `
        
    `;
    
    // Add the new rule to the container
    container.appendChild(newRule);
    
    // Increment the index for the next rule
    ruleIndex++;
}

/**
 * Remove a rule row from the form
 * @param {HTMLElement} button - The delete button that was clicked
 */
function removeRuleRow(button) {
    // Find the rule card that contains this button
    const ruleCard = button.closest('.rule-row');
    
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
    const rules = document.querySelectorAll('.rule-row');
    
    // Reset the index counter
    ruleIndex = 0;
    
    // Loop through each rule and update its input names
    rules.forEach(function(rule) {
        // Find all inputs and selects in this rule
        const inputs = rule.querySelectorAll('input, select');
        
        // Update each input's name attribute
        inputs.forEach(function(input) {
            const name = input.getAttribute('name');
            if (name) {
                // Replace the old index with the new one
                // Example: rules[2].name becomes rules[0].name
                input.setAttribute('name', name.replace(/\[\d+\]/, `[${ruleIndex}]`));
            }
        });
        
        // Move to next index
        ruleIndex++;
    });
}
