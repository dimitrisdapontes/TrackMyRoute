package com.dapontes.trackmyroute.loaders;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.content.AsyncTaskLoader;

import com.dapontes.trackmyroute.Route;
import com.dapontes.trackmyroute.database.DbContentProvider;
import com.dapontes.trackmyroute.database.RoutesTable;

public class RoutesLoader extends AsyncTaskLoader<List<Route>>
{
    List<Route> routes;
    Context context;
    
    public RoutesLoader(Context context)
    {
        super(context);
        this.context = context;
    }       
    
    /**
     * This is where the bulk of our work is done.  This function is
     * called in a background thread and should generate a new set of
     * data to be published by the loader.
     */
    @Override
    public List<Route> loadInBackground()
    {        
        Cursor cursor = context.getContentResolver().query(DbContentProvider.ROUTES_CONTENT_URI, null, null, null, null);        
        List<Route> routes = new ArrayList<Route>();
        
        while(cursor.moveToNext())
        {
            routes.add(RoutesTable.getRoute(cursor));
        }
        
        return routes;        
    }
    
    /**
     * Called when there is new data to deliver to the client.  The
     * super class will take care of delivering it; the implementation
     * here just adds a little more logic.
     */
    @Override
    public void deliverResult(List<Route> listOfData)
    {
        if (isReset())
        {
            // An async query came in while the loader is stopped.  We
            // don't need the result.
            if (listOfData != null)
            {
                onReleaseResources(listOfData);
            }
        }
        
        List<Route> previousRoutes = listOfData;
        routes = listOfData;
        
        if (isStarted())
        {
            // If the Loader is currently started, we can immediately
            // deliver its results.
            super.deliverResult(listOfData);
        }
        
        // At this point we can release the resources associated with
        // 'oldApps' if needed; now that the new result is delivered we
        // know that it is no longer in use.
        if (previousRoutes != null)
        {
            onReleaseResources(listOfData);
        }        
    }
    
    /**
     * Handles a request to start the Loader.
     */
    @Override
    protected void onStartLoading()
    {
        if (routes != null)
        {
            // If we currently have a result available, deliver it immediately.
            deliverResult(routes);
        }
        
        if (takeContentChanged() || routes == null)
        {
            // If the data has changed since the last time it was loaded
            // or is not currently available, start a load.
            forceLoad();
        }
    }
    

    /**
     * Handles a request to stop the Loader.
     */
    @Override
    protected void onStopLoading()
    {
        // Attempt to cancel the current load task if possible.
        cancelLoad();
    }
    
    /**
     * Handles a request to cancel a load.
     */
    @Override
    public void onCanceled(List<Route> data)
    {
        super.onCanceled(data);
        
        // At this point we can release the resources associated with 'apps'
        // if needed.
        onReleaseResources(data);
    }
    
    /**
     * Handles a request to completely reset the Loader.
     */
    @Override
    protected void onReset()
    {
        super.onReset();
        
        onStopLoading();
        
        // At this point we can release the resources associated with 'routes' if needed.
        if (routes != null)
        {
            onReleaseResources(routes);
            routes = null;
        }
    }
    
    /**
     * Helper function to take care of releasing resources associated
     * with an actively loaded data set.
     */
    protected void onReleaseResources(List<Route> routes)
    {
        // For a simple List<> there is nothing to do.  For something
        // like a Cursor, we would close it here.

    }
}
