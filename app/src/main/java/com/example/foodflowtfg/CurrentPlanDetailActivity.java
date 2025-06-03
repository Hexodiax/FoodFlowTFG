// CurrentPlanDetailActivity.java
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
    }

    @Override
    public void onDiaSeleccionado(String dia) {
        viewPager.setCurrentItem(0, true);
        adapter.actualizarDiaPlan(dia);
    }

    public void mostrarDialogoCompletarDia(String dia) {
        new AlertDialog.Builder(this)
                .setTitle("¿Completaste este día?")
                .setMessage("Selecciona una opción para el día " + dia)
                .setPositiveButton("Sí, completado", (dialog, which) -> marcarDiaCompletado(dia))
                .setNegativeButton("No completado", (dialog, which) -> mostrarDialogoNoCompletado(dia))
                .setNeutralButton("Cancelar", null)
                .show();
    }

    private void mostrarDialogoNoCompletado(String dia) {
        new AlertDialog.Builder(this)
                .setTitle("¿No completaste este día?")
                .setMessage("¿Quieres un mensaje de motivación para mañana?")
                .setPositiveButton("Sí", (dialog, which) -> generarMensajeMotivacion(dia))
                .setNegativeButton("No", (dialog, which) -> {
                    Toast.makeText(this, "Mañana es otra oportunidad. ¡Tú puedes!", Toast.LENGTH_SHORT).show();
                })
                .show();
    }

    private void generarMensajeMotivacion(String dia) {
        new Thread(() -> {
            try {
                String prompt = "Genera un mensaje motivador de 20-30 palabras para animar a alguien que no completó su plan de alimentación hoy. " +
                        "Usa un tono positivo y 2 emojis. Ejemplo: 'No te rindas, mañana es nueva oportunidad para cuidarte 🌟 " +
                        "Cada día cuenta, ¡tú puedes! 💪'";

                String mensaje = obtenerRespuestaGemini(prompt);

                runOnUiThread(() -> {
                    if (mensaje != null && !mensaje.isEmpty()) {
                        NotificationHelper.showNotification(this, "¡Ánimo!", mensaje);
                    } else {
                        mostrarMensajeFallbackMotivacion();
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Error al generar mensaje motivador", e);
                runOnUiThread(this::mostrarMensajeFallbackMotivacion);
            }
        }).start();
    }

    private void mostrarMensajeFallbackMotivacion() {
        String[] mensajes = {
                "Mañana es un nuevo día para cuidarte 🌟 ¡Tú puedes lograrlo! 💪",
                "No te desanimes, cada día es una nueva oportunidad 🌱 ¡Sigue adelante! ✨",
                "Pequeños pasos llevan a grandes resultados 🏆 ¡Mañana será mejor! 🌞"
        };
        String mensaje = mensajes[(int)(Math.random() * mensajes.length)];
        NotificationHelper.showNotification(this, "¡Ánimo!", mensaje);
    }

    private void marcarDiaCompletado(String dia) {
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
                        updates.put("diasCompletados." + dia, true);
                        updates.put("ultimaFechaCompletado." + dia, fechaActual);

                        db.collection("plannings").document(docId)
                                .update(updates)
                                .addOnSuccessListener(aVoid -> {
                                    obtenerConteoDiasCompletados(docId, dia);
                                    verificarSemanaCompleta(docId);
                                });
                    }
                });
    }

    private void verificarSemanaCompleta(String docId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("plannings").document(docId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Map<String, Boolean> diasCompletados = (Map<String, Boolean>) documentSnapshot.get("diasCompletados");
                        Map<String, String> fechasCompletado = (Map<String, String>) documentSnapshot.get("ultimaFechaCompletado");

                        int contador = 0;
                        for (Map.Entry<String, Boolean> entry : diasCompletados.entrySet()) {
                            if (entry.getValue()) {
                                String fechaCompletado = fechasCompletado != null ? fechasCompletado.get(entry.getKey()) : null;
                                if (fechaCompletado != null && esDeEstaSemana(fechaCompletado)) {
                                    contador++;
                                }
                            }
                        }

                        if (contador == 7) {
                            generarMensajeSemanaCompleta();
                            reiniciarContadorSemanal(docId);
                        }
                    }
                });
    }

    private void reiniciarContadorSemanal(String docId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> updates = new HashMap<>();

        String[] dias = {"Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo"};
        for (String dia : dias) {
            updates.put("diasCompletados." + dia, false);
        }

        db.collection("plannings").document(docId)
                .update(updates);
    }

    private void generarMensajeSemanaCompleta() {
        new Thread(() -> {
            try {
                String prompt = "Genera un mensaje celebratorio de 30 palabras para alguien que completó toda la semana de plan alimenticio. " +
                        "Usa 3 emojis y tono motivador. Ejemplo: '¡Increíble disciplina! Semana completada 🎉🏆✨ " +
                        "Tu esfuerzo vale la pena, ¡sigue así!'";

                String mensaje = obtenerRespuestaGemini(prompt);

                runOnUiThread(() -> {
                    if (mensaje != null && !mensaje.isEmpty()) {
                        NotificationHelper.showNotification(this, "¡Semana completada!", mensaje);
                    } else {
                        mostrarMensajeFallbackSemanaCompleta();
                    }
                });

                guardarLogroSemanal();
            } catch (Exception e) {
                Log.e(TAG, "Error al generar mensaje de semana completa", e);
                runOnUiThread(this::mostrarMensajeFallbackSemanaCompleta);
            }
        }).start();
    }

    private void mostrarMensajeFallbackSemanaCompleta() {
        String mensaje = "¡Felicidades! Has completado toda la semana 🎉🏆✨";
        NotificationHelper.showNotification(this, "¡Semana completada!", mensaje);
    }

    private void guardarLogroSemanal() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String fecha = obtenerFechaActual();

        Map<String, Object> logro = new HashMap<>();
        logro.put("userId", userId);
        logro.put("tipo", "semana_completada");
        logro.put("fecha", fecha);
        logro.put("plan", nombrePlan);

        db.collection("logros").add(logro);
    }

    private void obtenerConteoDiasCompletados(String docId, String diaCompletado) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("plannings").document(docId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Map<String, Boolean> diasCompletados = (Map<String, Boolean>) documentSnapshot.get("diasCompletados");
                        Map<String, String> fechasCompletado = (Map<String, String>) documentSnapshot.get("ultimaFechaCompletado");

                        int contador = 0;
                        for (Map.Entry<String, Boolean> entry : diasCompletados.entrySet()) {
                            if (entry.getValue()) {
                                String fechaCompletado = fechasCompletado != null ? fechasCompletado.get(entry.getKey()) : null;
                                if (fechaCompletado != null && esDeEstaSemana(fechaCompletado)) {
                                    contador++;
                                }
                            }
                        }
                        generarNotificacionGemini(diaCompletado, contador);
                    }
                });
    }

    private void generarNotificacionGemini(String dia, int diasCompletados) {
        new Thread(() -> {
            try {
                String fecha = obtenerFechaActual();
                String prompt = "Genera un mensaje positivo de 25-35 palabras celebrando completar el día " + dia +
                        " (" + fecha + "). Lleva " + diasCompletados + "/7 días. " +
                        "Usa 2 emojis. Ejemplo: '¡Buen trabajo hoy! (" + fecha + ") 🌟 " +
                        "Vas " + diasCompletados + "/7 días. ¡Sigue así! 💪'";

                String mensaje = obtenerRespuestaGemini(prompt);

                runOnUiThread(() -> {
                    if (mensaje != null && !mensaje.isEmpty()) {
                        String titulo = "¡Día completado!";
                        NotificationHelper.showNotification(this, titulo, mensaje);
                    } else {
                        mostrarMensajeFallbackDiaCompletado(dia, diasCompletados);
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Error al generar notificación", e);
                runOnUiThread(() -> mostrarMensajeFallbackDiaCompletado(dia, diasCompletados));
            }
        }).start();
    }

    private void mostrarMensajeFallbackDiaCompletado(String dia, int diasCompletados) {
        String fecha = obtenerFechaActual();
        String mensaje = "¡" + dia + " completado! (" + fecha + ")\n" +
                "Progreso semanal: " + diasCompletados + "/7 días";
        NotificationHelper.showNotification(this, "¡Día completado!", mensaje);
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
                InputStream errorStream = conn.getErrorStream();
                String errorResponse = readStream(errorStream);
                throw new Exception("Error en la API: " + responseCode + " - " + errorResponse);
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

    private boolean esDeEstaSemana(String fecha) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Date fechaCompletado = sdf.parse(fecha);

            Calendar calCompletado = Calendar.getInstance();
            calCompletado.setTime(fechaCompletado);

            Calendar calHoy = Calendar.getInstance();

            return calCompletado.get(Calendar.WEEK_OF_YEAR) == calHoy.get(Calendar.WEEK_OF_YEAR)
                    && calCompletado.get(Calendar.YEAR) == calHoy.get(Calendar.YEAR);
        } catch (Exception e) {
            return false;
        }
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