package com.example.henry.tcp_app;

import android.app.AlertDialog;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class TCPClientActivity extends AppCompatActivity {

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    private int getLayoutResID() {
        return R.layout.activity_tcpclient;
    }

    private final int HANDLER_MSG_TELL_RECV = 0x124;

    private EditText mHostEditText = null;
    private EditText mPortEditText = null;
    private EditText mContentEditText = null;

    private Button mSubmitButton = null;
    private Button mCancelButton = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tcpclient);

        initEvent();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    private void initEvent() {
        initViews();

        setViews();
    }

    private void initViews() {
        mHostEditText = (EditText) findViewById(R.id.client_method_editText);
        mPortEditText = (EditText) findViewById(R.id.client_mode_editText);
        mContentEditText = (EditText) findViewById(R.id.client_content_editText);

        mSubmitButton = (Button) findViewById(R.id.client_submit_button);
        mCancelButton = (Button) findViewById(R.id.client_cancel_button);
    }

    private void setViews() {
        mSubmitButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                String host = mHostEditText.getText().toString();
                String port = mPortEditText.getText().toString();
                String content = mContentEditText.getText().toString();

                Toast.makeText(TCPClientActivity.this, host + ", " + port + ", " + content, 0).show();
                startNetThread(host, Integer.parseInt(port), content);
            }
        });

        mCancelButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                setEmptyEdittext();
            }
        });
    }


    private void startNetThread(final String host, final int port, final String data) {
        Thread thread = new Thread() {
            @Override
            public void run() {
                try {

                    Socket socket = new Socket(host, port);
                    OutputStream outputStream = socket.getOutputStream();
                    outputStream.write((data).getBytes());
                    outputStream.flush();
                    System.out.println(socket);

                    InputStream is = socket.getInputStream();
                    byte[] bytes = new byte[1024];
                    int n = is.read(bytes);
                    System.out.println(new String(bytes, 0, n));

                    Message msg = handler.obtainMessage(HANDLER_MSG_TELL_RECV, new String(bytes, 0, n));
                    msg.sendToTarget();

                    is.close();
                    socket.close();
                } catch (Exception e) {
                    System.out.println("Bad");
                }
            }
        };

        thread.start();
    }

    Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            AlertDialog.Builder builder = new AlertDialog.Builder(TCPClientActivity.this);
            builder.setMessage("来自服务器的数据：" + (String) msg.obj);
            builder.create().show();
        }

        ;
    };

    private void setEmptyEdittext() {
        mHostEditText.setText("");
        mPortEditText.setText("");
        mContentEditText.setText("");
    }

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("TCPClient Page") // TODO: Define a title for the content shown.
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
