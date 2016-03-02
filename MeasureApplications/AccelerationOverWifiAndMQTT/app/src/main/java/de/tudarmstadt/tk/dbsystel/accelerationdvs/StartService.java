package de.tudarmstadt.tk.dbsystel.accelerationdvs;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

import org.json.JSONException;
import org.json.JSONObject;

public class StartService extends Service {

    public final static String LOG_TAG = "AccelerationDVS";

    public static String BROKER_PORT;
    public static String TOPIC;
    public static String WLAN_SSID;
    public static String WLAN_PASSWORD;
    public static long SEND_INTERVAL_MSEC;

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
                BROKER_PORT = config.getString("broker_port");
            } catch (JSONException e) {
                BROKER_PORT = Config.BROKER_PORT;
            }
            try {
                TOPIC = config.getString("topic");
            } catch (JSONException e) {
                TOPIC = Config.TOPIC;
            }
            try {
                WLAN_SSID = config.getString("wlan_ssid");
            } catch (JSONException e) {
                WLAN_SSID = Config.WLAN_SSID;
            }
            try {
                WLAN_PASSWORD = config.getString("wlan_password");
            } catch (JSONException e) {
                WLAN_PASSWORD = Config.WLAN_PASSWORD;
            }
            try {
                SEND_INTERVAL_MSEC = config.getLong("send_interval_msec");
            } catch (JSONException e) {
                SEND_INTERVAL_MSEC = Config.SEND_INTERVAL_MSEC;
            }
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                new CollectAndSend(getApplicationContext());
            }
        }).start();
        return flags;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
