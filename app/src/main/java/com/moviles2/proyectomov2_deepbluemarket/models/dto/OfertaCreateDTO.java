package com.moviles2.proyectomov2_deepbluemarket.models.dto;

import com.google.gson.annotations.SerializedName;

/**
 * DTO usado para crear una nueva oferta (POST a Supabase).
 * No incluye "id" ni "fecha" porque son generados
 * automáticamente por la base de datos (identity / default now()).
 */
public class OfertaCreateDTO {

    @SerializedName("producto_id")
    private long productoId;

    @SerializedName("usuario_id")
    private long usuarioId;

    @SerializedName("monto")
    private double monto;

    public OfertaCreateDTO() {
    }

    public OfertaCreateDTO(long productoId, long usuarioId, double monto) {
        this.productoId = productoId;
        this.usuarioId = usuarioId;
        this.monto = monto;
    }

    public long getProductoId() {
        return productoId;
    }

    public void setProductoId(long productoId) {
        this.productoId = productoId;
    }

    public long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(long usuarioId) {
        this.usuarioId = usuarioId;
    }

    public double getMonto() {
        return monto;
    }

    public void setMonto(double monto) {
        this.monto = monto;
    }
}