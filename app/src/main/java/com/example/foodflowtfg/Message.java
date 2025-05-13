package com.example.foodflowtfg;

public class Message {
    private String text;
    private boolean isUser; // true si es del usuario, false si es de Gemini

    public Message(String text, boolean isUser) {
        this.text = text;
        this.isUser = isUser;
    }

    public String getText() {
        return text;
    }

    public boolean isUser() {
        return isUser;
    }
}
