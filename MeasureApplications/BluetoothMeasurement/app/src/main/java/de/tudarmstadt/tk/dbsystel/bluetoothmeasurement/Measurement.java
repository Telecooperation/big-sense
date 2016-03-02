package de.tudarmstadt.tk.dbsystel.bluetoothmeasurement;

/**
 * Created by Martin on 24.11.2015.
 */
public class Measurement {

    private int id;
    private long date;
    private double latitude;
    private double longitude;

    private String name;
    private String address;
    private String type;

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
