package com.example.lora.http;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.util.Log;

import com.example.goldtek.iot.demo.GoldtekApplication;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Terry on 2018/05/30.
 */
public class GetSensors implements IGetSensors {
    private List<Callback> mCallbacks = new ArrayList<>();
    private Map<Sensor, Bundle> mValues = new HashMap<>();
    private IGwConnector mConnector = new RaspberryGwConnector2(GoldtekApplication.getContext());
    private List<IGwConnector> mConnectors = new ArrayList<>();

    private WorkHandler mWorkHandler;
    private HandlerThread mWorkThread;
    private Handler mMain;

    public GetSensors() {
        mWorkThread = new HandlerThread("Work");
        mWorkThread.start();
        mWorkHandler = new WorkHandler(mWorkThread.getLooper());

        mMain = new Handler();

        mConnectors.add(new DummyGwConnector());
        mConnectors.add(new AmebaGwConnector());
        mConnectors.add(new RaspberryGwConnector2(GoldtekApplication.getContext()));
    }

    @Override
    public void keepconnect(final int gw, final String ip) {

    }

    @Override
    public void connect(final int gw, final String ip) {

        mWorkHandler.post(new Runnable() {
            @Override
            public void run() {
                if (0 <= gw && gw < mConnectors.size())
                    mConnector = mConnectors.get(gw);
                else
                    mConnector = mConnectors.get(0);

                final boolean connect = mConnector.connect(ip);
                mMain.removeCallbacksAndMessages(null);

                if (connect) {
                    mMain.postDelayed(new Request(mMain, Sensor.ALL, 200), 1000);
                }

                // Notify UI: connect state
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
        mConnector.disconnect();
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
