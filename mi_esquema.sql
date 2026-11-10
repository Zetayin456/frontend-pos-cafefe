--
-- DDL Oficial Corregido y Unificado - POS Cafefe 2026
-- 

CREATE SCHEMA IF NOT EXISTS public;
SET default_tablespace = '';
SET default_table_access_method = heap;

-- 1. Tabla: caja
CREATE TABLE public.caja (
    id_caja uuid NOT NULL,
    nombre character varying(155),
    activo boolean DEFAULT true,
    observaciones text,
    CONSTRAINT pk_caja PRIMARY KEY (id_caja)
);

-- 2. Tabla: rol
CREATE TABLE public.rol (
    id_rol uuid NOT NULL,
    nombre character varying(155),
    activo boolean DEFAULT true,
    observaciones text,
    CONSTRAINT pk_rol PRIMARY KEY (id_rol)
);

-- 3. Tabla: empleado
CREATE TABLE public.empleado (
    id_empleado uuid NOT NULL,
    nombre character varying(255),
    apellido character varying(255),
    activo boolean,
    comentarios text,
    CONSTRAINT pk_empleado PRIMARY KEY (id_empleado)
);

-- 4. Tabla: empleado_rol
CREATE TABLE public.empleado_rol (
    id_empleado_rol uuid NOT NULL,
    id_empleado uuid,
    id_rol uuid,
    activo boolean,
    observaciones text,
    CONSTRAINT pk_id_empleado_rol PRIMARY KEY (id_empleado_rol),
    CONSTRAINT fk_empleado_rol_empleado FOREIGN KEY (id_empleado) REFERENCES public.empleado(id_empleado) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_empleado_rol_rol FOREIGN KEY (id_rol) REFERENCES public.rol(id_rol) ON UPDATE CASCADE ON DELETE RESTRICT
);

-- 5. Tabla: tipo_producto
CREATE TABLE public.tipo_producto (
    id_tipo_producto uuid NOT NULL,
    nombre character varying(155),
    activo boolean,
    observaciones text,
    CONSTRAINT pk_tipo_producto PRIMARY KEY (id_tipo_producto)
);

-- 6. Tabla: producto (Incluye parche de precio_sugerido)
CREATE TABLE public.producto (
    id_producto uuid NOT NULL,
    nombre character varying(155),
    precio_sugerido numeric(8, 2) NOT NULL,
    activo boolean,
    comentarios text,
    CONSTRAINT pk_producto PRIMARY KEY (id_producto)
);

-- 7. Tabla: producto_tipo_producto (Incluye parche de PK simple)
CREATE TABLE public.producto_tipo_producto (
    id_producto_tipo_producto uuid NOT NULL,
    id_producto uuid,
    id_tipo_producto uuid,
    fecha_creacion timestamp with time zone,
    observaciones text,
    CONSTRAINT pk_producto_tipo_producto PRIMARY KEY (id_producto_tipo_producto),
    CONSTRAINT fk_producto_tipo_producto_producto FOREIGN KEY (id_producto) REFERENCES public.producto(id_producto) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT producto_tipo_producto_id_tipo_producto_fkey FOREIGN KEY (id_tipo_producto) REFERENCES public.tipo_producto(id_tipo_producto) ON UPDATE CASCADE ON DELETE RESTRICT
);

-- 8. Tabla: tipo_caracteristica
CREATE TABLE public.tipo_caracteristica (
    id_tipo_caracteristica uuid NOT NULL,
    nombre character varying(155),
    expresion_regular text DEFAULT '.'::text,
    activo boolean DEFAULT true,
    observaciones text,
    CONSTRAINT pk_tipo_caracteristica PRIMARY KEY (id_tipo_caracteristica)
);

-- 9. Tabla: caracteristica
CREATE TABLE public.caracteristica (
    id_caracteristica uuid NOT NULL,
    id_tipo_caracteristica uuid,
    nombre character varying(155),
    activo boolean,
    observaciones text,
    CONSTRAINT pk_caracteristica PRIMARY KEY (id_caracteristica),
    CONSTRAINT fk_caracteristica_tipo_caracteristica FOREIGN KEY (id_tipo_caracteristica) REFERENCES public.tipo_caracteristica(id_tipo_caracteristica) ON UPDATE CASCADE ON DELETE RESTRICT
);

-- 10. Tabla: producto_caracteristica
CREATE TABLE public.producto_caracteristica (
    id_producto_caracteristica uuid NOT NULL,
    id_producto uuid,
    id_caracteristica uuid,
    valor text,
    CONSTRAINT pk_producto_caracteristica PRIMARY KEY (id_producto_caracteristica),
    CONSTRAINT fk_producto_caracteristica_producto FOREIGN KEY (id_producto) REFERENCES public.producto(id_producto) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_producto_caracteristica_caracteristica FOREIGN KEY (id_caracteristica) REFERENCES public.caracteristica(id_caracteristica) ON UPDATE CASCADE ON DELETE RESTRICT
);

