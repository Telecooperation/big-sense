package de.tudarmstadt.tk.dbsystel.mobilespeedmeasurement.sensors;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import de.tudarmstadt.tk.dbsystel.mobilespeedmeasurement.StartService;

public class PositionManager {

    private Context context;
    private Location lastKnownLocation;

    public PositionManager(Context context) {
        this.context = context;

        LocationManager locationManager = (LocationManager) this.context.getSystemService(Context.LOCATION_SERVICE);

        LocationListener locationListener = new LocationListener() {

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            @Override
            public void onProviderEnabled(String provider) {
            }

            @Override
            public void onProviderDisabled(String provider) {
            }

            @Override
            public void onLocationChanged(Location location) {
                if(location.getProvider().equals(LocationManager.GPS_PROVIDER)) {
                    lastKnownLocation = location;
                    Log.i(StartService.LOG_TAG, "New Location (GPS_PROVIDER)");
                }
                else if(lastKnownLocation == null || (lastKnownLocation != null && (location.getTime() - lastKnownLocation.getTime()) > 2000 * StartService.MIN_GPS_UPDATE_INTERVAL_SEC)) {
                    lastKnownLocation = location;
                    Log.i(StartService.LOG_TAG, "New Location (NETWORK_PROVIDER)");
                }
            }
        };
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, StartService.MIN_GPS_UPDATE_INTERVAL_SEC * 1000, StartService.MIN_GPS_UPDATE_DISTANCE_METER, locationListener);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, StartService.MIN_GPS_UPDATE_INTERVAL_SEC * 1000, StartService.MIN_GPS_UPDATE_DISTANCE_METER*5, locationListener);

        Criteria criteria = new Criteria();
        String bestProvider = locationManager.getBestProvider(criteria, false);
        Location location = locationManager.getLastKnownLocation(bestProvider);

        if (location != null && (System.currentTimeMillis() - location.getTime()) < 300000)
            lastKnownLocation = location;
    }

    public Location getLastKnownLocation() {
        return this.lastKnownLocation;
    }
}