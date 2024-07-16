package com.zbadev.emotizone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class StackedBarChartActivity extends AppCompatActivity {

    private static final String TAG = "StackedBarChartActivity";
    private FirebaseFirestore db;
    private BarChart groupedBarChart;
    private Toolbar toolbargroupedbarchart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stacked_bar_chart);

        toolbargroupedbarchart = findViewById(R.id.toolbarstackedbarchart);
        setSupportActionBar(toolbargroupedbarchart);

        // Cambiar el título del Toolbar
        /*if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Cantidad de Registros por mes");
        }*/

        // Inicializar Firestore
        db = FirebaseFirestore.getInstance();

        // Inicializar BarChart
        groupedBarChart = findViewById(R.id.stackedBarChart);

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
                    Map<String, Map<String, Integer>> monthlyStateCounts = new HashMap<>();
                    String[] states = {"Estres Alto", "Estres Moderado", "Estres Bajo", "Relajacion", "Felicidad", "Ansiedad", "Tristeza"};

                    SimpleDateFormat monthFormat = new SimpleDateFormat("yyyy-MM", Locale.getDefault());
                    SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

                    for (QueryDocumentSnapshot document : task.getResult()) {
                        String state = document.getString("emotionalState");
                        String date = document.getString("date");
                        String month = "Unknown";

                        if (date != null) {
                            try {
                                month = monthFormat.format(inputFormat.parse(date));
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        }

                        if (!monthlyStateCounts.containsKey(month)) {
                            monthlyStateCounts.put(month, new HashMap<>());
                            for (String s : states) {
                                monthlyStateCounts.get(month).put(s, 0);
                            }
                        }

                        if (state != null && monthlyStateCounts.get(month).containsKey(state)) {
                            monthlyStateCounts.get(month).put(state, monthlyStateCounts.get(month).get(state) + 1);
                        }
                    }

                    ArrayList<BarEntry> estresAltoEntries = new ArrayList<>();
                    ArrayList<BarEntry> estresModeradoEntries = new ArrayList<>();
                    ArrayList<BarEntry> estresBajoEntries = new ArrayList<>();
                    ArrayList<BarEntry> relajacionEntries = new ArrayList<>();
                    ArrayList<BarEntry> felicidadEntries = new ArrayList<>();
                    ArrayList<BarEntry> ansiedadEntries = new ArrayList<>();
                    ArrayList<BarEntry> tristezaEntries = new ArrayList<>();
                    ArrayList<String> labels = new ArrayList<>();
                    int i = 0;

                    for (Map.Entry<String, Map<String, Integer>> entry : monthlyStateCounts.entrySet()) {
                        labels.add(entry.getKey());
                        estresAltoEntries.add(new BarEntry(i, entry.getValue().get("Estres Alto")));
                        estresModeradoEntries.add(new BarEntry(i, entry.getValue().get("Estres Moderado")));
                        estresBajoEntries.add(new BarEntry(i, entry.getValue().get("Estres Bajo")));
                        relajacionEntries.add(new BarEntry(i, entry.getValue().get("Relajacion")));
                        felicidadEntries.add(new BarEntry(i, entry.getValue().get("Felicidad")));
                        ansiedadEntries.add(new BarEntry(i, entry.getValue().get("Ansiedad")));
                        tristezaEntries.add(new BarEntry(i, entry.getValue().get("Tristeza")));
                        i++;
                    }

                    BarDataSet estresAltoDataSet = new BarDataSet(estresAltoEntries, "Estres Alto");
                    estresAltoDataSet.setColor(getResources().getColor(R.color.colorEstresAlto));
                    BarDataSet estresModeradoDataSet = new BarDataSet(estresModeradoEntries, "Estres Moderado");
                    estresModeradoDataSet.setColor(getResources().getColor(R.color.colorEstresModerado));
                    BarDataSet estresBajoDataSet = new BarDataSet(estresBajoEntries, "Estres Bajo");
                    estresBajoDataSet.setColor(getResources().getColor(R.color.colorEstresBajo));
                    BarDataSet relajacionDataSet = new BarDataSet(relajacionEntries, "Relajacion");
                    relajacionDataSet.setColor(getResources().getColor(R.color.colorRelajacion));
                    BarDataSet felicidadDataSet = new BarDataSet(felicidadEntries, "Felicidad");
                    felicidadDataSet.setColor(getResources().getColor(R.color.colorFelicidad));
                    BarDataSet ansiedadDataSet = new BarDataSet(ansiedadEntries, "Ansiedad");
                    ansiedadDataSet.setColor(getResources().getColor(R.color.colorAnsiedad));
                    BarDataSet tristezaDataSet = new BarDataSet(tristezaEntries, "Tristeza");
                    tristezaDataSet.setColor(getResources().getColor(R.color.colorTristeza));

                    BarData data = new BarData(estresAltoDataSet, estresModeradoDataSet, estresBajoDataSet, relajacionDataSet, felicidadDataSet, ansiedadDataSet, tristezaDataSet);
                    float groupSpace = 0.08f;
                    float barSpace = 0.02f;
                    float barWidth = 0.10f;

                    data.setBarWidth(barWidth);
                    groupedBarChart.setData(data);
                    groupedBarChart.groupBars(0f, groupSpace, barSpace);

                    // Ajustar el rango visible del eje X
                    XAxis xAxis = groupedBarChart.getXAxis();
                    xAxis.setAxisMinimum(0f);
                    xAxis.setAxisMaximum(labels.size());
                    xAxis.setCenterAxisLabels(true);
                    xAxis.setPosition(XAxis.XAxisPosition.TOP);
                    xAxis.setGranularity(1f);
                    xAxis.setGranularityEnabled(true);
                    xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));

                    groupedBarChart.setVisibleXRangeMaximum(3);


                    groupedBarChart.invalidate(); // refrescar el gráfico

                    // Configurar leyendas
                    Legend legend = groupedBarChart.getLegend();
                    legend.setForm(Legend.LegendForm.SQUARE);
                    legend.setTextSize(12f);
                    legend.setFormSize(12f);
                    legend.setXEntrySpace(5f);

                    // Configurar descripciones
                    Description description = new Description();
                    description.setText("Distribución de Estados Emocionales por Mes");
                    description.setTextSize(12f);
                    groupedBarChart.setDescription(description);

                    // Configurar etiquetas del eje X
                    groupedBarChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
                    groupedBarChart.getXAxis().setGranularity(1f);
                    groupedBarChart.getXAxis().setGranularityEnabled(true);
                    groupedBarChart.animateY(1400, Easing.EaseInOutQuad);

                    groupedBarChart.invalidate(); // refrescar el gráfico
                } else {
                    Log.w(TAG, "Error getting documents.", task.getException());
                }
            }
        });
    }
}
