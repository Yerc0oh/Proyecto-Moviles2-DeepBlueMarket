package com.moviles2.proyectomov2_deepbluemarket.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {

    private static final String PREF_NAME = "DeepBlueMarketSession";

    private static final String KEY_AUTH0_ID = "auth0_id";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_NOMBRE = "nombre";
    private static final String KEY_EMAIL_VERIFICADO = "email_verificado";

    private final SharedPreferences prefs;

    public SessionManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void guardarAuth0Id(String auth0Id) {
        prefs.edit().putString(KEY_AUTH0_ID, auth0Id).apply();
    }

    public void guardarEmail(String email) {
        prefs.edit().putString(KEY_EMAIL, email).apply();
    }

    public void guardarNombre(String nombre) {
        prefs.edit().putString(KEY_NOMBRE, nombre).apply();
    }

    public void guardarEmailVerificado(boolean verificado) {
        prefs.edit().putBoolean(KEY_EMAIL_VERIFICADO, verificado).apply();
    }

    public String getAuth0Id() {
        return prefs.getString(KEY_AUTH0_ID, null);
    }

    public String getEmail() {
        return prefs.getString(KEY_EMAIL, null);
    }

    public String getNombre() {
        return prefs.getString(KEY_NOMBRE, null);
    }

    public boolean isEmailVerificado() {
        return prefs.getBoolean(KEY_EMAIL_VERIFICADO, false);
    }

    public boolean haySesionActiva() {
        return getAuth0Id() != null;
    }

    public void cerrarSesion() {
        prefs.edit().clear().apply();
    }
}