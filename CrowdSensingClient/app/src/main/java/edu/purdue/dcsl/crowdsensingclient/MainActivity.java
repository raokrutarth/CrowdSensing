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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity
{
    public static String Jmsg = "edu.purdue.dcsl.crowdsensingclient.JSON_RESULT";
    private static String task_json;
    public final static String CONTROL_LOG = "edu.purdue.dcsl.crowdsensingclient.ControllogFile";
    public final static String GRYO_READING = "edu.purdue.dcsl.crowdsensingclient.GyroReadingFile";
    public final static String BARO_READING = "edu.purdue.dcsl.crowdsensingclient.BaroReadingFile";
    public final static String ACCEL_READING = "edu.purdue.dcsl.crowdsensingclient.AccelReadingFile";
    public final static String SENSOR_CONTROL = "edu.purdue.dcsl.crowdsensingclient.SensorControlFile";
    public static File SDCARD = Environment.getExternalStorageDirectory();
    private Intent controlLoggerIntent;
    private Intent gyroIntent;
    private AlarmManager alarmMgr;
    private SensorReader sr;
    private PendingIntent alarmIntent;
    private PendingIntent alarmIntentGyro;
 @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        File f = new File(getActivity().getFilesDir(), "controlLog.dat");
        if(!f.exists())
        {
            System.out.println("control log file created");
            new File(getActivity().getFilesDir(),"controlLog.dat"); // getActivity().getFilesDir(),
        }
        FileOutputStream outputStream;
        try {
            outputStream = openFileOutput("controlLog.dat", Context.MODE_PRIVATE);
            String string = "This is control log file";
            outputStream.write(string.getBytes());
            outputStream.close();
        }
        catch (Exception e)
        {
            System.out.println("creating control log file failed.");
        }

        // setup control info logging for every half hour
        controlLoggerIntent = new Intent(getActivity(), ControlLogger.class);
        alarmIntent = PendingIntent.getBroadcast(getBaseContext(), 0, controlLoggerIntent, 0);
        alarmMgr = (AlarmManager)getActivity().getSystemService(Context.ALARM_SERVICE);
        alarmMgr.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                AlarmManager.INTERVAL_HALF_HOUR,
                AlarmManager.INTERVAL_HALF_HOUR, alarmIntent);

/*
        gyroIntent = new Intent(getApplicationContext(), GyroService.class);
        alarmIntentGyro = PendingIntent.getBroadcast(getBaseContext(), 0, gyroIntent,0);

        alarmMgr.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                System.currentTimeMillis(),
                10, alarmIntentGyro);*/

        Calendar calendar = Calendar.getInstance();
        AlarmManager myscheduler = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intentGyro = new Intent(getApplicationContext(), GyroService.class);
        Intent intentBaro = new Intent(getApplicationContext(), BaroService.class);
        Intent intentAccel = new Intent(getApplicationContext(), AccelService.class);

        PendingIntent scheduleGyroIntent = PendingIntent.getService(getApplicationContext(), 0, intentGyro, PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent scheduleBaroIntent = PendingIntent.getService(getApplicationContext(), 0, intentBaro, PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent scheduleAccelIntent = PendingIntent.getService(getApplicationContext(), 0, intentAccel, PendingIntent.FLAG_UPDATE_CURRENT);

        myscheduler.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), 1, scheduleGyroIntent);
        myscheduler.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), 1, scheduleBaroIntent);
        myscheduler.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), 1, scheduleAccelIntent);

        sr = new SensorReader( getApplicationContext() );
    }

    private boolean processTask(String tskJson)
    {
        System.out.println("Task JSON received: ");
        System.out.println(tskJson);

        // call the relevant sensors here
        return true;
    }

    public JSONObject getSensorJson(String sensorName)
            // sensorNames: Baro, Accel, Gyro
    {
        //TODO: Logic error here. return reading values or control values?
        Reading rd = new Reading();

        rd.setRname(sensorName);

        /*
        ListcontrolReadings = sr.getCtl(sensorName);
        float[] controlReadings = new float[ListcontrolReadings.size()];
        int j = 0;
        for (Float f : ListcontrolReadings) {
            controlReadings[j++] = (f != null ? f : Float.NaN);
        }*/
        if (sensorName == "Gyro") rd.setAxisReading(sr.getGyro());
        else if (sensorName == "Baro") rd.setAxisReading(sr.getBaro());
        else if (sensorName == "Accel") rd.setAxisReading(sr.getAccl());
        //else if (sensorName == "GPS") rd.setAxisReading(sr.getGPS());
        //TODO: what is R1?

        //rd.setR1(controlReadings[0]);
        for(int i = 0; i < 10; i++)
            rd.addMetaData("Access: " + i + "pm");
        return JsonUtil.toJson(rd);
    }
    public JSONObject getControlJson()
    {

        try
        {
            File f = new File(getActivity().getFilesDir(), "controlLog.dat");
            FileInputStream is = new FileInputStream(f);
            byte[] bytes = new byte[1024];
            int n = is.read(bytes);
            is.close();

            String controlInfo = new String(bytes, 0, n);
            String[] controlEntry = controlInfo.split("\n");
            if(controlEntry[0] != null)
            {
                Reading r = new Reading();
                //TODO: what is setR1 used for?
                r.setR1(Double.valueOf(controlEntry[0]) );
                r.setRname("battery");
                return JsonUtil.toJson(r);
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
            JSONObject jl = getSensorJson("Gyro");
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


    public static synchronized void append(String string)
    {
        File logFile = new File(MainActivity.SDCARD, MainActivity.SENSOR_CONTROL);
        if (!logFile.exists())
        {
            try
            {
                logFile.createNewFile();
            }
            catch (IOException e)
            {
                System.out.println("[+] SD card state valid: " + checkSdCard() );
                System.out.println("[-] Unable to create new Sensor control file");
                e.printStackTrace();
            }
        }
        try
        {
            BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, false));
            buf.append(string);
            buf.newLine();
            System.out.println("finished writing to " + logFile.getAbsolutePath() );
            buf.close();
        }
        catch (IOException e)
        {
            System.out.println("[-] Unable to write to log file");
            e.printStackTrace();
        }

    }

    private static boolean checkSdCard()
    {
        /* Checks if external storage is available for read and write */
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state))
        {
            return true;
        }
        return false;
    }


}
