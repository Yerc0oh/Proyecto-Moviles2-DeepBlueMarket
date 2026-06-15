package com.moviles2.proyectomov2_deepbluemarket.auth;

import android.content.Context;
import android.util.Log;

import com.moviles2.proyectomov2_deepbluemarket.models.Usuario;
import com.moviles2.proyectomov2_deepbluemarket.network.UserService;
import com.moviles2.proyectomov2_deepbluemarket.utils.SessionManager;

public class UserSyncManager {

    private static final String TAG = "UserSyncManager";

    private final UserService userService;
    private final SessionManager sessionManager;

    public UserSyncManager(Context context) {
        this.userService = UserService.getInstance();
        this.sessionManager = new SessionManager(context);
    }

    public interface SyncCallback {
        void onSuccess(Usuario usuario);
        void onError(String errorMessage);
    }

    /**
     * Sincroniza el usuario autenticado de Auth0 con Supabase.
     * - Si el usuario ya existe en Supabase, carga sus datos localmente.
     * - Si no existe, lo crea automáticamente usando los datos de Auth0.
     *
     * @param auth0Id   ID de Auth0 del usuario autenticado
     * @param nombre    Nombre obtenido del perfil de Auth0
     * @param correo    Correo obtenido del perfil de Auth0
     * @param verificado Estado de verificación de email de Auth0
     * @param callback  Callback con el resultado de la sincronización
     */
    public void sincronizarUsuario(String auth0Id, String nombre, String correo,
                                   boolean verificado, SyncCallback callback) {

        if (auth0Id == null || auth0Id.isEmpty()) {
            callback.onError("auth0_id no puede ser nulo o vacío");
            return;
        }

        Log.d(TAG, "Sincronizando usuario con auth0_id: " + auth0Id);

        userService.getUserByAuth0Id(auth0Id, new UserService.UserCallback() {
            @Override
            public void onSuccess(Usuario usuarioExistente) {
                Log.d(TAG, "Usuario ya existe en Supabase, cargando datos locales");
                guardarSesionLocal(usuarioExistente);
                callback.onSuccess(usuarioExistente);
            }

            @Override
            public void onError(String errorMessage) {
                // Si no se encontró el usuario, se crea automáticamente
                Log.d(TAG, "Usuario no encontrado en Supabase, creando nuevo registro");
                crearUsuarioNuevo(auth0Id, nombre, correo, verificado, callback);
            }
        });
    }

    /**
     * Crea un nuevo usuario en Supabase a partir de los datos de Auth0.
     */
    private void crearUsuarioNuevo(String auth0Id, String nombre, String correo,
                                   boolean verificado, SyncCallback callback) {

        Usuario nuevoUsuario = new Usuario();
        nuevoUsuario.setAuth0Id(auth0Id);
        nuevoUsuario.setNombre(nombre);
        nuevoUsuario.setCorreo(correo);
        nuevoUsuario.setVerificado(verificado);

        userService.createUser(nuevoUsuario, new UserService.UserCallback() {
            @Override
            public void onSuccess(Usuario usuarioCreado) {
                Log.d(TAG, "Usuario creado exitosamente en Supabase");
                guardarSesionLocal(usuarioCreado);
                callback.onSuccess(usuarioCreado);
            }

            @Override
            public void onError(String errorMessage) {
                Log.e(TAG, "Error al crear usuario en Supabase: " + errorMessage);
                callback.onError(errorMessage);
            }
        });
    }

    /**
     * Guarda la información del usuario en SessionManager para uso local.
     */
    private void guardarSesionLocal(Usuario usuario) {
        sessionManager.guardarAuth0Id(usuario.getAuth0Id());
        sessionManager.guardarEmail(usuario.getCorreo());
        sessionManager.guardarNombre(usuario.getNombre());
        sessionManager.guardarEmailVerificado(usuario.isVerificado());
        sessionManager.guardarFotoPerfil(usuario.getFotoPerfilUrl());
        sessionManager.guardarUsuarioId(usuario.getId());
    }
}