package com.test.workstation.sensorsreader;

import android.app.Fragment;
import android.graphics.Color;
import android.os.Bundle;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;


public class GraphFragment extends Fragment {

    private static final String ARG_PARAM1 = "fragmentId";
    private static final String ARG_PARAM2 = "sensorType";
    private static final String ARG_PARAM3 = "valuesNumber";

    private final int NUMBER_DISPLAYED_VALUES = 300;
    private final String[] ARGUMENTS = {"x", "y", "z"};

    private List<List<Entry>> entries;
    private static int x = 0;
    private LineChart chart;
    private int valuesNumber;
    private String sensorType;
    private int fragmentId;
    private Observer<float[]> observer;

    public GraphFragment() {
    }

    public static GraphFragment newInstance(int fragmentId, String sensorType, int valuesNumber) {

        GraphFragment fragment = new GraphFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PARAM1, fragmentId);
        args.putString(ARG_PARAM2, sensorType);
        args.putInt(ARG_PARAM3, valuesNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            fragmentId = getArguments().getInt(ARG_PARAM1);
            sensorType = getArguments().getString(ARG_PARAM2);
            valuesNumber = getArguments().getInt(ARG_PARAM3);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_graph, container, false);
        chart = rootView.findViewById(R.id.chart);
        Description desc = new Description();
        desc.setText(sensorType);
        chart.setDescription(desc);

        chart.getDescription().setEnabled(true);
        chart.setTouchEnabled(true);
        chart.setDragEnabled(true);
        chart.setScaleEnabled(true);
        chart.setDrawGridBackground(false);
        chart.setPinchZoom(true);
        chart.setBackgroundColor(Color.LTGRAY);

        entries = new ArrayList<>();
        for (int i = 0; i < valuesNumber; i++) {
            entries.add(new ArrayList<Entry>());
        }

        Observable<float[]> observable = DataCollectonService.getObservable(fragmentId)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread());

        observer = new Observer<float[]>() {
            @Override
            public void onSubscribe(Disposable d) {
            }

            @Override
            public void onNext(float[] floats) {
//                Log.d("TAG", String.valueOf(floats[0]) + " " + String.valueOf(floats[1]) + " " + String.valueOf(floats[2]) + "\n");
                for (int i = 0; i < valuesNumber; i++) {
                    entries.get(i).add(new Entry(x, floats[i]));
                }
                setDataOnGraph();
                x++;
            }
            @Override
            public void onError(Throwable e) {
                Log.e("TAG", "onError: ", e);
                Toast.makeText(getActivity(), getResources().getString(R.string.could_not_data), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onComplete() {
                Log.d("TAG", "onCompleted: ");
            }
        };

        observable.subscribe(observer);
        return rootView;
    }

    @Override
    public void onDestroy() {
        observer.onComplete();
        super.onDestroy();
    }

    private LineDataSet createSet(int i) {
        LineDataSet set = new LineDataSet(entries.get(i), ARGUMENTS[i]);
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setColor(ColorTemplate.MATERIAL_COLORS[i]);
        set.setCircleColor(Color.WHITE);
        set.setLineWidth(2f);
        set.setCircleRadius(2f);
        set.setFillAlpha(65);
        set.setFillColor(ColorTemplate.getHoloBlue());
        set.setHighLightColor(Color.rgb(244, 117, 117));
        set.setValueTextColor(Color.WHITE);
        set.setValueTextSize(9f);
        set.setDrawValues(false);
        return set;
    }

    private void setDataOnGraph() {
        LineData lineData = new LineData();
        for (int i = 0; i < valuesNumber; i++) {
            lineData.addDataSet(createSet(i));
        }
        lineData.notifyDataChanged();
        chart.setData(lineData);
        chart.notifyDataSetChanged();
        chart.moveViewToX(lineData.getEntryCount());
        chart.setVisibleXRangeMaximum(NUMBER_DISPLAYED_VALUES);
    }
}
