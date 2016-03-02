package de.tudarmstadt.tk.dbsystel.mobilenetworkmeasurement.connection;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import de.tudarmstadt.tk.dbsystel.mobilenetworkmeasurement.Measurement;
import de.tudarmstadt.tk.dbsystel.mobilenetworkmeasurement.StartService;

public class HTTPFunctions {

    private ServerConnection serverConnection;

    private static String POST_TYPE = "addmobilenetworkparameter";

    public final static String POST_TYPE_IDENTIFIER = "type";
    public final static String POST_AUTH_PW_IDENTIFIER = "AUTH_PW";
    public final static String POST_IMEI_IDENTIFIER = "imei";
    public final static String POST_DEVICE_IDENTIFIER = "device";
    public final static String POST_DATE_IDENTIFIER = "date";
    public final static String POST_LATITUDE_IDENTIFIER = "latitude";
    public final static String POST_LONGITUDE_IDENTIFIER = "longitude";
    public final static String POST_CELLID_IDENTIFIER = "cellid";
    public final static String POST_OPERATOR_IDENTIFIER = "operator";
    public final static String POST_NETWORKTYPE_IDENTIFIER = "networktype";
    public final static String POST_LEVEL_IDENTIFIER = "level";
    public final static String POST_DBM_IDENTIFIER = "dbm";
    public final static String POST_ASU_IDENTIFIER = "asu";
    public final static String POST_PCI_IDENTIFIER = "pci";

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
        params.add(new BasicNameValuePair(POST_CELLID_IDENTIFIER, Integer.toString(measurement.getCellID())));
        params.add(new BasicNameValuePair(POST_OPERATOR_IDENTIFIER, measurement.getOperator()));
        params.add(new BasicNameValuePair(POST_NETWORKTYPE_IDENTIFIER, measurement.getNetworkType()));
        params.add(new BasicNameValuePair(POST_LEVEL_IDENTIFIER, Integer.toString(measurement.getLevel())));
        params.add(new BasicNameValuePair(POST_DBM_IDENTIFIER, Integer.toString(measurement.getDbm())));
        params.add(new BasicNameValuePair(POST_ASU_IDENTIFIER, Integer.toString(measurement.getAsu())));
        params.add(new BasicNameValuePair(POST_PCI_IDENTIFIER, Integer.toString(measurement.getPci())));

        return serverConnection.get(StartService.API_URL, params);
    }
}