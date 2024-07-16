package com.zbadev.emotizone;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import org.checkerframework.common.subtyping.qual.Bottom;


public class GraphsActivity extends AppCompatActivity {

    Toolbar toolbar;
    Button barChart, pieChart, lineChart, scatterChart, stackedBarChart, askedBarChart, horizontalBarChart;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graphs);

        toolbar = findViewById(R.id.toolbars);
        setSupportActionBar(toolbar);

        barChart = findViewById(R.id.barchart);
        pieChart = findViewById(R.id.piechart);
        lineChart = findViewById(R.id.linechart);
        scatterChart = findViewById(R.id.scatterchart);
        stackedBarChart = findViewById(R.id.stackedbarchart);
        askedBarChart = findViewById(R.id.askedchart);
        horizontalBarChart = findViewById(R.id.horizontalchart);

        barChart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GraphsActivity.this, BarChartActivity.class); // Cambia "NuevaActividad" al nombre de tu nueva actividad
                startActivity(intent);
            }
        });

        stackedBarChart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GraphsActivity.this, StackedBarChartActivity.class); // Cambia "NuevaActividad" al nombre de tu nueva actividad
                startActivity(intent);
            }
        });

        pieChart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GraphsActivity.this, PieChartActivity.class); // Cambia "NuevaActividad" al nombre de tu nueva actividad
                startActivity(intent);
            }
        });

        lineChart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GraphsActivity.this, LineChartActivity.class); // Cambia "NuevaActividad" al nombre de tu nueva actividad
                startActivity(intent);
            }
        });

        scatterChart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GraphsActivity.this, CubicLinesChartActivity.class); // Cambia "NuevaActividad" al nombre de tu nueva actividad
                startActivity(intent);
            }
        });

        askedBarChart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GraphsActivity.this, FrequentlyAskedActivity.class); // Cambia "NuevaActividad" al nombre de tu nueva actividad
                startActivity(intent);
            }
        });

        horizontalBarChart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GraphsActivity.this, HorizontalBarChartActivity.class); // Cambia "NuevaActividad" al nombre de tu nueva actividad
                startActivity(intent);
            }
        });

    }
}