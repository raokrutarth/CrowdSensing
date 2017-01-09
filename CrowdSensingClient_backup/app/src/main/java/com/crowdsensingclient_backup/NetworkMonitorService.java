package com.crowdsensingclient_backup;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import static android.content.ContentValues.TAG;


public class NetworkMonitorService extends IntentService {

    public NetworkMonitorService() {
        super("NetworkMonitorService");
    }

    @Override
    protected void onHandleIntent(Intent intent)
    {
        while(true)
            useTcpDump();
    }
    void useTcpDump()
    {
        System.out.println("Starting to monitor network using tcpdump...");
        try
        {
            String logfile = MainActivity.SDCARD + "/" + MainActivity.NETWORK_LOG;
            String command2="timeout 10 /data/local/tcpdump -i wlan0";
            String command="timeout 10 /data/local/tcpdump -i wlan0 > " + logfile + "\n";
            String command3="tcpdump -i wlan0";

            System.out.println("[+] Writing tcpdump output to " + logfile);
            // run the command using helper function
            String temp = sudoForResult(command);

            System.out.println("Command result: " + temp);

            File dumpedFile = new File(MainActivity.SDCARD, MainActivity.NETWORK_LOG);
            if(!dumpedFile.exists())
                System.out.println("[-] Network logfile not present");
            //open a reader on the tcpdump output file
            BufferedReader reader = new BufferedReader(new FileReader(dumpedFile));
            String temp2 = new String();
            //The while loop is broken if the thread is interrupted
            while (!Thread.interrupted())
            {
                temp2 = reader.readLine();
                if (temp2!=null)
                {
                    Log.e("READER",new String(temp));
                    System.out.println("Network Log entry: " + temp2);
                }
            }
        }
        catch (IOException ie)
        {
            System.out.println("[-] IO error in network monitor service");
        }
        /*catch (InterruptedException e)
        {
            System.out.println("[-] 2");
        }*/

    }

    public static String sudoForResult(String...strings) {
        String res = "";
        DataOutputStream outputStream = null;
        InputStream response = null;
        try{
            Process su = Runtime.getRuntime().exec("su -c sh");
            outputStream = new DataOutputStream(su.getOutputStream());
            response = su.getInputStream();

            for (String s : strings)
            {
                System.out.println("Running: " + s);
                outputStream.writeBytes(s+"\n");
                outputStream.flush();
            }

            outputStream.writeBytes("exit\n");
            outputStream.flush();
            try
            {
                su.waitFor();
            } catch (InterruptedException e)
            {
                e.printStackTrace();
            }
            res = readFully(response);
        }

        catch (IOException e){
            e.printStackTrace();
        } finally {
            Closer.closeSilently(outputStream, response);
        }
        return res;
    }

    public static String readFully(InputStream is) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[2048];
        int length = 0;
        while ((length = is.read(buffer)) != -1) {
            baos.write(buffer, 0, length);
        }
        return baos.toString("UTF-8");
    }


}