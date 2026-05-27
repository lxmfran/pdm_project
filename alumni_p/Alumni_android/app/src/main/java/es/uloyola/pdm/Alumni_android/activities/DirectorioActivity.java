package es.uloyola.pdm.Alumni_android.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.HashMap;
import java.util.Map;

import es.uloyola.pdm.Alumni_android.R;
import es.uloyola.pdm.Alumni_android.adapters.AlumniAdapter;
import es.uloyola.pdm.Alumni_android.conexionServer.PvoService;
import es.uloyola.pdm.Alumni_android.conexionServer.RetrofitClient;
import es.uloyola.pdm.Alumni_android.model.AlumniResumen;
import es.uloyola.pdm.Alumni_android.model.BuscarAlumniResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** Directorio de alumni con busqueda por texto, filtros y scroll infinito. */
public class DirectorioActivity extends AppCompatActivity implements AlumniAdapter.OnClick {

    private static final String[] FACULTADES = {"(todas)", "INGENIERIA", "DERECHO", "ADE", "ECONOMICAS", "MEDICINA", "BIOLOGIA", "HUMANISMO", "MATEMATICAS"};
    private static final String[] CAMPUS = {"(todos)", "SEVILLA", "CORDOBA", "GRANADA", "ONLINE"};

    private EditText etBusqueda;
    private TextView tvInfo, tvSinResultados;
    private ProgressBar progressBar;
    private AlumniAdapter adapter;
    private PvoService api;

    // Estado de filtros y paginacion
    private String fTexto, fTitulacion, fFacultad, fCampus, fCiudad, fTrabajo, fHobbies;
    private Integer fPromocion;
    private int page = 1;
    private final int size = 20;
    private int total = 0;
    private boolean cargando = false;
    private boolean hayMas = true;

