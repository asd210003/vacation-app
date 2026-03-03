package com.example.vacationapp.UI;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.vacationapp.R;
import com.example.vacationapp.database.VacationDB;
import com.example.vacationapp.entities.Vacation;
import com.example.vacationapp.repository.VacationRepository;

import java.util.List;

public class VacationList extends AppCompatActivity {

    private VacationRepository repository;
    private ListView vacationListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vacation_list);

        repository = new VacationRepository(this);

        vacationListView = findViewById(R.id.vacationListView);

        vacationListView.setOnItemClickListener((parent, view, position, id) -> {
            Vacation selected = (Vacation)
                    parent.getItemAtPosition(position);
            Intent intent = new Intent(VacationList.this, VacationDetail.class);
            intent.putExtra("vacationId", selected.getVacationID());
            startActivity(intent);
        });

        Button backBtn = findViewById(R.id.backButton);
        backBtn.setOnClickListener(v -> {
            Intent intent = new Intent(VacationList.this, MainActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadVacations();
    }

    private void loadVacations() {
        VacationDB.databaseExecutor.execute(() -> {
            List<Vacation> vacations = repository.getAllVacations();

            runOnUiThread(() -> {
                ArrayAdapter<Vacation> adapter = new ArrayAdapter<>(
                        VacationList.this,
                        android.R.layout.simple_list_item_1,
                        vacations
                );

                vacationListView.setAdapter(adapter);
            });
        });
    }
}