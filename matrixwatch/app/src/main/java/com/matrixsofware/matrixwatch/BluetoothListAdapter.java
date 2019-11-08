package com.matrixsofware.matrixwatch;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
public class BluetoothListAdapter extends ArrayAdapter<BluetoothDevice> {

    private LayoutInflater inflater;
    private int resView;
    private ArrayList<BluetoothDevice> devices = new ArrayList<>();

    public BluetoothListAdapter(Context context, int resource, ArrayList<BluetoothDevice> d) {

        super(context, resource, d);
        //типичный конструктор

        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        resView = resource;
        devices = d;

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        convertView = inflater.inflate(resView, null); //создаю элемент

        BluetoothDevice d = devices.get(position); //беру из листа устройство и рассказываю о нем {

        TextView name = convertView.findViewById(R.id.deviceName);

        TextView id = convertView.findViewById(R.id.bId);

        name.setText(d.getName());
        id.setText(d.getAddress());

        // }

        return convertView;
    }
}
