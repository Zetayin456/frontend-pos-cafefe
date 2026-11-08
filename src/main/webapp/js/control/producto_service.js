import DefaultService from "./default_service.js";
import Producto from "../entity/producto.js";

export default class ProductoService extends DefaultService {
    constructor(baseUrl) { super(baseUrl); this._URL = `${this.BASE_URL}productos`; }

    async findRange(first = 0, max = 20) {
        const response = await super.findRange(first, max);
        response.datos = response.datos.map((item) => new Producto(item));
        return response;
    }
}
