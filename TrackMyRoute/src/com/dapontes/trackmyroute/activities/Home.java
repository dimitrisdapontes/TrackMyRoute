package com.dapontes.trackmyroute.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bugsense.trace.BugSenseHandler;
import com.dapontes.trackmyroute.R;
import com.dapontes.trackmyroute.support.Logger;
import com.dapontes.trackmyroute.support.Utilities;

public class Home extends Activity
{
	// the tag used for logging
	private static final String TAG = "Home";
	
	// views
	private Button startTracking;
	private Button myRoutes;
	private TextView gpsStatusText;
	private ImageView gpsStatusImage;
	private ImageView infoImage;
	
	// activity for result request codes
    private static final int GPS_SETTINGS = 1;
    private static final int TOGGLE_GPS_SETTINGS = 2;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		BugSenseHandler.initAndStartSession(this, "06c7982d");
		setContentView(R.layout.activity_home);
		Logger.write(TAG, "OnCreate", this);		
		
		// get activity controls
		startTracking = (Button)findViewById(R.id.start_tracking);
		myRoutes = (Button)findViewById(R.id.my_routes);
		gpsStatusText = (TextView)findViewById(R.id.gps_status_text);
		gpsStatusImage = (ImageView)findViewById(R.id.gps_status_image);
		infoImage = (ImageView)findViewById(R.id.info_image);
		
		// set control events
		startTracking.setOnClickListener(onStartTrackingClicked);
		myRoutes.setOnClickListener(onMyRoutesClicked);
		gpsStatusText.setOnClickListener(toggleGpsStatus);
		gpsStatusImage.setOnClickListener(toggleGpsStatus);
		infoImage.setOnClickListener(showInfo);
				
		// change activity background color
		View root = startTracking.getRootView();
		root.setBackgroundColor(getResources().getColor(R.color.activity_background));
		
		//this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		refreshLocationProviderStatus();
	}
	
	@Override
	protected void onResume()
	{
		Logger.write("Events", "OnResume", this);
		super.onResume();		
		refreshLocationProviderStatus();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.activity_home, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch(item.getItemId())
		{
			case R.id.menu_dump_database:
			    Utilities.dumpDatabase(this);
			    return true;
		}		
		return super.onOptionsItemSelected(item);
	}
	
	/*
	 * Returns true if the gps service is enabled in the device
	 */
	private boolean isGPSEnabled()
	{
		return ((LocationManager)getSystemService(LOCATION_SERVICE)).isProviderEnabled(LocationManager.GPS_PROVIDER);
	}

	/*
	 * Refreshes the message in the activity that indicates whether the gps service is enabled 
	 */
	private void refreshLocationProviderStatus()
	{
		gpsStatusText.setText(isGPSEnabled() ? R.string.gps_enabled : R.string.gps_disabled);
		gpsStatusImage.setImageResource(isGPSEnabled() ? R.drawable.satellite_dish : R.drawable.satellite_dish_disabled);		
	}
	
	private OnClickListener onStartTrackingClicked = new OnClickListener()
	{
	    @Override
	    public void onClick(final View view)
	    {
	        if (!isGPSEnabled())
	        {
	            new AlertDialog.Builder(view.getContext())
	            .setTitle("No GPS enabled")
	            .setMessage("Would you like to activate GPS?")
	            .setPositiveButton("Yes", new DialogInterface.OnClickListener()
	            {
	                @Override
	                public void onClick(DialogInterface dialog, int which)
	                {
	                    startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), GPS_SETTINGS);                                 
	                }                       
	            })         
	            .setNegativeButton("No", null)
	            .show(); 
	        }
	        else
	        {
	            Intent intent = new Intent(view.getContext(), Tracking.class);
	            view.getContext().startActivity(intent);
	        }
	    }
	};
	
	/**
	 * Click handler for myRoutes button
	 * Starts a new activity to show all the routes
	 */
	private OnClickListener onMyRoutesClicked = new OnClickListener()
	{		
		@Override
		public void onClick(final View view)
		{
			Intent intent = new Intent(view.getContext(), Routes.class);
			view.getContext().startActivity(intent);
		}
	};
	
	private OnClickListener toggleGpsStatus = new OnClickListener()
	{
	    @Override
	    public void onClick(final View view)
	    {
	        startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), TOGGLE_GPS_SETTINGS);
	    }
	};
	
	private OnClickListener showInfo = new OnClickListener()
	{
	    @Override
	    public void onClick(final View view)
	    {
	        Intent intent = new Intent(view.getContext(), Information.class);
	        view.getContext().startActivity(intent);
	    }
	};
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) 
	{
	    super.onActivityResult(requestCode, resultCode, data);
	    
	    switch(requestCode)
        {
            case GPS_SETTINGS:
                if (isGPSEnabled())
                {
                    Intent intent = new Intent(this, Tracking.class);
                    this.startActivity(intent);
                }
                break;
            
            case TOGGLE_GPS_SETTINGS:
                refreshLocationProviderStatus();
                break;
        }
	};
}
