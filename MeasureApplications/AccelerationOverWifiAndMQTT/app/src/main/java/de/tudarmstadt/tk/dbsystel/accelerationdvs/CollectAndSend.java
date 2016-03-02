package de.tudarmstadt.tk.dbsystel.accelerationdvs;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Martin on 24.11.2015.
 */
public class CollectAndSend {

    private Context context;
    private MQTTConnection mqttConnection;
    private Hotspot hotspot;

    private long startTimestamp;
    private long lastSentTimestamp;

    private List<Float> valuesSinceLastSendingX;
    private List<Float> valuesSinceLastSendingY;
    private List<Float> valuesSinceLastSendingZ;

    /**
     * Starts all ongoing measurements
     * @param context
     */
    public CollectAndSend(Context context) {
        this.context = context;
        hotspot = new Hotspot(context);
        hotspot.openHotspot();
        mqttConnection = new MQTTConnection(context);
        mqttConnection.connect();

        Date now = new Date();
        startTimestamp = now.getTime();
        lastSentTimestamp = now.getTime();

        valuesSinceLastSendingX = new ArrayList<>();
        valuesSinceLastSendingY = new ArrayList<>();
        valuesSinceLastSendingZ = new ArrayList<>();

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

        }, sensor, SensorManager.SENSOR_DELAY_GAME);
    }

    /**
     * This function simply sends the data over the mqtt-connection in the given interval
     * The average of all values since last sending will be used
     * @param x
     * @param y
     * @param z
     */
    private void processSensorData(float x, float y, float z) {
        //Don't use the data of the first 10 seconds
        Date now = new Date();
        if(now.getTime() > startTimestamp + 10000 && now.getTime() > lastSentTimestamp + StartService.SEND_INTERVAL_MSEC) {
            lastSentTimestamp = now.getTime();

            //now calculate the average of the last values
            float averageX = x;
            for(float tempX : valuesSinceLastSendingX) averageX += tempX;
            averageX = averageX / (float) (valuesSinceLastSendingX.size() + 1);
            valuesSinceLastSendingX = new ArrayList<>();

            float averageY = y;
            for(float tempY : valuesSinceLastSendingY) averageY += tempY;
            averageY = averageY / (float) (valuesSinceLastSendingY.size() + 1);
            valuesSinceLastSendingY = new ArrayList<>();

            float averageZ = z;
            for(float tempZ : valuesSinceLastSendingZ) averageZ += tempZ;
            averageZ = averageZ / (float) (valuesSinceLastSendingZ.size() + 1);
            valuesSinceLastSendingZ = new ArrayList<>();

            //Send the data via MQTT to the server
            if(mqttConnection.isConnected()) {
                final float finalAverageX = averageX;
                final float finalAverageY = averageY;
                final float finalAverageZ = averageZ;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        mqttConnection.sendValues(finalAverageX, finalAverageY, finalAverageZ);
                    }
                }).start();
            }
            else {
                if(hotspot.isOpen()) mqttConnection.connect();
                else hotspot.openHotspot();
            }
        }
        else {
            valuesSinceLastSendingX.add(x);
            valuesSinceLastSendingY.add(y);
            valuesSinceLastSendingZ.add(z);
        }
    }
}
