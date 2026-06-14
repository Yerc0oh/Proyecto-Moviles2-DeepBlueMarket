package com.moviles2.proyectomov2_deepbluemarket.network;

import android.util.Log;
import com.moviles2.proyectomov2_deepbluemarket.models.Producto;
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
import com.moviles2.proyectomov2_deepbluemarket.utils.Constants;
import com.google.gson.Gson;
import com.moviles2.proyectomov2_deepbluemarket.models.dto.ProductoCreateDTO;
import com.moviles2.proyectomov2_deepbluemarket.models.dto.ProductoUpdateDTO;
public class ProductService {

    private static final String TAG = "ProductService";

    private static final String SUPABASE_URL = Constants.SUPABASE_URL;
    private static final String SUPABASE_ANON_KEY = Constants.SUPABASE_ANON_KEY;
    private static final String TABLE = "/rest/v1/productos";
    private static final String USERS_TABLE = "/rest/v1/usuarios";

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    // ─── INTERFACES ─────────────────────────────────────────────────────────────

    public interface ProductCallback {
        void onSuccess(List<Producto> productos);
        void onError(String error);
    }

    public interface SingleProductCallback {
        void onSuccess(Producto producto);
        void onError(String error);
    }

    public interface ActionCallback {
        void onSuccess();
        void onError(String error);
    }

    // ─── GET ALL ────────────────────────────────────────────────────────────────

    public void getAllProducts(ProductCallback callback) {
        executor.execute(() -> {
            try {
                URL url = new URL(SUPABASE_URL + TABLE + "?select=*&estado=eq.activo&order=fecha_publicacion.desc");
                HttpURLConnection conn = buildConnection(url, "GET");
                int code = conn.getResponseCode();
                if (code == 200) {
                    callback.onSuccess(parseList(readResponse(conn)));
                } else {
                    callback.onError("Error " + code + ": " + readError(conn));
                }
            } catch (Exception e) {
                Log.e(TAG, "getAllProducts", e);
                callback.onError(e.getMessage());
            }
        });
    }

    // ─── GET BY AUTH0 ID (JOIN con usuarios) ────────────────────────────────────
    // productos se une a usuarios por usuario_id,
    // y filtramos donde usuarios.auth0_id = el auth0Id del usuario logueado

    public void getProductsByAuth0Id(String auth0Id, ProductCallback callback) {
        executor.execute(() -> {
            try {
                String encoded = java.net.URLEncoder.encode(auth0Id, "UTF-8");
                URL url = new URL(SUPABASE_URL + TABLE
                        + "?select=*,usuarios!inner(auth0_id)"
                        + "&usuarios.auth0_id=eq." + encoded
                        + "&estado=eq.activo"
                        + "&order=fecha_publicacion.desc");
                HttpURLConnection conn = buildConnection(url, "GET");
                int code = conn.getResponseCode();
                if (code == 200) {
                    callback.onSuccess(parseList(readResponse(conn)));
                } else {
                    callback.onError("Error " + code + ": " + readError(conn));
                }
            } catch (Exception e) {
                Log.e(TAG, "getProductsByAuth0Id", e);
                callback.onError(e.getMessage());
            }
        });
    }

    // ─── CREATE (primero busca usuario_id numérico, luego inserta) ──────────────

    public void createProduct(String auth0Id, String titulo, String descripcion,
                              String categoria, double precio, String imagenUrl,
                              ActionCallback callback) {
        executor.execute(() -> {
            try {
                // Paso 1: obtener usuario_id numérico desde la tabla usuarios
                long usuarioId = fetchUsuarioId(auth0Id);
                if (usuarioId == -1) {
                    callback.onError("No se encontró el usuario en la base de datos");
                    return;
                }

                // Paso 2: insertar el producto con el usuario_id numérico
                URL url = new URL(SUPABASE_URL + TABLE);
                HttpURLConnection conn = buildConnection(url, "POST");
                conn.setDoOutput(true);
                conn.setRequestProperty("Prefer", "return=minimal");

                ProductoCreateDTO dto = new ProductoCreateDTO(
                        usuarioId, titulo, descripcion, categoria,
                        precio, imagenUrl != null ? imagenUrl : "", "activo"
                );
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
                Log.e(TAG, "createProduct", e);
                callback.onError(e.getMessage());
            }
        });
    }

    // ─── UPDATE ─────────────────────────────────────────────────────────────────

    public void updateProduct(long id, String titulo, String descripcion,
                              String categoria, double precio, String imagenUrl,
                              ActionCallback callback) {
        executor.execute(() -> {
            try {
                URL url = new URL(SUPABASE_URL + TABLE + "?id=eq." + id);
                HttpURLConnection conn = buildConnection(url, "PATCH");
                conn.setDoOutput(true);
                conn.setRequestProperty("Prefer", "return=minimal");

                ProductoUpdateDTO dto = new ProductoUpdateDTO(
                        titulo, descripcion, categoria, precio,
                        imagenUrl != null ? imagenUrl : "", "activo"
                );
                String bodyJson = new Gson().toJson(dto);

                try (OutputStream os = conn.getOutputStream()) {
                    os.write(bodyJson.getBytes(StandardCharsets.UTF_8));
                }

                int code = conn.getResponseCode();
                if (code == 200 || code == 204) {
                    callback.onSuccess();
                } else {
                    callback.onError("Error " + code + ": " + readError(conn));
                }
            } catch (Exception e) {
                Log.e(TAG, "updateProduct", e);
                callback.onError(e.getMessage());
            }
        });
    }

