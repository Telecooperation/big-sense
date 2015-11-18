package de.tudarmstadt.tk.dbsystel.peopleamountmeasure;

import android.app.IntentService;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import org.json.JSONException;
import org.json.JSONObject;

import static android.widget.Toast.*;


/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 */
public class StartService extends Service {

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent.getExtras() != null) { // intent extras provided
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
                throw new IllegalStateException("Type of intent extra not a config: "+extra_conf.getClass().getCanonicalName());
            }

            //Get the possible config parameters, which can be set by webinterface

            long interval = 0;
            try {
                interval = config.getLong("upload_interval");
                makeText(getApplicationContext(), "Interval: " + interval, LENGTH_LONG).show();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            //Start here with your normal application
        }
        return flags;
    }
}
