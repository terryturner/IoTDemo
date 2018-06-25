package com.example.lora.http;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.app.Activity;
import android.os.Vibrator;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.goldtek.iot.demo.R;
import com.example.goldtek.storage.IStorage;
import com.example.goldtek.storage.PrivatePreference;
import com.example.goldtek.storage.StorageCommon;

import java.util.Locale;

public class MainActivity extends Activity implements IGetSensors.Callback, View.OnClickListener, LoRaConnectDialog.OnClickListener {
    private IGetSensors mGetSensors = new GetSensors();
    private IStorage mStorage = new PrivatePreference(this);
    private RecyclerView mSensorList;
    private SensorsAdapter mSensorAdapter;
    private WarningChecker mWarningChecker;
    private Vibrator mVibrator;

    private final static int MY_PERMISSIONS_REQUEST_READ_PHONE_STATE = 0xA1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mVibrator = (Vibrator) getSystemService(Service.VIBRATOR_SERVICE);

        setContentView(R.layout.activity_lora_main);
        setTitle(R.string.lora_title_name);
        findViewById(R.id.btn_select).setTag(false);
        findViewById(R.id.btn_select).setOnClickListener(this);
        findViewById(R.id.imgAbout).setOnClickListener(this);
        findViewById(R.id.img_back).setOnClickListener(this);


        mSensorList = findViewById(R.id.lora_sensors);
        mSensorList.setLayoutManager(new GridLayoutManager(this, 2));

