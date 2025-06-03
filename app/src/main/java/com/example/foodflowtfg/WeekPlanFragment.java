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

        setupDayButton(view, R.id.btnMonday, "Lunes");
        setupDayButton(view, R.id.btnTuesday, "Martes");
        setupDayButton(view, R.id.btnWednesday, "Miércoles");
        setupDayButton(view, R.id.btnThursday, "Jueves");
        setupDayButton(view, R.id.btnFriday, "Viernes");
        setupDayButton(view, R.id.btnSaturday, "Sábado");
        setupDayButton(view, R.id.btnSunday, "Domingo");
    }

    private void setupDayButton(View view, int buttonId, String dayName) {
        Button button = view.findViewById(buttonId);

        // Click normal para mostrar las recetas del día
        button.setOnClickListener(v -> notificarDiaSeleccionado(dayName));

        // Long click para marcar el día como completado
        button.setOnLongClickListener(v -> {
            if (getActivity() instanceof CurrentPlanDetailActivity) {
                ((CurrentPlanDetailActivity)getActivity()).mostrarDialogoCompletarDia(dayName);
                return true;
            }
            return false;
        });
    }

    private void notificarDiaSeleccionado(String dia) {
        if (listener != null) {
            listener.onDiaSeleccionado(dia);
        }
    }
}