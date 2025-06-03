package com.example.foodflowtfg;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;


import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class YourRecipesFragment extends Fragment {

    private FirebaseFirestore db;
    private List<Recipe> listaRecipes;
    private RecipesGridAdapter adapter;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_your_recipes, container, false);
        GridView gridView = view.findViewById(R.id.gridViewRecipes);

        db = FirebaseFirestore.getInstance();
        listaRecipes = new ArrayList<>();
        adapter = new RecipesGridAdapter(requireContext(), listaRecipes);
        gridView.setAdapter(adapter);

        String userIdActual = FirebaseAuth.getInstance().getCurrentUser().getUid(); // 🔑 Usuario actual

        db.collection("recetas_personalizadas")
                .whereEqualTo("userId", userIdActual) // ✅ FILTRO POR USUARIO
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        listaRecipes.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Recipe recipe = document.toObject(Recipe.class);
                            listaRecipes.add(recipe);
                        }
                        adapter.notifyDataSetChanged();
                    } else {
                        Log.e("Firestore", "Error al cargar recetas", task.getException());
                    }
                });

        gridView.setOnItemClickListener((parent, view1, position, id) -> {
            Recipe recipeSeleccionada = listaRecipes.get(position);

            Intent intent = new Intent(requireContext(), RecipeDetailActivity.class);
            intent.putExtra("name", recipeSeleccionada.getNombre());
            intent.putExtra("ingredients", recipeSeleccionada.getIngredientes());
            intent.putExtra("steps", recipeSeleccionada.getPasos());
            intent.putExtra("imageUrl", recipeSeleccionada.getImagenUrl());
            startActivity(intent);
        });

        FloatingActionButton fab = view.findViewById(R.id.fab);
        fab.setOnClickListener(view1 -> {
            Intent intent = new Intent(getActivity(), AddCustomRecipe.class);
            startActivity(intent);
        });

        return view;
    }

}
