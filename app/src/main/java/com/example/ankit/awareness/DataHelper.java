package com.example.ankit.awareness;

import android.content.Context;
import android.util.Log;

import java.util.Vector;

/**
 * Created by Ankit on 2018-03-10.
 */

class DataHelper {

    private Vector<String> deviceName;
    private Vector<Vector<Double>> dataVector;

    Context c;

    public DataHelper(Context context)
    {
        this.c = context;
        deviceName = new Vector<String>();
        dataVector = new Vector<Vector<Double>>();
    }

    public int addDevice(String newDevice)
    {
        deviceName.add(newDevice);
        dataVector.add(new Vector<Double>());
        return (deviceName.size() - 1);
    }

    public void addData(int position, double data)
    {
        dataVector.elementAt(position).add(data);
    }

    public String getDevice(int position)
    {
        return deviceName.elementAt(position);
    }

    public void emptyData()
    {
        deviceName.clear();

        for(int i = 0; i < dataVector.size(); i++)
        {
            dataVector.elementAt(i).clear();
        }

        dataVector.clear();
    }

    public double totalData()
    {
        double total = 0;



        for (int i = 0; i < deviceName.size(); i++)
            for(int j = 0; j < dataVector.elementAt(i).size(); j++)
                total += dataVector.elementAt(i).elementAt(j);

        return total;
    }

    public int getDeviceSize()
    {
        return deviceName.size();
    }
}
