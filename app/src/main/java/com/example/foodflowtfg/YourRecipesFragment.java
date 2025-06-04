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
    private List<String> recipeIds; // Lista para IDs de documentos
    private RecipesGridAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_your_recipes, container, false);
        GridView gridView = view.findViewById(R.id.gridViewRecipes);

        db = FirebaseFirestore.getInstance();
        listaRecipes = new ArrayList<>();
        recipeIds = new ArrayList<>();
        adapter = new RecipesGridAdapter(requireContext(), listaRecipes);
        gridView.setAdapter(adapter);

        String userIdActual = FirebaseAuth.getInstance().getCurrentUser().getUid(); // Usuario actual

        db.collection("recetas_personalizadas")
                .whereEqualTo("userId", userIdActual) // Filtrar por usuario
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        listaRecipes.clear();
                        recipeIds.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Recipe recipe = document.toObject(Recipe.class);
                            listaRecipes.add(recipe);
                            recipeIds.add(document.getId()); // Guardamos el ID del documento
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

        // Listener para borrar con pulsación larga
        gridView.setOnItemLongClickListener((parent, view12, position, id) -> {
            new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                    .setTitle("Borrar receta")
                    .setMessage("¿Quieres borrar esta receta?")
                    .setPositiveButton("Sí", (dialog, which) -> {
                        String docId = recipeIds.get(position);
                        db.collection("recetas_personalizadas").document(docId)
                                .delete()
                                .addOnSuccessListener(aVoid -> {
                                    listaRecipes.remove(position);
                                    recipeIds.remove(position);
                                    adapter.notifyDataSetChanged();
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("Firestore", "Error al borrar receta", e);
                                });
                    })
                    .setNegativeButton("No", null)
                    .show();
            return true; // Evento consumido
        });

        FloatingActionButton fab = view.findViewById(R.id.fab);
        fab.setOnClickListener(view1 -> {
            Intent intent = new Intent(getActivity(), AddCustomRecipe.class);
            startActivity(intent);
        });

        return view;
    }

}
