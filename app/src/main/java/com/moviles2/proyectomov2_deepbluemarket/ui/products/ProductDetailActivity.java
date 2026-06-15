package com.moviles2.proyectomov2_deepbluemarket.ui.products;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.textfield.TextInputEditText;
import com.moviles2.proyectomov2_deepbluemarket.R;
import com.moviles2.proyectomov2_deepbluemarket.network.OfertaService;
import com.moviles2.proyectomov2_deepbluemarket.network.ProductService;
import com.moviles2.proyectomov2_deepbluemarket.utils.SessionManager;

public class ProductDetailActivity extends AppCompatActivity {

    private final ProductService productService = new ProductService();
    private final OfertaService ofertaService = new OfertaService();
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        sessionManager = new SessionManager(this);

        ImageView ivImagen = findViewById(R.id.ivProductDetail);
        TextView tvTitulo = findViewById(R.id.tvDetailTitulo);
        TextView tvDescripcion = findViewById(R.id.tvDetailDescripcion);
        TextView tvCategoria = findViewById(R.id.tvDetailCategoria);
        TextView tvPrecio = findViewById(R.id.tvDetailPrecio);
        TextView tvVendedor = findViewById(R.id.tvDetailVendedor);
        Button btnWhatsApp = findViewById(R.id.btnWhatsApp);
        Button btnHacerOferta = findViewById(R.id.btnHacerOferta);
        ProgressBar progressBar = findViewById(R.id.progressBarDetail);

        // Recibir datos del Intent
        long productoId = getIntent().getLongExtra("id", -1);
        String titulo = getIntent().getStringExtra("titulo");
        String descripcion = getIntent().getStringExtra("descripcion");
        String categoria = getIntent().getStringExtra("categoria");
        double precio = getIntent().getDoubleExtra("precio", 0.0);
        String imagenUrl = getIntent().getStringExtra("imagen_url");
        long usuarioId = getIntent().getLongExtra("usuario_id", -1);

        tvTitulo.setText(titulo != null ? titulo : "");
        tvDescripcion.setText(descripcion != null ? descripcion : "");
        tvCategoria.setText(categoria != null ? categoria.toUpperCase() : "");
        tvPrecio.setText(String.format("Bs. %.2f", precio));
        tvVendedor.setText("Cargando...");
        btnWhatsApp.setEnabled(false);

        if (imagenUrl != null && !imagenUrl.isEmpty()) {
            Glide.with(this).load(imagenUrl).centerCrop()
                    .placeholder(R.drawable.ic_launcher_background).into(ivImagen);
        }

        // Ocultar "Hacer oferta" si el producto es del usuario actual
        long miUsuarioId = sessionManager.getUsuarioId();
        if (productoId == -1 || usuarioId == -1 || usuarioId == miUsuarioId) {
            btnHacerOferta.setVisibility(View.GONE);
        } else {
            btnHacerOferta.setVisibility(View.VISIBLE);
            btnHacerOferta.setOnClickListener(v ->
                    mostrarDialogHacerOferta(productoId, miUsuarioId, titulo));
        }

        // Cargar nombre y teléfono del vendedor
        if (usuarioId != -1) {
            progressBar.setVisibility(View.VISIBLE);
            productService.getUsuarioById(usuarioId, new ProductService.UsuarioCallback() {
                @Override
                public void onSuccess(String nombre, String telefono) {
                    runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        tvVendedor.setText(nombre);

                        if (telefono != null && !telefono.isEmpty()) {
                            btnWhatsApp.setEnabled(true);
                            btnWhatsApp.setOnClickListener(v -> {
                                String numeroLimpio = telefono.replaceAll("[^0-9]", "");
                                // Agrega prefijo Bolivia si no lo tiene
                                if (!numeroLimpio.startsWith("591")) {
                                    numeroLimpio = "591" + numeroLimpio;
                                }
                                String mensaje = "Hola " + nombre + ", estoy interesado en tu producto: *"
                                        + titulo + "* por Bs. "
                                        + String.format("%.2f", precio)
                                        + ". ¿Sigue disponible?";
                                String url = "https://wa.me/" + numeroLimpio
                                        + "?text=" + Uri.encode(mensaje);
                                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                            });
                        } else {
                            btnWhatsApp.setText("Vendedor sin WhatsApp");
                            btnWhatsApp.setEnabled(false);
                        }
                    });
                }

                @Override
                public void onError(String error) {
                    runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        tvVendedor.setText("Vendedor desconocido");
                        Toast.makeText(ProductDetailActivity.this,
                                "No se pudo cargar info del vendedor", Toast.LENGTH_SHORT).show();
                    });
                }
            });
        }
    }

    /**
     * Muestra un dialog para que el usuario ingrese el monto de su oferta
     * y la envía a Supabase.
     */
    private void mostrarDialogHacerOferta(long productoId, long miUsuarioId, String tituloProducto) {
        if (miUsuarioId == -1) {
            Toast.makeText(this, "No se pudo identificar tu usuario", Toast.LENGTH_SHORT).show();
            return;
        }

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_hacer_oferta, null);

        TextView tvProductoTitulo = dialogView.findViewById(R.id.tvOfertaProductoTitulo);
        TextInputEditText etMonto = dialogView.findViewById(R.id.etMontoOferta);

        tvProductoTitulo.setText("Producto: " + (tituloProducto != null ? tituloProducto : ""));

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setPositiveButton("Enviar oferta", null)
                .setNegativeButton("Cancelar", null)
                .create();

        dialog.show();

        // Override del listener para validar antes de cerrar el dialog
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String montoStr = etMonto.getText() != null ? etMonto.getText().toString().trim() : "";

            if (montoStr.isEmpty()) {
                etMonto.setError("Ingresa un monto");
                return;
            }

            double monto;
            try {
                monto = Double.parseDouble(montoStr);
            } catch (NumberFormatException e) {
                etMonto.setError("Monto inválido");
                return;
            }

            if (monto <= 0) {
                etMonto.setError("El monto debe ser mayor a 0");
                return;
            }

            ofertaService.crearOferta(productoId, miUsuarioId, monto, new OfertaService.ActionCallback() {
                @Override
                public void onSuccess() {
                    runOnUiThread(() -> {
                        Toast.makeText(ProductDetailActivity.this,
                                "Oferta enviada correctamente", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    });
                }

                @Override
                public void onError(String error) {
                    runOnUiThread(() ->
                            Toast.makeText(ProductDetailActivity.this,
                                    "Error al enviar oferta: " + error, Toast.LENGTH_LONG).show());
                }
            });
        });
    }
}