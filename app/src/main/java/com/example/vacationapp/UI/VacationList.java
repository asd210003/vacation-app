package com.example.vacationapp.UI;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SearchView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.vacationapp.R;
import com.example.vacationapp.database.VacationDB;
import com.example.vacationapp.entities.Vacation;
import com.example.vacationapp.repository.VacationRepository;

import java.util.ArrayList;
import java.util.List;

public class VacationList extends AppCompatActivity {

    private VacationRepository repository;
    private ListView vacationListView;
    private SearchView searchView;
    private ArrayAdapter<Vacation> adapter;
    private List<Vacation> vacationList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vacation_list);

        repository = new VacationRepository(this);
        vacationListView = findViewById(R.id.vacationListView);
        searchView = findViewById(R.id.searchView);

        adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                vacationList
        );
        vacationListView.setAdapter(adapter);

        vacationListView.setOnItemClickListener((parent, view, position, id) -> {
            Vacation selected = adapter.getItem(position);
            if (selected != null) {
                Intent intent = new Intent(VacationList.this, VacationDetail.class);
                intent.putExtra("vacationId", selected.getVacationID());
                startActivity(intent);
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                adapter.getFilter().filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.getFilter().filter(newText);
                return false;
            }
        });

        // Back button
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
                vacationList.clear();
                vacationList.addAll(vacations);
                adapter.notifyDataSetChanged();
            });
        });
    }
}