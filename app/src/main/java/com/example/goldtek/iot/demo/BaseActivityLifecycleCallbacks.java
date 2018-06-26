package com.example.goldtek.iot.demo;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

/**
 * Created by Terry on 2018/06/26.
 */
public class BaseActivityLifecycleCallbacks implements Application.ActivityLifecycleCallbacks {
    public enum ActivityState {
        CREATED,STARTED, RESUMED, PAUSED, STOPPED, DESTR0YED;
    }

    private ActivityState loraHomeState;

    @Override
    public void onActivityCreated(Activity activity, Bundle bundle) {
        if (activity instanceof com.example.lora.http.MainActivity) {
            loraHomeState = ActivityState.CREATED;
        }
    }

    @Override
    public void onActivityStarted(Activity activity) {
        if (activity instanceof com.example.lora.http.MainActivity) {
            loraHomeState = ActivityState.STARTED;
        }
    }

    @Override
    public void onActivityResumed(Activity activity) {
        if (activity instanceof com.example.lora.http.MainActivity) {
            loraHomeState = ActivityState.RESUMED;
        }
    }

    @Override
    public void onActivityPaused(Activity activity) {
        if (activity instanceof com.example.lora.http.MainActivity) {
            loraHomeState = ActivityState.PAUSED;
        }
    }

    @Override
    public void onActivityStopped(Activity activity) {
        if (activity instanceof com.example.lora.http.MainActivity) {
            loraHomeState = ActivityState.STOPPED;
        }
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        if (activity instanceof com.example.lora.http.MainActivity) {
            loraHomeState = ActivityState.DESTR0YED;
        }
    }

    public ActivityState getLoraHomeState() {
        return loraHomeState;
    }

}
