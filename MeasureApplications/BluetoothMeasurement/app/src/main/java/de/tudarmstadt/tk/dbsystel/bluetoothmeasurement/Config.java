package de.tudarmstadt.tk.dbsystel.bluetoothmeasurement;

/**
 * Created by Martin on 25.11.2015.
 */
public class Config {
    public final static String API_URL = "coolURL";
    public final static String AUTH_PW = "coolPW";
    public final static int API_PORT = 80;
    public final static int MIN_GPS_UPDATE_INTERVAL_SEC = 5;
    public final static int MIN_GPS_UPDATE_DISTANCE_METER = 50;
    public final static int MEASUREMENT_INTERVAL_SEC = 180;
    public final static int UPLOAD_INTERVAL_SEC = 600;
    public final static boolean ONLY_MEASURE_WHILE_MOVING = true;
}
