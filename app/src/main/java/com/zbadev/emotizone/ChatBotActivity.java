package com.zbadev.emotizone;

import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.Bundle;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
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

import android.graphics.PorterDuff;

public class ChatBotActivity extends AppCompatActivity {

    // Declaración de variables de UI y lógica
    private TextInputEditText queryEditText;
    private ImageView sendQuery, appIcon;
    FloatingActionButton btnShowDialog;
    private ProgressBar progressBar;
    private LinearLayout chatResponse;
    private ChatFutures chatModel;
    Dialog dialog;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatbot);

        // Inicializar FirebaseAuth
        auth = FirebaseAuth.getInstance();

        // Crear e inicializar el diálogo
        dialog = new Dialog(this);
        dialog.setContentView(R.layout.message_dialog);

        // Configurar el fondo del diálogo como transparente
        if(dialog.getWindow() != null){
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        // Permitir que el diálogo se cierre al tocar fuera de él y con el botón atrás
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);

        // Inicializar componentes de la UI del diálogo
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

        // Configurar el botón de enviar consulta
        sendQuery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Ocultar el diálogo y mostrar la barra de progreso
                dialog.dismiss();
                progressBar.setVisibility(View.VISIBLE);
                appIcon.setVisibility(View.GONE);
                String query = queryEditText.getText().toString();

                // Limpiar el campo de texto
                queryEditText.setText("");

                // Cambiar el color del icono del logo
                Drawable logoIcon = getResources().getDrawable(R.drawable.emotizone_logo);
                logoIcon.setColorFilter(Color.parseColor("#0367fb"), PorterDuff.Mode.SRC_IN);

                // Obtener el usuario actual de Firebase
                FirebaseUser currentUser = auth.getCurrentUser();
                if (currentUser != null) {
                    Uri photoUrl = currentUser.getPhotoUrl();
                    if (photoUrl != null) {
                        // Cargar la foto de perfil del usuario usando Glide
                        Glide.with(ChatBotActivity.this).load(photoUrl).into(appIcon);
                        Glide.with(ChatBotActivity.this)
                                .load(photoUrl)
                                .into(new com.bumptech.glide.request.target.CustomTarget<Drawable>() {
                                    @Override
                                    public void onResourceReady(Drawable resource, com.bumptech.glide.request.transition.Transition<? super Drawable> transition) {
                                        chatBody("Tu", query, resource);
                                    }

                                    @Override
                                    public void onLoadCleared(Drawable placeholder) {
                                        // Opcional: manejar placeholder
                                    }
                                });
                    } else {
                        // Mostrar una imagen por defecto si el usuario no tiene foto de perfil
                        appIcon.setImageResource(R.drawable.man_user_circle_icon);
                        chatBody("Tu", query, getResources().getDrawable(R.drawable.man_user_circle_icon));
                    }
                }

                // Obtener la respuesta del modelo de chat
                GeminiResp.getResponse(chatModel, query, new ResponseCallback() {
                    @Override
                    public void onResponse(String response) {
                        progressBar.setVisibility(View.GONE);
                        chatBody("EmotiiZoneIA", response, logoIcon);
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        chatBody("EmotiiZoneIA", "Please try again.", logoIcon);
                        progressBar.setVisibility(View.GONE);
                    }
                });
            }
        });
    }

    // Método para obtener el modelo de chat
    private ChatFutures getChatModel(){
        GeminiResp model = new GeminiResp();
        GenerativeModelFutures modelFutures = model.getModel(this);

        return modelFutures.startChat();
    }

    // Método para agregar un mensaje al chat
    private void chatBody(String UserName, String query, Drawable image) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.chat_message, null);

        // Inicializar componentes del mensaje
        TextView name = view.findViewById(R.id.name);
        TextView message = view.findViewById(R.id.agentMessage);
        ImageView logo = view.findViewById(R.id.logo);

        // Establecer los valores de los componentes
        name.setText(UserName);
        message.setText(query);
        logo.setImageDrawable(image);

        // Agregar el mensaje al contenedor de respuestas
        chatResponse.addView(view);
        ScrollView scrollView = findViewById(R.id.scrollView);
        scrollView.post(() -> scrollView.fullScroll(View.FOCUS_BACKWARD));
    }
}
