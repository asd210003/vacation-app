package com.example.vacationapp.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.vacationapp.entities.Vacation;

import java.util.List;

@Dao
public interface VacationDAO {
    @Insert
    void insert(Vacation vacation);

    @Delete
    void delete(Vacation vacation);

    @Update
    void update(Vacation vacation);

    @Query("SELECT * FROM vacations ORDER BY startDate ASC")
    List<Vacation> getAllVacations();

    @Query("SELECT * FROM vacations WHERE vacationId = :id LIMIT 1")
    Vacation getVacationById(int id);
}
