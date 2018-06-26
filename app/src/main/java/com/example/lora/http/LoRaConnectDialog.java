package com.example.lora.http;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.goldtek.iot.demo.R;
import com.goldtek.iot.demo.ServerValidator;
import com.example.goldtek.storage.IStorage;
import com.example.goldtek.storage.PrivatePreference;
import com.example.goldtek.storage.StorageCommon;

/**
 * Created by Terry on 2018/06/21.
 */
public class LoRaConnectDialog extends Dialog implements View.OnClickListener, CompoundButton.OnCheckedChangeListener, AdapterView.OnItemSelectedListener {
    private final static String SELECT_HUM = "Humidity";
    private final static String SELECT_TEMP = "Temperature";
    private final static String SELECT_VR = "Proximity";
    private final static String SELECT_ACC = "Accelerometer";

    private final LoRaDialogCallback mClick;
    private final IStorage mStorage;
    private final Button mButtonOK;
    private final EditText mServerAddress;
    private final ImageView mImgSettings;
    private final ViewGroup mSettingsPanel;
    private final Spinner mSensorLimitSelector;
    private final Spinner mGatewaySelector;

    private final ToggleButton mOverLimitSwitch;
    private final ToggleButton mBelowLimitSwitch;
    private final Spinner mOverLimitSpinner;
    private final Spinner mBelowLimitSpinner;


    public interface OnClickListener {
        void onClick(boolean correctIP, String ip, int gw);
    }

