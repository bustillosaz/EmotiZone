package com.zbadev.emotizone;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.github.mikephil.charting.charts.RadarChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.RadarData;
import com.github.mikephil.charting.data.RadarDataSet;
import com.github.mikephil.charting.data.RadarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RadarChartActivity extends AppCompatActivity {

    private static final String TAG = "RadarChartActivity";
    private FirebaseFirestore db;
    private RadarChart radarChart;
    private Toolbar toolradarchart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_radar_chart);

        toolradarchart = findViewById(R.id.toolradarchart);
        setSupportActionBar(toolradarchart);

        // Inicializar Firestore
        db = FirebaseFirestore.getInstance();

        // Inicializar RadarChart
        radarChart = findViewById(R.id.radarChart);

        // Cargar datos y mostrar gráfico
        loadRadarChartData();
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

    private void loadRadarChartData() {
        String userEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        CollectionReference emotionalStatesRef = db.collection("emotionalStates");

        // Aquí debes cargar y procesar los datos para el gráfico de radar
        emotionalStatesRef.whereEqualTo("userEmail", userEmail).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    // Procesar los datos para el gráfico de radar
                    List<String> labels = new ArrayList<>();
                    List<RadarEntry> radarEntries = new ArrayList<>();

                    Map<String, Integer> stateCounts = new HashMap<>();

                    // Contar frecuencia de cada estado emocional
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        String state = document.getString("emotionalState");
                        if (stateCounts.containsKey(state)) {
                            stateCounts.put(state, stateCounts.get(state) + 1);
                        } else {
                            stateCounts.put(state, 1);
                        }
                    }

                    // Agregar datos al gráfico
                    for (Map.Entry<String, Integer> entry : stateCounts.entrySet()) {
                        labels.add(entry.getKey());
                        radarEntries.add(new RadarEntry(entry.getValue()));
                    }

                    // Verificar si hay datos para mostrar
                    if (radarEntries.isEmpty()) {
                        Log.d(TAG, "No hay datos disponibles para mostrar en el gráfico.");
                        return;
                    }

                    // Configurar el RadarChart
                    RadarDataSet dataSet = new RadarDataSet(radarEntries, "Frecuencia de Estados Emocionales");
                    dataSet.setColor(getResources().getColor(R.color.BlueLight));
                    dataSet.setFillColor(getResources().getColor(R.color.BlueLight));
                    dataSet.setDrawFilled(true);

                    RadarData data = new RadarData(dataSet);
                    radarChart.setData(data);

                    // Configurar el eje X del gráfico de radar
                    XAxis xAxis = radarChart.getXAxis();
                    xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));

                    // Configurar el eje Y del gráfico de radar
                    YAxis yAxis = radarChart.getYAxis();
                    yAxis.setAxisMinimum(0f); // Mínimo valor en el eje Y

                    // Configurar leyenda
                    Legend legend = radarChart.getLegend();
                    legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
                    legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
                    legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
                    legend.setDrawInside(false);
                    radarChart.animateXY(1000, 1000);

                    radarChart.getDescription().setEnabled(false); // Deshabilitar la descripción
                    radarChart.invalidate(); // Refrescar el gráfico
                } else {
                    Log.w(TAG, "Error obteniendo documentos: ", task.getException());
                }
            }
        });
    }
}
