package com.example.gym.ui.stats;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.gym.repository.StatRepository;
import com.example.gym.room.Stat;

import org.threeten.bp.LocalDate;

import java.util.List;

public class StatsViewModel extends AndroidViewModel {

    // Fields
    private StatRepository mRepository;
    private LiveData<List<Stat>> mAllStat;

    // Constructor
    public StatsViewModel(@NonNull Application application) {
        super(application);
        mRepository = new StatRepository(application);
        mAllStat = mRepository.getAllStats();
    }

    // Methods for Stats
    public void insert(Stat stat) {
        mRepository.insert(stat);
    }

    public void update(Stat stat) {
        mRepository.update(stat);
    }

    public void delete(Stat stat) {
        mRepository.delete(stat);
    }

    public void deleteAllStatByDate(LocalDate date) {
        mRepository.deleteStatByDate(date);
    }

    public void deleteAllStats() {
        mRepository.deleteAlStats();
    }

    public LiveData<List<Stat>> getStatByDate(LocalDate date) {
        return mRepository.getStatByDate(date);
    }

    LiveData<List<Stat>> getAllStats() {
        return mAllStat;
    }
}
