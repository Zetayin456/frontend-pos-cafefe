/** Modelo de producto usado por las vistas del catálogo. */
export default class Producto {
    constructor({ idProducto = null, nombre = "", precioSugerido = 0, activo = false, comentarios = "" } = {}) {
        Object.assign(this, { idProducto, nombre, precioSugerido, activo: Boolean(activo), comentarios });
    }
}
