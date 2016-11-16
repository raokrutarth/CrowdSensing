package edu.purdue.dcsl.crowdsensingclient;

import java.util.Random;

/**
 * Created by raok on 11/11/16.
 */

public class SensorReader
{
    // light reault;
    public float[] getSensorX()
    {
        // append timestamp.now() in control log file

        float minX = 10.0f;
        float maxX = 100.0f;

        Random rand = new Random();
        float[] res = new float[3];
        for(int i = 0; i < 3; i++)
            res[i] = rand.nextFloat() * (maxX - minX) + minX;
        return res;
    }
    public float[] getSensorY()
    {
        // append timestamp.now() in control log file

        float minX = 10.0f;
        float maxX = 100.0f;

        Random rand = new Random();
        float[] res = new float[3];
        for(int i = 0; i < 3; i++)
            res[i] = rand.nextFloat() * (maxX - minX) + minX;
        return res;
    }
    public float[] getGyro()
    {
        return null;
    }
    public float[] getBaro()
    {
        return null;
    }
    public float[] getAccl()
    {
        return null;
    }
    public float[] getGPS()
    {
        return null;
    }


}
