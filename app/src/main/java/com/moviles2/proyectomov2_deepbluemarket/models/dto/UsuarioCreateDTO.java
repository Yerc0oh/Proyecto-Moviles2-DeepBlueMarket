package com.moviles2.proyectomov2_deepbluemarket.models.dto;

import com.google.gson.annotations.SerializedName;

/**
 * DTO usado para crear un nuevo usuario (POST a Supabase).
 * No incluye "id" ni "fecha_registro" porque son generados
 * automáticamente por la base de datos (identity / default now()).
 */
public class UsuarioCreateDTO {

    @SerializedName("auth0_id")
    private String auth0Id;

    @SerializedName("nombre")
    private String nombre;

    @SerializedName("correo")
    private String correo;

    @SerializedName("telefono")
    private String telefono;

    @SerializedName("verificado")
    private boolean verificado;

    @SerializedName("foto_perfil_url")
    private String fotoPerfilUrl;

    @SerializedName("foto_documento_url")
    private String fotoDocumentoUrl;

    public UsuarioCreateDTO() {
    }

    public UsuarioCreateDTO(String auth0Id, String nombre, String correo,
                            String telefono, boolean verificado,
                            String fotoPerfilUrl, String fotoDocumentoUrl) {
        this.auth0Id = auth0Id;
        this.nombre = nombre;
        this.correo = correo;
        this.telefono = telefono;
        this.verificado = verificado;
        this.fotoPerfilUrl = fotoPerfilUrl;
        this.fotoDocumentoUrl = fotoDocumentoUrl;
    }

    public String getAuth0Id() {
        return auth0Id;
    }

    public void setAuth0Id(String auth0Id) {
        this.auth0Id = auth0Id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public boolean isVerificado() {
        return verificado;
    }

    public void setVerificado(boolean verificado) {
        this.verificado = verificado;
    }

    public String getFotoPerfilUrl() {
        return fotoPerfilUrl;
    }

    public void setFotoPerfilUrl(String fotoPerfilUrl) {
        this.fotoPerfilUrl = fotoPerfilUrl;
    }

    public String getFotoDocumentoUrl() {
        return fotoDocumentoUrl;
    }

    public void setFotoDocumentoUrl(String fotoDocumentoUrl) {
        this.fotoDocumentoUrl = fotoDocumentoUrl;
    }
}