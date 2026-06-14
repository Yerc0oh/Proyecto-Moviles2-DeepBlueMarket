package com.moviles2.proyectomov2_deepbluemarket.ui.products;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.moviles2.proyectomov2_deepbluemarket.R;
import com.moviles2.proyectomov2_deepbluemarket.network.ProductService;

public class ProductDetailActivity extends AppCompatActivity {

    private final ProductService productService = new ProductService();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        ImageView ivImagen = findViewById(R.id.ivProductDetail);
        TextView tvTitulo = findViewById(R.id.tvDetailTitulo);
        TextView tvDescripcion = findViewById(R.id.tvDetailDescripcion);
        TextView tvCategoria = findViewById(R.id.tvDetailCategoria);
        TextView tvPrecio = findViewById(R.id.tvDetailPrecio);
        TextView tvVendedor = findViewById(R.id.tvDetailVendedor);
        Button btnWhatsApp = findViewById(R.id.btnWhatsApp);
        ProgressBar progressBar = findViewById(R.id.progressBarDetail);

        // Recibir datos del Intent
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
}