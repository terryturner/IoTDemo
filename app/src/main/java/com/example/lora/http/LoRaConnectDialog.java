package com.example.lora.http;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;

import com.example.goldtek.iot.demo.R;
import com.example.goldtek.iot.demo.ServerValidator;
import com.example.goldtek.storage.IStorage;
import com.example.goldtek.storage.PrivatePreference;
import com.example.goldtek.storage.StorageCommon;

/**
 * Created by Terry on 2018/06/21.
 */
public class LoRaConnectDialog extends Dialog implements View.OnClickListener{
    private final OnClickListener mClick;
    private final IStorage mStorage;
    private final Button mButtonOK;
    private final EditText mServerAddress;
    private final ImageView mImgSettings;
    private final ViewGroup mSettingsPanel;
    private final Spinner mSensorLimitSelector;
    private final Spinner mGatewaySelector;

    public interface OnClickListener {
        void onClick(boolean correctIP, String ip, int gw);
    }

    public LoRaConnectDialog(Context context, int themeResId, OnClickListener listener) {
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

        if (mStorage.getString(StorageCommon.LORA_SERVER_IP) != null) {
            mServerAddress.setText(mStorage.getString(StorageCommon.LORA_SERVER_IP));
        }
        mImgSettings.setOnClickListener(this);
        mButtonOK.setOnClickListener(this);

        ArrayAdapter sensor_list = ArrayAdapter.createFromResource(
                context, R.array.lora_sensors, R.layout.spinner_item);
        sensor_list.setDropDownViewResource(R.layout.dropdown_spinner_item);
        mSensorLimitSelector.setAdapter(sensor_list);

        ArrayAdapter gw_options = ArrayAdapter.createFromResource(
                context, R.array.lora_gw_connectors, R.layout.spinner_item);
        gw_options.setDropDownViewResource(R.layout.dropdown_spinner_item);
        mGatewaySelector.setAdapter(gw_options);
        mGatewaySelector.setSelection(mStorage.getInt(StorageCommon.LORA_GW_SELECTOR));
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
}
