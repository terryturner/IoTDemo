package com.example.goldtek.iot.demo;

import android.app.Activity;
import android.app.Application;
import android.content.Context;

/**
 * Created by Terry on 2018/05/31.
 */
public class GoldtekApplication extends Application {
    private static Context mContext = null;

    public static Context getContext() {
        return mContext;
    }

    private BaseActivityLifecycleCallbacks mActCallback;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();

        mActCallback = new BaseActivityLifecycleCallbacks();
        registerActivityLifecycleCallbacks(mActCallback);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();



        System.exit(0);
    }

    public BaseActivityLifecycleCallbacks getBaseALC() {
        return mActCallback;
    }
}
