package com.example.foodflowtfg;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class ChefAIActivity extends AppCompatActivity {

    private RecyclerView chatRecyclerView;
    private EditText messageEditText;
    private ImageButton sendButton;
    private List<Message> messageList;
    private ChatAdapter chatAdapter;
    private LinearLayoutManager layoutManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chef_aiactivity);

        chatRecyclerView = findViewById(R.id.chatRecyclerView);
        messageEditText = findViewById(R.id.messageEditText);
        sendButton = findViewById(R.id.sendButton);

        messageList = new ArrayList<>();
        chatAdapter = new ChatAdapter(messageList);
        loadMessages();
        layoutManager = new LinearLayoutManager(this);

        layoutManager.setStackFromEnd(true);
        chatRecyclerView.setLayoutManager(layoutManager);

        chatRecyclerView.setAdapter(chatAdapter);

        sendButton.setOnClickListener(v -> {
            String text = messageEditText.getText().toString().trim();
            if (!text.isEmpty()) {
                addMessage(new Message(text, true));
                messageEditText.setText("");

                // Aquí llamas a la API de Gemini
                getGeminiResponse(text);
            }
        });
    }
    @Override
    protected void onPause() {
        super.onPause();
        saveMessages(); // Guarda el chat al salir o pausar la app
    }

    private void addMessage(Message message) {
        // Agrega el nuevo mensaje a la lista
        messageList.add(message);
        chatAdapter.notifyItemInserted(messageList.size() - 1);

        // Asegúrate de que layoutManager esté inicializado
        LinearLayoutManager layoutManager = (LinearLayoutManager) chatRecyclerView.getLayoutManager();

        // Solo hace scroll si el usuario está cerca del final de la lista
        if (layoutManager != null) {
            int lastVisibleItem = layoutManager.findLastCompletelyVisibleItemPosition();
            if (lastVisibleItem == messageList.size() - 2 || lastVisibleItem == messageList.size() - 1) {
                chatRecyclerView.scrollToPosition(messageList.size() - 1);
            }
        }
    }



    private void getGeminiResponse(String userInput) {
        new Thread(() -> {
            try {
                String apiKey = "AIzaSyA6c2dNuktVNNLZ0wA0G0o6cUhQyTIBHt4";
                String modelName = "gemini-2.0-flash";
                String endpoint = "https://generativelanguage.googleapis.com/v1beta/models/" + modelName + ":generateContent?key=" + apiKey;

                URL url = new URL(endpoint);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                // Construir JSON
                String systemPrompt = "Eres ChefAI, un chef experto, divertido y cercano. Responde con entusiasmo, usa emojis y habla como si fueras un amigo que adora cocinar. Da consejos útiles y haz que cocinar parezca fácil y divertido.";

                String jsonInput = "{ \"contents\": [ " +
                        "{ \"role\": \"user\", \"parts\": [ { \"text\": \"" + systemPrompt + "\" } ] }, " +
                        "{ \"role\": \"user\", \"parts\": [ { \"text\": \"" + userInput + "\" } ] } " +
                        "] }";

                // Enviar datos
                try (OutputStream os = conn.getOutputStream()) {
                    byte[] input = jsonInput.getBytes("utf-8");
                    os.write(input, 0, input.length);
                }

                // Leer respuesta
                int code = conn.getResponseCode();
                InputStream inputStream = (code >= 200 && code < 300) ? conn.getInputStream() : conn.getErrorStream();

                BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, "utf-8"));
                StringBuilder response = new StringBuilder();
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }

                String responseText = parseGeminiResponse(response.toString());

                // Actualizar UI con la respuesta
                runOnUiThread(() -> addMessage(new Message(responseText, false)));

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> addMessage(new Message("Error al contactar con la IA.", false)));
            }
        }).start();
    }
    private String parseGeminiResponse(String json) {
        try {
            JSONObject root = new JSONObject(json);
            JSONArray candidates = root.getJSONArray("candidates");
            if (candidates.length() > 0) {
                JSONObject firstCandidate = candidates.getJSONObject(0);
                JSONObject content = firstCandidate.getJSONObject("content");
                JSONArray parts = content.getJSONArray("parts");
                if (parts.length() > 0) {
                    JSONObject firstPart = parts.getJSONObject(0);
                    return firstPart.getString("text");
                }
            }
            return "No se encontró respuesta válida.";
        } catch (JSONException e) {
            e.printStackTrace();
            return "Error al procesar la respuesta.";
        }
    }
    private void saveMessages() {
        SharedPreferences prefs = getSharedPreferences("chat_prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        // Limitar los mensajes a los últimos 50
        if (messageList.size() > 3) {
            messageList.remove(0);  // Elimina el primer mensaje
        }

        JSONArray jsonArray = new JSONArray();
        for (Message msg : messageList) {
            JSONObject obj = new JSONObject();
            try {
                obj.put("text", msg.getText());
                obj.put("isUser", msg.isUser());
                jsonArray.put(obj);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        editor.putString("chat_history", jsonArray.toString());
        editor.apply();
    }

    private void loadMessages() {
        SharedPreferences prefs = getSharedPreferences("chat_prefs", MODE_PRIVATE);
        String json = prefs.getString("chat_history", null);

        if (json != null) {
            try {
                JSONArray jsonArray = new JSONArray(json);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject obj = jsonArray.getJSONObject(i);
                    String text = obj.getString("text");
                    boolean isUser = obj.getBoolean("isUser");
                    messageList.add(new Message(text, isUser));
                }
                chatAdapter.notifyDataSetChanged();
                chatRecyclerView.scrollToPosition(messageList.size() - 1);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

}
