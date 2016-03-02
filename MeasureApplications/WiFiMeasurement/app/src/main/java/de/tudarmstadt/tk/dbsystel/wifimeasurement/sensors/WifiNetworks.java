package de.tudarmstadt.tk.dbsystel.wifimeasurement.sensors;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import de.tudarmstadt.tk.dbsystel.wifimeasurement.Measurement;
import de.tudarmstadt.tk.dbsystel.wifimeasurement.StartService;

/**
 * Created by Martin on 24.11.2015.
 */
public class WifiNetworks {

    private Context context;

    private WifiManager wifiManager;

    private List<Measurement> networks;

    public WifiNetworks(Context context) {
        this.context = context;

        wifiManager = (WifiManager) this.context.getSystemService(Context.WIFI_SERVICE);

        boolean isWifiOn = false;
        boolean isHotspotOn = false;

        //First start wifi in right mode
        Method[] methods = wifiManager.getClass().getDeclaredMethods();
        for (Method m: methods) {
            if (m.getName().equals("getWifiApConfiguration")) {
                try {
                    Method method = wifiManager.getClass().getDeclaredMethod("getWifiApState");
                    method.setAccessible(true);
                    int hotspotState = (Integer) method.invoke(wifiManager, (Object[]) null);
                    isHotspotOn = hotspotState == 13;
                    isWifiOn = wifiManager.isWifiEnabled();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }

        if(isHotspotOn) {
            for(Method method: methods) {
                if (method.getName().equals("setWifiApEnabled")) {
                    try {
                        Log.i(StartService.LOG_TAG, "First turn off Hotspot");
                        method.invoke(wifiManager, null, false);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        if(!isWifiOn) {
            Log.i(StartService.LOG_TAG, "Turn on WiFi");
            wifiManager.setWifiEnabled(true);
        }
    }

    /**
     * Collects all important parameters from all available wifi networks
     * @return true, if at least one network was found
     */
    public boolean getLastValuesForMeasurement() {
        networks = new ArrayList<>();
        List<ScanResult> scanResults = wifiManager.getScanResults();
        for(ScanResult network : scanResults) {
            Measurement temp = new Measurement();
            temp.setBssid(network.BSSID);
            temp.setSsid(network.SSID);
            temp.setCapabilities(network.capabilities);
            temp.setFrequency(network.frequency);
            temp.setLevel(network.level);
            networks.add(temp);
        }

        return networks.size() > 0;
    }

    public List<Measurement> getMeasurements() {
        return this.networks;
    }
}
