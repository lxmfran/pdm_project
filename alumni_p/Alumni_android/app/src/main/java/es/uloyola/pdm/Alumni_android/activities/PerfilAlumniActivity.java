package es.uloyola.pdm.Alumni_android.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import es.uloyola.pdm.Alumni_android.R;
import es.uloyola.pdm.Alumni_android.conexionServer.RetrofitClient;
import es.uloyola.pdm.Alumni_android.model.AlumniResumen;
import es.uloyola.pdm.Alumni_android.model.PerfilResponse;
import es.uloyola.pdm.Alumni_android.model.Trabajo;
import es.uloyola.pdm.Alumni_android.util.AvatarUtil;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Perfil de un alumni con el diseno propuesto: cabecera azul + chips + cards
 * de FORMACION / EXPERIENCIA / INTERESES. La privacidad la sigue aplicando el
 * backend (los campos no visibles llegan a null).
 */
public class PerfilAlumniActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_perfil_alumni);

        final String usuario = getIntent().getStringExtra("usuario");
        String nombrePrev = getIntent().getStringExtra("nombre");
        if (usuario == null) { finish(); return; }
        setTitle(nombrePrev != null ? nombrePrev : "Perfil");

        final ProgressBar pb = findViewById(R.id.progressBar);
        pb.setVisibility(View.VISIBLE);

        RetrofitClient.getService().obtenerPerfil(usuario).enqueue(new Callback<PerfilResponse>() {
            @Override public void onResponse(Call<PerfilResponse> c, Response<PerfilResponse> r) {
                pb.setVisibility(View.GONE);
                PerfilResponse body = r.body();
                if (body == null || !body.isSuccess() || body.getPerfil() == null) {
                    Toast.makeText(PerfilAlumniActivity.this, "No se pudo cargar el perfil", Toast.LENGTH_SHORT).show();
                    return;
                }
                pintar(body.getPerfil());
            }
            @Override public void onFailure(Call<PerfilResponse> c, Throwable t) {
                pb.setVisibility(View.GONE);
                Toast.makeText(PerfilAlumniActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void pintar(AlumniResumen a) {
        setTitle(a.getNombreCompleto());

        // Avatar generado con iniciales
        ImageView avatar = findViewById(R.id.ivAvatar);
        AvatarUtil.pintarEn(avatar, a.getNombreCompleto(), 72);

        ((TextView) findViewById(R.id.tvNombre)).setText(a.getNombreCompleto());
        ((TextView) findViewById(R.id.tvTitulacion)).setText(a.getTitulacion() != null ? a.getTitulacion() : "");

        chip(R.id.chipCampus, a.getCampus());
        chip(R.id.chipPromocion, a.getPromocion() != null && a.getPromocion() > 0 ? "Promocion " + a.getPromocion() : null);
        chip(R.id.chipCiudad, a.getCiudad());

        // Contactar (solo si hay email o telefono visibles)
        boolean hayContacto = (a.getEmail() != null && !a.getEmail().isEmpty())
                || (a.getTelefono() != null && !a.getTelefono().isEmpty());
        findViewById(R.id.contactarBox).setVisibility(hayContacto ? View.VISIBLE : View.GONE);
        textoOOculto(R.id.tvEmailContacto, a.getEmail());
        textoOOculto(R.id.tvTelefonoContacto, a.getTelefono());

        // Card Formacion (siempre visible)
        dato(R.id.datoTitulacion, "TITULACION", a.getTitulacion());
        dato(R.id.datoFacultad, "FACULTAD", a.getFacultad());
        dato(R.id.datoCampus, "CAMPUS", a.getCampus());
        dato(R.id.datoPromocion, "ANIO GRADUACION",
                a.getAnioGraduacion() != null && a.getAnioGraduacion() > 0 ? String.valueOf(a.getAnioGraduacion()) : null);

        // Card Experiencia
        Trabajo t = a.getTrabajo();
        boolean hayTrabajo = false;
        if (t != null) {
            dato(R.id.datoPuesto, "PUESTO ACTUAL", t.getPosicion() != null ? t.getPosicion() : t.getDescripcion());
            dato(R.id.datoEmpresa, "EMPRESA / LUGAR", t.getLugar());
            dato(R.id.datoCiudadTrabajo, "CIUDAD TRABAJO", t.getCiudad());
            dato(R.id.datoInicio, "INICIO", t.getFechaInicio());
            hayTrabajo = anyText(t.getPosicion(), t.getDescripcion(), t.getLugar(), t.getCiudad());
        } else if (a.getTrabajoActual() != null && !a.getTrabajoActual().isEmpty()) {
            dato(R.id.datoPuesto, "PUESTO ACTUAL", a.getTrabajoActual());
            dato(R.id.datoEmpresa, null, null);
            dato(R.id.datoCiudadTrabajo, null, null);
            dato(R.id.datoInicio, null, null);
            hayTrabajo = true;
        }
        findViewById(R.id.cardTrabajo).setVisibility(hayTrabajo ? View.VISIBLE : View.GONE);

        // Card Intereses
        boolean hayHobbies = a.getHobbies() != null && !a.getHobbies().isEmpty();
        findViewById(R.id.cardHobbies).setVisibility(hayHobbies ? View.VISIBLE : View.GONE);
        if (hayHobbies) ((TextView) findViewById(R.id.tvHobbies)).setText(a.getHobbies());
    }

    private void chip(int id, String texto) {
        TextView tv = findViewById(id);
        if (texto == null || texto.isEmpty()) tv.setVisibility(View.GONE);
        else { tv.setText(texto); tv.setVisibility(View.VISIBLE); }
    }

    private void dato(int includeId, String etiqueta, String valor) {
        View v = findViewById(includeId);
        if (v == null) return;
        if (valor == null || valor.isEmpty()) { v.setVisibility(View.GONE); return; }
        v.setVisibility(View.VISIBLE);
        ((TextView) v.findViewById(R.id.tvEtiqueta)).setText(etiqueta != null ? etiqueta : "");
        ((TextView) v.findViewById(R.id.tvValor)).setText(valor);
    }

    private void textoOOculto(int id, String texto) {
        TextView tv = findViewById(id);
        if (texto == null || texto.isEmpty()) tv.setVisibility(View.GONE);
        else { tv.setText(texto); tv.setVisibility(View.VISIBLE); }
    }

    private boolean anyText(String... ss) {
        for (String s : ss) if (s != null && !s.isEmpty()) return true;
        return false;
    }
}
