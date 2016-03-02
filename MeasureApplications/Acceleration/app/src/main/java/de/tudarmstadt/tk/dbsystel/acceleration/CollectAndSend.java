package de.tudarmstadt.tk.dbsystel.acceleration;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.List;

import de.tudarmstadt.tk.dbsystel.acceleration.connection.HTTPFunctions;
import de.tudarmstadt.tk.dbsystel.acceleration.localdb.MeasurementDB;
import de.tudarmstadt.tk.dbsystel.acceleration.sensors.PositionManager;

/**
 * Created by Martin on 24.11.2015.
 */
public class CollectAndSend {

    private Context context;
    private MeasurementDB measurementDB;
    private PositionManager positionManager;
    private HTTPFunctions httpFunctions;

    private float gravityX;
    private float gravityY;
    private float gravityZ;

    private long startTimestamp;
    private boolean actuallySending;

    /**
     * Starts all ongoing measurements
     * @param context
     */
    public CollectAndSend(Context context) {
        this.context = context;
        measurementDB = new MeasurementDB(context);
        httpFunctions = new HTTPFunctions();
        positionManager = new PositionManager(context);

        Date now = new Date();
        startTimestamp = now.getTime();
        actuallySending = false;

        SensorManager sensorManager = (SensorManager) this.context.getSystemService(Context.SENSOR_SERVICE);
        Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        sensorManager.registerListener(new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {

                float x = event.values[0];
                float y = event.values[1];
                float z = event.values[2];
                processSensorData(x, y, z);
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
            }

        }, sensor, SensorManager.SENSOR_DELAY_FASTEST);
    }

    /**
     * This function transforms the accelerometer data into linear acceleration with a high pass filter
     * @param x
     * @param y
     * @param z
     */
    private void processSensorData(float x, float y, float z) {
        // alpha is calculated as t / (t + dT)
        // with t, the low-pass filter's time-constant
        // and dT, the event delivery rate
        final float alpha = 0.8f;

        gravityX = alpha * gravityX + (1 - alpha) * x;
        gravityY = alpha * gravityY + (1 - alpha) * y;
        gravityZ = alpha * gravityZ + (1 - alpha) * z;

        final float linearAccelerationX = x - gravityX;
        final float linearAccelerationY = y - gravityY;
        final float linearAccelerationZ = z - gravityZ;

        //Don't use the data of the first 10 seconds
        Date now = new Date();
        if(now.getTime() > startTimestamp + 10000 &&
                ((StartService.IMPORTANT_AXES.contains("x") && linearAccelerationX > StartService.ACCELERATION_THRESHOLD) ||
                        (StartService.IMPORTANT_AXES.contains("y") && linearAccelerationY > StartService.ACCELERATION_THRESHOLD) ||
                        (StartService.IMPORTANT_AXES.contains("z") && linearAccelerationZ > StartService.ACCELERATION_THRESHOLD))) {
            //start a new thread to store the data and send it afterwards
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Location location = positionManager.getLastKnownLocation();
                    if (location != null) {
                        Measurement tempMeasurement = new Measurement();
                        Date now = new Date();
                        tempMeasurement.setDate(now.getTime());
                        tempMeasurement.setLatitude(location.getLatitude());
                        tempMeasurement.setLongitude(location.getLongitude());
                        tempMeasurement.setAccX(linearAccelerationX);
                        tempMeasurement.setAccY(linearAccelerationY);
                        tempMeasurement.setAccZ(linearAccelerationZ);
                        measurementDB.addMeasurement(tempMeasurement);
                        Log.i(StartService.LOG_TAG, "Stored Measurement");
                    }

                    sendMeasurements();
                }
            }).start();
        }
    }

    /**
     * sends all measurements, which are in databank
     */
    public void sendMeasurements() {
        if(!actuallySending) {
            actuallySending = true;
            List<Measurement> allRemainingOnes = measurementDB.getAllRemainingRemainingMeasurements();
            Log.i(StartService.LOG_TAG, "Measurements to send: " + allRemainingOnes.size());

            if(allRemainingOnes.size() > 0) {
                try {
                    TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                    String imei = telephonyManager.getDeviceId();
                    JSONObject output = httpFunctions.sendMeasurement(allRemainingOnes, imei, getDeviceName());
                    if (output.getInt("success") == 1) {
                        for (Measurement measurement : allRemainingOnes) {
                            measurementDB.deleteMeasurement(measurement);
                            Log.i(StartService.LOG_TAG, "Sent and deleted local measurement with timestamp: " + measurement.getDate());
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            actuallySending = false;
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
