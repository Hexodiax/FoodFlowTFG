package com.example.foodflowtfg;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class TodayPlanFragment extends Fragment {

    private TextView txtComida, txtCena;
    private ImageView imgComida, imgCena;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_today_plan, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        txtComida = view.findViewById(R.id.txtComida);
        txtCena = view.findViewById(R.id.txtCena);
        imgComida = view.findViewById(R.id.imgComida);
        imgCena = view.findViewById(R.id.imgCena);

        Bundle args = getArguments();
        if (args == null) {
            Log.e("TodayPlanFragment", "No se recibieron argumentos");
            return;
        }

        String nombrePlan = args.getString("nombrePlan");
        String diaPlan = args.getString("diaPlan");

        if (nombrePlan == null || diaPlan == null) {
            Log.e("TodayPlanFragment", "nombrePlan o diaPlan son null");
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db.collection("plannings")
                .whereEqualTo("userId", userId)
                .whereEqualTo("nombre", nombrePlan)
                .limit(1)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        DocumentSnapshot doc = querySnapshot.getDocuments().get(0);

                        Object rawDiaData = doc.get(diaPlan);
                        Log.d("TodayPlanFragment", "Datos para día " + diaPlan + ": " + rawDiaData);

                        Map<String, Object> diaData;

                        if (rawDiaData instanceof Map) {
                            diaData = (Map<String, Object>) rawDiaData;
                        } else {
                            // Si el dato no es un Map, crear un mapa vacío para evitar errores
                            Log.w("TodayPlanFragment", "El campo para " + diaPlan + " no es un Map. Valor: " + rawDiaData);
                            diaData = new HashMap<>();
                        }

                        // Procesar comida
                        Object comidaObj = diaData.get("comida");
                        if (comidaObj instanceof Map) {
                            Map<String, Object> comidaMap = (Map<String, Object>) comidaObj;
                            String comidaNombre = (String) comidaMap.get("nombre");
                            String comidaId = (String) comidaMap.get("id");

                            txtComida.setText(comidaNombre != null ? "🍝 Comida: " + comidaNombre : "🍝 Comida: No asignada");
                            cargarImagenReceta(db, comidaId, imgComida);
                        } else {
                            txtComida.setText("🍝 Comida: No asignada");
                            imgComida.setImageResource(R.drawable.recipe_error);
                        }

                        // Procesar cena
                        Object cenaObj = diaData.get("cena");
                        if (cenaObj instanceof Map) {
                            Map<String, Object> cenaMap = (Map<String, Object>) cenaObj;
                            String cenaNombre = (String) cenaMap.get("nombre");
                            String cenaId = (String) cenaMap.get("id");

                            txtCena.setText(cenaNombre != null ? "🥗 Cena: " + cenaNombre : "🥗 Cena: No asignada");
                            cargarImagenReceta(db, cenaId, imgCena);
                        } else {
                            txtCena.setText("🥗 Cena: No asignada");
                            imgCena.setImageResource(R.drawable.recipe_error);
                        }

                    } else {
                        Log.e("TodayPlanFragment", "No se encontró el planning");
                        txtComida.setText("🍝 Comida: No asignada");
                        txtCena.setText("🥗 Cena: No asignada");
                        imgComida.setImageResource(R.drawable.recipe_error);
                        imgCena.setImageResource(R.drawable.recipe_error);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("TodayPlanFragment", "Error al obtener el planning", e);
                    txtComida.setText("🍝 Comida: Error");
                    txtCena.setText("🥗 Cena: Error");
                    imgComida.setImageResource(R.drawable.recipe_error);
                    imgCena.setImageResource(R.drawable.recipe_error);
                });
    }

    private void cargarImagenReceta(FirebaseFirestore db, String recetaId, ImageView imageView) {
        if (recetaId == null || recetaId.isEmpty()) {
            imageView.setImageResource(R.drawable.recipe_error);
            return;
        }

        // Buscar en recetas_personalizadas por campo "id"
        db.collection("recetas_personalizadas")
                .whereEqualTo("id", recetaId)
                .limit(1)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        DocumentSnapshot doc = querySnapshot.getDocuments().get(0);
                        String imagenUrl = doc.getString("imagenUrl");
                        if (imagenUrl != null && !imagenUrl.isEmpty()) {
                            Glide.with(requireContext()).load(imagenUrl).into(imageView);
                        } else {
                            imageView.setImageResource(R.drawable.recipe_error);
                        }
                    } else {
                        // Si no está en recetas_personalizadas, buscar en recetas generales
                        db.collection("recetas")
                                .whereEqualTo("id", recetaId)
                                .limit(1)
                                .get()
                                .addOnSuccessListener(qs -> {
                                    if (!qs.isEmpty()) {
                                        DocumentSnapshot doc = qs.getDocuments().get(0);
                                        String imagenUrl = doc.getString("imagen");
                                        if (imagenUrl != null && !imagenUrl.isEmpty()) {
                                            Glide.with(requireContext()).load(imagenUrl).into(imageView);
                                        } else {
                                            imageView.setImageResource(R.drawable.recipe_error);
                                        }
                                    } else {
                                        imageView.setImageResource(R.drawable.recipe_error);
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    imageView.setImageResource(R.drawable.recipe_error);
                                    Log.e("TodayPlanFragment", "Error al buscar receta general", e);
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    imageView.setImageResource(R.drawable.recipe_error);
                    Log.e("TodayPlanFragment", "Error al buscar receta personalizada", e);
                });
    }

}