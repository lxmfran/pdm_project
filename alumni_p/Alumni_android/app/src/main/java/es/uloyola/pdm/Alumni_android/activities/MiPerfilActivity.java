package es.uloyola.pdm.Alumni_android.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.HashMap;
import java.util.Map;

import es.uloyola.pdm.Alumni_android.R;
import es.uloyola.pdm.Alumni_android.conexionServer.PvoService;
import es.uloyola.pdm.Alumni_android.conexionServer.RetrofitClient;
import es.uloyola.pdm.Alumni_android.model.AlumniResumen;
import es.uloyola.pdm.Alumni_android.model.PerfilResponse;
import es.uloyola.pdm.Alumni_android.model.Trabajo;
import es.uloyola.pdm.Alumni_android.session.SessionManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** Ver y editar el propio perfil (solo para ALUMNI). */
public class MiPerfilActivity extends AppCompatActivity {

    private EditText etNombre, etApellidos, etEmail, etTelefono, etCiudad, etTitulacion, etPromocion, etTrabajo, etHobbies;
    private CheckBox cbCon, cbEmail, cbTel, cbCiu, cbTra, cbHob;
    private ProgressBar pb;
    private PvoService api;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_mi_perfil);
        setTitle(R.string.menu_mi_perfil);

        etNombre = findViewById(R.id.etNombre);
        etApellidos = findViewById(R.id.etApellidos);
        etEmail = findViewById(R.id.etEmail);
        etTelefono = findViewById(R.id.etTelefono);
        etCiudad = findViewById(R.id.etCiudad);
        etTitulacion = findViewById(R.id.etTitulacion);
        etPromocion = findViewById(R.id.etPromocion);
        etTrabajo = findViewById(R.id.etTrabajo);
        etHobbies = findViewById(R.id.etHobbies);
        cbCon = findViewById(R.id.cbMostrarContacto);
        cbEmail = findViewById(R.id.cbMostrarEmail);
        cbTel = findViewById(R.id.cbMostrarTelefono);
        cbCiu = findViewById(R.id.cbMostrarCiudad);
        cbTra = findViewById(R.id.cbMostrarTrabajo);
        cbHob = findViewById(R.id.cbMostrarHobbies);
        pb = findViewById(R.id.progressBar);

        api = RetrofitClient.getService();
        findViewById(R.id.btnGuardar).setOnClickListener(v -> guardar());

        cargar();
    }

    private void cargar() {
        String usuario = SessionManager.get().getUsuario().getUsuario();
        pb.setVisibility(View.VISIBLE);
        api.obtenerPerfil(usuario).enqueue(new Callback<PerfilResponse>() {
            @Override public void onResponse(Call<PerfilResponse> c, Response<PerfilResponse> r) {
                pb.setVisibility(View.GONE);
                PerfilResponse b = r.body();
                if (b == null || !b.isSuccess() || b.getPerfil() == null) {
                    Toast.makeText(MiPerfilActivity.this, "No se pudo cargar tu perfil", Toast.LENGTH_SHORT).show();
                    return;
                }
                pintar(b.getPerfil());
            }
            @Override public void onFailure(Call<PerfilResponse> c, Throwable t) {
                pb.setVisibility(View.GONE);
                Toast.makeText(MiPerfilActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void pintar(AlumniResumen a) {
        etNombre.setText(a.getNombre());
        etApellidos.setText(a.getApellidos());
        etEmail.setText(a.getEmail());
        etTelefono.setText(a.getTelefono());
        etCiudad.setText(a.getCiudad());
        etTitulacion.setText(a.getTitulacion());
        if (a.getPromocion() != null) etPromocion.setText(String.valueOf(a.getPromocion()));
        etTrabajo.setText(a.getTrabajoActual() != null ? a.getTrabajoActual()
                : (a.getTrabajo() != null ? a.getTrabajo().getDescripcion() : ""));
        etHobbies.setText(a.getHobbies());
        cbCon.setChecked(Boolean.TRUE.equals(a.getMostrarContacto()));
        cbEmail.setChecked(Boolean.TRUE.equals(a.getMostrarEmail()));
        cbTel.setChecked(Boolean.TRUE.equals(a.getMostrarTelefono()));
        cbCiu.setChecked(Boolean.TRUE.equals(a.getMostrarCiudad()));
        cbTra.setChecked(Boolean.TRUE.equals(a.getMostrarTrabajo()));
        cbHob.setChecked(Boolean.TRUE.equals(a.getMostrarHobbies()));
    }

    private void guardar() {
        Map<String, Object> p = new HashMap<>();
        p.put("usuario", SessionManager.get().getUsuario().getUsuario());
        p.put("nombre", etNombre.getText().toString().trim());
        p.put("apellidos", etApellidos.getText().toString().trim());
        p.put("email", etEmail.getText().toString().trim());
        p.put("telefono", etTelefono.getText().toString().trim());
        p.put("ciudad", etCiudad.getText().toString().trim());
        p.put("titulacion", etTitulacion.getText().toString().trim());
        String prom = etPromocion.getText().toString().trim();
        if (!prom.isEmpty()) try { p.put("promocion", Integer.parseInt(prom)); } catch (NumberFormatException ignored) {}
        p.put("hobbies", etHobbies.getText().toString().trim());

        String trab = etTrabajo.getText().toString().trim();
        if (!trab.isEmpty()) {
            Map<String, String> t = new HashMap<>();
            t.put("descripcion", trab);
            t.put("lugar", "");
            t.put("ciudad", etCiudad.getText().toString().trim());
            p.put("trabajo", t);
        }

        p.put("mostrarContacto", cbCon.isChecked());
        p.put("mostrarEmail", cbEmail.isChecked());
        p.put("mostrarTelefono", cbTel.isChecked());
        p.put("mostrarCiudad", cbCiu.isChecked());
        p.put("mostrarTrabajo", cbTra.isChecked());
        p.put("mostrarHobbies", cbHob.isChecked());

        pb.setVisibility(View.VISIBLE);
        api.actualizarPerfil(p).enqueue(new Callback<PerfilResponse>() {
            @Override public void onResponse(Call<PerfilResponse> c, Response<PerfilResponse> r) {
                pb.setVisibility(View.GONE);
                PerfilResponse b = r.body();
                if (b != null && b.isSuccess())
                    Toast.makeText(MiPerfilActivity.this, "Perfil actualizado", Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(MiPerfilActivity.this, b != null && b.getError() != null ? b.getError() : "No se pudo guardar", Toast.LENGTH_LONG).show();
            }
            @Override public void onFailure(Call<PerfilResponse> c, Throwable t) {
                pb.setVisibility(View.GONE);
                Toast.makeText(MiPerfilActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