        String[] sensors = getResources().getStringArray(R.array.lora_sensors);
        int[] icons = new int[sensors.length];
        for (int idx=0; idx<sensors.length; idx++) {
            String sensor = String.format("ic_%s", sensors[idx].toLowerCase()).replace(" ", "_");
            icons[idx] = getResources().getIdentifier(sensor,"drawable",getPackageName());
        }
        mSensorAdapter = new SensorsAdapter(this, sensors, icons);
        mSensorList.setAdapter(mSensorAdapter);
    }

    @Override
    public void setTitle(int titleId) {
        super.setTitle(titleId);
        TextView textView = findViewById(R.id.textView9);
        if (textView != null) textView.setText(titleId);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mStorage.init(StorageCommon.FILE, Context.MODE_PRIVATE);
        mWarningChecker = new WarningChecker(mStorage);
        mWarningChecker.updateConditions();

        //requestReadPhonePermission();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mGetSensors.stop();
        mGetSensors.removeValueChangeListener(this);
        setConnectionEnabled(false);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_PHONE_STATE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }

    @Override
    public void onBackPressed(){
        moveTaskToBack(true);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_select:
                if (view.getTag() instanceof Boolean) {

                    boolean state = (boolean) view.getTag();
                    if (state) {
                        mGetSensors.disconnect();
                        mGetSensors.removeValueChangeListener(this);
                        setConnectionEnabled(false);
                    } else {
                        onShowConnectDialog();
                    }
                }
                break;
            case R.id.imgAbout:
                final Dialog dialog = new Dialog(this, R.style.MyCustomDialog);
                dialog.setTitle(getString(R.string.about));
                dialog.setContentView(R.layout.about);
                Button dialogButton = (Button) dialog.findViewById(R.id.btnOK);
                // if button is clicked, close the custom dialog
                dialogButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
                dialog.show();
                break;
            case R.id.img_back:
                new AlertDialog.Builder(MainActivity.this)
                        .setIcon(android.R.drawable.ic_menu_help)
                        .setTitle(R.string.back_title)
                        .setMessage(R.string.back_msg)
                        .setPositiveButton(R.string.back_ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                finish();
                            }
                        })
                        .setNegativeButton(R.string.cancel, null)
                        .show();
                break;
        }
    }

    @Override
    public void onClick(boolean correctIP, String ip, int gw) {
        if (correctIP) {
            if (requestReadPhonePermission()) {
                Toast.makeText(MainActivity.this, "Please connect again due to permission", Toast.LENGTH_SHORT).show();
                return;
            }

            mWarningChecker.updateConditions();
            mGetSensors.connect(gw, ip);
            mGetSensors.setOnValuesChangeListener(MainActivity.this);
            setConnectionEnabled(true);
        } else {
            Toast.makeText(MainActivity.this, "Error format: " + ip, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onConnect(boolean result) {
        if (!result) {
            mGetSensors.disconnect();
            mGetSensors.removeValueChangeListener(this);
            setConnectionEnabled(false);
            Toast.makeText(MainActivity.this, R.string.not_reachable_server, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequest(Sensor type) {
        // TODO: UI could refresh for waiting request data
    }

    @Override
    public void onGetValue(final Sensor type, final Bundle value) {
        if (value == null) return;

        String msg = null;
        switch (type) {
            case Accelerometer:
                msg = String.format(Locale.getDefault(), "%1$,.2f, %2$,.2f, %3$,.2f",
                        value.getDouble(IGetSensors.KEY_ACCELEROMETER_X, 0),
                        value.getDouble(IGetSensors.KEY_ACCELEROMETER_Y, 0),
                        value.getDouble(IGetSensors.KEY_ACCELEROMETER_Z, 0));
                break;
            case AmbientLight:
                msg = String.format(Locale.getDefault(), "%d lux", value.getInt(IGetSensors.KEY_AMBIENT_LIGHT, 0));
                break;
            case Battery:
                msg = String.format(Locale.getDefault(), "%d %%", value.getInt(IGetSensors.KEY_BATTERY, 0));
                break;
            case Electric:
                msg = String.format(Locale.getDefault(), "%1$,.2f A, %2$,.2f V",
                        value.getFloat(IGetSensors.KEY_ELECTRIC_A),
                        value.getFloat(IGetSensors.KEY_ELECTRIC_V));
                break;
            case Gas:
                msg = String.format(Locale.getDefault(), "%d ppm", value.getInt(IGetSensors.KEY_GAS_PPM, 0));
                break;
            case Gyro:
                msg = String.format(Locale.getDefault(), "%d °", value.getInt(IGetSensors.KEY_GYRO_ANGLE, 0));
                break;
            case Humidity:
                msg = String.format(Locale.getDefault(), "%d %%", value.getInt(IGetSensors.KEY_HUMIDITY, 0));
                break;
            case Magnetic:
                msg = String.format(Locale.getDefault(), "%1$,.2f mt", value.getFloat(IGetSensors.KEY_MAGNETIC, 0));
                break;
            case Proximity:
                msg = String.format(Locale.getDefault(), "%.2f mm", value.getInt(IGetSensors.KEY_PROXIMITY, 0)/100.0);
                break;
            case Temperature:
                msg = String.format(Locale.getDefault(), "%d °C", value.getInt(IGetSensors.KEY_TEMPERATURE_C, 0));
                break;
            case Vibration:
                msg = String.format(Locale.getDefault(), "%1$,.2f mm", value.getFloat(IGetSensors.KEY_VIBRATION, 0));
                break;
        }

        final String updateValue = msg;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mSensorAdapter != null) {
                    boolean warning = mWarningChecker.update(type, value);
                    mSensorAdapter.updateValue(type, updateValue, warning ? Color.RED : Color.BLACK);

                    if (type.equals(Sensor.Accelerometer) && mVibrator.hasVibrator()) {
                        if (warning) mVibrator.vibrate(1000);
                        else mVibrator.cancel();
                    }
                }
            }
        });
    }

    private void onShowConnectDialog() {
        LoRaConnectDialog dialog = new LoRaConnectDialog(this, R.style.MyCustomDialog, this);
        dialog.show();
    }

    private void setConnectionEnabled(boolean enabled) {
        if (enabled) {
            ((Button) findViewById(R.id.btn_select)).setText(R.string.disconnect);
            findViewById(R.id.btn_select).setBackgroundResource(R.drawable.btn_disable_background);
        } else {
            ((Button) findViewById(R.id.btn_select)).setText(R.string.connect);
            findViewById(R.id.btn_select).setBackgroundResource(R.drawable.btn_background);
        }
        findViewById(R.id.btn_select).setTag(enabled);
    }

    private boolean requestReadPhonePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_CONTACTS}, MY_PERMISSIONS_REQUEST_READ_PHONE_STATE);
            return true;
        } else {
            return false;
        }
    }
}
