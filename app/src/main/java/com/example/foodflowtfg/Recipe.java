package com.example.foodflowtfg;

public class Recipe {
    private String id;
    private String nombre;
    private String imagenUrl;
    private String ingredientes;
    private String pasos;

    // Constructor vacío (obligatorio para Firestore)
    public Recipe() {}

    // Getters
    public String getNombre() { return nombre; }
    public String getImagenUrl() { return imagenUrl; }
    public String getIngredientes() { return ingredientes; }
    public String getPasos() { return pasos; }
    public String getId() {return id;}
    public void setId(String id) {this.id = id;}
}
