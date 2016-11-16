package edu.purdue.dcsl.crowdsensingclient;

import java.util.Random;

import android.content.Context;
import android.hardware.SensorManager;
import android.telephony.TelephonyManager;
import android.text.method.HideReturnsTransformationMethod;

import static android.telephony.TelephonyManager.NETWORK_TYPE_CDMA;
import static android.telephony.TelephonyManager.NETWORK_TYPE_EDGE;
import static android.telephony.TelephonyManager.NETWORK_TYPE_HSPA;
import static android.telephony.TelephonyManager.NETWORK_TYPE_LTE;
import static android.telephony.TelephonyManager.NETWORK_TYPE_GPRS;
import static android.telephony.TelephonyManager.NETWORK_TYPE_UMTS;

public class ControlInfoReader
{
//    "Baro_avail": "08:32:23/10:22:43/12:33:54/14:22:54",
//            "Acce_avail": "08:32:23/10:22:43/12:33:54/14:22:54",
//            "GPS_avail": "08:32:23/10:22:43/12:33:54/14:22:54",
//            "Gyro_avail": "08:32:23/10:22:43/12:33:54/14:22:54",
//            "Current_battery": "23%",
//            "Signal_strength": "2G/3G/4G/LTE",
//            "IMEI": "AA BBBBBB CCCCCC D",
//            "Mem_usage": "34%",
//            "Clk_rate": "23GHz",
//            "CPU_uti": "34%"
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
            return "4G";
        }
        else if (currentNetwork == NETWORK_TYPE_GPRS)
        {
            return "2G";
        }
        else if (currentNetwork == NETWORK_TYPE_CDMA ||currentNetwork == NETWORK_TYPE_HSPA || currentNetwork == NETWORK_TYPE_EDGE || currentNetwork == NETWORK_TYPE_UMTS)
        {
            return "3G";
        }
        else{
        return "Unknown";
        }
    }

}
