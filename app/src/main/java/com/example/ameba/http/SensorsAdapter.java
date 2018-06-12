package com.example.ameba.http;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.goldtek.iot.demo.R;

import java.util.HashMap;

/**
 * Created by Terry on 2018/05/29.
 */
public class SensorsAdapter extends RecyclerView.Adapter {
    private final Context mContext;
    private final String[] mSensors;
    private final String[] mValues;
    private final HashMap<Sensor, Integer> mMapPositions;
    private final int[] mIcons;

    public SensorsAdapter(Context context, String[] sensors, int[] icons) {
        mContext = context;
        mSensors = sensors;
        mIcons = icons;
        mValues = new String[mSensors.length];

        mMapPositions = new HashMap<>();
        for (int i=0; i<mSensors.length; i++) {
            for (Sensor eSensor : Sensor.values()) {
                if (eSensor.getName().equalsIgnoreCase(mSensors[i])) {
                    mMapPositions.put(eSensor, i);
                    break;
                }
            }
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(R.layout.sensor_recycler, null);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ViewHolder) {
            if (position < mSensors.length) ((ViewHolder)holder).sensorName.setText(mSensors[position]);
            if (position < mValues.length) ((ViewHolder)holder).sensorValue.setText(mValues[position]);
            if (position < mIcons.length) ((ViewHolder)holder).icon.setImageResource(mIcons[position]);
        }
    }

    @Override
    public int getItemCount() {
        return Math.min(mSensors.length, mIcons.length);
    }

    public void updateValue(int position, String value) {
        if (0 <= position && position < mValues.length) {
            mValues[position] = value;
            notifyDataSetChanged();
        }
    }

    public void updateValue(Sensor type, String value) {
        try {
            int position = mMapPositions.get(type);
            updateValue(position, value);
        } catch (NullPointerException e) {
            //e.printStackTrace();
        }
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        TextView sensorName;
        TextView sensorValue;
        ImageView icon;
        public ViewHolder(View itemView) {
            super(itemView);
            sensorName = itemView.findViewById(R.id.sensor_name);
            sensorValue = itemView.findViewById(R.id.sensor_value);
            icon = itemView.findViewById(R.id.icon);
        }
    }
}