    private final Handler debounceHandler = new Handler(Looper.getMainLooper());
    private Runnable pendiente;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_directorio);
        setTitle(R.string.menu_directorio);

        etBusqueda = findViewById(R.id.etBusqueda);
        tvInfo = findViewById(R.id.tvInfoResultados);
        tvSinResultados = findViewById(R.id.tvSinResultados);
        progressBar = findViewById(R.id.progressBar);
        ImageButton btnFiltros = findViewById(R.id.btnFiltros);
        RecyclerView rv = findViewById(R.id.rvAlumni);

        adapter = new AlumniAdapter(this);
        final LinearLayoutManager lm = new LinearLayoutManager(this);
        rv.setLayoutManager(lm);
        rv.setAdapter(adapter);
        rv.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override public void onScrolled(@NonNull RecyclerView r, int dx, int dy) {
                if (dy <= 0 || cargando || !hayMas) return;
                if (lm.findLastVisibleItemPosition() >= lm.getItemCount() - 5) cargarMas();
            }
        });

        etBusqueda.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void afterTextChanged(Editable s) {
                if (pendiente != null) debounceHandler.removeCallbacks(pendiente);
                pendiente = () -> {
                    fTexto = s.toString().trim();
                    if (fTexto.isEmpty()) fTexto = null;
                    nuevaBusqueda();
                };
                debounceHandler.postDelayed(pendiente, 500);
            }
        });

        btnFiltros.setOnClickListener(v -> abrirFiltros());

        api = RetrofitClient.getService();
        nuevaBusqueda();
    }

    private void abrirFiltros() {
        BottomSheetDialog dlg = new BottomSheetDialog(this);
        View v = getLayoutInflater().inflate(R.layout.bottom_sheet_filtros, null);
        dlg.setContentView(v);

        EditText eTit = v.findViewById(R.id.etFiltroTitulacion);
        EditText eProm = v.findViewById(R.id.etFiltroPromocion);
        Spinner spFac = v.findViewById(R.id.spFiltroFacultad);
        Spinner spCmp = v.findViewById(R.id.spFiltroCampus);
        EditText eCiu = v.findViewById(R.id.etFiltroCiudad);
        EditText eTra = v.findViewById(R.id.etFiltroTrabajo);
        EditText eHob = v.findViewById(R.id.etFiltroHobbies);

        spFac.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, FACULTADES));
        spCmp.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, CAMPUS));

        if (fTitulacion != null) eTit.setText(fTitulacion);
        if (fPromocion != null) eProm.setText(String.valueOf(fPromocion));
        if (fFacultad != null) spFac.setSelection(idxOrZero(FACULTADES, fFacultad));
        if (fCampus != null) spCmp.setSelection(idxOrZero(CAMPUS, fCampus));
        if (fCiudad != null) eCiu.setText(fCiudad);
        if (fTrabajo != null) eTra.setText(fTrabajo);
        if (fHobbies != null) eHob.setText(fHobbies);

        v.findViewById(R.id.btnLimpiarFiltros).setOnClickListener(x -> {
            fTitulacion = fFacultad = fCampus = fCiudad = fTrabajo = fHobbies = null;
            fPromocion = null;
            dlg.dismiss();
            nuevaBusqueda();
        });
        v.findViewById(R.id.btnAplicarFiltros).setOnClickListener(x -> {
            fTitulacion = aNull(eTit.getText().toString());
            String p = eProm.getText().toString().trim();
            try { fPromocion = p.isEmpty() ? null : Integer.parseInt(p); } catch (NumberFormatException e) { fPromocion = null; }
            fFacultad = spFac.getSelectedItemPosition() == 0 ? null : FACULTADES[spFac.getSelectedItemPosition()];
            fCampus = spCmp.getSelectedItemPosition() == 0 ? null : CAMPUS[spCmp.getSelectedItemPosition()];
            fCiudad = aNull(eCiu.getText().toString());
            fTrabajo = aNull(eTra.getText().toString());
            fHobbies = aNull(eHob.getText().toString());
            dlg.dismiss();
            nuevaBusqueda();
        });
        dlg.show();
    }

    private void nuevaBusqueda() {
        page = 1;
        hayMas = true;
        adapter.setAll(null);
        cargar();
    }

    private void cargarMas() { page++; cargar(); }

    private void cargar() {
        if (cargando) return;
        cargando = true;
        progressBar.setVisibility(View.VISIBLE);

        Map<String, String> q = new HashMap<>();
        if (fTexto != null) q.put("texto", fTexto);
        if (fTitulacion != null) q.put("titulacion", fTitulacion);
        if (fPromocion != null) q.put("promocion", String.valueOf(fPromocion));
        if (fFacultad != null) q.put("facultad", fFacultad);
        if (fCampus != null) q.put("campus", fCampus);
        if (fCiudad != null) q.put("ciudad", fCiudad);
        if (fTrabajo != null) q.put("trabajo", fTrabajo);
        if (fHobbies != null) q.put("hobbies", fHobbies);
        q.put("page", String.valueOf(page));
        q.put("size", String.valueOf(size));

        final int paginaActual = page;
        api.buscarAlumni(q).enqueue(new Callback<BuscarAlumniResponse>() {
            @Override public void onResponse(Call<BuscarAlumniResponse> c, Response<BuscarAlumniResponse> r) {
                cargando = false;
                progressBar.setVisibility(View.GONE);
                BuscarAlumniResponse b = r.body();
                if (!r.isSuccessful() || b == null || !b.isSuccess()) {
                    toast("No se pudo buscar"); return;
                }
                if (paginaActual == 1) adapter.setAll(b.getResultados());
                else adapter.addAll(b.getResultados());
                total = b.getTotal() == null ? adapter.getItemCount() : b.getTotal();
                hayMas = b.hayMasPaginas();
                tvInfo.setText("Mostrando " + adapter.getItemCount() + " de " + total);
                boolean vacio = adapter.getItemCount() == 0;
                tvSinResultados.setVisibility(vacio ? View.VISIBLE : View.GONE);
            }
            @Override public void onFailure(Call<BuscarAlumniResponse> c, Throwable t) {
                cargando = false;
                progressBar.setVisibility(View.GONE);
                toast("Error: " + t.getMessage());
            }
        });
    }

    @Override
    public void onAlumniClick(AlumniResumen a) {
        Intent i = new Intent(this, PerfilAlumniActivity.class);
        i.putExtra("usuario", a.getUsuario());
        i.putExtra("nombre", a.getNombreCompleto());
        startActivity(i);
    }

    private void toast(String s) { Toast.makeText(this, s, Toast.LENGTH_SHORT).show(); }
    private String aNull(String s) { return s == null || s.trim().isEmpty() ? null : s.trim(); }
    private int idxOrZero(String[] arr, String v) {
        for (int i = 0; i < arr.length; i++) if (arr[i].equalsIgnoreCase(v)) return i;
        return 0;
    }
}
