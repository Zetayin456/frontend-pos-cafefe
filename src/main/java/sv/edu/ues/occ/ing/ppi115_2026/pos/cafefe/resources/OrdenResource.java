package sv.edu.ues.occ.ing.ppi115_2026.pos.cafefe.resources;

import jakarta.ejb.EJB;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.GenericEntity;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import sv.edu.ues.occ.ing.ppi115_2026.pos.cafefe.control.OrdenDAOInterface;
import sv.edu.ues.occ.ing.ppi115_2026.pos.cafefe.entity.Orden;

@Path("v1/ordenes")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class OrdenResource {

    @EJB
    private OrdenDAOInterface ordenDAO;

    @GET
    public Response findRange(@QueryParam("first") @DefaultValue("0") int first,
                              @QueryParam("max") @DefaultValue("50") int max) {
        List<OrdenDTO> ordenes = ordenDAO.findRange(first, max).stream()
                .map(OrdenDTO::from)
                .collect(Collectors.toList());

        return Response.ok(new GenericEntity<List<OrdenDTO>>(ordenes) {})
                .header("total-registros", ordenDAO.count())
                .build();
    }

    @POST
    public Response create(Orden entity) {
        if (entity == null) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        ordenDAO.create(entity);
        return Response.status(Response.Status.CREATED).entity(OrdenDTO.from(entity)).build();
    }

    @PUT
    @Path("/{id}")
    public Response update(@PathParam("id") UUID id, Orden entity) {
        Orden actual = ordenDAO.findById(id);
        if (actual == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        ordenDAO.edit(entity);
        return Response.ok(OrdenDTO.from(entity)).build();
    }

    public static class OrdenDTO {
        private UUID idOrden;
        private Date fecha;
        private String cliente;
        private String estado;
        private String observaciones;

        public static OrdenDTO from(Orden o) {
            OrdenDTO dto = new OrdenDTO();
            dto.idOrden = o.getIdOrden();
            dto.fecha = o.getFecha();
            dto.cliente = o.getCliente();
            dto.estado = o.getEstado();
            dto.observaciones = o.getObservaciones();
            return dto;
        }

        public UUID getIdOrden() { return idOrden; }
        public Date getFecha() { return fecha; }
        public String getCliente() { return cliente; }
        public String getEstado() { return estado; }
        public String getObservaciones() { return observaciones; }
    }
}