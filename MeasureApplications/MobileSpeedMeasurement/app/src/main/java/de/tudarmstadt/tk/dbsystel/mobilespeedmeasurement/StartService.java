package de.tudarmstadt.tk.dbsystel.mobilespeedmeasurement;

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

    public final static String LOG_TAG = "MobileSpeed";

    public static String API_URL;
    public static String AUTH_PW;
    public static int API_PORT;
    public static int MIN_GPS_UPDATE_INTERVAL_SEC;
    public static int MIN_GPS_UPDATE_DISTANCE_METER;
    public static int MEASUREMENT_INTERVAL_SEC;
    public static boolean ONLY_MEASURE_WHILE_MOVING;
    public static String DOWNLOAD_URL;
    public static String DOWNLOAD_FILE;
    public static int DOWNLOAD_PORT;
    public static String UPLOAD_URL;
    public static String UPLOAD_FILE;
    public static int UPLOAD_PORT;
    public static int UPLOAD_SIZE_BYTES;

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
                ONLY_MEASURE_WHILE_MOVING = config.getBoolean("only_measure_while_moving");
            } catch (JSONException e) {
                ONLY_MEASURE_WHILE_MOVING = Config.ONLY_MEASURE_WHILE_MOVING;
            }
            try {
                DOWNLOAD_URL = config.getString("download_url");
            } catch (JSONException e) {
                DOWNLOAD_URL = Config.DOWNLOAD_URL;
            }
            try {
                DOWNLOAD_FILE = config.getString("download_file");
            } catch (JSONException e) {
                DOWNLOAD_FILE = Config.DOWNLOAD_FILE;
            }
            try {
                DOWNLOAD_PORT = config.getInt("download_port");
            } catch (JSONException e) {
                DOWNLOAD_PORT = Config.DOWNLOAD_PORT;
            }
            try {
                UPLOAD_URL = config.getString("upload_url");
            } catch (JSONException e) {
                UPLOAD_URL = Config.UPLOAD_URL;
            }
            try {
                UPLOAD_FILE = config.getString("upload_file");
            } catch (JSONException e) {
                UPLOAD_FILE = Config.UPLOAD_FILE;
            }
            try {
                UPLOAD_PORT = config.getInt("upload_port");
            } catch (JSONException e) {
                UPLOAD_PORT = Config.UPLOAD_PORT;
            }
            try {
                UPLOAD_SIZE_BYTES = config.getInt("upload_size_bytes");
            } catch (JSONException e) {
                UPLOAD_SIZE_BYTES = Config.UPLOAD_SIZE_BYTES;
            }
        }

        final CollectAndSend collectAndSend = new CollectAndSend(getApplicationContext());

        measurementTimer.scheduleAtFixedRate(new TimerTask() {
           @Override
            public void run() {
                collectAndSend.makeAndSendMeasurement();
            }
        }, 60000, MEASUREMENT_INTERVAL_SEC*1000);

        return flags;
    }

    @Override
    public void onDestroy() {
        measurementTimer.cancel();
        uploadTimer.cancel();
        super.onDestroy();
    }
}
