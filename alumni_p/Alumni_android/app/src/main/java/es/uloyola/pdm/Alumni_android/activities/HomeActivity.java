package es.uloyola.pdm.Alumni_android.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
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

/** Pantalla principal post-login con menu de navegacion. Adapta botones segun el rol. */
public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Usuario u = SessionManager.get().getUsuario();
        if (u == null) {
            volverAlLogin();
            return;
        }
        String rol = SessionManager.get().getRol();

        ((TextView) findViewById(R.id.tvBienvenida))
                .setText(getString(R.string.home_bienvenido, u.getNombreCompleto()));
        ((TextView) findViewById(R.id.tvRol)).setText(getString(R.string.home_rol, rol));
        ((TextView) findViewById(R.id.tvEmail)).setText(u.getEmail() != null ? u.getEmail() : "");

        // Botones disponibles para todos los roles autenticados
        findViewById(R.id.btnDirectorio).setOnClickListener(v -> abrir(DirectorioActivity.class));
        findViewById(R.id.btnMiPerfil).setOnClickListener(v -> abrir(MiPerfilActivity.class));
        findViewById(R.id.btnEventos).setOnClickListener(v -> abrir(EventosActivity.class));
        findViewById(R.id.btnActividades).setOnClickListener(v -> abrir(ActividadesActivity.class));

        Button btnInscripciones = findViewById(R.id.btnMisInscripciones);
        Button btnProponer = findViewById(R.id.btnProponer);
        Button btnPropuestas = findViewById(R.id.btnPropuestas);
        Button btnAdmin = findViewById(R.id.btnAdminPanel);

        // Alumni y PDI: inscripciones + proponer
        if ("ALUMNI".equalsIgnoreCase(rol) || "PDI".equalsIgnoreCase(rol)) {
            btnInscripciones.setOnClickListener(v -> abrir(MisInscripcionesActivity.class));
            btnProponer.setOnClickListener(v -> abrir(CrearEventoActivity.class));
        } else {
            btnInscripciones.setVisibility(View.GONE);
            btnProponer.setText(R.string.publicar_evento);
            btnProponer.setOnClickListener(v -> abrir(CrearEventoActivity.class));
        }

        // PTGAS / ADMIN: ven propuestas y (admin) panel
        if ("PTGAS".equalsIgnoreCase(rol) || "ADMIN".equalsIgnoreCase(rol)) {
            btnPropuestas.setVisibility(View.VISIBLE);
            btnPropuestas.setOnClickListener(v -> abrir(PropuestasActivity.class));
        }
        if ("ADMIN".equalsIgnoreCase(rol)) {
            btnAdmin.setVisibility(View.VISIBLE);
            btnAdmin.setOnClickListener(v -> abrir(AdminPanelActivity.class));
        }

        findViewById(R.id.btnCerrarSesion).setOnClickListener(v -> cerrarSesion());
    }

    private void abrir(Class<?> activity) {
        startActivity(new Intent(this, activity));
    }

    private void cerrarSesion() {
        PvoService api = RetrofitClient.getService();
        api.logout().enqueue(new Callback<GenericResponse>() {
            @Override public void onResponse(Call<GenericResponse> c, Response<GenericResponse> r) {
                SessionManager.get().cerrarSesion();
                volverAlLogin();
            }
            @Override public void onFailure(Call<GenericResponse> c, Throwable t) {
                Toast.makeText(HomeActivity.this, "Cerrada localmente.", Toast.LENGTH_SHORT).show();
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
