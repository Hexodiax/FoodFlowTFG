package com.example.foodflowtfg;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class CurrentPlanDetailActivity extends AppCompatActivity
        implements WeekPlanFragment.OnDiaSeleccionadoListener {

    private CurrentPlanPagerAdapter adapter;
    private ViewPager2 viewPager;
    private String nombrePlan;
    private static final String GEMINI_API_KEY = "AIzaSyA6c2dNuktVNNLZ0wA0G0o6cUhQyTIBHt4";
    private static final String TAG = "CurrentPlanDetail";

    // Contadores
    private int contadorCompletados = 0;
    private int contadorRegistrados = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_current_plan_detail);

        nombrePlan = getIntent().getStringExtra("Plan_actual");
        String diaPlan = getIntent().getStringExtra("plan_day");

        if (diaPlan == null) {
            diaPlan = obtenerDiaActual();
        }

        viewPager = findViewById(R.id.viewPagerCurrentPlan);
        TabLayout tabLayout = findViewById(R.id.tabLayoutCurrentPlan);

        adapter = new CurrentPlanPagerAdapter(this, nombrePlan, diaPlan);
        viewPager.setAdapter(adapter);

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            tab.setText(position == 0 ? "Día" : "Semana");
        }).attach();

        cargarContadoresIniciales();
    }

    private void cargarContadoresIniciales() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db.collection("plannings")
                .whereEqualTo("userId", userId)
                .whereEqualTo("nombre", nombrePlan)
                .limit(1)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        Long registrados = querySnapshot.getDocuments().get(0).getLong("contadorRegistrados");
                        Long completados = querySnapshot.getDocuments().get(0).getLong("contadorCompletados");

                        contadorRegistrados = registrados != null ? registrados.intValue() : 0;
                        contadorCompletados = completados != null ? completados.intValue() : 0;
                    }
                });
    }

    @Override
    public void onDiaSeleccionado(String dia) {
        viewPager.setCurrentItem(0, true);
        adapter.actualizarDiaPlan(dia);
    }

    public void mostrarDialogoCompletarDia(String dia) {
        new AlertDialog.Builder(this)
                .setTitle("Registrar día")
                .setMessage("¿Cómo completaste el día " + dia + "?")
                .setPositiveButton("Completado", (dialog, which) -> registrarDia(dia, true))
                .setNegativeButton("No completado", (dialog, which) -> registrarDia(dia, false))
                .setNeutralButton("Cancelar", null)
                .show();
    }

    private void registrarDia(String dia, boolean completado) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String fechaActual = obtenerFechaActual();

        db.collection("plannings")
                .whereEqualTo("userId", userId)
                .whereEqualTo("nombre", nombrePlan)
                .limit(1)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        String docId = querySnapshot.getDocuments().get(0).getId();
                        Map<String, Object> updates = new HashMap<>();

                        // Actualizar estado del día y fecha
                        updates.put("diasCompletados." + dia, completado);
                        updates.put("ultimaFechaCompletado." + dia, fechaActual);

                        // Actualizar contadores
                        updates.put("contadorRegistrados", FieldValue.increment(1));
                        if (completado) {
                            updates.put("contadorCompletados", FieldValue.increment(1));
                        }

                        db.collection("plannings").document(docId)
                                .update(updates)
                                .addOnSuccessListener(aVoid -> {
                                    // Actualizar UI
                                    actualizarEstadoDiaEnUI(dia, completado);

                                    // Actualizar contadores locales
                                    if (completado) {
                                        contadorCompletados++;
                                    }
                                    contadorRegistrados++;

                                    // Mostrar notificación del día
                                    mostrarNotificacionDiaria(dia, completado);

                                    // Verificar fin de semana
                                    verificarFinDeSemana(docId);
                                });
                    }
                });
    }

    private void mostrarNotificacionDiaria(String dia, boolean completado) {
        String titulo = completado ?
                String.format(Locale.getDefault(), "%s completado (%d/7)", dia, contadorCompletados) :
                String.format(Locale.getDefault(), "%s no completado (%d/7)", dia, contadorCompletados);

        new Thread(() -> {
            try {
                String prompt = completado ?
                        String.format(Locale.getDefault(),
                                "Genera un mensaje positivo de 15 palabras para celebrar completar el día %s (%d/7). Usa 1 emoji.",
                                dia, contadorCompletados) :
                        String.format(Locale.getDefault(),
                                "Genera un mensaje motivacional de 15 palabras para animar después de no completar el día %s (%d/7). Usa 1 emoji.",
                                dia, contadorCompletados);

                String mensaje = obtenerRespuestaGemini(prompt);

                runOnUiThread(() -> {
                    NotificationHelper.showNotification(
                            this,
                            titulo,
                            mensaje != null ? mensaje :
                                    (completado ?
                                            String.format("¡Buen trabajo! %s completado (%d/7)", dia, contadorCompletados) :
                                            String.format("No te preocupes. %s no completado (%d/7)", dia, contadorCompletados))
                    );
                });
            } catch (Exception e) {
                Log.e(TAG, "Error al generar notificación diaria", e);
                runOnUiThread(() -> {
                    NotificationHelper.showNotification(
                            this,
                            titulo,
                            completado ?
                                    String.format("¡Día %s completado! Progreso: %d/7", dia, contadorCompletados) :
                                    String.format("%s no completado. Vas %d/7", dia, contadorCompletados)
                    );
                });
            }
        }).start();
    }

    private void verificarFinDeSemana(String docId) {
        if (contadorRegistrados >= 7 || contadorCompletados >= 7) {
            if (contadorCompletados >= 7) {
                mostrarNotificacionSemanal(true);
            } else {
                mostrarNotificacionSemanal(false);
            }
            reiniciarSemana(docId);
        }
    }

    private void mostrarNotificacionSemanal(boolean semanaCompleta) {
        String titulo = semanaCompleta ? "¡Semana completada!" : "Resumen semanal";

        new Thread(() -> {
            try {
                String prompt = semanaCompleta ?
                        "Genera un mensaje celebratorio de 20 palabras con 3 emojis para una semana completada al 100%" :
                        String.format(Locale.getDefault(),
                                "Genera un mensaje motivacional de 20 palabras con 2 emojis para animar después de completar %d/7 días esta semana",
                                contadorCompletados);

                String mensaje = obtenerRespuestaGemini(prompt);

                runOnUiThread(() -> {
                    NotificationHelper.showNotification(
                            this,
                            titulo,
                            mensaje != null ? mensaje :
                                    (semanaCompleta ?
                                            "¡Felicidades! Has completado todos los días esta semana 🎉🌟💯" :
                                            String.format("Completaste %d/7 días. ¡Sigue así! 💪", contadorCompletados))
                    );
                });
            } catch (Exception e) {
                Log.e(TAG, "Error al generar notificación semanal", e);
                runOnUiThread(() -> {
                    NotificationHelper.showNotification(
                            this,
                            titulo,
                            semanaCompleta ?
                                    "¡Excelente! Semana completada al 100%" :
                                    String.format("Resumen: %d/7 días completados", contadorCompletados)
                    );
                });
            }
        }).start();
    }

    private void reiniciarSemana(String docId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> updates = new HashMap<>();

        // Reiniciar contadores
        updates.put("contadorRegistrados", 0);
        updates.put("contadorCompletados", 0);

        // Poner todos los días como no completados
        String[] diasSemana = {"Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo"};
        for (String dia : diasSemana) {
            updates.put("diasCompletados." + dia, false);
        }

        db.collection("plannings").document(docId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    // Resetear contadores locales
                    contadorRegistrados = 0;
                    contadorCompletados = 0;

                    // Actualizar UI
                    reiniciarUI();

                    Toast.makeText(this, "¡Nueva semana comenzada!", Toast.LENGTH_SHORT).show();
                });
    }

    private void actualizarEstadoDiaEnUI(String dia, boolean completado) {
        WeekPlanFragment weekFragment = (WeekPlanFragment) getSupportFragmentManager()
                .findFragmentByTag("f" + adapter.getItemId(1));
        if (weekFragment != null) {
            weekFragment.actualizarEstadoDia(dia, completado);
        }
    }

    private void reiniciarUI() {
        WeekPlanFragment weekFragment = (WeekPlanFragment) getSupportFragmentManager()
                .findFragmentByTag("f" + adapter.getItemId(1));
        if (weekFragment != null) {
            weekFragment.reiniciarTodosLosBotones();
        }
    }

    private String obtenerRespuestaGemini(String prompt) throws Exception {
        HttpURLConnection conn = null;
        BufferedReader reader = null;
        try {
            URL url = new URL("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=" + GEMINI_API_KEY);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);
            conn.setDoOutput(true);

            JSONObject requestBody = new JSONObject();
            JSONArray contents = new JSONArray();
            JSONObject content = new JSONObject();
            JSONArray parts = new JSONArray();
            JSONObject part = new JSONObject();
            part.put("text", prompt);
            parts.put(part);
            content.put("parts", parts);
            contents.put(content);
            requestBody.put("contents", contents);

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = requestBody.toString().getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            int responseCode = conn.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw new Exception("Error en la API: " + responseCode);
            }

            InputStream inputStream = conn.getInputStream();
            String jsonResponse = readStream(inputStream);

            JSONObject json = new JSONObject(jsonResponse);
            JSONArray candidates = json.getJSONArray("candidates");
            JSONObject candidate = candidates.getJSONObject(0);
            JSONObject contentObj = candidate.getJSONObject("content");
            JSONArray partsArray = contentObj.getJSONArray("parts");
            return partsArray.getJSONObject(0).getString("text");

        } finally {
            if (reader != null) {
                reader.close();
            }
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    private String readStream(InputStream inputStream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        return response.toString();
    }

    private String obtenerFechaActual() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        return sdf.format(new Date());
    }

    private String obtenerDiaActual() {
        String[] dias = {"Domingo", "Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado"};
        Calendar calendar = Calendar.getInstance();
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        return dias[dayOfWeek - 1];
    }
}