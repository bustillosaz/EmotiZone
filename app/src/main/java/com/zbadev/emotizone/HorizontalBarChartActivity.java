package com.zbadev.emotizone;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class HorizontalBarChartActivity extends AppCompatActivity {

    private HorizontalBarChart horizontalBarChart;
    private FirebaseFirestore db;
    private Toolbar toolbarchart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_horizontal_bar_chart);

        toolbarchart = findViewById(R.id.toolhorizontal);
        setSupportActionBar(toolbarchart);

        horizontalBarChart = findViewById(R.id.horizontalBarChart);
        db = FirebaseFirestore.getInstance();

        loadDataFromFirestore();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_chart, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_exit) {
            // Acción de salida
            finish();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    private void loadDataFromFirestore() {
        db.collection("emotionalStates")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            Map<String, Integer> userCounts = new HashMap<>();

                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String dateStr = document.getString("date");
                                if (dateStr != null) {
                                    try {
                                        // Parse the date and get the month
                                        String month = new SimpleDateFormat("MMM", Locale.getDefault()).format(sdf.parse(dateStr));
                                        if (userCounts.containsKey(month)) {
                                            userCounts.put(month, userCounts.get(month) + 1);
                                        } else {
                                            userCounts.put(month, 1);
                                        }
                                    } catch (ParseException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }

                            displayData(userCounts);
                        } else {
                            Toast.makeText(HorizontalBarChartActivity.this, "Error getting documents: " + task.getException(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void displayData(Map<String, Integer> userCounts) {
        ArrayList<BarEntry> entries = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();

        int index = 0;
        for (Map.Entry<String, Integer> entry : userCounts.entrySet()) {
            entries.add(new BarEntry(index, entry.getValue()));
            labels.add(entry.getKey());
            index++;
        }

        BarDataSet dataSet = new BarDataSet(entries, "Total Usuarios");
        dataSet.setColor(getResources().getColor(R.color.BlueLight)); // Color personalizado para las barras
        dataSet.setValueTextSize(16f); // Tamaño del texto de los valores

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.9f);

        horizontalBarChart.setData(barData);
        horizontalBarChart.setFitBars(true);
        Description description = new Description();
        description.setText("Total de Usuarios por Mes");
        horizontalBarChart.setDescription(description);

        horizontalBarChart.getXAxis().setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return labels.get((int) value);
            }
        });

        horizontalBarChart.getXAxis().setGranularity(1f); // Asegurar la granularidad de 1
        horizontalBarChart.getXAxis().setGranularityEnabled(true);

        horizontalBarChart.getAxisLeft().setDrawLabels(false); // Ocultar etiquetas del eje izquierdo
        horizontalBarChart.getAxisRight().setTextSize(16f); // Tamaño del texto del eje derecho

        // Mostrar los valores al final de las barras
        dataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.valueOf((int) value);
            }
        });
        horizontalBarChart.animateY(1400, Easing.EaseInOutQuad);

        horizontalBarChart.invalidate();

        horizontalBarChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                int index = (int) e.getX();
                String month = labels.get(index);
                int count = (int) e.getY();
                String message = month + ": "+ count;
                showQuestionDialog(message);
                //Toast.makeText(HorizontalBarChartActivity.this, month + ": " + count, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected() {
                // Do nothing
            }
        });
    }
    private void showQuestionDialog(String message) {
        new AlertDialog.Builder(this)
                .setTitle("Usuarios Registrados")
                .setMessage(message)
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .show();
    }
}
