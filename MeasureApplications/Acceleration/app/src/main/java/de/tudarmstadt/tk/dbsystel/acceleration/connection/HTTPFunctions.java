package de.tudarmstadt.tk.dbsystel.acceleration.connection;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import de.tudarmstadt.tk.dbsystel.acceleration.Measurement;
import de.tudarmstadt.tk.dbsystel.acceleration.StartService;

public class HTTPFunctions {

    private ServerConnection serverConnection;

    private static String POST_TYPE = "addacceleration";

    public final static String POST_TYPE_IDENTIFIER = "type";
    public final static String POST_AUTH_PW_IDENTIFIER = "AUTH_PW";
    public final static String POST_ALL_DATA_IDENTIFIER = "allDataArray";

    public final static String POST_IMEI_IDENTIFIER = "imei";
    public final static String POST_DEVICE_IDENTIFIER = "device";
    public final static String POST_DATE_IDENTIFIER = "date";
    public final static String POST_LATITUDE_IDENTIFIER = "latitude";
    public final static String POST_LONGITUDE_IDENTIFIER = "longitude";
    public final static String POST_ACC_X_IDENTIFIER = "accx";
    public final static String POST_ACC_Y_IDENTIFIER = "accy";
    public final static String POST_ACC_Z_IDENTIFIER = "accz";

    // constructor
    public HTTPFunctions(){
        serverConnection = new ServerConnection();
    }

    public JSONObject sendMeasurement(List<Measurement> measurements, String imei, String device) throws Exception{
        JSONArray jsonDataArray = new JSONArray();
        for(Measurement measurement : measurements) {
            JSONObject jsonMeasurement = new JSONObject();
            jsonMeasurement.put(POST_IMEI_IDENTIFIER, imei);
            jsonMeasurement.put(POST_DEVICE_IDENTIFIER, device);
            jsonMeasurement.put(POST_DATE_IDENTIFIER, Long.toString(measurement.getDate()));
            jsonMeasurement.put(POST_LATITUDE_IDENTIFIER, Double.toString(measurement.getLatitude()));
            jsonMeasurement.put(POST_LONGITUDE_IDENTIFIER, Double.toString(measurement.getLongitude()));
            jsonMeasurement.put(POST_ACC_X_IDENTIFIER, Float.toString(measurement.getAccX()));
            jsonMeasurement.put(POST_ACC_Y_IDENTIFIER, Float.toString(measurement.getAccY()));
            jsonMeasurement.put(POST_ACC_Z_IDENTIFIER, Float.toString(measurement.getAccZ()));
            jsonDataArray.put(jsonMeasurement);
        }

        // Building Parameters
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair(POST_AUTH_PW_IDENTIFIER, StartService.AUTH_PW));
        params.add(new BasicNameValuePair(POST_TYPE_IDENTIFIER, POST_TYPE));
        params.add(new BasicNameValuePair(POST_IMEI_IDENTIFIER, imei));
        params.add(new BasicNameValuePair(POST_DEVICE_IDENTIFIER, device));
        params.add(new BasicNameValuePair(POST_ALL_DATA_IDENTIFIER, jsonDataArray.toString().replace("\\", "")));

        return serverConnection.get(StartService.API_URL, params);
    }
}