package com.example.lora.http;

import android.os.Bundle;
import android.util.Log;

import java.util.HashMap;

/**
 * Created by Terry on 2018/06/21.
 */
public class WarningChecker {
    private final HashMap<Sensor, Bundle> mValues = new HashMap<>();

    public boolean update(Sensor sensor, Bundle newValue) {
        boolean warning = false;

        if (newValue != null) {
            switch (sensor) {
                case Humidity:
                    if (newValue.getInt(IGetSensors.KEY_HUMIDITY, 0) > 70 ||
                            newValue.getInt(IGetSensors.KEY_HUMIDITY, 0) < 60)
                        warning = true;
                    break;
                case Temperature:
                    if (newValue.getInt(IGetSensors.KEY_TEMPERATURE_C, 0) > 30 ||
                            newValue.getInt(IGetSensors.KEY_TEMPERATURE_C, 0) < 20)
                        warning = true;
                    break;
                case Proximity:
                    if (newValue.getInt(IGetSensors.KEY_PROXIMITY, 0)/100.0 > 10 ||
                            newValue.getInt(IGetSensors.KEY_PROXIMITY, 0) < -1)
                        warning = true;
                    break;
            }

            Bundle oldValue = mValues.get(sensor);
            if (oldValue != null) {
                switch (sensor) {
                    case Accelerometer:
                        double oldX = oldValue.getDouble(IGetSensors.KEY_ACCELEROMETER_X, 0);
                        double oldY = oldValue.getDouble(IGetSensors.KEY_ACCELEROMETER_Y, 0);
                        double oldZ = oldValue.getDouble(IGetSensors.KEY_ACCELEROMETER_Z, 0);

                        double newX = newValue.getDouble(IGetSensors.KEY_ACCELEROMETER_X, 0);
                        double newY = newValue.getDouble(IGetSensors.KEY_ACCELEROMETER_Y, 0);
                        double newZ = newValue.getDouble(IGetSensors.KEY_ACCELEROMETER_Z, 0);

                        int singleLimit = 5;
                        if ((singleLimit < Math.abs(oldX - newX) && Math.abs(oldX - newX) < 255 - singleLimit)
                                || (singleLimit < Math.abs(oldY - newY) && Math.abs(oldY - newY) < 255 - singleLimit)
                                || (singleLimit < Math.abs(oldZ - newZ) && Math.abs(oldZ - newZ) < 255 - singleLimit))
                        {
                            warning = true;
                        }

                        break;
                }
            }
        }

        mValues.put(sensor, newValue);
        return warning;
    }
}
