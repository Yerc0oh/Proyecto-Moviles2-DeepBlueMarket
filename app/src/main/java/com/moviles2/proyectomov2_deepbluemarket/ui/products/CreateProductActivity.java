package com.moviles2.proyectomov2_deepbluemarket.ui.products;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.google.android.material.textfield.TextInputEditText;
import com.moviles2.proyectomov2_deepbluemarket.R;
import com.moviles2.proyectomov2_deepbluemarket.utils.SessionManager;
import com.moviles2.proyectomov2_deepbluemarket.network.ProductService;
import com.moviles2.proyectomov2_deepbluemarket.storage.StorageManager;

public class CreateProductActivity extends AppCompatActivity {

    private TextInputEditText etTitulo, etDescripcion, etPrecio;
    private AutoCompleteTextView actvCategoria;
    private ImageView ivPreview;
    private ProgressBar progressBar;
    private Uri imageUri;
    private SessionManager sessionManager;
    private final ProductService productService = new ProductService();

    private final String[] CATEGORIAS = {
            "Electrónica", "Ropa", "Hogar", "Deportes",
            "Libros", "Juguetes", "Vehículos", "Otros"
    };

    private final ActivityResultLauncher<String> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    imageUri = uri;
                    Glide.with(this).load(uri).centerCrop().into(ivPreview);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_product);

        sessionManager = new SessionManager(this);

        etTitulo = findViewById(R.id.etTitulo);
        etDescripcion = findViewById(R.id.etDescripcion);
        etPrecio = findViewById(R.id.etPrecio);
        actvCategoria = findViewById(R.id.actvCategoria);
        ivPreview = findViewById(R.id.ivProductPreview);
        progressBar = findViewById(R.id.progressBar);
        Button btnImagen = findViewById(R.id.btnSeleccionarImagen);
        Button btnPublicar = findViewById(R.id.btnPublicar);

        ArrayAdapter<String> catAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, CATEGORIAS);
        actvCategoria.setAdapter(catAdapter);

        btnImagen.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));
        btnPublicar.setOnClickListener(v -> publicarProducto());
    }

    private void publicarProducto() {
        String titulo = etTitulo.getText() != null ? etTitulo.getText().toString().trim() : "";
        String descripcion = etDescripcion.getText() != null ? etDescripcion.getText().toString().trim() : "";
        String precioStr = etPrecio.getText() != null ? etPrecio.getText().toString().trim() : "";
        String categoria = actvCategoria.getText().toString().trim();

        if (titulo.isEmpty() || descripcion.isEmpty() || precioStr.isEmpty() || categoria.isEmpty()) {
            Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        double precio;
        try {
            precio = Double.parseDouble(precioStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Precio inválido", Toast.LENGTH_SHORT).show();
            return;
        }

        String auth0Id = sessionManager.getAuth0Id();
        if (auth0Id == null || auth0Id.isEmpty()) {
            Toast.makeText(this, "Sesión no válida", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        if (imageUri != null) {

            StorageManager storageManager = new StorageManager(this);

            storageManager.uploadProductImage(
                    imageUri,
                    auth0Id,
                    new StorageManager.UploadCallback() {

                        @Override
                        public void onSuccess(String imageUrl) {
                            crearProducto(
                                    auth0Id,
                                    titulo,
                                    descripcion,
                                    categoria,
                                    precio,
                                    imageUrl
                            );
                        }

                        @Override
                        public void onError(String error) {
                            runOnUiThread(() -> {
                                progressBar.setVisibility(View.GONE);
                                Toast.makeText(
                                        CreateProductActivity.this,
                                        "Error al subir imagen: " + error,
                                        Toast.LENGTH_LONG
                                ).show();
                            });
                        }
                    }
            );

        } else {
            crearProducto(auth0Id, titulo, descripcion, categoria, precio, "");
        }
    }

    private void crearProducto(String auth0Id, String titulo, String descripcion,
                               String categoria, double precio, String imagenUrl) {
        productService.createProduct(auth0Id, titulo, descripcion, categoria, precio, imagenUrl,
                new ProductService.ActionCallback() {
                    @Override
                    public void onSuccess() {
                        runOnUiThread(() -> {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(CreateProductActivity.this,
                                    "¡Producto publicado!", Toast.LENGTH_SHORT).show();
                            finish();
                        });
                    }

                    @Override
                    public void onError(String error) {
                        runOnUiThread(() -> {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(CreateProductActivity.this,
                                    "Error: " + error, Toast.LENGTH_LONG).show();
                        });
                    }
                });
    }
}