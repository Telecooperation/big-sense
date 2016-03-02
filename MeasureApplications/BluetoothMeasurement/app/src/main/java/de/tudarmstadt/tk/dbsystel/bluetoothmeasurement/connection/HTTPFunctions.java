package de.tudarmstadt.tk.dbsystel.bluetoothmeasurement.connection;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import de.tudarmstadt.tk.dbsystel.bluetoothmeasurement.Measurement;
import de.tudarmstadt.tk.dbsystel.bluetoothmeasurement.StartService;

public class HTTPFunctions {

    private ServerConnection serverConnection;

    private static String POST_TYPE = "addbluetooth";

    public final static String POST_TYPE_IDENTIFIER = "type";
    public final static String POST_AUTH_PW_IDENTIFIER = "AUTH_PW";
    public final static String POST_IMEI_IDENTIFIER = "imei";
    public final static String POST_DEVICE_IDENTIFIER = "device";
    public final static String POST_DATE_IDENTIFIER = "date";
    public final static String POST_LATITUDE_IDENTIFIER = "latitude";
    public final static String POST_LONGITUDE_IDENTIFIER = "longitude";
    public final static String POST_NAME_IDENTIFIER = "name";
    public final static String POST_ADDRESS_IDENTIFIER = "address";
    public final static String POST_DEVICETYPE_IDENTIFIER = "devicetype";

    // constructor
    public HTTPFunctions(){
        serverConnection = new ServerConnection();
    }

    public JSONObject sendMeasurement(Measurement measurement, String imei, String device) throws Exception{
        // Building Parameters
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair(POST_AUTH_PW_IDENTIFIER, StartService.AUTH_PW));
        params.add(new BasicNameValuePair(POST_TYPE_IDENTIFIER, POST_TYPE));
        params.add(new BasicNameValuePair(POST_IMEI_IDENTIFIER, imei));
        params.add(new BasicNameValuePair(POST_DEVICE_IDENTIFIER, device));
        params.add(new BasicNameValuePair(POST_DATE_IDENTIFIER, Long.toString(measurement.getDate())));
        params.add(new BasicNameValuePair(POST_LATITUDE_IDENTIFIER, Double.toString(measurement.getLatitude())));
        params.add(new BasicNameValuePair(POST_LONGITUDE_IDENTIFIER, Double.toString(measurement.getLongitude())));
        params.add(new BasicNameValuePair(POST_NAME_IDENTIFIER, measurement.getName()));
        params.add(new BasicNameValuePair(POST_ADDRESS_IDENTIFIER, measurement.getAddress()));
        params.add(new BasicNameValuePair(POST_DEVICETYPE_IDENTIFIER, measurement.getType()));

        return serverConnection.get(StartService.API_URL, params);
    }
}