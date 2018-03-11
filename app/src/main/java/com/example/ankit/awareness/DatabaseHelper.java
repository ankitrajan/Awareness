package com.example.ankit.awareness;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

import java.util.Vector;

public class DatabaseHelper extends SQLiteOpenHelper {

    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 3;

    // Database Name
    private static final String DATABASE_NAME = "Data Manager";

    // Contacts table name
    private static final String TABLE_DATA = "data";
    private static final String TABLE_DEVICE = "device";

    private static final int DATABASE_SIZE = 32;

    //private static int currentSize = 0;

    //private static int position = 0;

    // Contacts Table Columns names
    //private static final String KEY_ID = "ID";
    private static final String KEY_APPLIANCE = "name";
    private static final String KEY_INPUT = "input";
    private static final String KEY_YEAR = "year";
    private static final String KEY_MONTH = "month";
    private static final String KEY_DAY = "day";
    private static final String KEY_HOUR = "hour";
    private static final String KEY_MINUTE = "minute";
    private static final String KEY_SECOND = "second";
    private static final String KEY_DATA = "value";

    private static final String KEY_DEVICE = "deviceID";
    private static final String KEY_STATUS = "status";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_DATA_TABLE = "CREATE TABLE " + TABLE_DATA + "("
                + KEY_INPUT + " LONG PRIMARY KEY," + KEY_APPLIANCE + " TEXT,"
                + KEY_YEAR + " INTEGER," + KEY_MONTH + " INTEGER," + KEY_DAY + " INTEGER,"
                + KEY_HOUR + " INTEGER," + KEY_MINUTE + " INTEGER," + KEY_SECOND + " INTEGER," + KEY_DATA + " DOUBLE" +")";
        String CREATE_DEVICE_TABLE = "CREATE TABLE " + TABLE_DEVICE + "("
                + KEY_DEVICE + " TEXT PRIMARY KEY," + KEY_STATUS + " TEXT" +")";
        db.execSQL(CREATE_DATA_TABLE);
        db.execSQL(CREATE_DEVICE_TABLE);
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DATA);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DEVICE);

        // Create tables again
        onCreate(db);
    }

    public void deleteAllData()
    {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("delete from "+ TABLE_DATA);
    }

    public void deleteAllDevice()
    {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("delete from "+ TABLE_DEVICE);
    }

    // Adding new contact
    public void addData(String name, long stamp, double data, Context c)
    {
        String countQuery = "SELECT  * FROM " + TABLE_DATA;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);

        if (cursor.getCount() == DATABASE_SIZE)
        {
            Cursor newCursor = db.query(TABLE_DATA, null, null, null, null, null, null);

            if(newCursor.moveToFirst())
            {
                String deleteId = newCursor.getString(newCursor.getColumnIndex(KEY_INPUT));

                db.delete(TABLE_DATA, KEY_INPUT + "=?",  new String[]{deleteId});
            }
        }

        ContentValues values = new ContentValues();
        values.put(KEY_APPLIANCE, name);
        values.put(KEY_INPUT, stamp);
        values.put(KEY_YEAR, (long)((stamp%(1000000000000L))/10000000000L));
        values.put(KEY_MONTH, (long)((stamp%(10000000000L))/100000000L));
        values.put(KEY_DAY, (long)((stamp%(100000000L))/1000000L));
        values.put(KEY_HOUR, (long)((stamp%(1000000L))/10000L));
        values.put(KEY_MINUTE, (long)((stamp%(10000L))/100L));
        values.put(KEY_SECOND, (long)(stamp%(100L)));
        values.put(KEY_DATA, data);

        db.insert(TABLE_DATA, null, values);
        db.close();
    }

    public void addDevice(String deviceName, String status)
    {
        Log.d("MyAccountActivity", "Adding device in database");
        String countQuery = "SELECT deviceID FROM " + TABLE_DEVICE;
        SQLiteDatabase db = this.getWritableDatabase();

        /*Cursor cursor = db.rawQuery(countQuery, null);


        if (cursor.getCount() == DATABASE_SIZE)
        {
            Cursor newCursor = db.query(TABLE_DEVICE, null, null, null, null, null, null);

            if(newCursor.moveToFirst())
            {
                String deleteId = newCursor.getString(newCursor.getColumnIndex(KEY_DEVICE));

                db.delete(TABLE_DEVICE, KEY_DEVICE + "=?",  new String[]{deleteId});
            }
        }
        */

        ContentValues values = new ContentValues();
        values.put(KEY_DEVICE, deviceName);
        values.put(KEY_STATUS, status);

        db.insert(TABLE_DEVICE, null, values);
        db.close();

        Log.d("MyAccountActivity", "Device added");
    }

    public Vector<Long> getAllStamp()
    {
        Vector<Long>  allStamp = new Vector<Long>();
        String selectQuery = "SELECT input FROM " + TABLE_DATA + " ";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst())
        {
            int myPosition = 0;

            do
            {
                allStamp.add(myPosition++, cursor.getLong(0));
            } while (cursor.moveToNext());
        }

        db.close();

        return allStamp;
    }

    public Vector<Long> getSpecificStamp(String queryDevice)
    {
        Vector<Long>  specifiedStamp = new Vector<Long>();
        String selectQuery = "SELECT input FROM " + TABLE_DATA + " WHERE name = \"" + queryDevice + "\" ";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst())
        {
            int myPosition = 0;

            do
            {
                specifiedStamp.add(myPosition++, cursor.getLong(0));
            } while (cursor.moveToNext());
        }

        db.close();

        return specifiedStamp;
    }

    public Vector<Double> getSpecificData(String queryDevice)
    {
        Vector<Double>  specifiedData = new Vector<Double>();
        String selectQuery = "SELECT value FROM " + TABLE_DATA + " WHERE name = \"" + queryDevice + "\" ";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst())
        {
            int myPosition = 0;

            do
            {
                specifiedData.add(myPosition++, cursor.getDouble(0));
            } while (cursor.moveToNext());
        }

        db.close();

        return specifiedData;
    }

    public double getSpecificDataTotal(String queryDevice)
    {
        Vector<Double>  specifiedData = new Vector<Double>();
        String selectQuery = "SELECT value FROM " + TABLE_DATA + " WHERE name = \"" + queryDevice + "\" ";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst())
        {
            int myPosition = 0;

            do
            {
                specifiedData.add(myPosition++, cursor.getDouble(0));
            } while (cursor.moveToNext());
        }

        db.close();

        double total = 0;

        for(int i = 0; i < specifiedData.size(); i++)
            total += specifiedData.elementAt(i);

        return total;
    }

    public Vector<String> getAllDevice()
    {
        Vector<String>  allDevice = new Vector<String>();
        String selectQuery = "SELECT deviceID FROM " + TABLE_DEVICE + " ";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst())
        {
            int myPosition = 0;

            do
            {
                allDevice.add(myPosition++, cursor.getString(0));
            } while (cursor.moveToNext());
        }

        db.close();

        Log.d("MyAccountActivity", "Returned vector size = " + allDevice.size());

        return allDevice;
    }

    public Vector<String> getAllAppliance()
    {
        Vector<String>  allAppliance = new Vector<String>();
        String selectQuery = "SELECT name FROM " + TABLE_DATA + " ";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst())
        {
            int myPosition = 0;

            do
            {
                allAppliance.add(myPosition++, cursor.getString(0));
            } while (cursor.moveToNext());
        }


        db.close();

        return allAppliance;
    }

    public Vector<Double> getAllData()
    {
        Vector<Double>  allData = new Vector<Double>();
        String selectQuery = "SELECT value FROM " + TABLE_DATA + " ";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst())
        {
            int myPosition = 0;

            do
            {
                allData.add(myPosition++, cursor.getDouble(0));
            } while (cursor.moveToNext());
        }

        db.close();

        return allData;
    }

    public void changeDeviceStatus(String deviceID, String newValue)
    {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_STATUS, newValue);

        db.update(TABLE_DEVICE, values, KEY_DEVICE + " = ?", new String[] { deviceID });
    }

    public void deleteDevice(String device)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        try
        {
            db.delete(TABLE_DEVICE, "deviceID = ?", new String[] { device });
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            db.close();
        }
    }
}