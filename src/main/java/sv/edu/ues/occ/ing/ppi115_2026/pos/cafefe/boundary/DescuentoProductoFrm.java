package sv.edu.ues.occ.ing.ppi115_2026.pos.cafefe.boundary;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.List;
import java.util.UUID;
import sv.edu.ues.occ.ing.ppi115_2026.pos.cafefe.control.DescuentoRepository;
import sv.edu.ues.occ.ing.ppi115_2026.pos.cafefe.control.DescuentoProductoRepository;
import sv.edu.ues.occ.ing.ppi115_2026.pos.cafefe.control.ProductoRepository;
import sv.edu.ues.occ.ing.ppi115_2026.pos.cafefe.entity.Descuento;
import sv.edu.ues.occ.ing.ppi115_2026.pos.cafefe.entity.DescuentoProducto;
import sv.edu.ues.occ.ing.ppi115_2026.pos.cafefe.entity.Producto;

@Named(value = "descuentoProductoFrm")
@ViewScoped
public class DescuentoProductoFrm implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private DescuentoProductoRepository descuentoProductoRepository;

    @Inject
    private DescuentoRepository descuentoRepository;

    @Inject
    private ProductoRepository productoRepository;

    private List<DescuentoProducto> listaDescuentoProducto;
    private List<Descuento> listaDescuentos;
    private List<Producto> listaProductos;

    private DescuentoProducto registroSeleccionado;
    private UUID idDescuentoSeleccionado;
    private UUID idProductoSeleccionado;
    private UUID idProductoMaestro;

    @PostConstruct
    public void init() {
        this.listaDescuentoProducto = new java.util.ArrayList<>();
        cargarPorProducto(null);
    }

    public void nuevoRegistro() {
        this.registroSeleccionado = new DescuentoProducto();
        this.idDescuentoSeleccionado = null;
        this.idProductoSeleccionado = idProductoMaestro;
    }

    public void cargarDatos() {
        try {
            this.listaDescuentoProducto = descuentoProductoRepository.findAllWithRelations();
            this.listaDescuentos = descuentoRepository.findActivos();
            this.listaProductos = productoRepository.findActivos();
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Error al cargar los datos"));
        }
    }

    public void cargarPorProducto(UUID idProducto) {
        idProductoMaestro = idProducto;
        nuevoRegistro();
        try {
            listaDescuentoProducto = idProducto == null
                    ? new java.util.ArrayList<>()
                    : descuentoProductoRepository.buscarPorIdProducto(idProducto);
            cargarOpciones();
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error",
                            "No se pudieron cargar los descuentos del producto."));
        }
    }

    private void cargarOpciones() {
        listaDescuentos = descuentoRepository.findActivos();
        listaProductos = productoRepository.findActivos();
    }

    public void seleccionarRegistro(DescuentoProducto dp) {
        this.registroSeleccionado = dp;
        if (dp != null) {
            this.idDescuentoSeleccionado = (dp.getIdDescuento() != null) ? dp.getIdDescuento().getIdDescuento() : null;
            this.idProductoSeleccionado = (dp.getIdProducto() != null) ? dp.getIdProducto().getIdProducto() : null;
        }
    }

    public void guardar() {
       try {
            if (registroSeleccionado == null || idDescuentoSeleccionado == null || idProductoSeleccionado == null) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_WARN, "Advertencia", "Debe seleccionar un Producto y un Descuento"));
                return;
            }

            Descuento desc = listaDescuentos == null ? null : listaDescuentos.stream()
                    .filter(item -> idDescuentoSeleccionado.equals(item.getIdDescuento()))
                    .findFirst().orElse(null);
            Producto prod = productoRepository.find(idProductoSeleccionado);

            if (desc == null || prod == null) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "El descuento no está activo o el producto no existe."));
                return;
            }

            if (!Boolean.TRUE.equals(prod.getActivo())
                    || desc.getIdTipoDescuento() == null
                    || !Boolean.TRUE.equals(desc.getIdTipoDescuento().getActivo())) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_WARN, "Validación",
                                "Solo se pueden asignar productos y descuentos activos."));
                return;
            }

            // VALIDACIÓN 1: Porcentaje Máximo
            if (registroSeleccionado.getValor() != null) {
                int valorIngresado = registroSeleccionado.getValor();

                if (valorIngresado < 0 || valorIngresado > 100) {
                    FacesContext.getCurrentInstance().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error de Validación", "El porcentaje debe estar entre 0% y 100%"));
                    return;
                }

                if (desc.getIdTipoDescuento() != null && desc.getIdTipoDescuento().getDescuentoMaximo() != null) {
                    int limiteMaximo = desc.getIdTipoDescuento().getDescuentoMaximo();
                    if (valorIngresado > limiteMaximo) {
                        FacesContext.getCurrentInstance().addMessage(null,
                                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error de Validación",
                                        "El valor ingresado (" + valorIngresado + "%) supera el descuento máximo permitido por el tipo (" + limiteMaximo + "%)."));
                        return;
                    }
                }
            }

            // VALIDACIÓN 2: Fechas Límites
            if (registroSeleccionado.getFechaDesde() != null && registroSeleccionado.getFechaHasta() != null) {
                if (registroSeleccionado.getFechaHasta().before(registroSeleccionado.getFechaDesde())) {
                    FacesContext.getCurrentInstance().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error de Validación", "La Fecha Hasta no puede ser anterior a la Fecha Desde."));
                    return;
                }
            }

            if (desc.getFechaDesde() != null && (registroSeleccionado.getFechaDesde() == null
                    || registroSeleccionado.getFechaDesde().before(desc.getFechaDesde()))) {
                    FacesContext.getCurrentInstance().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error de Validación",
                                    "La fecha 'Desde' debe respetar el inicio de vigencia del descuento (" + desc.getFechaDesde() + ")."));
                    return;
            }

            if (desc.getFechaHasta() != null && (registroSeleccionado.getFechaHasta() == null
                    || registroSeleccionado.getFechaHasta().after(desc.getFechaHasta()))) {
                    FacesContext.getCurrentInstance().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error de Validación",
                                    "La Fecha 'Hasta' excede el límite permitido por el descuento seleccionado (" + desc.getFechaHasta() + ")."));
                    return;
            }

            registroSeleccionado.setIdDescuento(desc);
            registroSeleccionado.setIdProducto(prod);

            if (registroSeleccionado.getIdDescuentoProducto() == null) {
                registroSeleccionado.setIdDescuentoProducto(UUID.randomUUID());
                descuentoProductoRepository.create(registroSeleccionado);
            } else {
                descuentoProductoRepository.edit(registroSeleccionado);
            }

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Asociación Descuento-Producto procesada correctamente"));

            nuevoRegistro();
            cargarPorProducto(idProductoMaestro);

        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Error al guardar los datos"));
        }
    }

    public void eliminar(DescuentoProducto asignacion) {
        try {
            descuentoProductoRepository.remove(asignacion);
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Asignación de descuento eliminada."));
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "No se pudo eliminar la asignación: " + e.getMessage()));
        }
        cargarPorProducto(idProductoMaestro);
    }

    public List<DescuentoProducto> getListaDescuentoProducto() { return listaDescuentoProducto; }
    public void setListaDescuentoProducto(List<DescuentoProducto> listaDescuentoProducto) { this.listaDescuentoProducto = listaDescuentoProducto; }

    public List<Descuento> getListaDescuentos() { return listaDescuentos; }
    public void setListaDescuentos(List<Descuento> listaDescuentos) { this.listaDescuentos = listaDescuentos; }

    public List<Producto> getListaProductos() { return listaProductos; }
    public void setListaProductos(List<Producto> listaProductos) { this.listaProductos = listaProductos; }

    public DescuentoProducto getRegistroSeleccionado() { return registroSeleccionado; }
    public void setRegistroSeleccionado(DescuentoProducto registroSeleccionado) { this.registroSeleccionado = registroSeleccionado; }

    public UUID getIdDescuentoSeleccionado() { return idDescuentoSeleccionado; }
    public void setIdDescuentoSeleccionado(UUID idDescuentoSeleccionado) { this.idDescuentoSeleccionado = idDescuentoSeleccionado; }

    public UUID getIdProductoSeleccionado() { return idProductoSeleccionado; }
    public void setIdProductoSeleccionado(UUID idProductoSeleccionado) { this.idProductoSeleccionado = idProductoSeleccionado; }
    public UUID getIdProductoMaestro() { return idProductoMaestro; }
}
