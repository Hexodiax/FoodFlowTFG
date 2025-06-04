package com.example.foodflowtfg;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.SpannableString;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class PlanningActivity extends AppCompatActivity {

    private TextView nombrePlanningActual;
    private TextView fechaPlanningActual;
    private TextView diaPlanningActual;
    private RecyclerView recyclerOtrosPlannings;
    private PlanningAdapter adaptador;
    private ArrayList<String> listaDePlannings;

    private SharedPreferences prefs;
    private static final String PREFS_NAME = "MisPlanes";
    private static final String KEY_PLAN_ACTUAL = "plan_actual";

    private String nombrePlanningActualTexto = "Plan Actual";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_planning);

        nombrePlanningActual = findViewById(R.id.currentPlanningName);
        fechaPlanningActual = findViewById(R.id.currentPlanningDate);
        diaPlanningActual = findViewById(R.id.currentPlanningDay);
        recyclerOtrosPlannings = findViewById(R.id.recyclerOtherPlannings);

        TextView lunchText = findViewById(R.id.lunchText);
        TextView dinnerText = findViewById(R.id.dinnerText);

        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        nombrePlanningActualTexto = prefs.getString(KEY_PLAN_ACTUAL, "Plan Actual");

        listaDePlannings = new ArrayList<>();

        adaptador = new PlanningAdapter(listaDePlannings, new PlanningAdapter.PlanningListener() {
            @Override
            public void onEditClick(String nombrePlanning) {
                // Por hacer
            }

            @Override
            public void onUseClick(String nombrePlanning) {
                new AlertDialog.Builder(PlanningActivity.this)
                        .setTitle("Confirmar")
                        .setMessage("¿Quieres usar el plan \"" + nombrePlanning + "\" ahora?")
                        .setPositiveButton("Sí", (dialog, which) -> {
                            prefs.edit().putString(KEY_PLAN_ACTUAL, nombrePlanning).apply();
                            nombrePlanningActualTexto = nombrePlanning;
                            actualizarTextoPlanningActual(new Date());
                        })
                        .setNegativeButton("No", null)
                        .show();
            }

            @Override
            public void onDeleteClick(String nombrePlanning) {
                mostrarDialogoConfirmacionEliminacion(nombrePlanning);
            }
        });

        recyclerOtrosPlannings.setLayoutManager(new LinearLayoutManager(this));
        recyclerOtrosPlannings.setAdapter(adaptador);

        cargarPlanningsDesdeFirestore();
        setComidaConFormato(lunchText, "🍝", "Comida", "Texto de la comida");
        setComidaConFormato(dinnerText, "🥗", "Cena", "Texto de la cena");

        actualizarTextoPlanningActual(new Date());
        cargarComidasDelDia();
        findViewById(R.id.currentPlanningCard).setOnClickListener(view -> {
            Intent intent = new Intent(PlanningActivity.this, CurrentPlanDetailActivity.class);
            String diaParaCodigo = normalizarDiaSemana(diaPlanningActual.getText().toString());
            intent.putExtra("plan_day", diaParaCodigo);
            intent.putExtra("Plan_actual", nombrePlanningActual.getText().toString());
            startActivity(intent);
        });

        findViewById(R.id.fabAddPlanning).setOnClickListener(view -> {
            Intent intent = new Intent(PlanningActivity.this, CreatePlanningActivity.class);
            startActivity(intent);
        });
    }

    private void mostrarDialogoConfirmacionEliminacion(String nombrePlanning) {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar plan")
                .setMessage("¿Eliminar permanentemente \"" + nombrePlanning + "\"?")
                .setPositiveButton("Eliminar", (d, w) -> procesarEliminacion(nombrePlanning))
                .setNegativeButton("Cancelar", null)
                .show();
    }
    private String normalizarDiaSemana(String diaConTilde) {
        return diaConTilde
                .replace("á", "a")
                .replace("é", "e")
                .replace("í", "i")
                .replace("ó", "o")
                .replace("ú", "u")
                .replace("Á", "A")
                .replace("É", "E")
                .replace("Í", "I")
                .replace("Ó", "O")
                .replace("Ú", "U")
                .replace("ñ", "n")
                .replace("Ñ", "N");
    }

    private void procesarEliminacion(String nombrePlanning) {
        String planActual = prefs.getString(KEY_PLAN_ACTUAL, "");
        if (nombrePlanning.equals(planActual)) {
            prefs.edit().remove(KEY_PLAN_ACTUAL).apply();
            nombrePlanningActualTexto = "Plan Actual";
            actualizarTextoPlanningActual(new Date());
        }

        eliminarPlanDeFirestore(nombrePlanning);
        listaDePlannings.remove(nombrePlanning);
        adaptador.notifyDataSetChanged();

        Toast.makeText(this, nombrePlanning + " eliminado", Toast.LENGTH_SHORT).show();
    }

    private void eliminarPlanDeFirestore(String nombrePlanning) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db.collection("plannings")
                .whereEqualTo("userId", userId)
                .whereEqualTo("nombre", nombrePlanning)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            db.collection("plannings").document(document.getId()).delete()
                                    .addOnSuccessListener(aVoid -> Log.d("Firestore", "Documento eliminado"))
                                    .addOnFailureListener(e -> Log.e("Firestore", "Error al eliminar", e));
                        }
                    } else {
                        Log.e("Firestore", "Error al buscar plan", task.getException());
                    }
                });
    }

    private void actualizarTextoPlanningActual(Date fecha) {
        nombrePlanningActual.setText(nombrePlanningActualTexto);

        SimpleDateFormat formatoFecha = new SimpleDateFormat("d 'de' MMMM", new Locale("es", "ES"));
        String fechaFormateada = formatoFecha.format(fecha);

        int posDe = fechaFormateada.indexOf("de ");
        if (posDe != -1 && posDe + 3 < fechaFormateada.length()) {
            char letraMayus = Character.toUpperCase(fechaFormateada.charAt(posDe + 3));
            fechaFormateada = fechaFormateada.substring(0, posDe + 3) + letraMayus + fechaFormateada.substring(posDe + 4);
        }
        fechaPlanningActual.setText(fechaFormateada);

        SimpleDateFormat formatoDiaSemana = new SimpleDateFormat("EEEE", new Locale("es", "ES"));
        String diaSemana = formatoDiaSemana.format(fecha);
        diaSemana = diaSemana.substring(0, 1).toUpperCase() + diaSemana.substring(1);
        diaPlanningActual.setText(diaSemana);
    }

    private void cargarPlanningsDesdeFirestore() {
        String userIdActual = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("plannings")
                .whereEqualTo("userId", userIdActual)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        listaDePlannings.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String nombrePlan = document.getString("nombre");
                            if (nombrePlan != null && !listaDePlannings.contains(nombrePlan)) {
                                listaDePlannings.add(nombrePlan);
                            }
                        }
                        adaptador.notifyDataSetChanged();
                    } else {
                        Log.e("Firestore", "Error al cargar plannings", task.getException());
                    }
                });
    }

    private void setComidaConFormato(TextView textView, String emoji, String tipoComida, String nombreReceta) {
        String textoCompleto = emoji + " " + tipoComida + ": " + nombreReceta;
        SpannableString spannable = new SpannableString(textoCompleto);

        int finNegrita = (emoji + " " + tipoComida + ":").length();
        spannable.setSpan(new android.text.style.StyleSpan(android.graphics.Typeface.BOLD),
                0, finNegrita, android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        textView.setText(spannable);
    }
    private void cargarComidasDelDia() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String nombrePlan = prefs.getString(KEY_PLAN_ACTUAL, "Plan Actual");

        // Usamos el texto ya mostrado en pantalla como día (ej: "Lunes", "Martes")
        final String diaSemana = normalizarDiaSemana(diaPlanningActual.getText().toString());

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("plannings")
                .whereEqualTo("userId", userId)
                .whereEqualTo("nombre", nombrePlan)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            Object datosDia = doc.get(diaSemana);
                            if (datosDia instanceof Map) {
                                @SuppressWarnings("unchecked")
                                Map<String, Object> comidas = (Map<String, Object>) datosDia;

                                Object comidaObj = comidas.get("comida");
                                String nombreComida = "No disponible";
                                if (comidaObj instanceof Map) {
                                    Map<String, Object> comidaMap = (Map<String, Object>) comidaObj;
                                    Object nombre = comidaMap.get("nombre");
                                    if (nombre != null) {
                                        nombreComida = nombre.toString();
                                    }
                                }

                                Object cenaObj = comidas.get("cena");
                                String nombreCena = "No disponible";
                                if (cenaObj instanceof Map) {
                                    Map<String, Object> cenaMap = (Map<String, Object>) cenaObj;
                                    Object nombre = cenaMap.get("nombre");
                                    if (nombre != null) {
                                        nombreCena = nombre.toString();
                                    }
                                }

                                setComidaConFormato(findViewById(R.id.lunchText), "🍝", "Comida", nombreComida);
                                setComidaConFormato(findViewById(R.id.dinnerText), "🥗", "Cena", nombreCena);
                            } else {
                                Log.e("Firestore", "El campo del día no es un mapa válido: " + datosDia);
                            }



                        }
                    } else {
                        Log.e("Firestore", "No se encontró el documento con nombre: " + nombrePlan);
                    }
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Error al obtener comidas del día", e));
    }


    @Override
    protected void onResume() {
        super.onResume();
        cargarPlanningsDesdeFirestore();
    }
}
