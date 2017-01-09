package edu.purdue.dcsl.crowdsensingclient;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;


/**
 * Created by Henry on 11/26/16.
 */

public class SyncService extends BroadcastReceiver {


    @Override
    public void onReceive(Context context, Intent intent)
    {
        Log.w("SyncService", "in SyncService");
        MainActivity.SyncServer1();
    }
}
