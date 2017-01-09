package com.crowdsensingclient_backup;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.TextView;

public class DisplayMessageActivity extends AppCompatActivity {
    /// this activity is not yet useed

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_message);

        Intent intent = getIntent();
        String message = intent.getStringExtra(MainActivity.Jmsg);
        TextView textView = new TextView(this);
        textView.setTextSize(14);
        textView.setText(message);
        textView.append("\n");
        textView.append("This activity will send the data over to the " +
                "server and show progress to the user");

        ViewGroup layout = (ViewGroup) findViewById(R.id.activity_display_message);
        layout.addView(textView);
    }
}
