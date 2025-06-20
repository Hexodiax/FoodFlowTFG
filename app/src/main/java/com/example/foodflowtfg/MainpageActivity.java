package com.example.foodflowtfg;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.card.MaterialCardView;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainpageActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 100;

    private MaterialCardView cardPlanning, cardRecetas, cardCocineroIA, cardSettings;
    private ImageView btnLogout;
    private TextView tvTitle;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseAnalytics analytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        NotificationHelper.createNotificationChannel(this);
        setContentView(R.layout.activity_mainpage);

        // Solicitar permiso de notificaciones si es necesario
        requestNotificationPermission();

        // Inicializar Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        analytics = FirebaseAnalytics.getInstance(this);

        // Referencias UI
        tvTitle = findViewById(R.id.tvTitle);
        cardPlanning = findViewById(R.id.cardPlanning);
        cardRecetas = findViewById(R.id.cardRecetas);
        cardCocineroIA = findViewById(R.id.cardCocineroIA);
        cardSettings = findViewById(R.id.cardSettings);
        btnLogout = findViewById(R.id.btnLogout);

        // Cargar datos del usuario
        loadUserData();

        // Configurar listeners
        setupCardListeners();
        setupLogoutButton();

        // Analytics Event
        Bundle bundle = new Bundle();
        bundle.putString("message", "Pantalla principal cargada");
        analytics.logEvent("MainScreenLoaded", bundle);
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        PERMISSION_REQUEST_CODE
                );
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("MainpageActivity", "Las notificaciones activadas");
            } else {
                // Permiso denegado
                Log.d("MainpageActivity", "Las notificaciones estarán desactivadas");
            }
        }
    }

    private void loadUserData() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            db.collection("users").document(currentUser.getUid())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String userName = documentSnapshot.getString("name");
                            String fullText = "Bienvenido, " + userName + " 👨‍🍳";
                            SpannableString spannable = new SpannableString(fullText);
                            //Poner el userName en rojo
                            int start = fullText.indexOf(userName);
                            int end = start + userName.length();
                            int primaryColor = ContextCompat.getColor(this, R.color.primary_color);
                            spannable.setSpan(new ForegroundColorSpan(primaryColor), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                            tvTitle.setText(spannable);
                        }
                    })
                    .addOnFailureListener(e -> {
                        tvTitle.setText("Bienvenido, chef 👨‍🍳");
                        Log.d("MainpageActivity", "Error al cargar datos del usuario: " + e.getMessage());
                    });
        }
    }

    private void setupCardListeners() {
        cardPlanning.setOnClickListener(v -> {
            startActivity(new Intent(MainpageActivity.this, PlanningActivity.class));
            logEvent("planning_clicked", "User accessed Planning");
        });

        cardRecetas.setOnClickListener(v -> {
            startActivity(new Intent(MainpageActivity.this, RecipesActivity.class));
            logEvent("recipes_clicked", "User accessed Recipes");
        });

        cardCocineroIA.setOnClickListener(v -> {
            startActivity(new Intent(MainpageActivity.this, ChefAIActivity.class));
            logEvent("chef_ai_clicked", "User accessed Chef AI");
        });

        cardSettings.setOnClickListener(v -> {
            startActivity(new Intent(MainpageActivity.this, InfoActivity.class));
            logEvent("settings_clicked", "User accessed Settings");
        });
    }

    private void setupLogoutButton() {
        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            Log.d("MainpageActivity", "Sesión cerrada correctamente");
            Intent intent = new Intent(MainpageActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
            logEvent("logout", "User logged out");
        });
    }

    private void logEvent(String eventId, String message) {
        Bundle params = new Bundle();
        params.putString("event_details", message);
        analytics.logEvent(eventId, params);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mAuth.getCurrentUser() == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
    }
}
