package es.uloyola.pdm.Alumni_android.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import es.uloyola.pdm.Alumni_android.R;
import es.uloyola.pdm.Alumni_android.conexionServer.PvoService;
import es.uloyola.pdm.Alumni_android.conexionServer.RetrofitClient;
import es.uloyola.pdm.Alumni_android.model.AlumniResumen;
import es.uloyola.pdm.Alumni_android.model.PerfilResponse;
import es.uloyola.pdm.Alumni_android.model.Trabajo;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** Ver perfil de otro alumni con privacidad aplicada por el backend. */
public class PerfilAlumniActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil_alumni);

        final String usuario = getIntent().getStringExtra("usuario");
        String nombrePrev = getIntent().getStringExtra("nombre");
        if (usuario == null) {
            Toast.makeText(this, "Usuario no indicado", Toast.LENGTH_SHORT).show();
            finish(); return;
        }
        setTitle(nombrePrev != null ? nombrePrev : "Perfil");

        final ProgressBar pb = findViewById(R.id.progressBar);
        pb.setVisibility(View.VISIBLE);

        RetrofitClient.getService().obtenerPerfil(usuario).enqueue(new Callback<PerfilResponse>() {
            @Override public void onResponse(Call<PerfilResponse> c, Response<PerfilResponse> r) {
                pb.setVisibility(View.GONE);
                PerfilResponse b = r.body();
                if (!r.isSuccessful() || b == null || !b.isSuccess() || b.getPerfil() == null) {
                    Toast.makeText(PerfilAlumniActivity.this, "No se pudo cargar el perfil", Toast.LENGTH_SHORT).show();
                    return;
                }
                pintar(b.getPerfil());
            }
            @Override public void onFailure(Call<PerfilResponse> c, Throwable t) {
                pb.setVisibility(View.GONE);
                Toast.makeText(PerfilAlumniActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void pintar(AlumniResumen a) {
        setTitle(a.getNombreCompleto());
        ((TextView) findViewById(R.id.tvNombre)).setText(a.getNombreCompleto());
        ((TextView) findViewById(R.id.tvTitulacion)).setText(a.getTitulacion());
        StringBuilder acad = new StringBuilder();
        if (a.getFacultad() != null) acad.append(a.getFacultad());
        if (a.getCampus() != null) { if (acad.length() > 0) acad.append(" · "); acad.append(a.getCampus()); }
        if (a.getPromocion() != null && a.getPromocion() > 0) {
            if (acad.length() > 0) acad.append(" · ");
            acad.append("Promocion ").append(a.getPromocion());
        }
        ((TextView) findViewById(R.id.tvAcademico)).setText(acad.toString());

        mostrar(R.id.tvEmail, a.getEmail() != null ? "Email: " + a.getEmail() : null);
        mostrar(R.id.tvTelefono, a.getTelefono() != null ? "Telefono: " + a.getTelefono() : null);
        mostrar(R.id.tvCiudad, a.getCiudad() != null ? "Ciudad: " + a.getCiudad() : null);

        String trabajo = null;
        Trabajo t = a.getTrabajo();
        if (t != null && t.getResumen() != null) trabajo = "Trabajo: " + t.getResumen();
        else if (a.getTrabajoActual() != null && !a.getTrabajoActual().isEmpty()) trabajo = "Trabajo: " + a.getTrabajoActual();
        mostrar(R.id.tvTrabajo, trabajo);

        mostrar(R.id.tvHobbies, a.getHobbies() != null && !a.getHobbies().isEmpty() ? "Hobbies: " + a.getHobbies() : null);
    }

    private void mostrar(int id, String texto) {
        TextView tv = findViewById(id);
        if (texto == null || texto.isEmpty()) tv.setVisibility(View.GONE);
        else { tv.setText(texto); tv.setVisibility(View.VISIBLE); }
    }
}
