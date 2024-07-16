package com.zbadev.emotizone;

import androidx.appcompat.app.AlertDialog;
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
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FrequentlyAskedActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private BarChart barChart;
    private Toolbar toolbarchart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_frequently_asked);

        db = FirebaseFirestore.getInstance();
        barChart = findViewById(R.id.askedbarChart);

        toolbarchart = findViewById(R.id.askedtoolChart);
        setSupportActionBar(toolbarchart);

        fetchQuestionsFromFirestore();
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

    private void fetchQuestionsFromFirestore() {
        db.collection("conversations")
                .whereEqualTo("sender", "user")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<String> questions = new ArrayList<>();
                        for (DocumentSnapshot document : task.getResult()) {
                            String question = document.getString("message");
                            if (question != null) {
                                questions.add(question);
                            }
                        }
                        processQuestions(questions);
                    } else {
                        Log.e("FrequentlyAskedActivity", "Error al obtener las preguntas: ", task.getException());
                    }
                });
    }

    private void processQuestions(List<String> questions) {
        Map<String, Integer> questionFrequency = new HashMap<>();

        for (String question : questions) {
            questionFrequency.put(question, questionFrequency.getOrDefault(question, 0) + 1);
        }

        // Ordenar por frecuencia descendente
        List<Map.Entry<String, Integer>> sortedQuestions = new ArrayList<>(questionFrequency.entrySet());
        sortedQuestions.sort((entry1, entry2) -> entry2.getValue().compareTo(entry1.getValue()));

        // Tomar las N preguntas más frecuentes (por ejemplo, las 10 más frecuentes)
        int topN = 10;
        List<Map.Entry<String, Integer>> topQuestions = sortedQuestions.subList(0, Math.min(topN, sortedQuestions.size()));

        displayFrequentQuestions(topQuestions);
    }

    private void displayFrequentQuestions(List<Map.Entry<String, Integer>> topQuestions) {
        List<BarEntry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        for (int i = 0; i < topQuestions.size(); i++) {
            Map.Entry<String, Integer> entry = topQuestions.get(i);
            entries.add(new BarEntry(i, entry.getValue()));
            labels.add(entry.getKey());
        }

        BarDataSet dataSet = new BarDataSet(entries, "Preguntas Frecuentes");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);

        BarData data = new BarData(dataSet);
        barChart.setData(data);

        barChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        barChart.getXAxis().setLabelRotationAngle(90); // Rotar etiquetas si son largas
        barChart.getXAxis().setGranularity(1f); // Espaciado entre etiquetas
        barChart.getXAxis().setGranularityEnabled(true);

        Description description = new Description();
        description.setText("Preguntas más frecuentes");
        barChart.setDescription(description);
        barChart.animateY(1400, Easing.EaseInOutQuad);

        barChart.invalidate(); // refrescar el gráfico

        // Configurar el listener para mostrar el diálogo
        barChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                int index = (int) e.getX();
                String question = labels.get(index);
                showQuestionDialog(question);
            }

            @Override
            public void onNothingSelected() {
                // No hacer nada
            }
        });
    }

    private void showQuestionDialog(String question) {
        new AlertDialog.Builder(this)
                .setTitle("Pregunta Frecuente")
                .setMessage(question)
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .show();
    }
}
