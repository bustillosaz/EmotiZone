package com.zbadev.emotizone;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class CalendarFragment extends Fragment {

    private RecyclerView recyclerView;
    private ScheduleAdapter scheduleAdapter;
    private List<ScheduleItem> scheduleList;
    private CalendarView calendarView;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    public CalendarFragment() {
        // Required empty public constructor
    }

    public static CalendarFragment newInstance(String param1, String param2) {
        CalendarFragment fragment = new CalendarFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            // handle arguments if any
        }
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_calendar, container, false);

        calendarView = view.findViewById(R.id.calendarView);
        recyclerView = view.findViewById(R.id.recyclerView);

        // Initialize schedule list and adapter
        scheduleList = new ArrayList<>();
        scheduleAdapter = new ScheduleAdapter(scheduleList);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(scheduleAdapter);

        // Set listener for calendar date change
        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                String selectedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth);
                fetchDataForDate(selectedDate);
            }
        });

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Fetch data for current selected date (today's date)
        // Here you can use any initial date or today's date as per your requirement
        // For demonstration, I'm fetching data for today's date.
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
        String todayDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth);
        fetchDataForDate(todayDate);
    }

    private void fetchDataForDate(String date) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            return;
        }
        String userEmail = currentUser.getEmail();

        CollectionReference emotionalStatesRef = db.collection("emotionalStates");
        emotionalStatesRef.whereEqualTo("userEmail", userEmail)
                .orderBy("date", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        scheduleList.clear();
                        for (QueryDocumentSnapshot document : querySnapshot) {
                            String dateTime = document.getString("date");
                            if (dateTime != null && dateTime.startsWith(date)) {
                                String time = dateTime.split(" ")[1];  // Obtén la hora de dateTime
                                String emotionalState = document.getString("emotionalState");
                                scheduleList.add(new ScheduleItem(time, emotionalState, "Estado Emocional", emotionalState));  // Usa tu ícono apropiado
                            }
                        }

                        scheduleAdapter.notifyDataSetChanged();
                    }
                });
    }
}
