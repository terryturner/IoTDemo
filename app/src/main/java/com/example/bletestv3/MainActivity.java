
/*
 * Copyright (c) 2015, Nordic Semiconductor
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
 * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.example.bletestv3;




import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.util.Date;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.goldtek.iot.demo.R;

public class MainActivity extends Activity implements RadioGroup.OnCheckedChangeListener {
    private static final int REQUEST_SELECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    private static final int UART_PROFILE_READY = 10;
    public static final String TAG = "MainActivity";
    private static final int UART_PROFILE_CONNECTED = 20;
    private static final int UART_PROFILE_DISCONNECTED = 21;
    private static final int STATE_OFF = 10;

    final Context context = this;
    TextView mRemoteRssiVal;
    RadioGroup mRg;
    private int mState = UART_PROFILE_DISCONNECTED;
    private UartService mService = null;
    private BluetoothDevice mDevice = null;
    private BluetoothAdapter mBtAdapter = null;
    private ListView messageListView;
    private ArrayAdapter<String> listAdapter;
    private Button btnConnectDisconnect, btnSend;
    private EditText edtMessage;

    private TextView txtStateInfo, txtDevInfo;
    private ImageView imgAbout;

    ProgressDialog m_LoadingDialog;

    // Function Layout Part
    private LinearLayout lytFunction;

    private ImageView imgfanLow, imgfanMed, imgfanHigh, imgfanOff;
    private ImageView imgLamp1_on, imgLamp1_off;
    private ImageView imgLamp2_on, imgLamp2_off;
    private ImageView imgLamp3_on, imgLamp3_off;

    private ImageView splashImageView;
    boolean splashloading = false;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bletest_main);
        this.setTitle(R.string.sub1g_title_name);

        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBtAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        messageListView = (ListView) findViewById(R.id.listMessage);
        listAdapter = new ArrayAdapter<String>(this, R.layout.message_detail);
        messageListView.setAdapter(listAdapter);
        messageListView.setDivider(null);

        btnConnectDisconnect=(Button) findViewById(R.id.btn_select);
        btnSend=(Button) findViewById(R.id.sendButton);
        edtMessage = (EditText) findViewById(R.id.sendText);

        txtStateInfo = (TextView) findViewById(R.id.txtStatus);
        txtDevInfo=  (TextView) findViewById(R.id.deviceName);
        imgAbout = (ImageView) findViewById(R.id.imgAbout);

        imgfanLow = (ImageView) findViewById(R.id.fanButtonLow);
        imgfanMed = (ImageView) findViewById(R.id.fanButtonMed);
        imgfanHigh = (ImageView) findViewById(R.id.fanButtonHigh);
        imgfanOff = (ImageView) findViewById(R.id.fanButtonOnoff);

        imgLamp1_on = (ImageView) findViewById(R.id.lamp1_on);
        imgLamp1_off = (ImageView) findViewById(R.id.lamp1_off);
        imgLamp2_on = (ImageView) findViewById(R.id.lamp2_on);
        imgLamp2_off = (ImageView) findViewById(R.id.lamp2_off);
        imgLamp3_on = (ImageView) findViewById(R.id.lamp3_on);
        imgLamp3_off = (ImageView) findViewById(R.id.lamp3_off);


        lytFunction = (LinearLayout)findViewById(R.id.function_layout);
        SettingUIVisibility(mState);

        service_init();

        // setOnClickListener
        btnConnectDisconnect.setOnClickListener(new OnButtonEvent());
        btnSend.setOnClickListener(new OnButtonEvent());
        imgAbout.setOnClickListener(new OnButtonEvent());

        // FAN
        imgfanLow.setOnClickListener(new OnImageViewEvent());
        imgfanMed.setOnClickListener(new OnImageViewEvent());
        imgfanHigh.setOnClickListener(new OnImageViewEvent());
        imgfanOff.setOnClickListener(new OnImageViewEvent());
        // LAMP
        imgLamp1_on.setOnClickListener(new OnImageViewEvent());
        imgLamp1_off.setOnClickListener(new OnImageViewEvent());
        imgLamp2_on.setOnClickListener(new OnImageViewEvent());
        imgLamp2_off.setOnClickListener(new OnImageViewEvent());
        imgLamp3_on.setOnClickListener(new OnImageViewEvent());
        imgLamp3_off.setOnClickListener(new OnImageViewEvent());

        m_LoadingDialog = new ProgressDialog(this);
        m_LoadingDialog.setMessage(getString(R.string.pleasewait));
        m_LoadingDialog.setTitle(getString(R.string.connecting));
        m_LoadingDialog.setIndeterminate(false);
        m_LoadingDialog.setCancelable(false);
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "onStop");
        super.onStop();
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause");
        super.onPause();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(TAG, "onRestart");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        if (!mBtAdapter.isEnabled()) {
            Log.i(TAG, "onResume - BT not enabled yet");
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }

    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy()");

        try {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(UARTStatusChangeReceiver);
        } catch (Exception ignore) {
            Log.e(TAG, ignore.toString());
        }
        unbindService(mServiceConnection);
        mService.stopSelf();
        mService= null;

    }

    //UART service connected/disconnected
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder rawBinder) {
            mService = ((UartService.LocalBinder) rawBinder).getService();
            Log.d(TAG, "onServiceConnected mService= " + mService);
            if (!mService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
        }

        public void onServiceDisconnected(ComponentName classname) {
            ////     mService.disconnect(mDevice);
            mService = null;
        }
    };

    private void service_init() {
        Intent bindIntent = new Intent(this, UartService.class);
        bindService(bindIntent, mServiceConnection, Context.BIND_AUTO_CREATE);

        LocalBroadcastManager.getInstance(this).registerReceiver(UARTStatusChangeReceiver, makeGattUpdateIntentFilter());
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(UartService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(UartService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(UartService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(UartService.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(UartService.DEVICE_DOES_NOT_SUPPORT_UART);
        return intentFilter;
    }

    private Handler mHandler = new Handler() {
        @Override
        //Handler events that received from UART service 
        public void handleMessage(Message msg) {
  
        }
    };

    private final BroadcastReceiver UARTStatusChangeReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            // region BroadcastReceiver
            String action = intent.getAction();
            final Intent mIntent = intent;
            if (action.equals(UartService.ACTION_GATT_CONNECTED)) {
                mState = UART_PROFILE_CONNECTED;
                SettingUIVisibility(mState);
                runOnUiThread(new Runnable() {
                    public void run() {
                        String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                        Log.d(TAG, "UART_CONNECT_MSG");
                        txtDevInfo.setText(mDevice.getName() + " - " + mDevice.getAddress());
                        listAdapter.add("[" + currentDateTimeString + "] Connected to: " + mDevice.getName());
                        messageListView.smoothScrollToPosition(listAdapter.getCount() - 1);
                        m_LoadingDialog.dismiss();
                    }
                });
            }
            if (action.equals(UartService.ACTION_GATT_DISCONNECTED)) {
                mState = UART_PROFILE_DISCONNECTED;
                SettingUIVisibility(mState);
                runOnUiThread(new Runnable() {
                    public void run() {
                        String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                        Log.d(TAG, "UART_DISCONNECT_MSG");
                        txtDevInfo.setText("Not Connected");
                        listAdapter.add("[" + currentDateTimeString + "] Disconnected to: " + mDevice.getName());
                        mService.close();
                        m_LoadingDialog.dismiss();
                    }
                });
                showMessage("Device is Disconnecting");
            }

            if (action.equals(UartService.ACTION_GATT_SERVICES_DISCOVERED)) {
                mService.enableTXNotification();
            }
            if (action.equals(UartService.ACTION_DATA_AVAILABLE)) {

                final byte[] txValue = intent.getByteArrayExtra(UartService.EXTRA_DATA);
                runOnUiThread(new Runnable() {
                    public void run() {
                        try {
                            String text = new String(txValue, "UTF-8");
                            String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                            listAdapter.add("[" + currentDateTimeString + "] RX: " + text);
                            messageListView.smoothScrollToPosition(listAdapter.getCount() - 1);

                        } catch (Exception e) {
                            Log.e(TAG, e.toString());
                        }
                    }
                });
            }
            if (action.equals(UartService.DEVICE_DOES_NOT_SUPPORT_UART)) {
                showMessage("Device doesn't support UART. Disconnecting");
                mService.disconnect();
            }
            // endregion
        }
    };

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {

        case REQUEST_SELECT_DEVICE:
        	//When the DeviceListActivity return, with the selected device address
            if (resultCode == Activity.RESULT_OK && data != null) {
                String deviceAddress = data.getStringExtra(BluetoothDevice.EXTRA_DEVICE);
                String deviceName = data.getStringExtra(BluetoothDevice.EXTRA_NAME);
                mDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(deviceAddress);

                Log.d(TAG, "... onActivityResultdevice.address==" + mDevice + "mserviceValue" + mService);
                txtDevInfo.setText(deviceName + "@" + deviceAddress);
                mService.connect(deviceAddress);
            }
            else if(resultCode == Activity.RESULT_CANCELED){
                m_LoadingDialog.dismiss();
            }
            break;
        case REQUEST_ENABLE_BT:
            // When the request to enable Bluetooth returns
            if (resultCode == Activity.RESULT_OK) {
                Toast.makeText(this, "Bluetooth has turned on ", Toast.LENGTH_SHORT).show();

            } else {
                // User did not enable Bluetooth or an error occurred
                Log.d(TAG, "BT not enabled");
                Toast.makeText(this, "Problem in BT Turning ON ", Toast.LENGTH_SHORT).show();
                finish();
            }
            break;
        default:
            Log.e(TAG, "wrong request code");
            break;
        }
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
       
    }

    @Override
    public void onBackPressed() {
        if (mState == UART_PROFILE_CONNECTED) {
            Intent startMain = new Intent(Intent.ACTION_MAIN);
            startMain.addCategory(Intent.CATEGORY_HOME);
            startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(startMain);
            showMessage("nRFUART's running in background.\n             Disconnect to exit");
        }
        else {
            new AlertDialog.Builder(this)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setTitle(R.string.popup_title)
            .setMessage(R.string.popup_message)
            .setPositiveButton(R.string.popup_yes, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
   	                finish();
                }
            })
            .setNegativeButton(R.string.popup_no, null)
            .show();
        }
    }

    private class OnButtonEvent implements View.OnClickListener {
        // region OnClickListener
        @Override
        public void onClick(View v) {
            if(v.getId() == btnSend.getId()){
                EditText editText = (EditText) findViewById(R.id.sendText);
                String message = editText.getText().toString();
                //send data to service
                try {
                    byte[] value = message.getBytes("UTF-8");
                    SendCommand(message, value);
                } catch (UnsupportedEncodingException e1) {
                    e1.printStackTrace();
                }
            }
            else if(v.getId() == btnConnectDisconnect.getId()){
                String test1 = getDeviceName();
                String test2 = getAndroidVersion();
                txtDevInfo.setText("");
                if (!mBtAdapter.isEnabled()) {
                    Log.i(TAG, "onClick - BT not enabled yet");
                    Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
                }
                else {
                    if (btnConnectDisconnect.getText().equals(getString(R.string.connect))){
                        m_LoadingDialog.show();
                        //Connect button pressed, open DeviceListActivity class, with popup windows that scan for devices
                        Intent newIntent = new Intent(MainActivity.this, DeviceListActivity.class);
                        startActivityForResult(newIntent, REQUEST_SELECT_DEVICE);
                    } else {
                        m_LoadingDialog.dismiss();
                        //Disconnect button pressed
                        if (mDevice!=null)
                            mService.disconnect();
                    }
                }
            }
            else if(v.getId() == imgAbout.getId()){
                // custom dialog
                final Dialog dialog = new Dialog(context, R.style.MyCustomDialog);
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
            }


        }
        // endregion
    }

    // BUTTON FUNCTION
    private class OnImageViewEvent implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            //SettingUIImage_FAN(v);
            if(v.getId() == imgfanLow.getId()){
                Log.i(TAG, "fanLOW");
                SendCommandHEX(Common._fancmd.LV1);
            }
            else if(v.getId() == imgfanMed.getId()){
                Log.i(TAG, "fanMED");
                SendCommandHEX(Common._fancmd.LV2);
            }
            else if(v.getId() == imgfanHigh.getId()){
                Log.i(TAG, "fanHIGH");
                SendCommandHEX(Common._fancmd.LV3);
            }
            else if(v.getId() == imgfanOff.getId()){
                Log.i(TAG, "fanOFF");
                SendCommandHEX(Common._fancmd.OFF);
            }
            //============================================================================
            else if(v.getId() == imgLamp1_on.getId()){
                Log.i(TAG, "lamp1_on");
                SendCommandHEX(Common._lampcmd.lamp1_on);
            }
            else if(v.getId() == imgLamp1_off.getId()){
                Log.i(TAG, "lamp1_off");
                SendCommandHEX(Common._lampcmd.lamp1_off);
            }
            //---------------------------------------------
            else if(v.getId() == imgLamp2_on.getId()){
                Log.i(TAG, "lamp2_on");
                SendCommandHEX(Common._lampcmd.lamp2_on);
            }
            else if(v.getId() == imgLamp2_off.getId()){
                Log.i(TAG, "lamp2_off");
                SendCommandHEX(Common._lampcmd.lamp2_off);
            }
            //---------------------------------------------
            else if(v.getId() == imgLamp3_on.getId()){
                Log.i(TAG, "lamp3_on");
                SendCommandHEX(Common._lampcmd.lamp3_on);
            }
            else if(v.getId() == imgLamp3_off.getId()){
                Log.i(TAG, "lamp3_off");
                SendCommandHEX(Common._lampcmd.lamp3_off);
            }
            //============================================================================
        }
    }

    //##############################################################################################
    //  FUNCTION
    //##############################################################################################

    public void SettingUIVisibility(int nState){
        int nVisible = View.GONE;
        boolean bEnable = false;
        String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());

        if(nState == UART_PROFILE_CONNECTED){
            nVisible = View.VISIBLE;
            bEnable = true;
            btnConnectDisconnect.setText(R.string.disconnect);
            //txtDevInfo.setText(mDevice.getName() + " - ready");
            txtStateInfo.setText(R.string.connected);
        }
        else if(nState == UART_PROFILE_DISCONNECTED){
            nVisible = View.GONE;
            bEnable = false;
            btnConnectDisconnect.setText(R.string.connect);
            //txtDevInfo.setText("Not Connected");
            txtStateInfo.setText(R.string.disconnected);
        }

        lytFunction.setVisibility(nVisible);
        edtMessage.setEnabled(bEnable);
        btnSend.setEnabled(bEnable);
    }

    public void SettingUIImage_FAN(View v){
        imgfanLow.setBackgroundResource(R.drawable.btnbase_left);
        imgfanMed.setBackgroundResource(R.drawable.btnbase_mid);
        imgfanHigh.setBackgroundResource(R.drawable.btnbase_right);

        if(v.getId() == imgfanLow.getId())
            v.setBackgroundResource(R.drawable.btnbase_left_s);
        else if(v.getId() == imgfanMed.getId())
            v.setBackgroundResource(R.drawable.btnbase_mid_s);
        if(v.getId() == imgfanHigh.getId())
            v.setBackgroundResource(R.drawable.btnbase_right_s);
    }


    public String getDeviceName() {

        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;

        if (model.startsWith(manufacturer)) {
            return capitalize(model);
        } else {
            return capitalize(manufacturer) + " " + model;
        }
    }

    private String getAndroidVersion() {
        String aaa = String.valueOf(Build.VERSION.SDK_INT);
        return android.os.Build.VERSION.RELEASE;
    }

    private String capitalize(String s) {
        if (s == null || s.length() == 0) {
            return "";
        }
        char first = s.charAt(0);
        if (Character.isUpperCase(first)) {
            return s;
        } else {
            return Character.toUpperCase(first) + s.substring(1);
        }
    }

    private void showMessage(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    private void SendCommandHEX(String message){
        if(!Common.DEBUG) {
            byte[] value = Common.hexStringToByteArray(message);
            mService.writeRXCharacteristic(value);
        }
        else
            Log.i(TAG, "[TX]: " + message);
    }

    private void SendCommand(String message, byte[] value){
        if(!Common.DEBUG) {
            mService.writeRXCharacteristic(value);
            //Update the log with time stamp
            String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
            listAdapter.add("[" + currentDateTimeString + "] TX: " + message);
            messageListView.smoothScrollToPosition(listAdapter.getCount() - 1);
            edtMessage.setText("");
        }
        else
            Log.i(TAG, "[TX]: " + message);
    }

}
