package com.moviles2.proyectomov2_deepbluemarket.models;

import com.google.gson.annotations.SerializedName;

/**
 * Representa una fila de la tabla "oferta" con el usuario ofertante
 * embebido mediante el resource embedding de PostgREST:
 *
 *   GET /oferta?select=*,usuarios(nombre,telefono)&producto_id=eq.X
 *
 * Supabase devuelve el objeto relacionado anidado bajo la clave del
 * nombre de la tabla referenciada ("usuarios").
 */
public class OfertaConUsuario {

    @SerializedName("id")
    private long id;

    @SerializedName("producto_id")
    private long productoId;

    @SerializedName("usuario_id")
    private long usuarioId;

    @SerializedName("monto")
    private double monto;

    @SerializedName("fecha")
    private String fecha;

    @SerializedName("usuarios")
    private UsuarioResumen usuarios;

    @SerializedName("productos")
    private ProductoResumen producto;

    public long getId() {
        return id;
    }

    public long getProductoId() {
        return productoId;
    }

    public long getUsuarioId() {
        return usuarioId;
    }

    public double getMonto() {
        return monto;
    }

    public String getFecha() {
        return fecha;
    }

    public UsuarioResumen getUsuarios() {
        return usuarios;
    }

    public ProductoResumen getProducto() {
        return producto;
    }

    /**
     * Subconjunto de campos del producto embebidos en la
     * respuesta de la oferta (usado en el polling de notificaciones).
     */
    public static class ProductoResumen {

        @SerializedName("titulo")
        private String titulo;

        public String getTitulo() {
            return titulo;
        }
    }

    /**
     * Subconjunto de campos del usuario ofertante embebidos
     * en la respuesta de la oferta.
     */
    public static class UsuarioResumen {

        @SerializedName("nombre")
        private String nombre;

        @SerializedName("telefono")
        private String telefono;

        public String getNombre() {
            return nombre;
        }

        public String getTelefono() {
            return telefono;
        }
    }
}