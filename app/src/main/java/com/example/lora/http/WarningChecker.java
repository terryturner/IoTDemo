package com.example.lora.http;

import android.os.Bundle;
import android.util.Log;

import com.example.goldtek.iot.demo.GoldtekApplication;
import com.example.goldtek.iot.demo.R;
import com.example.goldtek.storage.IStorage;
import com.example.goldtek.storage.StorageCommon;

import java.util.HashMap;

/**
 * Created by Terry on 2018/06/21.
 */
public class WarningChecker {
    private final HashMap<Sensor, Bundle> mValues = new HashMap<>();
    private final IStorage mStorage;

    private boolean mConsiderHumOverLimit = false;
    private boolean mConsiderHumBelowLimit = false;
    private boolean mConsiderTempOverLimit = false;
    private boolean mConsiderTempBelowLimit = false;
    private boolean mConsiderVROverLimit = false;
    private boolean mConsiderAccOverLimit = false;

    private int mHumOverLimit = -1;
    private int mHumBelowLimit = -1;
    private int mTempOverLimit = -1;
    private int mTempBelowLimit = -1;
    private int mVROverLimit = -1;
    private int mAccOverLimit = -1;

    public WarningChecker(IStorage storage) {
        mStorage = storage;
    }

    public void updateConditions() {
        if (mStorage != null) {
            int[] hum_warings = GoldtekApplication.getContext().getResources().getIntArray(R.array.humidity_warning_values);
            int[] temp_warings = GoldtekApplication.getContext().getResources().getIntArray(R.array.temperature_warning_values);
            int[] vr_warings = GoldtekApplication.getContext().getResources().getIntArray(R.array.vr_warning_values);
            int[] acc_warings = GoldtekApplication.getContext().getResources().getIntArray(R.array.acc_warning_values);

            mConsiderHumOverLimit = mStorage.getBoolean(StorageCommon.HUM_OVER_LIMIT_SWITCH);
            mConsiderHumBelowLimit = mStorage.getBoolean(StorageCommon.HUM_BELOW_LIMIT_SWITCH);
            int index = mStorage.getInt(StorageCommon.HUM_OVER_LIMIT_VALUE);
            if (0 <= index && index < hum_warings.length)
                mHumOverLimit = hum_warings[index];
            else
                mHumOverLimit = hum_warings[0];
            index = mStorage.getInt(StorageCommon.HUM_BELOW_LIMIT_VALUE);
            if (0 <= index && index < hum_warings.length)
                mHumBelowLimit = hum_warings[index];
            else
                mHumBelowLimit = hum_warings[0];

            mConsiderTempOverLimit = mStorage.getBoolean(StorageCommon.TEMP_OVER_LIMIT_SWITCH);
            mConsiderTempBelowLimit = mStorage.getBoolean(StorageCommon.TEMP_BELOW_LIMIT_SWITCH);
            index = mStorage.getInt(StorageCommon.TEMP_OVER_LIMIT_VALUE);
            if (0 <= index && index < temp_warings.length)
                mTempOverLimit = temp_warings[index];
            else
                mTempOverLimit = temp_warings[0];
            index = mStorage.getInt(StorageCommon.TEMP_BELOW_LIMIT_VALUE);
            if (0 <= index && index < temp_warings.length)
                mTempBelowLimit = temp_warings[index];

            mConsiderVROverLimit = mStorage.getBoolean(StorageCommon.VR_OVER_LIMIT_SWITCH);
            index = mStorage.getInt(StorageCommon.VR_OVER_LIMIT_VALUE);
            if (0 <= index && index < vr_warings.length)
                mVROverLimit = vr_warings[index];
            else
                mVROverLimit = vr_warings[0];

            mConsiderAccOverLimit= mStorage.getBoolean(StorageCommon.ACC_OVER_LIMIT_SWITCH);
            index = mStorage.getInt(StorageCommon.ACC_OVER_LIMIT_VALUE);
            if (0 <= index && index < acc_warings.length)
                mAccOverLimit = acc_warings[index];
            else
                mAccOverLimit = acc_warings[0];

        }
//        Log.i("terry", String.format("Hum condition - Over(%b): %d, Below(%b): %d", mConsiderHumOverLimit, mHumOverLimit, mConsiderHumBelowLimit, mHumBelowLimit));
//        Log.i("terry", String.format("Temp condition - Over(%b): %d, Below(%b): %d", mConsiderTempOverLimit, mTempOverLimit, mConsiderTempBelowLimit, mTempBelowLimit));
//        Log.i("terry", String.format("VR condition - Over(%b): %d", mConsiderVROverLimit, mVROverLimit));
//        Log.i("terry", String.format("Acc condition - Over(%b): %d", mConsiderAccOverLimit, mAccOverLimit));
    }

