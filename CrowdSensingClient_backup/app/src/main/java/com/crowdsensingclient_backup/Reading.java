package com.crowdsensingclient_backup;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by raok on 11/11/16.
 */

public class Reading
{
    private double r1;
    private List<String> metaData;
    private float[] axisReading;
    private String rname;

    public void setAxisReading(float[] readings)
    {
        // Only takes the first three even if more are provided
        if(readings == null)
            System.out.println("set() called with null");
        int i = 0;
        axisReading = new float[3];
        for (float f: readings)
        {
            if(i < 3)
                axisReading[i++] = f;
        }
    }
    public float[] getaxisReadings()
    {
        if(axisReading != null)
            return axisReading;
        else
        {
            System.out.println("Axis reading not yet set");
            return null;
        }
    }
    public double getR1() {
        return r1;
    }

    public void setR1(double r1) {
        this.r1 = r1;
    }

    public List<String> getMetaData() {
        return metaData;
    }

    public void addMetaData(String newMetaData) {
        if(metaData == null)
        {
            metaData = new ArrayList<String>();
            metaData.add(newMetaData);
        }
        else
            metaData.add(newMetaData);
    }

    public String getRname() {
        return rname;
    }

    public void setRname(String rname) {
        this.rname = rname;
    }
}
