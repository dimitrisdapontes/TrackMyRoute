package com.dapontes.trackmyroute.activities;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import com.dapontes.trackmyroute.R;
import com.bugsense.trace.BugSenseHandler;
// http://mobile.tutsplus.com/tutorials/android/android-sdk_loading-data_cursorloader/
// http://stackoverflow.com/questions/7182485/usage-cursorloader-without-contentprovider/7422343#7422343

public class Routes extends FragmentActivity
{
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{	 
	    super.onCreate(savedInstanceState);
	    BugSenseHandler.initAndStartSession(this, "06c7982d");
	    setContentView(R.layout.activity_routes);	    	   
	}
}