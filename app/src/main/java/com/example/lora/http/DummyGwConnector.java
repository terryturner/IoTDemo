package com.example.lora.http;

import android.os.Bundle;

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
                bundle.putDouble(IGetSensors.KEY_ACCELEROMETER_X, r.nextDouble() * 100);
                bundle.putDouble(IGetSensors.KEY_ACCELEROMETER_Y, r.nextDouble() * 100);
                bundle.putDouble(IGetSensors.KEY_ACCELEROMETER_Z, r.nextDouble() * 100);
                bundle.putInt(IGetSensors.KEY_AMBIENT_LIGHT, r.nextInt(1000));
                bundle.putInt(IGetSensors.KEY_BATTERY, r.nextInt(100));
                bundle.putFloat(IGetSensors.KEY_ELECTRIC_A, r.nextFloat() * 10);
                bundle.putFloat(IGetSensors.KEY_ELECTRIC_V, r.nextFloat() + 12);
                bundle.putInt(IGetSensors.KEY_GAS_PPM, r.nextInt(1000));
                bundle.putInt(IGetSensors.KEY_GYRO_ANGLE, r.nextInt(32767*2) - 32767);
                bundle.putInt(IGetSensors.KEY_HUMIDITY, r.nextInt(100));
                bundle.putFloat(IGetSensors.KEY_MAGNETIC, r.nextInt(2700) + 300);
                bundle.putInt(IGetSensors.KEY_PROXIMITY, r.nextInt(59) + 1);
                bundle.putInt(IGetSensors.KEY_TEMPERATURE_C, r.nextInt(150) - 50);
                bundle.putFloat(IGetSensors.KEY_VIBRATION, r.nextFloat() * 3);
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
                bundle.putInt(IGetSensors.KEY_HUMIDITY, r.nextInt(100));
                break;
            case Magnetic:
                bundle.putFloat(IGetSensors.KEY_MAGNETIC, r.nextInt(2700) + 300);
                break;
            case Proximity:
                bundle.putInt(IGetSensors.KEY_PROXIMITY, r.nextInt(59) + 1);
                break;
            case Temperature:
                bundle.putInt(IGetSensors.KEY_TEMPERATURE_C, r.nextInt(150) - 50);
                break;
            case Vibration:
                bundle.putFloat(IGetSensors.KEY_VIBRATION, r.nextFloat() * 3);
                break;
        }
        return bundle;
    }
}
