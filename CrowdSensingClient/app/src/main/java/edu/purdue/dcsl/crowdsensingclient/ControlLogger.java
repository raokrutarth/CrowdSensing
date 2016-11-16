package edu.purdue.dcsl.crowdsensingclient;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.os.Environment;

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
        logControl();
        System.out.println("Flag 2");
    }

    /* This method will run every ~1hr and save
        the needed control info in a log file */
    private void logControl()
    {
        System.out.println("Control logger called");

        ControlInfoReader cir = new ControlInfoReader();
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



/* ############# Auto generated code (don't touch) ################ */


    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_FOO = "edu.purdue.dcsl.crowdsensingclient.action.FOO";
    private static final String ACTION_BAZ = "edu.purdue.dcsl.crowdsensingclient.action.BAZ";


    private static final String EXTRA_PARAM1 = "edu.purdue.dcsl.crowdsensingclient.extra.PARAM1";
    private static final String EXTRA_PARAM2 = "edu.purdue.dcsl.crowdsensingclient.extra.PARAM2";

    public ControlLogger() {
        super("ControlLogger");
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.

     */
    @Override
    public void onCreate() {
        super.onCreate();
    }

    public static void startActionFoo(Context context, String param1, String param2) {
        Intent intent = new Intent(context, ControlLogger.class);
        intent.setAction(ACTION_FOO);
        intent.putExtra(EXTRA_PARAM1, param1);
        intent.putExtra(EXTRA_PARAM2, param2);
        context.startService(intent);
    }

    /**
     * Starts this service to perform action Baz with the given parameters. If
     * the service is already performing a task this action will be queued.
     *

     */

    public static void startActionBaz(Context context, String param1, String param2) {
        Intent intent = new Intent(context, ControlLogger.class);
        intent.setAction(ACTION_BAZ);
        intent.putExtra(EXTRA_PARAM1, param1);
        intent.putExtra(EXTRA_PARAM2, param2);
        context.startService(intent);
    }



    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionFoo(String param1, String param2) {

        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Handle action Baz in the provided background thread with the provided
     * parameters.
     */
    private void handleActionBaz(String param1, String param2) {

        throw new UnsupportedOperationException("Not yet implemented");
    }
}
