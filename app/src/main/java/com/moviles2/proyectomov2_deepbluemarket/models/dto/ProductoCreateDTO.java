package com.moviles2.proyectomov2_deepbluemarket.models.dto;

import com.google.gson.annotations.SerializedName;

/**
 * DTO usado para crear un nuevo producto (POST a Supabase).
 * No incluye "id" ni "fecha_publicacion" porque son generados
 * automáticamente por la base de datos (identity / default now()).
 */
public class ProductoCreateDTO {

    @SerializedName("usuario_id")
    private long usuarioId;

    @SerializedName("titulo")
    private String titulo;

    @SerializedName("descripcion")
    private String descripcion;

    @SerializedName("categoria")
    private String categoria;

    @SerializedName("precio")
    private double precio;

    @SerializedName("imagen_url")
    private String imagenUrl;

    @SerializedName("estado")
    private String estado;

    public ProductoCreateDTO() {
    }

    public ProductoCreateDTO(long usuarioId, String titulo, String descripcion,
                             String categoria, double precio,
                             String imagenUrl, String estado) {
        this.usuarioId = usuarioId;
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.categoria = categoria;
        this.precio = precio;
        this.imagenUrl = imagenUrl;
        this.estado = estado;
    }

    public long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(long usuarioId) {
        this.usuarioId = usuarioId;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public double getPrecio() {
        return precio;
    }

    public void setPrecio(double precio) {
        this.precio = precio;
    }

    public String getImagenUrl() {
        return imagenUrl;
    }

    public void setImagenUrl(String imagenUrl) {
        this.imagenUrl = imagenUrl;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}