    public boolean update(Sensor sensor, Bundle newValue) {
        boolean warning = false;

        if (newValue != null) {
            switch (sensor) {
                case Humidity:
                    if (mConsiderHumOverLimit && mConsiderHumBelowLimit) {
                        if (mHumOverLimit > mHumBelowLimit) {
                            warning = isOutside(newValue.getInt(IGetSensors.KEY_HUMIDITY, 0), mHumOverLimit, mHumBelowLimit);
                        } else if (mHumOverLimit < mHumBelowLimit) {
                            warning = isInside(newValue.getInt(IGetSensors.KEY_HUMIDITY, 0), mHumBelowLimit, mHumOverLimit);
                        } else {
                            warning = newValue.getInt(IGetSensors.KEY_HUMIDITY, 0) == mHumBelowLimit;
                        }
                    } else if (mConsiderHumOverLimit) {
                        warning = isOutside(newValue.getInt(IGetSensors.KEY_HUMIDITY, 0), mHumOverLimit, Integer.MIN_VALUE);
                    } else if (mConsiderHumBelowLimit) {
                        warning = isOutside(newValue.getInt(IGetSensors.KEY_HUMIDITY, 0), Integer.MAX_VALUE, mHumBelowLimit);
                    }
                    break;
                case Temperature:
                    if (mConsiderTempOverLimit && mConsiderTempBelowLimit) {
                        if (mTempOverLimit > mTempBelowLimit) {
                            warning = isOutside(newValue.getInt(IGetSensors.KEY_TEMPERATURE_C, 0), mTempOverLimit, mTempBelowLimit);
                        } else if (mTempOverLimit < mTempBelowLimit) {
                            warning = isInside(newValue.getInt(IGetSensors.KEY_TEMPERATURE_C, 0), mTempBelowLimit, mTempOverLimit);
                        } else {
                            warning = newValue.getInt(IGetSensors.KEY_TEMPERATURE_C, 0) == mTempBelowLimit;
                        }
                    } else if (mConsiderTempOverLimit) {
                        warning = isOutside(newValue.getInt(IGetSensors.KEY_TEMPERATURE_C, 0), mTempOverLimit, Integer.MIN_VALUE);
                    } else if (mConsiderTempBelowLimit) {
                        warning = isOutside(newValue.getInt(IGetSensors.KEY_TEMPERATURE_C, 0), Integer.MAX_VALUE, mTempBelowLimit);
                    }
                    break;
                case Proximity:
                    if (mConsiderVROverLimit) {
                        warning = isOutside(newValue.getInt(IGetSensors.KEY_PROXIMITY, 0), mVROverLimit*100, Integer.MIN_VALUE);
                    }
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


                        if (mConsiderAccOverLimit) {
                            if ((mAccOverLimit < Math.abs(oldX - newX) && Math.abs(oldX - newX) < 255 - mAccOverLimit)
                                    || (mAccOverLimit < Math.abs(oldY - newY) && Math.abs(oldY - newY) < 255 - mAccOverLimit)
                                    || (mAccOverLimit < Math.abs(oldZ - newZ) && Math.abs(oldZ - newZ) < 255 - mAccOverLimit))
                            {
                                warning = true;
                            }
                        }

                        break;
                }
            }
        }

        mValues.put(sensor, newValue);
        return warning;
    }

    private boolean isOutside(int value, int topLimit, int bottomLimit) {
        if (value >= topLimit || value <= bottomLimit) return true;
        else return false;
    }

    private boolean isInside(int value, int topLimit, int bottomLimit) {
        if (bottomLimit <= value && value <= topLimit) return true;
        else return false;
    }
}
