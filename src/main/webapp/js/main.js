import CajaService from "./control/caja_service.js";
import NotificationFactory from "./utils/notification_factory.js";
import "./pwa.js";
import "./componentes/tipo-producto-modal.js";

const cajaService = new CajaService();

document.addEventListener("DOMContentLoaded", () => {
    setupCajaLookup();
    setupCounterWorker();
});

function setupCajaLookup() {
    const button = document.querySelector("#load-cajas");
    const feedback = document.querySelector("#api-feedback");
    const body = document.querySelector("#cajas-body");
    const pageSize = document.querySelector("#page-size");
    if (!button || !feedback || !body) return;

    button.addEventListener("click", async () => {
        button.disabled = true;
        feedback.className = "feedback";
        feedback.textContent = "Consultando el servicio…";
        body.replaceChildren();
        try {
            const response = await cajaService.findRange(0, Number(pageSize.value));
            feedback.textContent = `${response.datos.length} registros recibidos · ${response.totales} en total.`;
            feedback.classList.add("is-success");
            NotificationFactory.emit(document, {
                title: "Consulta completada",
                detail: `Se recibieron ${response.datos.length} registros de cajas.`,
                type: "info",
                emitter: "Consulta de cajas"
            });
            if (response.datos.length === 0) {
                renderEmpty(body, "El servicio no devolvió cajas.");
            } else {
                for (const caja of response.datos) {
                    const row = document.createElement("tr");
                    appendCell(row, caja.nombre ?? "Sin nombre");
                    appendCell(row, caja.activo ? "Activa" : "Inactiva");
                    appendCell(row, caja.observaciones || "—");
                    body.append(row);
                }
            }
        } catch (error) {
            const detail = error instanceof Error ? error.message : error.mensaje;
            feedback.textContent = detail;
            feedback.classList.add("is-error");
            renderEmpty(body, "No se pudieron cargar los registros.");
            NotificationFactory.emit(document, {
                title: "Error al consultar cajas",
                detail,
                type: "error",
                emitter: "Consulta de cajas"
            });
        } finally {
            button.disabled = false;
        }
    });
}

function appendCell(row, value) {
    const cell = document.createElement("td");
    cell.textContent = String(value);
    row.append(cell);
}

function renderEmpty(body, message) {
    const row = document.createElement("tr");
    const cell = document.createElement("td");
    cell.colSpan = 3;
    cell.className = "empty-state";
    cell.textContent = message;
    row.append(cell);
    body.append(row);
}

function setupCounterWorker() {
    const result = document.querySelector("#resultado");
    const startInput = document.querySelector("#worker-start");
    const toggle = document.querySelector("#worker-toggle");
    const reset = document.querySelector("#worker-reset");
    const feedback = document.querySelector("#worker-feedback");
    if (!result || !startInput || !toggle || !reset || !feedback) return;

    if (!("Worker" in window)) {
        toggle.disabled = true;
        feedback.textContent = "Este navegador no dispone de Web Workers.";
        feedback.classList.add("is-error");
        return;
    }

    let worker = null;
    let running = false;
    const stop = () => {
        worker?.terminate();
        worker = null;
        running = false;
        toggle.textContent = "Iniciar";
        feedback.textContent = "Worker detenido.";
    };

    toggle.addEventListener("click", () => {
        if (running) {
            stop();
            return;
        }
        const initialValue = Number(startInput.value);
        if (!Number.isSafeInteger(initialValue) || initialValue < 0) {
            feedback.textContent = "Ingresa un número entero igual o mayor que cero.";
            feedback.className = "feedback is-error";
            NotificationFactory.emit(document, {
                title: "Valor no válido",
                detail: "Ingresa un número entero igual o mayor que cero.",
                type: "warning",
                emitter: "Contador Web Worker"
            });
            startInput.focus();
            return;
        }
        worker = new Worker("js/contador.js");
        worker.onmessage = ({ data }) => { result.textContent = String(data); };
        worker.onerror = () => {
            stop();
            feedback.textContent = "Ocurrió un error al ejecutar el Web Worker.";
            feedback.className = "feedback is-error";
            NotificationFactory.emit(document, {
                title: "Falló el contador",
                detail: "Ocurrió un error al ejecutar la tarea en segundo plano.",
                type: "error",
                emitter: "Contador Web Worker"
            });
        };
        worker.postMessage({ type: "start", value: initialValue });
        running = true;
        toggle.textContent = "Pausar";
        feedback.className = "feedback";
        feedback.textContent = "Contador activo en segundo plano.";
    });

    reset.addEventListener("click", () => {
        stop();
        startInput.value = "0";
        result.textContent = "0";
        feedback.className = "feedback";
        feedback.textContent = "Contador reiniciado.";
    });
}
