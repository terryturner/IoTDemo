package com.example.ameba.http;

import android.os.Bundle;
import android.support.annotation.NonNull;

/**
 * Created by Terry on 2018/05/30.
 */
public interface IGetSensors {
    String KEY_RESPONSE_STATE = "response_state";
    String KEY_ACCELEROMETER_X = "acc_x";
    String KEY_ACCELEROMETER_Y = "acc_y";
    String KEY_ACCELEROMETER_Z = "acc_z";
    String KEY_AMBIENT_LIGHT = "amb_lux";
    String KEY_ELECTRIC_V = "electric_v";
    String KEY_ELECTRIC_A = "electric_a";
    String KEY_GAS_PPM = "gas_ppm";
    String KEY_GYRO_ANGLE = "gyro_degree";
    String KEY_HUMIDITY = "humidity_value";
    String KEY_MAGNETIC = "magnetic_mt";
    String KEY_MAGNETIC_X = "magnetic_x";
    String KEY_MAGNETIC_Y = "magnetic_y";
    String KEY_MAGNETIC_Z = "magnetic_z";
    String KEY_PROXIMITY = "proximity_mm";
    String KEY_TEMPERATURE_C = "temperature_celsius";
    String KEY_VIBRATION = "vibration_mm";
    String KEY_BATTERY = "battery_mh";

    interface Callback {
        void onConnect(boolean result);
        void onRequest(Sensor type);
        void onGetValue(Sensor type, Bundle value);
    }

    void connect(boolean isDevices, String url);
    void disconnect();
    void stop();
    void setOnValuesChangeListener(@NonNull Callback cb);
    void removeValueChangeListener(@NonNull Callback cb);
    Bundle getValue(Sensor type);
}
