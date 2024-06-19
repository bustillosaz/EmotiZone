package com.zbadev.emotizone;

import androidx.appcompat.app.AppCompatActivity;

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

import com.google.ai.client.generativeai.java.ChatFutures;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import android.graphics.PorterDuff;


public class ChatBotActivity extends AppCompatActivity {

    private TextInputEditText queryEditText;
    private ImageView sendQuery, logo, appIcon;
    FloatingActionButton btnShowDialog;
    private ProgressBar progressBar;
    private LinearLayout chatResponse;
    private ChatFutures chatModel;
    Dialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatbot);

        // Inicializar BuildConfig con el contexto de la actividad
        //BuildConfig.initialize(this);

        //API_KEY = getString(R.string.openai_api_key_two); // Accede a la clave API desde secrets.xml
        dialog = new Dialog(this);
        dialog.setContentView(R.layout.message_dialog);

        if(dialog.getWindow() != null){
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.setCancelable(false);
        }

        dialog.setCancelable(true);  // Permite cerrar el diálogo presionando fuera de él y con el botón atrás
        dialog.setCanceledOnTouchOutside(true);  // Permite cerrar el diálogo presionando fuera de él

        sendQuery = dialog.findViewById(R.id.sendMesage);
        queryEditText = dialog.findViewById(R.id.queryEditText);

        btnShowDialog = findViewById(R.id.showMessageDIalog);
        progressBar = findViewById(R.id.progressBar);
        chatResponse = findViewById(R.id.chatResponse);
        appIcon = findViewById(R.id.appIcon);

        chatModel = getChatModel();

        btnShowDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.show();
            }
        });

        sendQuery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                progressBar.setVisibility(View.VISIBLE);
                appIcon.setVisibility(View.GONE);
                String query = queryEditText.getText().toString();

                queryEditText.setText("");

                // Cambiar el color del drawable
                Drawable logoIcon = getResources().getDrawable(R.drawable.emotizone_logo);
                logoIcon.setColorFilter(Color.parseColor("#0367fb"), PorterDuff.Mode.SRC_IN);

                chatBody("You", query, getDrawable(R.drawable.man_user_circle_icon));
                GeminiResp.getResponse(chatModel, query, new ResponseCallback() {
                    @Override
                    public void onResponse(String response) {
                        progressBar.setVisibility(View.GONE);
                        chatBody("AI", response, logoIcon);
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        chatBody("AI", "Please try again.", logoIcon);
                        progressBar.setVisibility(View.GONE);
                    }
                });
            }
        });
    }

    private ChatFutures getChatModel(){
        GeminiResp model = new GeminiResp();
        GenerativeModelFutures modelFutures = model.getModel(this);

        return modelFutures.startChat();
    }

    private void chatBody(String UserName, String query, Drawable image) {

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.chat_message, null);

        TextView name = view.findViewById(R.id.name);
        TextView message = view.findViewById(R.id.agentMessage);
        ImageView logo = view.findViewById(R.id.logo);

        name.setText(UserName);
        message.setText(query);
        logo.setImageDrawable(image);

        chatResponse.addView(view);
        ScrollView scrollView = findViewById(R.id.scrollView);
        scrollView.post(() -> scrollView.fullScroll(View.FOCUS_BACKWARD));

    }
}