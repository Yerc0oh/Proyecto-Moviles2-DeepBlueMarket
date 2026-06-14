package com.moviles2.proyectomov2_deepbluemarket.storage;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.moviles2.proyectomov2_deepbluemarket.utils.Constants;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Gestiona la subida de imágenes a Supabase Storage mediante OkHttp,
 * usando la API REST de Storage de Supabase.
 *
 * Estructura de buckets esperada en Supabase:
 * - avatars               -> fotos de perfil
 * - productos             -> fotos de productos
 * - verificaciones        -> fotos de documentos de identidad (privado recomendado)
 */
public class StorageManager {

    private static final String TAG = "StorageManager";

    private static final String BUCKET_AVATARS = "avatars";
    private static final String BUCKET_PRODUCTOS = "productos";
    private static final String BUCKET_VERIFICACIONES = "verificaciones";

    private final Context context;
    private final OkHttpClient client;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public StorageManager(Context context) {
        this.context = context;
        this.client = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build();
    }

    public interface UploadCallback {
        void onSuccess(String publicUrl);
        void onError(String errorMessage);
    }

    /**
     * Sube una imagen de perfil al bucket "avatars".
     * El path generado es: avatars/{auth0Id}/profile.jpg
     */
    public void uploadProfileImage(Uri imageUri, String auth0Id, UploadCallback callback) {
        String path = "profile_" + auth0Id.replace("|","_") + ".jpg";

        subirArchivo(BUCKET_AVATARS, path, imageUri, callback);
    }

    /**
     * Sube una imagen de producto al bucket "productos".
     * El path generado es: productos/{productoId}_{timestamp}.jpg
     */
    public void uploadProductImage(Uri imageUri, String productoId, UploadCallback callback) {
        String fileName = productoId + "_" + System.currentTimeMillis() + ".jpg";
        subirArchivo(BUCKET_PRODUCTOS, fileName, imageUri, callback);
    }

    /**
     * Sube una imagen de documento de verificación al bucket "verificaciones".
     * El path generado es: verificaciones/{auth0Id}_doc.jpg
     */
    public void uploadProfileVerificationImage(Uri imageUri, String auth0Id, UploadCallback callback) {
        String fileName = auth0Id.replace("|","_") + "_doc.jpg";
        subirArchivo(BUCKET_VERIFICACIONES, fileName, imageUri, callback);
    }

    /**
     * Construye la URL pública de un archivo dado su path dentro de un bucket.
     * Lista para usar directamente con Glide.
     *
     * @param bucket Nombre del bucket (ej. "avatars")
     * @param path   Ruta del archivo dentro del bucket (ej. "profile_xyz.jpg")
     */
    public String getPublicUrl(String bucket, String path) {
        return Constants.SUPABASE_URL + "/storage/v1/object/public/" + bucket + "/" + path;
    }

    /**
     * Lee el contenido de un Uri local y lo sube a Supabase Storage
     * mediante una petición PUT (upsert) a la API REST de Storage.
     */
    private void subirArchivo(String bucket, String fileName, Uri imageUri, UploadCallback callback) {
        try {
            byte[] datos = leerBytesDesdeUri(imageUri);

            if (datos == null) {
                callback.onError("No se pudo leer el archivo seleccionado");
                return;
            }

            String url = Constants.SUPABASE_URL + "/storage/v1/object/" + bucket + "/" + fileName;

            RequestBody body = RequestBody.create(datos, MediaType.parse("image/jpeg"));

            Request request = new Request.Builder()
                    .url(url)
                    .header("apikey", Constants.SUPABASE_PUBL_KEY)
                    .header("Authorization", "Bearer " + Constants.SUPABASE_PUBL_KEY)
                    .header("x-upsert", "true")
                    .put(body)
                    .build();

            client.newCall(request).enqueue(new okhttp3.Callback() {
                @Override
                public void onFailure(okhttp3.Call call, IOException e) {
                    Log.e(TAG, "Error de red al subir imagen", e);
                    mainHandler.post(() -> callback.onError("Error de conexión: " + e.getMessage()));
                }

                @Override
                public void onResponse(okhttp3.Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        String publicUrl = getPublicUrl(bucket, fileName);
                        Log.d(TAG, "Imagen subida correctamente: " + publicUrl);
                        mainHandler.post(() -> callback.onSuccess(publicUrl));
                    } else {
                        String errorBody = response.body() != null ? response.body().string() : "";
                        Log.e(TAG, "Error al subir imagen: " + response.code() + " - " + errorBody);
                        mainHandler.post(() -> callback.onError("Error al subir imagen: " + response.code()));
                    }
                    response.close();
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "Error inesperado al subir imagen", e);
            callback.onError("Error inesperado: " + e.getMessage());
        }
    }

    /**
     * Lee todos los bytes de un Uri usando el ContentResolver del contexto.
     */
    private byte[] leerBytesDesdeUri(Uri uri) throws IOException {
        InputStream inputStream = context.getContentResolver().openInputStream(uri);

        if (inputStream == null) {
            return null;
        }

        java.io.ByteArrayOutputStream buffer = new java.io.ByteArrayOutputStream();
        byte[] data = new byte[8192];
        int bytesRead;

        while ((bytesRead = inputStream.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, bytesRead);
        }

        inputStream.close();
        return buffer.toByteArray();
    }
}