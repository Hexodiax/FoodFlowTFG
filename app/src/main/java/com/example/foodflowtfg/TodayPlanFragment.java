package com.example.foodflowtfg;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Map;

public class TodayPlanFragment extends Fragment {

    private TextView txtComida, txtCena, tvDayOfWeek;
    private ImageView imgComida, imgCena;

    // Variables para guardar las recetas completas al cargar
    private Recipe currentComidaRecipe = null;
    private Recipe currentCenaRecipe = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_today_plan, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initializeViews(view);
        resetViews();
        loadPlanData();

        // Listener para abrir detalle comida
        imgComida.setOnClickListener(v -> {
            if (currentComidaRecipe != null) {
                openRecipeDetail(currentComidaRecipe);
            }
        });

        // Listener para abrir detalle cena
        imgCena.setOnClickListener(v -> {
            if (currentCenaRecipe != null) {
                openRecipeDetail(currentCenaRecipe);
            }
        });
    }

    private void initializeViews(View view) {
        txtComida = view.findViewById(R.id.txtComida);
        txtCena = view.findViewById(R.id.txtCena);
        imgComida = view.findViewById(R.id.imgComida);
        imgCena = view.findViewById(R.id.imgCena);
        tvDayOfWeek = view.findViewById(R.id.tvDayOfWeek);
    }

    private void resetViews() {
        txtComida.setText("🍝 Comida 🍝");
        txtCena.setText("🥗 Cena 🥗");
        imgComida.setImageResource(R.drawable.recipe_placeholder);
        imgCena.setImageResource(R.drawable.recipe_placeholder);
        currentComidaRecipe = null;
        currentCenaRecipe = null;
    }

    private void loadPlanData() {
        Bundle args = getArguments();
        if (args == null) return;

        String nombrePlan = args.getString("nombrePlan");
        String diaPlan = args.getString("diaPlan");
        if (diaPlan != null) {
            tvDayOfWeek.setText(diaPlan.toUpperCase());
        }
        if (nombrePlan != null && diaPlan != null) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

            tvDayOfWeek.setText(diaPlan.toUpperCase());

            db.collection("plannings")
                    .whereEqualTo("userId", userId)
                    .whereEqualTo("nombre", nombrePlan)
                    .limit(1)
                    .get()
                    .addOnSuccessListener(querySnapshot -> {
                        if (!querySnapshot.isEmpty()) {
                            DocumentSnapshot plan = querySnapshot.getDocuments().get(0);
                            Map<String, Object> diaData = (Map<String, Object>) plan.get(diaPlan);
                            if (diaData != null) {
                                processMeal(diaData.get("comida"), txtComida, imgComida, "🍝 Comida 🍝", true);
                                processMeal(diaData.get("cena"), txtCena, imgCena, "🥗 Cena 🥗", false);
                            }
                        }
                    })
                    .addOnFailureListener(e -> Log.e("TodayPlanFragment", "Error loading plan", e));
        }
    }

    private void processMeal(Object mealData, TextView textView, ImageView imageView, String prefix, boolean isComida) {
        if (mealData instanceof Map) {
            Map<String, Object> meal = (Map<String, Object>) mealData;
            String nombre = (String) meal.get("nombre");
            String id = (String) meal.get("id");

            textView.setText(prefix + "\n" + (nombre != null ? nombre : "No asignada"));
            if (id != null && !id.isEmpty()) {
                loadFullRecipe(id, imageView, isComida);
            } else {
                imageView.setImageResource(R.drawable.recipe_error);
                if (isComida) currentComidaRecipe = null;
                else currentCenaRecipe = null;
            }
        }
    }

    private void loadFullRecipe(String recipeId, ImageView imageView, boolean isComida) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("recetas").document(recipeId).get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        Recipe recipe = document.toObject(Recipe.class);
                        if (recipe != null) {
                            if (isComida) currentComidaRecipe = recipe;
                            else currentCenaRecipe = recipe;

                            if (recipe.getImagenUrl() != null && !recipe.getImagenUrl().isEmpty()) {
                                loadImageWithGlide(recipe.getImagenUrl(), imageView);
                            } else {
                                checkCustomRecipes(db, recipeId, imageView, isComida);
                            }
                        } else {
                            checkCustomRecipes(db, recipeId, imageView, isComida);
                        }
                    } else {
                        checkCustomRecipes(db, recipeId, imageView, isComida);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("TodayPlanFragment", "Error loading recipe", e);
                    checkCustomRecipes(db, recipeId, imageView, isComida);
                });
    }

    private void checkCustomRecipes(FirebaseFirestore db, String recipeId, ImageView imageView, boolean isComida) {
        db.collection("recetas_personalizadas")
                .whereEqualTo("id", recipeId)
                .limit(1)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        Recipe recipe = querySnapshot.getDocuments().get(0).toObject(Recipe.class);
                        if (recipe != null) {
                            if (isComida) currentComidaRecipe = recipe;
                            else currentCenaRecipe = recipe;

                            if (recipe.getImagenUrl() != null && !recipe.getImagenUrl().isEmpty()) {
                                loadImageWithGlide(recipe.getImagenUrl(), imageView);
                            } else {
                                imageView.setImageResource(R.drawable.recipe_error);
                            }
                        } else {
                            imageView.setImageResource(R.drawable.recipe_error);
                            if (isComida) currentComidaRecipe = null;
                            else currentCenaRecipe = null;
                        }
                    } else {
                        imageView.setImageResource(R.drawable.recipe_error);
                        if (isComida) currentComidaRecipe = null;
                        else currentCenaRecipe = null;
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("TodayPlanFragment", "Error loading custom recipe", e);
                    imageView.setImageResource(R.drawable.recipe_error);
                    if (isComida) currentComidaRecipe = null;
                    else currentCenaRecipe = null;
                });
    }

    private void loadImageWithGlide(String imageUrl, ImageView imageView) {
        Glide.with(this)
                .load(imageUrl)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.drawable.recipe_placeholder)
                .error(R.drawable.recipe_error)
                .into(imageView);
    }

    private void openRecipeDetail(Recipe recipe) {
        if (recipe == null) return;

        Intent intent = new Intent(requireContext(), RecipeDetailActivity.class);
        intent.putExtra("name", recipe.getNombre());
        intent.putExtra("ingredients", recipe.getIngredientes());
        intent.putExtra("steps", recipe.getPasos());
        intent.putExtra("imageUrl", recipe.getImagenUrl());
        startActivity(intent);
    }
}
