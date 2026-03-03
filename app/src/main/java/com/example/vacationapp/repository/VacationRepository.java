package com.example.vacationapp.repository;

import android.content.Context;

import com.example.vacationapp.dao.ExcursionDAO;
import com.example.vacationapp.dao.VacationDAO;
import com.example.vacationapp.database.VacationDB;
import com.example.vacationapp.entities.Excursion;
import com.example.vacationapp.entities.Vacation;

import java.util.List;
import java.util.concurrent.Callable;

public class VacationRepository {

    private final VacationDAO vacationDAO;

    private final ExcursionDAO excursionDAO;

    public VacationRepository(Context context) {
        VacationDB database = VacationDB.getInstance(context);
        vacationDAO = database.vacationDAO();
        excursionDAO = database.excursionDAO();
    }

    public void runOnDbThread(Runnable task) {
        VacationDB.databaseExecutor.execute(task);
    }

    public <T> void runOnDbThread(Callable<T> task, DbCallback<T> callback) {
        VacationDB.databaseExecutor.execute(() -> {
            try {
                T result = task.call();
                callback.onComplete(result);
            } catch (Exception e) {
                e.printStackTrace();
                callback.onComplete(null);
            }
        });
    }

    public interface DbCallback<T> {
        void onComplete(T result);
    }

    public void insertVacation(Vacation vacation) { vacationDAO.insert(vacation); }

    public void deleteVacation(Vacation vacation) { vacationDAO.delete(vacation); }

    public void updateVacation(Vacation vacation) { vacationDAO.update(vacation); }

    public List<Vacation> getAllVacations() { return vacationDAO.getAllVacations(); }

    public Vacation getVacationById(int id) { return vacationDAO.getVacationById(id); }

    public void insertExcursion(Excursion excursion) { excursionDAO.insert(excursion); }

    public void deleteExcursion(Excursion excursion) { excursionDAO.delete(excursion); }

    public void updateExcursion(Excursion excursion) { excursionDAO.update(excursion); }

    public List<Excursion> getAllExcursions(int vacationID) { return excursionDAO.getExcursionsForVacation(vacationID); }

    public Excursion getExcursionById(int excursionID) { return excursionDAO.getExcursionById(excursionID); }

    public boolean hasExcursions(int vacationID) {
        return excursionDAO.countExcursionsForVacation(vacationID) > 0;
    }

}
