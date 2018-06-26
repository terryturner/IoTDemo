package com.example.lora.http;

import android.os.Bundle;

import com.goldtek.iot.demo.GoldtekApplication;
import com.example.lora.http.mqtt.Constants;
import com.example.lora.http.mqtt.MQTT;

import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.Random;

/**
 * Created by Terry on 2018/06/19.
 */
public class RaspberryGwConnector implements IGwConnector, MQTT.Callback {
    private Random r = new Random();
    private MQTT mService = MQTT.getInstance(GoldtekApplication.getContext());
    private Bundle mBundle = new Bundle();

    @Override
    public boolean connect(String url) {

        if (!mService.isServiceConnected()) {
            mService.init(GoldtekApplication.getContext(), String.format(Constants.MQTT_VM_BROKER_URL_TCP_FORMAT, url));
            mService.connect();
        }
        mService.setCallback(this);

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return mService.isBrokerConnected();
    }

    @Override
    public void disconnect() {
        mService.removeCallback(this);
        mService.disConnect();
    }

    @Override
    public void stop() {
    }

    @Override
    public Bundle getValue(Sensor type) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(IGetSensors.KEY_RESPONSE_STATE, true);
        switch (type) {
            case ALL:
                if (mService.isBrokerConnected()) {
                    mService.publish(Constants.PUBLISH_TOPIC, Long.toString(System.currentTimeMillis()) + " : " + this.toString() , 1);
                }
                bundle = mBundle;
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
    public void messageArrived(String topic, MqttMessage message) {
        mBundle.putBoolean(IGetSensors.KEY_RESPONSE_STATE, true);
        mBundle.putDouble(IGetSensors.KEY_ACCELEROMETER_X, r.nextDouble() * 100);
        mBundle.putDouble(IGetSensors.KEY_ACCELEROMETER_Y, r.nextDouble() * 100);
        mBundle.putDouble(IGetSensors.KEY_ACCELEROMETER_Z, r.nextDouble() * 100);
        mBundle.putInt(IGetSensors.KEY_HUMIDITY, r.nextInt(100));
        mBundle.putInt(IGetSensors.KEY_PROXIMITY, r.nextInt(59) + 1);
        mBundle.putInt(IGetSensors.KEY_TEMPERATURE_C, r.nextInt(150) - 50);
    }
}
