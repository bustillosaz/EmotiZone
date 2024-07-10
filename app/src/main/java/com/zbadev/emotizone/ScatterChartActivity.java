package com.zbadev.emotizone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.github.mikephil.charting.charts.ScatterChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.ScatterData;
import com.github.mikephil.charting.data.ScatterDataSet;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ScatterChartActivity extends AppCompatActivity {

    private static final String TAG = "ScatterChartActivity";
    private FirebaseFirestore db;
    private ScatterChart scatterChart;
    private Toolbar toolscatterchart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scatter_chart);

        toolscatterchart = findViewById(R.id.toolbarchart);
        setSupportActionBar(toolscatterchart);

        // Cambiar el título del Toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Distribución de Estados Emocionales");
        }

        // Inicializar Firestore
        db = FirebaseFirestore.getInstance();

        // Inicializar ScatterChart
        scatterChart = findViewById(R.id.scatterChart);

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

                    ArrayList<Entry> entries = new ArrayList<>();
                    int i = 0;
                    for (Map.Entry<String, Integer> entry : stateCounts.entrySet()) {
                        entries.add(new Entry(i, entry.getValue()));
                        i++;
                    }

                    ScatterDataSet dataSet = new ScatterDataSet(entries, "Estados Emocionales");

                    // Definir colores personalizados para cada estado emocional
                    int[] colors = new int[]{
                            getResources().getColor(R.color.colorEstresAlto),
                            getResources().getColor(R.color.colorEstresModerado),
                            getResources().getColor(R.color.colorEstresBajo),
                            getResources().getColor(R.color.colorRelajacion),
                            getResources().getColor(R.color.colorFelicidad),
                            getResources().getColor(R.color.colorAnsiedad),
                            getResources().getColor(R.color.colorTristeza)
                    };
                    dataSet.setColors(colors);

                    ScatterData data = new ScatterData(dataSet);
                    scatterChart.setData(data);

                    // Configurar leyendas
                    Legend legend = scatterChart.getLegend();
                    legend.setForm(Legend.LegendForm.CIRCLE);
                    legend.setTextSize(12f);
                    legend.setFormSize(12f);
                    legend.setXEntrySpace(5f);

                    // Configurar descripciones
                    Description description = new Description();
                    description.setText("Distribución de Estados Emocionales");
                    description.setTextSize(12f);
                    scatterChart.setDescription(description);

                    scatterChart.invalidate(); // refrescar el gráfico
                } else {
                    Log.w(TAG, "Error getting documents.", task.getException());
                }
            }
        });
    }
}
