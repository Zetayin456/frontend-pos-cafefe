import TipoProducto from "../entity/tipo_producto.js";
import "./tarjeta-simple.js";

export default class TarjetaTipoProducto extends HTMLElement {
    constructor() { super(); this.attachShadow({ mode: "open" }); }

    connectedCallback() {
        if (this.shadowRoot.hasChildNodes()) return;
        const item = new TipoProducto({
            idTipoProducto: this.getAttribute("data-id"),
            nombre: this.getAttribute("nombre") ?? "Categoría",
            activo: this.getAttribute("activo") !== "false",
            observaciones: this.getAttribute("observaciones") ?? ""
        });
        const card = document.createElement("tarjeta-simple");
        const title = document.createElement("span"); title.slot = "titulo"; title.textContent = item.nombre;
        const description = document.createElement("p"); description.slot = "contenido";
        description.textContent = item.observaciones || "Explora los productos de esta categoría.";
        const badge = document.createElement("span"); badge.slot = "pie";
        badge.className = `badge${item.activo ? " active" : ""}`;
        badge.textContent = item.activo ? "Disponible" : "Inactiva";
        const select = document.createElement("button");
        select.type = "button";
        select.slot = "pie";
        select.className = "select-category";
        select.textContent = "Seleccionar categoría";
        card.append(title, description, badge);
        this.shadowRoot.append(card);
        card.append(select);
        select.addEventListener("click", (event) => {
            event.stopPropagation();
            this.shadowRoot.dispatchEvent(new CustomEvent("cafefe-category-request", {
                bubbles: true,
                composed: true,
                detail: { id: item.idTipoProducto, nombre: item.nombre }
            }));
        });
    }
}

if (!customElements.get("tarjeta-tipo-producto")) customElements.define("tarjeta-tipo-producto", TarjetaTipoProducto);
