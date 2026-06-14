package com.moviles2.proyectomov2_deepbluemarket.ui.products;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.moviles2.proyectomov2_deepbluemarket.R;
import com.moviles2.proyectomov2_deepbluemarket.utils.SessionManager;
import com.moviles2.proyectomov2_deepbluemarket.models.Producto;
import com.moviles2.proyectomov2_deepbluemarket.network.ProductService;
import java.util.ArrayList;
import java.util.List;

public class MyProductsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private ProductAdapter adapter;
    private final List<Producto> productos = new ArrayList<>();
    private final ProductService productService = new ProductService();
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_products);

        sessionManager = new SessionManager(this);

        recyclerView = findViewById(R.id.recyclerViewMyProducts);
        progressBar = findViewById(R.id.progressBar);
        tvEmpty = findViewById(R.id.tvEmpty);
        Button btnAgregar = findViewById(R.id.btnAgregarProducto);

        btnAgregar.setOnClickListener(v ->
                startActivity(new Intent(this, CreateProductActivity.class)));

        adapter = new ProductAdapter(this, productos, producto -> {
            Intent intent = new Intent(this, ProductDetailActivity.class);
            intent.putExtra("titulo", producto.getTitulo());
            intent.putExtra("descripcion", producto.getDescripcion());
            intent.putExtra("categoria", producto.getCategoria());
            intent.putExtra("precio", producto.getPrecio());
            intent.putExtra("imagen_url", producto.getImagenUrl());
            startActivity(intent);
        });
        adapter.setActionListener(new ProductAdapter.OnProductActionListener() {
            @Override
            public void onEdit(Producto producto) {
                Intent intent = new Intent(MyProductsActivity.this, EditProductActivity.class);
                intent.putExtra("id", producto.getId());
                intent.putExtra("titulo", producto.getTitulo());
                intent.putExtra("descripcion", producto.getDescripcion());
                intent.putExtra("categoria", producto.getCategoria());
                intent.putExtra("precio", producto.getPrecio());
                intent.putExtra("imagen_url", producto.getImagenUrl());
                startActivity(intent);
            }

            @Override
            public void onDelete(Producto producto) {
                new androidx.appcompat.app.AlertDialog.Builder(MyProductsActivity.this)
                        .setTitle("Eliminar producto")
                        .setMessage("¿Estás seguro de que quieres eliminar \"" + producto.getTitulo() + "\"?")
                        .setPositiveButton("Eliminar", (dialog, which) -> {
                            productService.deleteProduct(producto.getId(), new ProductService.ActionCallback() {
                                @Override
                                public void onSuccess() {
                                    runOnUiThread(() -> {
                                        Toast.makeText(MyProductsActivity.this,
                                                "Producto eliminado", Toast.LENGTH_SHORT).show();
                                        loadMyProducts();
                                    });
                                }
                                @Override
                                public void onError(String error) {
                                    runOnUiThread(() ->
                                            Toast.makeText(MyProductsActivity.this,
                                                    "Error: " + error, Toast.LENGTH_LONG).show());
                                }
                            });
                        })
                        .setNegativeButton("Cancelar", null)
                        .show();
            }
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        loadMyProducts();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadMyProducts();
    }

    private void loadMyProducts() {
        String auth0Id = sessionManager.getAuth0Id();
        if (auth0Id == null || auth0Id.isEmpty()) {
            Toast.makeText(this, "Sesión no válida", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);

        productService.getProductsByAuth0Id(auth0Id, new ProductService.ProductCallback() {
            @Override
            public void onSuccess(List<Producto> result) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    productos.clear();
                    productos.addAll(result);
                    adapter.notifyDataSetChanged();
                    if (productos.isEmpty()) {
                        tvEmpty.setVisibility(View.VISIBLE);
                    } else {
                        recyclerView.setVisibility(View.VISIBLE);
                    }
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(MyProductsActivity.this, "Error: " + error, Toast.LENGTH_LONG).show();
                    tvEmpty.setVisibility(View.VISIBLE);
                });
            }
        });
    }
}