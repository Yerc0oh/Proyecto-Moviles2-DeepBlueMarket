package com.moviles2.proyectomov2_deepbluemarket.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.moviles2.proyectomov2_deepbluemarket.R;
import com.moviles2.proyectomov2_deepbluemarket.ui.products.MyProductsActivity;

public class NotificationHelper {

    private static final String CHANNEL_ID = "ofertas_channel";
    private static final String CHANNEL_NAME = "Ofertas";
    private static final String CHANNEL_DESC = "Notificaciones de nuevas ofertas por tus productos";

    private final Context context;

    public NotificationHelper(Context context) {
        this.context = context;
        crearCanal();
    }

    private void crearCanal() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription(CHANNEL_DESC);

            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    /**
     * Muestra una notificación local indicando que un usuario hizo una
     * oferta por un producto. Al pulsarla, abre MyProductsActivity.
     *
     * @param notificationId id único para esta notificación (ej. el id de la oferta)
     * @param nombreOfertante nombre del usuario que hizo la oferta
     * @param tituloProducto  título del producto ofertado
     * @param productoId      id del producto (extra para abrir su detalle desde MyProductsActivity)
     */

    public void mostrarNotificacionOferta(int notificationId, String nombreOfertante,
                                          String tituloProducto, long productoId) {

        Intent intent = new Intent(context, MyProductsActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("producto_id", productoId);

        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, notificationId, intent, flags);

        String titulo = nombreOfertante + " ha hecho una oferta por tu " + tituloProducto;

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Nueva oferta")
                .setContentText(titulo)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(titulo))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        NotificationManagerCompat managerCompat = NotificationManagerCompat.from(context);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                managerCompat.areNotificationsEnabled()) {
            try {
                managerCompat.notify(notificationId, builder.build());
            } catch (SecurityException e) {
                // Permiso POST_NOTIFICATIONS no concedido (Android 13+)
            }
        }
    }
}