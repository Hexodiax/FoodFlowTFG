package com.example.foodflowtfg;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class EditPlanningActivity extends AppCompatActivity {
    private static final String[] dias = {"Lunes", "Martes", "Miercoles", "Jueves", "Viernes", "Sabado", "Domingo"};
    private static final int SELECT_RECIPE_REQUEST = 100;

    private String planningId;
    private EditText editTextPlanningName;
    private final Map<String, Map<String, Map<String, String>>> planningMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_planning);

        editTextPlanningName = findViewById(R.id.editTextPlanningName);
        Button btnGuardar = findViewById(R.id.btnGuardarPlanning);

        planningId = getIntent().getStringExtra("planningId");
        String planningName = getIntent().getStringExtra("planningName");
        editTextPlanningName.setText(planningName);

        // Inicializar estructura de datos
        for (String dia : dias) {
            planningMap.put(dia, new HashMap<>());

            // Cargar datos existentes
            String comidaNombre = getIntent().getStringExtra(dia + "_comida");
            String comidaId = getIntent().getStringExtra(dia + "_comida_id");
            String cenaNombre = getIntent().getStringExtra(dia + "_cena");
            String cenaId = getIntent().getStringExtra(dia + "_cena_id");

            if (comidaNombre != null && comidaId != null) {
                Map<String, String> comidaData = new HashMap<>();
                comidaData.put("nombre", comidaNombre);
                comidaData.put("id", comidaId);
                planningMap.get(dia).put("comida", comidaData);

                TextView comidaView = findViewById(getResources().getIdentifier("comida_" + dia, "id", getPackageName()));
                comidaView.setText(comidaNombre);
            }

            if (cenaNombre != null && cenaId != null) {
                Map<String, String> cenaData = new HashMap<>();
                cenaData.put("nombre", cenaNombre);
                cenaData.put("id", cenaId);
                planningMap.get(dia).put("cena", cenaData);

                TextView cenaView = findViewById(getResources().getIdentifier("cena_" + dia, "id", getPackageName()));
                cenaView.setText(cenaNombre);
            }

            // Configurar listeners
            Button btnComida = findViewById(getResources().getIdentifier("btn_comida_" + dia, "id", getPackageName()));
            Button btnCena = findViewById(getResources().getIdentifier("btn_cena_" + dia, "id", getPackageName()));

            if (btnComida != null) {
                btnComida.setOnClickListener(v -> abrirSelectorRecetas(dia, "comida"));
            }

            if (btnCena != null) {
                btnCena.setOnClickListener(v -> abrirSelectorRecetas(dia, "cena"));
            }
        }

        btnGuardar.setOnClickListener(v -> guardarCambios());
    }

    private void abrirSelectorRecetas(String dia, String tipo) {
        Intent intent = new Intent(this, SelectRecipeActivity.class);
        intent.putExtra("dia", dia);
        intent.putExtra("tipo", tipo);
        startActivityForResult(intent, SELECT_RECIPE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SELECT_RECIPE_REQUEST && resultCode == RESULT_OK && data != null) {
            String dia = data.getStringExtra("dia");
            String tipo = data.getStringExtra("tipo");
            String nombreReceta = data.getStringExtra("nombreReceta");
            String idReceta = data.getStringExtra("idReceta");

            // Actualizar vista
            TextView view = findViewById(getResources().getIdentifier(tipo + "_" + dia, "id", getPackageName()));
            view.setText(nombreReceta);

            // Actualizar estructura de datos
            Map<String, String> recetaData = new HashMap<>();
            recetaData.put("nombre", nombreReceta);
            recetaData.put("id", idReceta);

            planningMap.get(dia).put(tipo, recetaData);
        }
    }

    private void guardarCambios() {
        String nuevoNombre = editTextPlanningName.getText().toString().trim();
        if (nuevoNombre.isEmpty()) {
            Toast.makeText(this, "Por favor ingresa un nombre", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("nombre", nuevoNombre);

        // Agregar todos los días actualizados
        for (String dia : dias) {
            updates.put(dia, planningMap.get(dia));
        }

        FirebaseFirestore.getInstance()
                .collection("plannings")
                .document(planningId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Log.d("PlanningActivity", "Planning actualizado");
                    setResult(RESULT_OK);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Log.e("PlanningActivity", "Error al actualizar: " + e.getMessage(), e);
                });
    }
}