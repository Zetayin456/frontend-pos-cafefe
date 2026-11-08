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
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import sv.edu.ues.occ.ing.ppi115_2026.pos.cafefe.control.TipoProductoDAOInterface;
import sv.edu.ues.occ.ing.ppi115_2026.pos.cafefe.entity.TipoProducto;

/** Read-only paginated endpoint for the Web Components category catalog. */
@Path("v1/tipos-producto")
@Produces(MediaType.APPLICATION_JSON)
public class TipoProductoResource {

    @EJB
    private TipoProductoDAOInterface tipoProductoDAO;

    @GET
    public Response findRange(@QueryParam("first") @DefaultValue("0") int first,
            @QueryParam("max") @DefaultValue("20") int max) {
        if (first < 0 || max < 1) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("first debe ser ≥ 0 y max debe ser ≥ 1")
                    .type(MediaType.TEXT_PLAIN).build();
        }
        List<TipoProductoDTO> tipos = tipoProductoDAO.findRange(first, max).stream()
                .map(TipoProductoDTO::from).collect(Collectors.toList());
        return Response.ok(new GenericEntity<List<TipoProductoDTO>>(tipos) { })
                .header("total-registros", tipoProductoDAO.count()).build();
    }

    /** Flat DTO prevents serializing the lazy product relationship. */
    public static class TipoProductoDTO {
        private UUID idTipoProducto;
        private String nombre;
        private Boolean activo;
        private String observaciones;

        public TipoProductoDTO() { }

        static TipoProductoDTO from(TipoProducto tipo) {
            TipoProductoDTO dto = new TipoProductoDTO();
            dto.idTipoProducto = tipo.getIdTipoProducto();
            dto.nombre = tipo.getNombre();
            dto.activo = tipo.getActivo();
            dto.observaciones = tipo.getObservaciones();
            return dto;
        }

        public UUID getIdTipoProducto() { return idTipoProducto; }
        public String getNombre() { return nombre; }
        public Boolean getActivo() { return activo; }
        public String getObservaciones() { return observaciones; }
    }
}
