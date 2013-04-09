package com.dapontes.trackmyroute.activities;

import java.text.NumberFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.Settings;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.dapontes.trackmyroute.Position;
import com.dapontes.trackmyroute.R;
import com.dapontes.trackmyroute.Route;
import com.dapontes.trackmyroute.database.DbContentProvider;
import com.dapontes.trackmyroute.database.PositionsTable;
import com.dapontes.trackmyroute.database.RoutesTable;
import com.dapontes.trackmyroute.support.Logger;
import com.dapontes.trackmyroute.support.ScreenReceiver;
import com.dapontes.trackmyroute.support.Utilities;
import com.bugsense.trace.BugSenseHandler;

public class Tracking extends FragmentActivity
{
    /**
     *  The tag used for logging
     */
    private static final String TAG = "Tracking";
    
    /**
     * Debug variable used to indicate whether the GPS or the NETWORK provided should be used
     */
    private static final boolean useGps = true;
    
    // elements    
    private RelativeLayout waitForSignalLayout;
    private LinearLayout trackingLayout;
    private TextView trackingTime;
    private TextView trackingDistance;
    private TextView trackingSpeed;
    private Button stopTracking;    
    
    /**
     * Activity result request codes
     */
    private static final int GPS_SETTINGS = 1;
    
    /**
     * The receiver used to lister to screen changes
     */
    private BroadcastReceiver receiver = null;
    
    /**
     * The id of the currently tracked route
     */
    private long currentRouteId = -1;
    
    /**
     * The start time (in milliseconds) when the tracking began
     */
    private long trackingStartTime =-1;
    
    /**
     * The total meters for the currently tracked route
     */
    private long trackRouteMeters = -1;   
    
    /**
     * Indicates whether the activity is resumed
     */
    private boolean isActivityResumed = false;
    
    /**
     * Timer used to count the time the route is being tracked
     * @see #startClockTimer()
     */
    private Timer routeClockTimer;
    
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        BugSenseHandler.initAndStartSession(this, "06c7982d");
        setContentView(R.layout.activity_tracking);
        Logger.write("OnCreate", TAG, this);
        
        // initialize the screen receiver (listen for screen off events)
        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        receiver = new ScreenReceiver();
        registerReceiver(receiver, filter);        
        
        // get activity controls
        waitForSignalLayout = (RelativeLayout) findViewById(R.id.wait_for_signal_layout);
        trackingLayout = (LinearLayout) findViewById(R.id.tracking_layout);    
        trackingTime = (TextView) findViewById(R.id.trackingTime);
        trackingDistance = (TextView) findViewById(R.id.trackingDistance);
        trackingSpeed = (TextView) findViewById(R.id.trackingSpeed);
        stopTracking = (Button)findViewById(R.id.stop_tracking);

        // element listeners
        stopTracking.setOnClickListener(onStopTrackingClicked);        
        
