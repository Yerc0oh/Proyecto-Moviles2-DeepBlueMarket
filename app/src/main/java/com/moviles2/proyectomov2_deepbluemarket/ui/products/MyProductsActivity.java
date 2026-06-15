package com.moviles2.proyectomov2_deepbluemarket.ui.products;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.moviles2.proyectomov2_deepbluemarket.R;
import com.moviles2.proyectomov2_deepbluemarket.utils.OfertaPollingManager;
import com.moviles2.proyectomov2_deepbluemarket.utils.SessionManager;
import com.moviles2.proyectomov2_deepbluemarket.models.Producto;
import com.moviles2.proyectomov2_deepbluemarket.network.OfertaService;
import com.moviles2.proyectomov2_deepbluemarket.network.ProductService;
import java.util.ArrayList;
import java.util.List;
import com.moviles2.proyectomov2_deepbluemarket.utils.PdfReportGenerator;
import android.net.Uri;
import androidx.core.content.FileProvider;
import java.io.File;

public class MyProductsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private ProductAdapter adapter;
    private final List<Producto> productos = new ArrayList<>();
    private final ProductService productService = new ProductService();
    private final OfertaService ofertaService = new OfertaService();
    private SessionManager sessionManager;
    private OfertaPollingManager pollingManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 100);
            }
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_products);

        sessionManager = new SessionManager(this);
        pollingManager = new OfertaPollingManager(this);

        recyclerView = findViewById(R.id.recyclerViewMyProducts);
        progressBar = findViewById(R.id.progressBar);
        tvEmpty = findViewById(R.id.tvEmpty);
        Button btnAgregar = findViewById(R.id.btnAgregarProducto);

        btnAgregar.setOnClickListener(v ->
                startActivity(new Intent(this, CreateProductActivity.class)));

        Button btnGenerarPdf = findViewById(R.id.btnGenerarPdf);
        btnGenerarPdf.setOnClickListener(v -> generarReportePdf());

        adapter = new ProductAdapter(this, productos, producto -> {
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
                intent.putExtra("usuario_id", producto.getUsuarioId());
                startActivity(intent);
            }

            @Override
            public void onDelete(Producto producto) {
                new androidx.appcompat.app.AlertDialog.Builder(MyProductsActivity.this)
                        .setTitle("Eliminar producto")
                        .setMessage("¿Estás seguro de que quieres eliminar \"" + producto.getTitulo() + "\"?")
                        .setPositiveButton("Eliminar", (dialog, which) -> {
                            productService.deactivateProduct(producto.getId(), new ProductService.ActionCallback() {
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

            @Override
            public void onVerOfertas(Producto producto) {
                mostrarDialogVerOfertas(producto);
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
        pollingManager.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        pollingManager.stop();
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

    /**
     * Muestra un dialog con la lista de ofertas recibidas para el producto
     * indicado, permitiendo aceptar (abre WhatsApp) o rechazar (elimina
     * la oferta) cada una.
     */
    private void mostrarDialogVerOfertas(Producto producto) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_ver_ofertas, null);

        ProgressBar progressBarOfertas = dialogView.findViewById(R.id.progressBarOfertas);
        TextView tvSinOfertas = dialogView.findViewById(R.id.tvSinOfertas);
        RecyclerView recyclerOfertas = dialogView.findViewById(R.id.recyclerViewOfertas);

        recyclerOfertas.setLayoutManager(new LinearLayoutManager(this));

        androidx.appcompat.app.AlertDialog dialog = new androidx.appcompat.app.AlertDialog.Builder(this)
                .setView(dialogView)
                .setNegativeButton("Cerrar", null)
                .create();

        dialog.show();

        progressBarOfertas.setVisibility(View.VISIBLE);
        recyclerOfertas.setVisibility(View.GONE);
        tvSinOfertas.setVisibility(View.GONE);

        ofertaService.getOfertasByProducto(producto.getId(), new OfertaService.OfertaListCallback() {
            @Override
            public void onSuccess(List<OfertaService.OfertaConUsuario> ofertas) {
                runOnUiThread(() -> {
                    progressBarOfertas.setVisibility(View.GONE);

                    if (ofertas == null || ofertas.isEmpty()) {
                        tvSinOfertas.setVisibility(View.VISIBLE);
                        return;
                    }

                    recyclerOfertas.setVisibility(View.VISIBLE);

                    OfertaAdapter ofertaAdapter = new OfertaAdapter(
                            MyProductsActivity.this,
                            ofertas,
                            new OfertaAdapter.OnOfertaActionListener() {
                                @Override
                                public void onAceptar(OfertaService.OfertaConUsuario oferta) {
                                    abrirWhatsAppConOfertante(oferta, producto);
                                }

                                @Override
                                public void onRechazar(OfertaService.OfertaConUsuario oferta) {
                                    rechazarOferta(oferta, ofertas, recyclerOfertas, tvSinOfertas);
                                }
                            }
                    );

                    recyclerOfertas.setAdapter(ofertaAdapter);
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    progressBarOfertas.setVisibility(View.GONE);
                    tvSinOfertas.setText("Error al cargar ofertas: " + error);
                    tvSinOfertas.setVisibility(View.VISIBLE);
                });
            }
        });
    }

    /**
     * Abre WhatsApp con un mensaje pregrabado hacia el número del ofertante.
     */
    private void abrirWhatsAppConOfertante(OfertaService.OfertaConUsuario oferta, Producto producto) {
        String telefono = oferta.telefonoOfertante;

        if (telefono == null || telefono.isEmpty()) {
            Toast.makeText(this, "El ofertante no tiene número de WhatsApp registrado", Toast.LENGTH_SHORT).show();
            return;
        }

        String numeroLimpio = telefono.replaceAll("[^0-9]", "");
        if (!numeroLimpio.startsWith("591")) {
            numeroLimpio = "591" + numeroLimpio;
        }

        String nombre = oferta.nombreOfertante != null ? oferta.nombreOfertante : "";

        String mensaje = "Hola " + nombre + ", acepto tu oferta de Bs. "
                + String.format("%.2f", oferta.monto)
                + " por mi producto: *" + producto.getTitulo() + "*. ¡Coordinemos la entrega!";

        String url = "https://wa.me/" + numeroLimpio + "?text=" + android.net.Uri.encode(mensaje);

        startActivity(new Intent(Intent.ACTION_VIEW, android.net.Uri.parse(url)));
    }

    /**
     * Elimina la oferta de la base de datos (acción "Rechazar") y la
     * remueve de la lista mostrada en el dialog.
     */
    private void rechazarOferta(OfertaService.OfertaConUsuario oferta,
                                List<OfertaService.OfertaConUsuario> ofertas,
                                RecyclerView recyclerOfertas,
                                TextView tvSinOfertas) {

        ofertaService.eliminarOferta(oferta.id, new OfertaService.ActionCallback() {
            @Override
            public void onSuccess() {
                runOnUiThread(() -> {
                    ofertas.remove(oferta);
                    if (recyclerOfertas.getAdapter() != null) {
                        recyclerOfertas.getAdapter().notifyDataSetChanged();
                    }

                    if (ofertas.isEmpty()) {
                        recyclerOfertas.setVisibility(View.GONE);
                        tvSinOfertas.setText("Aún no hay ofertas para este producto.");
                        tvSinOfertas.setVisibility(View.VISIBLE);
                    }

                    Toast.makeText(MyProductsActivity.this, "Oferta rechazada", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() ->
                        Toast.makeText(MyProductsActivity.this,
                                "Error al rechazar oferta: " + error, Toast.LENGTH_LONG).show());
            }
        });
    }
    private void generarReportePdf() {

        if (productos.isEmpty()) {
            Toast.makeText(
                    this,
                    "No tienes productos para generar un reporte",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        try {

            File pdf =
                    PdfReportGenerator.generarReporteProductos(
                            this,
                            productos
                    );

            Toast.makeText(
                    this,
                    "PDF generado correctamente",
                    Toast.LENGTH_LONG
            ).show();

            Uri uri = FileProvider.getUriForFile(
                    this,
                    getPackageName() + ".provider",
                    pdf
            );

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, "application/pdf");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            startActivity(intent);

        } catch (Exception e) {

            Toast.makeText(
                    this,
                    "Error al generar PDF: " + e.getMessage(),
                    Toast.LENGTH_LONG
            ).show();

            e.printStackTrace();
        }
    }
}