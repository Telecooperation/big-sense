package de.orolle.bigsense.update;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.FutureTask;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.telephony.TelephonyManager;
import android.util.Log;

import de.orolle.bigsense.util.StreamPumper;

/**
 * UpdateService connects to BigSenseWeb backend which
 * starts the update process. 
 * The service connects to the backend server and than to the local ssh
 * server. The data between the 2 connections is transfered from one to
 * the other. 
 * 
 * @author Oliver Rolle
 *
 */
public class UpdateService extends Service {
	/**
	 * prevents android from sleep
	 */
	private WakeLock mWakeLock;

	/**
	 * Schedule update retry
	 */
	private final Timer timer = new Timer();

	/**
	 * Update retry interval.
	 */
	private long INTERVAL = 3600000; // every hour

	/**
	 * Remote SSH host and port
	 */
	public static final String SSH_HOST = "cooldomain.com";
	public static final int SSH_PORT = 33822;

	private Process rootProcess;

	private static final String LOGTAG = "BigSense";

	@SuppressLint({ "Wakelock", "WorldWriteableFiles" })
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "UpdateService");
		mWakeLock.acquire();
		
		//Store imei in file, so that the server can read it
		TelephonyManager TelephonyMgr = (TelephonyManager)getSystemService(TELEPHONY_SERVICE);
		String imei = TelephonyMgr.getDeviceId();
		
		String path = getExternalFilesDir(null).getAbsolutePath();
		FileOutputStream outputStream;
		try {
		  outputStream = new FileOutputStream(new File(path + "/imei"));
		  outputStream.write(imei.getBytes());
		  outputStream.close();
		} catch (Exception e) {
		  e.printStackTrace();
		}

		/*
		 * Run update every hour
		 */
		timer.scheduleAtFixedRate(new TimerTask() {
			Socket remoteTunnel, localSSH;
			StreamPumper p0, p1;
			Thread t0, t1;

			@Override
			public void run() {
				Log.d(LOGTAG,"Run update, Restarting of SSH-Server");
	            try {
					rootProcess = Runtime.getRuntime().exec("su");
		            DataOutputStream os = new DataOutputStream(rootProcess.getOutputStream());
		            os.writeBytes("am force-stop com.icecoldapps.sshserver\n");
					os.writeBytes("am start \"com.icecoldapps.sshserver/.viewStart\" &\n");
					os.writeBytes("exit\n");
		            os.flush();
		            Thread.sleep(5000);
				} catch (IOException e) {
					e.printStackTrace();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				//lastSshRestart = System.currentTimeMillis();

				if(remoteTunnel != null) {
					if(remoteTunnel.isClosed()) {
						stop();
					}
				}

				if(localSSH != null) {
					if(localSSH.isClosed()) {
						stop();
					}
				}

				if(p0 != null && p1 != null) {
					if(!p0.isFinished() && !p1.isFinished()) {
						return;
					} else {
						stop();
					}
				}

				try {
					remoteTunnel = new Socket(SSH_HOST, SSH_PORT);

					while(!remoteTunnel.isConnected()) {
						Thread.sleep(100);
					}

					localSSH = new Socket("127.0.0.1", 33822);
					
					/*
					 * Pump data from one to the other connection
					 */
					p0 = new StreamPumper(localSSH.getInputStream(), remoteTunnel.getOutputStream());
					p1 = new StreamPumper(remoteTunnel.getInputStream(), localSSH.getOutputStream());

					FutureTask<String> task0 = new FutureTask(p0);
					FutureTask<String> task1 = new FutureTask(p1);

					t1 = new Thread(task0);
					t0 = new Thread(task1);

					t0.start();
					t1.start();

					Log.i(LOGTAG, "!!!SSH Tunnel started!!!");
					task1.get(); //This one is necessary, because it blocks till the ssh connection is over
					//stop ssh server
					try {
						rootProcess = Runtime.getRuntime().exec("su");
						DataOutputStream os = new DataOutputStream(rootProcess.getOutputStream());
						os.writeBytes("am force-stop com.icecoldapps.sshserver\n");
						os.writeBytes("exit\n");
						os.flush();
						Log.i(LOGTAG, "!!!SSH Tunnel stopped!!!");
					} catch (IOException e) {
						e.printStackTrace();
					}
					stop();
				}catch(Exception e) {
					e.printStackTrace();
					stop();
				}
			}

			private void stop() {
				try {
					remoteTunnel.close();
				} catch (Exception e) {}

				try {
					localSSH.close();
				} catch (Exception e) {}

				try {
					t0.stop();
				} catch (Exception e) {}

				try {
					t1.stop();
				} catch (Exception e) {}

				remoteTunnel = localSSH = null;
				t0 = t1 = null;
				p0 = p1 = null;
			}
		}, 1000, INTERVAL);
		return flags;
	}

	@Override
	public void onDestroy() {
		timer.cancel();
		super.onDestroy();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

}
