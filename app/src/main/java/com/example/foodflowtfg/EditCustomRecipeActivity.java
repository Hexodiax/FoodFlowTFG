package com.example.foodflowtfg;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EditCustomRecipeActivity extends AppCompatActivity {

    private EditText editTextNombre, editTextIngredientes, editTextPasos;
    private ImageView imagePreview;
    private Uri imageUri;
    private String recipeId, currentImageUrl;
    private boolean imageChanged = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_custom_recipe);

        // Inicializar vistas
        editTextNombre = findViewById(R.id.editTextNombre);
        editTextIngredientes = findViewById(R.id.editTextIngredientes);
        editTextPasos = findViewById(R.id.editTextPasos);
        imagePreview = findViewById(R.id.imagePreview);
        Button btnSeleccionarImagen = findViewById(R.id.btnSeleccionarImagen);
        Button btnGuardar = findViewById(R.id.btnGuardar);

        // Obtener datos de la receta del Intent
        Intent intent = getIntent();
        recipeId = intent.getStringExtra("recipeId");
        String nombre = intent.getStringExtra("nombre");
        String ingredientes = intent.getStringExtra("ingredientes");
        String pasos = intent.getStringExtra("pasos");
        currentImageUrl = intent.getStringExtra("imagenUrl");

        // Rellenar los campos con los datos de la receta
        editTextNombre.setText(nombre);
        editTextIngredientes.setText(ingredientes);
        editTextPasos.setText(pasos);

        // Cargar la imagen actual usando Glide
        if (currentImageUrl != null && !currentImageUrl.isEmpty()) {
            Glide.with(this)
                    .load(currentImageUrl)
                    .placeholder(R.drawable.placeholder_recipe)
                    .into(imagePreview);
        }

        // Listeners de los botones
        btnSeleccionarImagen.setOnClickListener(v -> abrirGaleria());
        btnGuardar.setOnClickListener(v -> guardarCambios());

        // Configurar botón de eliminar si es necesario
        Button btnEliminar = findViewById(R.id.btnEliminar);
        btnEliminar.setOnClickListener(v -> eliminarReceta());
    }

    private void abrirGaleria() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            imagePreview.setImageURI(imageUri);
            imageChanged = true;
        }
    }

    private void guardarCambios() {
        String nombre = editTextNombre.getText().toString().trim();
        String ingredientes = editTextIngredientes.getText().toString().trim();
        String pasos = editTextPasos.getText().toString().trim();
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        if (nombre.isEmpty() || ingredientes.isEmpty() || pasos.isEmpty()) {
            Toast.makeText(this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        if (imageChanged && imageUri != null) {
            // Subir nueva imagen
            StorageReference storageRef = FirebaseStorage.getInstance().getReference();
            StorageReference imageRef = storageRef.child("recetas_personalizadas/" + userId + "/" + UUID.randomUUID().toString());

            imageRef.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot ->
                            imageRef.getDownloadUrl().addOnSuccessListener(uri ->
                                    actualizarRecetaEnFirestore(nombre, ingredientes, pasos, uri.toString())
                            )
                    )
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Error al subir la imagen", Toast.LENGTH_SHORT).show();
                    });
        } else {
            // Mantener la misma imagen
            actualizarRecetaEnFirestore(nombre, ingredientes, pasos, currentImageUrl);
        }
    }

    private void actualizarRecetaEnFirestore(String nombre, String ingredientes, String pasos, String imageUrl) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("nombre", nombre);
        updates.put("ingredientes", ingredientes);
        updates.put("pasos", pasos);
        updates.put("imagenUrl", imageUrl);

        FirebaseFirestore.getInstance()
                .collection("recetas_personalizadas")
                .document(recipeId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Receta actualizada correctamente", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al actualizar la receta", Toast.LENGTH_SHORT).show();
                });
    }

    private void eliminarReceta() {
        FirebaseFirestore.getInstance()
                .collection("recetas_personalizadas")
                .document(recipeId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Receta eliminada correctamente", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al eliminar la receta", Toast.LENGTH_SHORT).show();
                });
    }
}