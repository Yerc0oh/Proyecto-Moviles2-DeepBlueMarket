package com.moviles2.proyectomov2_deepbluemarket.ui.products;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
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
    private TextInputEditText etBuscar;
    private ProductAdapter adapter;
    private final List<Producto> todosLosProductos = new ArrayList<>();
    private final List<Producto> productosFiltrados = new ArrayList<>();
    private final ProductService productService = new ProductService();
    private String categoriaSeleccionada = null;
    private String textoBusqueda = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_list);

        recyclerView = findViewById(R.id.recyclerViewProducts);
        progressBar = findViewById(R.id.progressBar);
        tvEmpty = findViewById(R.id.tvEmpty);
        chipGroup = findViewById(R.id.chipGroupCategorias);
        etBuscar = findViewById(R.id.etBuscar);

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

        // Búsqueda en tiempo real mientras escribe
        etBuscar.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                textoBusqueda = s != null ? s.toString().trim() : "";
                aplicarFiltro();
            }
        });

        // También filtra al presionar "Buscar" en el teclado
        etBuscar.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                textoBusqueda = etBuscar.getText() != null
                        ? etBuscar.getText().toString().trim() : "";
                aplicarFiltro();
                return true;
            }
            return false;
        });

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

        Chip chipTodas = new Chip(this);
        chipTodas.setText("Todas");
        chipTodas.setCheckable(true);
        chipTodas.setChecked(true);
        chipTodas.setChipBackgroundColorResource(
                com.google.android.material.R.color.m3_chip_background_color);
        chipGroup.addView(chipTodas);

        Set<String> categorias = new LinkedHashSet<>();
        for (Producto p : todosLosProductos) {
            if (p.getCategoria() != null && !p.getCategoria().isEmpty()) {
                categorias.add(p.getCategoria());
            }
        }

        for (String categoria : categorias) {
            Chip chip = new Chip(this);
            chip.setText(categoria);
            chip.setCheckable(true);
            chip.setChipBackgroundColorResource(
                    com.google.android.material.R.color.m3_chip_background_color);
            chipGroup.addView(chip);
        }

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

        List<Producto> resultado = todosLosProductos.stream()
                // Filtro por categoría
                .filter(p -> categoriaSeleccionada == null
                        || categoriaSeleccionada.equalsIgnoreCase(p.getCategoria()))
                // Filtro por texto de búsqueda
                .filter(p -> textoBusqueda.isEmpty()
                        || (p.getTitulo() != null
                        && p.getTitulo().toLowerCase().contains(textoBusqueda.toLowerCase())))
                .collect(Collectors.toList());

        productosFiltrados.addAll(resultado);
        adapter.notifyDataSetChanged();

        if (productosFiltrados.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);

            if (!textoBusqueda.isEmpty() && categoriaSeleccionada != null) {
                tvEmpty.setText("No hay resultados para \"" + textoBusqueda
                        + "\" en " + categoriaSeleccionada);
            } else if (!textoBusqueda.isEmpty()) {
                tvEmpty.setText("No se encontraron productos para \"" + textoBusqueda + "\"");
            } else if (categoriaSeleccionada != null) {
                tvEmpty.setText("No hay productos en \"" + categoriaSeleccionada + "\"");
            } else {
                tvEmpty.setText("No hay productos disponibles");
            }
        } else {
            tvEmpty.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }
}