package de.tudarmstadt.tk.dbsystel.mobilespeedmeasurement;

import android.content.Context;
import android.location.Location;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.List;

import de.tudarmstadt.tk.dbsystel.mobilespeedmeasurement.connection.HTTPFunctions;
import de.tudarmstadt.tk.dbsystel.mobilespeedmeasurement.localdb.MeasurementDB;
import de.tudarmstadt.tk.dbsystel.mobilespeedmeasurement.sensors.MobileSpeed;
import de.tudarmstadt.tk.dbsystel.mobilespeedmeasurement.sensors.PositionManager;

/**
 * Created by Martin on 24.11.2015.
 */
public class CollectAndSend {

    private Context context;
    private MeasurementDB measurementDB;
    private MobileSpeed mobilespeed;
    private PositionManager positionManager;
    private HTTPFunctions httpFunctions;

    private Location lastSavedLocation;

    /**
     * Starts all ongoing measurements
     * @param context
     */
    public CollectAndSend(Context context) {
        this.context = context;
        measurementDB = new MeasurementDB(context);
        httpFunctions = new HTTPFunctions();

        mobilespeed = new MobileSpeed(context);
        positionManager = new PositionManager(context);
    }

    /**
     * Uses actual measure data and stores them together with gps coordinates in local db
     * Afterwards it tries to send the data to inet db
     */
    public void makeAndSendMeasurement() {
        Location location = positionManager.getLastKnownLocation();
        boolean locationHasMoved = true;
        if(lastSavedLocation != null) {
            if(location.distanceTo(lastSavedLocation) < StartService.MIN_GPS_UPDATE_DISTANCE_METER) locationHasMoved = false;
        }

        Log.i(StartService.LOG_TAG, "Make new Measurement");
        if(((StartService.ONLY_MEASURE_WHILE_MOVING && locationHasMoved) ||
                    !StartService.ONLY_MEASURE_WHILE_MOVING) && location != null) {
            mobilespeed.measureSpeed();

            while(!mobilespeed.isTestOver()) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                }
            }

            if(!mobilespeed.isTestFailed()) {
                lastSavedLocation = location;

                Measurement tempMeasurement = new Measurement();
                Date now = new Date();
                tempMeasurement.setDate(now.getTime());
                tempMeasurement.setLatitude(location.getLatitude());
                tempMeasurement.setLongitude(location.getLongitude());
                tempMeasurement.setOperator(mobilespeed.getOperator());
                tempMeasurement.setNetworkType(mobilespeed.getNetworkType());
                tempMeasurement.setDownloadSpeed(mobilespeed.getDownloadSpeed());
                tempMeasurement.setUploadSpeed(mobilespeed.getUploadSpeed());
                tempMeasurement.setDownloadSpeeds(mobilespeed.getDownloadSpeeds().toString());
                tempMeasurement.setUploadSpeeds(mobilespeed.getUploadSpeeds().toString());
                measurementDB.addMeasurement(tempMeasurement);

                Log.i(StartService.LOG_TAG, "Stored Measurement");

                sendMeasurements();
            }
        }
    }

    /**
     * sends all measurements, which are in databank
     */
    private void sendMeasurements() {
        List<Measurement> allRemainingOnes = measurementDB.getAllRemainingRemainingMeasurements();
        Log.i(StartService.LOG_TAG, "Measurements to send: " + allRemainingOnes.size());
        for(Measurement measurement : allRemainingOnes) {
            try {
                TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                String imei = telephonyManager.getDeviceId();
                JSONObject output = httpFunctions.sendMeasurement(measurement, imei, getDeviceName());
                if (output.getInt("success") == 1) {
                    measurementDB.deleteMeasurement(measurement);
                    Log.i(StartService.LOG_TAG, "Sent and deleted local measurement with timestamp: " + measurement.getDate());
                }
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return model;
        } else {
            return manufacturer + " " + model;
        }
    }
}
