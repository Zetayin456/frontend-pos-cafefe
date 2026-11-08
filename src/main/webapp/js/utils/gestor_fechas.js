/** Helpers de fecha para mostrar valores locales sin mutar la fecha recibida. */
export function formatearFecha(fecha, opciones = {}) {
    const valor = fecha instanceof Date ? fecha : new Date(fecha);
    if (Number.isNaN(valor.getTime())) throw new TypeError("La fecha proporcionada no es válida.");
    return new Intl.DateTimeFormat("es-SV", { dateStyle: "medium", ...opciones }).format(valor);
}

export function fechaISO(fecha = new Date()) {
    const valor = fecha instanceof Date ? fecha : new Date(fecha);
    if (Number.isNaN(valor.getTime())) throw new TypeError("La fecha proporcionada no es válida.");
    return new Date(valor.getTime() - valor.getTimezoneOffset() * 60000).toISOString().slice(0, 10);
}
