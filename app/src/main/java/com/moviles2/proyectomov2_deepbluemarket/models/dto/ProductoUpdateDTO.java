package com.moviles2.proyectomov2_deepbluemarket.models.dto;

import com.google.gson.annotations.SerializedName;

/**
 * DTO usado para actualizar un producto existente (PATCH a Supabase).
 * No incluye "id" (identity, no se puede actualizar), "usuario_id"
 * (dueño del producto, no debería cambiar) ni "fecha_publicacion"
 * (se establece solo al crear).
 */
public class ProductoUpdateDTO {

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

    public ProductoUpdateDTO() {
    }

    public ProductoUpdateDTO(String titulo, String descripcion, String categoria,
                             double precio, String imagenUrl, String estado) {
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.categoria = categoria;
        this.precio = precio;
        this.imagenUrl = imagenUrl;
        this.estado = estado;
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