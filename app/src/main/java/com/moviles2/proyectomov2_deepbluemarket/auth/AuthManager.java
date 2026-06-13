package com.moviles2.proyectomov2_deepbluemarket.auth;

import android.app.Activity;
import android.content.Intent;
import android.widget.Toast;

import com.auth0.android.Auth0;
import com.auth0.android.authentication.AuthenticationException;
import com.auth0.android.callback.Callback;
import com.auth0.android.provider.WebAuthProvider;
import com.auth0.android.result.Credentials;
import com.auth0.android.result.UserProfile;
import com.moviles2.proyectomov2_deepbluemarket.models.Usuario;
import com.moviles2.proyectomov2_deepbluemarket.ui.home.HomeActivity;
import com.moviles2.proyectomov2_deepbluemarket.utils.SessionManager;
import com.moviles2.proyectomov2_deepbluemarket.auth.UserSyncManager;

public class AuthManager {

    private final Activity activity;
    private final Auth0 auth0;
    private final SessionManager sessionManager;

    private final UserSyncManager userSyncManager;

    public AuthManager(Activity activity, String clientId, String domain) {
        this.activity = activity;
        this.auth0 = Auth0.getInstance(clientId, domain);
        this.sessionManager = new SessionManager(activity);
        this.userSyncManager = new UserSyncManager(activity);
    }

    /**
     * Inicia el flujo de login con Auth0 usando WebAuthProvider.
     * Al obtener las credenciales, guarda los datos del usuario en SessionManager
     * y navega hacia HomeActivity.
     */
    public void login() {
        WebAuthProvider.login(auth0)
                .withScheme("deepbluemarket")
                .withScope("openid profile email")
                .start(activity, new Callback<Credentials, AuthenticationException>() {
                    @Override
                    public void onSuccess(Credentials credentials) {
                        guardarUsuario(credentials);

                        UserProfile profile = credentials.getUser();

                        if (profile == null) {
                            Toast.makeText(activity, "Error perfil Auth0", Toast.LENGTH_LONG).show();
                            return;
                        }

                        userSyncManager.sincronizarUsuario(
                                profile.getId(),
                                profile.getName(),
                                profile.getEmail(),
                                Boolean.TRUE.equals(profile.isEmailVerified()),
                                new UserSyncManager.SyncCallback() {
                                    @Override
                                    public void onSuccess(Usuario usuario) {
                                        irAHome();
                                    }

                                    @Override
                                    public void onError(String errorMessage) {
                                        Toast.makeText(activity,
                                                "Error sync usuario: " + errorMessage,
                                                Toast.LENGTH_LONG).show();
                                    }
                                }
                        );
                    }

                    @Override
                    public void onFailure(AuthenticationException error) {
                        Toast.makeText(activity,
                                "Error al iniciar sesión: " + error.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    /**
     * Inicia el flujo de registro con Auth0, forzando la pantalla de
     * "Sign Up" mediante el parámetro screen_hint=signup.
     */
    public void registro() {
        WebAuthProvider.login(auth0)
                .withScheme("deepbluemarket")
                .withScope("openid profile email")
                .withParameters(java.util.Collections.singletonMap("screen_hint", "signup"))
                .start(activity, new Callback<Credentials, AuthenticationException>() {
                    @Override
                    public void onSuccess(Credentials credentials) {
                        UserProfile profile = credentials.getUser();

                        if (profile == null) return;

                        userSyncManager.sincronizarUsuario(
                                profile.getId(),
                                profile.getName(),
                                profile.getEmail(),
                                Boolean.TRUE.equals(profile.isEmailVerified()),
                                new UserSyncManager.SyncCallback() {
                                    @Override
                                    public void onSuccess(Usuario usuario) {
                                        irAHome();
                                    }

                                    @Override
                                    public void onError(String errorMessage) {
                                        Toast.makeText(activity, "Error sync: " + errorMessage, Toast.LENGTH_LONG).show();
                                    }
                                }
                        );
                    }

                    @Override
                    public void onFailure(AuthenticationException error) {
                        Toast.makeText(activity,
                                "Error al registrarse: " + error.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    /**
     * Cierra la sesión en Auth0 y limpia la sesión local.
     */
    public void logout(Runnable onLogoutComplete) {
        WebAuthProvider.logout(auth0)
                .withScheme("deepbluemarket")
                .start(activity, new Callback<Void, AuthenticationException>() {
                    @Override
                    public void onSuccess(Void result) {
                        sessionManager.cerrarSesion();
                        if (onLogoutComplete != null) {
                            onLogoutComplete.run();
                        }
                    }

                    @Override
                    public void onFailure(AuthenticationException error) {
                        Toast.makeText(activity,
                                "Error al cerrar sesión: " + error.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    /**
     * Obtiene el usuario autenticado actualmente desde SessionManager.
     */
    public boolean hayUsuarioAutenticado() {
        return sessionManager.haySesionActiva();
    }

    public String getAuth0IdActual() {
        return sessionManager.getAuth0Id();
    }

    public String getEmailActual() {
        return sessionManager.getEmail();
    }

    public String getNombreActual() {
        return sessionManager.getNombre();
    }

    /**
     * Guarda los datos del usuario autenticado en SessionManager
     * a partir de las credenciales obtenidas de Auth0.
     */
    private void guardarUsuario(Credentials credentials) {
        UserProfile profile = credentials.getUser();

        if (profile != null) {
            sessionManager.guardarAuth0Id(profile.getId());
            sessionManager.guardarEmail(profile.getEmail());
            sessionManager.guardarNombre(profile.getName());
            sessionManager.guardarEmailVerificado(Boolean.TRUE.equals(profile.isEmailVerified()));
        }
    }

    /**
     * Navega hacia HomeActivity después de un login exitoso.
     */
    private void irAHome() {
        Intent intent = new Intent(activity, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        activity.startActivity(intent);
        activity.finish();
    }
}