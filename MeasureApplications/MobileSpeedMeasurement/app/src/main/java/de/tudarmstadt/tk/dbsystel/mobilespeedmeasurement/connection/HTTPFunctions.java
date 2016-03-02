package de.tudarmstadt.tk.dbsystel.mobilespeedmeasurement.connection;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import de.tudarmstadt.tk.dbsystel.mobilespeedmeasurement.Measurement;
import de.tudarmstadt.tk.dbsystel.mobilespeedmeasurement.StartService;

public class HTTPFunctions {

    private ServerConnection serverConnection;

    private static String POST_TYPE = "addmobilenetworkspeed";

    public final static String POST_TYPE_IDENTIFIER = "type";
    public final static String POST_AUTH_PW_IDENTIFIER = "AUTH_PW";
    public final static String POST_IMEI_IDENTIFIER = "imei";
    public final static String POST_DEVICE_IDENTIFIER = "device";
    public final static String POST_DATE_IDENTIFIER = "date";
    public final static String POST_LATITUDE_IDENTIFIER = "latitude";
    public final static String POST_LONGITUDE_IDENTIFIER = "longitude";
    public final static String POST_OPERATOR_IDENTIFIER = "operator";
    public final static String POST_NETWORKTYPE_IDENTIFIER = "networktype";
    public final static String POST_DOWNLOADSPEED_IDENTIFIER = "downloadspeed";
    public final static String POST_UPLOADSPEED_IDENTIFIER = "uploadspeed";
    public final static String POST_DOWNLOADSPEEDS_IDENTIFIER = "downloadspeeds";
    public final static String POST_UPLOADSPEEDS_IDENTIFIER = "uploadspeeds";

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
        params.add(new BasicNameValuePair(POST_OPERATOR_IDENTIFIER, measurement.getOperator()));
        params.add(new BasicNameValuePair(POST_NETWORKTYPE_IDENTIFIER, measurement.getNetworkType()));
        params.add(new BasicNameValuePair(POST_DOWNLOADSPEED_IDENTIFIER, measurement.getDownloadSpeed()));
        params.add(new BasicNameValuePair(POST_UPLOADSPEED_IDENTIFIER, measurement.getUploadSpeed()));
        params.add(new BasicNameValuePair(POST_DOWNLOADSPEEDS_IDENTIFIER, measurement.getDownloadSpeeds()));
        params.add(new BasicNameValuePair(POST_UPLOADSPEEDS_IDENTIFIER, measurement.getUploadSpeeds()));

        return serverConnection.get(StartService.API_URL, params);
    }
}