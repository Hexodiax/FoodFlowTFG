package com.example.foodflowtfg;

import android.os.Bundle;
import android.text.SpannableString;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class PlanningActivity extends AppCompatActivity {

    private TextView nombrePlanningActual;
    private TextView fechaPlanningActual;
    private TextView diaPlanningActual;
    private RecyclerView recyclerOtrosPlannings;
    private PlanningAdapter adaptador;
    private ArrayList<String> listaDePlannings;

    private String nombrePlanningActualTexto = "Plan Actual";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_planning);

        nombrePlanningActual = findViewById(R.id.currentPlanningName);
        fechaPlanningActual = findViewById(R.id.currentPlanningDate);
        diaPlanningActual = findViewById(R.id.currentPlanningDay);
        recyclerOtrosPlannings = findViewById(R.id.recyclerOtherPlannings);

        // TextViews comidas
        TextView breakfastText = findViewById(R.id.breakfastText);
        TextView lunchText = findViewById(R.id.lunchText);
        TextView dinnerText = findViewById(R.id.dinnerText);

        // Inicializa lista y adapter
        listaDePlannings = new ArrayList<>();
        listaDePlannings.add("Semana de desfase");
        listaDePlannings.add("Semana Vegetariana");
        listaDePlannings.add("Plan Bajo en Carbohidratos");

        adaptador = new PlanningAdapter(listaDePlannings, new PlanningAdapter.PlanningListener() {
            @Override
            public void onEditClick(String nombrePlanning) {
                // Por hacer (de momento no hace nada)
            }

            @Override
            public void onUseClick(String nombrePlanning) {
                new AlertDialog.Builder(PlanningActivity.this)
                        .setTitle("Confirmar")
                        .setMessage("¿Quieres usar el plan \"" + nombrePlanning + "\" ahora?")
                        .setPositiveButton("Sí", (dialog, which) -> {
                            nombrePlanningActualTexto = nombrePlanning;
                            actualizarTextoPlanningActual(new Date());
                        })
                        .setNegativeButton("No", null)
                        .show();
            }

            @Override
            public void onDeleteClick(String nombrePlanning) {
                new AlertDialog.Builder(PlanningActivity.this)
                        .setTitle("Confirmar eliminación")
                        .setMessage("¿Seguro que quieres eliminar el plan \"" + nombrePlanning + "\"?")
                        .setPositiveButton("Eliminar", (dialog, which) -> {
                            listaDePlannings.remove(nombrePlanning);
                            adaptador.notifyDataSetChanged();
                        })
                        .setNegativeButton("Cancelar", null)
                        .show();
            }
        });

        recyclerOtrosPlannings.setLayoutManager(new LinearLayoutManager(this));
        recyclerOtrosPlannings.setAdapter(adaptador);

        // Set comidas con formato
        setComidaConFormato(breakfastText, "🥐", "Desayuno", "Texto del desayuno");
        setComidaConFormato(lunchText, "🍝", "Comida", "Texto de la comida");
        setComidaConFormato(dinnerText, "🥗", "Cena", "Texto de la cena");

        // Actualizar la parte superior con fecha y día
        actualizarTextoPlanningActual(new Date());
    }

    private void actualizarTextoPlanningActual(Date fecha) {
        nombrePlanningActual.setText(nombrePlanningActualTexto);

        // Fecha actual
        SimpleDateFormat formatoFecha = new SimpleDateFormat("d 'de' MMMM", new Locale("es", "ES"));
        String fechaFormateada = formatoFecha.format(fecha);

        // Primera letra del mes en Mayus y añadir "de" entre medias
        int posDe = fechaFormateada.indexOf("de ");
        if (posDe != -1 && posDe + 3 < fechaFormateada.length()) {
            char letraMayus = Character.toUpperCase(fechaFormateada.charAt(posDe + 3));
            fechaFormateada = fechaFormateada.substring(0, posDe + 3) + letraMayus + fechaFormateada.substring(posDe + 4);
        }
        fechaPlanningActual.setText(fechaFormateada);

        // Día de la semana  con la primera letra en mayus
        SimpleDateFormat formatoDiaSemana = new SimpleDateFormat("EEEE", new Locale("es", "ES"));
        String diaSemana = formatoDiaSemana.format(fecha);
        diaSemana = diaSemana.substring(0, 1).toUpperCase() + diaSemana.substring(1);
        diaPlanningActual.setText(diaSemana);
    }


    private void setComidaConFormato(TextView textView, String emoji, String tipoComida, String nombreReceta) {
        String textoCompleto = emoji + " " + tipoComida + ": " + nombreReceta;
        SpannableString spannable = new SpannableString(textoCompleto);

        int finNegrita = (emoji + " " + tipoComida + ":").length();
        spannable.setSpan(new android.text.style.StyleSpan(android.graphics.Typeface.BOLD),
                0, finNegrita, android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        textView.setText(spannable);
    }
}
