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

    // UI and logic variables declaration
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

        // Initialize FirebaseAuth and FirebaseFirestore
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Create and initialize the dialog
        dialog = new Dialog(this);
        dialog.setContentView(R.layout.message_dialog);

        // Configure dialog background as transparent
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        // Allow dialog to be closed when touching outside or pressing back button
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);

        // Initialize dialog UI components
        sendQuery = dialog.findViewById(R.id.sendMesage);
        queryEditText = dialog.findViewById(R.id.queryEditText);

        // Initialize activity components
        btnShowDialog = findViewById(R.id.showMessageDIalog);
        progressBar = findViewById(R.id.progressBar);
        chatResponse = findViewById(R.id.chatResponse);
        appIcon = findViewById(R.id.appIcon);

        // Get the chat model
        chatModel = getChatModel();

        // Configure button to show dialog
        btnShowDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.show();
            }
        });

        // Configure send message button
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

        // Get emotional state of current user and send automatic message
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            String userEmail = currentUser.getEmail();
            fetchEmotionalStateAndSendMessage(userEmail);
        } else {
            // Handle case where user is not logged in
            // You may want to redirect to login screen or handle accordingly
        }
    }

    // Method to get the chat model
    private ChatFutures getChatModel() {
        GeminiResp model = new GeminiResp();
        GenerativeModelFutures modelFutures = model.getModel(this);
        return modelFutures.startChat();
    }

    // Method to add a message to the chat
    private void chatBody(String userName, String query, Drawable image) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.chat_message, null);

        // Initialize message components
        TextView name = view.findViewById(R.id.name);
        TextView message = view.findViewById(R.id.agentMessage);
        ImageView logo = view.findViewById(R.id.logo);

        // Set component values
        name.setText(userName);
        message.setText(query);
        logo.setImageDrawable(image);

        // Add message to response container
        chatResponse.addView(view);
        ScrollView scrollView = findViewById(R.id.scrollView);
        scrollView.post(() -> scrollView.fullScroll(View.FOCUS_DOWN));
    }

    // Method to fetch emotional state from Firestore and send automatic message
    private void fetchEmotionalStateAndSendMessage(String userEmail) {
        db.collection("emotionalStates")
                .whereEqualTo("userEmail", userEmail)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            detectedEmotion = document.getString("emotionalState");
                            break; // Only want the first result
                        }
                        if (detectedEmotion != null && !detectedEmotion.isEmpty()) {
                            // Send initial message with emotional context
                            sendAutomaticMessage("Hoy estoy con un estado de " + detectedEmotion);
                        } else {
                            // User must start the chat with no emotional context
                            // Optionally show a prompt to start the chat
                        }
                    } else {
                        // Handle errors
                        Log.e("ChatBotActivity", "Error fetching emotional state: ", task.getException());
                    }
                });
    }

    // Method to send automatic message based on emotional context
    private void sendAutomaticMessage(String query) {
        dialog.dismiss();
        progressBar.setVisibility(View.VISIBLE);
        appIcon.setVisibility(View.GONE);

        // Change logo icon color
        Drawable logoIcon = getResources().getDrawable(R.drawable.emotizone_logo);
        logoIcon.setColorFilter(Color.parseColor("#0367fb"), PorterDuff.Mode.SRC_IN);

        // Get current user from Firebase
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            Uri photoUrl = currentUser.getPhotoUrl();
            if (photoUrl != null) {
                // Load user profile photo using Glide
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
                                // Optional: handle placeholder
                            }
                        });
            } else {
                // Show default image if user does not have a profile photo
                appIcon.setImageResource(R.drawable.man_user_circle_icon);
                chatBody("Tu", query, getResources().getDrawable(R.drawable.man_user_circle_icon));
                requestChatResponse(query, logoIcon);
            }
        }
    }

    // Method to request chat response from the chat model
    private void requestChatResponse(String query, Drawable logoIcon) {
        // Get response from chat model
        GeminiResp.getResponse(chatModel, query, new ResponseCallback() {
            @Override
            public void onResponse(String response) {
                progressBar.setVisibility(View.GONE);
                chatBody("EmotiiZoneIA", response, logoIcon);
            }

            @Override
            public void onError(Throwable throwable) {
                chatBody("EmotiiZoneIA", "Please try again. Error: " + throwable.getMessage(), logoIcon);
                progressBar.setVisibility(View.GONE);
            }
        });
    }
}
