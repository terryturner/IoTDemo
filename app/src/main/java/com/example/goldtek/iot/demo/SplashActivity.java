package com.example.goldtek.iot.demo;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.example.bletestv3.Common;

/**
 * Created by darwinhu on 2017/11/2.
 */

public class SplashActivity extends Activity {
    private final static int IOT_ITEM_BLE = 0;
    private final static int IOT_ITEM_LORA = 1;
    private final static int BLE_ITEM_HM10 = 0;
    private final static int BLE_ITEM_NORDIC = 1;

    private Context ctx;
    private Intent intentMain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ctx = this;
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        CreateIotOptions();
    }

    public void CreateIotOptions() {
        AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
        builder.setTitle("Choose the IoT function");

        final String[] values = getResources().getStringArray(R.array.iot_options);
        builder.setSingleChoiceItems(values, -1, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                if (item == IOT_ITEM_BLE) {
                    dialog.dismiss();
                    CreateBLEOptions();
                } else if (item == IOT_ITEM_LORA) {
                    intentMain = new Intent(getApplication(), com.example.lora.http.MainActivity.class);
                    startActivity(intentMain);
                    finish();
                }
            }
        });

        builder.create().show();
    }

    public void CreateBLEOptions() {
        AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
        builder.setTitle("Select Your Choice");

        final String[] values = getResources().getStringArray(R.array.ble_options);
        builder.setSingleChoiceItems(values, -1, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                switch (item) {
                    case BLE_ITEM_HM10:
                        Common.SetBLE(BLE_ITEM_HM10);
                        Toast.makeText(ctx, values[BLE_ITEM_HM10], Toast.LENGTH_LONG).show();
                        break;
                    case BLE_ITEM_NORDIC:
                        Common.SetBLE(BLE_ITEM_NORDIC);
                        Toast.makeText(ctx, values[BLE_ITEM_NORDIC], Toast.LENGTH_LONG).show();
                        break;
                }
                intentMain = new Intent(getApplication(), com.example.bletestv3.MainActivity.class);
                dialog.dismiss();
                startActivity(intentMain);
                finish();
            }
        });

        builder.create().show();
    }


}