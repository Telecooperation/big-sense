package de.tudarmstadt.tk.dbsystel.accelerationdvs;

import android.content.Context;
import android.telephony.TelephonyManager;
import android.util.Log;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Date;
import java.util.Enumeration;
import java.util.ResourceBundle;

/**
 * Created by Martin on 11.12.2015.
 */
public class MQTTConnection {

    private String clientId;
    private int qos;
    private Context context;
    private MemoryPersistence persistence;

    private boolean connected;
    private MqttClient mqttClient;

    public MQTTConnection(Context context) {
        this.context = context;
        this.qos = 2;
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        this.clientId = telephonyManager.getDeviceId();
        this.persistence = new MemoryPersistence();
        this.connected = false;
    }

    /**
     * Publishes the given data via its given topic
     * @param linearAccelerationX
     * @param linearAccelerationY
     * @param linearAccelerationZ
     */
    public void sendValues(float linearAccelerationX, float linearAccelerationY, float linearAccelerationZ) {
        if(isConnected()) {
            String content = "(" + linearAccelerationX + "," + linearAccelerationY + "," + linearAccelerationZ + ")";
            MqttMessage message = new MqttMessage(content.getBytes());
            message.setQos(qos);
            try {
                mqttClient.publish(StartService.TOPIC, message);
            } catch (MqttException e) {
                Log.i(StartService.LOG_TAG, "Can't publish a message");
                e.printStackTrace();
            }
        }
    }

    /**
     * Tries to connect to the MQTT Broker
     */
    public void connect() {
        try {
            String brokerIP = getBrokerIP();
            ResourceBundle.getBundle("org.eclipse.paho.client.mqttv3.internal.nls.logcat");
            mqttClient = new MqttClient("tcp://" + brokerIP + ":" + StartService.BROKER_PORT, clientId, persistence);
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);
            Log.i(StartService.LOG_TAG, "Connecting to broker: " + "tcp://" + brokerIP + ":" + StartService.BROKER_PORT);
            mqttClient.connect(connOpts);
            Log.i(StartService.LOG_TAG, "Connected");
            connected = true;
        } catch(MqttException me) {
            Log.i(StartService.LOG_TAG, "Reason " + me.getReasonCode());
            Log.i(StartService.LOG_TAG, "msg " + me.getMessage());
            Log.i(StartService.LOG_TAG, "loc " + me.getLocalizedMessage());
            Log.i(StartService.LOG_TAG, "cause " + me.getCause());
            Log.i(StartService.LOG_TAG, "excep " + me);
            me.printStackTrace();
        }
        catch(Exception e) {
            Log.i(StartService.LOG_TAG, "There was a problem with connecting to mqtt broker");
            e.printStackTrace();
        }
    }

    /**
     * Gets the first ip of all nettwork connections which is no loopback-address
     * @return
     * @throws SocketException
     */
    private static InetAddress getOwnIP() throws SocketException {
        Enumeration<NetworkInterface> nis = NetworkInterface.getNetworkInterfaces();
        NetworkInterface ni;
        while (nis.hasMoreElements()) {
            ni = nis.nextElement();
            if (!ni.isLoopback()/*not loopback*/ && ni.isUp()/*it works now*/) {
                for (InterfaceAddress ia : ni.getInterfaceAddresses()) {
                    //filter for ipv4/ipv6
                    if (ia.getAddress().getAddress().length == 4 && ia.getAddress().toString().startsWith("/192")) {
                        //4 for ipv4, 16 for ipv6
                        return ia.getAddress();
                    }
                }
            }
        }
        return null;
    }

    String result = "";

    /**
     * Scans all devices in its own subnet
     * @return the IP of the first found device in local network
     */
    private String getBrokerIP() {
        try {
            // Get localhost
            InetAddress addr = getOwnIP();

            if (addr != null) {
                // Get IP Address
                String ownAddress = addr.toString();
                ownAddress = ownAddress.substring(ownAddress.indexOf("/") + 1);

                String subnetAddress = ownAddress.substring(0, ownAddress.lastIndexOf(".")) + ".";
                result = "";

                // Loop to scan each address on the local subnet
                for (int i = 2; i < 255; i++) {
                    final String pingIP = subnetAddress + i;
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Runtime runtime = Runtime.getRuntime();
                                Process mIpAddrProcess = runtime.exec("/system/bin/ping -c 1 " + pingIP);
                                int mExitValue = mIpAddrProcess.waitFor();
                                if(mExitValue==0){
                                    result = pingIP;
                                }
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();
                }

                //Check if a result is there and stop the ping after 60 seconds
                Date now = new Date();
                Date comparingTime = new Date();
                while(result == "" && (comparingTime.getTime() - now.getTime()) < 60000) {
                    Thread.sleep(100);
                    comparingTime = new Date();
                }
                return result;
            }
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "";
    }

    public boolean isConnected() {
        return this.connected;
    }
}
