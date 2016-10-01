package com.example.datacollector2;

import java.util.List;

import android.content.Context;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.telephony.CellIdentityCdma;
import android.telephony.CellIdentityGsm;
import android.telephony.CellIdentityLte;
import android.telephony.CellIdentityWcdma;
import android.telephony.CellInfo;
import android.telephony.CellInfoCdma;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.CellLocation;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;

public class MyPhoneStateListener extends PhoneStateListener {
	Context mContext;
	private final static String TAG = "MyPhoneStateListener";
	private final static String eventData = "cellData";
	private final static String eventCellInfo = "cellInfoChanged";
	private final static String eventCellLoc = "cellLocChanged";
	
	private PowerManager powerManager;
	private WakeLock mWakeLockDataAct = null;
	private WakeLock mWakeLockDataCellInfo = null;
	private WakeLock mWakeLockDataLocation = null;
	private TelephonyManager  telephonyManager;

	public MyPhoneStateListener(Context context) {
		mContext = context;
		powerManager = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
		telephonyManager = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE); 
	}

	@Override
	public void onDataActivity(int direction) {
		mWakeLockDataAct = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyPhoneStateListenerWakelockDataAct");
		mWakeLockDataAct.acquire();
		
		super.onDataActivity(direction);
		
		switch (direction) {
		case TelephonyManager.DATA_ACTIVITY_NONE:
			LogWriter.getInstance().write(mContext, eventData, "activity=DATA_ACTIVITY_NONE");
			//Log.i(TAG, "onDataActivity: DATA_ACTIVITY_NONE");
			break;
		case TelephonyManager.DATA_ACTIVITY_IN:
			LogWriter.getInstance().write(mContext, eventData, "activity=DATA_ACTIVITY_IN");
			//Log.i(TAG, "onDataActivity: DATA_ACTIVITY_IN");
			break;
		case TelephonyManager.DATA_ACTIVITY_OUT:
			LogWriter.getInstance().write(mContext, eventData, "activity=DATA_ACTIVITY_OUT");
			//Log.i(TAG, "onDataActivity: DATA_ACTIVITY_OUT");
			break;
		case TelephonyManager.DATA_ACTIVITY_INOUT:
			LogWriter.getInstance().write(mContext, eventData, "activity=DATA_ACTIVITY_INOUT");
			//Log.i(TAG, "onDataActivity: DATA_ACTIVITY_INOUT");
			break;
		case TelephonyManager.DATA_ACTIVITY_DORMANT:
			LogWriter.getInstance().write(mContext, eventData, "activity=DATA_ACTIVITY_DORMANT");
			//Log.i(TAG, "onDataActivity: DATA_ACTIVITY_DORMANT");
			break;
		default:
			Log.w(TAG, "onDataActivity: UNKNOWN " + direction);
			break;
		}
		mWakeLockDataAct.release();
		mWakeLockDataAct = null;
	}

	//@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
	@Override
	public void onCellInfoChanged(List<CellInfo> cellInfo) {
		mWakeLockDataCellInfo = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyPhoneStateListenerWakelockCellInfo");
		mWakeLockDataCellInfo.acquire();
		
		super.onCellInfoChanged(cellInfo);
		
		if (cellInfo == null) {
			Log.d(TAG, "onCellInfoChanged(): cellInfo is null");
			//return;
		}
		
		String info = getMyCellInfo(cellInfo);
		
		LogWriter.getInstance().write(mContext, eventCellInfo, info);
		//Log.i(TAG, eventCellInfo + ": " + info);
		
		mWakeLockDataCellInfo.release();
		mWakeLockDataCellInfo = null;
	}
	
	private String getMyCellInfo(List<CellInfo> cellInfo) {
		boolean hasNetwork = telephonyManager.getNetworkType() != android.telephony.TelephonyManager.NETWORK_TYPE_UNKNOWN;
		String retStr = null;
		String networkType = "";
		String cid = "";
		
		if (!hasNetwork) {
			retStr = "cellNetType=unknown, cid=unknown, cloc=unknown";
		} else if(cellInfo == null) {
			retStr = "cellNetType=null, cid=null, cloc=null";
		} else {
			for(CellInfo ci : cellInfo)
			{	
				if (ci instanceof CellInfoGsm) {
					CellInfoGsm cellInfoGsm = (CellInfoGsm) ci;
					CellIdentityGsm cellIdGsm = cellInfoGsm.getCellIdentity();
					if (cellInfoGsm.isRegistered()) {
						networkType = "GSM";
						cid = cellIdGsm.toString();
						break;
					}
				} else if(ci instanceof CellInfoCdma) {
					CellInfoCdma cellInfoCdma = (CellInfoCdma) ci;
					CellIdentityCdma cellIdCdma = cellInfoCdma.getCellIdentity();
					if (cellInfoCdma.isRegistered()) {
						networkType = "CDMA";
						cid = cellIdCdma.toString();
						break;
					}
				} else if(ci instanceof CellInfoWcdma) {
					CellInfoWcdma cellInfoWcdma = (CellInfoWcdma) ci;
					CellIdentityWcdma cellIdWcdma = cellInfoWcdma.getCellIdentity();
					if (cellInfoWcdma.isRegistered()) {
						networkType = "WCDMA";
						cid = cellIdWcdma.toString();
						break;
					}
				} else if(ci instanceof CellInfoLte) {
					CellInfoLte cellInfoLte = (CellInfoLte) ci;
					CellIdentityLte cellIdLte = cellInfoLte.getCellIdentity();
					if (cellInfoLte.isRegistered()) {
						networkType = "LTE";
						cid = cellIdLte.toString();
						break;
					}
				}	
			}
			
			retStr = String.format("cellNetType=%s, cid=%s",
	 				 networkType,
	 				 cid);
		}
		
		return retStr;
	}
	
	//@TargetApi(Build.VERSION_CODES.GINGERBREAD)
	@Override
	public void onCellLocationChanged(CellLocation location) {
		mWakeLockDataLocation = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyPhoneStateListenerWakelockLocation");
		mWakeLockDataLocation.acquire();
		
		super.onCellLocationChanged(location);
		
		String newLocStr = null;
		
		if (location instanceof GsmCellLocation) {
			GsmCellLocation gcLoc = (GsmCellLocation) location;
			newLocStr = "newGcLoc=" + gcLoc.toString();
		} else if (location instanceof CdmaCellLocation) {
			CdmaCellLocation ccLoc = (CdmaCellLocation) location;
			newLocStr = "newCcLoc=" + ccLoc.toString();
		} else {
			newLocStr = "newLoc=" + location.toString();
		}
		
		String cellInfoStr = getMyCellInfo((List<CellInfo>) telephonyManager.getAllCellInfo());
		
		String msg = newLocStr + ", " + cellInfoStr;
		
		LogWriter.getInstance().write(mContext, eventCellLoc, msg);
		
		mWakeLockDataLocation.release();
		mWakeLockDataLocation = null;
	}	
	
	
