package es.uloyola.pdm.Alumni_android.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import es.uloyola.pdm.Alumni_android.R;
import es.uloyola.pdm.Alumni_android.classes.Usuario;
import es.uloyola.pdm.Alumni_android.conexionServer.PvoService;
import es.uloyola.pdm.Alumni_android.conexionServer.RetrofitClient;
import es.uloyola.pdm.Alumni_android.model.GenericResponse;
import es.uloyola.pdm.Alumni_android.session.SessionManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * HomeActivity
 * ------------
 * Pantalla provisional tras el login.
 *
 * Muestra el nombre y rol del usuario autenticado leyendolo de
 * SessionManager (no hace falta volver a llamar al backend para esto).
 * Permite cerrar sesion: invoca POST /LogoutServlet, limpia la cookie de
 * sesion y vuelve al login.
 *
 * En fases posteriores aqui ira la navegacion principal (directorio,
 * perfil, eventos, actividades, etc.).
 */
public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        TextView tvBienvenida   = findViewById(R.id.tvBienvenida);
        TextView tvRol          = findViewById(R.id.tvRol);
        TextView tvEmail        = findViewById(R.id.tvEmail);
        Button   btnCerrarSesion = findViewById(R.id.btnCerrarSesion);

        // Si por algun motivo se llega aqui sin sesion, volver al login
        Usuario u = SessionManager.get().getUsuario();
        if (u == null) {
            volverAlLogin();
            return;
        }

        tvBienvenida.setText(getString(R.string.home_bienvenido, u.getNombreCompleto()));
        tvRol.setText(getString(R.string.home_rol, SessionManager.get().getRol()));
        tvEmail.setText(u.getEmail() != null ? u.getEmail() : "");

        btnCerrarSesion.setOnClickListener(v -> cerrarSesion());
    }

    /** Invoca al backend para invalidar la sesion y limpia el estado local. */
    private void cerrarSesion() {
        PvoService api = RetrofitClient.getService();
        api.logout().enqueue(new Callback<GenericResponse>() {
            @Override
            public void onResponse(Call<GenericResponse> call, Response<GenericResponse> response) {
                // Tanto si responde bien como si no, limpiamos el estado local
                SessionManager.get().cerrarSesion();
                volverAlLogin();
            }

            @Override
            public void onFailure(Call<GenericResponse> call, Throwable t) {
                Toast.makeText(HomeActivity.this,
                        "No se pudo contactar con el servidor, sesion cerrada localmente.",
                        Toast.LENGTH_SHORT).show();
                SessionManager.get().cerrarSesion();
                volverAlLogin();
            }
        });
    }

    private void volverAlLogin() {
        Intent i = new Intent(this, LoginActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }
}
