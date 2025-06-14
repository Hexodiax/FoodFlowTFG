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

public class PlanningActivity extends AppCompatActivity implements PlanningAdapter.PlanningListener {

    private static final int EDIT_PLANNING_REQUEST = 1001;
    private static final String[] dias = {"Lunes", "Martes", "Miercoles", "Jueves", "Viernes", "Sabado", "Domingo"};

    private TextView nombrePlanningActual;
    private TextView fechaPlanningActual;
    private TextView diaPlanningActual;
    private RecyclerView recyclerOtrosPlannings;
    private PlanningAdapter adaptador;
    private ArrayList<PlanningAdapter.PlanningItem> listaDePlannings;

    private SharedPreferences prefs;
    private static final String PREFS_NAME = "MisPlanes";
    private static final String KEY_PLAN_ACTUAL = "plan_actual";
    private String nombrePlanningActualTexto = "Plan Actual";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_planning);

        // Inicializar vistas
        nombrePlanningActual = findViewById(R.id.currentPlanningName);
        fechaPlanningActual = findViewById(R.id.currentPlanningDate);
        diaPlanningActual = findViewById(R.id.currentPlanningDay);
        recyclerOtrosPlannings = findViewById(R.id.recyclerOtherPlannings);

        TextView lunchText = findViewById(R.id.lunchText);
        TextView dinnerText = findViewById(R.id.dinnerText);

        // Configurar SharedPreferences
        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        nombrePlanningActualTexto = prefs.getString(KEY_PLAN_ACTUAL, "Plan Actual");

        // Inicializar lista y adaptador
        listaDePlannings = new ArrayList<>();
        adaptador = new PlanningAdapter(listaDePlannings, this);
        recyclerOtrosPlannings.setLayoutManager(new LinearLayoutManager(this));
        recyclerOtrosPlannings.setAdapter(adaptador);

        // Configurar textos de comida/cena
        setComidaConFormato(lunchText, "🍝", "Comida", "Texto de la comida");
        setComidaConFormato(dinnerText, "🥗", "Cena", "Texto de la cena");

        // Actualizar UI
        actualizarTextoPlanningActual(new Date());
        cargarComidasDelDia();
        cargarPlanningsDesdeFirestore();

        // Configurar listeners
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

    @Override
    public void onEditClick(String planningId, String planningName) {
        abrirEditarPlanning(planningId, planningName);
    }

    @Override
    public void onUseClick(String planningId, String planningName) {
        new AlertDialog.Builder(PlanningActivity.this)
                .setTitle("Confirmar")
                .setMessage("¿Quieres usar el plan \"" + planningName + "\" ahora?")
                .setPositiveButton("Sí", (dialog, which) -> {
                    prefs.edit().putString(KEY_PLAN_ACTUAL, planningName).apply();
                    nombrePlanningActualTexto = planningName;
                    actualizarTextoPlanningActual(new Date());
                    cargarComidasDelDia();
                })
                .setNegativeButton("No", null)
                .show();
    }

    @Override
    public void onDeleteClick(String planningId, String planningName) {
        mostrarDialogoConfirmacionEliminacion(planningId, planningName);
    }

    private void abrirEditarPlanning(String planningId, String planningName) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("plannings").document(planningId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Intent intent = new Intent(this, EditPlanningActivity.class);
                        intent.putExtra("planningId", planningId);
                        intent.putExtra("planningName", planningName);

                        // Pasar todos los datos del planning
                        for (String dia : dias) {
                            if (documentSnapshot.contains(dia)) {
                                Map<String, Object> diaData = (Map<String, Object>) documentSnapshot.get(dia);
                                if (diaData != null) {
                                    Map<String, String> comidaData = (Map<String, String>) diaData.get("comida");
                                    Map<String, String> cenaData = (Map<String, String>) diaData.get("cena");

                                    if (comidaData != null) {
                                        intent.putExtra(dia + "_comida", comidaData.get("nombre"));
                                        intent.putExtra(dia + "_comida_id", comidaData.get("id"));
                                    }

                                    if (cenaData != null) {
                                        intent.putExtra(dia + "_cena", cenaData.get("nombre"));
                                        intent.putExtra(dia + "_cena_id", cenaData.get("id"));
                                    }
                                }
                            }
                        }

                        startActivityForResult(intent, EDIT_PLANNING_REQUEST);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al cargar el planning", Toast.LENGTH_SHORT).show();
                    Log.e("PlanningActivity", "Error al cargar planning", e);
                });
    }

    private void mostrarDialogoConfirmacionEliminacion(String planningId, String planningName) {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar plan")
                .setMessage("¿Eliminar permanentemente \"" + planningName + "\"?")
                .setPositiveButton("Eliminar", (d, w) -> procesarEliminacion(planningId, planningName))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void procesarEliminacion(String planningId, String planningName) {
        String planActual = prefs.getString(KEY_PLAN_ACTUAL, "");
        if (planningName.equals(planActual)) {
            prefs.edit().remove(KEY_PLAN_ACTUAL).apply();
            nombrePlanningActualTexto = "Plan Actual";
            actualizarTextoPlanningActual(new Date());
        }

        eliminarPlanDeFirestore(planningId);
        listaDePlannings.removeIf(item -> item.id.equals(planningId));
        adaptador.notifyDataSetChanged();

        Toast.makeText(this, planningName + " eliminado", Toast.LENGTH_SHORT).show();
    }

    private void eliminarPlanDeFirestore(String planningId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("plannings").document(planningId)
                .delete()
                .addOnSuccessListener(aVoid -> Log.d("Firestore", "Documento eliminado"))
                .addOnFailureListener(e -> Log.e("Firestore", "Error al eliminar", e));
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
                            if (nombrePlan != null) {
                                listaDePlannings.add(new PlanningAdapter.PlanningItem(
                                        document.getId(),
                                        nombrePlan
                                ));
                            }
                        }
                        adaptador.notifyDataSetChanged();
                    } else {
                        Log.e("Firestore", "Error al cargar plannings", task.getException());
                        Toast.makeText(this, "Error al cargar plannings", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void actualizarTextoPlanningActual(Date fecha) {
        nombrePlanningActual.setText(nombrePlanningActualTexto);

        SimpleDateFormat formatoFecha = new SimpleDateFormat("d 'de' MMMM", new Locale("es", "ES"));
        String fechaFormateada = formatoFecha.format(fecha);

        // Capitalizar el mes
        int posDe = fechaFormateada.indexOf("de ");
        if (posDe != -1 && posDe + 3 < fechaFormateada.length()) {
            char letraMayus = Character.toUpperCase(fechaFormateada.charAt(posDe + 3));
            fechaFormateada = fechaFormateada.substring(0, posDe + 3) + letraMayus + fechaFormateada.substring(posDe + 4);
        }
        fechaPlanningActual.setText(fechaFormateada);

        // Obtener día de la semana
        SimpleDateFormat formatoDiaSemana = new SimpleDateFormat("EEEE", new Locale("es", "ES"));
        String diaSemana = formatoDiaSemana.format(fecha);
        diaSemana = diaSemana.substring(0, 1).toUpperCase() + diaSemana.substring(1);
        diaPlanningActual.setText(diaSemana);
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

    private void cargarComidasDelDia() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String nombrePlan = prefs.getString(KEY_PLAN_ACTUAL, "Plan Actual");
        String diaSemana = normalizarDiaSemana(diaPlanningActual.getText().toString());

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("plannings")
                .whereEqualTo("userId", userId)
                .whereEqualTo("nombre", nombrePlan)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            Map<String, Object> comidas = (Map<String, Object>) doc.get(diaSemana);
                            if (comidas != null) {
                                Map<String, Object> comida = (Map<String, Object>) comidas.get("comida");
                                String nombreComida = comida != null ? (String) comida.get("nombre") : "No disponible";

                                Map<String, Object> cena = (Map<String, Object>) comidas.get("cena");
                                String nombreCena = cena != null ? (String) cena.get("nombre") : "No disponible";

                                setComidaConFormato(findViewById(R.id.lunchText), "🍝", "Comida", nombreComida);
                                setComidaConFormato(findViewById(R.id.dinnerText), "🥗", "Cena", nombreCena);
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Error al obtener comidas del día", e));
    }

    private void setComidaConFormato(TextView textView, String emoji, String tipoComida, String nombreReceta) {
        String textoCompleto = emoji + " " + tipoComida + ": " + nombreReceta;
        SpannableString spannable = new SpannableString(textoCompleto);

        int finNegrita = (emoji + " " + tipoComida + ":").length();
        spannable.setSpan(new android.text.style.StyleSpan(android.graphics.Typeface.BOLD),
                0, finNegrita, android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        textView.setText(spannable);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == EDIT_PLANNING_REQUEST && resultCode == RESULT_OK) {
            cargarPlanningsDesdeFirestore();
            cargarComidasDelDia();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarPlanningsDesdeFirestore();
        cargarComidasDelDia();
    }
}