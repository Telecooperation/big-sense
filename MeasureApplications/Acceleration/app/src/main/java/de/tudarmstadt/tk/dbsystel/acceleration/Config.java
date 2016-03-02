package de.tudarmstadt.tk.dbsystel.acceleration;

/**
 * Created by Martin on 25.11.2015.
 */
public class Config {
    public final static String API_URL = "coolURL";
    public final static String AUTH_PW = "coolPW";
    public final static int API_PORT = 80;
    public final static int MIN_GPS_UPDATE_INTERVAL_SEC = 5;
    public final static int MIN_GPS_UPDATE_DISTANCE_METER = 50;

    public static float ACCELERATION_THRESHOLD = 0.8f;
    public static String IMPORTANT_AXES = "xz";
}
