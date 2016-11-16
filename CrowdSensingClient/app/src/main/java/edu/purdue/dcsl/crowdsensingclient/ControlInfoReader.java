package edu.purdue.dcsl.crowdsensingclient;

import android.content.Context;
import android.telephony.TelephonyManager;

import java.util.Random;

import static android.telephony.TelephonyManager.NETWORK_TYPE_CDMA;
import static android.telephony.TelephonyManager.NETWORK_TYPE_EDGE;
import static android.telephony.TelephonyManager.NETWORK_TYPE_HSPA;
import static android.telephony.TelephonyManager.NETWORK_TYPE_LTE;
import static android.telephony.TelephonyManager.NETWORK_TYPE_GPRS;
import static android.telephony.TelephonyManager.NETWORK_TYPE_UMTS;


public class ControlInfoReader
{

    public Context context;
    public TelephonyManager Tmgr;
    public ControlInfoReader(Context context)
    {
        this.context = context;
        Tmgr = (TelephonyManager) context.getSystemService(context.TELEPHONY_SERVICE);
    }

    public float batteryP()
    {
        float minX = 10.0f;
        float maxX = 100.0f;
        Random rand = new Random();

        return rand.nextFloat() * (maxX - minX) + minX;
    }
    public String getImei()
    {

        String IMEI = Tmgr.getDeviceId();
        return IMEI;

    }
    public String getSignalStrength()
    {
        int currentNetwork = Tmgr.getNetworkType();
        if (currentNetwork == NETWORK_TYPE_LTE)
        {
            return "4G-LTE";
        }
        else if (currentNetwork == NETWORK_TYPE_GPRS)
        {
            return "2G";
        }
        else if (currentNetwork == NETWORK_TYPE_CDMA ||
                currentNetwork == NETWORK_TYPE_HSPA ||
                currentNetwork == NETWORK_TYPE_EDGE ||
                currentNetwork == NETWORK_TYPE_UMTS)
        {
            return "3G";
        }
        else
        {
            return "Unknown";
        }
    }
}
