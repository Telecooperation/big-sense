package de.tudarmstadt.tk.dbsystel.acceleration.localdb;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import de.tudarmstadt.tk.dbsystel.acceleration.Measurement;

public class MeasurementDB {

    private DatabaseHelper dbHelper;
    public final static String TABLE="Measurements";

    public final static String ROW_ID="id";
    public final static String ROW_DATE="date";
    public final static String ROW_LATITUDE="latitude";
    public final static String ROW_LONGITUDE="longitude";
    public final static String ROW_ACC_X="accX";
    public final static String ROW_ACC_Y="accY";
    public final static String ROW_ACC_Z="accZ";


    public static final String MEASUREMENTDB_CREATE = "CREATE TABLE " + TABLE + " (" + ROW_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
            ROW_DATE + " LONG, " +
            ROW_LATITUDE + " DOUBLE, " +
            ROW_LONGITUDE + " DOUBLE, " +
            ROW_ACC_X + " DOUBLE, " +
            ROW_ACC_Y + " DOUBLE, " +
            ROW_ACC_Z + " DOUBLE);";
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
        values.put(ROW_ACC_X, measurement.getAccX());
        values.put(ROW_ACC_Y, measurement.getAccY());
        values.put(ROW_ACC_Z, measurement.getAccZ());

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
                        measurement.setAccX(cursor.getFloat(4));
                        measurement.setAccY(cursor.getFloat(5));
                        measurement.setAccZ(cursor.getFloat(6));
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
        String[] cols = new String[] {ROW_ID, ROW_DATE, ROW_LATITUDE, ROW_LONGITUDE, ROW_ACC_X, ROW_ACC_Y, ROW_ACC_Z};
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor mCursor = db.query(true, TABLE,cols,null
                , null, null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor; // iterate to get each value.
    }
}