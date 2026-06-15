package com.moviles2.proyectomov2_deepbluemarket.utils;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.moviles2.proyectomov2_deepbluemarket.models.Producto;
import com.moviles2.proyectomov2_deepbluemarket.network.OfertaService;
import com.moviles2.proyectomov2_deepbluemarket.network.ProductService;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Hace polling periódico (cada ~20s) buscando ofertas nuevas para los
 * productos del usuario actual, y dispara una notificación local por
 * cada oferta nueva detectada.
 *
 * Debe iniciarse en onResume() y detenerse en onPause() de la Activity
 * que lo use (normalmente MyProductsActivity).
 */
public class OfertaPollingManager {

    private static final String TAG = "OfertaPollingManager";
    private static final long INTERVALO_MS = 20_000L; // 20 segundos

    private final SessionManager sessionManager;
    private final ProductService productService;
    private final OfertaService ofertaService;
    private final NotificationHelper notificationHelper;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable pollingRunnable;
    private boolean running = false;

    // Marca de tiempo (ISO 8601 UTC) desde la cual buscar ofertas nuevas.
    private String ultimaFechaRevisada;

    public OfertaPollingManager(Context context) {
        this.sessionManager = new SessionManager(context);
        this.productService = new ProductService();
        this.ofertaService = new OfertaService();
        this.notificationHelper = new NotificationHelper(context);
    }

    /**
     * Inicia el polling. Si ya está corriendo, no hace nada.
     */
    public void start() {
        if (running) return;
        running = true;

        // Inicializamos la marca de tiempo en "ahora" para no notificar
        // ofertas que ya existían antes de abrir esta pantalla.
        if (ultimaFechaRevisada == null) {
            ultimaFechaRevisada = isoAhora();
        }

        pollingRunnable = new Runnable() {
            @Override
            public void run() {
                revisarOfertasNuevas();
                if (running) {
                    handler.postDelayed(pollingRunnable, INTERVALO_MS);
                }
            }
        };

        handler.postDelayed(pollingRunnable, INTERVALO_MS);
    }

    /**
     * Detiene el polling.
     */
    public void stop() {
        running = false;
        if (pollingRunnable != null) {
            handler.removeCallbacks(pollingRunnable);
        }
    }

    private void revisarOfertasNuevas() {
        String auth0Id = sessionManager.getAuth0Id();
        if (auth0Id == null) return;

        productService.getProductsByAuth0Id(auth0Id, new ProductService.ProductCallback() {
            @Override
            public void onSuccess(List<Producto> productos) {
                if (productos == null || productos.isEmpty()) return;

                List<Long> ids = new ArrayList<>();
                for (Producto p : productos) {
                    ids.add(p.getId());
                }

                String fechaConsulta = ultimaFechaRevisada;

                ofertaService.getOfertasNuevas(ids, fechaConsulta, new OfertaService.OfertaListCallback() {
                    @Override
                    public void onSuccess(List<OfertaService.OfertaConUsuario> ofertas) {
                        if (ofertas == null || ofertas.isEmpty()) return;

                        for (OfertaService.OfertaConUsuario oferta : ofertas) {
                            String nombreOfertante = oferta.nombreOfertante != null
                                    ? oferta.nombreOfertante : "Alguien";

                            String tituloProducto = oferta.tituloProducto != null
                                    ? oferta.tituloProducto : "producto";

                            notificationHelper.mostrarNotificacionOferta(
                                    (int) oferta.id,
                                    nombreOfertante,
                                    tituloProducto,
                                    oferta.productoId
                            );
                        }

                        // Actualizamos la marca de tiempo para no repetir notificaciones
                        ultimaFechaRevisada = isoAhora();
                    }

                    @Override
                    public void onError(String errorMessage) {
                        Log.e(TAG, "Error al revisar ofertas nuevas: " + errorMessage);
                    }
                });
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Error al obtener productos para polling: " + error);
            }
        });
    }

    /**
     * Devuelve la fecha/hora actual en formato ISO 8601 UTC,
     * compatible con el formato de "fecha" en Supabase.
     */
    private String isoAhora() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        return sdf.format(new java.util.Date());
    }
}