//	@Override
//	public void onDataConnectionStateChanged(int state) {
//		super.onDataConnectionStateChanged(state);
//	    switch(state){
//	    case TelephonyManager.DATA_CONNECTED :
//	    	Log.i(TAG, "onDataConnectionStateChanged: DATA_CONNECTED");
//	        break;
//	    case TelephonyManager.DATA_CONNECTING :
//	    	Log.i(TAG, "onDataConnectionStateChanged: DATA_CONNECTING");
//	        break;
//	    case TelephonyManager.DATA_DISCONNECTED :
//	    	Log.i(TAG, "onDataConnectionStateChanged: DATA_DISCONNECTED");
//	        break;
//	    case TelephonyManager.DATA_SUSPENDED :
//	    	Log.i(TAG, "onDataConnectionStateChanged: DATA_SUSPENDED");
//	        break;
//	    }
//	}

//	@Override
//	public void onServiceStateChanged(ServiceState serviceState) {
//		super.onServiceStateChanged(serviceState);
//		Log.i(TAG, "onServiceStateChanged: " + serviceState.toString());
//		Log.i(TAG, "onServiceStateChanged: getOperatorAlphaLong "
//				+ serviceState.getOperatorAlphaLong());
//		Log.i(TAG, "onServiceStateChanged: getOperatorAlphaShort "
//				+ serviceState.getOperatorAlphaShort());
//		Log.i(TAG, "onServiceStateChanged: getOperatorNumeric "
//				+ serviceState.getOperatorNumeric());
//		Log.i(TAG, "onServiceStateChanged: getIsManualSelection "
//				+ serviceState.getIsManualSelection());
//		Log.i(TAG,
//				"onServiceStateChanged: getRoaming "
//						+ serviceState.getRoaming());
//
//		switch (serviceState.getState()) {
//		case ServiceState.STATE_IN_SERVICE:
//			Log.i(TAG, "onServiceStateChanged: STATE_IN_SERVICE");
//			break;
//		case ServiceState.STATE_OUT_OF_SERVICE:
//			Log.i(TAG, "onServiceStateChanged: STATE_OUT_OF_SERVICE");
//			break;
//		case ServiceState.STATE_EMERGENCY_ONLY:
//			Log.i(TAG, "onServiceStateChanged: STATE_EMERGENCY_ONLY");
//			break;
//		case ServiceState.STATE_POWER_OFF:
//			Log.i(TAG, "onServiceStateChanged: STATE_POWER_OFF");
//			break;
//		}
//	}

//	@Override
//	public void onCallStateChanged(int state, String incomingNumber) {
//		super.onCallStateChanged(state, incomingNumber);
//		switch (state) {
//		case TelephonyManager.CALL_STATE_IDLE:
//			Log.i(TAG, "onCallStateChanged: CALL_STATE_IDLE");
//			break;
//		case TelephonyManager.CALL_STATE_RINGING:
//			Log.i(TAG, "onCallStateChanged: CALL_STATE_RINGING");
//			break;
//		case TelephonyManager.CALL_STATE_OFFHOOK:
//			Log.i(TAG, "onCallStateChanged: CALL_STATE_OFFHOOK");
//			break;
//		default:
//			Log.i(TAG, "UNKNOWN_STATE: " + state);
//			break;
//		}
//	}


//
//	@Override
//	public void onCallForwardingIndicatorChanged(boolean cfi) {
//		super.onCallForwardingIndicatorChanged(cfi);
//		Log.i(TAG, "onCallForwardingIndicatorChanged: " + cfi);
//	}
//
//	@Override
//	public void onMessageWaitingIndicatorChanged(boolean mwi) {
//		super.onMessageWaitingIndicatorChanged(mwi);
//		Log.i(TAG, "onMessageWaitingIndicatorChanged: " + mwi);
//	}
}