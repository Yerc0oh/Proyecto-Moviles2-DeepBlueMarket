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
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.moviles2.proyectomov2_deepbluemarket.R;
import com.moviles2.proyectomov2_deepbluemarket.models.Producto;
import com.moviles2.proyectomov2_deepbluemarket.network.ProductService;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ProductListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private ChipGroup chipGroup;
    private ProductAdapter adapter;
    private final List<Producto> todosLosProductos = new ArrayList<>();
    private final List<Producto> productosFiltrados = new ArrayList<>();
    private final ProductService productService = new ProductService();
    private String categoriaSeleccionada = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_list);

        recyclerView = findViewById(R.id.recyclerViewProducts);
        progressBar = findViewById(R.id.progressBar);
        tvEmpty = findViewById(R.id.tvEmpty);
        chipGroup = findViewById(R.id.chipGroupCategorias);

        adapter = new ProductAdapter(this, productosFiltrados, producto -> {
            Intent intent = new Intent(this, ProductDetailActivity.class);
            intent.putExtra("id", producto.getId());
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
                    todosLosProductos.clear();
                    todosLosProductos.addAll(result);
                    construirChips();
                    aplicarFiltro();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(ProductListActivity.this,
                            "Error: " + error, Toast.LENGTH_LONG).show();
                    tvEmpty.setVisibility(View.VISIBLE);
                });
            }
        });
    }

    private void construirChips() {
        chipGroup.removeAllViews();

        // Chip "Todas" siempre primero
        Chip chipTodas = new Chip(this);
        chipTodas.setText("Todas");
        chipTodas.setCheckable(true);
        chipTodas.setChecked(true);
        chipTodas.setChipBackgroundColorResource(
                com.google.android.material.R.color.m3_chip_background_color);
        chipGroup.addView(chipTodas);

        // Extraer categorías únicas de los productos reales
        Set<String> categorias = new LinkedHashSet<>();
        for (Producto p : todosLosProductos) {
            if (p.getCategoria() != null && !p.getCategoria().isEmpty()) {
                categorias.add(p.getCategoria());
            }
        }

        // Crear un chip por cada categoría real
        for (String categoria : categorias) {
            Chip chip = new Chip(this);
            chip.setText(categoria);
            chip.setCheckable(true);
            chip.setChipBackgroundColorResource(
                    com.google.android.material.R.color.m3_chip_background_color);
            chipGroup.addView(chip);
        }

        // Listener general sobre el ChipGroup
        chipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                categoriaSeleccionada = null;
            } else {
                Chip chipSeleccionado = findViewById(checkedIds.get(0));
                String texto = chipSeleccionado != null
                        ? chipSeleccionado.getText().toString() : null;
                categoriaSeleccionada = "Todas".equals(texto) ? null : texto;
            }
            aplicarFiltro();
        });
    }

    private void aplicarFiltro() {
        productosFiltrados.clear();

        if (categoriaSeleccionada == null) {
            productosFiltrados.addAll(todosLosProductos);
        } else {
            String filtro = categoriaSeleccionada;
            List<Producto> filtrados = todosLosProductos.stream()
                    .filter(p -> filtro.equalsIgnoreCase(p.getCategoria()))
                    .collect(Collectors.toList());
            productosFiltrados.addAll(filtrados);
        }

        adapter.notifyDataSetChanged();

        if (productosFiltrados.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            tvEmpty.setText(categoriaSeleccionada != null
                    ? "No hay productos en \"" + categoriaSeleccionada + "\""
                    : "No hay productos disponibles");
        } else {
            tvEmpty.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }
}