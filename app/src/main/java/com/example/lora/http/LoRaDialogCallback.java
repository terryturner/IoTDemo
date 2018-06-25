package com.example.lora.http;

/**
 * Created by Terry on 2018/06/25.
 */
public interface LoRaDialogCallback {
    void onClick(boolean correctIP, String ip, int gw);
    void onConfig();
}
