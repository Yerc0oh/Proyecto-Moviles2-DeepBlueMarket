package com.moviles2.proyectomov2_deepbluemarket.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.moviles2.proyectomov2_deepbluemarket.R;
import com.moviles2.proyectomov2_deepbluemarket.auth.AuthManager;
import com.moviles2.proyectomov2_deepbluemarket.ui.auth.LoginActivity;
import com.moviles2.proyectomov2_deepbluemarket.ui.home.HomeActivity;
import com.moviles2.proyectomov2_deepbluemarket.utils.SessionManager;
import android.widget.ImageView;
import com.bumptech.glide.Glide;

public class ProfileActivity extends AppCompatActivity {

    private SessionManager sessionManager;
    private AuthManager authManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        sessionManager = new SessionManager(this);

        String clientId = getString(R.string.com_auth0_client_id);
        String domain = getString(R.string.com_auth0_domain);
        authManager = new AuthManager(this, clientId, domain);

        TextView tvNombre = findViewById(R.id.tvNombre);
        TextView tvEmail = findViewById(R.id.tvEmail);
        TextView tvAuth0Id = findViewById(R.id.tvAuth0Id);
        TextView tvVerificado = findViewById(R.id.tvVerificado);
        ImageView ivProfilePhoto = findViewById(R.id.ivProfilePhoto);

        Button btnEditarPerfil = findViewById(R.id.btnEditarPerfil);
        Button btnVolverHome = findViewById(R.id.btnVolverHome);
        Button btnCerrarSesion = findViewById(R.id.btnCerrarSesion);

        // Cargar datos desde la sesión local
        tvNombre.setText(valorOrDefault(sessionManager.getNombre()));
        tvEmail.setText(valorOrDefault(sessionManager.getEmail()));
        tvAuth0Id.setText(valorOrDefault(sessionManager.getAuth0Id()));
        tvVerificado.setText(sessionManager.isEmailVerificado() ? "Verificado" : "No verificado");

        btnEditarPerfil.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, UpdateProfileActivity.class);
            startActivity(intent);
        });

        btnVolverHome.setOnClickListener(v -> {
            finish();
        });

        btnCerrarSesion.setOnClickListener(v -> cerrarSesion());
        String fotoUrl = sessionManager.getFotoPerfil();

        if (fotoUrl != null && !fotoUrl.isEmpty()) {
            Glide.with(ProfileActivity.this)
                    .load(fotoUrl)
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .error(R.drawable.ic_launcher_foreground)
                    .circleCrop()
                    .into(ivProfilePhoto);
        }
    }

    /**
     * Realiza el logout real: cierra sesión en Auth0, limpia SessionManager
     * y redirige a LoginActivity.
     */
    private void cerrarSesion() {
        authManager.logout(() -> {
            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private String valorOrDefault(String valor) {
        return (valor == null || valor.isEmpty()) ? "No disponible" : valor;
    }
}