package com.example.ameba.http;

import android.os.Bundle;
import android.util.Log;

import com.example.goldtek.iot.demo.Tls12SocketFactory;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


/**
 * Created by Terry on 2018/05/31.
 */
public class AmebaGwConnector implements IGwConnector {
    private static final String ATGG_GET_DATA = "/command?c=ATGG=get_data%0D%0A";
    private static final String ATWQ_IFCONFIG = "/command?c=ATW?";
    private static final String ATGB_BATTERY = "/command?c=ATGB";
    private static final String ATGP_PROXIMITY = "/command?c=ATGP";
    private static final String PATTERN_ATGG_GET_DATA = "VR=\\d{1,4},HUM=\\d{1,2},TMP=\\d{1,3},Gx=\\d{1,4},Gy=\\d{1,4},Gz=\\d{1,4}";
    private String mURL = "";

    @Override
    public boolean connect(String url) {
        mURL = url;
        OkHttpClient client = getNewHttpClient();
        try {
            Request request = new Request.Builder().url(mURL + ATWQ_IFCONFIG).build();
            Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                //Log.i("terry", response.body().string());
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void stop() {

    }

    @Override
    public Bundle getValue(Sensor type) {
        String rawOutput = null;
        Bundle bundle = new Bundle();
        switch (type) {
            case ALL:
                rawOutput = executeRequest(ATGG_GET_DATA);
                if (rawOutput != null) {
                    Log.i("terry", "raw: " + rawOutput.length() + " => " + rawOutput );
                    int newLineIndex = rawOutput.indexOf("\n");
                    if (newLineIndex > 0) rawOutput = rawOutput.substring(0, newLineIndex).trim();
                    Log.i("terry", "fine: " + rawOutput.length() + " => " + rawOutput );
                }
                try {
                    if (rawOutput != null && Pattern.compile(PATTERN_ATGG_GET_DATA).matcher(rawOutput).matches()) {
                        String[] values = rawOutput.split(",");
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
                break;
            case Accelerometer:
                //bundle.putDouble(IGetSensors.KEY_ACCELEROMETER_X, r.nextDouble() * 100);
                //bundle.putDouble(IGetSensors.KEY_ACCELEROMETER_Y, r.nextDouble() * 100);
                //bundle.putDouble(IGetSensors.KEY_ACCELEROMETER_Z, r.nextDouble() * 100);
                break;
            case AmbientLight:
                //bundle.putInt(IGetSensors.KEY_AMBIENT_LIGHT, r.nextInt(1000));
                break;
            case Battery:
                rawOutput = executeRequest(ATGB_BATTERY);
                try {
                    int value = Integer.valueOf(rawOutput);
                    bundle.putBoolean(IGetSensors.KEY_RESPONSE_STATE, true);
                    bundle.putInt(IGetSensors.KEY_BATTERY, value);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
                break;
            case Electric:
                //bundle.putFloat(IGetSensors.KEY_ELECTRIC_A, r.nextFloat() * 10);
                //bundle.putFloat(IGetSensors.KEY_ELECTRIC_V, r.nextFloat() + 12);
                break;
            case Gas:
                //bundle.putInt(IGetSensors.KEY_GAS_PPM, r.nextInt(1000));
                break;
            case Gyro:
                //bundle.putInt(IGetSensors.KEY_GYRO_ANGLE, r.nextInt(32767*2) - 32767);
                break;
            case Humidity:
                //bundle.putInt(IGetSensors.KEY_HUMIDITY, r.nextInt(100));
                break;
            case Magnetic:
                //bundle.putFloat(IGetSensors.KEY_MAGNETIC, r.nextInt(2700) + 300);
                break;
            case Proximity:
                rawOutput = executeRequest(ATGP_PROXIMITY);
                try {
                    int value = Integer.valueOf(rawOutput);
                    bundle.putBoolean(IGetSensors.KEY_RESPONSE_STATE, true);
                    bundle.putInt(IGetSensors.KEY_PROXIMITY, value);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
                break;
            case Temperature:
                //bundle.putInt(IGetSensors.KEY_TEMPERATURE_C, r.nextInt(150) - 50);
                break;
            case Vibration:
                //bundle.putFloat(IGetSensors.KEY_VIBRATION, r.nextFloat() * 3);
                break;
        }
        return bundle;
    }

    private OkHttpClient getNewHttpClient() {
        OkHttpClient.Builder client = new OkHttpClient.Builder()
                .followRedirects(true)
                .followSslRedirects(true)
                .retryOnConnectionFailure(true)
                .cache(null)
                .connectTimeout(5, TimeUnit.SECONDS)
                .writeTimeout(5, TimeUnit.SECONDS)
                .readTimeout(5, TimeUnit.SECONDS);

        return Tls12SocketFactory.enableTls12OnPreLollipop(client).build();
    }

    private String executeRequest(String cmd) {
        OkHttpClient client = getNewHttpClient();

        try {
            Request request = new Request.Builder().url(mURL + cmd).build();
            Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                //Log.i("terry", response.body().string());
                return response.body().string();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
        return null;
    }
}
