package com.example.lora.http;

import android.os.Bundle;
import android.util.Log;

import com.goldtek.iot.demo.GoldtekApplication;
import com.goldtek.iot.demo.R;
import com.example.goldtek.storage.IStorage;
import com.example.goldtek.storage.StorageCommon;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

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

    private Queue<Bundle> mAccTriggers = null;

    private boolean mHumAlarming = false;
    private boolean mTempAlarming = false;
    private boolean mVRAlarming = false;
    private boolean mAccAlarming = false;


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

        if (mAccTriggers != null) {
            mAccTriggers.clear();
            mAccTriggers = null;
        }
    }

    public boolean update(Sensor sensor, Bundle newValue) {
        boolean warning = false;

        if (newValue != null) {
            switch (sensor) {
                case Humidity:
                    if (mConsiderHumOverLimit && mConsiderHumBelowLimit) {
                        if (mHumOverLimit > mHumBelowLimit) {
                            warning = isOutside(newValue.getInt(IGetSensors.KEY_HUMIDITY, IGetSensors.VALUE_DEFAULT), mHumOverLimit, mHumBelowLimit);
                        } else if (mHumOverLimit < mHumBelowLimit) {
                            warning = isInside(newValue.getInt(IGetSensors.KEY_HUMIDITY, IGetSensors.VALUE_DEFAULT), mHumBelowLimit, mHumOverLimit);
                        } else {
                            warning = newValue.getInt(IGetSensors.KEY_HUMIDITY, IGetSensors.VALUE_DEFAULT) == mHumBelowLimit;
                        }
                    } else if (mConsiderHumOverLimit) {
                        warning = isOutside(newValue.getInt(IGetSensors.KEY_HUMIDITY, IGetSensors.VALUE_DEFAULT), mHumOverLimit, Integer.MIN_VALUE);
                    } else if (mConsiderHumBelowLimit) {
                        warning = isOutside(newValue.getInt(IGetSensors.KEY_HUMIDITY, IGetSensors.VALUE_DEFAULT), Integer.MAX_VALUE, mHumBelowLimit);
                    }
                    mHumAlarming = warning;
                    break;
                case Temperature:
                    if (mConsiderTempOverLimit && mConsiderTempBelowLimit) {
                        if (mTempOverLimit > mTempBelowLimit) {
                            warning = isOutside(newValue.getInt(IGetSensors.KEY_TEMPERATURE_C, IGetSensors.VALUE_DEFAULT), mTempOverLimit, mTempBelowLimit);
                        } else if (mTempOverLimit < mTempBelowLimit) {
                            warning = isInside(newValue.getInt(IGetSensors.KEY_TEMPERATURE_C, IGetSensors.VALUE_DEFAULT), mTempBelowLimit, mTempOverLimit);
                        } else {
                            warning = newValue.getInt(IGetSensors.KEY_TEMPERATURE_C, IGetSensors.VALUE_DEFAULT) == mTempBelowLimit;
                        }
                    } else if (mConsiderTempOverLimit) {
                        warning = isOutside(newValue.getInt(IGetSensors.KEY_TEMPERATURE_C, IGetSensors.VALUE_DEFAULT), mTempOverLimit, Integer.MIN_VALUE);
                    } else if (mConsiderTempBelowLimit) {
                        warning = isOutside(newValue.getInt(IGetSensors.KEY_TEMPERATURE_C, IGetSensors.VALUE_DEFAULT), Integer.MAX_VALUE, mTempBelowLimit);
                    }
                    mTempAlarming = warning;
                    break;
                case Proximity:
                    if (mConsiderVROverLimit) {
                        warning = isOutside(newValue.getInt(IGetSensors.KEY_PROXIMITY, IGetSensors.VALUE_DEFAULT), mVROverLimit*100, Integer.MIN_VALUE);
                    }
                    mVRAlarming = warning;
                    break;
            }

            Bundle oldValue = mValues.get(sensor);
            if (oldValue != null) {
                switch (sensor) {
                    case Accelerometer:
                        if (mConsiderAccOverLimit) {


                            double newX = newValue.getDouble(IGetSensors.KEY_ACCELEROMETER_X, IGetSensors.VALUE_DEFAULT);
                            double newY = newValue.getDouble(IGetSensors.KEY_ACCELEROMETER_Y, IGetSensors.VALUE_DEFAULT);
                            double newZ = newValue.getDouble(IGetSensors.KEY_ACCELEROMETER_Z, IGetSensors.VALUE_DEFAULT);

                            if (mAccTriggers == null) {
                                double oldX = oldValue.getDouble(IGetSensors.KEY_ACCELEROMETER_X, IGetSensors.VALUE_DEFAULT);
                                double oldY = oldValue.getDouble(IGetSensors.KEY_ACCELEROMETER_Y, IGetSensors.VALUE_DEFAULT);
                                double oldZ = oldValue.getDouble(IGetSensors.KEY_ACCELEROMETER_Z, IGetSensors.VALUE_DEFAULT);

                                if ((mAccOverLimit < Math.abs(oldX - newX) && Math.abs(oldX - newX) < 255 - mAccOverLimit)
                                        || (mAccOverLimit < Math.abs(oldY - newY) && Math.abs(oldY - newY) < 255 - mAccOverLimit)
                                        || (mAccOverLimit < Math.abs(oldZ - newZ) && Math.abs(oldZ - newZ) < 255 - mAccOverLimit))
                                {
                                    Log.i("terry", "earthquake");

                                    mAccTriggers = new LinkedList<>();
                                    mAccTriggers.offer(oldValue);
                                    warning = true;
                                }
                            } else if (mAccTriggers.peek() != null) {

                                if (oldValue.getLong(IGetSensors.KEY_RESPONSE_TIME, -1) <
                                        newValue.getLong(IGetSensors.KEY_RESPONSE_TIME, -1)) {
                                    double oldX = mAccTriggers.peek().getDouble(IGetSensors.KEY_ACCELEROMETER_X, IGetSensors.VALUE_DEFAULT);
                                    double oldY = mAccTriggers.peek().getDouble(IGetSensors.KEY_ACCELEROMETER_Y, IGetSensors.VALUE_DEFAULT);
                                    double oldZ = mAccTriggers.peek().getDouble(IGetSensors.KEY_ACCELEROMETER_Z, IGetSensors.VALUE_DEFAULT);

                                    if ((mAccOverLimit < Math.abs(oldX - newX) && Math.abs(oldX - newX) < 255 - mAccOverLimit)
                                            || (mAccOverLimit < Math.abs(oldY - newY) && Math.abs(oldY - newY) < 255 - mAccOverLimit)
                                            || (mAccOverLimit < Math.abs(oldZ - newZ) && Math.abs(oldZ - newZ) < 255 - mAccOverLimit))
                                    {
                                        mAccTriggers.clear();
                                    }
                                    mAccTriggers.offer(newValue);
                                    warning = true;

                                    if (mAccTriggers.size() >= 5) {
                                        Log.i("terry", "peace");

                                        mAccTriggers.clear();
                                        mAccTriggers = null;
                                        warning = false;
                                    } else {
                                        Log.i("terry", "dbg " + mAccTriggers.size());
                                    }
                                } else {
                                    warning = true;
                                }
                            }
                        }
                        mAccAlarming = warning;
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

    public boolean isHumidityAlarming() {
        return mHumAlarming;
    }

    public boolean isTemperatureAlarming() {
        return mTempAlarming;
    }

    public boolean isBreak() {
        return mVRAlarming;
    }

    public boolean isEarthQuake() {
        return (mAccTriggers != null);
    }

    public int getEarthQuakeTime() {
        if (isEarthQuake()) {
            return mAccTriggers.size();
        }
        return -1;
    }

}
