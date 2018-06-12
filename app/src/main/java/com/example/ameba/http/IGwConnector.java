package com.example.ameba.http;

import android.os.Bundle;

/**
 * Created by Terry on 2018/05/31.
 */
public interface IGwConnector {
    boolean connect(String url);
    void stop();
    Bundle getValue(Sensor type);
}
