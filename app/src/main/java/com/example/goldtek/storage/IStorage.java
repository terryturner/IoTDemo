package com.example.goldtek.storage;

/**
 * Created by Terry on 2018/06/12.
 */
public interface IStorage {

    void init(String filename, int mode);

    void putString(String key, String value);
    String getString(String key);

    void putInt(String key, int value);
    int getInt(String key);

    void putBoolean(String key, boolean value);
    boolean getBoolean(String key);
}
