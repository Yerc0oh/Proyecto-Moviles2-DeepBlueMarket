package com.moviles2.proyectomov2_deepbluemarket.ui.auth;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.moviles2.proyectomov2_deepbluemarket.ui.home.HomeActivity;
import com.moviles2.proyectomov2_deepbluemarket.R;
import com.moviles2.proyectomov2_deepbluemarket.auth.AuthManager;

public class LoginActivity extends AppCompatActivity {

    private AuthManager authManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        String clientId = getString(R.string.com_auth0_client_id);
        String domain = getString(R.string.com_auth0_domain);

        authManager = new AuthManager(this, clientId, domain);

        // Si ya hay sesión activa, ir directo a HomeActivity
        if (authManager.hayUsuarioAutenticado()) {
            irAHome();
            return;
        }

        Button btnLogin = findViewById(R.id.btnLogin);
        Button btnRegister = findViewById(R.id.btnRegister);

        btnLogin.setOnClickListener(v -> realizarLogin());
        btnRegister.setOnClickListener(v -> realizarRegistro());
    }

    /**
     * Ejecuta el flujo de login con Auth0 (Universal Login).
     */
    private void realizarLogin() {
        try {
            authManager.login();
        } catch (Exception e) {
            Toast.makeText(this,
                    "Error al iniciar sesión: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Ejecuta el flujo de registro, forzando la pantalla "Sign Up"
     * de Auth0 mediante el parámetro screen_hint=signup.
     */
    private void realizarRegistro() {
        try {
            authManager.registro();
        } catch (Exception e) {
            Toast.makeText(this,
                    "Error al registrarse: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }

    private void irAHome() {
        startActivity(new android.content.Intent(this, HomeActivity.class));
        finish();
    }
}