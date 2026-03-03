package com.example.vacationapp.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.vacationapp.entities.Excursion;

import java.util.List;

@Dao
public interface ExcursionDAO {
    @Insert
    void insert(Excursion excursion);

    @Delete
    void delete(Excursion excursion);

    @Update
    void update(Excursion excursion);

    @Query("SELECT * FROM excursions WHERE vacationId = :vacationId ORDER BY date ASC")
    List<Excursion> getExcursionsForVacation(int vacationId);

    @Query("SELECT * FROM excursions WHERE excursionId = :id")
    Excursion getExcursionById(int id);

    @Query("SELECT COUNT(*) FROM excursions WHERE vacationId = :vacationId")
    int countExcursionsForVacation(int vacationId);
}
