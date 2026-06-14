package com.moviles2.proyectomov2_deepbluemarket.ui.products;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.moviles2.proyectomov2_deepbluemarket.R;
import com.moviles2.proyectomov2_deepbluemarket.models.Producto;
import com.moviles2.proyectomov2_deepbluemarket.network.ProductService;
import java.util.ArrayList;
import java.util.List;

public class ProductListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private ProductAdapter adapter;
    private final List<Producto> productos = new ArrayList<>();
    private final ProductService productService = new ProductService();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_list);

        recyclerView = findViewById(R.id.recyclerViewProducts);
        progressBar = findViewById(R.id.progressBar);
        tvEmpty = findViewById(R.id.tvEmpty);

        adapter = new ProductAdapter(this, productos, producto -> {
            Intent intent = new Intent(this, ProductDetailActivity.class);
            intent.putExtra("titulo", producto.getTitulo());
            intent.putExtra("descripcion", producto.getDescripcion());
            intent.putExtra("categoria", producto.getCategoria());
            intent.putExtra("precio", producto.getPrecio());
            intent.putExtra("imagen_url", producto.getImagenUrl());
            intent.putExtra("usuario_id", producto.getUsuarioId());
            startActivity(intent);
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        loadProducts();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadProducts();
    }

    private void loadProducts() {
        progressBar.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);

        productService.getAllProducts(new ProductService.ProductCallback() {
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
                    Toast.makeText(ProductListActivity.this, "Error: " + error, Toast.LENGTH_LONG).show();
                    tvEmpty.setVisibility(View.VISIBLE);
                });
            }
        });
    }
}
