package sv.edu.ues.occ.ing.ppi115_2026.pos.cafefe.service;

import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import sv.edu.ues.occ.ing.ppi115_2026.pos.cafefe.control.DescuentoProductoRepository;
import sv.edu.ues.occ.ing.ppi115_2026.pos.cafefe.entity.Descuento;
import sv.edu.ues.occ.ing.ppi115_2026.pos.cafefe.entity.DescuentoProducto;
import sv.edu.ues.occ.ing.ppi115_2026.pos.cafefe.entity.OrdenProducto;
import sv.edu.ues.occ.ing.ppi115_2026.pos.cafefe.entity.Producto;

/** Reglas de vigencia y cálculo de descuentos aplicables a productos. */
@Stateless
public class DescuentoService {

    @Inject
    private DescuentoProductoRepository descuentoProductoRepository;

    public List<DescuentoProducto> disponibles(Producto producto, Date fecha) {
        if (producto == null || producto.getIdProducto() == null || fecha == null) return List.of();
        List<DescuentoProducto> asignaciones = descuentoProductoRepository != null
                ? descuentoProductoRepository.buscarVigentesPorProducto(producto.getIdProducto(), fecha)
                : producto.getDescuentoProductoList();
        if (asignaciones == null || asignaciones.isEmpty()) return List.of();
        List<DescuentoProducto> resultado = new ArrayList<>();
        for (DescuentoProducto asignacion : asignaciones) {
            if (asignacion == null || asignacion.getValor() == null
                    || asignacion.getValor() < 0 || asignacion.getValor() > 100) continue;
            Descuento descuento = asignacion.getIdDescuento();
            if (descuento == null || descuento.getIdTipoDescuento() == null
                    || !Boolean.TRUE.equals(descuento.getIdTipoDescuento().getActivo())) continue;
            if (!enRango(fecha, asignacion.getFechaDesde(), asignacion.getFechaHasta())
                    || !enRango(fecha, descuento.getFechaDesde(), descuento.getFechaHasta())) continue;
            Integer maximo = descuento.getIdTipoDescuento().getDescuentoMaximo();
            if (maximo == null || asignacion.getValor() <= maximo) resultado.add(asignacion);
        }
        return resultado;
    }

    public void aplicar(OrdenProducto item, UUID idDescuento, Date fecha) {
        if (item == null || item.getIdProducto() == null || idDescuento == null) {
            throw new IllegalArgumentException("Selecciona un descuento vigente.");
        }
        DescuentoProducto seleccionado = disponibles(item.getIdProducto(), fecha).stream()
                .filter(d -> idDescuento.equals(d.getIdDescuentoProducto())).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("El descuento seleccionado ya no está vigente para este producto."));
        BigDecimal precioBase = item.getIdProducto().getPrecioSugerido();
        if (precioBase == null || precioBase.signum() < 0) {
            throw new IllegalArgumentException("El producto no tiene un precio base válido.");
        }
        BigDecimal porcentaje = BigDecimal.valueOf(seleccionado.getValor()).movePointLeft(2);
        item.setPrecio(precioBase.subtract(precioBase.multiply(porcentaje)).setScale(2, RoundingMode.HALF_UP));
        String nombre = seleccionado.getIdDescuento().getNombre();
        item.setObservaciones("Descuento aplicado: " + nombre + " (" + seleccionado.getValor() + "%).");
    }

    private boolean enRango(Date fecha, Date desde, Date hasta) {
        return (desde == null || !fecha.before(desde)) && (hasta == null || !fecha.after(hasta));
    }
}
