package com.example.foodflowtfg;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Arrays;
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

        db.collection("recetas")  // O la colección que corresponda
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


        return view;



    }
}
