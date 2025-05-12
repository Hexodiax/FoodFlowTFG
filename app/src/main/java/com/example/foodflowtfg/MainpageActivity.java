package com.example.foodflowtfg;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.card.MaterialCardView;
import com.google.firebase.analytics.FirebaseAnalytics;

public class MainpageActivity extends AppCompatActivity {

    MaterialCardView cardPlanning, cardRecetas, cardCocineroIA, cardSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mainpage);

        // Analytics Event
        FirebaseAnalytics analytics = FirebaseAnalytics.getInstance(this);
        Bundle bundle = new Bundle();
        bundle.putString("message", "Integración de Firebase completa");
        analytics.logEvent("InitScreen", bundle);

        cardPlanning = findViewById(R.id.cardPlanning);
        cardRecetas = findViewById(R.id.cardRecetas);
        cardCocineroIA = findViewById(R.id.cardCocineroIA);
        cardSettings = findViewById(R.id.cardSettings);

        cardPlanning.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainpageActivity.this, PlanningActivity.class);
                startActivity(intent);
            }
        });

        cardRecetas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainpageActivity.this, RecipesActivity.class);
                startActivity(intent);
            }
        });

        cardCocineroIA.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainpageActivity.this, ChefAIActivity.class);
                startActivity(intent);
            }
        });

        cardSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainpageActivity.this, SettingsActivity.class);
                startActivity(intent);
            }
        });
    }
}