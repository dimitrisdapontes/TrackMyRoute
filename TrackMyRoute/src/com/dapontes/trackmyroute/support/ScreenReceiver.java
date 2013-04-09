package com.dapontes.trackmyroute.support;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Receiver able to say when the screen has turned off / on 
 * (http://thinkandroid.wordpress.com/2010/01/24/handling-screen-off-and-screen-on-intents/) 
 */
public class ScreenReceiver extends BroadcastReceiver
{
    public static boolean wasScreenOn = true;
    
    @Override
    public void onReceive(Context context, Intent intent)
    {
        if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF))
        {
            wasScreenOn = false;            
        }
        else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON))
        {
            wasScreenOn = true;
        }
    }
}
