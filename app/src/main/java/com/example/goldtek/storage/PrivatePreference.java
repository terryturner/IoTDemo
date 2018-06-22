package com.example.goldtek.storage;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Terry on 2018/06/12.
 */
public class PrivatePreference implements IStorage {
    private final Context mContext;
    private boolean mInitFlag = false;
    private SharedPreferences mPref;

    public PrivatePreference(Context context) {
        mContext = context;
    }

    @Override
    public void init(String filename, int mode) {
        if (!mInitFlag) {
            mPref = mContext.getSharedPreferences(filename, mode);
            mInitFlag = true;
        }
    }

    @Override
    public void putString(String key, String value) {
        if (mInitFlag) {
            mPref.edit().putString(key, value).commit();
        }
    }

    @Override
    public String getString(String key) {
        if (mInitFlag) {
            String result = null;
            try {
                result = mPref.getString(key, null);
            } catch (ClassCastException e) {
                e.printStackTrace();
            }
            return result;
        }
        return null;
    }

    @Override
    public void putInt(String key, int value) {
        if (mInitFlag) {
            mPref.edit().putInt(key, value).commit();
        }
    }

    @Override
    public int getInt(String key) {
        if (mInitFlag) {
            return mPref.getInt(key, 0);
        }
        return 0;
    }

    @Override
    public void putBoolean(String key, boolean value) {
        if (mInitFlag) {
            mPref.edit().putBoolean(key, value).commit();
        }
    }

    @Override
    public boolean getBoolean(String key) {
        if (mInitFlag) {
            return mPref.getBoolean(key, false);
        }
        return false;
    }
}
