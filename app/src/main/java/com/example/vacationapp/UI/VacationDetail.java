package com.example.vacationapp.UI;

import static android.app.ProgressDialog.show;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.vacationapp.R;
import com.example.vacationapp.entities.Excursion;
import com.example.vacationapp.entities.Vacation;
import com.example.vacationapp.repository.VacationRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.List;

public class VacationDetail extends AppCompatActivity {

    private VacationRepository repository;

    private EditText editTitle;
    private EditText editHotel;
    private EditText editStartDate;
    private EditText editEndDate;

    private ListView excursionListView;
    private ArrayAdapter<Excursion> excursionAdapter;

    private Button btnStartAlert;
    private Button btnEndAlert;
    private Button btnShareVacation;

    private int vacationId = -1;

    private static final DateTimeFormatter STRICT_DATE =
            DateTimeFormatter.ofPattern("uuuu-MM-dd").withResolverStyle(ResolverStyle.STRICT);

    private static final int REQ_POST_NOTIFICATIONS = 5001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vacation_detail);

        repository = new VacationRepository(this);

        editTitle = findViewById(R.id.editTitle);
        editHotel = findViewById(R.id.editHotel);
        editStartDate = findViewById(R.id.editStartDate);
        editEndDate = findViewById(R.id.editEndDate);

        excursionListView = findViewById(R.id.excursionListView);

        Button btnSave = findViewById(R.id.btnSave);
        Button btnDelete = findViewById(R.id.btnDelete);

        Button btnAddExcursion = findViewById(R.id.btnAddTestExcursion);

        btnStartAlert = findViewById(R.id.btnStartAlert);
        btnEndAlert = findViewById(R.id.btnEndAlert);

        btnShareVacation = findViewById(R.id.btnShareVacation);

        excursionAdapter = new ArrayAdapter<Excursion>(
                this,
                android.R.layout.simple_list_item_2,
                android.R.id.text1,
                new ArrayList<>()
        ) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View row = super.getView(position, convertView, parent);

                TextView text1 = row.findViewById(android.R.id.text1);
                TextView text2 = row.findViewById(android.R.id.text2);

                Excursion e = getItem(position);
                if (e != null) {
                    text1.setText(e.getTitle());
                    text2.setText(e.getDate());
                }

                return row;
            }
        };
        excursionListView.setAdapter(excursionAdapter);

        excursionListView.setOnItemClickListener((parent, view, position, id) -> {
            if (vacationId == -1) return;

            Excursion selected = (Excursion) parent.getItemAtPosition(position);
            if (selected == null) return;

            Intent intent = new Intent(VacationDetail.this, ExcursionDetail.class);
            intent.putExtra("vacationId", vacationId);
            intent.putExtra("excursionId", selected.getExcursionID());
            startActivity(intent);
        });

        if (getIntent() != null && getIntent().hasExtra("vacationId")) {

            vacationId = getIntent().getIntExtra("vacationId", -1);
        }
            if (vacationId != -1) {
                repository.runOnDbThread(
                        () -> repository.getVacationById(vacationId),
                        existing -> runOnUiThread(() -> {
                        if (existing != null) {
                            editTitle.setText(existing.getTitle());
                            editHotel.setText(existing.getHotel());
                            editStartDate.setText(existing.getStartDate());
                            editEndDate.setText(existing.getEndDate());
                        }
                        btnDelete.setEnabled(true);
                        btnAddExcursion.setEnabled(true);
                        btnStartAlert.setEnabled(true);
                        btnEndAlert.setEnabled(true);
                        btnShareVacation.setEnabled(true);
                    })
                );
            } else {
                btnDelete.setEnabled(false);
                btnAddExcursion.setEnabled(false);
                btnStartAlert.setEnabled(false);
                btnEndAlert.setEnabled(false);
                btnShareVacation.setEnabled(false);
            }

        btnSave.setOnClickListener(v -> saveVacation());
        btnDelete.setOnClickListener(v -> deleteVacation());

        btnAddExcursion.setOnClickListener(v -> {
            if (vacationId == -1) {
                Toast.makeText(this, "Save the vacation to add an excursion",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(VacationDetail.this, ExcursionDetail.class);
            intent.putExtra("vacationId", vacationId);
            startActivity(intent);
        });

        btnStartAlert.setOnClickListener(v -> setAlert(true));
        btnEndAlert.setOnClickListener(v -> setAlert(false));
        btnShareVacation.setOnClickListener(v -> shareVacation());
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadExcursions();
    }

    private void loadExcursions() {
        if (vacationId == -1) {
            excursionAdapter.clear();
            excursionAdapter.notifyDataSetChanged();
            return;
        }

        repository.runOnDbThread(() ->
                repository.getAllExcursions(vacationId), excursions -> {
            runOnUiThread(() -> {
                excursionAdapter.clear();
                if (excursions != null) {
                    excursionAdapter.addAll(excursions);
                }
                excursionAdapter.notifyDataSetChanged();
            });
        });
    }

    private void saveVacation() {
        String title = editTitle.getText().toString().trim();
        String hotel = editHotel.getText().toString().trim();
        String startDate = editStartDate.getText().toString().trim();
        String endDate = editEndDate.getText().toString().trim();

        if (title.isEmpty() || hotel.isEmpty() || startDate.isEmpty() || endDate.isEmpty()) {
            Toast.makeText(this, "Please enter all required fields.",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        LocalDate startDateV;
        LocalDate endDateV;

        try {
            startDateV = LocalDate.parse(startDate, STRICT_DATE);
        } catch (DateTimeParseException e) {
            Toast.makeText(this, "Start date format is incorrect",
                    Toast.LENGTH_LONG).show();
            return;
        }

        try {
            endDateV = LocalDate.parse(endDate, STRICT_DATE);
        } catch (DateTimeParseException e) {
            Toast.makeText(this, "End date format is incorrect",
                    Toast.LENGTH_LONG).show();
            return;
        }

        if (!endDateV.isAfter(startDateV)) {
            Toast.makeText(this, "End date is after start date.",
                    Toast.LENGTH_LONG).show();
            return;
        }


        if (vacationId == -1) {
            Vacation v = new Vacation(title, hotel, startDate, endDate);
            repository.runOnDbThread(() -> {
                repository.insertVacation(v);
                runOnUiThread(() -> {
                    Toast.makeText(this, "Vacation added", Toast.LENGTH_SHORT).show();
                });
            });
        } else {
            Vacation v = new Vacation(title, hotel, startDate, endDate);
            v.setVacationID(vacationId);
            repository.runOnDbThread(() -> {
                repository.updateVacation(v);
                runOnUiThread(() -> {
                    Toast.makeText(this, "Vacation updated", Toast.LENGTH_SHORT).show();
                });
            });
        }
        finish();

        List<Vacation> vacations = repository.getAllVacations();


        // Log vacations to logcat
        for (Vacation v : vacations) {
            String log = String.format("%s | %s | %s | %s",
                    LocalDateTime.now(),
                    v.getTitle(),
                    v.getHotel(),
                    v.getStartDate());
            Log.i("Report of Vacations", log);
        }
    }

    private void deleteVacation() {
        if (vacationId == -1) return;

        repository.runOnDbThread(() ->
                repository.hasExcursions(vacationId), hasExcursions -> {
            runOnUiThread(() -> {
                if (hasExcursions != null && hasExcursions) {
                    Toast.makeText(this, "Cannot delete a vacation with excursions",
                            Toast.LENGTH_LONG).show();
                } else {
                    repository.runOnDbThread(() -> {
                        Vacation v = repository.getVacationById(vacationId);
                        if (v != null) {
                            repository.deleteVacation(v);
                        }
                        runOnUiThread(() -> {
                            Toast.makeText(this, "Vacation deleted", Toast.LENGTH_SHORT).show();
                            finish();
                        });

                    });
                }
            });
        });
    }

    private void setAlert(boolean isStart) {
        if (vacationId == -1) {
            Toast.makeText(this, "Save the vacation first.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.POST_NOTIFICATIONS)
                        != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        this,
                        new String[]
                                {Manifest.permission.POST_NOTIFICATIONS},
                                REQ_POST_NOTIFICATIONS
                );

                Toast.makeText(this, "Grant notification permission, then tap.",
                Toast.LENGTH_SHORT).show();
                return;
            }
        }

        String vacationTitle = editTitle.getText().toString().trim();
        String dateStr = isStart ? editStartDate.getText().toString().trim() :
                editEndDate.getText().toString().trim();

        if (vacationTitle.isEmpty() || dateStr.isEmpty()) {
            Toast.makeText(this, "Enter title and date first.",
            Toast.LENGTH_SHORT).show();
            return;
        }

        LocalDate date;
        try {
            date = LocalDate.parse(dateStr, STRICT_DATE);
        } catch (DateTimeParseException ex) {
            Toast.makeText(this, "Date format is incorrect.", Toast.LENGTH_SHORT).show();
            return;
        }

        long triggerAtMillis = date.atTime(9, 0)
                .atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli();

        if (triggerAtMillis < System.currentTimeMillis()) {
            Toast.makeText(this, "That alert time is in the past.",
                    Toast.LENGTH_LONG).show();
            return;
        }

        scheduleNotification(isStart, vacationId, vacationTitle, dateStr, triggerAtMillis);

        Toast.makeText(this, (isStart ? "Start alert set for " : "End alert set for")
                       + dateStr + " 9:00 AM",
                        Toast.LENGTH_LONG).show();
    }

    private void scheduleNotification(boolean isStart, int vacationId, String vacationTitle, String dateStr, long triggerAtMillis) {
        AlarmManager am = (AlarmManager)
                getSystemService(ALARM_SERVICE);

        Intent intent = new Intent(this, AlertReceiver.class);

        String title = isStart ? "Vacation Start" : "Vacation End";
        String message = vacationTitle + " (" + dateStr + ")";

        int notificationId = (vacationId * 10) + (isStart ? 1 : 2);

        intent.putExtra(AlertReceiver.EXTRA_TITLE, title);
        intent.putExtra(AlertReceiver.EXTRA_MESSAGE, message);
        intent.putExtra(AlertReceiver.EXTRA_NOTIFICATION_ID, notificationId);

        int requestCode = notificationId;

        PendingIntent pi = PendingIntent.getBroadcast(
                this,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        am.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, pi);
    }

    // @Override
    public void onRequestPermissionResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQ_POST_NOTIFICATIONS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission granted. Tap the alert",
                Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Notification permissions denied",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    private void shareVacation() {
        if (vacationId == -1) {
            Toast.makeText(this, "Save the vacation first before sharing.",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        final String title = editTitle.getText().toString().trim();
        final String hotel = editHotel.getText().toString().trim();
        final String start = editStartDate.getText().toString().trim();
        final String end = editEndDate.getText().toString().trim();

        repository.runOnDbThread(
                () -> repository.getAllExcursions(vacationId),
                excursions -> runOnUiThread(() -> {
                    StringBuilder sb = new StringBuilder();
                    sb.append("Vacation Details\n");
                    sb.append("----------------\n");
                    sb.append("Title: ").append(title).append("\n");
                    sb.append("Hotel/Location:").append(hotel).append("\n");
                    sb.append("Start Date: ").append(start).append("\n");
                    sb.append("End Date: ").append(end).append("\n");
                    sb.append("\nExcursions\n");
                    sb.append("----------\n");

                    if (excursions == null || excursions.isEmpty()) {
                        sb.append("None\n");
                    } else {
                        for (Excursion e : excursions) {
                            sb.append("- ").append(e.getTitle()).append(" (").append(e.getDate()).append(")\n");
                        }
                    }

                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                    shareIntent.setType("text/plain");
                    shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Vacation: " + title);
                    shareIntent.putExtra(Intent.EXTRA_TEXT, sb.toString());

                    startActivity(Intent.createChooser(shareIntent, "Share vacation via"));
                })
        );
    }
}