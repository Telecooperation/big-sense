package de.tudarmstadt.tk.dbsystel.mobilespeedmeasurement;

/**
 * Created by Martin on 24.11.2015.
 */
public class Measurement {

    private int id;
    private long date;
    private double latitude;
    private double longitude;
    private String operator;
    private String networkType;
    private String downloadSpeed;
    private String uploadSpeed;
    private String downloadSpeeds;
    private String uploadSpeeds;

    public Measurement() {
    }

    public int getID() {
        return id;
    }

    public long getDate() {
        return date;
    }

    public void setID(int id) {
        this.id = id;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public String getNetworkType() {
        return networkType;
    }

    public void setNetworkType(String networkType) {
        this.networkType = networkType;
    }

    public String getUploadSpeed() {
        return uploadSpeed;
    }

    public void setUploadSpeed(String uploadSpeed) {
        this.uploadSpeed = uploadSpeed;
    }

    public String getDownloadSpeed() {
        return downloadSpeed;
    }

    public void setDownloadSpeed(String downloadSpeed) {
        this.downloadSpeed = downloadSpeed;
    }

    public String getUploadSpeeds() {
        return uploadSpeeds;
    }

    public void setUploadSpeeds(String uploadSpeeds) {
        this.uploadSpeeds = uploadSpeeds;
    }

    public String getDownloadSpeeds() {
        return downloadSpeeds;
    }

    public void setDownloadSpeeds(String downloadSpeeds) {
        this.downloadSpeeds = downloadSpeeds;
    }
}
