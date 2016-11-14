package edu.purdue.dcsl.crowdsensingclient;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
{
    public final static String Jmsg = "edu.purdue.dcsl.crowdsensingclient.JSON_RESULT";
    public final static String CONTROL_LOG = "edu.purdue.dcsl.crowdsensingclient.ControllogFile";
    public static File SDCARD = Environment.getExternalStorageDirectory();
    private static String task_json;
    private static final String CS_SERVER = "35.160.36.179";
    private static final int SERVER_PORT = 21567;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // setup control info logging for every half hour
        Intent controlLoggerIntent = new Intent(getActivity(), ControlLogger.class);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(getBaseContext(), 0, controlLoggerIntent, 0);
        AlarmManager alarmMgr = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
        alarmMgr.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                AlarmManager.INTERVAL_FIFTEEN_MINUTES,
                AlarmManager.INTERVAL_FIFTEEN_MINUTES,
                alarmIntent);
    }
    private boolean processTask(String tskJson)
    {
        System.out.println("Task JSON received: ");
        System.out.println(tskJson);

        // call the relevant sensors here

        return true;
    }

    public static JSONObject getSensorJson(String sensorName)
    {
        SensorReader sr = new SensorReader();
        float[] controlReadings;
        if( sensorName.equals("Gyro") )
            controlReadings = sr.getSensorY();
        else
            controlReadings = sr.getSensorX();

        Reading rd = new Reading();
        rd.setRname(sensorName);
        rd.setAxisReading(controlReadings);
        return JsonUtil.toJson(rd);
    }
    public JSONObject getControlJson()
    {
        JSONObject controlJson = new JSONObject();
        int entry_n = 1;
        try
        {
            File file = new File(SDCARD, CONTROL_LOG);
            StringBuilder text = new StringBuilder();
            ArrayList<String> logFile = new ArrayList<String>();
            try
            {
                BufferedReader br = new BufferedReader(new FileReader(file));
                String line;
                while ((line = br.readLine()) != null)
                    logFile.add(line);
                br.close();
            }
            catch (IOException e)
            {
                System.out.println("Unable to read file in getControlJson()");
            }

            for(String entry : logFile)
            {
                JSONObject controlEntry = new JSONObject();
                if(entry != null)
                {
                    String[] controlReadings = entry.split(",");
                    System.out.println("# of control readings: " + controlReadings.length);
                    for(int i = 0; i < controlReadings.length; ++i )
                    {
                        switch(i)
                        {
                            case 0:
                                controlEntry.put("Battery", controlReadings[i]);
                                break;
                            case 1:
                                controlEntry.put("IMIE", controlReadings[i]);
                                break;
                            case 2:
                                controlEntry.put("SignalStrength", controlReadings[i]);
                                break;
                        }
                        if(i > 2)
                            System.out.println("Extra control readings detected");
                    }
                    controlJson.put("Entry" + entry_n++, controlEntry);
                }
            }
            // Clear the Control log file
            File f = new File(SDCARD, CONTROL_LOG);
            if(f.exists())
                f.delete();

            return controlJson;
        }
        catch (Exception e)
        {
            System.out.println("Reading from log file failed");
            System.out.println(e.getMessage() );
            return null;
        }
    }
    public void SyncServer(View view)
    {
        try
        {

            TextView tv = (TextView)findViewById(R.id.statusBox);
            tv.setText("Sending Readings & Control Info...");
            tv.append("\n");

            JSONObject controlJson = getControlJson();
            JSONArray sensorArr = new JSONArray();
            for(int i = 10; i > 0; i--)
            {
                JSONObject sensorJson;
                if(i%2 == 0)
                    sensorJson = getSensorJson("Gyro");
                else
                    sensorJson = getSensorJson("OtherSensor");
                sensorArr.put(sensorJson);
            }
            JSONObject finalRes = new JSONObject();
            finalRes.put("Sensors", sensorArr );
            finalRes.put("Control", controlJson);
            tv.append( finalRes.toString() );

            serverExchange(CS_SERVER, SERVER_PORT, finalRes.toString() );
        }
        catch (Exception ex)
        {
            System.out.println(ex.getMessage());
            System.out.println("Sync Server exception");
        }

    }
    private void serverExchange(final String host, final int port, final String data) {
        Thread thread = new Thread()
        {
            @Override
            public void run()
            {
                try
                {
                    Socket socket = new Socket(host, port);

                    OutputStream outputStream = socket.getOutputStream();
                    outputStream.write((data).getBytes());
                    outputStream.flush();

                    InputStream is = socket.getInputStream();
                    byte[] bytes = new byte[1024];
                    int n = is.read(bytes);
                    task_json = new String(bytes, 0, n);
                    processTask(task_json);

                    is.close();
                    socket.close();
                }
                catch (Exception e)
                {
                    System.out.println("Socket creation failed");
                }
            }
        };
        thread.start();
    }
    private Activity getActivity()
    {
        return MainActivity.this;
    }
}
