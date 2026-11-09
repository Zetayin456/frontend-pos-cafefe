package sv.edu.ues.occ.ing.ppi115_2026.pos.cafefe.boundary;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import sv.edu.ues.occ.ing.ppi115_2026.pos.cafefe.control.ProductoRepository;
import sv.edu.ues.occ.ing.ppi115_2026.pos.cafefe.control.ProductoTipoProductoRepository;
import sv.edu.ues.occ.ing.ppi115_2026.pos.cafefe.control.TipoProductoRepository;
import sv.edu.ues.occ.ing.ppi115_2026.pos.cafefe.entity.Producto;
import sv.edu.ues.occ.ing.ppi115_2026.pos.cafefe.entity.ProductoTipoProducto;
import sv.edu.ues.occ.ing.ppi115_2026.pos.cafefe.entity.TipoProducto;

@Named(value = "productoTipoProductoFrm")
@ViewScoped
public class ProductoTipoProductoFrm implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER =
            Logger.getLogger(ProductoTipoProductoFrm.class.getName());

    @Inject
    private ProductoTipoProductoRepository productoTipoProductoRepository;

    @Inject
    private ProductoRepository productoRepository;

    @Inject
    private TipoProductoRepository tipoProductoRepository;

    private List<ProductoTipoProducto> listaProductoTipoProducto;
    private List<Producto> listaProductos;
    private List<TipoProducto> listaTiposProducto;

    private ProductoTipoProducto registroSeleccionado;
    private UUID idProductoSeleccionado;
    private UUID idTipoProductoSeleccionado;
    private UUID idProductoMaestro;

    @PostConstruct
    public void init() {
        listaProductoTipoProducto = new ArrayList<>();
        cargarPorProducto(null);
    }

    // Cargar las asignaciones, los productos y los tipos.
    public void cargarDatos() {
        try {
            List<ProductoTipoProducto> asignaciones =
                    productoTipoProductoRepository.findAllWithRelations();

            listaProductoTipoProducto = asignaciones != null
                    ? asignaciones : new ArrayList<>();

            List<Producto> productos = productoRepository.findActivos();

            listaProductos = productos != null
                    ? productos : new ArrayList<>();

            List<TipoProducto> tipos = tipoProductoRepository.findActivos();

            listaTiposProducto = tipos != null
                    ? tipos : new ArrayList<>();

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE,
                    "Error al cargar las asignaciones de productos y tipos", e);

            mensaje(FacesMessage.SEVERITY_ERROR,
                    "Error", "No se pudieron cargar los datos.");
        }
    }

    public void cargarPorProducto(UUID idProducto) {
        idProductoMaestro = idProducto;
        nuevoRegistro();
        try {
            listaProductoTipoProducto = idProducto == null
                    ? new ArrayList<>()
                    : productoTipoProductoRepository.buscarPorIdProducto(idProducto);
            cargarOpciones();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al cargar tipos del producto", e);
            mensaje(FacesMessage.SEVERITY_ERROR, "Error",
                    "No se pudieron cargar los tipos asignados al producto.");
        }
    }

    private void cargarOpciones() {
        listaProductos = productoRepository.findActivos();
        listaTiposProducto = tipoProductoRepository.findActivos();
    }

    // Preparar el formulario para una asignación nueva.
    public void nuevoRegistro() {
        registroSeleccionado = new ProductoTipoProducto();
        registroSeleccionado.setFechaCreacion(new Date());

        idProductoSeleccionado = idProductoMaestro;
        idTipoProductoSeleccionado = null;
    }

    // Guardar una asignación nueva o actualizar una existente.
    public void guardar() {
        try {
            if (registroSeleccionado == null) {
                mensaje(FacesMessage.SEVERITY_WARN,
                        "Aviso", "No hay un registro seleccionado.");
                return;
            }

            if (idProductoSeleccionado == null) {
                mensaje(FacesMessage.SEVERITY_WARN,
                        "Validación", "Selecciona un producto.");
                return;
            }

            if (idTipoProductoSeleccionado == null) {
                mensaje(FacesMessage.SEVERITY_WARN,
                        "Validación", "Selecciona un tipo de producto.");
                return;
            }

            // Buscar las entidades seleccionadas.
            Producto producto =
                    productoRepository.find(idProductoSeleccionado);

            TipoProducto tipo =
                    tipoProductoRepository.find(idTipoProductoSeleccionado);

            if (producto == null || tipo == null) {
                mensaje(FacesMessage.SEVERITY_ERROR,
                        "Error", "El producto o el tipo no existe.");
                return;
            }

            // Evitar asignar productos inactivos.
            if (!Boolean.TRUE.equals(producto.getActivo())) {
                mensaje(FacesMessage.SEVERITY_WARN,
                        "Validación",
                        "No puedes asignar tipos a un producto inactivo.");
                return;
            }

            // Validación requerida: el tipo debe estar activo.
            if (!Boolean.TRUE.equals(tipo.getActivo())) {
                mensaje(FacesMessage.SEVERITY_WARN,
                        "Validación",
                        "No puedes asignar un tipo de producto inactivo.");
                return;
            }

            // Evitar asignaciones duplicadas.
            if (existeAsignacion(
                    idProductoSeleccionado,
                    idTipoProductoSeleccionado,
                    registroSeleccionado.getIdProductoTipoProducto())) {

                mensaje(FacesMessage.SEVERITY_WARN,
                        "Asignación duplicada",
                        "Este producto ya tiene asignado ese tipo.");
                return;
            }

            registroSeleccionado.setIdProducto(producto);
            registroSeleccionado.setIdTipoProducto(tipo);

            if (registroSeleccionado.getFechaCreacion() == null) {
                registroSeleccionado.setFechaCreacion(new Date());
            }

            // Crear o actualizar.
            if (registroSeleccionado.getIdProductoTipoProducto() == null) {

                registroSeleccionado.setIdProductoTipoProducto(
                        UUID.randomUUID());

                productoTipoProductoRepository.create(registroSeleccionado);

                mensaje(FacesMessage.SEVERITY_INFO,
                        "Éxito", "Asignación creada correctamente.");

            } else {

                productoTipoProductoRepository.edit(registroSeleccionado);

                mensaje(FacesMessage.SEVERITY_INFO,
                        "Éxito", "Asignación actualizada correctamente.");
            }

            nuevoRegistro();
            cargarPorProducto(idProductoMaestro);

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE,
                    "Error al guardar la asignación", e);

            mensaje(FacesMessage.SEVERITY_ERROR,
                    "Error",
                    "No se pudo guardar la asignación. "
                    + "Revisa el registro y las relaciones.");
        }
    }

    // Comprobar si el producto ya tiene asignado ese tipo.
    private boolean existeAsignacion(
            UUID idProducto,
            UUID idTipoProducto,
            UUID idAsignacionActual) {

        if (listaProductoTipoProducto == null) {
            return false;
        }

        for (ProductoTipoProducto asignacion : listaProductoTipoProducto) {

            if (asignacion == null) {
                continue;
            }

            // Al editar, ignorar el registro que estamos modificando.
            if (idAsignacionActual != null
                    && idAsignacionActual.equals(
                            asignacion.getIdProductoTipoProducto())) {
                continue;
            }

            Producto producto = asignacion.getIdProducto();
            TipoProducto tipo = asignacion.getIdTipoProducto();

            if (producto != null && tipo != null
                    && idProducto.equals(producto.getIdProducto())
                    && idTipoProducto.equals(tipo.getIdTipoProducto())) {
                return true;
            }
        }

        return false;
    }

    // Seleccionar una asignación para editarla.
    public void seleccionarRegistro(ProductoTipoProducto asignacion) {
        if (asignacion == null) {
            return;
        }

        registroSeleccionado = new ProductoTipoProducto();

        registroSeleccionado.setIdProductoTipoProducto(
                asignacion.getIdProductoTipoProducto());

        registroSeleccionado.setFechaCreacion(
                asignacion.getFechaCreacion());

        registroSeleccionado.setObservaciones(
                asignacion.getObservaciones());

        registroSeleccionado.setIdProducto(
                asignacion.getIdProducto());

        registroSeleccionado.setIdTipoProducto(
                asignacion.getIdTipoProducto());

        idProductoSeleccionado =
                asignacion.getIdProducto() != null
                ? asignacion.getIdProducto().getIdProducto()
                : null;

        idTipoProductoSeleccionado =
                asignacion.getIdTipoProducto() != null
                ? asignacion.getIdTipoProducto().getIdTipoProducto()
                : null;
    }

    // Eliminar una asignación.
    public void eliminar(ProductoTipoProducto asignacion) {
        if (asignacion == null
                || asignacion.getIdProductoTipoProducto() == null) {

            mensaje(FacesMessage.SEVERITY_WARN,
                    "Aviso", "Selecciona una asignación válida.");
            return;
        }

        try {
            productoTipoProductoRepository.remove(asignacion);

            mensaje(FacesMessage.SEVERITY_INFO,
                    "Éxito", "Asignación eliminada correctamente.");

            if (registroSeleccionado != null
                    && asignacion.getIdProductoTipoProducto().equals(
                            registroSeleccionado.getIdProductoTipoProducto())) {
                nuevoRegistro();
            }

            cargarPorProducto(idProductoMaestro);

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE,
                    "Error al eliminar la asignación", e);

            mensaje(FacesMessage.SEVERITY_ERROR,
                    "Error",
                    "No se pudo eliminar la asignación. "
                    + "Comprueba si existen restricciones en la base de datos.");
        }
    }

    private void mensaje(
            FacesMessage.Severity severidad,
            String titulo,
            String detalle) {

        FacesContext.getCurrentInstance().addMessage(
                null, new FacesMessage(severidad, titulo, detalle));
    }

    // Getters y setters.

    public List<ProductoTipoProducto> getListaProductoTipoProducto() {
        return listaProductoTipoProducto;
    }

    public List<Producto> getListaProductos() {
        return listaProductos;
    }

    public List<TipoProducto> getListaTiposProducto() {
        return listaTiposProducto;
    }

    public UUID getIdProductoMaestro() { return idProductoMaestro; }

    public ProductoTipoProducto getRegistroSeleccionado() {
        return registroSeleccionado;
    }

    public void setRegistroSeleccionado(
            ProductoTipoProducto registroSeleccionado) {
        this.registroSeleccionado = registroSeleccionado;
    }

    public UUID getIdProductoSeleccionado() {
        return idProductoSeleccionado;
    }

    public void setIdProductoSeleccionado(UUID idProductoSeleccionado) {
        this.idProductoSeleccionado = idProductoSeleccionado;
    }

    public UUID getIdTipoProductoSeleccionado() {
        return idTipoProductoSeleccionado;
    }

    public void setIdTipoProductoSeleccionado(
            UUID idTipoProductoSeleccionado) {
        this.idTipoProductoSeleccionado = idTipoProductoSeleccionado;
    }
}
