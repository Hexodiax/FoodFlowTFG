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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class YourRecipesFragment extends Fragment {

    private FirebaseFirestore db;
    private List<Receta> listaRecetas;
    private RecipesGridAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_your_recipes, container, false);
        GridView gridView = view.findViewById(R.id.gridViewRecipes);

        db = FirebaseFirestore.getInstance();
        listaRecetas = new ArrayList<>();
        adapter = new RecipesGridAdapter(requireContext(), listaRecetas);
        gridView.setAdapter(adapter);

        db.collection("recetas_personalizadas")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        listaRecetas.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Receta receta = document.toObject(Receta.class);
                            listaRecetas.add(receta);
                        }
                        adapter.notifyDataSetChanged();
                    } else {
                        Log.e("Firestore", "Error al cargar recetas", task.getException());
                    }
                });
        gridView.setOnItemClickListener((parent, view1, position, id) -> {
            Receta recetaSeleccionada = listaRecetas.get(position);

            Intent intent = new Intent(requireContext(), RecipeDetailActivity.class);
            intent.putExtra("name", recetaSeleccionada.getNombre());
            intent.putExtra("ingredients", recetaSeleccionada.getIngredientes());
            intent.putExtra("steps", recetaSeleccionada.getPasos());
            intent.putExtra("imageUrl", recetaSeleccionada.getImagenUrl());
            startActivity(intent);
        });

        FloatingActionButton fab = view.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), AddCustomRecipe.class);
                startActivity(intent);
            }
        });

        return view;
    }


}
