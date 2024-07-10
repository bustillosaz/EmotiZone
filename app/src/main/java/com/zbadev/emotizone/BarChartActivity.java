package com.zbadev.emotizone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class BarChartActivity extends AppCompatActivity {

    private static final String TAG = "BarChartActivity";
    private FirebaseFirestore db;
    private BarChart barChart;
    private Toolbar toolbarchart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bar_chart);

        toolbarchart = findViewById(R.id.toolbarchart);
        setSupportActionBar(toolbarchart);

        // Cambiar el título del Toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Cantidad de Registros");
        }

        // Inicializar Firestore
        db = FirebaseFirestore.getInstance();

        // Inicializar BarChart
        barChart = findViewById(R.id.barChart);

        // Cargar datos y mostrar gráfico
        loadChartData();
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

    private void loadChartData() {
        CollectionReference emotionalStatesRef = db.collection("emotionalStates");

        emotionalStatesRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    Map<String, Integer> stateCounts = new HashMap<>();
                    stateCounts.put("Estres Alto", 0);
                    stateCounts.put("Estres Moderado", 0);
                    stateCounts.put("Estres Bajo", 0);
                    stateCounts.put("Relajacion", 0);
                    stateCounts.put("Felicidad", 0);
                    stateCounts.put("Ansiedad", 0);
                    stateCounts.put("Tristeza", 0);

                    for (QueryDocumentSnapshot document : task.getResult()) {
                        String state = document.getString("emotionalState");
                        stateCounts.put(state, stateCounts.get(state) + 1);
                    }

                    ArrayList<BarEntry> entries = new ArrayList<>();
                    ArrayList<String> labels = new ArrayList<>();
                    int i = 0;
                    for (Map.Entry<String, Integer> entry : stateCounts.entrySet()) {
                        entries.add(new BarEntry(i, entry.getValue()));
                        labels.add(entry.getKey());
                        i++;
                    }

                    BarDataSet dataSet = new BarDataSet(entries, "Estados Emocionales");

                    // Definir colores personalizados para cada estado emocional
                    int[] colors = new int[]{
                            getResources().getColor(R.color.colorTristeza),
                            getResources().getColor(R.color.colorEstresAlto),
                            getResources().getColor(R.color.colorEstresBajo),
                            getResources().getColor(R.color.colorFelicidad),
                            getResources().getColor(R.color.colorAnsiedad),
                            getResources().getColor(R.color.colorRelajacion),
                            getResources().getColor(R.color.colorEstresModerado)
                    };
                    dataSet.setColors(colors);

                    BarData data = new BarData(dataSet);
                    barChart.setData(data);

                    // Configurar leyendas
                    Legend legend = barChart.getLegend();
                    legend.setForm(Legend.LegendForm.SQUARE);
                    legend.setTextSize(12f);
                    legend.setFormSize(12f);
                    legend.setXEntrySpace(5f);

                    // Configurar descripciones
                    Description description = new Description();
                    description.setText("Distribución de Estados Emocionales");
                    description.setTextSize(12f);
                    barChart.setDescription(description);

                    // Configurar etiquetas del eje X
                    barChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
                    barChart.getXAxis().setGranularity(1f);
                    barChart.getXAxis().setGranularityEnabled(true);

                    barChart.invalidate(); // refrescar el gráfico
                } else {
                    Log.w(TAG, "Error getting documents.", task.getException());
                }
            }
        });
    }
}
