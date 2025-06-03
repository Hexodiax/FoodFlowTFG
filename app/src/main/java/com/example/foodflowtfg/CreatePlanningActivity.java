package com.example.foodflowtfg;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class CreatePlanningActivity extends AppCompatActivity {


    private final Map<String, Map<String, Map<String, String>>> planningMap = new HashMap<>();
    private final String[] dias = {"Lunes", "Martes", "Miercoles", "Jueves", "Viernes", "Sabado", "Domingo"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_planning);

        for (String dia : dias) {
            int comidaId = getResources().getIdentifier("comida_" + dia, "id", getPackageName());
            int cenaId = getResources().getIdentifier("cena_" + dia, "id", getPackageName());

            TextView comida = findViewById(comidaId);
            TextView cena = findViewById(cenaId);

            if (comida != null) {
                comida.setOnClickListener(view -> abrirSelectorRecetas(dia, "comida"));
            } else {
                Log.e("CreatePlanningActivity", "comida_" + dia + " es null");
            }

            if (cena != null) {
                cena.setOnClickListener(view -> abrirSelectorRecetas(dia, "cena"));
            } else {
                Log.e("CreatePlanningActivity", "cena_" + dia + " es null");
            }

            planningMap.put(dia, new HashMap<>());
        }

        EditText editTextPlanningName = findViewById(R.id.editTextPlanningName);
        Button guardarBtn = findViewById(R.id.btnGuardarPlanning);

        guardarBtn.setOnClickListener(v -> {
            String nombrePlanning = editTextPlanningName.getText().toString().trim();
            if (nombrePlanning.isEmpty()) {
                Toast.makeText(this, "Por favor ingresa un nombre para el planning", Toast.LENGTH_SHORT).show();
                return;
            }
            guardarPlanning(nombrePlanning);
        });
    }

    private void abrirSelectorRecetas(String dia, String tipo) {
        Intent intent = new Intent(this, SelectRecipeActivity.class);
        intent.putExtra("dia", dia);
        intent.putExtra("tipo", tipo);
        startActivityForResult(intent, 100);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK && data != null) {
            String dia = data.getStringExtra("dia");
            String tipo = data.getStringExtra("tipo");
            String nombreReceta = data.getStringExtra("nombreReceta");
            String idReceta = data.getStringExtra("idReceta");

            String idView = tipo + "_" + dia;
            TextView view = findViewById(getResources().getIdentifier(idView, "id", getPackageName()));
            view.setText(nombreReceta);

            if (!planningMap.containsKey(dia)) {
                planningMap.put(dia, new HashMap<>());
            }

            Map<String, Map<String, String>> tipoMap = planningMap.get(dia);
            Map<String, String> recetaData = new HashMap<>();
            recetaData.put("nombre", nombreReceta);
            recetaData.put("id", idReceta);

            tipoMap.put(tipo, recetaData);
        }
    }

    private void guardarPlanning(String nombrePlanning) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String, Object> planningData = new HashMap<>();
        planningData.put("userId", userId);
        planningData.put("nombre", nombrePlanning);
        planningData.put("diasCompletados", new HashMap<String, Boolean>());

        for (String dia : dias) {
            Map<String, Object> comidasDia = new HashMap<>();
            Map<String, Map<String, String>> tipoMap = planningMap.get(dia);

            if (tipoMap != null) {
                for (String tipo : tipoMap.keySet()) {
                    Map<String, String> recetaData = tipoMap.get(tipo);
                    if (recetaData != null) {
                        comidasDia.put(tipo, recetaData);
                    }
                }
            }

            planningData.put(dia, comidasDia);
        }

        db.collection("plannings")
                .add(planningData)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Planning guardado", Toast.LENGTH_SHORT).show();

                    generateMotivationalMessage(nombrePlanning);

                    setResult(RESULT_OK);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al guardar", Toast.LENGTH_SHORT).show();
                });
    }


    private void generateMotivationalMessage(String planningName) {
        new Thread(() -> {
            try {
                String apiKey = "AIzaSyA6c2dNuktVNNLZ0wA0G0o6cUhQyTIBHt4";
                String modelName = "gemini-2.0-flash";
                String endpoint = "https://generativelanguage.googleapis.com/v1beta/models/" + modelName + ":generateContent?key=" + apiKey;

                URL url = new URL(endpoint);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                String prompt = "Genera un mensaje corto (máximo 50 palabras) motivacional y alegre " +
                        "para felicitar a un usuario por completar su planificación semanal de comidas '" +
                        planningName + "'. Usa emojis y un tono cercano. Ejemplo: '¡Plan completado con éxito! 🎉 " +
                        "Esta semana vas a comer como un rey/una reina. ¡A por todas! 👨‍🍳👩‍🍳'";

                String jsonInput = "{ \"contents\": [ " +
                        "{ \"role\": \"user\", \"parts\": [ { \"text\": \"" + prompt + "\" } ] } " +
                        "] }";

                try (OutputStream os = conn.getOutputStream()) {
                    byte[] input = jsonInput.getBytes("utf-8");
                    os.write(input, 0, input.length);
                }


                int code = conn.getResponseCode();
                InputStream inputStream = (code >= 200 && code < 300) ? conn.getInputStream() : conn.getErrorStream();

                BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, "utf-8"));
                StringBuilder response = new StringBuilder();
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }

                String responseText = parseGeminiResponse(response.toString());


                runOnUiThread(() -> {
                    NotificationHelper.showNotification(
                            CreatePlanningActivity.this,
                            "¡Planificación completada!",
                            responseText
                    );
                });

            } catch (Exception e) {
                e.printStackTrace();

                runOnUiThread(() -> {
                    NotificationHelper.showNotification(
                            CreatePlanningActivity.this,
                            "¡Planificación completada!",
                            "¡Felicidades por completar tu planificación semanal! 🎉 Estás un paso más cerca de tus objetivos. 👨‍🍳"
                    );
                });
            }
        }).start();
    }

    private String parseGeminiResponse(String json) {
        try {
            JSONObject root = new JSONObject(json);
            JSONArray candidates = root.getJSONArray("candidates");
            if (candidates.length() > 0) {
                JSONObject firstCandidate = candidates.getJSONObject(0);
                JSONObject content = firstCandidate.getJSONObject("content");
                JSONArray parts = content.getJSONArray("parts");
                if (parts.length() > 0) {
                    JSONObject firstPart = parts.getJSONObject(0);
                    return firstPart.getString("text");
                }
            }
            return "¡Planificación guardada con éxito! 🎉";
        } catch (JSONException e) {
            e.printStackTrace();
            return "¡Buen trabajo completando tu planificación! 👏";
        }
    }}