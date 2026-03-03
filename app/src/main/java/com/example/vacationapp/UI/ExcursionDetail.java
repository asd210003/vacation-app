package com.example.vacationapp.UI;

import static android.app.ProgressDialog.show;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.vacationapp.R;
import com.example.vacationapp.entities.Excursion;
import com.example.vacationapp.repository.VacationRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.Date;

public class ExcursionDetail extends AppCompatActivity {

    private VacationRepository repository;

    private EditText editTitle;
    private EditText editDate;

    private int excursionId = -1;
    private int vacationId = -1;

    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_excursion_detail);

        repository = new VacationRepository(this);

        editTitle = findViewById(R.id.editExcursionTitle);
        editDate = findViewById(R.id.editExcursionDate);

        Button btnSave = findViewById(R.id.btnSaveExcursion);
        Button btnDelete = findViewById(R.id.btnDeleteExcursion);
        Button btnAlert = findViewById(R.id.btnSetExcursionAlert);

        if (getIntent() != null) {
            vacationId = getIntent().getIntExtra("vacationId", -1);
            excursionId = getIntent().getIntExtra("excursionId", -1);
        }

        if (excursionId != -1) {
            btnDelete.setEnabled(true);
            loadExcursion(excursionId);
        } else {
            btnDelete.setEnabled(false);
        }


        btnSave.setOnClickListener(v -> saveExcursion());
        btnDelete.setOnClickListener(v -> deleteExcursion());
        btnAlert.setOnClickListener(v -> setAlertForCurrentInputs());

    }
    private void loadExcursion(int id) {
            repository.runOnDbThread(
                    () -> repository.getExcursionById(id),
                    existing -> runOnUiThread(() -> {
                        if (existing != null) {
                            editTitle.setText(existing.getTitle());
                            editDate.setText(existing.getDate());
                        } else {
                            Toast.makeText(this, "Excursion not found.",
                                    Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    })
            );

    }

    private void saveExcursion() {
        final String title = editTitle.getText().toString().trim();
        final String date = editDate.getText().toString().trim();

        if (title.isEmpty()) {
            Toast.makeText(this, "Title is required", Toast.LENGTH_SHORT).show();
            return;
        }

        final LocalDate excDate = parseStrictDateOrToast(date);
        if (excDate == null) return;

        repository.runOnDbThread(
                () -> repository.getVacationById(vacationId),
                vacation -> runOnUiThread(() -> {
                    if (vacation == null) {
                        Toast.makeText(this, "Vacation not found.",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    LocalDate vacStart = parseStrictDateOrToast(vacation.getStartDate(), "Vacation start date is invalid.");

                    if (vacStart == null) return;

                    LocalDate vacEnd = parseStrictDateOrToast(vacation.getEndDate(), "Vacation end date is invalid.");

                    if (vacEnd == null) return;

                    if (excDate.isBefore(vacStart) || excDate.isAfter(vacEnd)) {
                        Toast.makeText(this, "Excursion must be during vacation",
                        Toast.LENGTH_LONG).show();
                        return;
                    }
                    if (excursionId == -1) {
                        Excursion e = new Excursion(date, title, vacationId);
                        repository.runOnDbThread(() ->
                                repository.insertExcursion(e));
                        Toast.makeText(this, "Excursion saved.", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        repository.runOnDbThread(
                                () ->
                                        repository.getExcursionById(excursionId),
                                existing -> {
                                    if (existing != null) {
                                        existing.setTitle(title);
                                        existing.setDate(date);

                                        repository.runOnDbThread(() ->
                                                repository.updateExcursion(existing));
                                }
                                    runOnUiThread(() -> {
                                    Toast.makeText(this, "Excursion updated.",
                                    Toast.LENGTH_SHORT).show();
                                        finish();
                                    });
                                }
                        );
                    }
                })
        );
    }

    private void deleteExcursion() {
        if (excursionId == -1) return;

        repository.runOnDbThread(
                () -> repository.getExcursionById(excursionId),
                e -> {
                    if (e != null) {
                        repository.runOnDbThread(() -> repository.deleteExcursion(e));
                    }
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Excursion deleted.", Toast.LENGTH_SHORT).show();
                        finish();
                    });
                }
        );
    }

    private void setAlertForCurrentInputs() {
        String title = editTitle.getText().toString().trim();
        String dateStr = editDate.getText().toString().trim();

        if (title.isEmpty()) {
            Toast.makeText(this, "Enter a title first", Toast.LENGTH_SHORT).show();
            return;
        }

        LocalDate excDate = parseStrictDateOrToast(dateStr);
        if (excDate == null) return;

        scheduleExcursionAlarm(title, excDate);
    }

    private void scheduleExcursionAlarm(String title, LocalDate date) {
        long triggerAtMillis = date
                .atTime(LocalTime.of(9,0))
                .atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli();

        if (triggerAtMillis <= System.currentTimeMillis()) {
            Toast.makeText(this, "Alert time is in the past", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(this, ExcursionAlertReceiver.class);
        intent.putExtra("title", title);

        int requestCode = (excursionId != -1) ? excursionId : (int) (System.currentTimeMillis() & 0x7fffffff);

        PendingIntent pi = PendingIntent.getBroadcast(
                this,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT |
                        (Build.VERSION.SDK_INT >= 23 ? PendingIntent.FLAG_IMMUTABLE : 0)
        );

        AlarmManager am = (AlarmManager)
                getSystemService(Context.ALARM_SERVICE);
        if (am == null) {
            Toast.makeText(this, "Alarm service unavailable", Toast.LENGTH_SHORT).show();
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pi);
        } else {
            am.setExact(AlarmManager.RTC_WAKEUP, triggerAtMillis, pi);
        }

        Toast.makeText(this, "Alarm set for " + date + " (9:00 AM).",
        Toast.LENGTH_SHORT).show();
    }

    private LocalDate parseStrictDateOrToast(String dateStr) {
        return parseStrictDateOrToast(dateStr, "Date must be formatted as yyyy-mm-dd");
    }

    private LocalDate parseStrictDateOrToast(String dateStr, String errorMsg) {
        try {
            return LocalDate.parse(dateStr, DATE_FMT);
        } catch (DateTimeParseException ex) {
            Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show();
            return null;
        }
    }
}