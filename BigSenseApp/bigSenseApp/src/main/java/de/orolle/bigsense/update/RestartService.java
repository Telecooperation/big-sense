package de.orolle.bigsense.update;

import java.io.DataOutputStream;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.BatteryManager;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

public class RestartService extends Service {
    /**
     * prevents android from sleep
     */
    private WakeLock mWakeLock;

    private final Timer checkInetTimer = new Timer();

    private Process rootProcess;
    private long lastOnlineTimestamp;

    private static final String LOGTAG = "BigSense Restarter";

    @SuppressLint({ "Wakelock", "WorldWriteableFiles" })
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Notification notification = new NotificationCompat.Builder(this).build();
        startForeground(1338, notification);

        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "RestartService");
        mWakeLock.acquire();


        //First check if the device is connected to the internet (after start from empty battery some devices does not connect)
        try {
            Thread.sleep(10000);
            if (!checkOnline()) {
                Log.i("Device Offline", "Reboot to get new connection");
                rootProcess = Runtime.getRuntime().exec("su");
                DataOutputStream os = new DataOutputStream(rootProcess.getOutputStream());
                os.writeBytes("reboot\n");
                os.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        Date now = new Date();
        lastOnlineTimestamp = now.getTime();
		/*
		 * Check inet every half hour and restart phone if no connection is available for over 3 hours
		 */
        checkInetTimer.scheduleAtFixedRate(new TimerTask() {

            @Override
            public void run() {
                try {
                    checkOnline();

                    Date now = new Date();
                    if ((now.getTime() - lastOnlineTimestamp) / 1000 > 10800) {
                        Log.i(LOGTAG, "Reboot to get new connection");
                        rootProcess = Runtime.getRuntime().exec("su");
                        DataOutputStream os = new DataOutputStream(rootProcess.getOutputStream());
                        os.writeBytes("reboot\n");
                        os.close();
                    }

                    //Shutdown phone, if its not in loading state and battery is less than 50%
                    Intent batteryIntent = registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
                    int level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                    int isPlugged = batteryIntent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
                    if (level != -1 && level < 50 && !(isPlugged == BatteryManager.BATTERY_PLUGGED_USB || isPlugged == BatteryManager.BATTERY_PLUGGED_AC)) {
                        Log.i(LOGTAG, "Shutdown phone");
                        rootProcess = Runtime.getRuntime().exec("su");
                        DataOutputStream os = new DataOutputStream(rootProcess.getOutputStream());
                        os.writeBytes("reboot -p\n");
                        os.close();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, UpdateService.INTERVAL / 2, UpdateService.INTERVAL);
        return flags;
    }

    @Override
    public void onDestroy() {
        checkInetTimer.cancel();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * Looks first if the device is connected to a mobile network and if so pings google
     * @return
     */
    private Boolean checkOnline()	{
        Log.i(LOGTAG, "Checks Inet connection");
        ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        if(ni != null && ni.isConnected()) {
            try {
                Process p1 = java.lang.Runtime.getRuntime().exec("ping -c 1 www.google.com");
                int returnVal = p1.waitFor();
                boolean reachable = (returnVal==0);
                if(reachable) {
                    Date now = new Date();
                    lastOnlineTimestamp = now.getTime();
                    return true;
                }
                else return false;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        }
        return false;
    }
}
