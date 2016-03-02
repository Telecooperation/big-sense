package de.tudarmstadt.tk.dbsystel.acceleration;

/**
 * Created by Martin on 24.11.2015.
 */
public class Measurement {

    private int id;
    private long date;
    private double latitude;
    private double longitude;
    private float accX;
    private float accY;
    private float accZ;

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

    public float getAccX() {
        return accX;
    }

    public void setAccX(float accX) {
        this.accX = accX;
    }

    public float getAccY() {
        return accY;
    }

    public void setAccY(float accY) {
        this.accY = accY;
    }

    public float getAccZ() {
        return accZ;
    }

    public void setAccZ(float accZ) {
        this.accZ = accZ;
    }
}
