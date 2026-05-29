package es.uloyola.pdm.Alumni_android.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import es.uloyola.pdm.Alumni_android.R;
import es.uloyola.pdm.Alumni_android.adapters.UsuarioAdminAdapter;
import es.uloyola.pdm.Alumni_android.classes.Usuario;
import es.uloyola.pdm.Alumni_android.conexionServer.PvoService;
import es.uloyola.pdm.Alumni_android.conexionServer.RetrofitClient;
import es.uloyola.pdm.Alumni_android.model.AlumniResumen;
import es.uloyola.pdm.Alumni_android.model.DashboardResponse;
import es.uloyola.pdm.Alumni_android.model.GenericResponse;
import es.uloyola.pdm.Alumni_android.model.UsuarioAdminResponse;
import es.uloyola.pdm.Alumni_android.model.UsuariosListResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Panel del administrador: metricas + gestion completa de usuarios (crear,
 * editar y eliminar - este ultimo es anonimizacion RGPD en el backend).
 */
public class AdminPanelActivity extends AppCompatActivity implements UsuarioAdminAdapter.Listener {

    private static final String[] ROLES = {"ALUMNI", "PDI", "PTGAS", "ADMIN"};
    private static final String[] CAMPUS = {"SEVILLA", "CORDOBA", "GRANADA", "ONLINE"};
    private static final String[] FACULTADES = {"INGENIERIA", "DERECHO", "ADE", "ECONOMICAS",
            "MEDICINA", "BIOLOGIA", "HUMANISMO", "MATEMATICAS"};

    private PvoService api;
    private ProgressBar pb;
    private TextView tvMetricas, tvFormTitulo;
    private UsuarioAdminAdapter adapter;

    private Spinner spRol, spCampus, spFacultad;
    private EditText etUsuario, etDni, etContrasenia, etNombre, etApellidos, etEmail,
            etTelefono, etTitulacion, etPromocion, etCiudad, etTrabajo, etHobbies;
    private Button btnGuardar, btnLimpiar;

