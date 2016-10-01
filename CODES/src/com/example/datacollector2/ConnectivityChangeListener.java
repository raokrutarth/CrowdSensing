package com.example.datacollector2;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;

public class ConnectivityChangeListener extends BroadcastReceiver {
    
	private final static String TAG = "ConnectivityChangeListener";
	private final static String eventConnChange = "connectivityChanged";
	
	private PowerManager powerManager;
	private WakeLock mWakeLock = null;
	
	public ConnectivityChangeListener(Context context) {
		powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
	}
	
	@Override
    public void onReceive(Context context, Intent intent) {
		mWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyBatteryStateListenerWakelock");
		mWakeLock.acquire();
		
		final String action = intent.getAction();
		if(ConnectivityManager.CONNECTIVITY_ACTION.equals(action)) {
			NetworkInfo info = intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO); 
			if (info != null) {
				String msg = "networkType=" + info.getTypeName();
				if (!info.getSubtypeName().equals(""))
					msg = msg + ", subType=" + info.getSubtypeName();
				else
					msg = msg + ", subType=null";
				msg = msg + ", state=" + info.getState();
				msg = msg + ", reason=" + info.getReason();
				LogWriter.getInstance().write(context, eventConnChange, msg);
			}
		}
		
		mWakeLock.release();
		mWakeLock = null;
    }
}
