package com.example.datacollector2;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class AutoStart extends BroadcastReceiver {
	
	private final static String TAG = "AutoStart";
	
    @Override
    public void onReceive(Context context, Intent intent) {
    	Log.i(TAG, "Auto start service");
    	if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
    		Log.i(TAG, "ACTION_BOOT_COMPLETED");
	        //context.startService(new Intent(context, LogService.class));
	        context.startService(new Intent(context, PhoneStateService.class));
    	}
    }
}
