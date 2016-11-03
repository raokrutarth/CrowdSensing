package com.example.henry.test_sensors;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;


import android.content.Context;
import android.util.Log;

public class LogWriter {
	
	private static LogWriter instance = null; 
	private final static String TAG = "LogWriter";
	private static String filename;
	
	public LogWriter() {
	}
	
	public static LogWriter getInstance() {
		if (instance == null) {
			synchronized(LogWriter.class) {
				if (instance == null) {
					filename = LogWriter.generateFileName();
					instance = new LogWriter(); 
					Log.d(TAG, "filename = " + filename);
				}
			}
		}
		return instance;
	}

	public void write(Context ctx, String event, String msg) {
		String tmpMsg = "event="+ event + ", " + msg;
		Thread t = new Thread(new WriteTask(ctx, tmpMsg));
		t.start();	
	}
	
	public void write(Context ctx, String msg) {
		Thread t = new Thread(new WriteTask(ctx, msg));
		t.start();	
	}
	
	class WriteTask implements Runnable {
		Context ctx;
		String msg;
		
		public WriteTask(Context ctx, String msg) {
			this.ctx = ctx;
			this.msg = msg;
		}
		
        @Override
        public void run() {
            _write(ctx, msg);
        }
    }
	
	synchronized public void _write(Context ctx, String msg) {
		try {
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS z");
			dateFormat.setTimeZone(TimeZone.getTimeZone("EST"));
	        String currentTimeStamp = dateFormat.format(new Date());
			
	        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(ctx.openFileOutput(filename, Context.MODE_APPEND));
	        String tmp = currentTimeStamp + ": " + msg;
	        Log.d(TAG, tmp);
	        outputStreamWriter.write(tmp + "\n");
	        outputStreamWriter.close();
	    }
	    catch (IOException e) {
	        Log.e(TAG, "File write failed: " + e.toString());
	    } 
	}
	
	static public String generateFileName() {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        String currentTimeStamp = dateFormat.format(new Date());
        return "SenseAid_" + currentTimeStamp + ".log";
	}
}
