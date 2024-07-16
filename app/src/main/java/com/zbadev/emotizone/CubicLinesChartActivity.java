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
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CubicLinesChartActivity extends AppCompatActivity {

    private static final String TAG = "CubicLinesChartActivity";
    private FirebaseFirestore db;
    private LineChart cubicLineChart;
    private Toolbar toolcubicchart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cubic_lines_chart);

        toolcubicchart = findViewById(R.id.toolcubicline);
        setSupportActionBar(toolcubicchart);

        // Inicializar Firestore
        db = FirebaseFirestore.getInstance();

        // Inicializar LineChart
        cubicLineChart = findViewById(R.id.cubicLineChart);

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
        String userEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        CollectionReference emotionalStatesRef = db.collection("emotionalStates");

        emotionalStatesRef.whereEqualTo("userEmail", userEmail).orderBy("date").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    Map<Integer, Float> monthData = new HashMap<>();
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

                    for (QueryDocumentSnapshot document : task.getResult()) {
                        String state = document.getString("emotionalState");
                        String dateString = document.getString("date");

                        try {
                            Date date = sdf.parse(dateString);
                            Calendar calendar = Calendar.getInstance();
                            calendar.setTime(date);
                            int month = calendar.get(Calendar.MONTH);

                            float yValue = mapEmotionalStateToYValue(state);

                            if (monthData.containsKey(month)) {
                                monthData.put(month, monthData.get(month) + yValue);
                            } else {
                                monthData.put(month, yValue);
                            }
                        } catch (ParseException e) {
                            Log.w(TAG, "Error parsing date.", e);
                        }
                    }

                    List<Entry> entries = new ArrayList<>();
                    for (Map.Entry<Integer, Float> entry : monthData.entrySet()) {
                        entries.add(new Entry(entry.getKey(), entry.getValue()));
                    }

                    LineDataSet dataSet = new LineDataSet(entries, "Actividad en el tiempo");
                    dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
                    dataSet.setDrawFilled(true);

                    // Definir colores personalizados para cada estado emocional
                    dataSet.setColor(getResources().getColor(R.color.BlueLight));
                    dataSet.setFillColor(getResources().getColor(R.color.BlueLight));

                    LineData data = new LineData(dataSet);
                    cubicLineChart.setData(data);

                    // Configurar leyendas y descripciones
                    Legend legend = cubicLineChart.getLegend();
                    legend.setForm(Legend.LegendForm.LINE);
                    legend.setTextSize(12f);
                    legend.setFormSize(12f);
                    legend.setXEntrySpace(5f);

                    Description description = new Description();
                    description.setText("Interaccion con el sistema a lo largo del tiempo");
                    description.setTextSize(12f);
                    cubicLineChart.setDescription(description);

                    // Configurar eje X para mostrar nombres de meses
                    XAxis xAxis = cubicLineChart.getXAxis();
                    xAxis.setGranularity(1f);
                    xAxis.setValueFormatter(new ValueFormatter() {
                        private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM", Locale.getDefault());

                        @Override
                        public String getFormattedValue(float value) {
                            Calendar calendar = Calendar.getInstance();
                            int month = (int) value % 12; // Obtener el mes correspondiente
                            calendar.set(Calendar.MONTH, month);
                            return dateFormat.format(calendar.getTime());
                        }
                    });


                    cubicLineChart.invalidate(); // refrescar el gráfico
                } else {
                    Log.w(TAG, "Error getting documents.", task.getException());
                }
            }
        });
    }

    private float mapEmotionalStateToYValue(String state) {
        switch (state) {
            case "Estres Alto":
                return 5f;
            case "Estres Moderado":
                return 4f;
            case "Estres Bajo":
                return 3f;
            case "Relajacion":
                return 2f;
            case "Felicidad":
                return 1f;
            case "Ansiedad":
                return 6f;
            case "Tristeza":
                return 7f;
            default:
                return 0f;
        }
    }
}
