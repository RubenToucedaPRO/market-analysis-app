document.querySelectorAll(".ai-form").forEach((form) => {
  form.addEventListener("submit", function () {
    const btn = this.querySelector(".btn-ai");
    const content = btn.querySelector(".btn-content");
    const spinner = btn.querySelector(".spinner-border");

    // Estado de carga
    btn.disabled = true;
    content.classList.add("d-none");
    spinner.classList.remove("d-none");
  });
});
