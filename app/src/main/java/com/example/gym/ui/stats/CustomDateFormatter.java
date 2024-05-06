package com.example.gym.ui.stats;

import com.example.gym.room.Stat;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import org.threeten.bp.format.DateTimeFormatter;

import java.util.List;

public class CustomDateFormatter extends IndexAxisValueFormatter {

    // Fields
    private List<Stat> mStatList;

    // Constructor
    CustomDateFormatter(List<Stat> statList) {
        mStatList = statList;
    }

//    @Override
    public String getAxisLabel(float value, AxisBase axis) {

        int position = (int) value;

        // Format date based on string from position in the list
        return getDateStringFormat(position);
    }

    private String getDateStringFormat(int position) {
        if (position == 0 || position > mStatList.size()) {
            return "";
        } else {
            return mStatList.get(position-1).getMDate().format(DateTimeFormatter.ofPattern("M/d"));
        }
    }
}
