package com.zbadev.emotizone;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.ai.client.generativeai.java.ChatFutures;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class ChatBotActivity extends AppCompatActivity {

    // Declaración de variables de la interfaz de usuario y lógica
    private TextInputEditText queryEditText;
    private ImageView sendQuery, appIcon;
    private FloatingActionButton btnShowDialog;
    private ProgressBar progressBar;
    private LinearLayout chatResponse;
    private ChatFutures chatModel;
    private Dialog dialog;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private String detectedEmotion = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatbot);

        // Inicializar FirebaseAuth y FirebaseFirestore
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Crear e inicializar el diálogo
        dialog = new Dialog(this);
        dialog.setContentView(R.layout.message_dialog);

        // Configurar el fondo del diálogo como transparente
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        // Permitir que el diálogo se cierre al tocar fuera o presionar el botón de retroceso
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);

        // Inicializar componentes de la interfaz de usuario del diálogo
        sendQuery = dialog.findViewById(R.id.sendMesage);
        queryEditText = dialog.findViewById(R.id.queryEditText);

        // Inicializar componentes de la actividad
        btnShowDialog = findViewById(R.id.showMessageDIalog);
        progressBar = findViewById(R.id.progressBar);
        chatResponse = findViewById(R.id.chatResponse);
        appIcon = findViewById(R.id.appIcon);

        // Obtener el modelo de chat
        chatModel = getChatModel();

        // Configurar el botón para mostrar el diálogo
        btnShowDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.show();
            }
        });

        // Configurar el botón de enviar mensaje
        sendQuery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String query = queryEditText.getText().toString();
                if (!query.isEmpty()) {
                    sendAutomaticMessage(query);
                    queryEditText.setText("");
                }
            }
        });

        // Obtener el estado emocional del usuario actual y enviar un mensaje automático
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            String userEmail = currentUser.getEmail();
            fetchEmotionalStateAndSendMessage(userEmail);
        } else {
            // Manejar el caso donde el usuario no ha iniciado sesión
            // Puede redirigir a la pantalla de inicio de sesión o manejarlo de acuerdo a sus necesidades
        }
    }

    // Método para obtener el modelo de chat
    private ChatFutures getChatModel() {
        GeminiResp model = new GeminiResp();
        GenerativeModelFutures modelFutures = model.getModel(this);
        return modelFutures.startChat();
    }

    // Método para agregar un mensaje al chat
    private void chatBody(String userName, String query, Drawable image) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.chat_message, null);

        // Inicializar componentes del mensaje
        TextView name = view.findViewById(R.id.name);
        TextView message = view.findViewById(R.id.agentMessage);
        ImageView logo = view.findViewById(R.id.logo);

        // Establecer valores de los componentes
        name.setText(userName);
        message.setText(query);
        logo.setImageDrawable(image);

        // Agregar mensaje al contenedor de respuestas
        chatResponse.addView(view);
        ScrollView scrollView = findViewById(R.id.scrollView);
        scrollView.post(() -> scrollView.fullScroll(View.FOCUS_DOWN));
    }

    // Método para obtener el estado emocional desde Firestore y enviar un mensaje automático
    private void fetchEmotionalStateAndSendMessage(String userEmail) {
        db.collection("emotionalStates")
                .whereEqualTo("userEmail", userEmail)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            detectedEmotion = document.getString("emotionalState");
                            break; // Solo queremos el primer resultado
                        }
                        if (detectedEmotion != null && !detectedEmotion.isEmpty()) {
                            // Enviar mensaje inicial con el contexto emocional
                            sendAutomaticMessage("Hoy estoy con un estado de " + detectedEmotion);
                        } else {
                            // El usuario debe iniciar el chat sin contexto emocional
                            // Opcionalmente, mostrar un mensaje para iniciar el chat
                        }
                    } else {
                        // Manejar errores
                        Log.e("ChatBotActivity", "Error al obtener el estado emocional: ", task.getException());
                    }
                });
    }

    // Método para enviar un mensaje automático basado en el contexto emocional
    private void sendAutomaticMessage(String query) {
        dialog.dismiss();
        progressBar.setVisibility(View.VISIBLE);
        appIcon.setVisibility(View.GONE);

        // Cambiar color del icono del logo
        Drawable logoIcon = getResources().getDrawable(R.drawable.emotizone_logo);
        logoIcon.setColorFilter(Color.parseColor("#0367fb"), PorterDuff.Mode.SRC_IN);

        // Obtener el usuario actual desde Firebase
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            Uri photoUrl = currentUser.getPhotoUrl();
            if (photoUrl != null) {
                // Cargar foto de perfil del usuario usando Glide
                Glide.with(ChatBotActivity.this).load(photoUrl).into(appIcon);
                Glide.with(ChatBotActivity.this)
                        .load(photoUrl)
                        .into(new com.bumptech.glide.request.target.CustomTarget<Drawable>() {
                            @Override
                            public void onResourceReady(Drawable resource, com.bumptech.glide.request.transition.Transition<? super Drawable> transition) {
                                chatBody("Tu", query, resource);
                                requestChatResponse(query, logoIcon);
                            }

                            @Override
                            public void onLoadCleared(Drawable placeholder) {
                                // Opcional: manejar el marcador de posición
                            }
                        });
            } else {
                // Mostrar imagen predeterminada si el usuario no tiene foto de perfil
                appIcon.setImageResource(R.drawable.man_user_circle_icon);
                chatBody("Tu", query, getResources().getDrawable(R.drawable.man_user_circle_icon));
                requestChatResponse(query, logoIcon);
            }
        }
    }

    // Método para solicitar una respuesta de chat al modelo de chat
    private void requestChatResponse(String query, Drawable logoIcon) {
        // Obtener respuesta del modelo de chat
        GeminiResp.getResponse(chatModel, query, new ResponseCallback() {
            @Override
            public void onResponse(String response) {
                progressBar.setVisibility(View.GONE);
                chatBody("EmotiiZoneIA", response, logoIcon);
            }

            @Override
            public void onError(Throwable throwable) {
                chatBody("EmotiiZoneIA", "Por favor, inténtalo de nuevo. Error: " + throwable.getMessage(), logoIcon);
                progressBar.setVisibility(View.GONE);
            }
        });
    }
}
