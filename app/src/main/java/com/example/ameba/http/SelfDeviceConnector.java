package com.example.ameba.http;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.BatteryManager;
import android.os.Bundle;
import android.util.Log;

import com.example.goldtek.iot.demo.GoldtekApplication;
import com.example.goldtek.iot.demo.ReceiveManager;

import java.util.HashMap;
import java.util.Map;


/**
 * Created by Terry on 2018/05/31.
 */
public class SelfDeviceConnector implements IGwConnector, SensorEventListener {
    private Map<Sensor, Bundle> mValues = new HashMap<>();
    private final ReceiveManager mReceiveManager;

    private final SensorManager mSensorManager;
    private final android.hardware.Sensor mAccelerometer;
    private final android.hardware.Sensor mAmbientLight;
    private final android.hardware.Sensor mTemperature;
    private final android.hardware.Sensor mHumidity;
    private final android.hardware.Sensor mProximity;
    private final android.hardware.Sensor mGyro;
    private final android.hardware.Sensor mMagnetic;

    public SelfDeviceConnector() {
        mReceiveManager = new ReceiveManager(GoldtekApplication.getContext());
        mSensorManager = (SensorManager) GoldtekApplication.getContext().getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(android.hardware.Sensor.TYPE_ACCELEROMETER);
        mAmbientLight = mSensorManager.getDefaultSensor(android.hardware.Sensor.TYPE_LIGHT);
        mTemperature = mSensorManager.getDefaultSensor(android.hardware.Sensor.TYPE_AMBIENT_TEMPERATURE);
        mHumidity = mSensorManager.getDefaultSensor(android.hardware.Sensor.TYPE_RELATIVE_HUMIDITY);
        mProximity = mSensorManager.getDefaultSensor(android.hardware.Sensor.TYPE_PROXIMITY);
        mGyro = mSensorManager.getDefaultSensor(android.hardware.Sensor.TYPE_GYROSCOPE);
        mMagnetic = mSensorManager.getDefaultSensor(android.hardware.Sensor.TYPE_MAGNETIC_FIELD);
    }


    @Override
    public boolean connect(String url) {
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mAmbientLight, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mTemperature, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mHumidity, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mProximity, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mGyro, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mMagnetic, SensorManager.SENSOR_DELAY_NORMAL);

        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        mReceiveManager.registerReceiver(mBroadcastReceiver, filter);
        return true;
    }

    @Override
    public void stop() {
        mSensorManager.unregisterListener(this);
        mReceiveManager.unregisterReceiver(mBroadcastReceiver);
    }

    @Override
    public Bundle getValue(Sensor type) {
        return getBundle(type);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Bundle bundle;
        switch (event.sensor.getType()) {
            case android.hardware.Sensor.TYPE_ACCELEROMETER:
                bundle = getBundle(Sensor.Accelerometer);
                bundle.putDouble(IGetSensors.KEY_ACCELEROMETER_X, event.values[0]);
                bundle.putDouble(IGetSensors.KEY_ACCELEROMETER_Y, event.values[1]);
                bundle.putDouble(IGetSensors.KEY_ACCELEROMETER_Z, event.values[2]);
                mValues.put(Sensor.Accelerometer, bundle);
                break;
            case android.hardware.Sensor.TYPE_LIGHT:
                bundle = getBundle(Sensor.AmbientLight);
                bundle.putInt(IGetSensors.KEY_AMBIENT_LIGHT, (int) event.values[0]);
                mValues.put(Sensor.AmbientLight, bundle);
                break;
            case android.hardware.Sensor.TYPE_AMBIENT_TEMPERATURE:
                bundle = getBundle(Sensor.Temperature);
                bundle.putInt(IGetSensors.KEY_TEMPERATURE_C, (int) event.values[0]);
                mValues.put(Sensor.Temperature, bundle);
                break;
            case android.hardware.Sensor.TYPE_RELATIVE_HUMIDITY:
                //Log.i("terry",  "h: " + event.values.length + " " + event.values[0] + " " + event.values[1] + " " + event.values[2]);
                bundle = getBundle(Sensor.Humidity);
                bundle.putInt(IGetSensors.KEY_HUMIDITY, (int) event.values[0]);
                mValues.put(Sensor.Humidity, bundle);
                break;
            case android.hardware.Sensor.TYPE_PROXIMITY:
                bundle = getBundle(Sensor.Proximity);
                bundle.putInt(IGetSensors.KEY_PROXIMITY, (int) event.values[0]);
                mValues.put(Sensor.Proximity, bundle);
                break;
            case android.hardware.Sensor.TYPE_GYROSCOPE:
                bundle = getBundle(Sensor.Gyro);
                bundle.putInt(IGetSensors.KEY_GYRO_ANGLE, (int) event.values[0]);
                mValues.put(Sensor.Gyro, bundle);
                break;
            case android.hardware.Sensor.TYPE_MAGNETIC_FIELD:
                bundle = getBundle(Sensor.Magnetic);
                bundle.putFloat(IGetSensors.KEY_MAGNETIC, event.values[0]);
                bundle.putFloat(IGetSensors.KEY_MAGNETIC_X, event.values[0]);
                bundle.putFloat(IGetSensors.KEY_MAGNETIC_Y, event.values[1]);
                bundle.putFloat(IGetSensors.KEY_MAGNETIC_Z, event.values[2]);
                mValues.put(Sensor.Magnetic, bundle);
                break;
        }
    }

    @Override
    public void onAccuracyChanged(android.hardware.Sensor sensor, int i) {

    }

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // get the battery level
            if (intent.getAction().equalsIgnoreCase(Intent.ACTION_BATTERY_CHANGED)) {
                int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                Bundle bundle = getBundle(Sensor.Battery);
                bundle.putInt(IGetSensors.KEY_BATTERY, level);
                mValues.put(Sensor.Battery, bundle);
            }
        }
    };

    private Bundle getBundle(Sensor sensor) {
        Bundle bundle = mValues.get(sensor);
        if (bundle == null) bundle = new Bundle();
        return bundle;
    }
}
