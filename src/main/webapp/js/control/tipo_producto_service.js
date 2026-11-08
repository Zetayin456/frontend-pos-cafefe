import DefaultService from "./default_service.js";
import TipoProducto from "../entity/tipo_producto.js";

export default class TipoProductoService extends DefaultService {
    constructor(baseUrl) { super(baseUrl); this._URL = `${this.BASE_URL}tipos-producto`; }

    async findRange(first = 0, max = 20) {
        const response = await super.findRange(first, max);
        response.datos = response.datos.map((item) => new TipoProducto(item));
        return response;
    }
}
