package sv.edu.ues.occ.ing.ppi115_2026.pos.cafefe.resources;

import jakarta.ejb.EJB;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.GenericEntity;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import sv.edu.ues.occ.ing.ppi115_2026.pos.cafefe.control.PagoDAOInterface;
import sv.edu.ues.occ.ing.ppi115_2026.pos.cafefe.entity.Pago;

@Path("v1/pagos")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PagoResource {

    @EJB
    private PagoDAOInterface pagoDAO;

    @GET
    public Response findRange(@QueryParam("first") @DefaultValue("0") int first,
                              @QueryParam("max") @DefaultValue("50") int max) {
        List<PagoDTO> pagos = pagoDAO.findRange(first, max).stream()
                .map(PagoDTO::from)
                .collect(Collectors.toList());

        return Response.ok(new GenericEntity<List<PagoDTO>>(pagos) {})
                .header("total-registros", pagoDAO.count())
                .build();
    }

    @POST
    public Response create(Pago entity) {
        if (entity == null) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        pagoDAO.create(entity);
        return Response.status(Response.Status.CREATED).entity(PagoDTO.from(entity)).build();
    }

    public static class PagoDTO {
        private UUID idPago;
        private BigDecimal monto;
        private Date fecha;
        private String metodoPago;

        public static PagoDTO from(Pago p) {
            PagoDTO dto = new PagoDTO();
            dto.idPago = p.getIdPago();
            dto.monto = p.getMonto();
            dto.fecha = p.getFecha();
            dto.metodoPago = p.getMetodoPago();
            return dto;
        }

        public UUID getIdPago() { return idPago; }
        public BigDecimal getMonto() { return monto; }
        public Date getFecha() { return fecha; }
        public String getMetodoPago() { return metodoPago; }
    }
}