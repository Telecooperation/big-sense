package de.tudarmstadt.tk.dbsystel.mobilespeedmeasurement;

/**
 * Created by Martin on 25.11.2015.
 */
public class Config {
    public final static String API_URL = "coolURL";
    public final static String AUTH_PW = "coolPW";
    public final static int API_PORT = 80;
    public final static int MIN_GPS_UPDATE_INTERVAL_SEC = 60;
    public final static int MIN_GPS_UPDATE_DISTANCE_METER = 200;
    public final static int MEASUREMENT_INTERVAL_SEC = 900;
    public final static boolean ONLY_MEASURE_WHILE_MOVING = true;
    public static String DOWNLOAD_URL = "coolDownloadURL";
    public static String DOWNLOAD_FILE = "/SpeedTest/100.txt";
    public static int DOWNLOAD_PORT = 80;
    public static String UPLOAD_URL = "coolUploadURL";
    public static String UPLOAD_FILE = "/SpeedTest/uploadPost.html";
    public static int UPLOAD_PORT = 80;
    public static int UPLOAD_SIZE_BYTES = 100000;
}
