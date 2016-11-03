package com.example.henry.test_sensors;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.example.henry.test_sensors.BatteryInfo;
import com.example.henry.test_sensors.CbLocationManager;
import com.example.henry.test_sensors.ConnectivityChangeListener;
import com.example.henry.test_sensors.LogWriter;
import com.example.henry.test_sensors.MyPhoneStateListener;

public class PhoneStateService extends Service {
	private final static String TAG = "PhoneStateService";
	private MyPhoneStateListener mPhoneListener;
	private TelephonyManager  telephonyManager;
	
	//private MyBatteryStateListener mBatteryListener;
	private final static int BATTERY_MONITOR_INTERVAL = (1000 * 60 * 10);	// 10 min 
	private final Handler mHandler = new Handler(); 
	
	private ConnectivityChangeListener mConnListener;
	
	private CbLocationManager locationManager;
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(TAG, "onStartCommand");
		//LogWriter.getInstance().write(this, "Start service.");
		
		// cellular data listener
		telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
		mPhoneListener = new MyPhoneStateListener(this);
//		telephonyManager.listen(new MyPhoneStateListener(this),
//		PhoneStateListener.LISTEN_CALL_STATE
//		| PhoneStateListener.LISTEN_CELL_INFO // Requires API 17
//		| PhoneStateListener.LISTEN_CELL_LOCATION
//		| PhoneStateListener.LISTEN_DATA_ACTIVITY
//		| PhoneStateListener.LISTEN_DATA_CONNECTION_STATE
//		| PhoneStateListener.LISTEN_SERVICE_STATE
//		| PhoneStateListener.LISTEN_SIGNAL_STRENGTHS
//		| PhoneStateListener.LISTEN_CALL_FORWARDING_INDICATOR
//		| PhoneStateListener.LISTEN_MESSAGE_WAITING_INDICATOR);
		telephonyManager.listen(mPhoneListener, PhoneStateListener.LISTEN_DATA_ACTIVITY 
												| PhoneStateListener.LISTEN_CELL_LOCATION 
												| PhoneStateListener.LISTEN_CELL_INFO);
		 
		// battery listener
		//mBatteryListener = new MyBatteryStateListener(this);
		//registerReceiver(mBatteryListener, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
		startBatteryMonitor();
		
		// location listener
		locationManager = new CbLocationManager(this);
		locationManager.startGettingLocations();
		
		// connectivity listener
		mConnListener = new ConnectivityChangeListener(this);
		registerReceiver(mConnListener, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
		
	    return Service.START_STICKY;
	}	
	
	private Runnable mBatteryMonitorRunnable = new Runnable() {
		
		private static final String batteryMonitorEvent = "batteryMonitor"; 
		
		@Override
	    public void run() {
			BatteryInfo batInfo = new BatteryInfo(getApplicationContext());
			String msg = String.format("batteryLevel=%d, batteryCapacity=%d, isCharging=%s", 
										batInfo.getCurrentBatteryLevel(), 
										batInfo.getCurrentBatteryCapacity(), 
										batInfo.isCharging()? "1": "0");
			LogWriter.getInstance().write(getApplicationContext(), batteryMonitorEvent, msg);
			mHandler.postDelayed(mBatteryMonitorRunnable, BATTERY_MONITOR_INTERVAL);
	    }
	};
	
	private void startBatteryMonitor() {
		mHandler.post(mBatteryMonitorRunnable);
	}
	
	private void stopBatteryMonitor() {
		mHandler.removeCallbacks(mBatteryMonitorRunnable);
		mBatteryMonitorRunnable = null;
	}

	@Override
	public IBinder onBind(Intent intent) {
		//TODO for communication return IBinder implementation
		Log.d(TAG, "onBind");
		return null;
	}
	
	@Override
	public void onDestroy() {
		Log.d(TAG, "onDestroy");
		//LogWriter.getInstance().write(this, "Stop service.");
		telephonyManager.listen(mPhoneListener, PhoneStateListener.LISTEN_NONE);
		//unregisterReceiver(mBatteryListener);
		stopBatteryMonitor();
		locationManager.stopGettingLocations();
		unregisterReceiver(mConnListener);
	}	
}
