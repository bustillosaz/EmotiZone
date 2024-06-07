package com.zbadev.emotizone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    DrawerLayout drawerLayout;
    BottomNavigationView bottomNavigationView;
    FragmentManager fragmentManager;
    Toolbar toolbar;
    FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fab = findViewById(R.id.fab);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle=new ActionBarDrawerToggle(this, drawerLayout,toolbar,R.string.navigation_drawer_open,R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView=findViewById(R.id.navigation_drawer);
        navigationView.setNavigationItemSelectedListener(this);

        bottomNavigationView=findViewById(R.id.bottom_navigation);
        bottomNavigationView.setBackground(null);


        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                //handleNavigation(item.getItemId());
                int itemId=item.getItemId();
                if (itemId==R.id.bottom_home){
                    openFragment(new HomeFragment());
                    navigationView.setCheckedItem(R.id.nav_home);
                    return true;
                }else if (itemId==R.id.bottom_modules){
                    openFragment(new ModulesFragment());
                    navigationView.setCheckedItem(R.id.nav_modules);
                    return true;
                }else if (itemId==R.id.bottom_calendar){
                    openFragment(new CalendarFragment());
                    navigationView.setCheckedItem(R.id.nav_calendar);
                    return true;
                }else if (itemId==R.id.bottom_me){
                    openFragment(new PerfilFragment());
                    navigationView.setCheckedItem(R.id.nav_me);
                    return true;
                }

                return true;
            }
        });

        fragmentManager=getSupportFragmentManager();
        openFragment(new HomeFragment());

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Upload Video", Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        //handleNavigation(item.getItemId());
        int itenId=item.getItemId();
        if(itenId == R.id.nav_home){
            openFragment(new HomeFragment());
            bottomNavigationView.setSelectedItemId(R.id.bottom_home);
        }else if(itenId == R.id.nav_modules){
            openFragment(new ModulesFragment());
            bottomNavigationView.setSelectedItemId(R.id.bottom_modules);
        }else if(itenId == R.id.nav_calendar){
            openFragment(new CalendarFragment());
            bottomNavigationView.setSelectedItemId(R.id.bottom_calendar);
        }else if(itenId == R.id.nav_me){
            openFragment(new PerfilFragment());
            bottomNavigationView.setSelectedItemId(R.id.bottom_me);
        } else if(itenId == R.id.nav_chatbot){
            Toast.makeText(this, "Chatbot", Toast.LENGTH_SHORT).show();
            clearBottomNavigationSelection();
        }else if(itenId == R.id.nav_stadistics){
            Toast.makeText(this, "Estadisticas", Toast.LENGTH_SHORT).show();
            clearBottomNavigationSelection();
        }else if(itenId == R.id.nav_logout){
            Toast.makeText(this, "Cerrar sesion", Toast.LENGTH_SHORT).show();
            clearBottomNavigationSelection();
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)){
            drawerLayout.closeDrawer(GravityCompat.START);
        }else {
            super.onBackPressed();
        }
    }

    private void openFragment(Fragment fragment){
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void clearBottomNavigationSelection() {
        bottomNavigationView.getMenu().setGroupCheckable(0, true, false);
        for (int i = 0; i < bottomNavigationView.getMenu().size(); i++) {
            bottomNavigationView.getMenu().getItem(i).setChecked(false);
        }
        bottomNavigationView.getMenu().setGroupCheckable(0, true, true);
    }

}