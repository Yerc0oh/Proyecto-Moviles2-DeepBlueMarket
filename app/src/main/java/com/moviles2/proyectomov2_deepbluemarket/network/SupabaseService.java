package com.moviles2.proyectomov2_deepbluemarket.network;

import com.moviles2.proyectomov2_deepbluemarket.models.Oferta;
import com.moviles2.proyectomov2_deepbluemarket.models.Producto;
import com.moviles2.proyectomov2_deepbluemarket.models.Usuario;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface SupabaseService {

    // ---------- USUARIOS ----------

    @GET("usuario")
    Call<List<Usuario>> getUsuarios();

    @GET("usuario")
    Call<List<Usuario>> getUsuarioByAuth0Id(@Query("auth0_id") String auth0Id);

    @Headers("Prefer: return=representation")
    @POST("usuario")
    Call<List<Usuario>> crearUsuario(@Body Usuario usuario);

    @Headers("Prefer: return=representation")
    @PATCH("usuario")
    Call<List<Usuario>> actualizarUsuario(@Query("id") String id, @Body Usuario usuario);

    // ---------- PRODUCTOS ----------

    @GET("producto")
    Call<List<Producto>> getProductos();

    @GET("producto")
    Call<List<Producto>> getProductoById(@Query("id") String id);

    @Headers("Prefer: return=representation")
    @POST("producto")
    Call<List<Producto>> crearProducto(@Body Producto producto);

    // ---------- OFERTAS ----------

    @Headers("Prefer: return=representation")
    @POST("oferta")
    Call<List<Oferta>> crearOferta(@Body Oferta oferta);

    @GET("oferta")
    Call<List<Oferta>> getOfertasByProducto(@Query("producto_id") String productoId);
}