package com.zbadev.emotizone;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PerfilFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PerfilFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private ImageView profileImage;
    private TextView userEmail;
    private TextView userName;
    private TextView userDate;
    private TextView userConnections;
    private Button configureButton;

    public PerfilFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment PerfilFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static PerfilFragment newInstance(String param1, String param2) {
        PerfilFragment fragment = new PerfilFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        //return inflater.inflate(R.layout.fragment_perfil, container, false);
        View view = inflater.inflate(R.layout.fragment_perfil, container, false);
        profileImage = view.findViewById(R.id.profile_image);
        userEmail = view.findViewById(R.id.user_email);
        userName = view.findViewById(R.id.user_name);
        userDate = view.findViewById(R.id.user_date);
        userConnections = view.findViewById(R.id.user_connections);
        configureButton = view.findViewById(R.id.configure_button);

        // Obtener los datos del usuario actual
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        updateUI(currentUser);
        countUserConnections(currentUser);

        // Establecer la fecha actual
        setCurrentDate();

        configureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Implementar la acción al hacer clic en el botón
            }
        });

        return view;
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            // Obtener el correo del usuario
            String email = user.getEmail();
            userEmail.setText(email != null ? email : "Email no disponible");

            // Obtener el nombre del usuario
            String name = user.getDisplayName();
            userName.setText(name != null ? name : "Nombre no disponible");

            // Obtener la foto de perfil del usuario
            Uri photoUrl = user.getPhotoUrl();
            if (photoUrl != null) {
                Glide.with(this).load(photoUrl).into(profileImage);
            } else {
                profileImage.setImageResource(R.drawable.man_user_circle_icon); // Imagen por defecto
            }
        }
    }

    private void setCurrentDate() {
        String currentDate = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());
        userDate.setText(currentDate);
    }

    private void countUserConnections(FirebaseUser user) {
        if (user != null) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("emotionalStates")
                    .whereEqualTo("userEmail", user.getEmail())
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            QuerySnapshot querySnapshot = task.getResult();
                            if (querySnapshot != null) {
                                int count = querySnapshot.size();
                                userConnections.setText(String.valueOf(count));
                            } else {
                                userConnections.setText("0");
                            }
                        } else {
                            userConnections.setText("Error al obtener conexiones");
                        }
                    });
        } else {
            userConnections.setText("Usuario no autenticado");
        }
    }
}