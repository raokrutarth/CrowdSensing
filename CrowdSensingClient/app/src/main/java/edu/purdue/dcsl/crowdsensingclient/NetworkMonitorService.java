package edu.purdue.dcsl.crowdsensingclient;

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

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
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
            String command="timeout 3 tcpdump -i wlan0"; // > " + logfile + "\n";
            String command2="tcpdump -i wlan0 > " + logfile + "\n";
            String command3="tcpdump -i wlan0";

            System.out.println("[+] Writing tcpdump output to " + logfile);
            System.out.println("[+] Running command: " + command);
            String temp = sudoForResult(command);
            System.out.println("Command result: " + temp);




            //sleep 2 second to ensure that the new process is listed by the system
            Thread.sleep(2000);
            // process.destroy();
            /* get the pid of the process in which we exec tcpdump
            * to do that we use the ps command, so we need to launch
            * another process to achieve that
            */
            /*Process process2 = Runtime.getRuntime().exec("ps tcpdump");
            //read the output of ps
            BufferedReader br = new BufferedReader(new InputStreamReader(process2.getInputStream()));
            temp = br.readLine();
            temp = br.readLine();
            System.out.println("[+] reading process2 line: " + temp);
            //We apply a regexp to the second line of the ps output to get the pid
            temp = temp.replaceAll("^root *([0-9]*).*","$1");
            int pid = Integer.parseInt(temp);
            //the ps process is no more needed
            process2.destroy();

            //to kill tcpdump process we create a new process to run the kill command
            //this process terminate immediately so we don't need to kill it
            String killCommand = "kill "+ pid;
            Process process3 = Runtime.getRuntime().exec("su");
            DataOutputStream os2 = new DataOutputStream(process3.getOutputStream());
            os2.writeBytes(killCommand);
            os2.flush();
            os2.writeBytes("exit\n");
            os2.flush();
            os2.close();*/



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
        catch (InterruptedException e)
        {
            System.out.println("[-] 2");
        }

    }

    public static String sudoForResult(String...strings) {
        String res = "";
        DataOutputStream outputStream = null;
        InputStream response = null;
        try{
            Process su = Runtime.getRuntime().exec("su");
            outputStream = new DataOutputStream(su.getOutputStream());
            response = su.getInputStream();

            for (String s : strings) {
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
        byte[] buffer = new byte[1024];
        int length = 0;
        while ((length = is.read(buffer)) != -1) {
            baos.write(buffer, 0, length);
        }
        return baos.toString("UTF-8");
    }


}
