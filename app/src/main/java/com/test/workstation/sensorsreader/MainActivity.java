package com.test.workstation.sensorsreader;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Point;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.view.Display;
import android.view.ViewGroup;
import android.widget.LinearLayout;


import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {

    private static final String INTENT_KEY_TYPES = "sensorType";
    private static final String INTENT_KEY_NUMBERS = "valuesNumber";

    private LinearLayout container;
    private ArrayList<LinearLayout> linearLayouts;

    int screenWidth;
    int screenHeight;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        container = findViewById(R.id.mainLayout);

        ArrayList<String> sensorTypes = new ArrayList<>();
        sensorTypes.add(getResources().getString(R.string.type_accelerometer));
        sensorTypes.add(getResources().getString(R.string.type_gravity));
        sensorTypes.add(getResources().getString(R.string.type_gyroscope));
        sensorTypes.add("light");
        ArrayList<Integer> valuesNumber = new ArrayList<>();
        valuesNumber.add(3);
        valuesNumber.add(3);
        valuesNumber.add(3);
        valuesNumber.add(1);


        linearLayouts = new ArrayList<>();

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        screenWidth = size.x;
        screenHeight = size.y;

        //  if (savedInstanceState==null){
        Intent intent = new Intent(this, DataCollectonService.class);
        intent.putStringArrayListExtra(INTENT_KEY_TYPES, sensorTypes);
        intent.putIntegerArrayListExtra(INTENT_KEY_NUMBERS, valuesNumber);

        startService(intent);
        for (int i = 0; i < sensorTypes.size(); i++) {
            addGraphFragment(i, sensorTypes.get(i), valuesNumber.get(i));
        }
        //}


    }

    void addGraphFragment(int i, String sensorType, int valuesNumber) {
        linearLayouts.add(new LinearLayout(this));
        linearLayouts.get(i).setId(i + 1);
        getFragmentManager().beginTransaction().add(linearLayouts.get(i).getId(), GraphFragment
                .newInstance(i, sensorType, valuesNumber), "someTag" + i).commit();
        container.addView(linearLayouts.get(i));
        setLayoutSize(i, screenWidth, ViewGroup.LayoutParams.MATCH_PARENT);

    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopService(new Intent(this, DataCollectonService.class));
    }

    void setLayoutSize(int i, int width, int height) {
        ViewGroup.LayoutParams params = linearLayouts.get(i).getLayoutParams();
        params.height = height;
        params.width = width;
        linearLayouts.get(i).getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
        linearLayouts.get(i).requestLayout();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            container.setOrientation(LinearLayout.HORIZONTAL);
            for (int i = 0; i < 3; i++) {
                setLayoutSize(i, screenHeight / 2, screenWidth / 2);
            }
        }
        if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            container.setOrientation(LinearLayout.VERTICAL);
            for (int i = 0; i < 3; i++) {
                setLayoutSize(i, screenWidth, ViewGroup.LayoutParams.MATCH_PARENT);

            }
        }
    }
}
