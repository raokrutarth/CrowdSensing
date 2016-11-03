package com.example.henry.test_sensors;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;

public class BatteryInfo {
	Context ctx;
	
	public BatteryInfo(Context ctx) {
		this.ctx = ctx;
	}
	
	public boolean isCharging() {
		Intent batteryIntent = ctx.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
		int status = batteryIntent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
		boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
		                     status == BatteryManager.BATTERY_STATUS_FULL;
		return isCharging;
	}
	
	public int getCurrentBatteryLevel() {
		Intent batteryIntent = ctx.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
	    return batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
	}
	
	public int getCurrentBatteryCapacity() {
		Intent batteryIntent = ctx.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
	    return batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
	}
	
	public float getCurrentBatteryPercent() {
		Intent batteryIntent = ctx.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
	    int level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
	    int scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
	    return ((float)level / (float)scale) * 100.0f; 
	}
	
}
