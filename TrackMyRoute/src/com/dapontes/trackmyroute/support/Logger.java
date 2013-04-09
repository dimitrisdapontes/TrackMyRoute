package com.dapontes.trackmyroute.support;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

public class Logger
{
	private final static String LOG_FILE_NAME = "trackmyroute.txt";
	
	public static void write(String message, String source, Context context)
	{
		File logFile = new File(context.getFilesDir() + "/" + LOG_FILE_NAME);		
		if (!logFile.exists())
		{
			try			
			{
				logFile.createNewFile();
			}
			catch(IOException ex)
			{
				Log.e("Logger.write", "Error while creating new log file. Ex=" + ex.toString());
				return;
			}
		}
		
		try
		{
			String currentTime = (new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", new Locale("el"))).format(new Date());
			
			// BufferedWriter for performance, true to set append to file flag
			BufferedWriter buffer = new BufferedWriter(new FileWriter(logFile, true));
			buffer.append(currentTime + "\t" + source + "\t" + message);
			buffer.newLine();
			buffer.close();
		}
		catch(IOException ex)
		{
			Log.e("Logger.write", "Error while writing message to log file. Ex=" + ex.toString());		
		}		
	}
	
	public static void dumpLog(Context context)
	{
		File logFile = new File(context.getFilesDir() + "/" + LOG_FILE_NAME);
		File sdCopy = new File(Environment.getExternalStorageDirectory() + "/" + LOG_FILE_NAME);
		
		if (sdCopy.exists())
		{
			try			
			{
				sdCopy.delete();
			}
			catch(Exception ex)
			{
				Log.e("Logger.dumpLog", "Unable to delete previous log. Ex=" + ex.toString());
				return;
			}
		}
		
		FileInputStream srcStream = null;
		FileOutputStream dstStream = null;
		
		try
		{			
			srcStream = new FileInputStream(logFile);
			dstStream = new FileOutputStream(sdCopy);
						
			FileChannel srcChannel = srcStream.getChannel();
            FileChannel dstChannel = dstStream.getChannel();
            dstChannel.transferFrom(srcChannel, 0, srcChannel.size());
            srcChannel.close();
            dstChannel.close();
		}
		catch(Exception ex)
		{
			Log.e("Logger.dumpLog", "Error while dumping log file. Ex=" + ex.toString());
			Toast.makeText(context, "Unable to dump log", Toast.LENGTH_LONG).show();
		}
		finally
		{
			try
			{
				if (srcStream != null)
				{
					srcStream.close();
				}
				
				if (dstStream != null)
				{
					dstStream.close();
				}
			}
			catch(IOException ex)
			{
				Log.e("Logger.dumpLog", "Error while closing opened streams");
				Toast.makeText(context, "Unable to dump log", Toast.LENGTH_LONG).show();
				return;
			}
		}
		
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setDataAndType(Uri.fromFile(sdCopy), "text/plain");
		context.startActivity(intent);
	}
}
