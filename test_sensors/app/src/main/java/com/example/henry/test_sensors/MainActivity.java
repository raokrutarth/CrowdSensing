package com.example.henry.test_sensors;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

//import com.example.henry.test_sensors.PhoneStateService;
//import com.example.henry.test_sensors.R;
//import com.example.datacollector2.R;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;

public class MainActivity extends Activity {

    private static final String TAG = "MainActivity";
    private boolean mIsLogStart = false;
    private boolean mIsPhoneStateStart = false;

    private TextView mTvInfo;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTvInfo = (TextView) findViewById(R.id.textView1);

//		if (isServiceRunning(LogService.class)) {
//			Log.d(TAG, "log service is already running.");
//			mIsLogStart = true;
//			mTvInfo.setText("log service is already running.\n");
//		}

        if (isServiceRunning(com.example.henry.test_sensors.PhoneStateService.class)) {
            Log.d(TAG, "PhoneStateService service is already running.");
            mIsPhoneStateStart = true;
            mTvInfo.setText("PhoneStateService service is already running.\n");
        }
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    public void btnOnClickStartService(View view) {

//		if (mIsLogStart == false) {
//			Log.d(TAG, "start log service");
//			startService(new Intent(getBaseContext(), LogService.class));
//			mTvInfo.setText(mTvInfo.getText() + "start log service\n");
//			mIsLogStart = true;
//		}

        if (mIsPhoneStateStart == false) {
            Log.d(TAG, "start PhoneStateService");
            startService(new Intent(getBaseContext(), com.example.henry.test_sensors.PhoneStateService.class));
            mTvInfo.setText(mTvInfo.getText() + "start PhoneStateService\n");
            mIsPhoneStateStart = true;
        }
    }

    // Stop the  service
    public void btnOnClinckStopService(View view) {

//		if (mIsLogStart == true) {
//			Log.d(TAG, "stop log service");
//			stopService(new Intent(getBaseContext(), LogService.class));		
//			mTvInfo.setText(mTvInfo.getText() + "stop service\n");
//			mIsLogStart = false;
//		}

        if (mIsPhoneStateStart == true) {
            Log.d(TAG, "stop PhoneStateService");
            stopService(new Intent(getBaseContext(), PhoneStateService.class));
            mTvInfo.setText(mTvInfo.getText() + "stop PhoneStateService\n");
            mIsPhoneStateStart = false;
        }
    }

    private boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("Main Page") // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        AppIndex.AppIndexApi.start(client, getIndexApiAction());
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.end(client, getIndexApiAction());
        client.disconnect();
    }
}
