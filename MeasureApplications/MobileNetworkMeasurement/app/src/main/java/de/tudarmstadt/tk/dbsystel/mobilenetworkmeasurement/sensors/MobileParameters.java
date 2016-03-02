package de.tudarmstadt.tk.dbsystel.mobilenetworkmeasurement.sensors;

import android.content.Context;
import android.telephony.CellIdentityLte;
import android.telephony.CellInfo;
import android.telephony.CellInfoLte;
import android.telephony.CellSignalStrengthLte;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;

import de.tudarmstadt.tk.dbsystel.mobilenetworkmeasurement.StartService;

/**
 * Created by Martin on 24.11.2015.
 */
public class MobileParameters {

    private Context context;

    private TelephonyManager telephonyManager;
    private SignalStrength listenedSignalStrength;

    private int cellID;
    private int pci;
    private String operator;
    private String networkType;
    private int level;
    private int dbm;
    private int asu;

    private String lastOperator;

    public MobileParameters(Context context) {
        this.context = context;

        telephonyManager = (TelephonyManager) this.context.getSystemService(Context.TELEPHONY_SERVICE);

        telephonyManager.listen(new PhoneStateListener() {
            @Override
            public void onSignalStrengthsChanged(SignalStrength signalStrength) {
                super.onSignalStrengthsChanged(signalStrength);
                listenedSignalStrength = signalStrength;
            }
        }, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
    }

    /**
     * Collects all important parameters for gsm and lte data
     * @return true, if all parameters (except pci) are set
     */
    public boolean getLastValuesForMeasurement() {
        cellID = -1;
        pci = -1;
        lastOperator = operator;
        operator = "";
        this.networkType = "";
        level = -1;
        dbm = -1;
        asu = -1;

        operator = telephonyManager.getNetworkOperatorName();

        //Get network type
        int networkType = telephonyManager.getNetworkType();
        this.networkType = getNetworkTypeAsString(networkType);

        //This only works with lte on some models (like Nexus devices, not on Samsung phones)
        if (telephonyManager.getAllCellInfo() != null) {
            for (CellInfo info : telephonyManager.getAllCellInfo()) {
                if (info instanceof CellInfoLte && info.isRegistered()) {
                    CellIdentityLte lteCellIdentity = ((CellInfoLte) info).getCellIdentity();
                    CellSignalStrengthLte lteSignalStrength = ((CellInfoLte) info).getCellSignalStrength();
                    level = lteSignalStrength.getLevel();
                    dbm = lteSignalStrength.getDbm();
                    asu = lteSignalStrength.getAsuLevel();

                    cellID = lteCellIdentity.getCi();
                    pci = lteCellIdentity.getPci();
                }
            }
        }
        //no info was given in the above request, then try another one
        //these infos are valid (normal) only for gsm, but on Samsung phones these values are also correct for lte
        if (cellID == -1 || dbm == -1 || asu == -1 || level == -1) {
            GsmCellLocation cellLocation = (GsmCellLocation) telephonyManager.getCellLocation();
            if (cellLocation != null) {
                cellID = cellLocation.getCid();
            }

            if (listenedSignalStrength != null) {
                asu = listenedSignalStrength.getGsmSignalStrength();
                level = listenedSignalStrength.getLevel();
                if (listenedSignalStrength.getGsmSignalStrength() != 99)
                    dbm = listenedSignalStrength.getGsmSignalStrength() * 2 - 113;
                else
                    dbm = listenedSignalStrength.getGsmSignalStrength();
            }
        }

        Log.i(StartService.LOG_TAG, "Operator: " + operator + ", NetworkType: " + this.networkType + ", CellID: " + cellID + ", DBM: " + dbm + ", ASU: " + asu);

        if (dbm != -1 && asu != -1) {
            if(cellID == 0) cellID = -1;
            if(operator.equals("")) operator = lastOperator;
            else lastOperator = operator;
            return true;
        } else return false;
    }

    /**
     * Gets the name of network type as a string (e.g. GPRS or LTE)
     * @param networkType type as int (defined in TelephonyManager)
     * @return NetworkType as String
     */
    private String getNetworkTypeAsString(int networkType) {
        switch (networkType) {
            case TelephonyManager.NETWORK_TYPE_1xRTT: return "1xRTT";
            case TelephonyManager.NETWORK_TYPE_CDMA: return "CDMA";
            case TelephonyManager.NETWORK_TYPE_EDGE: return "EDGE";
            case TelephonyManager.NETWORK_TYPE_EHRPD: return "eHRPD";
            case TelephonyManager.NETWORK_TYPE_EVDO_0: return "EVDO rev. 0";
            case TelephonyManager.NETWORK_TYPE_EVDO_A: return "EVDO rev. A";
            case TelephonyManager.NETWORK_TYPE_EVDO_B: return "EVDO rev. B";
            case TelephonyManager.NETWORK_TYPE_GPRS: return "GPRS";
            case TelephonyManager.NETWORK_TYPE_HSDPA: return "HSDPA";
            case TelephonyManager.NETWORK_TYPE_HSPA: return "HSPA";
            case TelephonyManager.NETWORK_TYPE_HSPAP: return "HSPA+";
            case TelephonyManager.NETWORK_TYPE_HSUPA: return "HSUPA";
            case TelephonyManager.NETWORK_TYPE_IDEN: return "iDen";
            case TelephonyManager.NETWORK_TYPE_LTE: return "LTE";
            case TelephonyManager.NETWORK_TYPE_UMTS: return "UMTS";
            case TelephonyManager.NETWORK_TYPE_UNKNOWN: return "Unknown";
        }
        return "";
    }

    public int getAsu() {
        return asu;
    }

    public int getDbm() {
        return dbm;
    }

    public int getLevel() {
        return level;
    }

    public String getNetworkType() {
        return networkType;
    }

    public String getOperator() {
        return operator;
    }

    public int getPci() {
        return pci;
    }

    public int getCellID() {
        return cellID;
    }
}
