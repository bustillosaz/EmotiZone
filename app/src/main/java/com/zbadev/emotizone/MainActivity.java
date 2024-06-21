package com.zbadev.emotizone;

// Importaciones necesarias para manejar componentes de UI y fragmentos

import static androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG;
import static androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL;

// Anotación para parámetros que no deben ser nulos
import androidx.annotation.NonNull;
// Para manejar el toggle del DrawerLayout
import androidx.appcompat.app.ActionBarDrawerToggle;
// Para manejar actividades que utilizan la compatibilidad con ActionBar
import androidx.appcompat.app.AppCompatActivity;
// Para manejar la barra de herramientas (Toolbar)
import androidx.appcompat.widget.Toolbar;
// Para manejar compatibilidad con vistas
import androidx.core.view.GravityCompat;
// Para manejar el DrawerLayout
import androidx.drawerlayout.widget.DrawerLayout;
// Para manejar fragmentos
import androidx.fragment.app.Fragment;
// Para manejar el FragmentManager
import androidx.fragment.app.FragmentManager;
// Para manejar transacciones de fragmentos
import androidx.fragment.app.FragmentTransaction;

// Importaciones necesarias para manejo de vistas y componentes
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

// Importaciones necesarias para componentes de Material Design
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.navigation.NavigationView;

//otro
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.widget.Button;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.concurrent.Executor;

