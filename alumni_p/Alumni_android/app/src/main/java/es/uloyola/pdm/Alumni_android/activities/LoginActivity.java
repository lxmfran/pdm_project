package es.uloyola.pdm.Alumni_android.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import es.uloyola.pdm.Alumni_android.R;
import es.uloyola.pdm.Alumni_android.conexionServer.PvoService;
import es.uloyola.pdm.Alumni_android.conexionServer.RetrofitClient;
import es.uloyola.pdm.Alumni_android.model.LoginRequest;
import es.uloyola.pdm.Alumni_android.model.LoginResponse;
import es.uloyola.pdm.Alumni_android.session.SessionManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * LoginActivity
 * -------------
 * Pantalla de inicio de la app. El usuario introduce sus credenciales y la
 * actividad invoca POST /LoginServlet a traves de Retrofit.
 *
 * Si el backend responde {"success": true, ...}:
 *   - Se guarda el usuario en SessionManager.
 *   - La cookie JSESSIONID queda en AlumniCookieJar y se reenviara
 *     automaticamente en las llamadas siguientes.
 *   - Se navega a HomeActivity (no se permite volver atras al login).
 *
 * Si responde con error logico o de red, se muestra un Toast.
 *
 * Los cuatro botones inferiores rellenan los campos con los usuarios de
 * prueba (mgarcia / afernandez / lmarin / phidalgo) y disparan el login.
 */
public class LoginActivity extends AppCompatActivity {

    private EditText etUsuario;
    private EditText etContrasenia;
    private Button   btnEntrar;
    private PvoService api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etUsuario     = findViewById(R.id.etUsuario);
        etContrasenia = findViewById(R.id.etContrasenia);
        btnEntrar     = findViewById(R.id.btnEntrar);

        // Instancia unica del servicio Retrofit
        api = RetrofitClient.getService();

        btnEntrar.setOnClickListener(v -> intentarLogin());

        // Botones de acceso rapido a los usuarios de prueba
        configurarAccesoRapido(R.id.btnDemoAlumni, "mgarcia",    "Loyola2026!");
        configurarAccesoRapido(R.id.btnDemoPdi,    "afernandez", "Loyola2026!");
        configurarAccesoRapido(R.id.btnDemoPtgas,  "lmarin",     "Loyola2026!");
        configurarAccesoRapido(R.id.btnDemoAdmin,  "phidalgo",   "Loyola2026!");
    }

    /** Rellena usuario/contrasena y dispara el login para los botones de demo. */
    private void configurarAccesoRapido(int botonId, String usuario, String contrasenia) {
        Button btn = findViewById(botonId);
        btn.setOnClickListener(v -> {
            etUsuario.setText(usuario);
            etContrasenia.setText(contrasenia);
            intentarLogin();
        });
    }

    /** Validacion previa + llamada asincrona a POST /LoginServlet. */
    private void intentarLogin() {
        String usuario     = etUsuario.getText().toString().trim();
        String contrasenia = etContrasenia.getText().toString();

        if (TextUtils.isEmpty(usuario) || TextUtils.isEmpty(contrasenia)) {
            Toast.makeText(this, R.string.login_credenciales_obligatorias, Toast.LENGTH_SHORT).show();
            return;
        }

        btnEntrar.setEnabled(false);

        api.login(new LoginRequest(usuario, contrasenia)).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                btnEntrar.setEnabled(true);

                LoginResponse body = response.body();
                if (response.isSuccessful() && body != null && body.isSuccess()) {
                    // Sesion iniciada correctamente.
                    SessionManager.get().iniciarSesion(body.getUsuario(), body.getRol());

                    Intent i = new Intent(LoginActivity.this, HomeActivity.class);
                    // Cierra esta pantalla para que "atras" no la vuelva a mostrar
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(i);
                    finish();
                } else {
                    String mensaje = body != null && body.getError() != null
                            ? body.getError()
                            : "Credenciales incorrectas o cuenta no activa.";
                    Toast.makeText(LoginActivity.this, mensaje, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                btnEntrar.setEnabled(true);
                String mensaje = getString(R.string.login_error_conexion, t.getMessage());
                Toast.makeText(LoginActivity.this, mensaje, Toast.LENGTH_LONG).show();
            }
        });
    }
}
