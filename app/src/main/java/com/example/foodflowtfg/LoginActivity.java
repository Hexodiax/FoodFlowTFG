package com.example.foodflowtfg;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;


import com.example.foodflowtfg.RegisterActivity;
import com.google.android.material.button.MaterialButton;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private MaterialButton btnLogin;
    private TextView tvForgotPassword, tvSignUpAction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Inicializar vistas
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        //tvForgotPassword = findViewById(R.id.tvForgotPassword);
        tvSignUpAction = findViewById(R.id.tvSignUpAction);

        // Evento del botón Login
        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
            } else {
                    startActivity(new Intent(this, MainpageActivity.class));
            }

        });
        /**
         // Evento "Olvidé mi contraseña"
         tvForgotPassword.setOnClickListener(v -> {
         startActivity(new Intent(this, ForgotPasswordActivity.class));
         });
         **/
        // Evento "Registrarse"
        tvSignUpAction.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
        });
    }

    private void loginUser(String email, String password) {
        Toast.makeText(this, "Iniciando sesión...", Toast.LENGTH_SHORT).show();

        /**
         * // Ejemplo de redirección después del login:
         startActivity(new Intent(this, MainActivity.class));
         finish();
         **/
    }
}
