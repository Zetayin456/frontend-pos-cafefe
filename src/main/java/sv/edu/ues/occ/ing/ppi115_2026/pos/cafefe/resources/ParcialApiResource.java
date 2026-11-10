package sv.edu.ues.occ.ing.ppi115_2026.pos.cafefe.resources;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.math.BigDecimal;
import java.util.*;
import sv.edu.ues.occ.ing.ppi115_2026.pos.cafefe.entity.*;

/** REST contracts backed only by columns in the official PostgreSQL DDL. */
@Stateless
@Path("/")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ParcialApiResource {
    @PersistenceContext(unitName = "cafefePU") private EntityManager em;

    public static class LoginRequest { public String nombre; public String usuario; public String contrasena; public String password; }
    public static class OrderRequest { public String cliente; public String nombreCliente; public List<OrderLine> productos; }
    public static class OrderLine { public UUID idProducto; public Integer cantidad; public String observaciones; public String comentarios; }
    public static class InvoiceRequest { public UUID idOrden; public UUID idCaja; public UUID idEmpleadoRol; public String cliente; public String observaciones; }
    public static class PaymentRequest { public UUID idFactura; public List<PaymentLine> detalles; public String observaciones; }
    public static class PaymentLine { public String tipoPago; public BigDecimal monto; public String referenciaExterna; public String observaciones; }

    /** The official schema has no password column; per requirement the employee UUID is the password. */
    @POST @Path("empleado/login")
    public Response login(LoginRequest in) {
        String nombre = in == null ? null : (in.nombre == null ? in.usuario : in.nombre);
        String password = in == null ? null : (in.contrasena == null ? in.password : in.contrasena);
        UUID employeeId;
        try { employeeId = UUID.fromString(password); }
        catch (Exception ex) { return error(401, "Credenciales inválidas"); }
        List<Empleado> found = em.createQuery("SELECT e FROM Empleado e WHERE LOWER(e.nombre)=LOWER(:nombre) AND e.idEmpleado=:id AND e.activo=TRUE", Empleado.class)
                .setParameter("nombre", nombre == null ? "" : nombre.trim()).setParameter("id", employeeId).setMaxResults(1).getResultList();
        if (found.isEmpty()) return error(401, "Credenciales inválidas");
        Empleado e = found.get(0);
        List<Map<String,Object>> roles = em.createQuery("SELECT er FROM EmpleadoRol er JOIN FETCH er.idRol WHERE er.idEmpleado.idEmpleado=:id AND er.activo=TRUE AND er.idRol.activo=TRUE", EmpleadoRol.class)
                .setParameter("id", employeeId).getResultList().stream()
                .map(er -> Map.<String,Object>of("idEmpleadoRol", er.getIdEmpleadoRol(), "idRol", er.getIdRol().getIdRol(), "nombre", er.getIdRol().getNombre() == null ? "" : er.getIdRol().getNombre())).toList();
        if (roles.isEmpty()) return error(401, "Empleado sin roles activos");
        Map<String,Object> out = new LinkedHashMap<>(); out.put("idEmpleado", e.getIdEmpleado()); out.put("nombre", e.getNombre()); out.put("apellido", e.getApellido()); out.put("activo", e.getActivo()); out.put("roles", roles);
        return Response.ok(out).build();
    }

    /** Public orders use the preconfigured active virtual waiter EmployeeRole. */
    @POST @Path("orden")
    public Response crearOrden(OrderRequest in) {
        if (in == null || in.productos == null || in.productos.isEmpty()) return error(400, "productos debe contener al menos un elemento");
        EmpleadoRol virtual = em.createQuery("SELECT er FROM EmpleadoRol er JOIN FETCH er.idEmpleado JOIN FETCH er.idRol WHERE er.activo=TRUE AND er.idEmpleado.activo=TRUE AND er.idRol.activo=TRUE AND (LOWER(er.idEmpleado.nombre)='mesero virtual' OR LOWER(er.idRol.nombre)='mesero virtual')", EmpleadoRol.class)
                .setMaxResults(1).getResultList().stream().findFirst().orElse(null);
        if (virtual == null) return error(503, "No existe un EmpleadoRol activo configurado como Mesero Virtual");
        List<Producto> products = new ArrayList<>();
        for (OrderLine line : in.productos) {
            if (line == null || line.idProducto == null || line.cantidad == null || line.cantidad < 1) return error(400, "Cada producto requiere idProducto y cantidad >= 1");
            Producto p = em.find(Producto.class, line.idProducto);
            if (p == null || !Boolean.TRUE.equals(p.getActivo())) return error(400, "Producto inexistente o inactivo: " + line.idProducto);
            products.add(p);
        }
        Orden order = new Orden(UUID.randomUUID()); order.setFechaCreacion(new Date()); order.setIdEmpleadoRol(virtual); em.persist(order);
        String customer = in.cliente == null ? in.nombreCliente : in.cliente;
        List<Map<String,Object>> lines = new ArrayList<>(); BigDecimal total = BigDecimal.ZERO;
        for (int i = 0; i < in.productos.size(); i++) {
            OrderLine requested = in.productos.get(i); Producto product = products.get(i);
            String note = "Cliente: " + (customer == null ? "Público" : customer);
            String comments = requested.observaciones == null ? requested.comentarios : requested.observaciones;
            if (comments != null && !comments.isBlank()) note += "; " + comments;
            // No quantity column exists in DDL: persist one order_product row per unit.
            for (int unit = 0; unit < requested.cantidad; unit++) {
                OrdenProducto op = new OrdenProducto(UUID.randomUUID()); op.setIdOrden(order); op.setIdProducto(product); op.setPrecio(product.getPrecioSugerido()); op.setObservaciones(note); em.persist(op);
                total = total.add(product.getPrecioSugerido());
                lines.add(Map.of("idOrdenProducto", op.getIdOrdenProducto(), "idProducto", product.getIdProducto(), "nombre", product.getNombre() == null ? "" : product.getNombre(), "cantidad", 1, "precio", product.getPrecioSugerido(), "observaciones", note));
            }
        }
        return Response.status(201).entity(Map.of("idOrden", order.getIdOrden(), "fechaCreacion", order.getFechaCreacion(), "estado", "PENDIENTE_COCINA", "cliente", customer == null ? "Público" : customer, "productos", lines, "total", total)).build();
    }

    /** An order is pending while it has no fully paid invoice linked to its order_product rows. */
    @GET @Path("orden/pendientes")
    public List<Map<String,Object>> pendientes() {
        List<Orden> orders = em.createQuery("SELECT DISTINCT o FROM Orden o LEFT JOIN FETCH o.ordenProductoList op LEFT JOIN FETCH op.idProducto WHERE NOT EXISTS (SELECT fp FROM FacturaOrdenProducto fp JOIN fp.idFactura f JOIN f.pagoList p WHERE fp.idOrdenProducto.idOrden=o AND p.estado='PAGADO') ORDER BY o.fechaCreacion", Orden.class).getResultList();
        return orders.stream().map(this::ordenDto).toList();
    }

    /** Create the invoice/ticket and link every line of the order. */
    @POST @Path("factura")
    public Response factura(InvoiceRequest in) {
        if (in == null || in.idOrden == null || in.idCaja == null || in.idEmpleadoRol == null) return error(400, "idOrden, idCaja e idEmpleadoRol son requeridos");
        Orden order = em.find(Orden.class, in.idOrden); Caja box = em.find(Caja.class, in.idCaja); EmpleadoRol cashier = em.find(EmpleadoRol.class, in.idEmpleadoRol);
        if (order == null || box == null || cashier == null) return error(404, "Orden, caja o EmpleadoRol no encontrado");
        List<OrdenProducto> lines = em.createQuery("SELECT op FROM OrdenProducto op JOIN FETCH op.idProducto WHERE op.idOrden.idOrden=:id", OrdenProducto.class).setParameter("id", order.getIdOrden()).getResultList();
        if (lines.isEmpty()) return error(409, "La orden no tiene productos");
        Long linked = em.createQuery("SELECT COUNT(fp) FROM FacturaOrdenProducto fp WHERE fp.idOrdenProducto.idOrden.idOrden=:id", Long.class).setParameter("id", order.getIdOrden()).getSingleResult();
        if (linked > 0) return error(409, "La orden ya tiene factura");
        Factura invoice = new Factura(UUID.randomUUID()); invoice.setIdCaja(box); invoice.setIdEmpleadoRol(cashier); invoice.setCliente(in.cliente); invoice.setFechaFacturacion(new Date()); invoice.setEstado("PENDIENTE"); invoice.setObservaciones(in.observaciones); em.persist(invoice);
        BigDecimal total = BigDecimal.ZERO;
        for (OrdenProducto op : lines) { FacturaOrdenProducto row = new FacturaOrdenProducto(UUID.randomUUID()); row.setIdFactura(invoice); row.setIdOrdenProducto(op); row.setPrecio(op.getPrecio()); em.persist(row); total = total.add(op.getPrecio()); }
        return Response.status(201).entity(ticket(invoice, order, lines, total)).build();
    }

    @GET @Path("factura/pendientes")
    public List<Map<String,Object>> facturasPendientes() {
        List<Factura> invoices = em.createQuery("SELECT f FROM Factura f WHERE f.estado='PENDIENTE' ORDER BY f.fechaFacturacion", Factura.class).getResultList();
        List<Map<String,Object>> result = new ArrayList<>();
        for (Factura f : invoices) {
            List<FacturaOrdenProducto> rows = em.createQuery("SELECT fp FROM FacturaOrdenProducto fp JOIN FETCH fp.idOrdenProducto op JOIN FETCH op.idProducto WHERE fp.idFactura.idFactura=:id", FacturaOrdenProducto.class).setParameter("id", f.getIdFactura()).getResultList();
            BigDecimal total = rows.stream().map(FacturaOrdenProducto::getPrecio).reduce(BigDecimal.ZERO, BigDecimal::add);
            Map<String,Object> item = new LinkedHashMap<>(); item.put("idFactura", f.getIdFactura()); item.put("cliente", f.getCliente()); item.put("fechaFacturacion", f.getFechaFacturacion()); item.put("total", total); result.add(item);
        }
        return result;
    }

    @GET @Path("factura/{id}")
    public Response ticket(@PathParam("id") UUID id) {
        Factura f = em.find(Factura.class, id); if (f == null) return error(404, "Factura no encontrada");
        List<FacturaOrdenProducto> rows = em.createQuery("SELECT fp FROM FacturaOrdenProducto fp JOIN FETCH fp.idOrdenProducto op JOIN FETCH op.idProducto WHERE fp.idFactura.idFactura=:id", FacturaOrdenProducto.class).setParameter("id", id).getResultList();
        List<OrdenProducto> lines = rows.stream().map(FacturaOrdenProducto::getIdOrdenProducto).toList();
        Orden order = lines.isEmpty() ? null : lines.get(0).getIdOrden();
        BigDecimal total = rows.stream().map(FacturaOrdenProducto::getPrecio).reduce(BigDecimal.ZERO, BigDecimal::add);
        return Response.ok(ticket(f, order, lines, total)).build();
    }

    /** Split tender is accepted as multiple payment_detail rows, with exact total validation. */
    @POST @Path("pago")
    public Response pagar(PaymentRequest in) {
        if (in == null || in.idFactura == null || in.detalles == null || in.detalles.isEmpty()) return error(400, "idFactura y detalles son requeridos");
        Factura f = em.find(Factura.class, in.idFactura); if (f == null) return error(404, "Factura no encontrada");
        if ("PAGADA".equalsIgnoreCase(f.getEstado())) return error(409, "Factura ya pagada");
        List<FacturaOrdenProducto> rows = em.createQuery("SELECT fp FROM FacturaOrdenProducto fp WHERE fp.idFactura.idFactura=:id", FacturaOrdenProducto.class).setParameter("id", f.getIdFactura()).getResultList();
        BigDecimal due = rows.stream().map(FacturaOrdenProducto::getPrecio).reduce(BigDecimal.ZERO, BigDecimal::add); BigDecimal paid = BigDecimal.ZERO;
        for (PaymentLine d : in.detalles) {
            String type = d == null || d.tipoPago == null ? "" : d.tipoPago.toUpperCase(Locale.ROOT);
            if (d == null || d.monto == null || d.monto.signum() <= 0 || !Set.of("EFECTIVO", "TARJETA", "QR").contains(type)) return error(400, "Cada detalle requiere monto positivo y tipo EFECTIVO, TARJETA o QR");
            paid = paid.add(d.monto);
        }
        if (paid.compareTo(due) != 0) return error(400, "La suma de detalles debe coincidir con el total " + due);
        Pago payment = new Pago(UUID.randomUUID()); payment.setIdFactura(f); payment.setEstado("PAGADO"); payment.setObservaciones(in.observaciones); em.persist(payment);
        List<Map<String,Object>> detailDtos = new ArrayList<>();
        for (PaymentLine d : in.detalles) { PagoDetalle row = new PagoDetalle(UUID.randomUUID()); row.setIdPago(payment); row.setTipoPago(d.tipoPago.toUpperCase(Locale.ROOT)); row.setMonto(d.monto); row.setReferenciaExterna(d.referenciaExterna); row.setObservaciones(d.observaciones); em.persist(row); detailDtos.add(Map.of("idPagoDetalle", row.getIdPagoDetalle(), "tipoPago", row.getTipoPago(), "monto", row.getMonto())); }
        f.setEstado("PAGADA");
        Map<String,Object> out = new LinkedHashMap<>(); out.put("idPago", payment.getIdPago()); out.put("idFactura", f.getIdFactura()); out.put("estado", payment.getEstado()); out.put("total", paid); out.put("detalles", detailDtos); return Response.status(201).entity(out).build();
    }

    private Map<String,Object> ordenDto(Orden o) {
        List<Map<String,Object>> lines = o.getOrdenProductoList().stream().map(op -> Map.<String,Object>of("idOrdenProducto", op.getIdOrdenProducto(), "idProducto", op.getIdProducto().getIdProducto(), "nombre", op.getIdProducto().getNombre() == null ? "" : op.getIdProducto().getNombre(), "cantidad", 1, "precio", op.getPrecio(), "observaciones", op.getObservaciones() == null ? "" : op.getObservaciones())).toList();
        BigDecimal total = o.getOrdenProductoList().stream().map(OrdenProducto::getPrecio).reduce(BigDecimal.ZERO, BigDecimal::add);
        Map<String,Object> out = new LinkedHashMap<>(); out.put("idOrden", o.getIdOrden()); out.put("fechaCreacion", o.getFechaCreacion()); out.put("estado", "PENDIENTE_COCINA"); out.put("productos", lines); out.put("total", total); return out;
    }
    private Map<String,Object> ticket(Factura f, Orden order, List<OrdenProducto> lines, BigDecimal total) {
        List<Map<String,Object>> items = lines.stream().map(op -> Map.<String,Object>of("idProducto", op.getIdProducto().getIdProducto(), "producto", op.getIdProducto().getNombre() == null ? "" : op.getIdProducto().getNombre(), "cantidad", 1, "precio", op.getPrecio(), "observaciones", op.getObservaciones() == null ? "" : op.getObservaciones())).toList();
        Map<String,Object> out = new LinkedHashMap<>(); out.put("idFactura", f.getIdFactura()); out.put("idOrden", order == null ? null : order.getIdOrden()); out.put("cliente", f.getCliente()); out.put("fechaFacturacion", f.getFechaFacturacion()); out.put("estado", f.getEstado()); out.put("productos", items); out.put("total", total); return out;
    }
    private static Response error(int status, String message) { return Response.status(status).entity(Map.of("error", message)).build(); }
}
