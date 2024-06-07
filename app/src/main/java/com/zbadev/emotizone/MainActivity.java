package com.zbadev.emotizone;

// Importaciones necesarias para manejar componentes de UI y fragmentos

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
import android.widget.Toast;

// Importaciones necesarias para componentes de Material Design
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.navigation.NavigationView;

// La actividad principal que implementa la interfaz NavigationView.OnNavigationItemSelectedListener
public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    // Declaración de variables para los componentes de la interfaz de usuario
    DrawerLayout drawerLayout;
    BottomNavigationView bottomNavigationView;
    FragmentManager fragmentManager;
    Toolbar toolbar;
    FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
            clearBottomNavigationSelection();
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
}
