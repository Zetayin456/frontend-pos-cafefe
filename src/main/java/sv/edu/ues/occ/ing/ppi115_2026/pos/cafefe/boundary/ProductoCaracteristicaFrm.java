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
import java.util.regex.Pattern;
import sv.edu.ues.occ.ing.ppi115_2026.pos.cafefe.control.CaracteristicaRepository;
import sv.edu.ues.occ.ing.ppi115_2026.pos.cafefe.control.ProductoCaracteristicaRepository;
import sv.edu.ues.occ.ing.ppi115_2026.pos.cafefe.control.ProductoRepository;
import sv.edu.ues.occ.ing.ppi115_2026.pos.cafefe.entity.Caracteristica;
import sv.edu.ues.occ.ing.ppi115_2026.pos.cafefe.entity.Producto;
import sv.edu.ues.occ.ing.ppi115_2026.pos.cafefe.entity.ProductoCaracteristica;

@Named(value = "productoCaracteristicaFrm")
@ViewScoped
public class ProductoCaracteristicaFrm implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private ProductoCaracteristicaRepository productoCaracteristicaRepository;

    @Inject
    private ProductoRepository productoRepository;

    @Inject
    private CaracteristicaRepository caracteristicaRepository;

    private List<ProductoCaracteristica> listaProductoCaracteristica;
    private List<Producto> listaProductos;
    private List<Caracteristica> listaCaracteristicas;

    private ProductoCaracteristica registroSeleccionado;
    private UUID idProductoSeleccionado;
    private UUID idCaracteristicaSeleccionada;
    private UUID idProductoMaestro;

    @PostConstruct
    public void init() {
        this.listaProductoCaracteristica = new java.util.ArrayList<>();
        cargarPorProducto(null);
    }

    public void nuevoRegistro() {
        this.registroSeleccionado = new ProductoCaracteristica();
        this.idProductoSeleccionado = idProductoMaestro;
        this.idCaracteristicaSeleccionada = null;
    }

    public void cargarDatos() {
        try {
            this.listaProductoCaracteristica = productoCaracteristicaRepository.findAllWithRelations();
            this.listaProductos = productoRepository.findActivos();
            this.listaCaracteristicas = caracteristicaRepository.findActivos();
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Error al cargar la información"));
        }
    }

    public void cargarPorProducto(UUID idProducto) {
        idProductoMaestro = idProducto;
        nuevoRegistro();
        try {
            listaProductoCaracteristica = idProducto == null
                    ? new java.util.ArrayList<>()
                    : productoCaracteristicaRepository.buscarPorIdProducto(idProducto);
            cargarOpciones();
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error",
                            "No se pudieron cargar las características del producto."));
        }
    }

    private void cargarOpciones() {
        listaProductos = productoRepository.findActivos();
        listaCaracteristicas = caracteristicaRepository.findActivos();
    }

    public void guardar() {
        try {
            if (idProductoSeleccionado != null && idCaracteristicaSeleccionada != null) {
                Producto prod = productoRepository.find(idProductoSeleccionado);
                Caracteristica car = caracteristicaRepository.find(idCaracteristicaSeleccionada);

                if (prod == null || !Boolean.TRUE.equals(prod.getActivo())) {
                    mensajeError("Solo se pueden asignar características a productos activos.");
                    return;
                }
                // Reobtener la característica desde la lista con relaciones ya cargadas.
                if (listaCaracteristicas != null) {
                    car = listaCaracteristicas.stream()
                            .filter(c -> idCaracteristicaSeleccionada.equals(c.getIdCaracteristica()))
                            .findFirst().orElse(car);
                }
                if (car == null || !Boolean.TRUE.equals(car.getActivo())
                        || car.getIdTipoCaracteristica() == null
                        || !Boolean.TRUE.equals(car.getIdTipoCaracteristica().getActivo())) {
                    mensajeError("No se puede asignar una característica cuyo tipo esté inactivo.");
                    return;
                }

                // Validación de Expresión Regular exigida por la rúbrica
                if (car != null && car.getIdTipoCaracteristica() != null 
                        && car.getIdTipoCaracteristica().getExpresionRegular() != null) {
                    
                    String regex = car.getIdTipoCaracteristica().getExpresionRegular();
                    String valor = registroSeleccionado.getValor();

                    boolean coincide;
                    try {
                        coincide = valor != null && Pattern.matches(regex, valor);
                    } catch (java.util.regex.PatternSyntaxException ex) {
                        mensajeError("La expresión regular configurada para el tipo no es válida.");
                        return;
                    }
                    if (!coincide) {
                        FacesContext.getCurrentInstance().addMessage(null,
                                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error de Validación", 
                                        "El valor ingresado no cumple con la Expresión Regular: " + regex));
                        return;
                    }
                }

                registroSeleccionado.setIdProducto(prod);
                registroSeleccionado.setIdCaracteristica(car);

                if (registroSeleccionado.getIdProductoCaracteristica() == null) {
                    registroSeleccionado.setIdProductoCaracteristica(UUID.randomUUID());
                    productoCaracteristicaRepository.create(registroSeleccionado);
                    FacesContext.getCurrentInstance().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Característica asignada al producto"));
                } else {
                    productoCaracteristicaRepository.edit(registroSeleccionado);
                    FacesContext.getCurrentInstance().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Característica actualizada"));
                }
                nuevoRegistro();
                cargarPorProducto(idProductoMaestro);
            } else {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_WARN, "Atención", "Debe seleccionar un Producto y una Característica"));
            }
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Error al guardar el registro"));
        }
    }

    public void seleccionar(ProductoCaracteristica asignacion) {
        this.registroSeleccionado = new ProductoCaracteristica();
        this.registroSeleccionado.setIdProductoCaracteristica(asignacion.getIdProductoCaracteristica());
        this.registroSeleccionado.setValor(asignacion.getValor());
        this.idProductoSeleccionado = asignacion.getIdProducto() != null ? asignacion.getIdProducto().getIdProducto() : null;
        this.idCaracteristicaSeleccionada = asignacion.getIdCaracteristica() != null ? asignacion.getIdCaracteristica().getIdCaracteristica() : null;
    }

    public void eliminar(ProductoCaracteristica asignacion) {
        try {
            productoCaracteristicaRepository.remove(asignacion);
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Asignación eliminada."));
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "No se pudo eliminar la asignación: " + e.getMessage()));
        }
        nuevoRegistro();
        cargarPorProducto(idProductoMaestro);
    }

    private void mensajeError(String detalle) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Validación", detalle));
    }

    // Getters y Setters
    public List<ProductoCaracteristica> getListaProductoCaracteristica() { return listaProductoCaracteristica; }
    public List<Producto> getListaProductos() { return listaProductos; }
    public List<Caracteristica> getListaCaracteristicas() { return listaCaracteristicas; }

    public ProductoCaracteristica getRegistroSeleccionado() { return registroSeleccionado; }
    public void setRegistroSeleccionado(ProductoCaracteristica registroSeleccionado) { this.registroSeleccionado = registroSeleccionado; }

    public UUID getIdProductoSeleccionado() { return idProductoSeleccionado; }
    public void setIdProductoSeleccionado(UUID idProductoSeleccionado) { this.idProductoSeleccionado = idProductoSeleccionado; }

    public UUID getIdCaracteristicaSeleccionada() { return idCaracteristicaSeleccionada; }
    public void setIdCaracteristicaSeleccionada(UUID idCaracteristicaSeleccionada) { this.idCaracteristicaSeleccionada = idCaracteristicaSeleccionada; }
    public UUID getIdProductoMaestro() { return idProductoMaestro; }
}
