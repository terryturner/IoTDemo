package com.example.ameba.http;

import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Created by Terry on 2018/05/30.
 */
public class GetSensors implements IGetSensors {
    private List<Callback> mCallbacks = new ArrayList<>();
    private Map<Sensor, Bundle> mValues = new HashMap<>();
    private IGwConnector mConnector = new DummyGwConnector();

    private WorkHandler mWorkHandler;
    private HandlerThread mWorkThread;
    private Handler mMain;

    public GetSensors() {
        mWorkThread = new HandlerThread("Work");
        mWorkThread.start();
        mWorkHandler = new WorkHandler(mWorkThread.getLooper());

        mMain = new Handler();
    }

    @Override
    public void connect(boolean isDevices, final String ip) {
        if (isDevices) mConnector = new AmebaGwConnector();

        mWorkHandler.post(new Runnable() {
            @Override
            public void run() {
                String url = "http://" + ip;
                final boolean connect = mConnector.connect(url);
                mMain.removeCallbacksAndMessages(null);

                if (connect) {
                    mMain.postDelayed(new Request(mMain, Sensor.ALL, 1000), 1000);
                }

                mMain.post(new Runnable() {
                    @Override
                    public void run() {
                        for (IGetSensors.Callback cb : mCallbacks)
                            cb.onConnect(connect);
                    }
                });

            }
        });
    }

    @Override
    public void disconnect() {
        mMain.removeCallbacksAndMessages(null);
    }

    @Override
    public void stop() {
        mConnector.stop();

        mWorkHandler.removeCallbacksAndMessages(null);
        mWorkThread.interrupt();
        mWorkThread.quit();
    }

    @Override
    public void setOnValuesChangeListener(@NonNull Callback cb) {
        if (!mCallbacks.contains(cb)) mCallbacks.add(cb);
    }

    @Override
    public void removeValueChangeListener(@NonNull Callback cb) {
        mCallbacks.remove(cb);
    }

    @Override
    public synchronized Bundle getValue(Sensor type) {
        return mValues.get(type);
    }

    private class Request implements Runnable {
        private final Handler mHandler;
        private final Sensor mSensor;
        private final long mDelay;
        public Request(Handler handler, Sensor sensor, long delay) {
            mHandler = handler;
            mSensor = sensor;
            mDelay = delay;
        }
        @Override
        public void run() {
            mWorkHandler.sendMessage(Message.obtain(mWorkHandler, 0, mSensor));
            mHandler.postDelayed(this, mDelay);
        }
    }

    private class WorkHandler extends Handler {
        public WorkHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            if (msg.obj instanceof Sensor) {

                Sensor sensor = (Sensor) msg.obj;

                if (sensor.equals(Sensor.ALL)) {
                    for (Sensor idx : Sensor.values())
                        for (IGetSensors.Callback cb : mCallbacks)
                            if (cb != null) cb.onRequest(idx);

                    Bundle bundle = mConnector.getValue(sensor);
                    if (bundle != null && bundle.getBoolean(KEY_RESPONSE_STATE, false)) {
                        for (Sensor idx : Sensor.values()) {
                            synchronized (mValues) {
                                mValues.put(idx, bundle);
                            }
                        }
                    }

                    for (Sensor idx : Sensor.values())
                        for (IGetSensors.Callback cb : mCallbacks)
                            if (cb != null) cb.onGetValue(idx, mValues.get(sensor));

                } else {
                    for (IGetSensors.Callback cb : mCallbacks)
                        if (cb != null) cb.onRequest(sensor);

                    Bundle bundle = mConnector.getValue(sensor);
                    if (bundle != null && bundle.getBoolean(KEY_RESPONSE_STATE, false)) {
                        synchronized (mValues) {
                            mValues.put(sensor, bundle);
                        }
                    }

                    for (IGetSensors.Callback cb : mCallbacks)
                        if (cb != null) cb.onGetValue(sensor, mValues.get(sensor));
                }


            }

        }
    }
}