-- 11. Tabla: tipo_descuento
CREATE TABLE public.tipo_descuento (
    id_tipo_descuento uuid NOT NULL,
    nombre character varying,
    activo boolean DEFAULT true,
    descuento_maximo integer,
    observaciones text,
    CONSTRAINT pk_tipo_descuento PRIMARY KEY (id_tipo_descuento)
);

-- 12. Tabla: descuento
CREATE TABLE public.descuento (
    id_descuento uuid NOT NULL,
    id_tipo_descuento uuid,
    nombre character varying(155),
    fecha_desde timestamp with time zone DEFAULT now(),
    fecha_hasta timestamp with time zone,
    observaciones text,
    CONSTRAINT pk_descuento PRIMARY KEY (id_descuento),
    CONSTRAINT fk_descuento_tipo_descuento FOREIGN KEY (id_tipo_descuento) REFERENCES public.tipo_descuento(id_tipo_descuento) ON UPDATE CASCADE ON DELETE RESTRICT
);

-- 13. Tabla: descuento_producto
CREATE TABLE public.descuento_producto (
    id_descuento_producto uuid NOT NULL,
    id_descuento uuid,
    id_producto uuid,
    fecha_desde timestamp with time zone,
    fecha_hasta timestamp with time zone,
    valor integer,
    observaciones text,
    CONSTRAINT pk_descuento_producto PRIMARY KEY (id_descuento_producto),
    CONSTRAINT fk_descuento_producto_descuento FOREIGN KEY (id_descuento) REFERENCES public.descuento(id_descuento) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_descuento_producto_producto FOREIGN KEY (id_producto) REFERENCES public.producto(id_producto) ON UPDATE CASCADE ON DELETE RESTRICT
);

-- 14. Tabla: orden
CREATE TABLE public.orden (
    id_orden uuid NOT NULL,
    id_empleado_rol uuid,
    fecha_creacion timestamp with time zone DEFAULT now(),
    CONSTRAINT pk_orden PRIMARY KEY (id_orden),
    CONSTRAINT fk_orden_empleado_rol FOREIGN KEY (id_empleado_rol) REFERENCES public.empleado_rol(id_empleado_rol) ON UPDATE CASCADE ON DELETE RESTRICT
);

-- 15. Tabla: orden_producto
CREATE TABLE public.orden_producto (
    id_orden_producto uuid NOT NULL,
    id_orden uuid,
    id_producto uuid,
    precio numeric(8,2),
    observaciones text,
    CONSTRAINT pk_orden_producto PRIMARY KEY (id_orden_producto),
    CONSTRAINT fk_orden_producto_orden FOREIGN KEY (id_orden) REFERENCES public.orden(id_orden) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_orden_producto_producto FOREIGN KEY (id_producto) REFERENCES public.producto(id_producto) ON UPDATE CASCADE ON DELETE RESTRICT
);

-- 16. Tabla: factura
CREATE TABLE public.factura (
    id_factura uuid NOT NULL,
    id_caja uuid,
    id_empleado_rol uuid,
    cliente character varying(155),
    fecha_facturacion timestamp with time zone DEFAULT now(),
    estado character varying(20),
    observaciones text,
    CONSTRAINT pk_factura PRIMARY KEY (id_factura),
    CONSTRAINT fk_factura_caja FOREIGN KEY (id_caja) REFERENCES public.caja(id_caja) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_factura_empleado_rol FOREIGN KEY (id_empleado_rol) REFERENCES public.empleado_rol(id_empleado_rol) ON UPDATE CASCADE ON DELETE RESTRICT
);

-- 17. Tabla: factura_orden_producto
CREATE TABLE public.factura_orden_producto (
    id_factura_orden_producto uuid NOT NULL,
    id_factura uuid,
    id_orden_producto uuid,
    precio numeric(8,2),
    observaciones text,
    CONSTRAINT pk_factura_orden_producto PRIMARY KEY (id_factura_orden_producto),
    CONSTRAINT fk_factura_orden_producto_factura FOREIGN KEY (id_factura) REFERENCES public.factura(id_factura) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_factura_orden_producto_orden_producto FOREIGN KEY (id_orden_producto) REFERENCES public.orden_producto(id_orden_producto) ON UPDATE CASCADE ON DELETE RESTRICT
);

-- 18. Tabla: pago
CREATE TABLE public.pago (
    id_pago uuid NOT NULL,
    id_factura uuid,
    estado character varying(20),
    observaciones text,
    CONSTRAINT pk_pago PRIMARY KEY (id_pago),
    CONSTRAINT fk_pago_factura FOREIGN KEY (id_factura) REFERENCES public.factura(id_factura) ON UPDATE CASCADE ON DELETE RESTRICT
);

-- 19. Tabla: pago_detalle
CREATE TABLE public.pago_detalle (
    id_pago_detalle uuid NOT NULL,
    id_pago uuid,
    monto numeric(8,2),
    tipo_pago character varying(20),
    observaciones text,
    referencia_externa text,
    CONSTRAINT pk_pago_detalle PRIMARY KEY (id_pago_detalle),
    CONSTRAINT fk_pago_detalle_pago FOREIGN KEY (id_pago) REFERENCES public.pago(id_pago) ON UPDATE CASCADE ON DELETE RESTRICT
);