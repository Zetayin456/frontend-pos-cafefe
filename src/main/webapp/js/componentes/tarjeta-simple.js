const stylesheet = new URL("../../styles/tarjeta.css", import.meta.url).href;

export default class TarjetaSimple extends HTMLElement {
    constructor() { super(); this.attachShadow({ mode: "open" }); }

    connectedCallback() {
        if (this.shadowRoot.hasChildNodes()) return;
        const link = document.createElement("link");
        link.rel = "stylesheet";
        link.href = stylesheet;
        const template = document.createElement("template");
        template.innerHTML = `<article class="card"><header class="header"><slot name="titulo">Categoría</slot></header><div class="body"><slot name="contenido"></slot></div><footer class="footer"><slot name="pie"></slot></footer></article>`;
        this.shadowRoot.append(link, template.content.cloneNode(true));
    }
}

if (!customElements.get("tarjeta-simple")) customElements.define("tarjeta-simple", TarjetaSimple);
