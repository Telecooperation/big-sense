package de.tudarmstadt.tk.dbsystel.wifimeasurement.connection;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import de.tudarmstadt.tk.dbsystel.wifimeasurement.Measurement;
import de.tudarmstadt.tk.dbsystel.wifimeasurement.StartService;

public class HTTPFunctions {

    private ServerConnection serverConnection;

    private static String POST_TYPE = "addwifinetwork";

    public final static String POST_TYPE_IDENTIFIER = "type";
    public final static String POST_AUTH_PW_IDENTIFIER = "AUTH_PW";
    public final static String POST_IMEI_IDENTIFIER = "imei";
    public final static String POST_DEVICE_IDENTIFIER = "device";
    public final static String POST_DATE_IDENTIFIER = "date";
    public final static String POST_LATITUDE_IDENTIFIER = "latitude";
    public final static String POST_LONGITUDE_IDENTIFIER = "longitude";
    public final static String POST_BSSID_IDENTIFIER = "bssid";
    public final static String POST_SSID_IDENTIFIER = "ssid";
    public final static String POST_CAPABILITIES_IDENTIFIER = "capabilities";
    public final static String POST_FREQUENCY_IDENTIFIER = "frequency";
    public final static String POST_LEVEL_IDENTIFIER = "level";

    // constructor
    public HTTPFunctions(){
        serverConnection = new ServerConnection();
    }

    public JSONObject sendMeasurement(Measurement measurement, String imei, String device) throws Exception{
        // Building Parameters
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair(POST_AUTH_PW_IDENTIFIER, StartService.AUTH_PW));
        params.add(new BasicNameValuePair(POST_TYPE_IDENTIFIER, POST_TYPE));
        params.add(new BasicNameValuePair(POST_IMEI_IDENTIFIER, imei));
        params.add(new BasicNameValuePair(POST_DEVICE_IDENTIFIER, device));
        params.add(new BasicNameValuePair(POST_DATE_IDENTIFIER, Long.toString(measurement.getDate())));
        params.add(new BasicNameValuePair(POST_LATITUDE_IDENTIFIER, Double.toString(measurement.getLatitude())));
        params.add(new BasicNameValuePair(POST_LONGITUDE_IDENTIFIER, Double.toString(measurement.getLongitude())));
        params.add(new BasicNameValuePair(POST_BSSID_IDENTIFIER, measurement.getBssid()));
        params.add(new BasicNameValuePair(POST_SSID_IDENTIFIER, measurement.getSsid()));
        params.add(new BasicNameValuePair(POST_CAPABILITIES_IDENTIFIER, measurement.getCapabilities()));
        params.add(new BasicNameValuePair(POST_FREQUENCY_IDENTIFIER, Integer.toString(measurement.getFrequency())));
        params.add(new BasicNameValuePair(POST_LEVEL_IDENTIFIER, Integer.toString(measurement.getLevel())));

        return serverConnection.get(StartService.API_URL, params);
    }
}