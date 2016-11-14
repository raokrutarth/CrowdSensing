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
        Reading rd = new Reading();
        SensorReader sr = new SensorReader();
        float[] controlReadings;
        rd.setRname(sensorName);
        controlReadings = sr.getSensorX();
        rd.setAxisReading(controlReadings);
        rd.setR1(controlReadings[0]);
        for(int i = 0; i < 10; i++)
            rd.addMetaData("Access: " + i + "pm");
        return JsonUtil.toJson(rd);
    }
    public JSONObject getControlJson()
    {
        JSONObject controlJson = new JSONObject();
        try
        {
            /*FileInputStream is;
            File f = new File(SDCARD, CONTROL_LOG);
            is = new FileInputStream(f);
            byte[] bytes = new byte[1024];
            int n = is.read(bytes);
            is.close();*/
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
            /*String[] controlEntry = new String[logFile.size()];
            controlEntry = logFile.toArray( controlEntry);*/

            for(String entry : logFile)
            {
                if(entry != null)
                {
                    String[] controlReadings = entry.split(",");
                    System.out.println("# of control readings: " + controlReadings.length);
                    for(int i = 0; i < controlReadings.length; ++i )
                    {
                        Reading r = new Reading();
                        r.setR1(99 );
                        r.setRname("battery");
                    }
                }

            }
            return null;
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage() );
            System.out.println("Reading from log file failed");
            return null;
        }
    }
    public void SyncServer(View view)
    {
        try
        {
            JSONObject jl = getSensorJson("Light");
            JSONObject ja = getSensorJson("Accel");
            TextView tv = (TextView)findViewById(R.id.statusBox);
            tv.setText("Sending Readings & Control Info...");
            tv.append("\n");
            JSONArray sArr = new JSONArray();
            sArr.put(jl);
            sArr.put(ja);
            JSONObject jc_b = getControlJson();
            JSONArray cArr = new JSONArray();
            cArr.put(jc_b);

            JSONObject finalRes = new JSONObject();
            finalRes.put("Sensors", sArr );
            finalRes.put("Control", cArr);
            tv.append(finalRes.toString());

            serverExchange("35.160.36.179", 21567, finalRes.toString() );

            // Clear the Control log file
            File f = new File(SDCARD, CONTROL_LOG);
            if(f.exists())
                f.delete();

        /*Intent intent = new Intent(MainActivity.this, DisplayMessageActivity.class);
        intent.putExtra(Jmsg, jr);
        startActivity(intent);*/
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
