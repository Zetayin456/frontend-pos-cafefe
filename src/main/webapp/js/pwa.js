import "./componentes/toast-center.js";
import "./componentes/install-app-button.js";
import NotificationFactory from "./utils/notification_factory.js";

document.addEventListener("cafefe-notification", (event) => {
    document.querySelector("toast-center")?.show(event.detail);
});

// El padre común recibe el evento de la categoría y actualiza el panel hermano.
document.addEventListener("cafefe-category-selected", (event) => {
    const target = document.querySelector("#category-selection");
    if (target) target.textContent = `Categoría seleccionada: ${event.detail.nombre}.`;
});

if ("serviceWorker" in navigator && window.isSecureContext) {
    window.addEventListener("load", () => {
        const appRoot = new URL("./", document.baseURI);
        navigator.serviceWorker.register(new URL("service-worker.js", appRoot), { scope: appRoot.pathname })
                .catch(() => NotificationFactory.emit(document, {
                    title: "Modo sin conexión no disponible",
                    detail: "No fue posible preparar la caché local en este navegador.",
                    type: "warning",
                    emitter: "PWA"
                }));
    }, { once: true });
}
