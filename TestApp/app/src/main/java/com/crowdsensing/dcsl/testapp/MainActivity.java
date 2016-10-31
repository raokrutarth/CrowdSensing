package com.crowdsensing.dcsl.testapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



    }

    public void syncRClicked(View view) {
        TextView statusMsg = (TextView) findViewById(R.id.statusMessage);
        statusMsg.setText("Sync functionality not yet implemented");
        // TODO for sync button clicked
    }
}
