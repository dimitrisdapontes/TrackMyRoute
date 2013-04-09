package com.dapontes.trackmyroute.support;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.location.Location;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import com.dapontes.trackmyroute.R;
import com.google.android.gms.maps.model.LatLng;

public class Utilities
{
    private final static String DUMP_DATABASE_NAME = "trackmyroute_dump.db";
    
    private static void mailDatabase(Context context)
    {
        Intent i = new Intent(Intent.ACTION_SEND);              
        i.setType("message/rfc822");
        i.putExtra(Intent.EXTRA_EMAIL, new String[]{""});
        i.putExtra(Intent.EXTRA_SUBJECT, "Database Dump");
        i.putExtra(Intent.EXTRA_TEXT, "Database Dump");
        i.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + Environment.getExternalStorageDirectory() + "/" + DUMP_DATABASE_NAME));
        
        try     
        {
            context.startActivity(Intent.createChooser(i, "Send mail..."));
        }
        catch(Exception ex)
        {
            Log.e("sendDatabaseEmail", "Error. Ex=" + ex.toString());
            Logger.write("Unable to mail database. Ex=" + ex.toString(), "Utilities-mailDatabase", context);
        }
    }
    
    public static void dumpDatabase(Context context)
    {
        try 
        {
            File sd = Environment.getExternalStorageDirectory();
            File data = Environment.getDataDirectory();

            if (sd.canWrite()) 
            {
                String currentDBPath = "/data/com.dapontes.trackmyroute/databases/trackmyroute.db";
                String backupDBPath = DUMP_DATABASE_NAME;
                File currentDB = new File(data, currentDBPath);
                File backupDB = new File(sd, backupDBPath);

                if (currentDB.exists()) 
                {
                    if (backupDB.exists())
                    {
                        backupDB.delete();
                    }
                    
                    FileInputStream inputStream = new FileInputStream(currentDB);
                    FileOutputStream outputStream = new FileOutputStream(backupDB);
                    
                    FileChannel src = inputStream.getChannel();
                    FileChannel dst = outputStream.getChannel();                    
                    dst.transferFrom(src, 0, src.size());
                    src.close();
                    dst.close();
                    
                    inputStream.close();
                    outputStream.close();
                    
                    mailDatabase(context);
                }
                else
                {
                    Logger.write("Unable to locate current database", "Utilities-dumpDatabase", context);
                }
            }               
        } 
        catch (Exception e) 
        {
            Log.w("Settings Backup", e);
        }
    }
           
    public final static String VerbalizeTimeSpan(Date d1, Date d2, Resources resources, boolean shortNames)
    {
        long diff = d2.getTime() - d1.getTime();
        long diffSeconds = diff / 1000 % 60;
        long diffMinutes = diff / (60 * 1000) % 60;
        long diffHours = diff / (60 * 60 * 1000) % 60;               
        
        String response = "";
        if (diffHours > 0)
        {
            response += Long.toString(diffHours) + " " + (shortNames ? resources.getString(R.string.hours_short) : resources.getString(R.string.hours));
        }
        
        if (diffMinutes > 0)
        {
            response += " " + Long.toString(diffMinutes) + " " + (shortNames ? resources.getString(R.string.minutes_short) : resources.getString(R.string.minutes));
        }
        
        if (diffSeconds > 0)
        {
            response += " " + Long.toString(diffSeconds) + " " + (shortNames ? resources.getString(R.string.seconds_short_short) : resources.getString(R.string.seconds));
        }
        
        return response;
    }
    
    public final static String ClockFormatTimeSpan(Date d1, Date d2)
    {
        long diff = d2.getTime() - d1.getTime();
        long diffSeconds = diff / 1000 % 60;
        long diffMinutes = diff / (60 * 1000) % 60;
        long diffHours = diff / (60 * 60 * 1000) % 60;
        
        String response = "";
        if (diffHours > 0)
        {
            response += String.format("%02d", diffHours);            
        }
        else
        {
            response += "00";
        }        
        
        response += ":";
        
        if (diffMinutes > 0)
        {
            response += String.format("%02d", diffMinutes);
        }
        else
        {
            response += "00";
        }        
        
        response += ":";
        
        if (diffSeconds > 0)
        {            
            response += String.format("%02d", diffSeconds);
        }
        else
        {
            response += "00";
        }
        
        return response;
    }

    public final static Location locationFromLatLng(LatLng latLng)
    {
        Location location = new Location("");
        location.setLatitude(latLng.latitude);
        location.setLongitude(latLng.longitude);
        return location;
    }

    public final static Location locationFromCoordinates(double x, double y)
    {
        Location location = new Location("");
        location.setLongitude(x);
        location.setLatitude(y);        
        return location;
    }

    public final static String DateToText(Date date)
    {
        return new SimpleDateFormat("dd/MM/yyyy", new Locale("el")).format(date);
    }
    
    public final static String TimeToText(Date date)
    {
        return new SimpleDateFormat("HH:mm:ss", new Locale("el")).format(date);
    }
}
