package com.moviles2.proyectomov2_deepbluemarket.ui.products;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
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
import com.moviles2.proyectomov2_deepbluemarket.models.Usuario;
import com.moviles2.proyectomov2_deepbluemarket.network.UserService;
import android.content.Intent;

public class CreateProductActivity extends AppCompatActivity {

    private static final String TAG = "CreateProductActivity";

    private TextInputEditText etTitulo, etDescripcion, etPrecio, etCategoriaPersonalizada;
    private AutoCompleteTextView actvCategoria;
    private TextInputLayout tilCategoriaPersonalizada;
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
        // Verificar que el usuario tenga teléfono antes de mostrar el formulario
        String auth0Id = sessionManager.getAuth0Id();
        UserService.getInstance().getUserByAuth0Id(auth0Id, new UserService.UserCallback() {
            @Override
            public void onSuccess(Usuario usuario) {
                runOnUiThread(() -> {
                    String tel = usuario.getTelefono();
                    if (tel == null || tel.trim().isEmpty()) {
                        // No tiene teléfono → mostrar dialog y no dejar publicar
                        new androidx.appcompat.app.AlertDialog.Builder(CreateProductActivity.this)
                                .setTitle("Teléfono requerido")
                                .setMessage("Para publicar productos necesitas agregar tu número de WhatsApp en tu perfil.")
                                .setPositiveButton("Ir al perfil", (d, w) -> {
                                    startActivity(new Intent(CreateProductActivity.this,
                                            com.moviles2.proyectomov2_deepbluemarket.ui.profile.UpdateProfileActivity.class));
                                    finish();
                                })
                                .setNegativeButton("Cancelar", (d, w) -> finish())
                                .setCancelable(false)
                                .show();
                    }
                    // Si tiene teléfono, no hace nada — el formulario ya está visible
                });
            }

            @Override
            public void onError(String error) {
                // Si hay error verificando, dejamos publicar igual
            }
        });
        etTitulo = findViewById(R.id.etTitulo);
        etDescripcion = findViewById(R.id.etDescripcion);
        etPrecio = findViewById(R.id.etPrecio);
        actvCategoria = findViewById(R.id.actvCategoria);
        etCategoriaPersonalizada = findViewById(R.id.etCategoriaPersonalizada);
        tilCategoriaPersonalizada = findViewById(R.id.tilCategoriaPersonalizada);
        ivPreview = findViewById(R.id.ivProductPreview);
        progressBar = findViewById(R.id.progressBar);

        Button btnImagen = findViewById(R.id.btnSeleccionarImagen);
        Button btnPublicar = findViewById(R.id.btnPublicar);

        // Dropdown de categorías
        ArrayAdapter<String> catAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, CATEGORIAS);
        actvCategoria.setAdapter(catAdapter);

        // Mostrar campo extra solo si elige "Otros"
        actvCategoria.setOnItemClickListener((parent, view, position, id) -> {
            String seleccionado = CATEGORIAS[position];
            if ("Otros".equals(seleccionado)) {
                tilCategoriaPersonalizada.setVisibility(View.VISIBLE);
                etCategoriaPersonalizada.requestFocus();
            } else {
                tilCategoriaPersonalizada.setVisibility(View.GONE);
                etCategoriaPersonalizada.setText("");
            }
        });

        btnImagen.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));
        btnPublicar.setOnClickListener(v -> publicarProducto());
    }

    private void publicarProducto() {
        String titulo = etTitulo.getText() != null ? etTitulo.getText().toString().trim() : "";
        String descripcion = etDescripcion.getText() != null ? etDescripcion.getText().toString().trim() : "";
        String precioStr = etPrecio.getText() != null ? etPrecio.getText().toString().trim() : "";
        String categoriaSeleccionada = actvCategoria.getText().toString().trim();

        // Si eligió "Otros", usar el campo personalizado
        String categoria;
        if ("Otros".equals(categoriaSeleccionada)) {
            categoria = etCategoriaPersonalizada.getText() != null
                    ? etCategoriaPersonalizada.getText().toString().trim()
                    : "";
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
        if (auth0Id == null || auth0Id.isEmpty()) {
            Toast.makeText(this, "Sesión no válida", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        if (imageUri != null) {
            StorageManager storageManager = new StorageManager(this);
            String finalCategoria = categoria;

            storageManager.uploadProductImage(
                    imageUri,
                    auth0Id,
                    new StorageManager.UploadCallback() {
                        @Override
                        public void onSuccess(String imageUrl) {
                            Log.d(TAG, "Imagen subida: " + imageUrl);
                            crearProducto(auth0Id, titulo, descripcion, finalCategoria, precio, imageUrl);
                        }

                        @Override
                        public void onError(String error) {
                            Log.e(TAG, "Error imagen: " + error);
                            runOnUiThread(() -> {
                                progressBar.setVisibility(View.GONE);
                                // Si falla la imagen, publicamos sin imagen
                                Toast.makeText(CreateProductActivity.this,
                                        "No se pudo subir la imagen, publicando sin imagen...",
                                        Toast.LENGTH_SHORT).show();
                                crearProducto(auth0Id, titulo, descripcion, finalCategoria, precio, "");
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