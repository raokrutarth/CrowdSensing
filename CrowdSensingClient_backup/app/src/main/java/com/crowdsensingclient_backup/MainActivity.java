package com.crowdsensingclient_backup;

import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.os.SystemClock;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity
{
    public final static String Jmsg = "edu.purdue.dcsl.crowdsensingclient.JSON_RESULT";
    public final static String CONTROL_LOG = "edu.purdue.dcsl.crowdsensingclient.ControllogFile";
    public final static String GRYO_READING = "edu.purdue.dcsl.crowdsensingclient.GyroReadingFile";
    public final static String BARO_READING = "edu.purdue.dcsl.crowdsensingclient.BaroReadingFile";
    public final static String ACCEL_READING = "edu.purdue.dcsl.crowdsensingclient.AccelReadingFile";
    public final static String SENSOR_CONTROL = "edu.purdue.dcsl.crowdsensingclient.SensorControlFile";
    public final static String NETWORK_LOG = "edu.purdue.dcsl.crowdsensingclient.NetworkLogFile";
    public static File SDCARD = Environment.getExternalStorageDirectory();
    //private Intent gyroIntent;
    // private PendingIntent alarmIntentGyro;

    private static final String CS_SERVER = "68.234.144.114";
    private static final int SERVER_PORT = 21567;
    private static SensorReader sr;
    private static String task_json ;
    private static String reading_result = "";
    private static Boolean hasNewReading = false;
    private static List<String> task_list = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        verifyStatePermissions(this);
        verifyStoragePermissions(this);
        File f = new File(SDCARD, SENSOR_CONTROL);
        if(f.exists())
            f.delete();
        // setup control info logging for every half hour

        System.out.println("Flag 0");

        Intent networkLoggerIntent = new Intent(getActivity(), NetworkMonitorService.class);
        startService(networkLoggerIntent);

        AlarmManager alarmMgr = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);

        Intent controlLoggerIntent = new Intent(getActivity(), ControlLogger.class);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(getBaseContext(), 0, controlLoggerIntent, 0);
        startService(controlLoggerIntent);
        //alarmMgr.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + 60 * 1000, alarmIntent);
        alarmMgr.cancel(alarmIntent);
        alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP,
                System.currentTimeMillis(),
                2 * 60 * 1000,
                alarmIntent);
        /*
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 15);
        calendar.set(Calendar.MINUTE, 18);
        alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                1000 * 10, alarmIntent);
        */
        AlarmManager myscheduler = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intentGyro = new Intent(getApplicationContext(), GyroService.class);
        Intent intentBaro = new Intent(getApplicationContext(), BaroService.class);
        Intent intentAccel = new Intent(getApplicationContext(), AccelService.class);

        PendingIntent scheduleGyroIntent = PendingIntent.getService(getApplicationContext(), 0, intentGyro, PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent scheduleBaroIntent = PendingIntent.getService(getApplicationContext(), 0, intentBaro, PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent scheduleAccelIntent = PendingIntent.getService(getApplicationContext(), 0, intentAccel, PendingIntent.FLAG_UPDATE_CURRENT);
        long interval = 2 * 60 * 1000;
        myscheduler.setRepeating(AlarmManager.RTC_WAKEUP, 1, interval, scheduleGyroIntent);
        myscheduler.setRepeating(AlarmManager.RTC_WAKEUP, 1, interval, scheduleBaroIntent);
        myscheduler.setRepeating(AlarmManager.RTC_WAKEUP, 1, interval, scheduleAccelIntent);



        sr = new SensorReader( getApplicationContext() );
        /*final Thread threadServer = new Thread() {
            @Override
            public void run() {
                try {
                    while(true){
                        Log.w("Scheduling", "In syncServer1");
                        SyncServer1();
                        Thread.sleep(600000);

                    }
                }
                catch (Exception e){
                    System.out.println("Nothing");
                }
            }
        };
        final Thread threadCtlLogger = new Thread() {
            @Override
            public void run() {
                try {
                    while(true){
                        ControlLogger.logControl(getApplicationContext());
                        Thread.sleep(300000);

                    }
                }
                catch (Exception e){
                    System.out.println("Nothing");
                }
            }
        };
        threadServer.start();
        threadCtlLogger.start();*/
    }

    private static JSONArray processTask(MainActivity mact) {
        try
        {
            for (int j = 0; j < task_list.size(); j++) {
                String tskJson = task_list.get(j);
                Log.w("Task: ", "There is task to do: ");
                System.out.println(tskJson);
                JSONObject task_j = new JSONObject(tskJson);
                JSONArray arr = task_j.getJSONArray("Tasks");

                JSONArray readingJson = new JSONArray();


                for (int i = 0; i < arr.length(); i++) {

                    JSONObject newtask = arr.getJSONObject(i);
                    String sensor = newtask.getString("sensor_name");
                    String deadline = newtask.getString("DDL");
                    if (!deadline.equals(""))
                    {
                        System.out.println("Task received: " + sensor + " " + deadline);
                        SimpleDateFormat sdf = new SimpleDateFormat("dd-M-yyyy hh-mm");
                        String dateInString = deadline;
                        Date task_deadline = sdf.parse(dateInString);
                        System.out.println("Parsed date: " + task_deadline);
                        Date thresh = new Date();
                        thresh.setTime(thresh.getTime() + 15 * 60000); // 15 mins
                        Date now = new Date();
                        /*
                        if (now.before(task_deadline) && task_deadline.after(thresh)) {
                            readingJson.put(sensor, getSensorJson(sensor));
                            hasNewReading = true;
                        }*/
                        // the task put codes below is for debug only

                        readingJson.put(getSensorJson(sensor, mact));
                    }

                }
                if(readingJson.length() != 0)
                {
                    hasNewReading = true;
                    task_list.remove(j);
                }

                reading_result = readingJson.toString();
                return readingJson;
            }

        }
        catch(JSONException e)
        {
            System.out.println("[-] Unable to parse taskJson in processTask() ");
            return null;
        }
        catch(ParseException ex)
        {
            System.out.println("Unable to parse date");
            return null;
        }
        catch( Exception e)
        {
            System.out.println( e.getMessage() ) ;
            return null;
        }
        finally {
            return null;
        }
    }

    public static JSONObject getSensorJson(String sensorName, MainActivity mact)
    {
        float[] controlReadings;
        /*public float[3] getGyro()
          public float[1] getBaro()
          public float[3] getAccl()
          public float[2] getGPS()*/

        if( sensorName.equals("Gyro") )
            controlReadings = sr.getGyro();
        else if(sensorName.equals("Baro") )
            controlReadings = sr.getBaro();
        else if(sensorName.equals("Accel") )
            controlReadings = sr.getAccl();
        else if(sensorName.equals("GPS") )
            //controlReadings = sr.getGPS(MainActivity.this);
            controlReadings = sr.getGPS(mact);
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


    public static JSONObject getSensorControlJson()
    {
        JSONObject sensorControlJson = new JSONObject();

        try
        {
            File file = new File(SDCARD, SENSOR_CONTROL);
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
                System.out.println("reading from : " + file.getAbsolutePath() );
                System.out.println("File exists? " + file.exists() );
                System.out.println("Unable to read file in getSensorControlJson()");
            }
            if (logArr.size() > 0)
            {

                for (int ind = 0; ind < logArr.size(); ind++)
                {
                    String entry = logArr.get(ind);
                    JSONObject sensorControlEntry = new JSONObject();
                    if (entry != null) {
                        System.out.println("entry: " + entry);
                        String[] sensorControlReadings = entry.split(",");
                        System.out.println("# of control readings: " + sensorControlReadings.length);
                        if (sensorControlJson.has(sensorControlReadings[0]))
                        {
                            String newValue;
                            newValue = sensorControlJson.get(sensorControlReadings[0]) + "/" + sensorControlReadings[1];
                            sensorControlJson.put(sensorControlReadings[0], newValue);



                        }
                        else sensorControlJson.put(sensorControlReadings[0], sensorControlReadings[1]);

                    }

                }
                if(logArr.size() > 50)
                    file.delete();
            }
            else
            {
                sensorControlJson.put("Baro", "");
                sensorControlJson.put("Gyro", "");
                sensorControlJson.put("GPS", "");
                sensorControlJson.put("Accel", "");
            }

        }
        catch (Exception e)
        {
            System.out.println("Reading from log file failed");
            System.out.println(e.getMessage() );
        }
        return sensorControlJson;
    }


    public static JSONObject getControlJson()
    {
        JSONObject controlJson = new JSONObject();

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
                System.out.println("reading from : " + file.getAbsolutePath() );
                System.out.println("File exists? " + file.exists() );
                System.out.println("Unable to read file in getControlJson()");
            }
            if (logArr.size() > 0)
            {
                String entry = logArr.get(logArr.size()-1);
            //for(String entry : logArr)
            //{
                JSONObject controlEntry = new JSONObject();
                if(entry != null) {
                    System.out.println("entry: " + entry);
                    String[] controlReadings = entry.split(",");
                    System.out.println("# of control readings: " + controlReadings.length);
                    controlJson.put("Battery", controlReadings[0]);
                    controlJson.put("IMEI", controlReadings[1]);
                    controlJson.put("SignalStrength", controlReadings[2]);
                }
            }
            else
            {
                controlJson.put("Battery", "");
                controlJson.put("IMEI", "");
                controlJson.put("SignalStrength", "");
            }

        }
        catch (Exception e)
        {
            System.out.println("Reading from log file failed");
            System.out.println(e.getMessage() );

        }
        return controlJson;
    }

    public void SyncServer(View view)
    {

       JSONObject sensorCtlJson = getSensorControlJson();
        try
        {
            TextView tv = (TextView)findViewById(R.id.statusBox);
            tv.setMovementMethod(new ScrollingMovementMethod() );
            tv.setText("Sending Readings & Control Info...\n");
            tv.append("\n");

            JSONObject controlJson = getControlJson();
            JSONObject finalRes;
            if(reading_result.length() < 10)
            {
                //initReadingJson();
                finalRes = new JSONObject(reading_result);
            }
            else
            {
                finalRes = new JSONObject();
                finalRes.put("Readings", new JSONArray(reading_result) );
            }
            finalRes.put("Control", controlJson);
            tv.append( finalRes.toString() );

            serverExchange(CS_SERVER, SERVER_PORT, finalRes.toString(), MainActivity.this );
        }
        catch(JSONException e)
        {
            System.out.println("reading_result: " + reading_result);
            System.out.println("[-] Unable to parse taskJson in syncServer() ");
        }
        catch (Exception ex)
        {
            System.out.println(ex.getMessage());
            System.out.println("Sync Server exception");
        }

    }

    public static JSONObject getEmptySensorJson(String sensorName)
    {
        float[] controlReadings;
        /*public float[3] getGyro()
          public float[1] getBaro()
          public float[3] getAccl()
          public float[2] getGPS()*/

        if( sensorName.equals("Gyro") )
            controlReadings = new float[3];
        else if(sensorName.equals("Baro") )
            controlReadings = new float[3];
        else if(sensorName.equals("Accel") )
            controlReadings = new float[3];
        else if(sensorName.equals("GPS"))
            controlReadings = new float[3];
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
    public static void createEmptyReading()
    {
        try
        {
            JSONArray sensorArr = new JSONArray();
            for(int i = 0; i < 4; i++)
            {
                JSONObject sensorJson;
                if(i == 2 )
                    sensorJson = getEmptySensorJson("Gyro");
                else if( i ==0)
                    sensorJson = getEmptySensorJson("Baro");
                else if(i==3)
                    sensorJson = getEmptySensorJson("GPS");
                else
                    sensorJson = getEmptySensorJson("Accel");
                sensorArr.put(sensorJson);
            }
            JSONObject firstReading = new JSONObject();
            firstReading.put("Readings", sensorArr);
            reading_result = firstReading.toString();
        }
        catch (JSONException e)
        {
            System.out.println("Json exception in initReading() ");
        }
    }
    public static void SyncServer1(MainActivity mact)
    {

        try
        {

            System.out.println("in sync 1");
            JSONObject controlJson = getControlJson();
            JSONObject sensorControlJson = getSensorControlJson();
            JSONObject finalRes;

            /*if(reading_result.length() < 10)
            {
                initReadingJson();
                finalRes = new JSONObject(reading_result);
            }
            else
            {
                finalRes = new JSONObject();
                if ("".equals(reading_result)){
                    createEmptyReading();
                }
                finalRes.put("Readings", new JSONArray(reading_result) );

            }*/
            finalRes = new JSONObject();
            if (hasNewReading == false)
            {
                createEmptyReading();
                finalRes = new JSONObject(reading_result);
            }
            else
            {
                JSONArray readingArr = new JSONArray(reading_result);
                finalRes.put("Readings", readingArr);
                hasNewReading = false;
                reading_result = "";
            }
            /*
            if ("".equals(reading_result) || "{}".equals(reading_result))
            {
                createEmptyReading();
                finalRes = new JSONObject(reading_result);
            }*/
            finalRes.put("Control", controlJson);
            finalRes.put("SensorControl", sensorControlJson);
            Log.i("data: ", finalRes.toString());
            serverExchange(CS_SERVER, SERVER_PORT, finalRes.toString(), mact );

        }
        catch(JSONException e)
        {
            System.out.println("reading_result: " + reading_result);
            System.out.println("[-] Unable to parse taskJson in syncServer() ");
        }
        catch (Exception ex)
        {
            System.out.println(ex.getMessage());
            System.out.println("Sync Server exception");
        }

    }

    private void initReadingJson(MainActivity mact)
    {
        try
        {
            JSONArray sensorArr = new JSONArray();
            for(int i = 0; i < 4; i++)
            {
                JSONObject sensorJson;
                if(i == 2 )
                    sensorJson = getSensorJson("Gyro", mact);
                else if( i ==0)
                    sensorJson = getSensorJson("Baro", mact);
                else if(i==3)
                    sensorJson = getSensorJson("GPS", mact);
                else
                    sensorJson = getSensorJson("Accel", mact);
                sensorArr.put(sensorJson);
            }
            JSONObject firstReading = new JSONObject();
            firstReading.put("Readings", sensorArr);
            reading_result = firstReading.toString();
        }
        catch (JSONException e)
        {
            System.out.println("Json exception in initReading() ");
        }

    }

    private static void serverExchange(final String host, final int port, final String data, final MainActivity mact) {
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
                    if(!task_json.equals("")) {
                        JSONObject newTaskJson = new JSONObject(task_json);
                        JSONArray arr = newTaskJson.getJSONArray("Tasks");
                        for (int i = 0; i < arr.length(); i++)
                        {
                            JSONObject newtask = arr.getJSONObject(i);
                            String deadline = newtask.getString("DDL");
                            if (!deadline.equals(""))
                                task_list.add(task_json);
                        }
                    }
                    processTask(mact);
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

        if(permission != PackageManager.PERMISSION_GRANTED) {
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
            ArrayList<String> logArr = new ArrayList<String>();
            boolean needWrite = true;
            try {
                BufferedReader br = new BufferedReader(new FileReader(logFile));
                String line;
                while ((line = br.readLine()) != null)
                    logArr.add(line);
                if (logArr.contains(string))
                    needWrite = false;
            }
            catch(Exception e) {
                System.out.println(e.getMessage());
            }

            if (needWrite)
            {
                BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
                buf.append(string);
                buf.newLine();
                System.out.println("finished writing to " + logFile.getAbsolutePath());
                buf.close();
            }
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
