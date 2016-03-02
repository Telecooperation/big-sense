package de.tudarmstadt.tk.dbsystel.mobilespeedmeasurement.localdb;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import de.tudarmstadt.tk.dbsystel.mobilespeedmeasurement.Measurement;

public class MeasurementDB {

    private DatabaseHelper dbHelper;
    public final static String TABLE="Measurements";

    public final static String ROW_ID="id";
    public final static String ROW_DATE="date";
    public final static String ROW_LATITUDE="latitude";
    public final static String ROW_LONGITUDE="longitude";
    public final static String ROW_OPERATOR="operator";
    public final static String ROW_NETWORKTYPE="networktype";
    public final static String ROW_DOWNLOADSPEED="downloadspeed";
    public final static String ROW_UPLOADSPEED="uploadspeed";
    public final static String ROW_DOWNLOADSPEEDS="downloadspeeds";
    public final static String ROW_UPLOADSPEEDS="uploadspeeds";


    public static final String MEASUREMENTDB_CREATE = "CREATE TABLE " + TABLE + " (" + ROW_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
            ROW_DATE + " LONG, " +
            ROW_LATITUDE + " DOUBLE, " +
            ROW_LONGITUDE + " DOUBLE, " +
            ROW_OPERATOR + " VARCHAR, " +
            ROW_NETWORKTYPE + " VARCHAR, " +
            ROW_DOWNLOADSPEED + " VARCHAR, " +
            ROW_UPLOADSPEED + " VARCHAR, " +
            ROW_DOWNLOADSPEEDS + " VARCHAR, " +
            ROW_UPLOADSPEEDS + " VARCHAR);";
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
        values.put(ROW_OPERATOR, measurement.getOperator());
        values.put(ROW_NETWORKTYPE, measurement.getNetworkType());
        values.put(ROW_DOWNLOADSPEED, measurement.getDownloadSpeed());
        values.put(ROW_UPLOADSPEED, measurement.getUploadSpeed());
        values.put(ROW_DOWNLOADSPEEDS, measurement.getDownloadSpeeds());
        values.put(ROW_UPLOADSPEEDS, measurement.getUploadSpeeds());

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
                        measurement.setOperator(cursor.getString(4));
                        measurement.setNetworkType(cursor.getString(5));
                        measurement.setDownloadSpeed(cursor.getString(6));
                        measurement.setUploadSpeed(cursor.getString(7));
                        measurement.setDownloadSpeeds(cursor.getString(8));
                        measurement.setUploadSpeeds(cursor.getString(9));
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
        String[] cols = new String[] {ROW_ID, ROW_DATE, ROW_LATITUDE, ROW_LONGITUDE, ROW_OPERATOR, ROW_NETWORKTYPE, ROW_DOWNLOADSPEED, ROW_UPLOADSPEED, ROW_DOWNLOADSPEEDS, ROW_UPLOADSPEEDS};
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor mCursor = db.query(true, TABLE,cols,null
                , null, null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor; // iterate to get each value.
    }
}