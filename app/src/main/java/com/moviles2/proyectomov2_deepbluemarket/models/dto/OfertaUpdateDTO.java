package com.moviles2.proyectomov2_deepbluemarket.models.dto;

import com.google.gson.annotations.SerializedName;

/**
 * DTO usado para actualizar una oferta existente (PATCH a Supabase).
 * No incluye "id" (identity, no se puede actualizar), "producto_id"
 * ni "usuario_id" (no deberían cambiar) ni "fecha" (se establece
 * solo al crear).
 *
 * En la práctica, las ofertas suelen ser inmutables (no se editan,
 * se crean o se eliminan); este DTO se incluye por consistencia
 * en caso de que se necesite actualizar el monto antes de que
 * sea aceptada/rechazada.
 */
public class OfertaUpdateDTO {

    @SerializedName("monto")
    private double monto;

    public OfertaUpdateDTO() {
    }

    public OfertaUpdateDTO(double monto) {
        this.monto = monto;
    }

    public double getMonto() {
        return monto;
    }

    public void setMonto(double monto) {
        this.monto = monto;
    }
}