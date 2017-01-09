package edu.purdue.dcsl.crowdsensingclient;
import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by raok on 11/11/16.
 */

public class SensorReader
{
    private float[] GyroValues = new float[3];
    private float[] BaroValues = new float[1];
    private float[] AcceValues = new float[3];
    private float[] GPSValues = new float[2]; // first is latitude and second is logitude

    public Context context;
    private  long minTime = 30; // millisecondes
    private int minDistance = 10; // meters
    boolean isGPSEnabled = false;

    // flag for network status
    boolean isNetworkEnabled = false;

    boolean canGetLocation = false;

    Location location; // location
    public SensorManager mSensorManager;
    public LocationManager locManager;
    public SensorReader(Context context)
    {
        this.context = context;
        Intent gyroIntent = new Intent(context, GyroService.class);
        Intent baroIntent = new Intent(context, BaroService.class);
        Intent accelIntent = new Intent(context, AccelService.class);
        context.startService(gyroIntent);
        context.startService(baroIntent);
        context.startService(accelIntent);
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

    public float[] getGyro()
    {
        try {
            File file = new File(MainActivity.SDCARD, MainActivity.GRYO_READING);
            BufferedReader br = new BufferedReader(new FileReader(file));
            String sCurrentLine;
            while ((sCurrentLine = br.readLine()) != null) {
                System.out.println(sCurrentLine);
                String[] gValues = sCurrentLine.split("_");
                System.out.println(GyroValues.length);
                GyroValues[0] = Float.valueOf(gValues[0]);
                GyroValues[1] = Float.valueOf(gValues[1]);
                GyroValues[2] = Float.valueOf(gValues[2]);
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());

        }

        return GyroValues;
    }
    public float[] getBaro()
    {
        try {
            File file = new File(MainActivity.SDCARD, MainActivity.BARO_READING);
            BufferedReader br = new BufferedReader(new FileReader(file));
            String sCurrentLine;
            while ((sCurrentLine = br.readLine()) != null) {
                System.out.println(sCurrentLine);
                String[] gValues = sCurrentLine.split("_");
                System.out.println(BaroValues.length);
                BaroValues[0] = Float.valueOf(gValues[0]);
                //BaroValues[1] = Float.valueOf(gValues[1]);
                //BaroValues[2] = Float.valueOf(gValues[2]);
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());

        }
        return BaroValues;
    }
    public float[] getAccl()
    {
        try {
            File file = new File(MainActivity.SDCARD, MainActivity.ACCEL_READING);
            BufferedReader br = new BufferedReader(new FileReader(file));
            String sCurrentLine;
            while ((sCurrentLine = br.readLine()) != null) {
                System.out.println(sCurrentLine);
                String[] gValues = sCurrentLine.split("_");
                System.out.println(AcceValues.length);
                AcceValues[0] = Float.valueOf(gValues[0]);
                AcceValues[1] = Float.valueOf(gValues[1]);
                AcceValues[2] = Float.valueOf(gValues[2]);
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());

        }
        return AcceValues;
    }

    public float[] getGPS(Activity Act)
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
                            && ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                            && ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                    {
                        locManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, minTime, minDistance, locListener,  Looper.getMainLooper());
                    }
                    else
                    {
                        ActivityCompat.requestPermissions(Act, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                                Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.INTERNET}, 10);
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
                                && ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                                && ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                        {
                            locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, minTime, minDistance, locListener,  Looper.getMainLooper());
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
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
                String ts = sdf.format(new Date());
                MainActivity.append("GPS," + ts);
            }
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());;
        }
        return GPSValues;
    }

}