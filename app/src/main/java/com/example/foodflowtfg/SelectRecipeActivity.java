package com.example.foodflowtfg;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.GridView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class SelectRecipeActivity extends AppCompatActivity {

    private List<Receta> listaRecetas = new ArrayList<>();
    private RecipesGridAdapter adapter;
    private String dia, tipo;

    private boolean cargadoPersonalizadas = false;
    private boolean cargadoGenerales = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_recipe);

        dia = getIntent().getStringExtra("dia");
        tipo = getIntent().getStringExtra("tipo");

        GridView gridView = findViewById(R.id.gridViewRecipesSelect);
        adapter = new RecipesGridAdapter(this, listaRecetas);
        gridView.setAdapter(adapter);

        String userIdActual = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseFirestore.getInstance().collection("recetas_personalizadas")
                .whereEqualTo("userId", userIdActual)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Receta receta = document.toObject(Receta.class);
                            receta.setId(document.getId());
                            listaRecetas.add(receta);
                        }
                    } else {
                        Log.e("Firestore", "Error al cargar recetas personalizadas", task.getException());
                    }
                    cargadoPersonalizadas = true;
                    actualizarAdaptadorSiListo();
                });

        FirebaseFirestore.getInstance().collection("recetas")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Receta receta = document.toObject(Receta.class);
                            receta.setId(document.getId());
                            listaRecetas.add(receta);
                        }
                    } else {
                        Log.e("Firestore", "Error al cargar recetas generales", task.getException());
                    }
                    cargadoGenerales = true;
                    actualizarAdaptadorSiListo();
                });

        gridView.setOnItemClickListener((parent, view, position, id) -> {
            Receta receta = listaRecetas.get(position);
            Intent resultIntent = new Intent();
            resultIntent.putExtra("nombreReceta", receta.getNombre());
            resultIntent.putExtra("idReceta", receta.getId()); // ✅ Enviamos ID
            resultIntent.putExtra("dia", dia);
            resultIntent.putExtra("tipo", tipo);
            setResult(RESULT_OK, resultIntent);
            finish();
        });
    }

    private void actualizarAdaptadorSiListo() {
        if (cargadoPersonalizadas && cargadoGenerales) {
            adapter.notifyDataSetChanged();
        }
    }
}
