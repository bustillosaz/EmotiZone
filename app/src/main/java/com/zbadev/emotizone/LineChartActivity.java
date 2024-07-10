package com.zbadev.emotizone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
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

public class LineChartActivity extends AppCompatActivity {

    private static final String TAG = "LineChartActivity";
    private FirebaseFirestore db;
    private LineChart lineChart;
    private Toolbar toolbarchart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_line_chart);

        toolbarchart = findViewById(R.id.toollinechart);
        setSupportActionBar(toolbarchart);

        // Cambiar el título del Toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Cambio de Estados Emocionales a lo largo del tiempo");
        }

        // Inicializar Firestore
        db = FirebaseFirestore.getInstance();

        // Inicializar LineChart
        lineChart = findViewById(R.id.lineChart);

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

                    LineDataSet dataSet = new LineDataSet(entries, "Estados Emocionales");

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

                    LineData data = new LineData(dataSet);
                    lineChart.setData(data);

                    // Configurar leyendas
                    Legend legend = lineChart.getLegend();
                    legend.setForm(Legend.LegendForm.LINE);
                    legend.setTextSize(12f);
                    legend.setFormSize(12f);
                    legend.setXEntrySpace(5f);

                    // Configurar descripciones
                    Description description = new Description();
                    description.setText("Cambio de Estados Emocionales a lo largo del tiempo");
                    description.setTextSize(12f);
                    lineChart.setDescription(description);

                    lineChart.invalidate(); // refrescar el gráfico
                } else {
                    Log.w(TAG, "Error getting documents.", task.getException());
                }
            }
        });
    }
}
