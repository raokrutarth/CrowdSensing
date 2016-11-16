package edu.purdue.dcsl.crowdsensingclient;

import android.Manifest;
import android.app.Activity;
import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * An IntentService subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * helper methods.
 */
public class ControlLogger extends IntentService
{
    @Override
    protected void onHandleIntent(Intent intent)
    {
        System.out.println("Flag 1");
        for(int i = 0; i < 5; i++)
            logControl();
        System.out.println("Flag 2");
    }

    /* This method will run every ~1hr and save
        the needed control info in a log file */
    private void logControl()
    {
        System.out.println("Control logger called");

        ControlInfoReader cir = new ControlInfoReader( getApplicationContext() );
        double battery = cir.batteryP();
        String imie = cir.getImei();
        String signal = cir.getSignalStrength();
        String logEntry = "" + battery ;
        logEntry += "," + imie;
        logEntry += "," + signal;
        System.out.println("Logging entry: " + logEntry);

        appendLog(logEntry);
    }
    public static void appendLog(String text)
    {
        File logFile = new File(MainActivity.SDCARD, MainActivity.CONTROL_LOG);
        if (!logFile.exists())
        {
            try
            {
                logFile.createNewFile();
            }
            catch (IOException e)
            {
                System.out.println("[+] SD card state valid: " + checkSdCard() );
                System.out.println("[-] Unable to create new log file");
                e.printStackTrace();
            }
        }
        try
        {
            BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
            buf.append(text);
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

    public ControlLogger() {
        super("ControlLogger");
    }

    @Override
    public void onCreate ()
    {
        super.onCreate();
    }

}
