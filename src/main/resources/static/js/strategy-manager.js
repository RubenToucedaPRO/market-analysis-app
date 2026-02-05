/**
 * Gestiona la adición y eliminación dinámica de filas de reglas en el formulario de estrategias.
 */

// Inicializa el índice de la regla basándose en las filas existentes
let ruleIndex = document.querySelectorAll(".rule-row").length;

/**
 * Añade una nueva fila de regla al formulario.
 */
function addRuleRow() {
  const container = document.getElementById("rules-container");

  // Si no hay filas existentes, necesitamos una "plantilla"
  let rowToClone;
  if (ruleIndex === 0) {
    // Crear una plantilla básica si es la primera regla
    rowToClone = document.createElement("div");
    rowToClone.innerHTML = `
            <div class="col-span-12 md:col-span-4 space-y-1">
                <label class="text-[10px] font-bold uppercase text-slate-400">Subject Indicator</label>
                <select name="rules[0].subjectCode" class="w-full bg-transparent border-none p-0 focus:ring-0 text-sm font-semibold">
                    <option value="PRICE">Market Price</option>
                    <option value="SMA">Simple Moving Average</option>
                    <option value="RSI">Relative Strength Index</option>
                </select>
            </div>
            <div class="col-span-6 md:col-span-2 space-y-1">
                <label class="text-[10px] font-bold uppercase text-slate-400">Operator</label>
                <select name="rules[0].operator" class="w-full bg-transparent border-none p-0 focus:ring-0 text-sm font-bold text-primary text-center">
                    <option value=">">GREATER THAN</option>
                    <option value="<">LESS THAN</option>
                    <option value="=">EQUAL TO</option>
                </select>
            </div>
            <div class="col-span-6 md:col-span-4 space-y-1">
                <label class="text-[10px] font-bold uppercase text-slate-400">Target Value / Indicator</label>
                <input type="number" step="0.01" name="rules[0].targetParam" class="w-full bg-transparent border-none p-0 focus:ring-0 text-sm font-semibold" placeholder="0.00" />
            </div>
             <div class="col-span-1 flex items-center justify-end">
                <button type="button" onclick="removeRuleRow(this)" class="text-red-500 hover:text-red-700 opacity-100 transition-opacity">
                    <span class="material-icons-round text-xl">delete_forever</span>
                </button>
            </div>
        `;
    // Ajustar la clase principal del div que contiene todo
    const newRuleDiv = document.createElement("div");
    newRuleDiv.classList.add(
      "rule-row",
      "group",
      "grid",
      "grid-cols-12",
      "gap-4",
      "p-4",
      "bg-slate-50",
      "dark:bg-slate-900/50",
      "border",
      "border-surface-border-light",
      "dark:border-surface-border-dark",
      "rounded-xl",
      "transition-all",
      "hover:border-primary/50",
    );
    newRuleDiv.appendChild(rowToClone);
    rowToClone = newRuleDiv;
  } else {
    // Clonar la última fila existente
    const rows = document.querySelectorAll(".rule-row");
    rowToClone = rows[rows.length - 1].cloneNode(true);
  }

  // Limpiamos y actualizamos los atributos 'name' e 'id' de los inputs clonados
  rowToClone.querySelectorAll("select, input").forEach((input) => {
    const name = input.getAttribute("name");
    if (name) {
      input.setAttribute("name", name.replace(/\[\d+\]/, `[${ruleIndex}]`));
      input.value = ""; // Limpiar valores para la nueva fila
    }
  });

  container.appendChild(rowToClone);
  ruleIndex++;
}

/**
 * Elimina una fila de regla del formulario.
 * @param {HTMLElement} button El botón que activó la eliminación.
 */
function removeRuleRow(button) {
  const rowToRemove = button.closest(".rule-row");
  if (rowToRemove) {
    rowToRemove.remove();
    // Reindexar todas las filas restantes para mantener la secuencia correcta para Thymeleaf
    reindexRules();
  }
}

/**
 * Reindexa los atributos 'name' de las filas de reglas después de una eliminación.
 */
function reindexRules() {
  const rows = document.querySelectorAll(".rule-row");
  ruleIndex = 0; // Resetear el contador de índice
  rows.forEach((row) => {
    row.querySelectorAll("select, input").forEach((input) => {
      const name = input.getAttribute("name");
      if (name) {
        input.setAttribute("name", name.replace(/\[\d+\]/, `[${ruleIndex}]`));
      }
    });
    ruleIndex++;
  });
}

// Asegúrate de que al cargar la página, si hay 0 reglas, se añada una automáticamente.
// Esto es para que el formulario no esté vacío inicialmente.
document.addEventListener("DOMContentLoaded", () => {
  if (
    document.querySelectorAll(".rule-row").length === 0 &&
    document.getElementById("rules-container")
  ) {
    addRuleRow();
  }
});
