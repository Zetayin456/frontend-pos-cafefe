package sv.edu.ues.occ.ing.ppi115_2026.pos.cafefe.boundary;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import sv.edu.ues.occ.ing.ppi115_2026.pos.cafefe.control.EmpleadoRepository;
import sv.edu.ues.occ.ing.ppi115_2026.pos.cafefe.control.EmpleadoRolRepository;
import sv.edu.ues.occ.ing.ppi115_2026.pos.cafefe.control.RolRepository;
import sv.edu.ues.occ.ing.ppi115_2026.pos.cafefe.entity.Empleado;
import sv.edu.ues.occ.ing.ppi115_2026.pos.cafefe.entity.EmpleadoRol;
import sv.edu.ues.occ.ing.ppi115_2026.pos.cafefe.entity.Rol;

@Named(value = "empleadoRolFrm")
@ViewScoped
public class EmpleadoRolFrm implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER =
            Logger.getLogger(EmpleadoRolFrm.class.getName());

    @Inject
    private EmpleadoRolRepository empleadoRolRepository;

    @Inject
    private EmpleadoRepository empleadoRepository;

    @Inject
    private RolRepository rolRepository;

    private List<EmpleadoRol> listaAsignaciones;
    private List<Empleado> listaEmpleados;
    private List<Rol> listaRoles;

    private EmpleadoRol registroSeleccionado;
    private UUID idEmpleadoSeleccionado;
    private UUID idRolSeleccionado;

    @PostConstruct
    public void init() {
        nuevoRegistro();
        cargarDatos();
    }

    public void cargarDatos() {
        try {
            List<EmpleadoRol> asignaciones =
                    empleadoRolRepository.findAllWithRelations();

            listaAsignaciones = asignaciones != null
                    ? asignaciones : new ArrayList<>();

            List<Empleado> empleados = empleadoRepository.findActivos();

            listaEmpleados = empleados != null
                    ? empleados : new ArrayList<>();

            List<Rol> roles = rolRepository.findActivos();

            listaRoles = roles != null
                    ? roles : new ArrayList<>();

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE,
                    "Error al cargar las asignaciones de empleados y roles", e);

            mensaje(FacesMessage.SEVERITY_ERROR,
                    "Error", "No se pudieron cargar los datos.");
        }
    }

    public void nuevoRegistro() {
        registroSeleccionado = new EmpleadoRol();
        registroSeleccionado.setActivo(true);

        idEmpleadoSeleccionado = null;
        idRolSeleccionado = null;
    }

    public void seleccionar(EmpleadoRol asignacion) {
        if (asignacion == null) {
            return;
        }

        // Copiar la asignación seleccionada al formulario.
        registroSeleccionado = new EmpleadoRol();

        registroSeleccionado.setIdEmpleadoRol(
                asignacion.getIdEmpleadoRol());

        registroSeleccionado.setActivo(asignacion.getActivo());

        registroSeleccionado.setObservaciones(
                asignacion.getObservaciones());

        registroSeleccionado.setIdEmpleado(
                asignacion.getIdEmpleado());

        registroSeleccionado.setIdRol(
                asignacion.getIdRol());

        idEmpleadoSeleccionado =
                asignacion.getIdEmpleado() != null
                ? asignacion.getIdEmpleado().getIdEmpleado()
                : null;

        idRolSeleccionado =
                asignacion.getIdRol() != null
                ? asignacion.getIdRol().getIdRol()
                : null;
    }

    public void guardarCambios() {
        try {
            if (registroSeleccionado == null) {
                mensaje(FacesMessage.SEVERITY_WARN,
                        "Aviso", "No hay una asignación seleccionada.");
                return;
            }

            if (idEmpleadoSeleccionado == null) {
                mensaje(FacesMessage.SEVERITY_WARN,
                        "Validación", "Selecciona un empleado.");
                return;
            }

            if (idRolSeleccionado == null) {
                mensaje(FacesMessage.SEVERITY_WARN,
                        "Validación", "Selecciona un rol.");
                return;
            }

            // Buscar los registros por sus UUID.
            Empleado empleado =
                    empleadoRepository.find(idEmpleadoSeleccionado);

            Rol rol = rolRepository.find(idRolSeleccionado);

            if (empleado == null || rol == null) {
                mensaje(FacesMessage.SEVERITY_ERROR,
                        "Error", "El empleado o el rol no existe.");
                return;
            }

            // Para crear una asignación, ambos deben estar activos.
         boolean empleadoActivo = Boolean.TRUE.equals(empleado.getActivo());
boolean rolActivo = Boolean.TRUE.equals(rol.getActivo());

if (registroSeleccionado.getIdEmpleadoRol() == null) {

    // Para crear, ambos deben estar activos.
    if (!empleadoActivo || !rolActivo) {
        mensaje(FacesMessage.SEVERITY_WARN,
                "Validación",
                "Solo puedes asignar empleados y roles activos.");
        return;
    }

} else {

    // Para editar, permitir conservar una referencia inactiva,
    // pero no cambiarla por otra referencia inactiva.
    EmpleadoRol original = empleadoRolRepository.find(
            registroSeleccionado.getIdEmpleadoRol());

    boolean mismoEmpleado = original != null
            && original.getIdEmpleado() != null
            && original.getIdEmpleado().getIdEmpleado()
                    .equals(idEmpleadoSeleccionado);

    boolean mismoRol = original != null
            && original.getIdRol() != null
            && original.getIdRol().getIdRol()
                    .equals(idRolSeleccionado);

    if (!empleadoActivo && !mismoEmpleado) {
        mensaje(FacesMessage.SEVERITY_WARN,
                "Validación",
                "No puedes asignar un empleado inactivo.");
        return;
    }

    if (!rolActivo && !mismoRol) {
        mensaje(FacesMessage.SEVERITY_WARN,
                "Validación",
                "No puedes asignar un rol inactivo.");
        return;
    }
}

            // Evitar duplicar una asignación activa.
            for (EmpleadoRol existente : listaAsignaciones) {
                if (existente == null
                        || !Boolean.TRUE.equals(existente.getActivo())) {
                    continue;
                }

                if (registroSeleccionado.getIdEmpleadoRol() != null
                        && registroSeleccionado.getIdEmpleadoRol()
                                .equals(existente.getIdEmpleadoRol())) {
                    continue;
                }

                if (existente.getIdEmpleado() != null
                        && existente.getIdRol() != null
                        && existente.getIdEmpleado().getIdEmpleado()
                                .equals(idEmpleadoSeleccionado)
                        && existente.getIdRol().getIdRol()
                                .equals(idRolSeleccionado)) {

                    mensaje(FacesMessage.SEVERITY_WARN,
                            "Asignación duplicada",
                            "Este empleado ya tiene asignado ese rol.");
                    return;
                }
            }

            registroSeleccionado.setIdEmpleado(empleado);
            registroSeleccionado.setIdRol(rol);

            if (registroSeleccionado.getActivo() == null) {
                registroSeleccionado.setActivo(true);
            }

            if (registroSeleccionado.getIdEmpleadoRol() == null) {
                registroSeleccionado.setIdEmpleadoRol(
                        UUID.randomUUID());

                empleadoRolRepository.create(registroSeleccionado);

                mensaje(FacesMessage.SEVERITY_INFO,
                        "Éxito", "Rol asignado correctamente.");
            } else {
                empleadoRolRepository.edit(registroSeleccionado);

                mensaje(FacesMessage.SEVERITY_INFO,
                        "Éxito", "Asignación actualizada correctamente.");
            }

            nuevoRegistro();
            cargarDatos();

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE,
                    "Error al guardar la asignación", e);

            mensaje(FacesMessage.SEVERITY_ERROR,
                    "Error", "No se pudo guardar la asignación.");
        }
    }

    public void eliminar(EmpleadoRol asignacion) {
        if (asignacion == null
                || asignacion.getIdEmpleadoRol() == null) {
            mensaje(FacesMessage.SEVERITY_WARN,
                    "Aviso", "Selecciona una asignación válida.");
            return;
        }

        try {
            empleadoRolRepository.remove(asignacion);

            if (registroSeleccionado != null
                    && asignacion.getIdEmpleadoRol().equals(
                            registroSeleccionado.getIdEmpleadoRol())) {
                nuevoRegistro();
            }

            cargarDatos();

            mensaje(FacesMessage.SEVERITY_INFO,
                    "Éxito", "Asignación eliminada correctamente.");

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE,
                    "Error al eliminar la asignación", e);

            mensaje(FacesMessage.SEVERITY_ERROR,
                    "Error", "No se pudo eliminar la asignación.");
        }
    }

    private void mensaje(FacesMessage.Severity severidad,
                         String titulo, String detalle) {
        FacesContext.getCurrentInstance().addMessage(
                null, new FacesMessage(severidad, titulo, detalle));
    }

    // Getters y setters

    public List<EmpleadoRol> getListaAsignaciones() {
        return listaAsignaciones;
    }

    public List<Empleado> getListaEmpleados() {
        return listaEmpleados;
    }

    public List<Rol> getListaRoles() {
        return listaRoles;
    }

    public EmpleadoRol getRegistroSeleccionado() {
        return registroSeleccionado;
    }

    public void setRegistroSeleccionado(
            EmpleadoRol registroSeleccionado) {
        this.registroSeleccionado = registroSeleccionado;
    }

    public UUID getIdEmpleadoSeleccionado() {
        return idEmpleadoSeleccionado;
    }

    public void setIdEmpleadoSeleccionado(UUID idEmpleadoSeleccionado) {
        this.idEmpleadoSeleccionado = idEmpleadoSeleccionado;
    }

    public UUID getIdRolSeleccionado() {
        return idRolSeleccionado;
    }

    public void setIdRolSeleccionado(UUID idRolSeleccionado) {
        this.idRolSeleccionado = idRolSeleccionado;
    }
}
