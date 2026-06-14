package com.moviles2.proyectomov2_deepbluemarket.ui.products;

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
import com.google.android.material.textfield.TextInputLayout;
import com.moviles2.proyectomov2_deepbluemarket.R;
import com.moviles2.proyectomov2_deepbluemarket.network.ProductService;
import com.moviles2.proyectomov2_deepbluemarket.storage.StorageManager;
import com.moviles2.proyectomov2_deepbluemarket.utils.SessionManager;

public class EditProductActivity extends AppCompatActivity {

    private TextInputEditText etTitulo, etDescripcion, etPrecio, etCategoriaPersonalizada;
    private AutoCompleteTextView actvCategoria;
    private TextInputLayout tilCategoriaPersonalizada;
    private ImageView ivPreview;
    private ProgressBar progressBar;
    private Uri imageUri;
    private String imagenUrlActual;
    private long productoId;
    private final ProductService productService = new ProductService();
    private SessionManager sessionManager;

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
        setContentView(R.layout.activity_edit_product);

        sessionManager = new SessionManager(this);

        etTitulo = findViewById(R.id.etTitulo);
        etDescripcion = findViewById(R.id.etDescripcion);
        etPrecio = findViewById(R.id.etPrecio);
        actvCategoria = findViewById(R.id.actvCategoria);
        etCategoriaPersonalizada = findViewById(R.id.etCategoriaPersonalizada);
        tilCategoriaPersonalizada = findViewById(R.id.tilCategoriaPersonalizada);
        ivPreview = findViewById(R.id.ivProductPreview);
        progressBar = findViewById(R.id.progressBar);
        Button btnImagen = findViewById(R.id.btnSeleccionarImagen);
        Button btnGuardar = findViewById(R.id.btnGuardar);

        // Dropdown categorías
        ArrayAdapter<String> catAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, CATEGORIAS);
        actvCategoria.setAdapter(catAdapter);

        actvCategoria.setOnItemClickListener((parent, view, position, id) -> {
            if ("Otros".equals(CATEGORIAS[position])) {
                tilCategoriaPersonalizada.setVisibility(View.VISIBLE);
                etCategoriaPersonalizada.requestFocus();
            } else {
                tilCategoriaPersonalizada.setVisibility(View.GONE);
                etCategoriaPersonalizada.setText("");
            }
        });

        // Recibir datos del producto a editar
        productoId = getIntent().getLongExtra("id", -1);
        String titulo = getIntent().getStringExtra("titulo");
        String descripcion = getIntent().getStringExtra("descripcion");
        String categoria = getIntent().getStringExtra("categoria");
        double precio = getIntent().getDoubleExtra("precio", 0);
        imagenUrlActual = getIntent().getStringExtra("imagen_url");

        // Prellenar campos
        etTitulo.setText(titulo);
        etDescripcion.setText(descripcion);
        etPrecio.setText(String.valueOf(precio));

        // Manejar categoría: si no está en la lista, mostrar "Otros" + campo custom
        boolean esCategoriaEstandar = false;
        for (String cat : CATEGORIAS) {
            if (cat.equals(categoria)) {
                esCategoriaEstandar = true;
                break;
            }
        }
        if (esCategoriaEstandar) {
            actvCategoria.setText(categoria, false);
        } else {
            actvCategoria.setText("Otros", false);
            tilCategoriaPersonalizada.setVisibility(View.VISIBLE);
            etCategoriaPersonalizada.setText(categoria);
        }

        // Cargar imagen actual
        if (imagenUrlActual != null && !imagenUrlActual.isEmpty()) {
            Glide.with(this).load(imagenUrlActual).centerCrop()
                    .placeholder(R.drawable.ic_launcher_background).into(ivPreview);
        }

        btnImagen.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));
        btnGuardar.setOnClickListener(v -> guardarCambios());
    }

    private void guardarCambios() {
        String titulo = etTitulo.getText() != null ? etTitulo.getText().toString().trim() : "";
        String descripcion = etDescripcion.getText() != null ? etDescripcion.getText().toString().trim() : "";
        String precioStr = etPrecio.getText() != null ? etPrecio.getText().toString().trim() : "";
        String categoriaSeleccionada = actvCategoria.getText().toString().trim();

        String categoria;
        if ("Otros".equals(categoriaSeleccionada)) {
            categoria = etCategoriaPersonalizada.getText() != null
                    ? etCategoriaPersonalizada.getText().toString().trim() : "";
            if (categoria.isEmpty()) {
                Toast.makeText(this, "Escribe el nombre de tu categoría", Toast.LENGTH_SHORT).show();
                return;
            }
        } else {
            categoria = categoriaSeleccionada;
        }

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
        progressBar.setVisibility(View.VISIBLE);
        String finalCategoria = categoria;

        if (imageUri != null) {
            // Subir nueva imagen
            StorageManager storageManager = new StorageManager(this);
            storageManager.uploadProductImage(imageUri, auth0Id,
                    new StorageManager.UploadCallback() {
                        @Override
                        public void onSuccess(String imageUrl) {
                            actualizarProducto(titulo, descripcion, finalCategoria, precio, imageUrl);
                        }
                        @Override
                        public void onError(String error) {
                            runOnUiThread(() -> {
                                progressBar.setVisibility(View.GONE);
                                Toast.makeText(EditProductActivity.this,
                                        "Error al subir imagen, guardando sin cambiar imagen",
                                        Toast.LENGTH_SHORT).show();
                                actualizarProducto(titulo, descripcion, finalCategoria, precio, imagenUrlActual);
                            });
                        }
                    });
        } else {
            // Mantener imagen actual
            actualizarProducto(titulo, descripcion, finalCategoria, precio, imagenUrlActual);
        }
    }

    private void actualizarProducto(String titulo, String descripcion,
                                    String categoria, double precio, String imagenUrl) {
        productService.updateProduct(productoId, titulo, descripcion, categoria, precio, imagenUrl,
                new ProductService.ActionCallback() {
                    @Override
                    public void onSuccess() {
                        runOnUiThread(() -> {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(EditProductActivity.this,
                                    "¡Producto actualizado!", Toast.LENGTH_SHORT).show();
                            finish();
                        });
                    }
                    @Override
                    public void onError(String error) {
                        runOnUiThread(() -> {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(EditProductActivity.this,
                                    "Error: " + error, Toast.LENGTH_LONG).show();
                        });
                    }
                });
    }
}