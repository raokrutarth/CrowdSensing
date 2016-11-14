package edu.purdue.dcsl.crowdsensingclient;

import java.util.Random;


public class ControlInfoReader
{
//            "Baro_avail": "08:32:23/10:22:43/12:33:54/14:22:54",
//            "Acce_avail": "08:32:23/10:22:43/12:33:54/14:22:54",
//            "GPS_avail": "08:32:23/10:22:43/12:33:54/14:22:54",
//            "Gyro_avail": "08:32:23/10:22:43/12:33:54/14:22:54",
//            "Current_battery": "23%",
//            "Signal_strength": "2G/3G/4G/LTE",
//            "IMEI": "AA BBBBBB CCCCCC D",
//            "Mem_usage": "34%",
//            "Clk_rate": "23GHz",
//            "CPU_uti": "34%"

    public double batteryP()
    {
        float minX = 1.0f;
        float maxX = 100.0f;
        Random rand = new Random();

        return rand.nextDouble() * (maxX - minX) + minX;
    }
    public String getImei()
    {
        return "XXX-YYY-ZZZ";

    }
    public String getSignalStrength()
    {
        return "4G-LTE";
    }

}
