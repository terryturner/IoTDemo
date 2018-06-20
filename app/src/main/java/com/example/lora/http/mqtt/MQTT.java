package com.example.lora.http.mqtt;

import android.content.Context;
import android.util.Log;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Terry on 2018/06/19.
 */
public class MQTT {
    private MqttAndroidClient mqttAndroidClient;
    private MqttConnectOptions options;
    private static final String BROKER_IP = Constants.MQTT_VM_BROKER_URL;
    private String clientId;
    private static MQTT instance;
    private static final String USERNAME = "admin";
    private static final String PASSWORD = "password";
    private static final String TAG = "GtMqttClient";
    private boolean serviceConnected = false;
    private Boolean brokerConnected = false;
    private List<Callback> mCallbacks = new ArrayList<>();

    private MQTT(Context context) {
        //init(context, BROKER_IP);
    }

    public static MQTT getInstance(Context context) {
        if (null == instance) {
            synchronized (MQTT.class) {
                instance = new MQTT(context);
            }
        }
        return instance;
    }

    public interface Callback {
        void messageArrived(String topic, MqttMessage message);
    }

    public void init(Context context, String url) {
//        clientId = UUID.randomUUID().toString();
        clientId = Constants.CLIENT_ID;
        options = new MqttConnectOptions();
        options.setCleanSession(false);
//            // 设置连接的用户名
        options.setUserName(USERNAME);
        // 设置连接的密码
        options.setPassword(PASSWORD.toCharArray());
        // 设置超时时间 单位为秒
        options.setConnectionTimeout(10);
        // 设置会话心跳时间 单位为秒 服务器会每隔1.5*20秒的时间向客户端发送个消息判断客户端是否在线，但这个方法并没有重连的机制
        options.setKeepAliveInterval(20);
        options.setAutomaticReconnect(true);
        try {
            mqttAndroidClient = new MqttAndroidClient(context, url, clientId, new MemoryPersistence());
        } catch (Exception e) {
            e.printStackTrace();
            Log.i(TAG, "create mqtt client error");
        }
        mqttAndroidClient.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean b, String s) {
                Log.i(TAG, "complete:" + b + " " + s);
                synchronized (brokerConnected) {
                    brokerConnected = b;
                }
            }

            @Override
            public void connectionLost(Throwable cause) {
                //Log.i(TAG, "lost:" + cause);
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                for (Callback cb : mCallbacks) {
                    if (cb != null) cb.messageArrived(topic, message);
                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                //Log.i(TAG, "msg published");
            }
        });
    }

    /**
     * 连接服务器
     * MqttService有自己的重连机制，在断线情况下会重连，但是首次连接失败后，需要再调用connect方法
     */
    public void connect() {
        serviceConnected = true;
        try {
            mqttAndroidClient.connect(options, this, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.i(TAG, "connected");
                    try {
                        mqttAndroidClient.subscribe(Constants.SUBSCRIBE_TOPIC, 1);
                    } catch (MqttException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.i(TAG, "connect error:" + exception);
                    connect();//当发生连接失败的情况时继续连接。通常只发生在服务器未在线情况，一旦服务器上线，将立刻连接。
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    /**
     * 断开服务器链接
     */
    public void disConnect() {
        if (null == mqttAndroidClient) {
            return;
        }
        try {
            mqttAndroidClient.disconnect();
        } catch (MqttException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.i(TAG, "disconnected");
        serviceConnected = false;
    }

    public boolean isServiceConnected() {
        return serviceConnected;
    }

    public synchronized boolean isBrokerConnected() {
        if (isServiceConnected() && mqttAndroidClient != null) {
            return brokerConnected;
        }
        return false;
    }

    /**
     * 发布消息
     *
     * @param topic topic
     * @param msg   消息内容
     * @param qos   0：最多一次的传输；1：至少一次的传输；2： 只有一次的传输
     */
    public void publish(String topic, String msg, int qos) {
        MqttMessage mqttMessage = new MqttMessage();
        mqttMessage.setPayload(msg.getBytes());
        mqttMessage.setRetained(true);
        mqttMessage.setQos(qos);
        try {
            IMqttDeliveryToken token = mqttAndroidClient.publish(topic, mqttMessage);
            token.waitForCompletion();
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void setCallback(Callback cb) {
        if (!mCallbacks.contains(cb))
            mCallbacks.add(cb);
    }

    public void removeCallback(Callback cb) {
        mCallbacks.remove(cb);
    }
}