        // load the saved data
        if (savedInstanceState != null)
        {
            currentRouteId = savedInstanceState.getLong("currentRouteId");
            trackingStartTime = savedInstanceState.getLong("trackingStartTime");
            
            if (trackingStartTime > -1)
            {
                // a route is already being tracked, so start the clock immediately
                startClockTimer();
            }
        }
    }
    
    /**
     * Finalizes the tracking process by saving the route and displaying the route info activity
     */
    private OnClickListener onStopTrackingClicked = new OnClickListener()
    {
        @Override
        public void onClick(final View view)
        {
            // stop updates
            Log.i(TAG, "Stop Tracking - StopTracking");
            stopTracking();            
            
            // finalize route by updating endDate, totalMeters, totalSeconds
            Uri uri = Uri.parse(DbContentProvider.ROUTES_CONTENT_URI + "/" + currentRouteId);
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);                        
            Route currentRoute = null;
            
            if (cursor.moveToNext())
            {
                currentRoute = RoutesTable.getRoute(cursor);
                currentRoute.finalize(Tracking.this, new Date());
            }
            
            finish();
            
            if (currentRoute != null)
            {
                Intent intent = new Intent(view.getContext(), RouteInfo.class);
                intent.putExtra("selectedRoute", (Parcelable)currentRoute);
                view.getContext().startActivity(intent);
            }            
        }
    };
    
    @Override
    protected void onResume()
    {
        super.onResume();
        isActivityResumed = true;
        
        if (isGPSEnabled())
        {
            startTracking();
            startClockTimer();
            updateTrackingInfo(null);
        }
        else        
        {
            new AlertDialog.Builder(Tracking.this)
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
            .setNegativeButton("No", new DialogInterface.OnClickListener()
            {                    
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    // if not data have been recorded we exit the activity
                    // otherwise we show a confirmation message about saving / discarding the route
                    
                    if (currentRouteId == -1)
                    {
                        finish();
                    }
                    else
                    {
                        new AlertDialog.Builder(Tracking.this)
                        .setTitle("Save / Discard")
                        .setMessage("A route is already being tracked. Would you like to save or discard the route?")
                        .setPositiveButton("Save", new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                // stop updates
                                Log.i(TAG, "Stop Tracking - OnResume");
                                stopTracking();
                                
                                // update current route
                                String where = RoutesTable.COLUMN_ID + "=" + currentRouteId;
                                ContentValues values = new ContentValues();
                                values.put(RoutesTable.COLUMN_END_DATE, new Date().getTime());                    
                                getContentResolver().update(DbContentProvider.ROUTES_CONTENT_URI, values, where, null);
                                
                                finish();
                            }                       
                        })        
                        .setNegativeButton("Discard", new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                // delete current route
                                Uri uri = Uri.parse(DbContentProvider.ROUTES_CONTENT_URI  + "/" + Long.toString(currentRouteId));
                                Tracking.this.getContentResolver().delete(uri, null, null);
                                
                                finish();
                            }
                        }).show(); 
                    }
                }
            }).show();            
        }        
    }
    
    @Override
    protected void onPause() 
    {
        super.onPause();
        isActivityResumed = false;
        
        // clear the total meters, since the positions are inserted in the database
        // but the UI is not being updated. when the UI is resumed, the distance should be recalculated
        trackRouteMeters = -1;
    }
    
    @Override
    public void onBackPressed()
    {
        if (currentRouteId > -1)
        {
            new AlertDialog.Builder(this)
            .setMessage("A route is being tracked. Do you want to cancel this tracking?")
            .setTitle("Tracking Route In Progress")            
            .setPositiveButton("Yes", new DialogInterface.OnClickListener() 
            {
                @Override
                public void onClick(DialogInterface dialog, int which) 
                {
                    // delete current route
                    Uri uri = Uri.parse(DbContentProvider.ROUTES_CONTENT_URI  + "/" + Long.toString(currentRouteId));
                    Tracking.this.getContentResolver().delete(uri, null, null);
                    
                    // continue back pressed event
                    Tracking.super.onBackPressed();
                }
            })
            .setNegativeButton("No", null)                      
            .show();
        }
        else
        {
            super.onBackPressed();
        }
    }
    
    @Override
    protected void onDestroy()
    {
        Log.i(TAG, "Stop Tracking - OnDestroy");
        
        super.onDestroy();
        stopTracking();
        stopClockTimer();
        
        if (receiver != null)
        {
            unregisterReceiver(receiver);    
        }        
    }   
    
    /**
     * Starts the location tracking process by initializing the location listener
     */
    private void startTracking()
    {
        Logger.write("Start Tracking", TAG, this);
        
        if (currentRouteId > -1)
        {
            waitForSignalLayout.setVisibility(View.GONE);
            trackingLayout.setVisibility(View.VISIBLE);            
        }
        else
        {
            waitForSignalLayout.setVisibility(View.VISIBLE);
            trackingLayout.setVisibility(View.GONE);
        }
        
        LocationManager locationManager = (LocationManager)getSystemService(LOCATION_SERVICE);
        
        if (useGps)
        {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 10, locationListener);
        }
        else               
        {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 2000, 10, locationListener);
        }
    }
    
    /**
     * Stops the location tracking process by stopping the location listener
     */
    private void stopTracking()
    {        
        Logger.write("Stop tracking", TAG, this);
                
        LocationManager locationManager = (LocationManager)getSystemService(LOCATION_SERVICE);
        locationManager.removeUpdates(locationListener);
    }

    /**
     * The location listener used to track down the coordinates
     */
    private final LocationListener locationListener = new LocationListener()
    {
        // variable to hold whether the listener should be restarted after onProviderDisabled / onProviderEnabled
        boolean shouldRestart = false;
        
        public void onLocationChanged(Location location)
        {
            if (currentRouteId <= 0)
            {
                // show the tracking layout
                waitForSignalLayout.setVisibility(View.GONE);
                trackingLayout.setVisibility(View.VISIBLE);
                
                // create a new route
                ContentValues values = new ContentValues();
                values.put(RoutesTable.COLUMN_START_DATE, new Date().getTime());
                Uri getRouteUri = Tracking.this.getContentResolver().insert(DbContentProvider.ROUTES_CONTENT_URI, values);
                
                Cursor cursor = Tracking.this.getContentResolver().query(getRouteUri, null, null, null, null);
                if(cursor.moveToNext())
                {
                    Route route = RoutesTable.getRoute(cursor);
                    currentRouteId = route.id;
                    
                    // start the timer only if it is not already initialized (eg during screen transition)
                    if (trackingStartTime == -1)
                    {
                        trackingStartTime = new Date().getTime();
                        startClockTimer();
                    }
                }
            }
            
            Position position = new Position(location.getLongitude(), location.getLatitude(), new Date(location.getTime()), currentRouteId);
            
            // update the activity with the new point
            updateTrackingInfo(Utilities.locationFromCoordinates(position.longitude, position.latitude));
                        
            // write the position in the database
            ContentValues values = new ContentValues();
            values.put(PositionsTable.COLUMN_LONGITUDE, position.longitude);
            values.put(PositionsTable.COLUMN_LATITUDE, position.latitude);
            values.put(PositionsTable.COLUMN_TRACK_TIME, position.trackTime.getTime());
            values.put(PositionsTable.COLUMN_ROUTE_ID, position.routeId);
            Tracking.this.getContentResolver().insert(DbContentProvider.POSITIONS_CONTENT_URI, values);
                                              
            
            Logger.write(TAG, "locationListener-onLocationChanged", Tracking.this);
        }

        @Override
        public void onProviderDisabled(String provider)
        {
            Log.i(TAG, "Stop Tracking - OnProviderDisabled");
            stopTracking();
            shouldRestart = true;
        }

        @Override
        public void onProviderEnabled(String provider)
        {
            if (shouldRestart)
            {
                Log.i(TAG, "Start Tracking - OnProviderEnabled");
                LocationManager locationManager = (LocationManager)getSystemService(LOCATION_SERVICE);
                
                if (useGps)
                {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 10, locationListener);
                }
                else
                {
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 2000, 10, locationListener);
                }
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras)
        {
            Logger.write("LocationListener-OnStatusChanged (status=" + Integer.toString(status) + ")" , TAG, Tracking.this);
        } 
    };
    
    /**
     * Indicates whether the GPS provider is enabled on the device
     * @return True if the GPS provider is enabled, otherwise false
     */
    private boolean isGPSEnabled()
    {
        return ((LocationManager)getSystemService(LOCATION_SERVICE)).isProviderEnabled(LocationManager.GPS_PROVIDER);        
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) 
    {
        super.onActivityResult(requestCode, resultCode, data);
        
        switch(requestCode)
        {
            case GPS_SETTINGS:
                if (!isGPSEnabled())                
                {
                    waitForSignalLayout.setVisibility(View.INVISIBLE);
                    trackingLayout.setVisibility(View.INVISIBLE);
                }
                break;
        }
    };
    
    @Override
    public void onSaveInstanceState(Bundle saveInstanceState)
    {
        saveInstanceState.putLong("currentRouteId", currentRouteId);
        saveInstanceState.putLong("trackingStartTime", trackingStartTime);        
        stopClockTimer();
        
        // stop updates if anything but screen-off happened
        if (!ScreenReceiver.wasScreenOn)
        {
            Log.i(TAG, "Stop Tracking - OnSaveInstanceChange");
            stopTracking();
        }
    }
    
    /**
     * Initializes the timer that counts the timer the route is being tracked
     */
    private void startClockTimer()
    {
        if (!isActivityResumed)
        {
            // no need to start the timer, since the activity is paused
            return;
        }
        
        routeClockTimer = new Timer();
        routeClockTimer.scheduleAtFixedRate(new TimerTask()
        {
            @Override
            public void run()
            {
                // cancelling the timer outside the timer thread does not work, 
                // so we check if the timer has been set to null, to stop the loop
                // from within the timer
                
                if (routeClockTimer == null)
                {
                    this.cancel();
                }
                else
                {
                    Tracking.this.runOnUiThread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            trackingTime.setText(Utilities.ClockFormatTimeSpan(new Date(trackingStartTime), new Date()));
                        }
                    });    
                }
            }
        }, 1000, 1000);
    }
    
    /**
     * Stops the timer that counts the timer the route is being tracked
     */
    private void stopClockTimer()
    {
        if (routeClockTimer != null)
        {
            routeClockTimer.cancel();
            routeClockTimer = null;
        }
    }
    
    /**
     * Updates the tracking activity (distance / avgSpeed) with the new location
     * Make sure to call this before inserting the newLocation to the database, since the previous
     * position of the route is searched for to calculate the distance between that and the newLocation
     * @param newLocation The new location for the current route
     */
    private void updateTrackingInfo(Location newLocation)
    {
        if (!isActivityResumed)
        {
            // the activity is paused
            return;
        }
        
        if (currentRouteId < 0)
        {
            // no route being tracked
            return;
        }        
        
        if (trackRouteMeters < 0)
        {
            // calculate the route meters from the database
            Uri uri = Uri.parse(DbContentProvider.ROUTE_POSITIONS_CONTENT_URI  + "/" + Long.toString(currentRouteId));            
            Cursor positionsCursor = this.getContentResolver().query(uri, null, null, null, PositionsTable.COLUMN_TRACK_TIME + " ASC");
            
            Location previousLocation = null;
            while(positionsCursor.moveToNext())
            {
                Position position = PositionsTable.getPosition(positionsCursor);
                Location currentLocation = Utilities.locationFromCoordinates(position.longitude, position.latitude);
                
                if (previousLocation != null)
                {
                    trackRouteMeters += previousLocation.distanceTo(currentLocation);                    
                }                
                
                previousLocation = currentLocation;
            }                       
        }
        
        if (newLocation != null)
        {
            // if a location is provided, count the new location in the total meters
            // (need to get the previous point for this route from the database
            Uri uri = Uri.parse(DbContentProvider.ROUTE_POSITIONS_CONTENT_URI  + "/" + Long.toString(currentRouteId));
            Cursor cursor = this.getContentResolver().query(uri,  null, null, null, PositionsTable.COLUMN_TRACK_TIME + " DESC");
            if (cursor.moveToNext())
            {
                // check that the provided location is not the last one in the database
                Position position = PositionsTable.getPosition(cursor);
                if (position.longitude != newLocation.getLongitude() || position.latitude != newLocation.getLatitude())
                {
                    Location previousLocation = Utilities.locationFromCoordinates(position.longitude, position.latitude);
                    trackRouteMeters += previousLocation.distanceTo(newLocation);
                }
            }
        }
        
        // update the views in the activity
        String meters = (trackRouteMeters < 0 ? "0" : NumberFormat.getInstance().format(trackRouteMeters)) +  " " + this.getResources().getString(R.string.meters);
        String avgSpeed = "-";
        if (trackRouteMeters > 0 && newLocation != null)        
        {
            // (meters / second) * 3.6 = km / h            
            double metersPerSecond  = 0;
            double kilometersPerHour = 0;
            long now = new Date().getTime();
            
            if (now != trackingStartTime)
            {
                metersPerSecond = (double)trackRouteMeters / (now / 1000 - trackingStartTime / 1000);
                kilometersPerHour = metersPerSecond * 3.6;
                avgSpeed = NumberFormat.getInstance().format(kilometersPerHour) + " " + this.getResources().getString(R.string.kilometersperhour_short);
            }
        }
        
        trackingDistance.setText(meters);
        trackingSpeed.setText(avgSpeed);
    }
}