// La actividad principal que implementa la interfaz NavigationView.OnNavigationItemSelectedListener
public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    // Declaración de variables para los componentes de la interfaz de usuario
    DrawerLayout drawerLayout;
    BottomNavigationView bottomNavigationView;
    FragmentManager fragmentManager;
    Toolbar toolbar;
    FloatingActionButton fab;
    View header;
    ImageView navImage;
    TextView navNombre, navCorreo;

    // Declaración de variables
    private FirebaseFirestore db;
    private Button logoutButton; // Botón para cerrar sesión
    private FirebaseAuth auth; // Instancia de FirebaseAuth para gestionar la autenticación
    private GoogleSignInClient mGoogleSignInClient; // Cliente de inicio de sesión con Google
    private TextView userEmail; // TextView para mostrar el correo del usuario
    private ImageView userProfilePic; // ImageView para mostrar la foto de perfil del usuario
    private Executor executor; // Executor para manejar tareas de autenticación biométrica en el hilo principal
    private BiometricPrompt biometricPrompt; // Prompt para la autenticación biométrica
    private BiometricPrompt.PromptInfo promptInfo; // Información del prompt de autenticación biométrica
    private static final int REQUEST_CODE = 101010; // Código de solicitud para el enroll biométrico
    private static final String TAG = "MainActivity"; // Tag para logs de depuración
    private SharedPreferences sharedPreferences; // Preferencias compartidas para almacenar datos persistentes
    private Handler inactivityHandler; // Manejador para gestionar la inactividad del usuario
    private Runnable inactivityRunnable; // Runnable que se ejecuta tras un período de inactividad
    private static final long INACTIVITY_TIMEOUT = 5 * 60 * 1000; // Tiempo de inactividad en milisegundos (5 minutos)
    private boolean isAppInBackground = false; // Bandera para verificar si la app está en segundo plano

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inicialización de Firebase
        FirebaseApp.initializeApp(this);
        db = FirebaseFirestore.getInstance();

        // Inicializar el botón flotante (FloatingActionButton)
        fab = findViewById(R.id.fab);
        // Inicializar y configurar la barra de herramientas (Toolbar)
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Inicializar el DrawerLayout y configurar el toggle para abrir/cerrar el drawer
        drawerLayout = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Inicializar el NavigationView y establecer el listener para la selección de ítems
        NavigationView navigationView = findViewById(R.id.navigation_drawer);
        navigationView.setNavigationItemSelectedListener(this);

        // Inicializar el BottomNavigationView y establecer el listener para la selección de ítems
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setBackground(null);

        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                // Manejar la navegación cuando se selecciona un ítem en el BottomNavigationView
                int itemId = item.getItemId();
                if (itemId == R.id.bottom_home) {
                    openFragment(new HomeFragment());
                    navigationView.setCheckedItem(R.id.nav_home);
                    return true;
                } else if (itemId == R.id.bottom_modules) {
                    openFragment(new ModulesFragment());
                    navigationView.setCheckedItem(R.id.nav_modules);
                    return true;
                } else if (itemId == R.id.bottom_calendar) {
                    openFragment(new CalendarFragment());
                    navigationView.setCheckedItem(R.id.nav_calendar);
                    return true;
                } else if (itemId == R.id.bottom_me) {
                    openFragment(new PerfilFragment());
                    navigationView.setCheckedItem(R.id.nav_me);
                    return true;
                }

                return true;
            }
        });

        // Obtener el FragmentManager y abrir el fragmento inicial
        fragmentManager = getSupportFragmentManager();
        openFragment(new HomeFragment());

        // Establecer el listener para el botón flotante (FloatingActionButton)
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Mostrar el diálogo al hacer clic en el botón flotante
                showBottomDialog();
            }
        });

        //uno
        header = navigationView.getHeaderView(0);
        navImage = (ImageView) header.findViewById(R.id.nav_image);
        navNombre = (TextView) header.findViewById(R.id.nav_textnombre);
        navCorreo = (TextView) header.findViewById(R.id.nav_textcorreo);
        //uno

        ParaInicializar();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Manejar la selección de ítems en el NavigationView
        int itemId = item.getItemId();
        if (itemId == R.id.nav_home) {
            openFragment(new HomeFragment());
            bottomNavigationView.setSelectedItemId(R.id.bottom_home);
        } else if (itemId == R.id.nav_modules) {
            openFragment(new ModulesFragment());
            bottomNavigationView.setSelectedItemId(R.id.bottom_modules);
        } else if (itemId == R.id.nav_calendar) {
            openFragment(new CalendarFragment());
            bottomNavigationView.setSelectedItemId(R.id.bottom_calendar);
        } else if (itemId == R.id.nav_me) {
            openFragment(new PerfilFragment());
            bottomNavigationView.setSelectedItemId(R.id.bottom_me);
        } else if (itemId == R.id.nav_chatbot) {
            Toast.makeText(this, "Chatbot", Toast.LENGTH_SHORT).show();
            clearBottomNavigationSelection();
        } else if (itemId == R.id.nav_stadistics) {
            Toast.makeText(this, "Estadísticas", Toast.LENGTH_SHORT).show();
            clearBottomNavigationSelection();
        } else if (itemId == R.id.nav_logout) {
            Toast.makeText(this, "Cerrar sesión", Toast.LENGTH_SHORT).show();
            signOut();
            //clearBottomNavigationSelection();
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        // Manejar el comportamiento del botón de retroceso (back button)
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    private void openFragment(Fragment fragment) {
        // Método para abrir un fragmento
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void clearBottomNavigationSelection() {
        // Método para limpiar la selección en el BottomNavigationView
        bottomNavigationView.getMenu().setGroupCheckable(0, true, false);
        for (int i = 0; i < bottomNavigationView.getMenu().size(); i++) {
            bottomNavigationView.getMenu().getItem(i).setChecked(false);
        }
        bottomNavigationView.getMenu().setGroupCheckable(0, true, true);
    }

    private void showBottomDialog() {
        // Método para mostrar un diálogo desde la parte inferior de la pantalla
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.bottomsheet_layout);

        // Inicializar los elementos del diálogo
        LinearLayout ModuleOneLayout = dialog.findViewById(R.id.layout_module_one);
        LinearLayout ModuleTwoLayout = dialog.findViewById(R.id.layout_module_two);
        LinearLayout ModuleThreeLayout = dialog.findViewById(R.id.layout_module_three);
        LinearLayout ChatBotLayout = dialog.findViewById(R.id.layout_chatbot);
        ImageView cancelButton = dialog.findViewById(R.id.cancelButton);

        // Establecer listeners para los elementos del diálogo
        ModuleOneLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                Toast.makeText(MainActivity.this, "Module 1 is clicked", Toast.LENGTH_SHORT).show();
            }
        });

        ModuleTwoLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                Toast.makeText(MainActivity.this, "Module 2 is Clicked", Toast.LENGTH_SHORT).show();
            }
        });

        ModuleThreeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                Toast.makeText(MainActivity.this, "Module 3 is Clicked", Toast.LENGTH_SHORT).show();
            }
        });

        ChatBotLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                Toast.makeText(MainActivity.this, "ChatBot is Clicked", Toast.LENGTH_SHORT).show();
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        // Mostrar el diálogo y configurar sus atributos
        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        dialog.getWindow().setGravity(Gravity.BOTTOM);
    }

    private void ParaInicializar(){
        // Inicialización de variables y componentes de la interfaz
        auth = FirebaseAuth.getInstance();
        sharedPreferences = getSharedPreferences("EmotiizonePrefs", MODE_PRIVATE);

        // Inicialización del manejador de inactividad
        inactivityHandler = new Handler();
        inactivityRunnable = new Runnable() {
            @Override
            public void run() {
                if (auth.getCurrentUser() != null) {
                    // Si el usuario está autenticado, solicitar autenticación biométrica tras inactividad
                    authenticateUser();
                }
            }
        };

        // Verificar si hay un usuario autenticado
        if (auth.getCurrentUser() == null) {
            // Si no hay usuario autenticado, redirigir a la pantalla de inicio de sesión
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
        } else {
            // Si hay usuario autenticado, actualizar la interfaz de usuario
            updateUI(auth.getCurrentUser());
            // Reiniciar el temporizador de inactividad
            resetInactivityTimer();
        }

        // Configurar inicio de sesión con Google
        configureGoogleSignIn();
    }

    // Configuración de inicio de sesión con Google
    private void configureGoogleSignIn() {
        // Configuración de opciones de inicio de sesión con Google
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        // Inicialización del cliente de inicio de sesión con Google
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    // Método para cerrar sesión
    private void signOut() {
        // Cerrar sesión en Firebase
        auth.signOut();

        // Cerrar sesión en Google
        mGoogleSignInClient.signOut().addOnCompleteListener(this, task -> {
            // Mostrar mensaje de confirmación
            Toast.makeText(MainActivity.this, "Sesión cerrada", Toast.LENGTH_SHORT).show();
            // Actualizar preferencia de sesión iniciada
            sharedPreferences.edit().putBoolean("loggedIn", false).apply();
            // Redirigir a la pantalla de inicio de sesión
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
        });
    }

    // Actualizar la interfaz de usuario con la información del usuario
    private void updateUI(FirebaseUser user) {
        if (user != null) {
            // Obtener el correo electrónico del usuario
            String email = user.getEmail();
            String name = user.getDisplayName(); // Obtener el nombre del usuario
            navCorreo.setText(email);
            navNombre.setText(name); // Mostrar el nombre del usuario

            // Obtener la foto de perfil del usuario
            Uri photoUrl = user.getPhotoUrl();
            if (photoUrl != null) {
                // Cargar la foto de perfil usando Glide
                Glide.with(this).load(photoUrl).into(navImage);
            } else {
                // Establecer una imagen por defecto si no hay foto de perfil
                navImage.setImageResource(R.drawable.man_user_circle_icon);
            }
        }
    }

    // Método para autenticar al usuario usando biometría
    private void authenticateUser() {
        // Obtener instancia del administrador biométrico
        BiometricManager biometricManager = BiometricManager.from(this);
        // Verificar si se puede autenticar usando biometría
        switch (biometricManager.canAuthenticate(BIOMETRIC_STRONG | DEVICE_CREDENTIAL)) {
            case BiometricManager.BIOMETRIC_SUCCESS:
                // La autenticación biométrica está disponible
                Log.d(TAG, "La aplicación puede autenticarse mediante datos biométricos.");
                break;
            case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
                // No hay hardware biométrico disponible
                Toast.makeText(getApplicationContext(), "No hay hardware biométrico disponible", Toast.LENGTH_SHORT).show();
                // Redirigir a la pantalla de inicio de sesión
                LoginRedirect();
                break;
            case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
                // El hardware biométrico no está disponible actualmente
                Toast.makeText(getApplicationContext(), "Hardware biométrico no disponible actualmente", Toast.LENGTH_SHORT).show();
                // Redirigir a la pantalla de inicio de sesión
                LoginRedirect();
                break;
            case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                // No hay datos biométricos registrados
                final Intent enrollIntent = new Intent(Settings.ACTION_BIOMETRIC_ENROLL);
                enrollIntent.putExtra(Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED, BIOMETRIC_STRONG | DEVICE_CREDENTIAL);
                startActivityForResult(enrollIntent, REQUEST_CODE);
                Toast.makeText(getApplicationContext(), "No hay huellas digitales registradas", Toast.LENGTH_SHORT).show();
                // Redirigir a la pantalla de inicio de sesión
                LoginRedirect();
                break;
        }

        // Inicializar el executor para manejar las tareas de autenticación biométrica
        executor = ContextCompat.getMainExecutor(this);
        // Crear una instancia de BiometricPrompt
        biometricPrompt = new BiometricPrompt(MainActivity.this, executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                // Mostrar mensaje de error de autenticación
                Toast.makeText(getApplicationContext(), "Error de autenticación: " + errString, Toast.LENGTH_SHORT).show();
                // Redirigir a la pantalla de inicio de sesión
                LoginRedirect();
            }

            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                // Mostrar mensaje de autenticación exitosa
                Toast.makeText(getApplicationContext(), "Autenticación correcta!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                // Mostrar mensaje de autenticación fallida
                Toast.makeText(getApplicationContext(), "Autenticación fallida", Toast.LENGTH_SHORT).show();
                // Redirigir a la pantalla de inicio de sesión
                //LoginRedirect();
            }
        });

        // Configurar la información del prompt de autenticación biométrica
        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Inicio de sesión biométrico para EmotiiZone")
                .setSubtitle("Inicie sesión con su credencial biométrica")
                .setAllowedAuthenticators(BIOMETRIC_STRONG | DEVICE_CREDENTIAL)
                .build();

        // Mostrar el prompt de autenticación biométrica
        biometricPrompt.authenticate(promptInfo);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Si la aplicación estaba en segundo plano y el usuario está autenticado
        if (isAppInBackground) {
            if (auth.getCurrentUser() != null && sharedPreferences.getBoolean("loggedIn", false)) {
                // Solicitar autenticación biométrica
                authenticateUser();
            }
            // Marcar que la aplicación ya no está en segundo plano
            isAppInBackground = false;
        }
        // Reiniciar el temporizador de inactividad
        resetInactivityTimer();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Si el usuario está autenticado, marcar que la sesión está iniciada
        if (auth.getCurrentUser() != null) {
            sharedPreferences.edit().putBoolean("loggedIn", true).apply();
        }
        // Marcar que la aplicación está en segundo plano
        isAppInBackground = true;
        // Detener el temporizador de inactividad
        stopInactivityTimer();
    }

    // Método para redirigir a la pantalla de inicio de sesión y cerrar la sesión actual
    public void LoginRedirect() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        signOut();
        finish();
    }

    // Método para reiniciar el temporizador de inactividad
    private void resetInactivityTimer() {
        inactivityHandler.removeCallbacks(inactivityRunnable);
        inactivityHandler.postDelayed(inactivityRunnable, INACTIVITY_TIMEOUT);
    }

    // Método para detener el temporizador de inactividad
    private void stopInactivityTimer() {
        inactivityHandler.removeCallbacks(inactivityRunnable);
    }
}
