package com.example.foodflowtfg;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.Toast;

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

        cargarRecetasUsuario();

        gridView.setOnItemClickListener((parent, view1, position, id) -> {
            abrirDetalleReceta(position);
        });

        gridView.setOnItemLongClickListener((parent, view12, position, id) -> {
            mostrarMenuOpciones(position);
            return true; // Evento consumido
        });

        FloatingActionButton fab = view.findViewById(R.id.fab);
        fab.setOnClickListener(view1 -> {
            abrirAgregarReceta();
        });

        return view;
    }

    private void cargarRecetasUsuario() {
        String userIdActual = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db.collection("recetas_personalizadas")
                .whereEqualTo("userId", userIdActual)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        listaRecipes.clear();
                        recipeIds.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Recipe recipe = document.toObject(Recipe.class);
                            recipe.setId(document.getId()); // Asegurar que el ID está establecido
                            listaRecipes.add(recipe);
                            recipeIds.add(document.getId());
                        }
                        adapter.notifyDataSetChanged();
                    } else {
                        Log.e("Firestore", "Error al cargar recetas", task.getException());
                        Toast.makeText(requireContext(), "Error al cargar recetas", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void abrirDetalleReceta(int position) {
        Recipe recipeSeleccionada = listaRecipes.get(position);
        Intent intent = new Intent(requireContext(), RecipeDetailActivity.class);
        intent.putExtra("name", recipeSeleccionada.getNombre());
        intent.putExtra("ingredients", recipeSeleccionada.getIngredientes());
        intent.putExtra("steps", recipeSeleccionada.getPasos());
        intent.putExtra("imageUrl", recipeSeleccionada.getImagenUrl());
        startActivity(intent);
    }

    private void mostrarMenuOpciones(int position) {
        CharSequence[] options = {"Editar Receta", "Eliminar Receta", "Cancelar"};

        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Opciones")
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0: // Editar
                            editarReceta(position);
                            break;
                        case 1: // Eliminar
                            confirmarEliminacion(position);
                            break;
                        case 2: // Cancelar
                            dialog.dismiss();
                            break;
                    }
                })
                .show();
    }

    private void editarReceta(int position) {
        Recipe recipeSeleccionada = listaRecipes.get(position);
        String docId = recipeIds.get(position);

        Intent intent = new Intent(requireContext(), EditCustomRecipeActivity.class);
        intent.putExtra("recipeId", docId);
        intent.putExtra("nombre", recipeSeleccionada.getNombre());
        intent.putExtra("ingredientes", recipeSeleccionada.getIngredientes());
        intent.putExtra("pasos", recipeSeleccionada.getPasos());
        intent.putExtra("imagenUrl", recipeSeleccionada.getImagenUrl());
        startActivity(intent);
    }

    private void confirmarEliminacion(int position) {
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Confirmar eliminación")
                .setMessage("¿Estás seguro de que deseas eliminar esta receta?")
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    eliminarReceta(position);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void eliminarReceta(int position) {
        String docId = recipeIds.get(position);
        db.collection("recetas_personalizadas").document(docId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    listaRecipes.remove(position);
                    recipeIds.remove(position);
                    adapter.notifyDataSetChanged();
                    Toast.makeText(requireContext(), "Receta eliminada", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error al borrar receta", e);
                    Toast.makeText(requireContext(), "Error al eliminar receta", Toast.LENGTH_SHORT).show();
                });
    }

    private void abrirAgregarReceta() {
        Intent intent = new Intent(getActivity(), AddCustomRecipe.class);
        startActivity(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Actualizar la lista cuando el fragmento vuelva a estar visible
        cargarRecetasUsuario();
    }
}