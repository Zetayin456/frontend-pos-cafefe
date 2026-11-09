const CACHE_NAME = "cafefe-pos-shell-v1";
const APP_SHELL = [
    "./index.html",
    "./manifest.json",
    "./resources/css/principal.css",
    "./styles/tarjeta.css",
    "./js/main.js",
    "./js/pwa.js",
    "./js/promesas.js",
    "./js/contador.js",
    "./js/entity/default_error.js",
    "./js/entity/default_response.js",
    "./js/entity/tipo_producto.js",
    "./js/control/default_service.js",
    "./js/control/caja_service.js",
    "./js/control/tipo_producto_service.js",
    "./js/utils/notification_factory.js",
    "./js/componentes/tipo-producto-modal.js",
    "./js/componentes/tarjeta-tipo-producto.js",
    "./js/componentes/tarjeta-simple.js",
    "./js/componentes/toast-center.js",
    "./js/componentes/install-app-button.js",
    "./icons/cafefe-192.svg",
    "./icons/cafefe-512.svg",
    "./icons/cafefe-maskable-512.svg"
];

self.addEventListener("install", (event) => {
    event.waitUntil(caches.open(CACHE_NAME).then((cache) => cache.addAll(APP_SHELL)));
    self.skipWaiting();
});

self.addEventListener("activate", (event) => {
    event.waitUntil((async () => {
        const names = await caches.keys();
        await Promise.all(names.filter((name) => name.startsWith("cafefe-pos-") && name !== CACHE_NAME)
                .map((name) => caches.delete(name)));
        await self.clients.claim();
    })());
});

self.addEventListener("fetch", (event) => {
    const request = event.request;
    if (request.method !== "GET") return;
    const url = new URL(request.url);
    if (url.origin !== self.location.origin) return;

    if (request.mode === "navigate") {
        event.respondWith((async () => {
            try {
                const response = await fetch(request);
                if (response.ok && url.pathname.endsWith("/index.html")) {
                    const cache = await caches.open(CACHE_NAME);
                    await cache.put(request, response.clone());
                }
                return response;
            } catch {
                return await caches.match("./index.html")
                        ?? new Response("Cafefe POS está sin conexión. Abre el panel cuando la conexión vuelva.", {
                            status: 503, headers: { "Content-Type": "text/plain; charset=utf-8" }
                        });
            }
        })());
        return;
    }

    event.respondWith((async () => {
        const cached = await caches.match(request);
        if (cached) return cached;
        try {
            const response = await fetch(request);
            if (response.ok && response.type === "basic") {
                const cache = await caches.open(CACHE_NAME);
                await cache.put(request, response.clone());
            }
            return response;
        } catch {
            return new Response("Recurso no disponible sin conexión.", {
                status: 503, headers: { "Content-Type": "text/plain; charset=utf-8" }
            });
        }
    })());
});
