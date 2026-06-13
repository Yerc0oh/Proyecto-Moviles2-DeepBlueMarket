package com.moviles2.proyectomov2_deepbluemarket.models;

import com.google.gson.annotations.SerializedName;
import java.util.Date;

public class Oferta {

    @SerializedName("id")
    private long id;

    @SerializedName("producto_id")
    private long productoId;

    @SerializedName("usuario_id")
    private long usuarioId;

    @SerializedName("monto")
    private double monto;

    @SerializedName("fecha")
    private Date fecha;

    // Constructor vacío (requerido por Gson)
    public Oferta() {
    }

    // Constructor completo
    public Oferta(long id, long productoId, long usuarioId, double monto, Date fecha) {
        this.id = id;
        this.productoId = productoId;
        this.usuarioId = usuarioId;
        this.monto = monto;
        this.fecha = fecha;
    }

    // Getters y Setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
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

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    @Override
    public String toString() {
        return "Oferta{" +
                "id=" + id +
                ", productoId=" + productoId +
                ", usuarioId=" + usuarioId +
                ", monto=" + monto +
                ", fecha=" + fecha +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Oferta oferta = (Oferta) o;
        return id == oferta.id;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(id);
    }
}