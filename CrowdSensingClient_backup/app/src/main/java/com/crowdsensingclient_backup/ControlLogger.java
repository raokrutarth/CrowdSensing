package com.crowdsensingclient_backup;

import android.Manifest;
import android.app.Activity;
import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.media.MediaBrowserServiceCompat;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * An IntentService subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * helper methods.
 */
public class ControlLogger extends BroadcastReceiver
{

    @Override
    public void onReceive(Context context, Intent intent) {
        String message = "Hellooo, alrm worked ----";
        System.out.println(message);
        logControl(context);
    }
    /*
    @Override
    protected void onHandleIntent(Intent intent)
    {
        logControl();
    }
    */
    /* This method will run every ~1hr and save
        the needed control info in a log file */
    public static void logControl(Context context)
    {
        Log.w("Scheduling", "Control logger called");

        ControlInfoReader cir = new ControlInfoReader( context);
        String batteryInfo = cir.getBatteryStatus(context);
        String imei = cir.getImei();
        String signal = cir.getSignalStrength();
        String logEntry = "" + batteryInfo ;
        logEntry += "," + imei;
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

    /*public ControlLogger() {
        super("ControlLogger");
    }

    @Override
    public void onCreate ()
    {
        super.onCreate();
    }
    */

}
