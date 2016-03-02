package de.tudarmstadt.tk.dbsystel.wifimeasurement.localdb;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import de.tudarmstadt.tk.dbsystel.wifimeasurement.Measurement;

public class MeasurementDB {

    private DatabaseHelper dbHelper;
    public final static String TABLE="Measurements";

    public final static String ROW_ID="id";
    public final static String ROW_DATE="date";
    public final static String ROW_LATITUDE="latitude";
    public final static String ROW_LONGITUDE="longitude";
    public final static String ROW_BSSID="bssid";
    public final static String ROW_SSID="ssid";
    public final static String ROW_CAPABILITIES="capabilities";
    public final static String ROW_FREQUENCY="frequency";
    public final static String ROW_LEVEL="level";

    public static final String MEASUREMENTDB_CREATE = "CREATE TABLE " + TABLE + " (" + ROW_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
            ROW_DATE + " LONG, " +
            ROW_LATITUDE + " DOUBLE, " +
            ROW_LONGITUDE + " DOUBLE, " +
            ROW_BSSID + " VARCHAR, " +
            ROW_SSID + " VARCHAR, " +
            ROW_CAPABILITIES + " VARCHAR, " +
            ROW_FREQUENCY + " INTEGER, " +
            ROW_LEVEL + " INTEGER);";
    public static final String MEASUREMENTDB_DELETE = "DROP TABLE IF EXISTS " + TABLE;


    /**
     *
     * @param context
     */
    public MeasurementDB(Context context){
        dbHelper = new DatabaseHelper(context);
    }

    public synchronized long addMeasurement(Measurement measurement) {
        ContentValues values = new ContentValues();
        values.put(ROW_DATE, measurement.getDate());
        values.put(ROW_LATITUDE, measurement.getLatitude());
        values.put(ROW_LONGITUDE, measurement.getLongitude());
        values.put(ROW_BSSID, measurement.getBssid());
        values.put(ROW_SSID, measurement.getSsid());
        values.put(ROW_CAPABILITIES, measurement.getCapabilities());
        values.put(ROW_FREQUENCY, measurement.getFrequency());
        values.put(ROW_LEVEL, measurement.getLevel());

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        try {
            return db.insert(TABLE, null, values);
        } finally {
            try { db.close(); } catch (Exception ignore) {}
        }
    }

    public synchronized void deleteMeasurement(Measurement measurement) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        try {
            db.delete(TABLE, ROW_ID + "=?", new String[]{Integer.toString(measurement.getID())});
        } finally {
            try { db.close(); } catch (Exception ignore) {}
        }
    }

    public synchronized void deleteMeasurements(List<Measurement> measurements) {
        for(Measurement measurement : measurements) {
            deleteMeasurement(measurement);
        }
    }

    public synchronized void deleteMeasurementsByIDRange(int startID, int endID) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        try {
            db.delete(TABLE, ROW_ID + ">=? AND " + ROW_ID + " <=?", new String[]{Integer.toString(startID), Integer.toString(endID)});
        } finally {
            try { db.close(); } catch (Exception ignore) {}
        }
    }

    public synchronized List<Measurement> getAllRemainingRemainingMeasurements() {
        ArrayList<Measurement> outList = new ArrayList<>();

        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE + " ORDER BY " + ROW_DATE + " ASC";
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try {
            Cursor cursor = db.rawQuery(selectQuery, null);
            try {
                // looping through all rows and adding to list
                if (cursor.moveToFirst()) {
                    do {
                        Measurement measurement = new Measurement();
                        measurement.setID(cursor.getInt(0));
                        measurement.setDate(cursor.getLong(1));
                        measurement.setLatitude(cursor.getDouble(2));
                        measurement.setLongitude(cursor.getDouble(3));
                        measurement.setBssid(cursor.getString(4));
                        measurement.setSsid(cursor.getString(5));
                        measurement.setCapabilities(cursor.getString(6));
                        measurement.setFrequency(cursor.getInt(7));
                        measurement.setLevel(cursor.getInt(8));
                        outList.add(measurement);
                    } while (cursor.moveToNext());
                }
            } finally {
                try { cursor.close(); } catch (Exception ignore) {}
            }
        } finally {
            try { db.close(); } catch (Exception ignore) {}
        }
        return outList;
    }

    public synchronized Cursor selectRecords() {
        String[] cols = new String[] {ROW_ID, ROW_DATE, ROW_LATITUDE, ROW_LONGITUDE, ROW_BSSID, ROW_SSID, ROW_CAPABILITIES, ROW_FREQUENCY, ROW_LEVEL};
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor mCursor = db.query(true, TABLE,cols,null
                , null, null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor; // iterate to get each value.
    }
}