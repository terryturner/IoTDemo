package com.example.lora.http;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.example.lora.http.mqtt.Constants;
import com.example.lora.http.mqtt.PahoMqttClient;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import java.util.Random;
import java.util.regex.Pattern;

/**
 * Created by Terry on 2018/06/19.
 */
public class RaspberryGwConnector2 implements IGwConnector, MqttCallbackExtended {
    private final static String TAG = "terry";
    private final Context mContext;
    private Random r = new Random();
    private PahoMqttClient pahoMqttClient;
    private MqttAndroidClient client;
    private String mRawOutput = "";
    private boolean mConnected = false;

    public RaspberryGwConnector2(Context context) {
        mContext = context;
        pahoMqttClient = new PahoMqttClient();
    }

    @Override
    public boolean connect(String url) {
        url = String.format(Constants.MQTT_VM_BROKER_URL_TCP_FORMAT, url);
        Log.i(TAG, "connect " + url);

        String clientID = Constants.CLIENT_ID;
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.READ_PHONE_STATE) != PackageManager
                .PERMISSION_GRANTED) {

            return false;
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                clientID = Build.getSerial();
            } else {
                clientID = Build.SERIAL;
            }
        }
        client = pahoMqttClient.getMqttClient(mContext, url, clientID);
        client.setCallback(this);

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return mConnected;
    }

    @Override
    public void disconnect() {
        Log.i(TAG, "call disconnect");
        if (client == null) return;

        unSubscribe(Constants.SUBSCRIBE_TOPIC);

        try {
            pahoMqttClient.disconnect(client);
        } catch (MqttException e) {
            e.printStackTrace();
        }
        client = null;
        mConnected = false;
    }

    @Override
    public void stop() {
        // TODO: disable palo MqttService?!
        disconnect();
    }

    @Override
    public Bundle getValue(Sensor type) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(IGetSensors.KEY_RESPONSE_STATE, true);
        switch (type) {
            case ALL:
                String msg = Calendar.getInstance().getTime().toString();
                //publish(msg);

                synchronized (mRawOutput) {
                    try {
                        if (mRawOutput != null && Pattern.compile(PATTERN_ATGG_GET_DATA).matcher(mRawOutput).matches()) {
                            String[] values = mRawOutput.split(",");
                            String vr = values[0].substring(values[0].indexOf("=")+1);
                            String hum = values[1].substring(values[1].indexOf("=")+1);
                            String tmp = values[2].substring(values[2].indexOf("=")+1);
                            String x = values[3].substring(values[3].indexOf("=")+1);
                            String y = values[4].substring(values[4].indexOf("=")+1);
                            String z = values[5].substring(values[5].indexOf("=")+1);

                            bundle.putBoolean(IGetSensors.KEY_RESPONSE_STATE, true);
                            bundle.putInt(IGetSensors.KEY_PROXIMITY, Integer.valueOf(vr));
                            bundle.putInt(IGetSensors.KEY_HUMIDITY, Integer.valueOf(hum));
                            bundle.putInt(IGetSensors.KEY_TEMPERATURE_C, Integer.valueOf(tmp));
                            bundle.putDouble(IGetSensors.KEY_ACCELEROMETER_X, Integer.valueOf(x));
                            bundle.putDouble(IGetSensors.KEY_ACCELEROMETER_Y, Integer.valueOf(y));
                            bundle.putDouble(IGetSensors.KEY_ACCELEROMETER_Z, Integer.valueOf(z));
                        } else {
                            bundle.putBoolean(IGetSensors.KEY_RESPONSE_STATE, false);
                        }
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case Accelerometer:
                bundle.putDouble(IGetSensors.KEY_ACCELEROMETER_X, r.nextDouble() * 100);
                bundle.putDouble(IGetSensors.KEY_ACCELEROMETER_Y, r.nextDouble() * 100);
                bundle.putDouble(IGetSensors.KEY_ACCELEROMETER_Z, r.nextDouble() * 100);
                break;
            case AmbientLight:
                bundle.putInt(IGetSensors.KEY_AMBIENT_LIGHT, r.nextInt(1000));
                break;
            case Battery:
                bundle.putInt(IGetSensors.KEY_BATTERY, r.nextInt(100));
                break;
            case Electric:
                bundle.putFloat(IGetSensors.KEY_ELECTRIC_A, r.nextFloat() * 10);
                bundle.putFloat(IGetSensors.KEY_ELECTRIC_V, r.nextFloat() + 12);
                break;
            case Gas:
                bundle.putInt(IGetSensors.KEY_GAS_PPM, r.nextInt(1000));
                break;
            case Gyro:
                bundle.putInt(IGetSensors.KEY_GYRO_ANGLE, r.nextInt(32767*2) - 32767);
                break;
            case Humidity:
                bundle.putInt(IGetSensors.KEY_HUMIDITY, r.nextInt(100));
                break;
            case Magnetic:
                bundle.putFloat(IGetSensors.KEY_MAGNETIC, r.nextInt(2700) + 300);
                break;
            case Proximity:
                bundle.putInt(IGetSensors.KEY_PROXIMITY, r.nextInt(59) + 1);
                break;
            case Temperature:
                bundle.putInt(IGetSensors.KEY_TEMPERATURE_C, r.nextInt(150) - 50);
                break;
            case Vibration:
                bundle.putFloat(IGetSensors.KEY_VIBRATION, r.nextFloat() * 3);
                break;
        }
        return bundle;
    }

    @Override
    public void connectComplete(boolean reconnect, String serverURI) {
        Log.i(TAG, "connect done: " + reconnect + " " + serverURI);
        mConnected = true;
        if (!reconnect)
            subscribe(Constants.SUBSCRIBE_TOPIC);
    }

    @Override
    public void connectionLost(Throwable throwable) {
        Log.i(TAG, "connect lost: " + throwable);
    }

    @Override
    public void messageArrived(String topic, MqttMessage mqttMessage) {
        Log.i(TAG, "receive: " + topic + " " + mqttMessage.getPayload().length + " " + mqttMessage.toString());

        synchronized (mRawOutput) {
            //mRawOutput = String.format("VR=%d,HUM=%d,TMP=%d,Gx=%d,Gy=%d,Gz=%d", r.nextInt(4000), r.nextInt(100), r.nextInt(50), r.nextInt(1000), r.nextInt(1000), r.nextInt(1000));
            int nullIdx = -1;
            for (int i=0; i<mqttMessage.getPayload().length; i++) {
                if (mqttMessage.getPayload()[i] == 0) {
                    nullIdx = i;
//                    mRawOutput = new String(mqttMessage.getPayload(), 0, i, StandardCharsets.ISO_8859_1).trim();
                    break;
                }
            }

            if (nullIdx > 0)
                mRawOutput = new String(mqttMessage.getPayload(), 0, nullIdx, StandardCharsets.ISO_8859_1).trim();
            else
                mRawOutput = mqttMessage.toString().trim();

            int newLineIndex = mRawOutput.indexOf("\n");
            if (newLineIndex > 0) mRawOutput = mRawOutput.substring(0, newLineIndex).trim();
            Log.i(TAG, "fine: " + mRawOutput.length() + " => " + mRawOutput );

        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
    }

    private void publish(String msg) {
        if (client == null || !client.isConnected()) return;
        try {
            //Log.i(TAG, "publish " + msg);
            pahoMqttClient.publishMessage(client, msg, 1, Constants.PUBLISH_TOPIC);
        } catch (MqttException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    private void subscribe(String topic) {
        if (client == null || !client.isConnected()) return;
        try {
            pahoMqttClient.subscribe(client, topic, 1);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    private void unSubscribe(String topic) {
        if (client == null || !client.isConnected()) return;
        try {
            pahoMqttClient.unSubscribe(client, topic);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

}
