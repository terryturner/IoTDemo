package com.example.lora.http;

import android.os.Bundle;

/**
 * Created by Terry on 2018/05/31.
 */
public interface IGwConnector {
    String PATTERN_ATGG_GET_DATA = "VR=\\d{1,4},HUM=\\d{1,2},TMP=\\d{1,3},Gx=\\d{1,4},Gy=\\d{1,4},Gz=\\d{1,4}";

    boolean connect(String url);
    void disconnect();
    void stop();
    Bundle getValue(Sensor type);
}
