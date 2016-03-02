package de.tudarmstadt.tk.dbsystel.bluetoothmeasurement.sensors;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.ParcelUuid;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import de.tudarmstadt.tk.dbsystel.bluetoothmeasurement.Measurement;
import de.tudarmstadt.tk.dbsystel.bluetoothmeasurement.StartService;

/**
 * Created by Martin on 24.11.2015.
 */
public class Bluetooth {

    private Context context;
    private BluetoothAdapter mBluetoothAdapter;
    private List<Measurement> devices;

    // Create a BroadcastReceiver for ACTION_FOUND
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                Measurement temp = new Measurement();
                temp.setName(device.getName());
                temp.setAddress((device.getAddress()));
                int type = device.getType();
                switch(type) {
                    case BluetoothDevice.DEVICE_TYPE_UNKNOWN:
                        temp.setType("Unknown");
                        break;
                    case BluetoothDevice.DEVICE_TYPE_CLASSIC:
                        temp.setType("Classic");
                        break;
                    case BluetoothDevice.DEVICE_TYPE_LE:
                        temp.setType("LowEnergy");
                        break;
                    case BluetoothDevice.DEVICE_TYPE_DUAL:
                        temp.setType("Dual");
                        break;
                }
                devices.add(temp);

                Log.i(StartService.LOG_TAG, "Found Bluetooth Device with name: " + device.getName());
            }
        }
    };

    public Bluetooth(Context context) {
        this.context = context;

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Log.i(StartService.LOG_TAG, "Check for Bluetooth failed");
        }
        else {
            if (!mBluetoothAdapter.isEnabled()) {
                mBluetoothAdapter.enable();
            }
        }

        devices = new ArrayList<>();

        // Register the BroadcastReceiver
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        context.registerReceiver(mReceiver, filter);
    }

    /**
     * Says only if there are values, the measurement itself happens ascny
     * @return true, if at least one network was found
     */
    public boolean getLastValuesForMeasurement() {
        mBluetoothAdapter.startDiscovery();

        //Wait 15 seconds to discover all possible devices
        try {
            Thread.sleep(15000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return devices.size() > 0;
    }

    public List<Measurement> getMeasurements() {
        List<Measurement> out = devices;
        devices = new ArrayList<>();
        return out;
    }
}
