package com.example.foodflowtfg;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.card.MaterialCardView;

public class MainpageActivity extends AppCompatActivity {

    MaterialCardView cardPlanning, cardRecetas, cardCocineroIA, cardSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mainpage);

        cardPlanning = findViewById(R.id.cardPlanning);
        cardRecetas = findViewById(R.id.cardRecetas);
        cardCocineroIA = findViewById(R.id.cardCocineroIA);
        cardSettings = findViewById(R.id.cardSettings);

        cardPlanning.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Intent intent = new Intent(MainpageActivity.this, PlanningActivity.class);
                //startActivity(intent);
            }
        });

        cardRecetas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Intent intent = new Intent(MainpageActivity.this, RecetasActivity.class);
                //startActivity(intent);
            }
        });

        cardCocineroIA.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Intent intent = new Intent(MainpageActivity.this, CocineroIAActivity.class);
                //startActivity(intent);
            }
        });

        cardSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Intent intent = new Intent(MainpageActivity.this, AjustesActivity.class);
                //startActivity(intent);
            }
        });
    }
}