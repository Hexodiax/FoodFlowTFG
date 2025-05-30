package com.example.foodflowtfg;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class WeekPlanFragment extends Fragment {

    private String nombrePlan;
    private OnDiaSeleccionadoListener listener;

    public WeekPlanFragment() {
        super(R.layout.fragment_week_plan);
    }

    // Interfaz para comunicar el día seleccionado a la Activity
    public interface OnDiaSeleccionadoListener {
        void onDiaSeleccionado(String dia);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnDiaSeleccionadoListener) {
            listener = (OnDiaSeleccionadoListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " debe implementar OnDiaSeleccionadoListener");
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            nombrePlan = getArguments().getString("nombrePlan");
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button btnMonday = view.findViewById(R.id.btnMonday);
        Button btnTuesday = view.findViewById(R.id.btnTuesday);
        Button btnWednesday = view.findViewById(R.id.btnWednesday);
        Button btnThursday = view.findViewById(R.id.btnThursday);
        Button btnFriday = view.findViewById(R.id.btnFriday);
        Button btnSaturday = view.findViewById(R.id.btnSaturday);
        Button btnSunday = view.findViewById(R.id.btnSunday);

        btnMonday.setOnClickListener(v -> notificarDiaSeleccionado("Lunes"));
        btnTuesday.setOnClickListener(v -> notificarDiaSeleccionado("Martes"));
        btnWednesday.setOnClickListener(v -> notificarDiaSeleccionado("Miércoles"));
        btnThursday.setOnClickListener(v -> notificarDiaSeleccionado("Jueves"));
        btnFriday.setOnClickListener(v -> notificarDiaSeleccionado("Viernes"));
        btnSaturday.setOnClickListener(v -> notificarDiaSeleccionado("Sábado"));
        btnSunday.setOnClickListener(v -> notificarDiaSeleccionado("Domingo"));
    }

    private void notificarDiaSeleccionado(String dia) {
        if (listener != null) {
            listener.onDiaSeleccionado(dia);
        }
    }
}
