package com.example.foodflowtfg;

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
    }

    private void loadPlanData() {
        Bundle args = getArguments();
        if (args == null) return;

        String nombrePlan = args.getString("nombrePlan");
        String diaPlan = args.getString("diaPlan");

        if (nombrePlan != null && diaPlan != null) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

            String capitalizedDay = diaPlan.substring(0,1).toUpperCase() + diaPlan.substring(1).toLowerCase();
            tvDayOfWeek.setText(capitalizedDay);

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
                                processMeal(diaData.get("comida"), txtComida, imgComida, "🍝 Comida 🍝");
                                processMeal(diaData.get("cena"), txtCena, imgCena, "🥗 Cena 🥗");
                            }
                        }
                    })
                    .addOnFailureListener(e -> Log.e("TodayPlanFragment", "Error loading plan", e));
        }
    }

    private void processMeal(Object mealData, TextView textView, ImageView imageView, String prefix) {
        if (mealData instanceof Map) {
            Map<String, Object> meal = (Map<String, Object>) mealData;
            String nombre = (String) meal.get("nombre");
            String id = (String) meal.get("id");

            textView.setText(prefix +"\n" + (nombre != null ? nombre : "No asignada"));
            if (id != null && !id.isEmpty()) {
                loadRecipeImage(id, imageView);
            } else {
                imageView.setImageResource(R.drawable.recipe_error);
            }
        }
    }

    private void loadRecipeImage(String recipeId, ImageView imageView) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("recetas").document(recipeId).get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        String imageUrl = document.getString("imagenUrl");
                        if (imageUrl != null && !imageUrl.isEmpty()) {
                            loadImageWithGlide(imageUrl, imageView);
                        } else {
                            checkCustomRecipes(db, recipeId, imageView);
                        }
                    } else {
                        checkCustomRecipes(db, recipeId, imageView);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("TodayPlanFragment", "Error loading recipe", e);
                    checkCustomRecipes(db, recipeId, imageView);
                });
    }

    private void checkCustomRecipes(FirebaseFirestore db, String recipeId, ImageView imageView) {
        db.collection("recetas_personalizadas")
                .whereEqualTo("id", recipeId)
                .limit(1)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        String imageUrl = querySnapshot.getDocuments().get(0).getString("imagenUrl");
                        if (imageUrl != null && !imageUrl.isEmpty()) {
                            loadImageWithGlide(imageUrl, imageView);
                        } else {
                            imageView.setImageResource(R.drawable.recipe_error);
                        }
                    } else {
                        imageView.setImageResource(R.drawable.recipe_error);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("TodayPlanFragment", "Error loading custom recipe", e);
                    imageView.setImageResource(R.drawable.recipe_error);
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
}