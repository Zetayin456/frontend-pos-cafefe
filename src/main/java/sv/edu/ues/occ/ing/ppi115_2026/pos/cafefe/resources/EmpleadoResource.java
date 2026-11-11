package sv.edu.ues.occ.ing.ppi115_2026.pos.cafefe.resources;

import jakarta.ejb.EJB;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.GenericEntity;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import sv.edu.ues.occ.ing.ppi115_2026.pos.cafefe.control.EmpleadoDAOInterface;
import sv.edu.ues.occ.ing.ppi115_2026.pos.cafefe.entity.Empleado;

@Path("v1/empleados")
@Produces(MediaType.APPLICATION_JSON)
public class EmpleadoResource {

    @EJB
    private EmpleadoDAOInterface empleadoDAO;

    @GET
    public Response findRange(@QueryParam("first") @DefaultValue("0") int first,
                              @QueryParam("max") @DefaultValue("50") int max) {
        if (first < 0 || max < 1) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("first debe ser ≥ 0 y max debe ser ≥ 1")
                    .type(MediaType.TEXT_PLAIN)
                    .build();
        }

        List<EmpleadoDTO> lista = empleadoDAO.findRange(first, max).stream()
                .map(EmpleadoDTO::from)
                .collect(Collectors.toList());

        return Response.ok(new GenericEntity<List<EmpleadoDTO>>(lista) {})
                .header("total-registros", empleadoDAO.count())
                .build();
    }

    @GET
    @Path("/{id}")
    public Response findById(@PathParam("id") UUID id) {
        Empleado emp = empleadoDAO.find(id);
        if (emp == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(EmpleadoDTO.from(emp)).build();
    }

    public static class EmpleadoDTO {
        private UUID idEmpleado;
        private String nombre;
        private String apellido;
        private String usuario;
        private Boolean activo;

        public static EmpleadoDTO from(Empleado e) {
            EmpleadoDTO dto = new EmpleadoDTO();
            dto.idEmpleado = e.getIdEmpleado();
            dto.nombre = e.getNombre();
            dto.apellido = e.getApellido();
            dto.activo = e.getActivo();
            return dto;
        }

        public UUID getIdEmpleado() { return idEmpleado; }
        public String getNombre() { return nombre; }
        public String getApellido() { return apellido; }
        public Boolean getActivo() { return activo; }
    }
}