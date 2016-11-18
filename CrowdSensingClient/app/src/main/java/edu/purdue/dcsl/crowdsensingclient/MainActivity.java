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
import java.util.Calendar;
import java.util.Date;

public class MainActivity extends AppCompatActivity
{
    public final static String Jmsg = "edu.purdue.dcsl.crowdsensingclient.JSON_RESULT";
    public final static String CONTROL_LOG = "edu.purdue.dcsl.crowdsensingclient.ControllogFile";
    public final static String GRYO_READING = "edu.purdue.dcsl.crowdsensingclient.GyroReadingFile";
    public final static String BARO_READING = "edu.purdue.dcsl.crowdsensingclient.BaroReadingFile";
    public final static String ACCEL_READING = "edu.purdue.dcsl.crowdsensingclient.AccelReadingFile";
    public final static String SENSOR_CONTROL = "edu.purdue.dcsl.crowdsensingclient.SensorControlFile";
    public static File SDCARD = Environment.getExternalStorageDirectory();
    //private Intent gyroIntent;
    // private PendingIntent alarmIntentGyro;

    private static final String CS_SERVER = "128.46.208.244";
    private static final int SERVER_PORT = 21567;
    private SensorReader sr;
    private static String task_json ;
    private static String reading_result = "";

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
        Calendar calendar = Calendar.getInstance();
        AlarmManager myscheduler = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intentGyro = new Intent(getApplicationContext(), GyroService.class);
        Intent intentBaro = new Intent(getApplicationContext(), BaroService.class);
        Intent intentAccel = new Intent(getApplicationContext(), AccelService.class);

        PendingIntent scheduleGyroIntent = PendingIntent.getService(getApplicationContext(), 0, intentGyro, PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent scheduleBaroIntent = PendingIntent.getService(getApplicationContext(), 0, intentBaro, PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent scheduleAccelIntent = PendingIntent.getService(getApplicationContext(), 0, intentAccel, PendingIntent.FLAG_UPDATE_CURRENT);
        long interval = 120000;
        myscheduler.setRepeating(AlarmManager.RTC_WAKEUP, 1, interval, scheduleGyroIntent);
        myscheduler.setRepeating(AlarmManager.RTC_WAKEUP, 1, interval, scheduleBaroIntent);
        myscheduler.setRepeating(AlarmManager.RTC_WAKEUP, 1, interval, scheduleAccelIntent);

        sr = new SensorReader( getApplicationContext() );
    }

    private JSONObject processTask(String tskJson) {
        try
        {
            System.out.println("Task JSON received: ");
            System.out.println(tskJson);
            JSONObject task_j = new JSONObject(tskJson);
            JSONArray arr = task_j.getJSONArray("Tasks");

            JSONObject readingJson = new JSONObject();


            for (int i = 0; i < arr.length(); i++)
            {
                JSONObject newtask = arr.getJSONObject(i);
                String sensor = newtask.getString("sensor_name");
                String deadline = newtask.getString("DDL");
                System.out.println("Task received: " + sensor + " " + deadline);
                SimpleDateFormat sdf = new SimpleDateFormat("dd-M-yyyy hh:mm");
                String dateInString = deadline;
                Date task_deadline = sdf.parse(dateInString);
                System.out.println("Parsed date: " + task_deadline);
                Date thresh = new Date();
                thresh.setTime(thresh.getTime() + 15*60000 ); // 15 mins
                Date now = new Date();
                if (now.before(task_deadline) && task_deadline.after(thresh) )
                {
                    readingJson.put(sensor, getSensorJson(sensor));
                }
            }
            reading_result = readingJson.toString();
            return readingJson;
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

    public JSONObject getSensorJson(String sensorName)
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
        else if(sensorName.equals("Accl") )
            controlReadings = sr.getAccl();
        else if(sensorName.equals("GPS") )
            controlReadings = sr.getGPS(MainActivity.this);
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
                System.out.println("reading from : " + file.getAbsolutePath() );
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
                                controlJson.put("Battery", controlReadings[i]);
                                break;
                            case 1:
                                controlJson.put("IMEI", controlReadings[i]);
                                break;
                            case 2:
                                controlJson.put("SignalStrength", controlReadings[i]);
                                break;
                        }
                        if(i > 2)
                            System.out.println("Extra control readings detected");
                    }
                    // controlJson.put("Entry", controlEntry);
                    break;
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
            JSONObject finalRes;
            if(reading_result.length() < 10)
            {
                initReadingJson();
                finalRes = new JSONObject(reading_result);
            }
            else
            {
                finalRes = new JSONObject();
                finalRes.put("Readings", new JSONArray(reading_result) );
            }
            finalRes.put("Control", controlJson);
            tv.append( finalRes.toString() );

            serverExchange(CS_SERVER, SERVER_PORT, finalRes.toString() );
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

    private void initReadingJson()
    {
        try
        {
            JSONArray sensorArr = new JSONArray();
            for(int i = 0; i < 4; i++)
            {
                JSONObject sensorJson;
                if(i == 2 )
                    sensorJson = getSensorJson("Gyro");
                else if( i ==0)
                    sensorJson = getSensorJson("Baro");
                else if(i==3)
                    sensorJson = getSensorJson("GPS");
                else
                    sensorJson = getSensorJson("Accl");
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
            BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
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
