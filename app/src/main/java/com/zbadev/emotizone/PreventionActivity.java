package com.zbadev.emotizone;

import androidx.appcompat.app.AppCompatActivity;

import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.os.Bundle;

import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;



public class PreventionActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    List<DataClass> dataList;
    MyAdapter adapter;
    DataClass androidData;
    SearchView searchView;
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prevention);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        recyclerView = findViewById(R.id.recyclerView);
        searchView = findViewById(R.id.search);
        searchView.clearFocus();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                searchList(newText);
                return true;
            }
        });
        GridLayoutManager gridLayoutManager = new GridLayoutManager(PreventionActivity.this, 1);
        recyclerView.setLayoutManager(gridLayoutManager);
        dataList = new ArrayList<>();
        androidData = new DataClass("Planifica tu tiempo", R.string.detail_planifica, "Tip 1", R.drawable.planifica_detail);
        dataList.add(androidData);
        androidData = new DataClass("Haz ejercicios", R.string.detail_excercise, "Tip 2", R.drawable.exercise_detail);
        dataList.add(androidData);
        androidData = new DataClass("Medita y respira", R.string.detail_relax, "Tip 3", R.drawable.relax_detail);
        dataList.add(androidData);
        androidData = new DataClass("Alimentate saludable", R.string.detail_eat, "Tip 4", R.drawable.eat_detail);
        dataList.add(androidData);
        androidData = new DataClass("Duerme bien", R.string.detail_bed, "Tip 5", R.drawable.bed_detail);
        dataList.add(androidData);
        androidData = new DataClass("Manten contacto social", R.string.detail_social, "Tip 6", R.drawable.social_detail);
        dataList.add(androidData);
        adapter = new MyAdapter(PreventionActivity.this, dataList);
        recyclerView.setAdapter(adapter);
    }

    private void searchList(String text){
        List<DataClass> dataSearchList = new ArrayList<>();
        for (DataClass data : dataList){
            if (data.getDataTitle().toLowerCase().contains(text.toLowerCase())) {
                dataSearchList.add(data);
            }
        }
        if (dataSearchList.isEmpty()){
            Toast.makeText(this, "Not Found", Toast.LENGTH_SHORT).show();
        } else {
            adapter.setSearchList(dataSearchList);
        }
    }
}