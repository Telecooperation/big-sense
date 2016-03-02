package de.tudarmstadt.tk.dbsystel.mobilenetworkmeasurement;

/**
 * Created by Martin on 24.11.2015.
 */
public class Measurement {

    private int id;
    private long date;
    private double latitude;
    private double longitude;
    private int cellID;
    private int pci;
    private String operator;
    private String networkType;
    private int level;
    private int dbm;
    private int asu;

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

    public int getAsu() {
        return asu;
    }

    public void setAsu(int asu) {
        this.asu = asu;
    }

    public int getCellID() {
        return cellID;
    }

    public void setCellID(int cellID) {
        this.cellID = cellID;
    }

    public int getPci() {
        return pci;
    }

    public void setPci(int pci) {
        this.pci = pci;
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

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getDbm() {
        return dbm;
    }

    public void setDbm(int dbm) {
        this.dbm = dbm;
    }
}
