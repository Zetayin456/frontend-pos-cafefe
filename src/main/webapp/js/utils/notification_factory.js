const TYPES = new Set(["error", "warning", "info"]);

export class NotificationMessage {
    constructor({ title, detail, type = "info", emitter = "Aplicación" }) {
        if (!title || !detail) throw new TypeError("La notificación requiere título y detalle.");
        if (!TYPES.has(type)) throw new TypeError(`Tipo de notificación no válido: ${type}`);
        this.title = String(title);
        this.detail = String(detail);
        this.type = type;
        this.emitter = String(emitter);
    }
}

export default class NotificationFactory {
    static create({ title, detail, type = "info", emitter = "Aplicación" }) {
        const notification = new NotificationMessage({ title, detail, type, emitter });
        return new CustomEvent("cafefe-notification", {
            bubbles: true,
            composed: true,
            detail: notification
        });
    }

    static emit(target, message) {
        if (!target?.dispatchEvent) throw new TypeError("El emisor debe ser un EventTarget.");
        return target.dispatchEvent(this.create(message));
    }
}