    /** Si != null, estamos editando ese login (usuario login es la clave). */
    private String editandoUsuario;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_admin_panel);
        setTitle(R.string.menu_admin_panel);

        api = RetrofitClient.getService();
        pb = findViewById(R.id.progressBar);
        tvMetricas = findViewById(R.id.tvMetricas);
        tvFormTitulo = findViewById(R.id.tvFormTitulo);

        // Form bindings
        spRol = findViewById(R.id.spRol);
        spCampus = findViewById(R.id.spCampus);
        spFacultad = findViewById(R.id.spFacultad);
        etUsuario = findViewById(R.id.etUsuario);
        etDni = findViewById(R.id.etDni);
        etContrasenia = findViewById(R.id.etContrasenia);
        etNombre = findViewById(R.id.etNombre);
        etApellidos = findViewById(R.id.etApellidos);
        etEmail = findViewById(R.id.etEmail);
        etTelefono = findViewById(R.id.etTelefono);
        etTitulacion = findViewById(R.id.etTitulacion);
        etPromocion = findViewById(R.id.etPromocion);
        etCiudad = findViewById(R.id.etCiudad);
        etTrabajo = findViewById(R.id.etTrabajo);
        etHobbies = findViewById(R.id.etHobbies);
        btnGuardar = findViewById(R.id.btnGuardarUsuario);
        btnLimpiar = findViewById(R.id.btnLimpiar);

        spRol.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, ROLES));
        spCampus.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, CAMPUS));
        spFacultad.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, FACULTADES));

        // Lista de usuarios
        RecyclerView rv = findViewById(R.id.rvUsuarios);
        adapter = new UsuarioAdminAdapter(this);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);

        findViewById(R.id.btnActualizar).setOnClickListener(v -> cargarTodo());
        btnLimpiar.setOnClickListener(v -> limpiarForm());
        btnGuardar.setOnClickListener(v -> guardar());

        cargarTodo();
    }

    private void cargarTodo() {
        cargarMetricas();
        cargarUsuarios();
    }

    private void cargarMetricas() {
        api.dashboard().enqueue(new Callback<DashboardResponse>() {
            @Override public void onResponse(Call<DashboardResponse> c, Response<DashboardResponse> r) {
                DashboardResponse b = r.body();
                if (b == null || !b.isSuccess() || b.getMetricas() == null) return;
                StringBuilder sb = new StringBuilder();
                for (Map.Entry<String, Integer> e : b.getMetricas().entrySet()) {
                    sb.append(e.getKey()).append(": ").append(e.getValue()).append('\n');
                }
                tvMetricas.setText(sb.toString().trim());
            }
            @Override public void onFailure(Call<DashboardResponse> c, Throwable t) { /* silencioso */ }
        });
    }

    private void cargarUsuarios() {
        pb.setVisibility(View.VISIBLE);
        api.listarUsuarios().enqueue(new Callback<UsuariosListResponse>() {
            @Override public void onResponse(Call<UsuariosListResponse> c, Response<UsuariosListResponse> r) {
                pb.setVisibility(View.GONE);
                UsuariosListResponse b = r.body();
                if (b == null || !b.isSuccess()) {
                    Toast.makeText(AdminPanelActivity.this, "No se pudieron cargar los usuarios", Toast.LENGTH_SHORT).show();
                    return;
                }
                adapter.setAll(b.getUsuarios());
            }
            @Override public void onFailure(Call<UsuariosListResponse> c, Throwable t) {
                pb.setVisibility(View.GONE);
                Toast.makeText(AdminPanelActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void limpiarForm() {
        editandoUsuario = null;
        tvFormTitulo.setText(R.string.admin_crear_usuario);
        btnGuardar.setText(R.string.admin_guardar_usuario);
        spRol.setSelection(0);
        for (EditText et : new EditText[]{etUsuario, etDni, etContrasenia, etNombre, etApellidos,
                etEmail, etTelefono, etTitulacion, etPromocion, etCiudad, etTrabajo, etHobbies}) {
            et.setText("");
            et.setEnabled(true);
        }
    }

    private void guardar() {
        String usuario = etUsuario.getText().toString().trim();
        if (usuario.isEmpty() && editandoUsuario == null) {
            Toast.makeText(this, "El login (usuario) es obligatorio", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> p = new HashMap<>();
        p.put("usuario", editandoUsuario != null ? editandoUsuario : usuario);
        p.put("rol", ROLES[spRol.getSelectedItemPosition()]);
        meterSiHay(p, "dni", etDni);
        meterSiHay(p, "contrasenia", etContrasenia);
        meterSiHay(p, "nombre", etNombre);
        meterSiHay(p, "apellidos", etApellidos);
        meterSiHay(p, "email", etEmail);
        meterSiHay(p, "telefono", etTelefono);
        meterSiHay(p, "titulacion", etTitulacion);
        String prom = etPromocion.getText().toString().trim();
        if (!prom.isEmpty()) try { p.put("promocion", Integer.parseInt(prom)); } catch (NumberFormatException ignored) {}
        meterSiHay(p, "ciudad", etCiudad);
        p.put("campus", CAMPUS[spCampus.getSelectedItemPosition()]);
        p.put("facultad", FACULTADES[spFacultad.getSelectedItemPosition()]);

        // El backend usa la misma clave segun el rol: para alumni 'trabajoDescripcion',
        // para pdi 'areaTrabajo', para ptgas 'areaActual', y para admin/ptgas 'permisos/proyectos'.
        String trabajo = etTrabajo.getText().toString().trim();
        if (!trabajo.isEmpty()) {
            p.put("trabajoDescripcion", trabajo);
            p.put("areaTrabajo", trabajo);
            p.put("areaActual", trabajo);
        }
        String hobbies = etHobbies.getText().toString().trim();
        if (!hobbies.isEmpty()) {
            p.put("hobbies", hobbies);
            p.put("proyectos", hobbies);
            p.put("permisos", hobbies);
        }

        pb.setVisibility(View.VISIBLE);
        btnGuardar.setEnabled(false);
        // Capturamos datos para el SMS ANTES de enviar (limpiarForm() los vacia despues).
        // Solo se usaran si es una CREACION (no edicion) y la llamada al backend tiene exito.
        final boolean esCreacion = (editandoUsuario == null);
        final String telefonoNuevoUsuario = etTelefono.getText().toString().trim();
        final String usuarioNuevoUsuario  = usuario;
        Callback<UsuarioAdminResponse> cb = new Callback<UsuarioAdminResponse>() {
            @Override public void onResponse(Call<UsuarioAdminResponse> c, Response<UsuarioAdminResponse> r) {
                pb.setVisibility(View.GONE);
                btnGuardar.setEnabled(true);
                UsuarioAdminResponse b = r.body();
                if (b != null && b.isSuccess()) {
                    String msg = editandoUsuario != null ? "Usuario actualizado" : "Usuario creado";
                    if (b.getContrasenaTemporalGenerada() != null && b.getContrasenaTemporalGenerada())
                        msg += " (contrasena temporal generada)";
                    Toast.makeText(AdminPanelActivity.this, msg, Toast.LENGTH_SHORT).show();
                    limpiarForm();
                    cargarUsuarios();
                    // Intent IMPLICITO: solo cuando creamos un usuario nuevo y tenemos su telefono.
                    if (esCreacion) {
                        lanzarSmsAltaUsuario(telefonoNuevoUsuario, usuarioNuevoUsuario);
                    }
                } else {
                    String err = b != null && b.getError() != null ? b.getError() : "Error";
                    Toast.makeText(AdminPanelActivity.this, err, Toast.LENGTH_LONG).show();
                }
            }
            @Override public void onFailure(Call<UsuarioAdminResponse> c, Throwable t) {
                pb.setVisibility(View.GONE);
                btnGuardar.setEnabled(true);
                Toast.makeText(AdminPanelActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        };
        if (editandoUsuario != null) api.modificarUsuarioAdmin(p).enqueue(cb);
        else api.crearUsuarioAdmin(p).enqueue(cb);
    }

    private void meterSiHay(Map<String, Object> p, String key, EditText et) {
        String v = et.getText().toString().trim();
        if (!v.isEmpty()) p.put(key, v);
    }

    /**
     * Intent IMPLICITO (PDM): tras dar de alta a un usuario nuevo, lanza la app
     * de SMS predeterminada del dispositivo con el numero del destinatario y el
     * cuerpo del mensaje ya prerrellenados, para que el administrador confirme y
     * envie el aviso de bienvenida.
     *
     * Es intent IMPLICITO porque no especifica una clase destino: el sistema
     * decide que aplicacion atiende ACTION_SENDTO con URI "smsto:".
     * No se incluye contrasena en el mensaje (politica de seguridad).
     * No requiere permiso SEND_SMS porque NO enviamos el SMS desde la app;
     * delegamos en la app de mensajeria del usuario.
     */
    private void lanzarSmsAltaUsuario(String telefono, String usuario) {
        if (telefono == null || telefono.isEmpty()) {
            Toast.makeText(this,
                    "Usuario creado, pero no se ha podido avisar por SMS: no hay telefono.",
                    Toast.LENGTH_LONG).show();
            return;
        }
        // Mensaje sin datos sensibles (no contrasena).
        String mensaje = "Hola " + (usuario != null ? usuario : "") + ", "
                + "tu cuenta de Loyola Alumni ha sido dada de alta. "
                + "Ya puedes acceder a la app con tu usuario. "
                + "Bienvenido/a a la comunidad Alumni de la Universidad Loyola Andalucia.";

        Intent sms = new Intent(Intent.ACTION_SENDTO);
        sms.setData(Uri.parse("smsto:" + telefono));
        sms.putExtra("sms_body", mensaje);

        // resolveActivity evita un crash si el dispositivo (p.ej. emulador) no
        // tiene una app de SMS predeterminada que atienda este intent.
        if (sms.resolveActivity(getPackageManager()) != null) {
            startActivity(sms);
            Toast.makeText(this,
                    "Abriendo SMS para avisar al nuevo usuario.",
                    Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this,
                    "Usuario creado, pero este dispositivo no tiene app de SMS para enviar el aviso.",
                    Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onEditar(Usuario u) {
        editandoUsuario = u.getUsuario();
        tvFormTitulo.setText(R.string.admin_editar_usuario);
        btnGuardar.setText(R.string.admin_guardar_usuario);

        etUsuario.setText(u.getUsuario());
        etUsuario.setEnabled(false);   // no se puede cambiar el login
        etDni.setText(u.getDni() != null ? u.getDni() : "");
        etContrasenia.setText("");
        etNombre.setText(u.getNombre() != null ? u.getNombre() : "");
        etApellidos.setText(u.getApellidos() != null ? u.getApellidos() : "");
        etEmail.setText(u.getEmail() != null ? u.getEmail() : "");
        etTelefono.setText(u.getTelefono() != null ? u.getTelefono() : "");

        // Selecciona rol
        spRol.setSelection(indiceOZero(ROLES, u.getRol()));

        // Para alumni los datos vienen anidados en perfilAlumni; para otros roles en top-level
        AlumniResumen pa = u.getPerfilAlumni();
        if (pa != null) {
            etTitulacion.setText(pa.getTitulacion() != null ? pa.getTitulacion() : "");
            if (pa.getPromocion() != null && pa.getPromocion() > 0)
                etPromocion.setText(String.valueOf(pa.getPromocion()));
            else etPromocion.setText("");
            etCiudad.setText(pa.getCiudad() != null ? pa.getCiudad() : "");
            spCampus.setSelection(indiceOZero(CAMPUS, pa.getCampus()));
            spFacultad.setSelection(indiceOZero(FACULTADES, pa.getFacultad()));
            etTrabajo.setText(pa.getTrabajoActual() != null ? pa.getTrabajoActual() : "");
            etHobbies.setText(pa.getHobbies() != null ? pa.getHobbies() : "");
        } else {
            etTitulacion.setText(u.getTitulacion() != null ? u.getTitulacion() : "");
            etPromocion.setText("");
            etCiudad.setText("");
            spCampus.setSelection(indiceOZero(CAMPUS, u.getCampus()));
            spFacultad.setSelection(indiceOZero(FACULTADES, u.getFacultad()));
            String trabajo = u.getAreaTrabajo() != null ? u.getAreaTrabajo()
                    : (u.getAreaActual() != null ? u.getAreaActual() : "");
            etTrabajo.setText(trabajo);
            etHobbies.setText(u.getDepartamento() != null ? u.getDepartamento() : "");
        }

        // Scroll arriba (donde esta el form)
        findViewById(R.id.tvFormTitulo).requestFocus();
    }

    @Override
    public void onEliminar(Usuario u) {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar cuenta")
                .setMessage("¿Anonimizar la cuenta de " + u.getNombreCompleto() + "? "
                        + "Sus inscripciones se conservan pero los datos personales se borraran (RGPD).")
                .setPositiveButton("Anonimizar", (d, w) -> doEliminar(u))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void doEliminar(Usuario u) {
        Map<String, Object> body = new HashMap<>();
        body.put("usuario", u.getUsuario());
        pb.setVisibility(View.VISIBLE);
        api.eliminarUsuarioAdmin(body).enqueue(new Callback<GenericResponse>() {
            @Override public void onResponse(Call<GenericResponse> c, Response<GenericResponse> r) {
                pb.setVisibility(View.GONE);
                GenericResponse b = r.body();
                if (b != null && b.isSuccess()) {
                    Toast.makeText(AdminPanelActivity.this, "Cuenta anonimizada", Toast.LENGTH_SHORT).show();
                    cargarUsuarios();
                } else {
                    Toast.makeText(AdminPanelActivity.this,
                            b != null && b.getError() != null ? b.getError() : "Error",
                            Toast.LENGTH_LONG).show();
                }
            }
            @Override public void onFailure(Call<GenericResponse> c, Throwable t) {
                pb.setVisibility(View.GONE);
                Toast.makeText(AdminPanelActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private int indiceOZero(String[] arr, String v) {
        if (v == null) return 0;
        for (int i = 0; i < arr.length; i++) if (arr[i].equalsIgnoreCase(v)) return i;
        return 0;
    }
}
