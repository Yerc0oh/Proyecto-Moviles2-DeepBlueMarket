package com.moviles2.proyectomov2_deepbluemarket.network;

import android.util.Log;

import com.google.gson.Gson;
import com.moviles2.proyectomov2_deepbluemarket.models.Oferta;
import com.moviles2.proyectomov2_deepbluemarket.models.dto.OfertaCreateDTO;
import com.moviles2.proyectomov2_deepbluemarket.utils.Constants;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class OfertaService {

    private static final String TAG = "OfertaService";

    private static final String SUPABASE_URL = Constants.SUPABASE_URL;
    private static final String SUPABASE_ANON_KEY = Constants.SUPABASE_ANON_KEY;
    private static final String TABLE = "/rest/v1/ofertas";

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    // ─── INTERFACES ─────────────────────────────────────────────────────────────

    public interface OfertaCallback {
        void onSuccess(Oferta oferta);
        void onError(String error);
    }

    public interface OfertaListCallback {
        void onSuccess(List<OfertaConUsuario> ofertas);
        void onError(String error);
    }

    public interface ActionCallback {
        void onSuccess();
        void onError(String error);
    }

    /**
     * Representa una oferta junto con el nombre y teléfono del usuario
     * ofertante (obtenidos vía resource embedding de PostgREST), y
     * opcionalmente el título del producto (usado en polling de notis).
     */
    public static class OfertaConUsuario {
        public long id;
        public long productoId;
        public long usuarioId;
        public double monto;
        public String fecha;
        public String nombreOfertante;
        public String telefonoOfertante;
        public String tituloProducto;
    }

    // ─── CREATE ─────────────────────────────────────────────────────────────────

    public void crearOferta(long productoId, long usuarioId, double monto, ActionCallback callback) {
        executor.execute(() -> {
            try {
                URL url = new URL(SUPABASE_URL + TABLE);
                HttpURLConnection conn = buildConnection(url, "POST");
                conn.setDoOutput(true);
                conn.setRequestProperty("Prefer", "return=minimal");

                OfertaCreateDTO dto = new OfertaCreateDTO(productoId, usuarioId, monto);
                String bodyJson = new Gson().toJson(dto);

                try (OutputStream os = conn.getOutputStream()) {
                    os.write(bodyJson.getBytes(StandardCharsets.UTF_8));
                }

                int code = conn.getResponseCode();
                if (code == 201 || code == 200) {
                    callback.onSuccess();
                } else {
                    callback.onError("Error " + code + ": " + readError(conn));
                }
            } catch (Exception e) {
                Log.e(TAG, "crearOferta", e);
                callback.onError(e.getMessage());
            }
        });
    }

    // ─── GET OFERTAS POR PRODUCTO (con nombre/telefono del ofertante) ───────────

    public void getOfertasByProducto(long productoId, OfertaListCallback callback) {
        executor.execute(() -> {
            try {
                URL url = new URL(SUPABASE_URL + TABLE
                        + "?select=*,usuarios(nombre,telefono)"
                        + "&producto_id=eq." + productoId
                        + "&order=fecha.desc");

                HttpURLConnection conn = buildConnection(url, "GET");
                int code = conn.getResponseCode();
                if (code == 200) {
                    callback.onSuccess(parseOfertaConUsuarioList(readResponse(conn)));
                } else {
                    callback.onError("Error " + code + ": " + readError(conn));
                }
            } catch (Exception e) {
                Log.e(TAG, "getOfertasByProducto", e);
                callback.onError(e.getMessage());
            }
        });
    }

    // ─── GET OFERTAS NUEVAS (para polling de notificaciones) ────────────────────

    /**
     * Obtiene ofertas creadas después de fechaIso para los productos
     * indicados, incluyendo nombre del ofertante y título del producto.
     *
     * @param productoIds ids de productos del usuario actual
     * @param fechaIso    fecha ISO 8601 UTC desde la cual buscar (exclusiva)
     */
    public void getOfertasNuevas(List<Long> productoIds, String fechaIso, OfertaListCallback callback) {
        if (productoIds == null || productoIds.isEmpty()) {
            callback.onSuccess(new ArrayList<>());
            return;
        }

        executor.execute(() -> {
            try {
                StringBuilder idsParam = new StringBuilder("in.(");
                for (int i = 0; i < productoIds.size(); i++) {
                    idsParam.append(productoIds.get(i));
                    if (i < productoIds.size() - 1) idsParam.append(",");
                }
                idsParam.append(")");

                String encodedFecha = java.net.URLEncoder.encode("gt." + fechaIso, "UTF-8");
                String encodedIds = java.net.URLEncoder.encode(idsParam.toString(), "UTF-8");

                URL url = new URL(SUPABASE_URL + TABLE
                        + "?select=*,usuarios(nombre,telefono),productos(titulo)"
                        + "&producto_id=" + encodedIds
                        + "&fecha=" + encodedFecha
                        + "&order=fecha.asc");

                HttpURLConnection conn = buildConnection(url, "GET");
                int code = conn.getResponseCode();
                if (code == 200) {
                    callback.onSuccess(parseOfertaConUsuarioList(readResponse(conn)));
                } else {
                    Log.e(TAG, "getOfertasNuevas error " + code + ": " + readError(conn));
                    callback.onError("Error " + code);
                }
            } catch (Exception e) {
                Log.e(TAG, "getOfertasNuevas", e);
                callback.onError(e.getMessage());
            }
        });
    }

    // ─── ELIMINAR OFERTA (Rechazar) ──────────────────────────────────────────────

    public void eliminarOferta(long ofertaId, ActionCallback callback) {
        executor.execute(() -> {
            try {
                URL url = new URL(SUPABASE_URL + TABLE + "?id=eq." + ofertaId);
                HttpURLConnection conn = buildConnection(url, "DELETE");
                conn.setRequestProperty("Prefer", "return=minimal");

                int code = conn.getResponseCode();
                if (code == 200 || code == 204) {
                    callback.onSuccess();
                } else {
                    callback.onError("Error " + code + ": " + readError(conn));
                }
            } catch (Exception e) {
                Log.e(TAG, "eliminarOferta", e);
                callback.onError(e.getMessage());
            }
        });
    }

    // ─── PARSEO ─────────────────────────────────────────────────────────────────

    private List<OfertaConUsuario> parseOfertaConUsuarioList(String json) throws Exception {
        List<OfertaConUsuario> list = new ArrayList<>();
        JSONArray arr = new JSONArray(json);
        for (int i = 0; i < arr.length(); i++) {
            JSONObject o = arr.getJSONObject(i);

            OfertaConUsuario item = new OfertaConUsuario();
            item.id = o.optLong("id", 0);
            item.productoId = o.optLong("producto_id", 0);
            item.usuarioId = o.optLong("usuario_id", 0);
            item.monto = o.optDouble("monto", 0);
            item.fecha = o.optString("fecha", "");

            JSONObject usuario = o.optJSONObject("usuarios");
            if (usuario != null) {
                item.nombreOfertante = usuario.optString("nombre", "Usuario");
                item.telefonoOfertante = usuario.optString("telefono", "");
            } else {
                item.nombreOfertante = "Usuario";
                item.telefonoOfertante = "";
            }

            JSONObject producto = o.optJSONObject("productos");
            if (producto != null) {
                item.tituloProducto = producto.optString("titulo", "producto");
            } else {
                item.tituloProducto = "producto";
            }

            list.add(item);
        }
        return list;
    }

    // ─── HELPERS de conexión y lectura ──────────────────────────────────────────

    private HttpURLConnection buildConnection(URL url, String method) throws Exception {
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod(method);
        conn.setRequestProperty("apikey", SUPABASE_ANON_KEY);
        conn.setRequestProperty("Authorization", "Bearer " + SUPABASE_ANON_KEY);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(10000);
        return conn;
    }

    private String readResponse(HttpURLConnection conn) throws Exception {
        return readStream(conn.getInputStream());
    }

    private String readError(HttpURLConnection conn) {
        try {
            InputStream es = conn.getErrorStream();
            if (es == null) return "sin detalle";
            return readStream(es);
        } catch (Exception e) {
            return "error al leer respuesta";
        }
    }

    private String readStream(InputStream is) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        reader.close();
        return sb.toString();
    }
}