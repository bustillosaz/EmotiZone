package com.zbadev.emotizone;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

// Importaciones necesarias para el funcionamiento de la aplicación
import static android.content.ContentValues.TAG; // Etiqueta para los logs
import androidx.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.content.Intent;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class RegisterActivity extends AppCompatActivity {

    // Declaración de variables para los elementos de la interfaz de usuario y FirebaseAuth
    private FirebaseAuth auth;
    private EditText signupEmail, signupPassword, SignupConfirmPassword;
    private Button signupButton;
    private TextView LoginRedirectText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Llamada a los métodos para redireccionar al login y registrar un usuario
        Redirect();
        Register();
    }

    // Método para redireccionar al login
    void Redirect() {
        // Encontrar el TextView por su ID
        LoginRedirectText = findViewById(R.id.RedirectLogin);
        // Configurar un listener de clic para el TextView
        LoginRedirectText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Crear una intención para ir a la actividad de login
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                // Iniciar la actividad de login
                startActivity(intent);
                // Finalizar la actividad actual
                finish();
            }
        });
    }

    // Método para registrar un usuario con la confirmación de la contraseña
    void Register() {
        try {
            // Inicializar la instancia de FirebaseAuth
            auth = FirebaseAuth.getInstance();
            // Encontrar los elementos de la interfaz de usuario por sus ID
            signupEmail = findViewById(R.id.txt_reg_email);
            signupPassword = findViewById(R.id.txt_reg_pass);
            signupButton = findViewById(R.id.btn_registro);
            LoginRedirectText = findViewById(R.id.RedirectLogin);
            SignupConfirmPassword = findViewById(R.id.txt_reg_confirm_pass);

            // Configurar un listener de clic para el botón de registro
            signupButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Obtener los valores de los campos de texto
                    String pass = signupPassword.getText().toString().trim();
                    String user = signupEmail.getText().toString().trim();
                    String confirmPass = SignupConfirmPassword.getText().toString().trim();

                    // Validar que los campos no estén vacíos y que las contraseñas coincidan
                    if (user.isEmpty()) {
                        signupEmail.setError("El correo electrónico no puede estar vacío");
                        return;
                    }
                    if (pass.isEmpty()) {
                        signupPassword.setError("La contraseña no puede estar vacía");
                        return;
                    }
                    if (confirmPass.isEmpty()) {
                        SignupConfirmPassword.setError("Confirma tu contraseña");
                        return;
                    }
                    if (!pass.equals(confirmPass)) {
                        SignupConfirmPassword.setError("Las contraseñas no coinciden");
                        return;
                    }

                    // Crear un nuevo usuario con el email y la contraseña
                    auth.createUserWithEmailAndPassword(user, pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            // Si el registro es exitoso, mostrar un mensaje y redireccionar al login
                            if (task.isSuccessful()) {
                                Toast.makeText(RegisterActivity.this, "Registro Exitoso", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                            } else {
                                // Si el registro falla, mostrar un mensaje de error
                                Toast.makeText(RegisterActivity.this, "El registro falló: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                }
            });

            // Configurar un listener de clic para redireccionar al login
            LoginRedirectText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                }
            });

        } catch (Exception e) {
            // Mostrar un mensaje de error y registrar el error en los logs
            Toast.makeText(this, "Algo salió mal: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.e(TAG, "registro: Algo salió mal: " + e.getMessage());
            Log.w(TAG, "signInWithCredential:failure" + e.getMessage());
            e.printStackTrace();
        }
    }
}
