package com.moviles2.proyectomov2_deepbluemarket.ui.profile;

import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.textfield.TextInputEditText;
import com.moviles2.proyectomov2_deepbluemarket.R;
import com.moviles2.proyectomov2_deepbluemarket.models.Usuario;
import com.moviles2.proyectomov2_deepbluemarket.network.UserService;
import com.moviles2.proyectomov2_deepbluemarket.storage.StorageManager;
import com.moviles2.proyectomov2_deepbluemarket.utils.SessionManager;

public class UpdateProfileActivity extends AppCompatActivity {

    private SessionManager sessionManager;
    private UserService userService;
    private StorageManager storageManager;

    private TextInputEditText etNombre;
    private TextInputEditText etTelefono;
    private ImageView ivFotoPerfil;
    private TextView tvEstadoVerificacion;
    private Button btnSubirDocumento;

    private Usuario usuarioActual;

    private ActivityResultLauncher<String> seleccionarFotoPerfilLauncher;
    private ActivityResultLauncher<String> seleccionarDocumentoLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_profile);

        sessionManager = new SessionManager(this);
        userService = UserService.getInstance();
        storageManager = new StorageManager(this);

        etNombre = findViewById(R.id.etNombre);
        etTelefono = findViewById(R.id.etTelefono);
        ivFotoPerfil = findViewById(R.id.ivFotoPerfil);
        tvEstadoVerificacion = findViewById(R.id.tvEstadoVerificacion);
        btnSubirDocumento = findViewById(R.id.btnSubirDocumento);

        Button btnGuardarCambios = findViewById(R.id.btnGuardarCambios);
        Button btnCancelar = findViewById(R.id.btnCancelar);
        Button btnCambiarFoto = findViewById(R.id.btnCambiarFoto);

        // Launcher para seleccionar foto de perfil desde galería
        seleccionarFotoPerfilLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        subirFotoPerfil(uri);
                    }
                }
        );

        // Launcher para seleccionar foto de documento de verificación
        seleccionarDocumentoLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        subirDocumentoVerificacion(uri);
                    }
                }
        );

        cargarDatosUsuario();

        btnGuardarCambios.setOnClickListener(v -> guardarCambios());

        btnCancelar.setOnClickListener(v -> finish());

        btnCambiarFoto.setOnClickListener(v ->
                seleccionarFotoPerfilLauncher.launch("image/*"));

        btnSubirDocumento.setOnClickListener(v ->
                seleccionarDocumentoLauncher.launch("image/*"));
    }

    /**
     * Carga los datos actuales del usuario desde Supabase usando el auth0_id
     * guardado en SessionManager, y pre-llena los campos del formulario.
     */
    private void cargarDatosUsuario() {
        String auth0Id = sessionManager.getAuth0Id();

        if (auth0Id == null || auth0Id.isEmpty()) {
            Toast.makeText(this, "No hay sesión activa", Toast.LENGTH_SHORT).show();
            // Pre-llenar al menos con datos locales como fallback
            etNombre.setText(sessionManager.getNombre());
            return;
        }

        userService.getUserByAuth0Id(auth0Id, new UserService.UserCallback() {
            @Override
            public void onSuccess(Usuario usuario) {
                usuarioActual = usuario;
                etNombre.setText(usuario.getNombre());
                etTelefono.setText(usuario.getTelefono());

                actualizarEstadoVerificacion(usuario.getFotoDocumentoUrl());

                if (usuario.getFotoPerfilUrl() != null && !usuario.getFotoPerfilUrl().isEmpty()) {
                    Glide.with(UpdateProfileActivity.this)
                            .load(usuario.getFotoPerfilUrl())
                            .placeholder(android.R.drawable.sym_def_app_icon)
                            .error(android.R.drawable.sym_def_app_icon)
                            .circleCrop()
                            .into(ivFotoPerfil);
                }
            }

            @Override
            public void onError(String errorMessage) {
                Toast.makeText(UpdateProfileActivity.this,
                        "Error al cargar datos: " + errorMessage,
                        Toast.LENGTH_SHORT).show();

                // Fallback con datos locales
                etNombre.setText(sessionManager.getNombre());
            }
        });
    }

    /**
     * Actualiza el texto y estilo del botón/estado según si el usuario
     * ya tiene una foto de documento de verificación subida.
     */
    private void actualizarEstadoVerificacion(String fotoDocumentoUrl) {
        boolean subido = fotoDocumentoUrl != null && !fotoDocumentoUrl.isEmpty();

        if (subido) {
            tvEstadoVerificacion.setText("Subida");
            tvEstadoVerificacion.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
            btnSubirDocumento.setText("Cambiar");
        } else {
            tvEstadoVerificacion.setText("Sin subir");
            tvEstadoVerificacion.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            btnSubirDocumento.setText("Subir foto");
        }
    }

    /**
     * Sube la imagen seleccionada como foto de perfil a Supabase Storage
     * y actualiza el registro del usuario con la nueva URL pública.
     */
    private void subirFotoPerfil(Uri imageUri) {
        String auth0Id = sessionManager.getAuth0Id();

        if (auth0Id == null) {
            Toast.makeText(this, "No hay sesión activa", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, "Subiendo foto de perfil...", Toast.LENGTH_SHORT).show();

        storageManager.uploadProfileImage(imageUri, auth0Id, new StorageManager.UploadCallback() {
            @Override
            public void onSuccess(String publicUrl) {
                Glide.with(UpdateProfileActivity.this)
                        .load(publicUrl)
                        .placeholder(android.R.drawable.sym_def_app_icon)
                        .error(android.R.drawable.sym_def_app_icon)
                        .circleCrop()
                        .into(ivFotoPerfil);

                if (usuarioActual != null) {
                    usuarioActual.setFotoPerfilUrl(publicUrl);
                }

                Toast.makeText(UpdateProfileActivity.this,
                        "Foto de perfil subida correctamente",
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String errorMessage) {
                Toast.makeText(UpdateProfileActivity.this,
                        "Error al subir foto: " + errorMessage,
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * Sube la imagen seleccionada como documento de identidad para
     * verificación a Supabase Storage y actualiza el estado visual.
     */
    private void subirDocumentoVerificacion(Uri imageUri) {
        String auth0Id = sessionManager.getAuth0Id();

        if (auth0Id == null) {
            Toast.makeText(this, "No hay sesión activa", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, "Subiendo documento...", Toast.LENGTH_SHORT).show();

        storageManager.uploadProfileVerificationImage(imageUri, auth0Id, new StorageManager.UploadCallback() {
            @Override
            public void onSuccess(String publicUrl) {
                if (usuarioActual != null) {
                    usuarioActual.setFotoDocumentoUrl(publicUrl);
                }

                actualizarEstadoVerificacion(publicUrl);

                Toast.makeText(UpdateProfileActivity.this,
                        "Documento subido correctamente",
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String errorMessage) {
                Toast.makeText(UpdateProfileActivity.this,
                        "Error al subir documento: " + errorMessage,
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * Valida los campos y guarda los cambios en Supabase.
     */
    private void guardarCambios() {
        String nombre = etNombre.getText() != null ? etNombre.getText().toString().trim() : "";
        String telefono = etTelefono.getText() != null ? etTelefono.getText().toString().trim() : "";

        if (nombre.isEmpty()) {
            etNombre.setError("El nombre no puede estar vacío");
            return;
        }

        if (usuarioActual == null) {
            Toast.makeText(this, "No se pudieron cargar los datos del usuario", Toast.LENGTH_SHORT).show();
            return;
        }

        usuarioActual.setNombre(nombre);
        usuarioActual.setTelefono(telefono);

        userService.updateUser(usuarioActual, new UserService.UserCallback() {
            @Override
            public void onSuccess(Usuario usuarioActualizado) {
                sessionManager.guardarNombre(usuarioActualizado.getNombre());

                Toast.makeText(UpdateProfileActivity.this,
                        "Perfil actualizado correctamente",
                        Toast.LENGTH_SHORT).show();

                finish();
            }

            @Override
            public void onError(String errorMessage) {
                Toast.makeText(UpdateProfileActivity.this,
                        "Error al actualizar: " + errorMessage,
                        Toast.LENGTH_LONG).show();
            }
        });
    }
}