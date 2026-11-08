
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
import sv.edu.ues.occ.ing.ppi115_2026.pos.cafefe.control.EmpleadoRepository;
import sv.edu.ues.occ.ing.ppi115_2026.pos.cafefe.entity.Empleado;

@Named(value = "empleadoFrm")
@ViewScoped
public class EmpleadoFrm implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private EmpleadoRepository empleadoRepository;

    private List<Empleado> listaEmpleado;
    private Empleado registroSeleccionado;

    @PostConstruct
    public void init() {
        nuevoRegistro();
        cargarDatos();
    }

    public void nuevoRegistro() {
        registroSeleccionado = new Empleado();
        registroSeleccionado.setActivo(true);
    }

    public void cargarDatos() {
        try {
            List<Empleado> datos = empleadoRepository.findRange(0, 1000);
            listaEmpleado = datos != null ? datos : new ArrayList<>();
        } catch (Exception e) {
            mostrarMensaje(
                    FacesMessage.SEVERITY_ERROR,
                    "Error",
                    "No se pudo cargar la lista de empleados."
            );
        }
    }

    public void seleccionar(Empleado empleado) {
        if (empleado == null) {
            return;
        }

        // Copia los datos para no modificar directamente la fila.
        registroSeleccionado = new Empleado();
        registroSeleccionado.setIdEmpleado(empleado.getIdEmpleado());
        registroSeleccionado.setNombre(empleado.getNombre());
        registroSeleccionado.setApellido(empleado.getApellido());
        registroSeleccionado.setComentarios(empleado.getComentarios());
        registroSeleccionado.setActivo(empleado.getActivo());
    }

    public void guardar() {
        try {
            if (registroSeleccionado == null) {
                mostrarMensaje(
                        FacesMessage.SEVERITY_ERROR,
                        "Error",
                        "No hay un empleado seleccionado."
                );
                return;
            }

            String nombre = registroSeleccionado.getNombre();
            String apellido = registroSeleccionado.getApellido();

            if (nombre == null || nombre.trim().isEmpty()
                    || apellido == null || apellido.trim().isEmpty()) {
                mostrarMensaje(
                        FacesMessage.SEVERITY_WARN,
                        "Validación",
                        "El nombre y el apellido son obligatorios."
                );
                return;
            }

            registroSeleccionado.setNombre(nombre.trim());
            registroSeleccionado.setApellido(apellido.trim());

            if (registroSeleccionado.getIdEmpleado() == null) {
                registroSeleccionado.setIdEmpleado(UUID.randomUUID());
                empleadoRepository.create(registroSeleccionado);

                mostrarMensaje(
                        FacesMessage.SEVERITY_INFO,
                        "Éxito",
                        "Empleado registrado correctamente."
                );
            } else {
                empleadoRepository.edit(registroSeleccionado);

                mostrarMensaje(
                        FacesMessage.SEVERITY_INFO,
                        "Éxito",
                        "Empleado actualizado correctamente."
                );
            }

            nuevoRegistro();
            cargarDatos();

        } catch (Exception e) {
            mostrarMensaje(
                    FacesMessage.SEVERITY_ERROR,
                    "Error",
                    "No se pudo guardar el empleado. Verifica los datos."
            );
        }
    }

    public void eliminar(Empleado empleado) {
        if (empleado == null || empleado.getIdEmpleado() == null) {
            mostrarMensaje(
                    FacesMessage.SEVERITY_WARN,
                    "Aviso",
                    "No se seleccionó un empleado válido."
            );
            return;
        }

        try {
            empleadoRepository.remove(empleado);

            if (registroSeleccionado != null
                    && empleado.getIdEmpleado().equals(
                            registroSeleccionado.getIdEmpleado())) {
                nuevoRegistro();
            }

            cargarDatos();

            mostrarMensaje(
                    FacesMessage.SEVERITY_INFO,
                    "Éxito",
                    "Empleado eliminado correctamente."
            );

        } catch (Exception e) {
            mostrarMensaje(
                    FacesMessage.SEVERITY_ERROR,
                    "Error",
                    "No se pudo eliminar el empleado. Puede estar asociado a otros registros."
            );
        }
    }

    private void mostrarMensaje(
            FacesMessage.Severity severidad,
            String titulo,
            String detalle) {

        FacesContext.getCurrentInstance().addMessage(
                null,
                new FacesMessage(severidad, titulo, detalle)
        );
    }

    public List<Empleado> getListaEmpleado() {
        return listaEmpleado;
    }

    public void setListaEmpleado(List<Empleado> listaEmpleado) {
        this.listaEmpleado = listaEmpleado;
    }

    public Empleado getRegistroSeleccionado() {
        return registroSeleccionado;
    }

    public void setRegistroSeleccionado(Empleado registroSeleccionado) {
        this.registroSeleccionado = registroSeleccionado;
    }
}
