package com.zbadev.emotizone;

// Importación para el uso de la etiqueta de log
import static android.content.ContentValues.TAG;

// Importaciones necesarias para el funcionamiento de la aplicación
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import androidx.annotation.NonNull;

import android.content.Intent;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.AuthCredential;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class LoginActivity extends AppCompatActivity {

    // Declaración de variables para los componentes de la UI y autenticación
    private EditText loginEmail, loginPassword; // Campos de texto para email y contraseña
    private TextView signupRedirectText; // Texto para redireccionar al registro
    private Button loginButton, googleSignInButton; // Botones para iniciar sesión y para iniciar sesión con Google
    private FirebaseAuth auth; // Objeto para autenticación de Firebase
    private GoogleSignInClient mGoogleSignInClient; // Cliente de inicio de sesión de Google
    private static final int RC_SIGN_IN = 9001;  // Código de solicitud para el inicio de sesión de Google

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login); // Establece el diseño de la actividad

        initializeUI(); // Inicializa los elementos de la UI
        login(); // Configura el proceso de inicio de sesión
        configureGoogleSignIn(); // Configura el inicio de sesión de Google
        Redirect(); // Configura la redirección al registro
    }

    // Método para inicializar los elementos de la interfaz de usuario
    private void initializeUI(){
        loginEmail = findViewById(R.id.txt_email); // Encuentra el campo de texto del email por su ID
        loginPassword = findViewById(R.id.txt_password); // Encuentra el campo de texto de la contraseña por su ID
        loginButton = findViewById(R.id.btn_ingresar); // Encuentra el botón de inicio de sesión por su ID
        signupRedirectText = findViewById(R.id.RegisterRedirect); // Encuentra el texto de redirección al registro por su ID
        googleSignInButton = findViewById(R.id.btn_login_google); // Encuentra el botón de inicio de sesión con Google por su ID
        auth = FirebaseAuth.getInstance();  // Obtiene la instancia de autenticación de Firebase
    }

    // Método para configurar el proceso de inicio de sesión
    private void login(){
        loginButton.setOnClickListener(new View.OnClickListener() { // Configura el listener para el botón de inicio de sesión
            @Override
            public void onClick(View v) {
                String email = loginEmail.getText().toString().trim(); // Obtiene el email ingresado y elimina espacios en blanco
                String pass = loginPassword.getText().toString().trim(); // Obtiene la contraseña ingresada y elimina espacios en blanco

                Log.d(TAG, "Email entered: " + email); // Registra el email ingresado

                // Validación del email y la contraseña
                if (!email.isEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    if (!pass.isEmpty()) {
                        auth.signInWithEmailAndPassword(email, pass) // Intenta iniciar sesión con el email y contraseña
                                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                                    @Override
                                    public void onSuccess(AuthResult authResult) {
                                        Toast.makeText(LoginActivity.this, "Inicio de sesión exitoso", Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(LoginActivity.this, MainActivity.class)); // Redirige a MainActivity
                                        finish(); // Finaliza la actividad actual
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(LoginActivity.this, "Error de inicio de sesión: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        Log.e(TAG, "Login Failed: " + e.getMessage()); // Registra el error
                                    }
                                });
                    } else {
                        loginPassword.setError("No se permiten campos vacíos"); // Muestra error si la contraseña está vacía
                    }
                } else if (email.isEmpty()) {
                    loginEmail.setError("No se permiten campos vacíos"); // Muestra error si el email está vacío
                } else {
                    loginEmail.setError("Introduzca el correo electrónico correcto"); // Muestra error si el email es inválido
                }
            }
        });

        // Configura el listener para el botón de inicio de sesión con Google
        googleSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signInWithGoogle(); // Llama al método para iniciar sesión con Google
            }
        });

        // Configura el listener para el texto de redirección al registro
        signupRedirectText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class)); // Redirige a RegisterActivity
                finish(); // Finaliza la actividad actual
            }
        });
    }

    // Método para configurar el inicio de sesión de Google
    private void configureGoogleSignIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)) // Solicita el ID de token del cliente web
                .requestEmail() // Solicita el email del usuario
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso); // Obtiene el cliente de inicio de sesión de Google
    }

    // Método para iniciar sesión con Google
    private void signInWithGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent(); // Obtiene la intención de inicio de sesión de Google
        startActivityForResult(signInIntent, RC_SIGN_IN); // Inicia la actividad para el resultado con el código de solicitud RC_SIGN_IN
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Maneja el resultado del inicio de sesión de Google
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class); // Obtiene la cuenta de Google
                firebaseAuthWithGoogle(account); // Autentica con Firebase usando la cuenta de Google
            } catch (ApiException e) {
                Log.w(TAG, "Error al iniciar sesión en Google: ", e); // Registra el error
                Toast.makeText(this, "Error al iniciar sesión en Google:" + e.getMessage(), Toast.LENGTH_SHORT).show(); // Muestra un mensaje de error
            }
        }
    }

    // Método para autenticar con Firebase usando la cuenta de Google
    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null); // Obtiene las credenciales de autenticación
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = auth.getCurrentUser(); // Obtiene el usuario autenticado
                            Toast.makeText(LoginActivity.this, "Inicio de sesión de Google exitoso.", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(LoginActivity.this, MainActivity.class)); // Redirige a MainActivity
                            finish(); // Finaliza la actividad actual
                        } else {
                            Log.w(TAG, "signInWithCredential:failure", task.getException()); // Registra el error
                            Toast.makeText(LoginActivity.this, "Autenticación fallida.", Toast.LENGTH_SHORT).show(); // Muestra un mensaje de error
                        }
                    }
                });
    }

    // Método para configurar la redirección al registro
    void Redirect(){
        signupRedirectText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class); // Crea una intención para RegisterActivity
                startActivity(intent); // Inicia la actividad de registro
                finish(); // Finaliza la actividad actual
            }
        });
    }
}
