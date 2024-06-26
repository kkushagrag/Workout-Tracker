package com.example.gym.ui.stats;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.gym.MainActivity;
import com.example.gym.R;
import com.example.gym.room.Stat;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textview.MaterialTextView;

import org.threeten.bp.LocalDate;
import org.threeten.bp.format.DateTimeFormatter;
import org.threeten.bp.format.FormatStyle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class StatsFragment extends Fragment implements AddStatDialogFragment.StatListener {

    private static final String TAG = "StatsFragment";

    private String mWeightInput;
    private String mFatInput;
    private boolean mShouldUpdate = false;

    private MaterialTextView mWeightInputTV;
    private MaterialTextView mFatInputTV;
    private MaterialButton mUpdateButton;
    private LineChart mWeightChart;
    private LineChart mFatChart;

    private StatsViewModel mViewModel;

    public StatsFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_stats, container, false);
        MaterialToolbar toolbar = root.findViewById(R.id.stats_tb);
        MaterialTextView currentDateTV = root.findViewById(R.id.stats_current_date_tv);
        mWeightInputTV = root.findViewById(R.id.stats_weight_tv);
        mFatInputTV = root.findViewById(R.id.stats_body_fat_tv);
        mUpdateButton = root.findViewById(R.id.stats_update_btn);
        mWeightChart = root.findViewById(R.id.weight_chart);
        mFatChart = root.findViewById(R.id.fat_chart);

        String formattedCurrentDate = LocalDate.now().format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM));
        currentDateTV.setText(formattedCurrentDate);

        if (getActivity() != null) {
            ((MainActivity)getActivity()).setSupportActionBar(toolbar);
            ActionBar actionBar = ((MainActivity)getActivity()).getSupportActionBar();
            if (actionBar != null) {
                actionBar.setDisplayShowTitleEnabled(false);
            } else {
                Log.e(TAG, "onCreateView: Could not get reference to support action bar");
            }
        } else {
            Log.e(TAG, "onCreateView: Could not get reference to activity");
        }
        return root;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mUpdateButton.setOnClickListener((View view) -> startDialogFragment());

        mViewModel = new ViewModelProvider(this).get(StatsViewModel.class);
        mViewModel.getAllStats().observe(getViewLifecycleOwner(), stats -> onChanged(stats));
    }

    @Override
    public void sendStat(int weight, int fat) {
        if (mShouldUpdate) {
            mViewModel.update(new Stat(LocalDate.now(), weight, fat));
        } else {
            mViewModel.insert(new Stat(LocalDate.now(), weight, fat));
        }
    }

    private void startDialogFragment() {
        AddStatDialogFragment addStatDialogFragment = AddStatDialogFragment.newInstance(Integer.parseInt(mWeightInput), Integer.parseInt(mFatInput));
        addStatDialogFragment.setTargetFragment(this, 1);
        if (getFragmentManager() != null) {
            addStatDialogFragment.show(getFragmentManager(), "Update Stats");
        } else {
            Log.e(TAG, "startDialogFragment: Could not get reference to fragment manager");
        }
    }

    private void onChanged(List<Stat> stats) {
        mWeightChart.setNoDataTextColor(getResources().getColor(R.color.evening_blue, null));
        mWeightChart.setNoDataTextTypeface(getResources().getFont(R.font.open_sans));
        mWeightChart.setNoDataText(getResources().getString(R.string.no_data));

        mFatChart.setNoDataTextColor(getResources().getColor(R.color.evening_blue, null));
        mFatChart.setNoDataTextTypeface(getResources().getFont(R.font.open_sans));
        mFatChart.setNoDataText(getResources().getString(R.string.no_data));

        if (stats.size() == 0) {
            mWeightInput = MainActivity.EMPTY + "";
            mWeightChart.invalidate();
            mFatInput = MainActivity.EMPTY + "";
            mFatChart.invalidate();
            return;
        }

        Collections.sort(stats, (Stat o1, Stat o2) -> o1.getMDate().compareTo(o2

                .getMDate()));

        Stat lastEntry = stats.get(stats.size() - 1);
        if (lastEntry.getMDate().isEqual(LocalDate.now())) {
            mShouldUpdate = true;
            mWeightInput = lastEntry.getMWeight() + "";
            if (lastEntry.getMWeight() != MainActivity.EMPTY) {
                String displayWeight = mWeightInput + " lbs";
                mWeightInputTV.setText(displayWeight);
            } else {
                mWeightInputTV.setText(getResources().getString(R.string.none));
            }
            mFatInput = lastEntry.getMBodyFat() + "";
            if (lastEntry.getMBodyFat() != MainActivity.EMPTY) {
                String displayFat = mFatInput + "%";
                mFatInputTV.setText(displayFat);
            } else {
                mFatInputTV.setText(getResources().getString(R.string.none));
            }
        } else {
            mWeightInput = MainActivity.EMPTY + "";
            mFatInput = MainActivity.EMPTY + "";
            mWeightInputTV.setText(getResources().getString(R.string.none));
            mFatInputTV.setText(getResources().getString(R.string.none));
        }

        int yWeightMax = 0;
        int yWeightMin = 1000;
        int yBodyFatMax = 0;
        int yBodyFatMin = 100;

        List<Entry> weightEntries = new ArrayList<>();
        List<Entry> fatEntries = new ArrayList<>();
        List<Stat> weightOnlyList = new ArrayList<>();
        List<Stat> fatOnlyList = new ArrayList<>();

        for (Stat stat : stats) {
            if (stat.getMWeight() != MainActivity.EMPTY) {
                weightOnlyList.add(stat);
                weightEntries.add(new Entry(weightOnlyList.size(), stat.getMWeight()));
                if (stat.getMWeight() > yWeightMax) yWeightMax = stat.getMWeight();
                if (stat.getMWeight() < yWeightMin) yWeightMin = stat.getMWeight();
            }
            if (stat.getMBodyFat() != MainActivity.EMPTY) {
                fatOnlyList.add(stat);
                fatEntries.add(new Entry(fatOnlyList.size(), stat.getMBodyFat()));
                if (stat.getMBodyFat() > yBodyFatMax) yBodyFatMax = stat.getMBodyFat();
                if (stat.getMBodyFat() < yBodyFatMin) yBodyFatMin = stat.getMBodyFat();
            }
        }

        FloatToIntegerFormatter integerFormatter = new FloatToIntegerFormatter();

        if (weightEntries.size() > 0) {

            float xWeightMin = weightEntries.get(0).getX() - 1;
            float xWeightMax = weightEntries.get(weightEntries.size() - 1).getX() + 1;
            mWeightChart.getXAxis().setAxisMinimum(xWeightMin);
            mWeightChart.getXAxis().setAxisMaximum(xWeightMax);

            LineDataSet weightLineDataSet = new LineDataSet(weightEntries, "Weight (lb)");

            weightLineDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
            weightLineDataSet.setValueFormatter((ValueFormatter) integerFormatter);
            weightLineDataSet.setLineWidth(3f);
            weightLineDataSet.setValueTextSize(10f);
            weightLineDataSet.setColor(getResources().getColor(R.color.sailor_blue, null));
            weightLineDataSet.setCircleColor(getResources().getColor(R.color.sailor_blue, null));
            weightLineDataSet.getCircleHoleColor();
            weightLineDataSet.setCircleRadius(4f);

            List<ILineDataSet> weightListOfData = new ArrayList<>();
            weightListOfData.add(weightLineDataSet);

            LineData weightLineData = new LineData(weightListOfData);

            weightLineData.setValueTypeface(getResources().getFont(R.font.open_sans));

            mWeightChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
            CustomDateFormatter weightDateFormatter = new CustomDateFormatter(weightOnlyList);
            mWeightChart.getXAxis().setValueFormatter(weightDateFormatter);
            mWeightChart.getXAxis().setTypeface(getResources().getFont(R.font.open_sans));
            mWeightChart.getXAxis().setGranularity(1f);

            mWeightChart.getAxisLeft().setAxisMinimum(yWeightMin - 25 > 0 ? yWeightMin - 25 : 0);
            mWeightChart.getAxisLeft().setAxisMaximum(yWeightMax + 25);

            mWeightChart.setVisibleXRangeMaximum(8);
            mWeightChart.setVisibleXRangeMinimum(2);
            mWeightChart.moveViewToX(weightEntries.size() + 3);

            mWeightChart.getAxisRight().setDrawGridLines(false);
            mWeightChart.getXAxis().setDrawGridLines(false);
            mWeightChart.getAxisLeft().setDrawGridLines(false);

            mWeightChart.getAxisLeft().setDrawLabels(false);
            mWeightChart.getAxisLeft().setDrawAxisLine(false);
            mWeightChart.getAxisRight().setDrawLabels(false);
            mWeightChart.getAxisRight().setDrawAxisLine(false);

            mWeightChart.getDescription().setEnabled(false);

            mWeightChart.getLegend().setEnabled(false);

            mWeightChart.setDoubleTapToZoomEnabled(false);

            mWeightChart.setData(weightLineData);
            mWeightChart.animateX(500, Easing.EaseInCubic);
        } else {
            mWeightChart.clear();
            mWeightChart.invalidate();
        }
        if (fatEntries.size() > 0) {

            float xBodyFatMin = fatEntries.get(0).getX() - 1;
            float xBodyFatMax = fatEntries.get(fatEntries.size() - 1).getX() + 1;
            mFatChart.getXAxis().setAxisMinimum(xBodyFatMin);
            mFatChart.getXAxis().setAxisMaximum(xBodyFatMax);

            LineDataSet bodyFatLineDataSet = new LineDataSet(fatEntries, "Body Fat (%)");

            bodyFatLineDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
            bodyFatLineDataSet.setValueFormatter((ValueFormatter) integerFormatter);
            bodyFatLineDataSet.setLineWidth(3f);
            bodyFatLineDataSet.setValueTextSize(10f);
            bodyFatLineDataSet.setColor(getResources().getColor(R.color.reply_orange,

                    null));
            bodyFatLineDataSet.setCircleColor(getResources().getColor(R.color.reply_orange, null));
            bodyFatLineDataSet.getCircleHoleColor();
            bodyFatLineDataSet.setCircleRadius(4f);

            List<ILineDataSet> bodyFatListOfData = new ArrayList<>();
            bodyFatListOfData.add(bodyFatLineDataSet);

            LineData bodyFatLineData = new LineData(bodyFatListOfData);

            bodyFatLineData.setValueTypeface(getResources().getFont(R.font.open_sans));

            mFatChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
            CustomDateFormatter fatDateFormatter = new CustomDateFormatter(fatOnlyList);
            mFatChart.getXAxis().setValueFormatter(fatDateFormatter);
            mFatChart.getXAxis().setTypeface(getResources().getFont(R.font.open_sans));
            mFatChart.getXAxis().setGranularity(1f);

            mFatChart.getAxisLeft().setAxisMinimum(yBodyFatMin - 10 > 0 ? yBodyFatMin - 10 : 0);
            mFatChart.getAxisLeft().setAxisMaximum(yBodyFatMax + 10);

            mFatChart.setVisibleXRangeMaximum(8);
            mFatChart.setVisibleXRangeMinimum(2);
            mFatChart.moveViewToX(fatEntries.size() + 3);

            mFatChart.getAxisRight().setDrawGridLines(false);
            mFatChart.getXAxis().setDrawGridLines(false);
            mFatChart.getAxisLeft().setDrawGridLines(false);

            mFatChart.getAxisLeft().setDrawAxisLine(false);
            mFatChart.getAxisLeft().setDrawLabels(false);
            mFatChart.getAxisRight().setDrawAxisLine(false);
            mFatChart.getAxisRight().setDrawLabels(false);

            mFatChart.getDescription().setEnabled(false);

            mFatChart.getLegend().setEnabled(false);

            mFatChart.setDoubleTapToZoomEnabled(false);

            mFatChart.setData(bodyFatLineData);
            mFatChart.animateX(500, Easing.EaseInCubic);
        } else {
            mFatChart.clear();
            mFatChart.invalidate();
        }

    }

}
