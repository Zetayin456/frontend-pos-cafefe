export default class ToastCenter extends HTMLElement {
    constructor() {
        super();
        this.attachShadow({ mode: "open" });
        this.hideTimer = null;
    }

    connectedCallback() {
        if (this.shadowRoot.hasChildNodes()) return;
        this.shadowRoot.innerHTML = `
            <style>
                :host { position: fixed; z-index: 10000; top: 1rem; right: 1rem; width: min(24rem, calc(100vw - 2rem)); }
                .toast { display: grid; grid-template-columns: 1fr auto; gap: .2rem .8rem; padding: .9rem 1rem; border: 1px solid #d6c7b2; border-left: .35rem solid #4a2c2a; border-radius: .7rem; background: #fffdf9; color: #302923; box-shadow: 0 8px 30px #30292333; font: 14px/1.45 system-ui, sans-serif; }
                .toast[hidden] { display: none; }
                .toast[data-type="error"] { border-left-color: #b33b32; }
                .toast[data-type="warning"] { border-left-color: #bf7c22; }
                .toast[data-type="info"] { border-left-color: #497854; }
                strong { font-size: .9rem; }
                p { grid-column: 1 / -1; margin: 0; }
                small { color: #827970; }
                button { grid-column: 2; grid-row: 1; border: 0; background: transparent; color: inherit; font-size: 1.2rem; cursor: pointer; }
                button:focus-visible { outline: 2px solid #8b5a2b; outline-offset: 2px; }
            </style>
            <section class="toast" role="status" aria-live="polite" hidden>
                <strong></strong><button type="button" aria-label="Cerrar notificación">×</button>
                <p></p><small></small>
            </section>`;
        const toast = this.shadowRoot.querySelector(".toast");
        this.shadowRoot.querySelector("button").addEventListener("click", (event) => {
            event.stopPropagation();
            this.hide();
        });
        toast.addEventListener("click", (event) => event.stopPropagation());
    }

    show(message) {
        if (!message || !this.shadowRoot) return;
        const toast = this.shadowRoot.querySelector(".toast");
        if (!toast) return;
        toast.dataset.type = message.type ?? "info";
        toast.setAttribute("role", message.type === "error" ? "alert" : "status");
        toast.querySelector("strong").textContent = message.title ?? "Aviso";
        toast.querySelector("p").textContent = message.detail ?? "";
        toast.querySelector("small").textContent = message.emitter ? `Desde: ${message.emitter}` : "";
        toast.hidden = false;
        clearTimeout(this.hideTimer);
        this.hideTimer = setTimeout(() => this.hide(), 6000);
    }

    hide() {
        const toast = this.shadowRoot?.querySelector(".toast");
        if (toast) toast.hidden = true;
        clearTimeout(this.hideTimer);
    }
}

if (!customElements.get("toast-center")) customElements.define("toast-center", ToastCenter);
