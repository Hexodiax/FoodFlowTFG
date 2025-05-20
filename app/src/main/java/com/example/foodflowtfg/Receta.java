package com.example.foodflowtfg;

public class Receta {
    private String nombre;
    private String imagenUrl;
    private String ingredientes;
    private String pasos;

    // Constructor vacío (obligatorio para Firestore)
    public Receta() {}

    // Getters
    public String getNombre() { return nombre; }
    public String getImagenUrl() { return imagenUrl; }
    public String getIngredientes() { return ingredientes; }
    public String getPasos() { return pasos; }
}
