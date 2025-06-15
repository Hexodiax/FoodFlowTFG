package com.example.foodflowtfg;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.Map;

public class WeekPlanFragment extends Fragment {

    private String nombrePlan;
    private OnDiaSeleccionadoListener listener;
    private FirebaseFirestore db;
    private String userId;

    private final int[] dayButtonIds = {
            R.id.btnMonday,
            R.id.btnTuesday,
            R.id.btnWednesday,
            R.id.btnThursday,
            R.id.btnFriday,
            R.id.btnSaturday,
            R.id.btnSunday
    };

    private final int[] statusButtonIds = {
            R.id.statusMonday,
            R.id.statusTuesday,
            R.id.statusWednesday,
            R.id.statusThursday,
            R.id.statusFriday,
            R.id.statusSaturday,
            R.id.statusSunday
    };

    private final String[] diasSemana = {
            "Lunes",
            "Martes",
            "Miércoles",
            "Jueves",
            "Viernes",
            "Sábado",
            "Domingo"
    };

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
        db = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        if (getArguments() != null) {
            nombrePlan = getArguments().getString("nombrePlan");
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupDayButtons(view);
        cargarEstadosDias();
    }

    private void setupDayButtons(View view) {
        for (int i = 0; i < diasSemana.length; i++) {
            final String dia = diasSemana[i];
            Button dayButton = view.findViewById(dayButtonIds[i]);

            // Click corto para ver el día
            dayButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDiaSeleccionado(dia);
                }
            });

            // Long click para marcar completado/no completado
            dayButton.setOnLongClickListener(v -> {
                if (getActivity() instanceof CurrentPlanDetailActivity) {
                    ((CurrentPlanDetailActivity) getActivity()).mostrarDialogoCompletarDia(dia);
                    return true;
                }
                return false;
            });
        }
    }

    private void cargarEstadosDias() {
        if (nombrePlan == null || getView() == null) return;

        db.collection("plannings")
                .whereEqualTo("userId", userId)
                .whereEqualTo("nombre", nombrePlan)
                .limit(1)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        Map<String, Boolean> diasCompletados = (Map<String, Boolean>)
                                querySnapshot.getDocuments().get(0).get("diasCompletados");

                        if (diasCompletados != null) {
                            actualizarBotonesEstado(diasCompletados);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("WeekPlanFragment", "Error al cargar estados", e);
                });
    }

    public void actualizarEstadoDia(String dia, boolean completado) {
        if (getView() == null) return;

        for (int i = 0; i < diasSemana.length; i++) {
            if (diasSemana[i].equals(dia)) {
                Button statusButton = getView().findViewById(statusButtonIds[i]);
                if (statusButton != null) {
                    int colorRes = completado ? R.color.green_completed : R.color.red;
                    statusButton.setBackgroundTintList(
                            getResources().getColorStateList(colorRes));
                }
                break;
            }
        }
    }

    public void reiniciarTodosLosBotones() {
        if (getView() == null) return;

        for (int statusButtonId : statusButtonIds) {
            Button statusButton = getView().findViewById(statusButtonId);
            if (statusButton != null) {
                statusButton.setBackgroundTintList(
                        getResources().getColorStateList(R.color.red));
            }
        }
    }

    private void actualizarBotonesEstado(Map<String, Boolean> diasCompletados) {
        if (getView() == null) return;

        for (int i = 0; i < diasSemana.length; i++) {
            Button statusButton = getView().findViewById(statusButtonIds[i]);
            if (statusButton != null) {
                Boolean completado = diasCompletados.get(diasSemana[i]);
                int colorRes = (completado != null && completado) ?
                        R.color.green_completed : R.color.red;
                statusButton.setBackgroundTintList(
                        getResources().getColorStateList(colorRes));
            }
        }
    }
}