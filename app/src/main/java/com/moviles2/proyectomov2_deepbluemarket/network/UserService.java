package com.moviles2.proyectomov2_deepbluemarket.network;

import android.util.Log;

import com.moviles2.proyectomov2_deepbluemarket.models.Usuario;
import com.moviles2.proyectomov2_deepbluemarket.utils.Constants;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class UserService {

    private static final String TAG = "UserService";
    private final SupabaseService supabaseService;
    private static UserService instance;

    // Constructor privado para Singleton
    private UserService() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.SUPABASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        supabaseService = retrofit.create(SupabaseService.class);
    }

    // Obtener instancia única (Singleton)
    public static synchronized UserService getInstance() {
        if (instance == null) {
            instance = new UserService();
        }
        return instance;
    }

    /**
     * Interfaz para callbacks de operaciones con usuarios
     */
    public interface UserCallback {
        void onSuccess(Usuario user);
        void onError(String errorMessage);
    }

    public interface UserListCallback {
        void onSuccess(List<Usuario> users);
        void onError(String errorMessage);
    }

    public interface BooleanCallback {
        void onResult(boolean exists);
        void onError(String errorMessage);
    }

    /**
     * Obtiene un usuario por su auth0_id
     * @param auth0Id El ID de Auth0 del usuario
     * @param callback Callback para manejar la respuesta
     */
    public void getUserByAuth0Id(String auth0Id, UserCallback callback) {
        if (auth0Id == null || auth0Id.isEmpty()) {
            callback.onError("auth0_id no puede ser nulo o vacío");
            return;
        }

        Log.d(TAG, "Buscando usuario con auth0_id: " + auth0Id);

        supabaseService.getUsuarioByAuth0Id(auth0Id).enqueue(new Callback<List<Usuario>>() {
            @Override
            public void onResponse(Call<List<Usuario>> call, Response<List<Usuario>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Usuario> usuarios = response.body();
                    if (!usuarios.isEmpty()) {
                        Usuario usuario = usuarios.get(0);
                        Log.d(TAG, "Usuario encontrado: " + usuario.getNombre());
                        callback.onSuccess(usuario);
                    } else {
                        Log.d(TAG, "No se encontró usuario con auth0_id: " + auth0Id);
                        callback.onError("Usuario no encontrado");
                    }
                } else {
                    Log.e(TAG, "Error en respuesta: " + response.code() + " - " + response.message());
                    callback.onError("Error al obtener usuario: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<List<Usuario>> call, Throwable t) {
                Log.e(TAG, "Error de red al obtener usuario", t);
                callback.onError("Error de conexión: " + t.getMessage());
            }
        });
    }

    /**
     * Crea un nuevo usuario en Supabase
     * @param user Objeto Usuario a crear
     * @param callback Callback para manejar la respuesta
     */
    public void createUser(Usuario user, UserCallback callback) {
        if (user == null) {
            callback.onError("El usuario no puede ser nulo");
            return;
        }

        if (user.getAuth0Id() == null || user.getAuth0Id().isEmpty()) {
            callback.onError("auth0_id es requerido para crear usuario");
            return;
        }

        Log.d(TAG, "Creando nuevo usuario: " + user.getNombre());

        supabaseService.crearUsuario(user).enqueue(new Callback<List<Usuario>>() {
            @Override
            public void onResponse(Call<List<Usuario>> call, Response<List<Usuario>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Usuario> usuarios = response.body();
                    if (!usuarios.isEmpty()) {
                        Usuario usuarioCreado = usuarios.get(0);
                        Log.d(TAG, "Usuario creado exitosamente con ID: " + usuarioCreado.getId());
                        callback.onSuccess(usuarioCreado);
                    } else {
                        callback.onError("Respuesta vacía al crear usuario");
                    }
                } else {
                    Log.e(TAG, "Error al crear usuario: " + response.code() + " - " + response.message());
                    callback.onError("Error al crear usuario: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<List<Usuario>> call, Throwable t) {
                Log.e(TAG, "Error de red al crear usuario", t);
                callback.onError("Error de conexión: " + t.getMessage());
            }
        });
    }

    /**
     * Actualiza un usuario existente
     * Nota: Supabase usa PUT para actualizar, necesitas implementar @PUT en SupabaseService
     * @param user Objeto Usuario con datos actualizados
     * @param callback Callback para manejar la respuesta
     */
    public void updateUser(Usuario user, UserCallback callback) {
        if (user == null) {
            callback.onError("El usuario no puede ser nulo");
            return;
        }

        if (user.getId() <= 0) {
            callback.onError("ID de usuario inválido para actualización");
            return;
        }

        Log.d(TAG, "Actualizando usuario con ID: " + user.getId());

        // Nota: Necesitas agregar el método @PUT en SupabaseService
        // Por ahora usamos un workaround con GET + POST no es recomendado

        // Método alternativo: Usar consulta directa con Retrofit
        // Por ahora indicamos que se necesita implementar @PUT
        callback.onError("Método updateUser requiere implementar @PUT en SupabaseService. " +
                "Agrega: @PUT(\"usuario?id=eq.{id}\") Call<List<Usuario>> actualizarUsuario(@Path(\"id\") long id, @Body Usuario usuario)");
    }

    /**
     * Verifica si existe un usuario con el auth0_id proporcionado
     * @param auth0Id El ID de Auth0 del usuario
     * @param callback Callback que retorna true si existe, false si no
     */
    public void checkIfUserExists(String auth0Id, BooleanCallback callback) {
        if (auth0Id == null || auth0Id.isEmpty()) {
            callback.onError("auth0_id no puede ser nulo o vacío");
            return;
        }

        Log.d(TAG, "Verificando existencia de usuario con auth0_id: " + auth0Id);

        supabaseService.getUsuarioByAuth0Id(auth0Id).enqueue(new Callback<List<Usuario>>() {
            @Override
            public void onResponse(Call<List<Usuario>> call, Response<List<Usuario>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    boolean exists = !response.body().isEmpty();
                    Log.d(TAG, "Usuario existe: " + exists);
                    callback.onResult(exists);
                } else {
                    Log.e(TAG, "Error al verificar usuario: " + response.code());
                    callback.onResult(false); // Asumimos que no existe si hay error
                }
            }

            @Override
            public void onFailure(Call<List<Usuario>> call, Throwable t) {
                Log.e(TAG, "Error de red al verificar usuario", t);
                callback.onError("Error de conexión: " + t.getMessage());
            }
        });
    }

    /**
     * Método auxiliar para obtener usuario de forma síncrona (para uso en ViewModels)
     * @param auth0Id El ID de Auth0 del usuario
     * @return Usuario o null si no existe/error
     */
    public Usuario getUserByAuth0IdSync(String auth0Id) throws Exception {
        if (auth0Id == null || auth0Id.isEmpty()) {
            throw new IllegalArgumentException("auth0_id no puede ser nulo o vacío");
        }

        retrofit2.Response<List<Usuario>> response = supabaseService.getUsuarioByAuth0Id(auth0Id).execute();

        if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
            return response.body().get(0);
        }

        return null;
    }
}