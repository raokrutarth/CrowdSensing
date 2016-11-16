package edu.purdue.dcsl.crowdsensingclient;

import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
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
    // private static context = getApplicationContext();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        verifyStatePermissions(this);
        verifyStoragePermissions(this);

        // setup control info logging for every half hour
        Intent controlLoggerIntent = new Intent(getActivity(), ControlLogger.class);
        System.out.println("Flag 0");
        startService(controlLoggerIntent);

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

    public JSONObject getSensorJson(String sensorName)
    {
        SensorReader sr = new SensorReader( getApplicationContext() );
        float[] controlReadings;
        /*public float[3] getGyro()
          public float[1] getBaro()
          public float[3] getAccl()
          public float[2] getGPS()*/

        if( sensorName.equals("Gyro") )
            controlReadings = sr.getGyro();
        else if(sensorName.equals("Baro") )
            controlReadings = sr.getBaro();
        else if(sensorName.equals("Accl") )
            controlReadings = sr.getAccl();
        else if(sensorName.equals("GPS") )
            controlReadings = sr.getGPS();
        else
        {
            System.out.println("Unknown sensor requested");
            controlReadings = new float[0];
        }
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
            ArrayList<String> logArr = new ArrayList<String>();
            try
            {
                BufferedReader br = new BufferedReader(new FileReader(file));
                String line;
                while ((line = br.readLine()) != null)
                    logArr.add(line);
                br.close();
            }
            catch (IOException e)
            {
                System.out.println("File path : " + file.getAbsolutePath() );
                System.out.println("File exists? " + file.exists() );
                System.out.println("Unable to read file in getControlJson()");
            }

            for(String entry : logArr)
            {
                JSONObject controlEntry = new JSONObject();
                if(entry != null)
                {
                    System.out.println("entry: " + entry);
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
            tv.setMovementMethod(new ScrollingMovementMethod() );
            tv.setText("Sending Readings & Control Info...\n");
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

                    // Clear the Control log file
                    File f = new File(SDCARD, CONTROL_LOG);
                    if(f.exists())
                        f.delete();
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
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    public static void verifyStoragePermissions(Activity activity)
    {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        while(permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }
    public static void verifyStatePermissions(Activity activity)
    {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_PHONE_STATE);

        if(permission != PackageManager.PERMISSION_GRANTED)
        {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions( activity,
                    new String[]{Manifest.permission.READ_PHONE_STATE},
                    123);
        }
        if(permission == PackageManager.PERMISSION_GRANTED)
            System.out.println("PHONE state permission granted");
    }
}
