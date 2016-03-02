package de.tudarmstadt.tk.dbsystel.acceleration;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

import org.json.JSONException;
import org.json.JSONObject;

public class StartService extends Service {

    public final static String LOG_TAG = "Acceleration";
    
    public static String API_URL;
    public static String AUTH_PW;
    public static int API_PORT;
    public static int MIN_GPS_UPDATE_INTERVAL_SEC;
    public static int MIN_GPS_UPDATE_DISTANCE_METER;

    public static float ACCELERATION_THRESHOLD;
    public static String IMPORTANT_AXES; //This can be a combination of x, y and z

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Notification notification = new NotificationCompat.Builder(this).build();
        startForeground(startId, notification);

        //First get config
        if(intent.getExtras() != null) {
            Object extra_conf = intent.getExtras().get("config");
            JSONObject config = new JSONObject();

            if (extra_conf instanceof JSONObject) {
                config = (JSONObject) extra_conf;
            } else if (extra_conf instanceof String) {
                String str = (String) extra_conf;
                try {
                    config = new JSONObject(str);
                } catch (JSONException e) {
                }
            } else {
                throw new IllegalStateException("Type of intent extra not a config: " + extra_conf.getClass().getCanonicalName());
            }

            //Get the possible config parameters, which can be set by webinterface
            try {
                API_URL = config.getString("api_url");
            } catch (JSONException e) {
                API_URL = Config.API_URL;
            }
            try {
                AUTH_PW = config.getString("auth_pw");
            } catch (JSONException e) {
                AUTH_PW = Config.AUTH_PW;
            }
            try {
                API_PORT = config.getInt("api_port");
            } catch (JSONException e) {
                API_PORT = Config.API_PORT;
            }
            try {
                MIN_GPS_UPDATE_INTERVAL_SEC = config.getInt("min_gps_update_interval_sec");
            } catch (JSONException e) {
                MIN_GPS_UPDATE_INTERVAL_SEC = Config.MIN_GPS_UPDATE_INTERVAL_SEC;
            }
            try {
                MIN_GPS_UPDATE_DISTANCE_METER = config.getInt("min_gps_update_distance_meter");
            } catch (JSONException e) {
                MIN_GPS_UPDATE_DISTANCE_METER = Config.MIN_GPS_UPDATE_DISTANCE_METER;
            }

            try {
                ACCELERATION_THRESHOLD = (float) config.getDouble("acceleration_threshold");
            } catch (JSONException e) {
                ACCELERATION_THRESHOLD = Config.ACCELERATION_THRESHOLD;
            }
            try {
                IMPORTANT_AXES = config.getString("important_axes");
            } catch (JSONException e) {
                IMPORTANT_AXES = Config.IMPORTANT_AXES;
            }
        }

        final CollectAndSend collectAndSend = new CollectAndSend(getApplicationContext());

        //Send old measurements at start of the app
        new Thread(new Runnable() {
            @Override
            public void run() {
                collectAndSend.sendMeasurements();
            }
        }).start();

        return flags;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
