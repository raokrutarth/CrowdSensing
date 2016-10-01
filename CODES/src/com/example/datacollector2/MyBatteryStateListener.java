package com.example.datacollector2;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;

public class MyBatteryStateListener extends BroadcastReceiver {
	private static final String TAG = "MyBatteryStateListener";
	private static final String batteryChangeEvent = "batteryChanged"; 
	
	private PowerManager powerManager;
	private WakeLock mWakeLock = null;
	
	Context mCtx;
	
	public MyBatteryStateListener(Context context) {
		mCtx = context;
		powerManager = (PowerManager) mCtx.getSystemService(Context.POWER_SERVICE);
	}
	
	@Override
    public void onReceive(Context context, Intent intent) {
		mWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyBatteryStateListenerWakelock");
		mWakeLock.acquire();
		
        int batteryLevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
        int maxLevel = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 0);
		int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
		boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
		                     status == BatteryManager.BATTERY_STATUS_FULL;
		String msg = String.format("batteryLevel=%d, batteryCapacity=%d, isCharging=%s", batteryLevel, maxLevel, isCharging? "1": "0");
		LogWriter.getInstance().write(context, batteryChangeEvent, msg);
		//Log.i(TAG, "OnBatteryChange: " + msg);
		
		mWakeLock.release();
		mWakeLock = null;
    
	}
	
}
