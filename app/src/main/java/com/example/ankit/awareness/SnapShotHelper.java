package com.example.ankit.awareness;

import android.content.Context;
import android.widget.Toast;

import java.util.Vector;

/**
 * Created by Ankit on 2018-03-04.
 */

class SnapShotHelper {
    private Vector<Double> dataVector;

    Context c;

    public SnapShotHelper(Context context)
    {
        this.c = context;

        dataVector = new Vector<Double>();
    }

    public int addData(Double newData)
    {
        dataVector.add(newData);

        //Toast.makeText(c, "Data point added: " + newData, Toast.LENGTH_LONG).show();

        return dataVector.size();
    }

    /*
    public void dumpData(String device)
    {
        DatabaseHelper myDatabaseHelper = new DatabaseHelper(c);

        int myCounter = 0;


        for(int i = 0; i < stopVector.size(); i++)
        {
            for (int j = 0; j < ((stopVector.elementAt(i) - startVector.elementAt(i))+1); j++)
            {
                myDatabaseHelper.addData(device,(startVector.elementAt(i)+j) , dataVector.elementAt(myCounter++), c);
            }
        }

        clearVectors(myCounter);
    }

    private void clearVectors(int dataRemovePosition)
    {
        int startRemovePosition = stopVector.size();

        for (int i = startRemovePosition; i > 0; i--)
        {
            startVector.remove(i-1);
            stopVector.remove(i-1);
        }

        for (int i = dataRemovePosition; i > 0; i--)
        {
            dataVector.remove(i-1);
        }
    }
    */
}
