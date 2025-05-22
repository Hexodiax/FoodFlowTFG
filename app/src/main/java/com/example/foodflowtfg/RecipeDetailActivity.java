package com.example.foodflowtfg;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

public class RecipeDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);

        ImageView imageRecipe = findViewById(R.id.imageRecipe);
        TextView textName = findViewById(R.id.textName);
        TextView textIngredients = findViewById(R.id.textIngredients);
        TextView textSteps = findViewById(R.id.textSteps);

        Intent intent = getIntent();
        if (intent != null) {
            String name = intent.getStringExtra("name");
            String ingredients = intent.getStringExtra("ingredients");
            String steps = intent.getStringExtra("steps");
            String imageUrl = intent.getStringExtra("imageUrl");

            textName.setText(name);
            textIngredients.setText("Ingredientes:\n" + ingredients);
            textSteps.setText("Pasos:\n" + steps);

            Glide.with(this).load(imageUrl).into(imageRecipe);
        }
    }
}
