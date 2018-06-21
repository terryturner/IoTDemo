package com.example.lora.http;

import android.os.Bundle;
import android.util.Log;

import java.util.Random;

/**
 * Created by Terry on 2018/05/31.
 */
public class DummyGwConnector implements IGwConnector {
    private Random r = new Random();

    @Override
    public boolean connect(String url) {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return true;
    }

    @Override
    public void disconnect() {

    }

    @Override
    public void stop() {

    }

    @Override
    public Bundle getValue(Sensor type) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(IGetSensors.KEY_RESPONSE_STATE, true);
        switch (type) {
            case ALL:
                Bundle fake = getValue(Sensor.Accelerometer);
                bundle.putDouble(IGetSensors.KEY_ACCELEROMETER_X, fake.getDouble(IGetSensors.KEY_ACCELEROMETER_X));
                bundle.putDouble(IGetSensors.KEY_ACCELEROMETER_Y, fake.getDouble(IGetSensors.KEY_ACCELEROMETER_Y));
                bundle.putDouble(IGetSensors.KEY_ACCELEROMETER_Z, fake.getDouble(IGetSensors.KEY_ACCELEROMETER_Z));
                bundle.putInt(IGetSensors.KEY_AMBIENT_LIGHT,    getValue(Sensor.AmbientLight).getInt(IGetSensors.KEY_AMBIENT_LIGHT));
                bundle.putInt(IGetSensors.KEY_BATTERY,          getValue(Sensor.Battery).getInt(IGetSensors.KEY_BATTERY));
                fake = getValue(Sensor.Battery);
                bundle.putFloat(IGetSensors.KEY_ELECTRIC_A, fake.getFloat(IGetSensors.KEY_ELECTRIC_A));
                bundle.putFloat(IGetSensors.KEY_ELECTRIC_V, fake.getFloat(IGetSensors.KEY_ELECTRIC_V));
                bundle.putInt(IGetSensors.KEY_GAS_PPM,          getValue(Sensor.Gas).getInt(IGetSensors.KEY_GAS_PPM));
                bundle.putInt(IGetSensors.KEY_GYRO_ANGLE,       getValue(Sensor.Gyro).getInt(IGetSensors.KEY_GYRO_ANGLE));
                bundle.putInt(IGetSensors.KEY_HUMIDITY,         getValue(Sensor.Humidity).getInt(IGetSensors.KEY_HUMIDITY));
                bundle.putFloat(IGetSensors.KEY_MAGNETIC,       getValue(Sensor.Magnetic).getFloat(IGetSensors.KEY_MAGNETIC));
                bundle.putInt(IGetSensors.KEY_PROXIMITY,        getValue(Sensor.Proximity).getInt(IGetSensors.KEY_PROXIMITY));
                bundle.putInt(IGetSensors.KEY_TEMPERATURE_C,    getValue(Sensor.Temperature).getInt(IGetSensors.KEY_TEMPERATURE_C));
                bundle.putFloat(IGetSensors.KEY_VIBRATION,      getValue(Sensor.Vibration).getFloat(IGetSensors.KEY_VIBRATION));
                break;
            case Accelerometer:
                bundle.putDouble(IGetSensors.KEY_ACCELEROMETER_X, r.nextDouble() * 100);
                bundle.putDouble(IGetSensors.KEY_ACCELEROMETER_Y, r.nextDouble() * 100);
                bundle.putDouble(IGetSensors.KEY_ACCELEROMETER_Z, r.nextDouble() * 100);
                break;
            case AmbientLight:
                bundle.putInt(IGetSensors.KEY_AMBIENT_LIGHT, r.nextInt(1000));
                break;
            case Battery:
                bundle.putInt(IGetSensors.KEY_BATTERY, r.nextInt(100));
                break;
            case Electric:
                bundle.putFloat(IGetSensors.KEY_ELECTRIC_A, r.nextFloat() * 10);
                bundle.putFloat(IGetSensors.KEY_ELECTRIC_V, r.nextFloat() + 12);
                break;
            case Gas:
                bundle.putInt(IGetSensors.KEY_GAS_PPM, r.nextInt(1000));
                break;
            case Gyro:
                bundle.putInt(IGetSensors.KEY_GYRO_ANGLE, r.nextInt(32767*2) - 32767);
                break;
            case Humidity:
                bundle.putInt(IGetSensors.KEY_HUMIDITY, r.nextInt(40) + 40);
                break;
            case Magnetic:
                bundle.putFloat(IGetSensors.KEY_MAGNETIC, r.nextInt(2700) + 300);
                break;
            case Proximity:
                bundle.putInt(IGetSensors.KEY_PROXIMITY, r.nextInt(59) + 1);
                break;
            case Temperature:
                bundle.putInt(IGetSensors.KEY_TEMPERATURE_C, r.nextInt(10) + 25);
                break;
            case Vibration:
                bundle.putFloat(IGetSensors.KEY_VIBRATION, r.nextFloat() * 3);
                break;
        }
        return bundle;
    }
}