    // ─── DELETE ─────────────────────────────────────────────────────────────────

    public void deleteProduct(long id, ActionCallback callback) {
        executor.execute(() -> {
            try {
                URL url = new URL(SUPABASE_URL + TABLE + "?id=eq." + id);
                HttpURLConnection conn = buildConnection(url, "DELETE");
                conn.setRequestProperty("Prefer", "return=minimal");

                int code = conn.getResponseCode();
                if (code == 200 || code == 204) {
                    callback.onSuccess();
                } else {
                    callback.onError("Error " + code + ": " + readError(conn));
                }
            } catch (Exception e) {
                Log.e(TAG, "deleteProduct", e);
                callback.onError(e.getMessage());
            }
        });
    }
    // ─── DEACTIVATE (eliminación lógica) ────────────────────────────────────────

    public void deactivateProduct(long id, ActionCallback callback) {
        executor.execute(() -> {
            try {
                URL url = new URL(SUPABASE_URL + TABLE + "?id=eq." + id);
                HttpURLConnection conn = buildConnection(url, "PATCH");
                conn.setDoOutput(true);
                conn.setRequestProperty("Prefer", "return=minimal");

                ProductoUpdateDTO dto = new ProductoUpdateDTO();
                dto.setEstado("inactivo");
                String bodyJson = new Gson().toJson(dto);

                try (OutputStream os = conn.getOutputStream()) {
                    os.write(bodyJson.getBytes(StandardCharsets.UTF_8));
                }

                int code = conn.getResponseCode();
                if (code == 200 || code == 204) {
                    callback.onSuccess();
                } else {
                    callback.onError("Error " + code + ": " + readError(conn));
                }
            } catch (Exception e) {
                Log.e(TAG, "deactivateProduct", e);
                callback.onError(e.getMessage());
            }
        });
    }

    // ─── HELPER: buscar usuario_id numérico por auth0_id ────────────────────────

    private long fetchUsuarioId(String auth0Id) {
        try {
            String encoded = java.net.URLEncoder.encode(auth0Id, "UTF-8");
            URL url = new URL(SUPABASE_URL + USERS_TABLE
                    + "?auth0_id=eq." + encoded
                    + "&select=id"
                    + "&limit=1");
            HttpURLConnection conn = buildConnection(url, "GET");
            int code = conn.getResponseCode();
            if (code == 200) {
                String body = readResponse(conn);
                JSONArray arr = new JSONArray(body);
                if (arr.length() > 0) {
                    return arr.getJSONObject(0).getLong("id");
                }
            } else {
                Log.e(TAG, "fetchUsuarioId error: " + code + " " + readError(conn));
            }
        } catch (Exception e) {
            Log.e(TAG, "fetchUsuarioId", e);
        }
        return -1; // no encontrado
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

    // Lee el cuerpo de una respuesta exitosa (2xx)
    private String readResponse(HttpURLConnection conn) throws Exception {
        return readStream(conn.getInputStream());
    }

    // Lee el cuerpo de una respuesta de error (4xx/5xx)
    private String readError(HttpURLConnection conn) {
        try {
            InputStream es = conn.getErrorStream();
            if (es == null) return "sin detalle";
            return readStream(es);
        } catch (Exception e) {
            return "error al leer respuesta";
        }
    }

    // Lee un InputStream línea por línea (compatible con minSdk 29+)
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

    // ─── PARSEO ─────────────────────────────────────────────────────────────────

    private List<Producto> parseList(String json) throws Exception {
        List<Producto> list = new ArrayList<>();
        JSONArray arr = new JSONArray(json);
        for (int i = 0; i < arr.length(); i++) {
            list.add(parseOne(arr.getJSONObject(i)));
        }
        return list;
    }

    private Producto parseOne(JSONObject o) throws Exception {
        Producto p = new Producto();
        p.setId(o.optLong("id", 0));
        p.setUsuarioId(o.optLong("usuario_id", 0));
        p.setTitulo(o.optString("titulo", ""));
        p.setDescripcion(o.optString("descripcion", ""));
        p.setCategoria(o.optString("categoria", ""));
        p.setPrecio(o.optDouble("precio", 0));
        p.setImagenUrl(o.optString("imagen_url", ""));
        p.setEstado(o.optString("estado", ""));
        return p;
    }
    // ─── GET USUARIO BY ID ───────────────────────────────────────────────────────

    public interface UsuarioCallback {
        void onSuccess(String nombre, String telefono);
        void onError(String error);
    }

    public void getUsuarioById(long usuarioId, UsuarioCallback callback) {
        executor.execute(() -> {
            try {
                URL url = new URL(SUPABASE_URL + USERS_TABLE
                        + "?id=eq." + usuarioId
                        + "&select=nombre,telefono"
                        + "&limit=1");
                HttpURLConnection conn = buildConnection(url, "GET");
                int code = conn.getResponseCode();
                if (code == 200) {
                    String body = readResponse(conn);
                    JSONArray arr = new JSONArray(body);
                    if (arr.length() > 0) {
                        JSONObject o = arr.getJSONObject(0);
                        callback.onSuccess(
                                o.optString("nombre", "Vendedor"),
                                o.optString("telefono", "")
                        );
                    } else {
                        callback.onError("Usuario no encontrado");
                    }
                } else {
                    callback.onError("Error " + code);
                }
            } catch (Exception e) {
                Log.e(TAG, "getUsuarioById", e);
                callback.onError(e.getMessage());
            }
        });
    }
}