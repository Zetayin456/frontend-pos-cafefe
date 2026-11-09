import NotificationFactory from "../utils/notification_factory.js";

export default class InstallAppButton extends HTMLElement {
    constructor() {
        super();
        this.attachShadow({ mode: "open" });
        this.installPrompt = null;
        this.onBeforeInstall = this.onBeforeInstall.bind(this);
    }

    connectedCallback() {
        if (!this.shadowRoot.hasChildNodes()) {
            this.shadowRoot.innerHTML = `<style>
                button { padding: .5rem .8rem; border: 1px solid #d6c7b2; border-radius: 99rem; color: #4a2c2a; background: #fffdf9; font: 650 .8rem system-ui, sans-serif; cursor: pointer; }
                button:hover { background: #f4e2d5; }
                button:focus-visible { outline: 2px solid #8b5a2b; outline-offset: 2px; }
                button[hidden] { display:none; }
            </style><button type="button" hidden>Instalar Cafefe POS</button>`;
            this.shadowRoot.querySelector("button").addEventListener("click", () => this.install());
        }
        window.addEventListener("beforeinstallprompt", this.onBeforeInstall);
    }

    disconnectedCallback() {
        window.removeEventListener("beforeinstallprompt", this.onBeforeInstall);
    }

    onBeforeInstall(event) {
        event.preventDefault();
        this.installPrompt = event;
        const button = this.shadowRoot.querySelector("button");
        if (button) button.hidden = false;
    }

    async install() {
        if (!this.installPrompt) return;
        const promptEvent = this.installPrompt;
        this.installPrompt = null;
        this.shadowRoot.querySelector("button").hidden = true;
        try {
            await promptEvent.prompt();
            const choice = await promptEvent.userChoice;
            NotificationFactory.emit(this.shadowRoot, {
                title: choice.outcome === "accepted" ? "Instalación iniciada" : "Instalación pospuesta",
                detail: choice.outcome === "accepted" ? "Cafefe POS estará disponible desde tu dispositivo." : "Puedes instalar Cafefe POS más tarde desde este botón cuando vuelva a aparecer.",
                type: "info",
                emitter: "Instalación PWA"
            });
        } catch {
            NotificationFactory.emit(this.shadowRoot, {
                title: "No se pudo iniciar la instalación",
                detail: "Inténtalo de nuevo desde el navegador.",
                type: "error",
                emitter: "Instalación PWA"
            });
        }
    }
}

if (!customElements.get("install-app-button")) customElements.define("install-app-button", InstallAppButton);
