package edu.purdue.dcsl.crowdsensingclient;

import android.util.Log;

import java.io.Closeable;
import java.net.DatagramSocket;
import java.net.Socket;

/**
 * Created by raok on 11/21/16.
 */

public class Closer
{
    public static void closeSilently(Object... xs) {
        // Note: on Android API levels prior to 19 Socket does not implement Closeable
        for (Object x : xs) {
            if (x != null) {
                try {
                    if (x instanceof Closeable) {
                        ((Closeable)x).close();
                    } else if (x instanceof Socket) {
                        ((Socket)x).close();
                    } else if (x instanceof DatagramSocket) {
                        ((DatagramSocket)x).close();
                    } else {

                        throw new RuntimeException("cannot close "+x);
                    }
                } catch (Throwable e)
                {
                    System.out.println("[-] Error2 in Closer");
                }
            }
        }
    }
}
