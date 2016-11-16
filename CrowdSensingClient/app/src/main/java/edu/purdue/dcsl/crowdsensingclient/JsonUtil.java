package edu.purdue.dcsl.crowdsensingclient;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by raok on 11/11/16.
 */

public class JsonUtil
{
    public static JSONObject toJson(Reading reading)
    {
        try
        {
            JSONObject jsonObj = new JSONObject();
            jsonObj.put("Sensor", reading.getRname());
            float[] axisRs = reading.getaxisReadings();
            double r1 = reading.getR1();

            if(r1 != 0)
                jsonObj.put("R1", Double.toString(r1) );

            if(axisRs != null)
            {
                JSONArray jArr = new JSONArray();
                for(int i = 0; i < 3; i++)
                {
                    JSONObject pnObj = new JSONObject();
                    pnObj.put("" + (char)('x'+i) , axisRs[i] );
                    jArr.put(pnObj);
                }
                jsonObj.put("Axis-Reading", jArr);
            }
            
            ArrayList<String> md = (ArrayList<String>)reading.getMetaData();
            if(md != null)
            {
                JSONArray jsonArr = new JSONArray();
                int j = 0;
                for (String md_e : md )
                {
                    JSONObject pnObj = new JSONObject();
                    pnObj.put("meta" + j++, md_e.toLowerCase() );
                    jsonArr.put(pnObj);
                }
                jsonObj.put("MetaData", jsonArr);
            }
            return jsonObj;
        }
        catch(Exception ex)
        {
            System.out.println("JSON exception occurred");
            ex.printStackTrace();
        }
        return null;
    }
}
