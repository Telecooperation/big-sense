package de.tudarmstadt.tk.dbsystel.accelerationdvs;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by Martin on 11.12.2015.
 */
public class Hotspot {

    private Context context;
    private boolean open;

    public Hotspot(Context context) {
        this.context = context;
        this.open = false;
    }

    /**
     * Starts a hotspot with a defined ssid/password and also defined ip
     */
    public void openHotspot() {
        //First look if the hotspot isn't already opened with this configuration
        WifiManager wifiManager = (WifiManager) this.context.getSystemService(Context.WIFI_SERVICE);
        Method[] methods = wifiManager.getClass().getDeclaredMethods();
        for (Method m: methods) {
            if (m.getName().equals("getWifiApConfiguration")) {
                try {
                    WifiConfiguration config = (WifiConfiguration)m.invoke(wifiManager);
                    Method method = wifiManager.getClass().getDeclaredMethod("getWifiApState");
                    method.setAccessible(true);
                    int hotspotState = (Integer) method.invoke(wifiManager, (Object[]) null);
                    if(hotspotState == 13 &&
                            config.SSID.equals(StartService.WLAN_SSID) &&
                            config.preSharedKey.equals(StartService.WLAN_PASSWORD)) {
                        Log.i(StartService.LOG_TAG, "Hotspot with same configuration already started");
                        open = true;
                        return;
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                }
            }
        }

        //Now close the actual hotspot/wifi connection and start a new one
        if(wifiManager.isWifiEnabled())
        {
            wifiManager.setWifiEnabled(false);
        }
        Method[] wmMethods = wifiManager.getClass().getDeclaredMethods();
        boolean methodFound = false;
        for(Method method: wmMethods){
            if(method.getName().equals("setWifiApEnabled")){
                methodFound = true;
                WifiConfiguration netConfig = new WifiConfiguration();
                netConfig.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
                netConfig.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
                netConfig.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
                netConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
                netConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
                netConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
                netConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
                netConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
                netConfig.SSID = StartService.WLAN_SSID;
                netConfig.preSharedKey = StartService.WLAN_PASSWORD;

                try {
                    boolean apstatus = (Boolean) method.invoke(wifiManager, netConfig,true);
                    Log.i(StartService.LOG_TAG, "Creating a WiFi Network \"" + netConfig.SSID + "\" with password \"" + netConfig.preSharedKey + "\"");
                    for (Method isWifiApEnabledmethod: wmMethods)
                    {
                        if(isWifiApEnabledmethod.getName().equals("isWifiApEnabled")){
                            while(!(Boolean)isWifiApEnabledmethod.invoke(wifiManager)){
                            }
                            for(Method method1: wmMethods){
                                if(method1.getName().equals("getWifiApState")){
                                }
                            }
                        }
                    }
                    if(apstatus) {
                        open = true;
                        Log.i(StartService.LOG_TAG, "Successfully started Hotspot");
                    }
                    else Log.i(StartService.LOG_TAG, "Failure while creating Hotspot");
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        }
        if(!methodFound){
            Log.i(StartService.LOG_TAG, "Phone has not the ability to change wifi state");
        }
    }

    public boolean isOpen() {
        return open;
    }
}
