package sv.edu.ues.occ.ing.ppi115_2026.pos.cafefe.boundary;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import sv.edu.ues.occ.ing.ppi115_2026.pos.cafefe.entity.*;
import sv.edu.ues.occ.ing.ppi115_2026.pos.cafefe.service.OrdenService;
import sv.edu.ues.occ.ing.ppi115_2026.pos.cafefe.service.DescuentoService;

@Named("ordenFrm")
@ViewScoped
public class OrdenFrm implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private OrdenService ordenService;

    @Inject
    private DescuentoService descuentoService;

    private Orden registroSeleccionado;
    private List<Orden> registros;
    private List<OrdenProducto> itemsOrden = new ArrayList<>();
    private List<EmpleadoRol> listaCamareros;
    private List<Producto> listaProductos;

    private Producto productoSeleccionado;
    private String observacionItem;

    @PostConstruct
    public void init() {
        this.registroSeleccionado = new Orden();
        this.registroSeleccionado.setFechaCreacion(new Date());
        this.itemsOrden = new ArrayList<>();
        cargarDatos();
    }

    public void cargarDatos() {
        try {
            this.registros = ordenService.listar();
            this.listaProductos = ordenService.listarProductosDisponibles();
            this.listaCamareros = ordenService.listarCamarerosDisponibles();
        } catch (Exception e) {
            mensajeError("Error al cargar datos del formulario de órdenes.");
        }
    }


    public void seleccionarOrden(Orden orden) {
    if (orden == null) {
        return;
    }

    try {
        this.registroSeleccionado = orden;

        this.itemsOrden = new ArrayList<>();

        if (orden.getOrdenProductoList() != null) {
            this.itemsOrden.addAll(orden.getOrdenProductoList());
        }

        this.productoSeleccionado = null;
        this.observacionItem = "";

        mensajeInfo("Orden seleccionada para editar.");
    } catch (Exception e) {
        mensajeError("No se pudo cargar la orden para editar.");
    }
}

    public void agregarProducto() {
        if (productoSeleccionado == null) {
        mensajeWarn("Debe seleccionar un producto para agregar.");
        return;
    }

    OrdenProducto nuevoItem = new OrdenProducto();
    nuevoItem.setIdOrdenProducto(UUID.randomUUID());
    nuevoItem.setIdProducto(productoSeleccionado);

    // ASIGNAR LA RELACIÓN DE LA ORDEN PADRE
    nuevoItem.setIdOrden(this.registroSeleccionado);

    nuevoItem.setPrecio(productoSeleccionado.getPrecioSugerido());
    nuevoItem.setObservaciones(observacionItem != null && !observacionItem.trim().isEmpty()
            ? observacionItem : "Precio base sugerido.");

    this.itemsOrden.add(nuevoItem);

    this.productoSeleccionado = null;
    this.observacionItem = "";
    mensajeInfo("Producto agregado a la orden.");
    }

    public void removerProducto(OrdenProducto item) {
        if (item != null) {
            this.itemsOrden.remove(item);
            mensajeInfo("Producto eliminado de la orden.");
        }
    }

 public void btnGuardarHandler() {

    if (registroSeleccionado == null) {
        mensajeError("Selecciona una orden válida.");
        return;
    }

    try {
        boolean esNueva = registroSeleccionado.getIdOrden() == null;
        ordenService.guardar(registroSeleccionado, itemsOrden);
        mensajeInfo(esNueva ? "Orden creada exitosamente." : "Orden modificada correctamente.");

        limpiarFormulario();

    } catch (Exception e) {
        mensajeError("No se pudo guardar la orden: " + e.getMessage());
    }
}


 public void nuevaOrden() {
    registroSeleccionado = new Orden();
    registroSeleccionado.setFechaCreacion(new Date());

    itemsOrden = new ArrayList<>();
    productoSeleccionado = null;
    observacionItem = "";

    mensajeInfo("Formulario preparado para una nueva orden.");
}


 public void eliminarOrden(Orden orden) {
    if (orden == null) {
        mensajeError("Debe seleccionar una orden.");
        return;
    }

    try {
        ordenService.eliminar(orden);

        mensajeInfo("Orden eliminada correctamente.");

        if (registroSeleccionado != null
                && orden.equals(registroSeleccionado)) {
            nuevaOrden();
        }

        cargarDatos();

    } catch (Exception e) {
        mensajeError("No se puede eliminar el registro porque está siendo utilizado en el sistema.");
    }
}

    private void limpiarFormulario() {
        this.registroSeleccionado = new Orden();
        this.registroSeleccionado.setFechaCreacion(new Date());
        this.itemsOrden = new ArrayList<>();
        this.productoSeleccionado = null;
        this.observacionItem = "";
        cargarDatos(); // Vuelve a traer la lista de órdenes actualizada de la BD
    }


    private boolean esRolCamarero() {
        EmpleadoRol er = registroSeleccionado.getIdEmpleadoRol();
        return er != null && er.getIdRol() != null && "CAMARERO".equalsIgnoreCase(er.getIdRol().getNombre());
    }

    public List<DescuentoProducto> getDescuentosDisponibles(OrdenProducto item) {
        Date fecha = registroSeleccionado != null ? registroSeleccionado.getFechaCreacion() : null;
        return item != null ? descuentoService.disponibles(item.getIdProducto(), fecha) : List.of();
    }

    public String etiquetaDescuento(DescuentoProducto descuento) {
        if (descuento == null) return "";
        String nombre = descuento.getIdDescuento() != null ? descuento.getIdDescuento().getNombre() : "Descuento";
        String vigencia = descuento.getFechaHasta() == null ? "vigencia abierta" : "vigente por fechas";
        return nombre + " — " + descuento.getValor() + "% (" + vigencia + ")";
    }

    public BigDecimal totalOrden(Orden orden) {
        if (orden == null || orden.getOrdenProductoList() == null) return BigDecimal.ZERO;
        return orden.getOrdenProductoList().stream()
                .filter(item -> item != null && item.getPrecio() != null)
                .map(OrdenProducto::getPrecio)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getTotalActual() {
        return itemsOrden == null ? BigDecimal.ZERO : itemsOrden.stream()
                .filter(item -> item != null && item.getPrecio() != null)
                .map(OrdenProducto::getPrecio)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void aplicarDescuentoSeleccionado(OrdenProducto item) {
        if (item == null || item.getIdProducto() == null) return;
        UUID idSeleccionado = item.getIdDescuentoProductoSeleccionado();
        try {
            descuentoService.aplicar(item, idSeleccionado,
                    registroSeleccionado != null ? registroSeleccionado.getFechaCreacion() : null);
            mensajeInfo("Descuento aplicado al producto.");
        } catch (IllegalArgumentException e) {
            mensajeWarn(e.getMessage());
        }
    }

    private void mensajeError(String msg) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", msg));
    }

    private void mensajeWarn(String msg) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Atención", msg));
    }

    private void mensajeInfo(String msg) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito", msg));
    }

    // Getters y Setters
    public Orden getRegistroSeleccionado() { return registroSeleccionado; }
    public void setRegistroSeleccionado(Orden registroSeleccionado) { this.registroSeleccionado = registroSeleccionado; }
    public List<Orden> getRegistros() { return registros; }
    public List<OrdenProducto> getItemsOrden() { return itemsOrden; }
    public void setItemsOrden(List<OrdenProducto> itemsOrden) { this.itemsOrden = itemsOrden; }
    public List<EmpleadoRol> getListaCamareros() { return listaCamareros; }
    public List<Producto> getListaProductos() { return listaProductos; }
    public Producto getProductoSeleccionado() { return productoSeleccionado; }
    public void setProductoSeleccionado(Producto productoSeleccionado) { this.productoSeleccionado = productoSeleccionado; }
    public String getObservacionItem() { return observacionItem; }
    public void setObservacionItem(String observacionItem) { this.observacionItem = observacionItem; }
}
