package edu.purdue.dcsl.crowdsensingclient;

import java.util.Random;
import static java.util.concurrent.TimeUnit.*;
import android.Manifest;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static android.content.Context.SENSOR_SERVICE;

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
    private float[] GyroValues = new float[3];
    private float[] BaroValues = new float[1];
    private float[] AcceValues = new float[3];
    private float[] GPSValues = new float[2]; // first is latitude and second is logitude
    private List<String> GyroCtl = new ArrayList<String>();
    private List<String> BaroCtl = new ArrayList<String>();
    private List<String> AccelCtl = new ArrayList<String>();

    public Context context;
    private  long minTime = 30; // millisecondes
    private int minDistance = 10; // meters
    boolean isGPSEnabled = false;

    // flag for network status
    boolean isNetworkEnabled = false;

    boolean canGetLocation = false;

    Location location;
    public SensorManager mSensorManager;
    public LocationManager locManager;
    public SensorReader(Context context)
    {
        this.context = context;
        //scheduledRegister();
        //registeListener();
    }

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);



    public float[] getGyro()
    {
        registeListener();
        while(GyroValues[0] == 0.0 )
            System.out.println("waiting for Gyro");
        return GyroValues;
    }
    public float[] getBaro()
    {
        return BaroValues;
    }
    public float[] getAccl()
    {
        return AcceValues;
    }
    public float[] getGPS()
    {
        try {
            locManager = (LocationManager) context.getSystemService(context.LOCATION_SERVICE);
            isGPSEnabled = locManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            isNetworkEnabled = locManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if(!isGPSEnabled && !isNetworkEnabled)
            {
                Log.d("Location", "No locaiton provider is allowed");
            }
            else
            {
                this.canGetLocation = true;

                if(isNetworkEnabled)
                {
                    if (Build.VERSION.SDK_INT >= 23
                            && ContextCompat.checkSelfPermission(context,
                            Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                            && ContextCompat.checkSelfPermission(context,
                            Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                    {
                        locManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, minTime, minDistance, locListener);
                    }
                    if(locManager != null)
                    {
                        location = locManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        if(location != null)
                        {
                            GPSValues[0] = (float) location.getLatitude();
                            GPSValues[1] = (float) location.getLongitude();
                        }
                    }
                }
                if(isGPSEnabled)
                {
                    if (location == null)
                    {
                        if (Build.VERSION.SDK_INT >= 23
                                && ContextCompat.checkSelfPermission(context,
                                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                                && ContextCompat.checkSelfPermission(context,
                                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                        {
                            locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, minTime, minDistance, locListener);
                        }
                        if(locManager != null)
                        {
                            location = locManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            if(location != null)
                            {
                                GPSValues[0] = (float) location.getLatitude();
                                GPSValues[1] = (float) location.getLongitude();
                            }
                        }
                    }
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return GPSValues;
    }
    public List<String> getCtl(String sensorName)

    {
        if (sensorName == "Gyro")
            return GyroCtl;
        else if (sensorName == "Baro")
            return BaroCtl;
        else if (sensorName == "Accel")
            return AccelCtl;
        else
            return null;
    }



    public void scheduledRegister() {
        final Runnable beeper = new Runnable() {
            public void run()
            {
                registeListener();
            }
        };
        final ScheduledFuture<?> beeperHandle =
                scheduler.scheduleAtFixedRate(beeper, 10, 10, SECONDS); //regist sensor every hour
        scheduler.schedule(new Runnable()
        {
            public void run()
            {
                beeperHandle.cancel(true);
            }
        }, 10, SECONDS);
    }

    public void registeListener()
    {
        mSensorManager = (SensorManager) context.getSystemService(context.SENSOR_SERVICE);
        mSensorManager.registerListener(mSensorListener,
                mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE),
                SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(mSensorListener,
                mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(mSensorListener,
                mSensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE),
                SensorManager.SENSOR_DELAY_NORMAL);
    }
    public LocationListener locListener = new LocationListener(){
        @Override
        public void onStatusChanged(String provider, int status, Bundle extreas){

        }
        @Override
        public void onProviderEnabled(String provider){

        }
        @Override
        public void onProviderDisabled(String provider){

        }
        @Override
        public void onLocationChanged(Location location){

        }

    };


    public SensorEventListener mSensorListener = new SensorEventListener() {
        private static final String TAG = "HelloService";
        private static final String DEBUG_TAG = "SensorService";
        private boolean isRunning  = false;
        private SensorManager sensorManager = null;
        private Sensor sensor = null;
        private boolean pressure = false;
        private boolean gyro = false;
        private boolean accel= false;


        public IBinder onBind(Intent arg0) {
            Log.i(DEBUG_TAG, "Service onBind");
            return null;
        }

        @Override
        public void onSensorChanged(SensorEvent event) {
            Log.d("MY_APP", event.toString());

            Sensor sens = event.sensor;
            Long tsLong = System.currentTimeMillis()/1000;
            String ts = tsLong.toString();
            if (!gyro && sens.getType() == Sensor.TYPE_GYROSCOPE){
                Log.i(DEBUG_TAG, String.valueOf(event.values[0] + sens.getName()));
                Log.i(DEBUG_TAG, String.valueOf(event.values[1] + sens.getName()));
                Log.i(DEBUG_TAG, String.valueOf(event.values[2] + sens.getName()));
                GyroValues[0] = event.values[0];
                GyroValues[1] = event.values[1];
                GyroValues[2] = event.values[2];
                GyroCtl.add(ts);
                gyro = true;
            }
            if (!pressure && sens.getType() == Sensor.TYPE_PRESSURE) {
                Log.i(DEBUG_TAG, String.valueOf(event.values[0] + sens.getName()));
                BaroValues[0] = event.values[0];
                BaroCtl.add(ts);
                pressure = true;
            }
            if (!accel && sens.getType() == Sensor.TYPE_ACCELEROMETER){
                Log.i(DEBUG_TAG, String.valueOf(event.values[0] + sens.getName()));
                Log.i(DEBUG_TAG, String.valueOf(event.values[1] + sens.getName()));
                Log.i(DEBUG_TAG, String.valueOf(event.values[2] + sens.getName()));
                AcceValues[0] = event.values[0];
                AcceValues[1] = event.values[1];
                AcceValues[2] = event.values[2];

                AccelCtl.add(ts);
                accel = true;
            }

            if (pressure == true && gyro == true && accel == true) {
                sensorManager.unregisterListener(this);
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            Log.d("MY_APP", sensor.toString() + " - " + accuracy);
        }

    };



}
