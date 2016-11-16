package edu.purdue.dcsl.crowdsensingclient;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
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

    public String getBatteryStatus(Context context)
    {
        String res = new String();
        Intent batteryIntent = context.registerReceiver(null,
                new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        int level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

        // Error checking that probably isn't needed but I added just in case.
        if(level == -1 || scale == -1) {
            res += 50.0f;
        }
        else
            res += ((float)level / (float)scale) * 100.0f;

        int status = batteryIntent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);

        boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL;

        if(isCharging == true){
           res +="+Charging";
        }else{
            res +="+NOT_CHARGING";
        }
        return res;
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
