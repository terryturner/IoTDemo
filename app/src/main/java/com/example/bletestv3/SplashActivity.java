package com.example.bletestv3;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

/**
 * Created by darwinhu on 2017/11/2.
 */

public class SplashActivity extends Activity {

    Context ctx;

    AlertDialog alertDialog1;
    CharSequence[] values = {"HM_10", "Nordic"};
    Intent intentMain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ctx = this;
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        CreateAlertDialogWithRadioButtonGroup() ;
        intentMain = new Intent(this, MainActivity.class);

        alertDialog1.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                startActivity(intentMain);
                finish();
            }
        });


    }

    public void CreateAlertDialogWithRadioButtonGroup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
        builder.setTitle("Select Your Choice");
        builder.setSingleChoiceItems(values, -1, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                switch (item) {
                    case 0:
                        Common.SetBLE(0);
                        Toast.makeText(ctx, values[0], Toast.LENGTH_LONG).show();
                        break;
                    case 1:
                        Common.SetBLE(1);
                        Toast.makeText(ctx, values[1], Toast.LENGTH_LONG).show();
                        break;
                }
                alertDialog1.dismiss();
            }
        });

        alertDialog1 = builder.create();
        alertDialog1.show();
    }



}