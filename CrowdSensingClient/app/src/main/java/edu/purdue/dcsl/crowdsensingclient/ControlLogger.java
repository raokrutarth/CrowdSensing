package edu.purdue.dcsl.crowdsensingclient;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;

import java.io.FileOutputStream;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class ControlLogger extends IntentService
{
    @Override
    protected void onHandleIntent(Intent intent)
    {
        logControl();
    }

    public void logControl()
    {
        System.out.println("Control logger called");
        // this method will run every ~1hr and save
        // the needed control info in a log file
        ControlInfoReader cir = new ControlInfoReader(this);
        float battery = cir.getBattery(getApplicationContext());
        String filename = "controlLog.dat";
        String string = "" + battery + "\n";
        System.out.println("battery reading = " + string);
        FileOutputStream outputStream;
        try
        {
            System.out.println(filename + " created");
            outputStream = openFileOutput(filename, Context.MODE_PRIVATE);
            outputStream.write(string.getBytes());
            outputStream.close();
        }
        catch (Exception e)
        {
            System.out.println("Writing to log file failed");
        }
    }






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
