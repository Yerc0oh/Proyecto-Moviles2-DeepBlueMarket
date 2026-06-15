package com.moviles2.proyectomov2_deepbluemarket.models;

import com.google.gson.annotations.SerializedName;

public class Usuario {

    @SerializedName("id")
    private long id;

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

    @SerializedName("fecha_registro")
    private String fechaRegistro;

    // Constructor vacío (requerido por Gson)
    public Usuario() {
    }

    // Constructor completo
    public Usuario(long id, String auth0Id, String nombre, String correo,
                   String telefono, boolean verificado, String fotoPerfilUrl,
                   String fotoDocumentoUrl, String fechaRegistro) {
        this.id = id;
        this.auth0Id = auth0Id;
        this.nombre = nombre;
        this.correo = correo;
        this.telefono = telefono;
        this.verificado = verificado;
        this.fotoPerfilUrl = fotoPerfilUrl;
        this.fotoDocumentoUrl = fotoDocumentoUrl;
        this.fechaRegistro = fechaRegistro;
    }

    // Getters y Setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
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

    public String getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(String fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    @Override
    public String toString() {
        return "Usuario{" +
                "id=" + id +
                ", auth0Id='" + auth0Id + '\'' +
                ", nombre='" + nombre + '\'' +
                ", correo='" + correo + '\'' +
                ", telefono='" + telefono + '\'' +
                ", verificado=" + verificado +
                ", fotoPerfilUrl='" + fotoPerfilUrl + '\'' +
                ", fotoDocumentoUrl='" + fotoDocumentoUrl + '\'' +
                ", fechaRegistro=" + fechaRegistro +
                '}';
    }
}