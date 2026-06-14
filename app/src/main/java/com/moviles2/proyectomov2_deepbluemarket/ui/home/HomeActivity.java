package com.moviles2.proyectomov2_deepbluemarket.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.moviles2.proyectomov2_deepbluemarket.R;
import com.moviles2.proyectomov2_deepbluemarket.ui.auth.LoginActivity;
import com.moviles2.proyectomov2_deepbluemarket.ui.profile.ProfileActivity;
import com.moviles2.proyectomov2_deepbluemarket.utils.SessionManager;
import com.moviles2.proyectomov2_deepbluemarket.ui.products.ProductListActivity;
import com.moviles2.proyectomov2_deepbluemarket.ui.products.MyProductsActivity;
public class HomeActivity extends AppCompatActivity {

    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        sessionManager = new SessionManager(this);

        TextView tvWelcome = findViewById(R.id.tvWelcome);
        Button btnVerProductos = findViewById(R.id.btnVerProductos);
        Button btnMisProductos = findViewById(R.id.btnMisProductos);
        Button btnPerfil = findViewById(R.id.btnPerfil);
        Button btnSalir = findViewById(R.id.btnSalir);

        // Mostrar nombre del usuario desde la sesión local
        String nombre = sessionManager.getNombre();
        if (nombre != null && !nombre.isEmpty()) {
            tvWelcome.setText("¡Bienvenido, " + nombre + "!");
        } else {
            tvWelcome.setText("¡Bienvenido!");
        }

        btnVerProductos.setOnClickListener(v -> {
            startActivity(new Intent(HomeActivity.this, ProductListActivity.class));
        });

        btnMisProductos.setOnClickListener(v -> {
            startActivity(new Intent(HomeActivity.this, MyProductsActivity.class));
        });

        btnPerfil.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, ProfileActivity.class);
            startActivity(intent);
        });

        // Solo cierra la app, NO hace logout (eso se maneja en ProfileActivity)
        btnSalir.setOnClickListener(v -> finishAffinity());

        if (!sessionManager.haySesionActiva()) {
            // Si no hay sesión activa, redirigir a la pantalla de login
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        }


    }
}