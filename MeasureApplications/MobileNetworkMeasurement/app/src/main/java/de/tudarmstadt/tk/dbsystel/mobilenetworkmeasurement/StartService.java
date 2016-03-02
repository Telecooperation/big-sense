package de.tudarmstadt.tk.dbsystel.mobilenetworkmeasurement;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

public class StartService extends Service {

    public final static String LOG_TAG = "MobileParameter";

    public static String API_URL;
    public static String AUTH_PW;
    public static int API_PORT;
    public static int MIN_GPS_UPDATE_INTERVAL_SEC;
    public static int MIN_GPS_UPDATE_DISTANCE_METER;
    public static int MEASUREMENT_INTERVAL_SEC;
    public static int UPLOAD_INTERVAL_SEC;
    public static boolean ONLY_MEASURE_WHILE_MOVING;

    private final Timer measurementTimer = new Timer();
    private final Timer uploadTimer = new Timer();

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
                MEASUREMENT_INTERVAL_SEC = config.getInt("measurement_interval_sec");
            } catch (JSONException e) {
                MEASUREMENT_INTERVAL_SEC = Config.MEASUREMENT_INTERVAL_SEC;
            }
            try {
                UPLOAD_INTERVAL_SEC = config.getInt("upload_interval_sec");
            } catch (JSONException e) {
                UPLOAD_INTERVAL_SEC = Config.UPLOAD_INTERVAL_SEC;
            }

            try {
                ONLY_MEASURE_WHILE_MOVING = config.getBoolean("only_measure_while_moving");
            } catch (JSONException e) {
                ONLY_MEASURE_WHILE_MOVING = Config.ONLY_MEASURE_WHILE_MOVING;
            }
        }

        final CollectAndSend collectAndSend = new CollectAndSend(getApplicationContext());

        measurementTimer.scheduleAtFixedRate(new TimerTask() {
           @Override
            public void run() {
                collectAndSend.makeMeasurement();
            }
        }, 60000, MEASUREMENT_INTERVAL_SEC*1000);

        uploadTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                collectAndSend.sendMeasurements();
            }
        }, 60000, UPLOAD_INTERVAL_SEC*1000);
        return flags;
    }

    @Override
    public void onDestroy() {
        measurementTimer.cancel();
        uploadTimer.cancel();
        super.onDestroy();
    }
}
