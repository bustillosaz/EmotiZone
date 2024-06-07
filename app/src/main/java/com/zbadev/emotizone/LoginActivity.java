package com.zbadev.emotizone;

import static android.content.ContentValues.TAG;  // Importación para el uso de la etiqueta de log

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import androidx.annotation.NonNull;

import android.content.Intent;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
//import android.widget.TextView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.AuthCredential;

/*import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;*/

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class LoginActivity extends AppCompatActivity {

    // Declaración de variables para los componentes de la UI y autenticación
    private EditText loginEmail, loginPassword;
    private TextView signupRedirectText;
    private Button loginButton, googleSignInButton;
    private FirebaseAuth auth;
    /*private GoogleSignInClient mGoogleSignInClient;*/
    private static final int RC_SIGN_IN = 9001;  // Código de solicitud para el inicio de sesión de Google

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initializeUI();
        Redirect();

    }

    // Método para inicializar los elementos de la interfaz de usuario
    private void initializeUI(){
        loginEmail = findViewById(R.id.txt_email);
        loginPassword = findViewById(R.id.txt_password);
        loginButton = findViewById(R.id.btn_ingresar);
        signupRedirectText = findViewById(R.id.RegisterRedirect);
        googleSignInButton = findViewById(R.id.btn_login_google);
        auth = FirebaseAuth.getInstance();  // Obtener instancia de autenticación de Firebase
    }
    void Redirect(){
        signupRedirectText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}