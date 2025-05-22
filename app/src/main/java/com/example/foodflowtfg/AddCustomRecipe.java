package com.example.foodflowtfg;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AddCustomRecipe extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final String DEFAULT_IMAGE_URL = "https://firebasestorage.googleapis.com/v0/b/tu-proyecto.appspot.com/o/default_recipe_image.png?alt=media";

    private EditText editTextNombre, editTextIngredientes, editTextPasos;
    private ImageView imagePreview;
    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agregar_receta_personalizada);

        editTextNombre = findViewById(R.id.editTextNombre);
        editTextIngredientes = findViewById(R.id.editTextIngredientes);
        editTextPasos = findViewById(R.id.editTextPasos);
        imagePreview = findViewById(R.id.imagePreview);

        Button btnSeleccionarImagen = findViewById(R.id.btnSeleccionarImagen);
        Button btnGuardar = findViewById(R.id.btnGuardar);

        btnSeleccionarImagen.setOnClickListener(v -> abrirGaleria());
        btnGuardar.setOnClickListener(v -> guardarReceta());
    }

    private void abrirGaleria() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Selecciona una imagen"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                imagePreview.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void guardarReceta() {
        String nombre = editTextNombre.getText().toString().trim();
        String ingredientes = editTextIngredientes.getText().toString().trim();
        String pasos = editTextPasos.getText().toString().trim();

        if (nombre.isEmpty() || ingredientes.isEmpty() || pasos.isEmpty()) {
            Toast.makeText(this, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        if (imageUri == null) {
            // No hay imagen, usar imagen por defecto
            guardarEnFirestore(nombre, ingredientes, pasos, DEFAULT_IMAGE_URL);
        } else {
            // Subir imagen seleccionada
            StorageReference storageRef = FirebaseStorage.getInstance().getReference();
            StorageReference imageRef = storageRef.child("recetas_personalizadas/" + UUID.randomUUID().toString());

            imageRef.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot ->
                            imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                                String imageUrl = uri.toString();
                                guardarEnFirestore(nombre, ingredientes, pasos, imageUrl);
                            })
                    )
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Error al subir imagen", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    });
        }
    }

    private void guardarEnFirestore(String nombre, String ingredientes, String pasos, String imageUrl) {
        Map<String, Object> receta = new HashMap<>();
        receta.put("nombre", nombre);
        receta.put("ingredientes", ingredientes);
        receta.put("pasos", pasos);
        receta.put("imagenUrl", imageUrl);

        FirebaseFirestore.getInstance()
                .collection("recetas_personalizadas")
                .add(receta)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Receta guardada correctamente", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al guardar receta", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                });
    }
}
