package com.moviles2.proyectomov2_deepbluemarket.ui.products;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.moviles2.proyectomov2_deepbluemarket.R;

public class ProductDetailActivity extends AppCompatActivity {

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

        // Recibir datos del Intent
        String titulo = getIntent().getStringExtra("titulo");
        String descripcion = getIntent().getStringExtra("descripcion");
        String categoria = getIntent().getStringExtra("categoria");
        double precio = getIntent().getDoubleExtra("precio", 0.0);
        String imagenUrl = getIntent().getStringExtra("imagen_url");
        String vendedor = getIntent().getStringExtra("vendedor");

        tvTitulo.setText(titulo != null ? titulo : "");
        tvDescripcion.setText(descripcion != null ? descripcion : "");
        tvCategoria.setText(categoria != null ? categoria.toUpperCase() : "");
        tvPrecio.setText(String.format("Bs. %.2f", precio));
        tvVendedor.setText(vendedor != null ? vendedor : "Vendedor");

        if (imagenUrl != null && !imagenUrl.isEmpty()) {
            Glide.with(this).load(imagenUrl).centerCrop()
                    .placeholder(R.drawable.ic_launcher_background).into(ivImagen);
        }

        btnWhatsApp.setOnClickListener(v -> {
            String mensaje = "Hola, estoy interesado en tu producto: *" + titulo
                    + "* por Bs. " + String.format("%.2f", precio)
                    + ". ¿Sigue disponible?";
            String url = "https://wa.me/?text=" + Uri.encode(mensaje);
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
        });
    }
}