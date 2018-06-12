package com.example.ameba.http;

/**
 * Created by Terry on 2018/05/30.
 */
public enum Sensor {
    Humidity("Humidity", 0),
    Temperature("Temperature", 1),
    AmbientLight("AmbientLight", 2),
    Proximity("Proximity", 3),
    Accelerometer("Accelerometer", 4),
    Gyro("Gyro", 5),
    Battery("Battery", 6),
    Gas("Gas", 7),
    Electric("Electric", 8),
    Magnetic("Magnetic", 9),
    Vibration("Vibration", 10),
    ALL("All", 11);

    private final String mName;
    private final int mIndex;

    Sensor(String name, int index) {
        mName = name;
        mIndex = index;
    }

    public String getName() { return mName; }
    public int getOrdinate() { return mIndex; }
}