    public LoRaConnectDialog(Context context, int themeResId, LoRaDialogCallback listener) {
        super(context, themeResId);
        mStorage = new PrivatePreference(context);
        mStorage.init(StorageCommon.FILE, Context.MODE_PRIVATE);
        mClick = listener;

        setContentView(R.layout.dialog_lora_connect);
        mButtonOK = findViewById(R.id.btnOK);
        mServerAddress = findViewById(R.id.etServer);
        mImgSettings = findViewById(R.id.settings);
        mSettingsPanel = findViewById(R.id.settingsPanel);
        mSensorLimitSelector = findViewById(R.id.lora_sensor_config_selector);
        mGatewaySelector = findViewById(R.id.lora_gw_selector);

        mOverLimitSwitch = findViewById(R.id.toggle_over_limit);
        mBelowLimitSwitch = findViewById(R.id.toggle_below_limit);
        mOverLimitSpinner = findViewById(R.id.spinner_over_limit);
        mBelowLimitSpinner = findViewById(R.id.spinner_below_limit);

        mButtonOK.setText(R.string.connect);


        if (mStorage.getString(StorageCommon.LORA_SERVER_IP) != null) {
            mServerAddress.setText(mStorage.getString(StorageCommon.LORA_SERVER_IP));
        }
        mImgSettings.setOnClickListener(this);
        mButtonOK.setOnClickListener(this);

        ArrayAdapter sensor_list = ArrayAdapter.createFromResource(
                context, R.array.lora_sensors, R.layout.spinner_item);
        sensor_list.setDropDownViewResource(R.layout.dropdown_spinner_item);
        mSensorLimitSelector.setAdapter(sensor_list);
        mSensorLimitSelector.setOnItemSelectedListener(this);

        ArrayAdapter gw_options = ArrayAdapter.createFromResource(
                context, R.array.lora_gw_connectors, R.layout.spinner_item);
        gw_options.setDropDownViewResource(R.layout.dropdown_spinner_item);
        mGatewaySelector.setAdapter(gw_options);
        mGatewaySelector.setSelection(mStorage.getInt(StorageCommon.LORA_GW_SELECTOR));

        mOverLimitSwitch.setOnCheckedChangeListener(this);
        mBelowLimitSwitch.setOnCheckedChangeListener(this);
        mOverLimitSpinner.setOnItemSelectedListener(this);
        mBelowLimitSpinner.setOnItemSelectedListener(this);
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.settings:
                if (mSettingsPanel.getVisibility() == View.GONE) {
                    mSettingsPanel.setVisibility(View.VISIBLE);
                } else {
                    mSettingsPanel.setVisibility(View.GONE);
                }
                InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(mServerAddress.getWindowToken(), 0);

                break;
            case R.id.btnOK:
                ServerValidator validator = new ServerValidator();
                int gw = mGatewaySelector.getSelectedItemPosition();

                String ip = mServerAddress.getText().toString();
                if (validator.isValidIPV4(ip)) {
                    mStorage.putInt(StorageCommon.LORA_GW_SELECTOR, gw);
                    mStorage.putString(StorageCommon.LORA_SERVER_IP, ip);
                    mClick.onClick(true, ip, gw);
                } else {
                    mClick.onClick(false, ip, gw);
                }

                dismiss();
                break;
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
        switch (adapterView.getId()) {
            case R.id.lora_sensor_config_selector:
                //Log.i("terry", "select: " + ((TextView) view).getText().toString());
                switch (((TextView) view).getText().toString()) {
                    case SELECT_HUM: {
                        ArrayAdapter over_limits = ArrayAdapter.createFromResource(getContext(), R.array.humidity_warning_labels, R.layout.spinner_item);
                        over_limits.setDropDownViewResource(R.layout.dropdown_spinner_item);
                        mOverLimitSpinner.setAdapter(over_limits);

                        ArrayAdapter below_limits = ArrayAdapter.createFromResource(getContext(), R.array.humidity_warning_labels, R.layout.spinner_item);
                        below_limits.setDropDownViewResource(R.layout.dropdown_spinner_item);
                        mBelowLimitSpinner.setAdapter(below_limits);

                        mOverLimitSwitch.setChecked(mStorage.getBoolean(StorageCommon.HUM_OVER_LIMIT_SWITCH));
                        mOverLimitSpinner.setEnabled(mStorage.getBoolean(StorageCommon.HUM_OVER_LIMIT_SWITCH));
                        mOverLimitSpinner.setSelection(mStorage.getInt(StorageCommon.HUM_OVER_LIMIT_VALUE));

                        mBelowLimitSwitch.setEnabled(true);
                        mBelowLimitSpinner.setVisibility(View.VISIBLE);

                        mBelowLimitSwitch.setChecked(mStorage.getBoolean(StorageCommon.HUM_BELOW_LIMIT_SWITCH));
                        mBelowLimitSpinner.setEnabled(mStorage.getBoolean(StorageCommon.HUM_BELOW_LIMIT_SWITCH));
                        mBelowLimitSpinner.setSelection(mStorage.getInt(StorageCommon.HUM_BELOW_LIMIT_VALUE));
                        break;
                    }
                    case SELECT_TEMP: {
                        ArrayAdapter over_limits = ArrayAdapter.createFromResource(getContext(), R.array.temperature_warning_labels, R.layout.spinner_item);
                        over_limits.setDropDownViewResource(R.layout.dropdown_spinner_item);
                        mOverLimitSpinner.setAdapter(over_limits);

                        ArrayAdapter below_limits = ArrayAdapter.createFromResource(getContext(), R.array.temperature_warning_labels, R.layout.spinner_item);
                        below_limits.setDropDownViewResource(R.layout.dropdown_spinner_item);
                        mBelowLimitSpinner.setAdapter(below_limits);

                        mOverLimitSwitch.setChecked(mStorage.getBoolean(StorageCommon.TEMP_OVER_LIMIT_SWITCH));
                        mOverLimitSpinner.setEnabled(mStorage.getBoolean(StorageCommon.TEMP_OVER_LIMIT_SWITCH));
                        mOverLimitSpinner.setSelection(mStorage.getInt(StorageCommon.TEMP_OVER_LIMIT_VALUE));

                        mBelowLimitSwitch.setEnabled(true);
                        mBelowLimitSpinner.setVisibility(View.VISIBLE);

                        mBelowLimitSwitch.setChecked(mStorage.getBoolean(StorageCommon.TEMP_BELOW_LIMIT_SWITCH));
                        mBelowLimitSpinner.setEnabled(mStorage.getBoolean(StorageCommon.TEMP_BELOW_LIMIT_SWITCH));
                        mBelowLimitSpinner.setSelection(mStorage.getInt(StorageCommon.TEMP_BELOW_LIMIT_VALUE));
                        break;
                    }
                    case SELECT_VR: {
                        ArrayAdapter over_limits = ArrayAdapter.createFromResource(getContext(), R.array.vr_warning_labels, R.layout.spinner_item);
                        over_limits.setDropDownViewResource(R.layout.dropdown_spinner_item);
                        mOverLimitSpinner.setAdapter(over_limits);

                        mOverLimitSwitch.setChecked(mStorage.getBoolean(StorageCommon.VR_OVER_LIMIT_SWITCH));
                        mOverLimitSpinner.setEnabled(mStorage.getBoolean(StorageCommon.VR_OVER_LIMIT_SWITCH));
                        mOverLimitSpinner.setSelection(mStorage.getInt(StorageCommon.VR_OVER_LIMIT_VALUE));

                        mBelowLimitSwitch.setChecked(false);
                        mBelowLimitSwitch.setEnabled(false);
                        mBelowLimitSpinner.setAdapter(over_limits);
                        mBelowLimitSpinner.setVisibility(View.INVISIBLE);
                        break;
                    }
                    case SELECT_ACC: {
                        ArrayAdapter over_limits = ArrayAdapter.createFromResource(getContext(), R.array.acc_warning_labels, R.layout.spinner_item);
                        over_limits.setDropDownViewResource(R.layout.dropdown_spinner_item);
                        mOverLimitSpinner.setAdapter(over_limits);

                        mOverLimitSwitch.setChecked(mStorage.getBoolean(StorageCommon.ACC_OVER_LIMIT_SWITCH));
                        mOverLimitSpinner.setEnabled(mStorage.getBoolean(StorageCommon.ACC_OVER_LIMIT_SWITCH));
                        mOverLimitSpinner.setSelection(mStorage.getInt(StorageCommon.ACC_OVER_LIMIT_VALUE));

                        mBelowLimitSwitch.setChecked(false);
                        mBelowLimitSwitch.setEnabled(false);
                        mBelowLimitSpinner.setAdapter(over_limits);
                        mBelowLimitSpinner.setVisibility(View.INVISIBLE);
                        break;
                    }
                }
                break;
            case R.id.spinner_below_limit:
            case R.id.spinner_over_limit:
                switch (mSensorLimitSelector.getAdapter().getItem(mSensorLimitSelector.getSelectedItemPosition()).toString()) {
                    case SELECT_HUM:
                        if (adapterView.getId() == R.id.spinner_over_limit)
                            mStorage.putInt(StorageCommon.HUM_OVER_LIMIT_VALUE, position);
                        else if (adapterView.getId() == R.id.spinner_below_limit)
                            mStorage.putInt(StorageCommon.HUM_BELOW_LIMIT_VALUE, position);
                        break;
                    case SELECT_TEMP:
                        if (adapterView.getId() == R.id.spinner_over_limit)
                            mStorage.putInt(StorageCommon.TEMP_OVER_LIMIT_VALUE, position);
                        else if (adapterView.getId() == R.id.spinner_below_limit)
                            mStorage.putInt(StorageCommon.TEMP_BELOW_LIMIT_VALUE, position);
                        break;
                    case SELECT_VR:
                        if (adapterView.getId() == R.id.spinner_over_limit)
                            mStorage.putInt(StorageCommon.VR_OVER_LIMIT_VALUE, position);
                        break;
                    case SELECT_ACC:
                        if (adapterView.getId() == R.id.spinner_over_limit)
                            mStorage.putInt(StorageCommon.ACC_OVER_LIMIT_VALUE, position);
                        break;
                }
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        switch (mSensorLimitSelector.getAdapter().getItem(mSensorLimitSelector.getSelectedItemPosition()).toString()) {
            case SELECT_HUM:
                if (compoundButton.getId() == R.id.toggle_over_limit)
                    mStorage.putBoolean(StorageCommon.HUM_OVER_LIMIT_SWITCH, b);
                else if (compoundButton.getId() == R.id.toggle_below_limit)
                    mStorage.putBoolean(StorageCommon.HUM_BELOW_LIMIT_SWITCH, b);
                break;
            case SELECT_TEMP:
                if (compoundButton.getId() == R.id.toggle_over_limit)
                    mStorage.putBoolean(StorageCommon.TEMP_OVER_LIMIT_SWITCH, b);
                else if (compoundButton.getId() == R.id.toggle_below_limit)
                    mStorage.putBoolean(StorageCommon.TEMP_BELOW_LIMIT_SWITCH, b);
                break;
            case SELECT_VR:
                if (compoundButton.getId() == R.id.toggle_over_limit)
                    mStorage.putBoolean(StorageCommon.VR_OVER_LIMIT_SWITCH, b);
                break;
            case SELECT_ACC:
                if (compoundButton.getId() == R.id.toggle_over_limit)
                    mStorage.putBoolean(StorageCommon.ACC_OVER_LIMIT_SWITCH, b);
                break;
        }

        switch (compoundButton.getId()) {
            case R.id.toggle_over_limit:
                mOverLimitSpinner.setEnabled(b);
                break;
            case R.id.toggle_below_limit:
                mBelowLimitSpinner.setEnabled(b);
                break;
        }
    }
}
