package de.tudarmstadt.tk.dbsystel.mobilespeedmeasurement.sensors;

import android.content.Context;
import android.telephony.TelephonyManager;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import de.tudarmstadt.tk.dbsystel.mobilespeedmeasurement.StartService;
import fr.bmartel.speedtest.ISpeedTestListener;
import fr.bmartel.speedtest.SpeedTestSocket;

/**
 * Created by Martin on 04.12.2015.
 */
public class MobileSpeed {

    private TelephonyManager telephonyManager;
    private Context context;

    private String lastOperator;
    private String operator;
    private String networkType;
    private String downloadSpeed;
    private String uploadSpeed;
    private JSONArray downloadSpeeds;
    private JSONArray uploadSpeeds;
    private boolean testOver;
    private boolean testFailed;

    public MobileSpeed(Context context) {
        this.context = context;
        telephonyManager = (TelephonyManager) this.context.getSystemService(android.content.Context.TELEPHONY_SERVICE);
    }

    public void measureSpeed() {
        lastOperator = operator;
        operator = "";
        this.networkType = "";
        downloadSpeed = "";
        uploadSpeed = "";
        downloadSpeeds = new JSONArray();
        uploadSpeeds = new JSONArray();
        testOver = false;
        testFailed = false;

        new Thread(new Runnable() {
            @Override
            public void run() {
                /* instanciate speed test */
                SpeedTestSocket speedTestSocket = new SpeedTestSocket();

		        /* add a listener to wait for speed test completion and progress */
                speedTestSocket.addSpeedTestListener(new ISpeedTestListener() {

                    @Override
                    public void onDownloadPacketsReceived(int packetSize, float transferRateBitPerSeconds, float transferRateOctetPerSeconds) {
                        downloadSpeed = transferRateBitPerSeconds / 1000 + "kbits";
                        checkIfTestWasSuccessful(transferRateBitPerSeconds);
                    }

                    @Override
                    public void onDownloadProgress(int percent, long timeSinceStart, int alreadyReceivedBytes) {
                        JSONObject tempObject = new JSONObject();
                        try {
                            tempObject.put("timeSinceStart", timeSinceStart);
                            tempObject.put("alreadyReceivedBytes", alreadyReceivedBytes);
                            downloadSpeeds.put(tempObject);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        Log.i(StartService.LOG_TAG, "Download: " + percent);
                    }

                    @Override
                    public void onDownloadError(int errorCode, String message) {
                        testFailed = true;
                        testOver = true;
                        Log.i(StartService.LOG_TAG, "Download error " + errorCode + " occured with message : " + message);
                    }

                    @Override
                    public void onUploadPacketsReceived(int packetSize, float transferRateBitPerSeconds, float transferRateOctetPerSeconds) {
                        uploadSpeed = transferRateBitPerSeconds / 1000 + "kbits";
                        checkIfTestWasSuccessful(transferRateBitPerSeconds);
                    }

                    @Override
                    public void onUploadError(int errorCode, String message) {
                        testFailed = true;
                        testOver = true;
                        Log.i(StartService.LOG_TAG, "Upload error " + errorCode + " occured with message : " + message);
                    }


                    @Override
                    public void onUploadProgress(int percent, long timeSinceStart, int alreadySentBytes) {
                        JSONObject tempObject = new JSONObject();
                        try {
                            tempObject.put("timeSinceStart", timeSinceStart);
                            tempObject.put("alreadySentBytes", alreadySentBytes);
                            uploadSpeeds.put(tempObject);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        Log.i(StartService.LOG_TAG, "Upload: " + percent);
                    }
                });

		        /* start speed test download on favorite server */
                speedTestSocket.startDownload(StartService.DOWNLOAD_URL, StartService.DOWNLOAD_PORT, StartService.DOWNLOAD_FILE);

                // socket will be closed and reading thread will die if it exists
                speedTestSocket.closeSocketJoinRead();

		        /* start speed test upload on favorite server */
                speedTestSocket.startUpload(StartService.UPLOAD_URL, StartService.UPLOAD_PORT, StartService.UPLOAD_FILE, StartService.UPLOAD_SIZE_BYTES);

                // socket will be closed and reading thread will die if it exists
                speedTestSocket.closeSocketJoinRead();
            }
        }).start();
    }


    /**
     * Checks if download and upload were both successful; if so, the method will retrieve the mobile operator and networkType and will set the boolean value of testOver to true
     * @param transferRateBitPerSeconds Needs to be checked, if its greater than 0
     */
    private void checkIfTestWasSuccessful(float transferRateBitPerSeconds) {
        if(transferRateBitPerSeconds > 0) {
            if(!downloadSpeed.equals("") && !uploadSpeed.equals("")) {
                operator = telephonyManager.getNetworkOperatorName();

                if(operator.equals("")) operator = lastOperator;
                else lastOperator = operator;

                //Get network type
                int networkType = telephonyManager.getNetworkType();
                this.networkType = getNetworkTypeAsString(networkType);

                testOver = true;
                Log.i(StartService.LOG_TAG, "Operator: " + operator + ", NetworkType: " + this.networkType + ", DownloadSpeed: " + downloadSpeed + ", UploadSpeed: " + uploadSpeed);
            }
        }
        else {
            testFailed = true;
            testOver = true;
            Log.i(StartService.LOG_TAG, "Test failed, because upload or download speed was 0");
        }
    }

    /**
     * Gets the name of network type as a string (e.g. GPRS or LTE)
     * @param networkType type as int (defined in TelephonyManager)
     * @return NetworkType as String
     */
    private String getNetworkTypeAsString(int networkType) {
        switch (networkType) {
            case TelephonyManager.NETWORK_TYPE_1xRTT: return "1xRTT";
            case TelephonyManager.NETWORK_TYPE_CDMA: return "CDMA";
            case TelephonyManager.NETWORK_TYPE_EDGE: return "EDGE";
            case TelephonyManager.NETWORK_TYPE_EHRPD: return "eHRPD";
            case TelephonyManager.NETWORK_TYPE_EVDO_0: return "EVDO rev. 0";
            case TelephonyManager.NETWORK_TYPE_EVDO_A: return "EVDO rev. A";
            case TelephonyManager.NETWORK_TYPE_EVDO_B: return "EVDO rev. B";
            case TelephonyManager.NETWORK_TYPE_GPRS: return "GPRS";
            case TelephonyManager.NETWORK_TYPE_HSDPA: return "HSDPA";
            case TelephonyManager.NETWORK_TYPE_HSPA: return "HSPA";
            case TelephonyManager.NETWORK_TYPE_HSPAP: return "HSPA+";
            case TelephonyManager.NETWORK_TYPE_HSUPA: return "HSUPA";
            case TelephonyManager.NETWORK_TYPE_IDEN: return "iDen";
            case TelephonyManager.NETWORK_TYPE_LTE: return "LTE";
            case TelephonyManager.NETWORK_TYPE_UMTS: return "UMTS";
            case TelephonyManager.NETWORK_TYPE_UNKNOWN: return "Unknown";
        }
        return "";
    }

    public boolean isTestFailed() {
        return testFailed;
    }

    public boolean isTestOver() {
        return testOver;
    }

    public String getUploadSpeed() {
        return uploadSpeed;
    }

    public String getDownloadSpeed() {
        return downloadSpeed;
    }

    public String getNetworkType() {
        return networkType;
    }

    public String getOperator() {
        return operator;
    }

    public JSONArray getDownloadSpeeds() { return downloadSpeeds; }

    public JSONArray getUploadSpeeds() { return uploadSpeeds; }
}