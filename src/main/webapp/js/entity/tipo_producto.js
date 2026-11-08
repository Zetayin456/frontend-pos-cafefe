/** Modelo de una categoría de producto devuelta por la API. */
export default class TipoProducto {
    constructor({ idTipoProducto = null, nombre = "", activo = false, observaciones = "" } = {}) {
        Object.assign(this, { idTipoProducto, nombre, activo: Boolean(activo), observaciones });
    }
}
