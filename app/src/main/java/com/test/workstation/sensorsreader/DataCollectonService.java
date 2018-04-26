package com.test.workstation.sensorsreader;

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.IBinder;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;

public class DataCollectonService extends Service implements SensorEventListener {

    private static final String INTENT_KEY_TYPES = "sensorType";
    private static final String INTENT_KEY_NUMBERS = "valuesNumber";
    private SensorManager sensorManager = null;
    private Sensor sensor = null;
    private List<List<float[]>> dataList;
    private static List<Integer> fragmentList = new ArrayList<>();
    private boolean run;
    private ArrayList<Integer> valuesNumbers;

    private static List<PublishSubject<float[]>> publishSubjectList = new ArrayList<>();

    public static Observable<float[]> getObservable(int fragmentNumber) {
        fragmentList.add(fragmentNumber);
        PublishSubject<float[]> publishSubject = PublishSubject.create();
        publishSubjectList.add(publishSubject);
        return publishSubjectList.get(fragmentNumber);
    }

    static {
        System.loadLibrary("native-lib");
    }

    public native float[] getAv(List<float[]> dataList, int size);

    public DataCollectonService() { }

    @Override
    public void onCreate() {
        super.onCreate();
        Toast.makeText(this, "Service Started", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroy() {
        run = false;
        sensorManager.unregisterListener(this);
        Toast.makeText(this, "Service Destroyed", Toast.LENGTH_SHORT).show();
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        run = true;
        dataList = new ArrayList<>();

        ArrayList<String> types = intent.getStringArrayListExtra(INTENT_KEY_TYPES);
        valuesNumbers = intent.getIntegerArrayListExtra(INTENT_KEY_NUMBERS);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        if (sensorManager != null) {
            for (String s : types) {
                switch (s) {
                    case "accelerometer":
                        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
                        break;
                    case "gyroscope":
                        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
                        break;
                    case "gravity":
                        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
                        break;
                    case "light":
                        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
                        break;
                }
                dataList.add(new ArrayList<float[]>());
                sensorManager.registerListener(this, sensor,
                        SensorManager.SENSOR_DELAY_NORMAL);
            }
        } else {
            Toast.makeText(this, getResources().getString(R.string.could_not_access), Toast.LENGTH_SHORT).show();
        }

        delayedLaunch();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw null;
    }

    void calculateAverageValue() {
        final Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (dataList != null && run) {
                    for (int i = 0; i < dataList.size(); i++) {
                        sendData(i);
                    }
                    handler.postDelayed(this, 1000);
                }
            }
        });
    }

    private void delayedLaunch() {
        final Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000);
                    calculateAverageValue();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    void sendData(int i) {
        int size = valuesNumbers.get(i);
        float[] buf = getAv(dataList.get(i), size);
        publishSubjectList.get(i).onNext(buf);
        dataList.get(i).clear();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        switch (event.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                dataList.get(0).add(event.values.clone());
                break;
            case Sensor.TYPE_GYROSCOPE:
                dataList.get(1).add(event.values.clone());
                break;
            case Sensor.TYPE_GRAVITY:
                dataList.get(2).add(event.values.clone());
                break;
            case Sensor.TYPE_LIGHT:
                dataList.get(3).add(event.values.clone());
                break;
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
