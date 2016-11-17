package edu.purdue.dcsl.crowdsensingclient;

import android.app.IntentService;
import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Henry on 11/16/16.
 */

public class GyroService extends Service implements SensorEventListener {
    private static final String TAG = "GyroService";
    private static final String DEBUG_TAG = "GyroService";
    public SensorManager mSensorManager;
    private boolean gyro;
    @Override
    public IBinder onBind(Intent arg0) {
        Log.i(DEBUG_TAG, "Service onBind");
        return null;
    }
    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.i(TAG, "Service onStartCommand");
        gyro = false;

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);


        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE), SensorManager.SENSOR_DELAY_FASTEST);
        return Service.START_STICKY;
    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        Log.d("MY_APP", sensor.toString() + " - " + accuracy);
    }
    @Override
    public void onSensorChanged(SensorEvent event)
    {
        new SensorEventLoggerTask().execute(event);
        if (gyro == true) {
            mSensorManager.unregisterListener(this);
            stopSelf();
            Log.d(DEBUG_TAG, "Gyro service Stops here.");
        }

    }

    private class SensorEventLoggerTask extends
            AsyncTask<SensorEvent, Void, Void> {
        //@Override
        protected Void doInBackground(SensorEvent... events) {
            SensorEvent event = events[0];
            Log.d("MY_APP", event.toString());

            Sensor sens = event.sensor;
            Long tsLong = System.currentTimeMillis()/1000;
            String ts = tsLong.toString();
            String readings = "" + event.values[0] + "_" + event.values[1] +"_"+ event.values[2] + "_" + ts;
            appendReading(readings);
            MainActivity.append("Gryo," + ts);
            gyro = true;
            return null;
        }
    }

    public static void appendReading(String text)
    {
        File logFile = new File(MainActivity.SDCARD, MainActivity.GRYO_READING);
        if (!logFile.exists())
        {
            try
            {
                logFile.createNewFile();
            }
            catch (IOException e)
            {
                System.out.println("[+] SD card state valid: " + checkSdCard() );
                System.out.println("[-] Unable to create new GYRO reading file");
                e.printStackTrace();
            }
        }
        try
        {
            BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, false));
            buf.append(text);
            buf.newLine();
            SimpleDateFormat sdf = new SimpleDateFormat("dd-hh-mm-ss");
            String res = sdf.format(new Date());
            System.out.println("finished writing to [" + res+ "] " + logFile.getAbsolutePath() );
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