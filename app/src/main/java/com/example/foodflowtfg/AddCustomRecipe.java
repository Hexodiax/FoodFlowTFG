package com.example.foodflowtfg;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AddCustomRecipe extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int REQUEST_IMAGE_CAPTURE = 2;
    private static final int CAMERA_PERMISSION_REQUEST = 101;
    private static final String DEFAULT_IMAGE_URL = "https://firebasestorage.googleapis.com/v0/b/tu-proyecto.appspot.com/o/default_recipe_image.png?alt=media";

    private EditText editTextNombre, editTextIngredientes, editTextPasos;
    private ImageView imagePreview;
    private Uri imageUri;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_personalized_recipe);

        mAuth = FirebaseAuth.getInstance();

        editTextNombre = findViewById(R.id.editTextNombre);
        editTextIngredientes = findViewById(R.id.editTextIngredientes);
        editTextPasos = findViewById(R.id.editTextPasos);
        imagePreview = findViewById(R.id.imagePreview);

        Button btnSeleccionarImagen = findViewById(R.id.btnSeleccionarImagen);
        Button btnTomarFoto = findViewById(R.id.btnTomarFoto);
        Button btnGuardar = findViewById(R.id.btnGuardar);

        btnSeleccionarImagen.setOnClickListener(v -> abrirGaleria());
        btnTomarFoto.setOnClickListener(v -> verificarPermisosCamara());
        btnGuardar.setOnClickListener(v -> guardarReceta());
    }

    private void verificarPermisosCamara() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    CAMERA_PERMISSION_REQUEST);
        } else {
            abrirCamara();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                abrirCamara();
            } else {
                Toast.makeText(this, "Se necesitan permisos de cámara para tomar fotos", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void abrirCamara() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
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

        if (resultCode == RESULT_OK) {
            if (requestCode == PICK_IMAGE_REQUEST && data != null && data.getData() != null) {
                // Imagen de galería
                imageUri = data.getData();
                mostrarImagen();
            } else if (requestCode == REQUEST_IMAGE_CAPTURE && data != null) {
                // Foto de cámara
                Bundle extras = data.getExtras();
                Bitmap imageBitmap = (Bitmap) extras.get("data");
                imagePreview.setImageBitmap(imageBitmap);
                // Guardar el bitmap en un archivo temporal para obtener su URI
                imageUri = Utils.getImageUri(this, imageBitmap);
            }
        }
    }

    private void mostrarImagen() {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
            imagePreview.setImageBitmap(bitmap);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void guardarReceta() {
        String nombre = editTextNombre.getText().toString().trim();
        String ingredientes = editTextIngredientes.getText().toString().trim();
        String pasos = editTextPasos.getText().toString().trim();
        String userId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;

        if (userId == null) {
            Toast.makeText(this, "Debes iniciar sesión para guardar recetas", Toast.LENGTH_SHORT).show();
            return;
        }

        if (nombre.isEmpty() || ingredientes.isEmpty() || pasos.isEmpty()) {
            Toast.makeText(this, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        if (imageUri == null) {
            // No hay imagen, usar imagen por defecto
            guardarEnFirestore(userId, nombre, ingredientes, pasos, DEFAULT_IMAGE_URL);
        } else {
            // Subir imagen seleccionada
            StorageReference storageRef = FirebaseStorage.getInstance().getReference();
            StorageReference imageRef = storageRef.child("recetas_personalizadas/" + userId + "/" + UUID.randomUUID().toString());

            imageRef.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot ->
                            imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                                String imageUrl = uri.toString();
                                guardarEnFirestore(userId, nombre, ingredientes, pasos, imageUrl);
                            })
                    )
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Error al subir imagen", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    });
        }
    }

    private void guardarEnFirestore(String userId, String nombre, String ingredientes, String pasos, String imageUrl) {
        Map<String, Object> receta = new HashMap<>();
        receta.put("nombre", nombre);
        receta.put("ingredientes", ingredientes);
        receta.put("pasos", pasos);
        receta.put("imagenUrl", imageUrl);
        receta.put("userId", userId);

        FirebaseFirestore.getInstance()
                .collection("recetas_personalizadas")
                .add(receta)
                .addOnSuccessListener(documentReference -> {
                    String id = documentReference.getId();
                    // Actualizamos el campo "id" en el mismo documento
                    documentReference.update("id", id)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "Receta guardada correctamente", Toast.LENGTH_SHORT).show();
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Error al actualizar ID de la receta", Toast.LENGTH_SHORT).show();
                                e.printStackTrace();
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al guardar receta", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                });
    }

}