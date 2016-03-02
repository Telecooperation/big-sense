package de.tudarmstadt.tk.dbsystel.wifimeasurement;

/**
 * Created by Martin on 24.11.2015.
 */
public class Measurement {

    private int id;
    private long date;
    private double latitude;
    private double longitude;

    private String bssid;
    private String ssid;
    private String capabilities;
    private int frequency;
    private int level;

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

    public String getBssid() {
        return bssid;
    }

    public void setBssid(String bssid) {
        this.bssid = bssid;
    }

    public String getSsid() {
        return ssid;
    }

    public void setSsid(String ssid) {
        this.ssid = ssid;
    }

    public String getCapabilities() {
        return capabilities;
    }

    public void setCapabilities(String capabilities) {
        this.capabilities = capabilities;
    }

    public int getFrequency() {
        return frequency;
    }

    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }
}
