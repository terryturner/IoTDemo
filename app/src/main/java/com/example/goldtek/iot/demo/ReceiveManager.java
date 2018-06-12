package com.example.goldtek.iot.demo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Terry on 2018/05/14.
 */
public class ReceiveManager {
    private List<BroadcastReceiver> receivers = new ArrayList<BroadcastReceiver>();
    private Context context;

    public ReceiveManager(Context context){
        this.context = context;
    }

    public Intent registerReceiver(BroadcastReceiver receiver, IntentFilter intentFilter) {
        if (!isReceiverRegistered(receiver)) receivers.add(receiver);
        Intent intent = context.registerReceiver(receiver, intentFilter);
        return intent;
    }

    public boolean isReceiverRegistered(BroadcastReceiver receiver){
        boolean registered = receivers.contains(receiver);
        return registered;
    }

    public void unregisterReceiver(BroadcastReceiver receiver){
        if (isReceiverRegistered(receiver)){
            receivers.remove(receiver);
            context.unregisterReceiver(receiver);
        }
    }
}
