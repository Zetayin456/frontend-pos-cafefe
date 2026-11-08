package sv.edu.ues.occ.ing.ppi115_2026.pos.cafefe.resources;

import jakarta.ejb.EJB;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.GenericEntity;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import sv.edu.ues.occ.ing.ppi115_2026.pos.cafefe.control.ProductoDAOInterface;
import sv.edu.ues.occ.ing.ppi115_2026.pos.cafefe.entity.Producto;

/** Read-only paginated endpoint for the product catalog. */
@Path("v1/productos")
@Produces(MediaType.APPLICATION_JSON)
public class ProductoResource {

    @EJB
    private ProductoDAOInterface productoDAO;

    @GET
    public Response findRange(@QueryParam("first") @DefaultValue("0") int first,
            @QueryParam("max") @DefaultValue("20") int max) {
        if (first < 0 || max < 1) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("first debe ser ≥ 0 y max debe ser ≥ 1")
                    .type(MediaType.TEXT_PLAIN).build();
        }
        List<ProductoDTO> productos = productoDAO.findRange(first, max).stream()
                .map(ProductoDTO::from).collect(Collectors.toList());
        return Response.ok(new GenericEntity<List<ProductoDTO>>(productos) { })
                .header("total-registros", productoDAO.count()).build();
    }

    /** Flat DTO prevents serializing lazy product relationships. */
    public static class ProductoDTO {
        private UUID idProducto;
        private String nombre;
        private BigDecimal precioSugerido;
        private Boolean activo;
        private String comentarios;

        public ProductoDTO() { }

        static ProductoDTO from(Producto producto) {
            ProductoDTO dto = new ProductoDTO();
            dto.idProducto = producto.getIdProducto();
            dto.nombre = producto.getNombre();
            dto.precioSugerido = producto.getPrecioSugerido();
            dto.activo = producto.getActivo();
            dto.comentarios = producto.getComentarios();
            return dto;
        }

        public UUID getIdProducto() { return idProducto; }
        public String getNombre() { return nombre; }
        public BigDecimal getPrecioSugerido() { return precioSugerido; }
        public Boolean getActivo() { return activo; }
        public String getComentarios() { return comentarios; }
    }
}
