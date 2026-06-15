package com.moviles2.proyectomov2_deepbluemarket.network;

import com.moviles2.proyectomov2_deepbluemarket.models.Oferta;
import com.moviles2.proyectomov2_deepbluemarket.models.OfertaConUsuario;
import com.moviles2.proyectomov2_deepbluemarket.models.Producto;
import com.moviles2.proyectomov2_deepbluemarket.models.Usuario;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Query;

import com.moviles2.proyectomov2_deepbluemarket.models.dto.*;

import com.moviles2.proyectomov2_deepbluemarket.utils.Constants;

public interface SupabaseService {

    // ---------- USUARIOS ----------

    @GET(Constants.TABLA_USUARIOS)
    Call<List<Usuario>> getUsuarios();

    @GET(Constants.TABLA_USUARIOS)
    Call<List<Usuario>> getUsuarioByAuth0Id(@Query("auth0_id") String auth0Id);

    @Headers("Prefer: return=representation")
    @POST(Constants.TABLA_USUARIOS)
    Call<List<Usuario>> crearUsuario(@Body UsuarioCreateDTO usuario);

    @Headers("Prefer: return=representation")
    @PATCH(Constants.TABLA_USUARIOS)
    Call<List<Usuario>> actualizarUsuario(@Query("id") String id, @Body UsuarioUpdateDTO usuario);

    // ---------- PRODUCTOS ----------

    @GET(Constants.TABLA_PRODUCTOS)
    Call<List<Producto>> getProductos();

    @GET(Constants.TABLA_PRODUCTOS)
    Call<List<Producto>> getProductoById(@Query("id") String id);

    @Headers("Prefer: return=representation")
    @POST(Constants.TABLA_PRODUCTOS)
    Call<List<Producto>> crearProducto(@Body ProductoCreateDTO producto);

    @Headers("Prefer: return=representation")
    @PATCH(Constants.TABLA_PRODUCTOS)
    Call<List<Producto>> actualizarProducto(@Query("id") String id, @Body ProductoUpdateDTO producto);

    // ---------- OFERTAS ----------

    @Headers("Prefer: return=representation")
    @POST(Constants.TABLA_OFERTAS)
    Call<List<Oferta>> crearOferta(@Body OfertaCreateDTO oferta);

    @GET(Constants.TABLA_OFERTAS)
    Call<List<Oferta>> getOfertasByProducto(@Query("producto_id") String productoId);

    @Headers("Prefer: return=representation")
    @PATCH(Constants.TABLA_OFERTAS)
    Call<List<Oferta>> actualizarOferta(@Query("id") String id, @Body OfertaUpdateDTO oferta);

    /**
     * Obtiene las ofertas de un producto con los datos del usuario
     * ofertante embebidos (nombre y teléfono), mediante resource
     * embedding de PostgREST.
     *
     * select = "*,usuarios(nombre,telefono)"
     */
    @GET(Constants.TABLA_OFERTAS)
    Call<List<OfertaConUsuario>> getOfertasConUsuarioByProducto(
            @Query("producto_id") String productoId,
            @Query("select") String select);

    /**
     * Elimina una oferta por su id (usado al "Rechazar" una oferta).
     */
    @DELETE(Constants.TABLA_OFERTAS)
    Call<Void> eliminarOferta(@Query("id") String id);

    /**
     * Obtiene ofertas creadas después de una fecha dada, para los
     * productos indicados (usado por el polling de notificaciones).
     *
     * producto_id debe ir en formato "in.(1,2,3)" y fecha en
     * formato "gt.<ISO_TIMESTAMP>".
     */
    @GET(Constants.TABLA_OFERTAS)
    Call<List<OfertaConUsuario>> getOfertasNuevas(
            @Query("producto_id") String productoIdsIn,
            @Query("fecha") String fechaGt,
            @Query("select") String select);
}