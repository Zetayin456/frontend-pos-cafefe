import TipoProductoService from "../control/tipo_producto_service.js";
import NotificationFactory from "../utils/notification_factory.js";
import "./tarjeta-tipo-producto.js";

export default class TipoProductoModal extends HTMLElement {
    constructor() { super(); this.attachShadow({ mode: "open" }); this.service = new TipoProductoService(); }

    connectedCallback() {
        if (this.shadowRoot.hasChildNodes()) return;
        const link = document.createElement("link"); link.rel = "stylesheet";
        link.href = new URL("../../styles/tarjeta.css", import.meta.url).href;
        const template = document.createElement("template");
        template.innerHTML = `<section class="catalog"><div class="catalog-heading"><div><p class="eyebrow">CATÁLOGO</p><h2>Tipos de producto</h2></div><button type="button" class="refresh">Actualizar</button></div><p class="message" role="status" aria-live="polite">Cargando categorías…</p><p class="selected" aria-live="polite">Ninguna categoría seleccionada.</p><div class="cards" aria-label="Categorías de producto"></div></section>`;
        this.shadowRoot.append(link, template.content.cloneNode(true));
        this.shadowRoot.querySelector(".refresh").addEventListener("click", () => this.load());
        this.shadowRoot.addEventListener("cafefe-category-request", (event) => {
            // El contenedor común detiene la burbuja del hijo y coordina al componente hermano.
            event.stopPropagation();
            const selection = event.detail;
            this.shadowRoot.querySelector(".selected").textContent = `Seleccionada: ${selection.nombre}`;
            this.shadowRoot.dispatchEvent(new CustomEvent("cafefe-category-selected", {
                bubbles: true,
                composed: true,
                detail: selection
            }));
        });
        this.load();
    }

    async load() {
        const message = this.shadowRoot.querySelector(".message");
        const cards = this.shadowRoot.querySelector(".cards");
        if (!message || !cards) return;
        message.textContent = "Cargando categorías…"; message.className = "message";
        cards.replaceChildren();
        try {
            const response = await this.service.findRange(0, 50);
            const categories = response.datos.filter((item) => item.activo);
            if (!categories.length) { message.textContent = "No hay categorías activas para mostrar."; return; }
            categories.forEach((item) => {
                const card = document.createElement("tarjeta-tipo-producto");
                card.setAttribute("data-id", item.idTipoProducto ?? "");
                card.setAttribute("nombre", item.nombre ?? "Categoría");
                card.setAttribute("activo", String(item.activo));
                card.setAttribute("observaciones", item.observaciones ?? "");
                cards.append(card);
            });
            message.textContent = `${categories.length} categorías disponibles.`;
        } catch (error) {
            const detail = error?.message ?? "No se pudieron cargar las categorías.";
            message.textContent = detail;
            message.classList.add("error");
            NotificationFactory.emit(this.shadowRoot, {
                title: "No se pudieron cargar las categorías",
                detail,
                type: "error",
                emitter: "Catálogo de tipos de producto"
            });
        }
    }
}

if (!customElements.get("tipo-producto-modal")) customElements.define("tipo-producto-modal", TipoProductoModal);
