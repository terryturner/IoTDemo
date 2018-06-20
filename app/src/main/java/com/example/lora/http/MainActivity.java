package com.example.lora.http;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.app.Activity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.example.goldtek.iot.demo.R;
import com.example.goldtek.iot.demo.ServerValidator;
import com.example.goldtek.storage.IStorage;
import com.example.goldtek.storage.PrivatePreference;
import com.example.goldtek.storage.StorageCommon;

import java.util.Locale;

public class MainActivity extends Activity implements IGetSensors.Callback, View.OnClickListener {
    private IGetSensors mGetSensors = new GetSensors();
    private IStorage mStorage = new PrivatePreference(this);
    private SensorsAdapter mSensorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_lora_main);
        setTitle(R.string.lora_title_name);
        findViewById(R.id.btn_select).setTag(false);
        findViewById(R.id.btn_select).setOnClickListener(this);
        findViewById(R.id.imgAbout).setOnClickListener(this);

        String[] sensors = getResources().getStringArray(R.array.lora_sensors);
        int[] icons = new int[sensors.length];
        for (int idx=0; idx<sensors.length; idx++) {
            String sensor = String.format("ic_%s", sensors[idx].toLowerCase()).replace(" ", "_");
            icons[idx] = getResources().getIdentifier(sensor,"drawable",getPackageName());
        }
        mSensorAdapter = new SensorsAdapter(this, sensors, icons);

        RecyclerView sensorList = findViewById(R.id.lora_sensors);
        sensorList.setLayoutManager(new GridLayoutManager(this, 2));
        sensorList.setAdapter(mSensorAdapter);

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
    }

    @Override
    protected void onPause() {
        super.onPause();
        mGetSensors.disconnect();
        mGetSensors.removeValueChangeListener(this);
        setConnectionEnabled(false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mGetSensors.stop();
        mGetSensors.removeValueChangeListener(this);
        setConnectionEnabled(false);
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
                msg = String.format(Locale.getDefault(), "%d mm", value.getInt(IGetSensors.KEY_PROXIMITY, 0));
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
                    mSensorAdapter.updateValue(type, updateValue);
                }
            }
        });
    }

    private void onShowConnectDialog() {
        final Dialog dialog = new Dialog(this, R.style.MyCustomDialog);
        dialog.setContentView(R.layout.dialog_login);
        Button dialogButton = dialog.findViewById(R.id.btnOK);
        if (mStorage.getString(StorageCommon.LORA_SERVER_IP) != null) {
            EditText etServer = dialog.findViewById(R.id.etServer);
            etServer.setText(mStorage.getString(StorageCommon.LORA_SERVER_IP));
        }

        final Spinner dialogGwSelector = dialog.findViewById(R.id.lora_gw_selector);
        ArrayAdapter gw_options = ArrayAdapter.createFromResource(
                this, R.array.lora_gw_connectors, R.layout.spinner_item);
        gw_options.setDropDownViewResource(R.layout.spinner_item);
        dialogGwSelector.setAdapter(gw_options);
        dialogGwSelector.setSelection(mStorage.getInt(StorageCommon.LORA_GW_SELECTOR));


        // if button is clicked, close the custom dialog
        dialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ServerValidator validator = new ServerValidator();
                EditText etServer = ((ViewGroup) v.getParent()).findViewById(R.id.etServer);
                int gw = dialogGwSelector.getSelectedItemPosition();

                String ip = etServer.getText().toString();
                if (validator.isValidIPV4(ip)) {
                    mGetSensors.connect(gw, ip);
                    mGetSensors.setOnValuesChangeListener(MainActivity.this);

                    mStorage.putInt(StorageCommon.LORA_GW_SELECTOR, gw);
                    mStorage.putString(StorageCommon.LORA_SERVER_IP, ip);
                    setConnectionEnabled(true);
                } else {
                    Toast.makeText(MainActivity.this, "Error format: " + ip, Toast.LENGTH_SHORT).show();
                }

                dialog.dismiss();
            }
        });
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

}
