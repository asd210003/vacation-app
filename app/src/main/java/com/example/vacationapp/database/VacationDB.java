package com.example.vacationapp.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.vacationapp.dao.ExcursionDAO;
import com.example.vacationapp.dao.VacationDAO;
import com.example.vacationapp.entities.Excursion;
import com.example.vacationapp.entities.Vacation;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {Excursion.class, Vacation.class}, version = 2, exportSchema = false)
public abstract class VacationDB extends RoomDatabase {

    private static volatile VacationDB INSTANCE;

    public static final ExecutorService databaseExecutor = Executors.newFixedThreadPool(4);

    public abstract VacationDAO vacationDAO();

    public abstract ExcursionDAO excursionDAO();

    public static VacationDB getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (VacationDB.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            VacationDB.class,
                            "vacation_planner"
                    )
                            .allowMainThreadQueries()
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
