package com.dapontes.trackmyroute.loaders;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.content.AsyncTaskLoader;

import com.dapontes.trackmyroute.Position;
import com.dapontes.trackmyroute.database.DbContentProvider;
import com.dapontes.trackmyroute.database.PositionsTable;

public class PositionsLoader extends AsyncTaskLoader<List<Position>>
{
    List<Position> positions;
    Context context = null;
    long routeId = 0;       
    
    public PositionsLoader(Context context, long routeId)
    {
        super(context);
        this.context = context;
        this.routeId = routeId;
    }
    
    /**
     * This is where the bulk of our work is done.  This function is
     * called in a background thread and should generate a new set of
     * data to be published by the loader.
     */
    @Override
    public List<Position> loadInBackground()
    {
        Uri uri = Uri.parse(DbContentProvider.ROUTE_POSITIONS_CONTENT_URI  + "/" + Long.toString(routeId));
        
        Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);        
        List<Position> positions = new ArrayList<Position>();
        
        while(cursor.moveToNext())
        {
            positions.add(PositionsTable.getPosition(cursor));
        }
        
        return positions;        
    }
    
    /**
     * Called when there is new data to deliver to the client.  The
     * super class will take care of delivering it; the implementation
     * here just adds a little more logic.
     */
    @Override
    public void deliverResult(List<Position> listOfData)
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
        
        List<Position> previousPositions = listOfData;
        positions = listOfData;
        
        if (isStarted())
        {
            // If the Loader is currently started, we can immediately
            // deliver its results.
            super.deliverResult(listOfData);
        }
        
        // At this point we can release the resources associated with
        // 'oldApps' if needed; now that the new result is delivered we
        // know that it is no longer in use.
        if (previousPositions != null)
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
        if (positions != null)
        {
            // If we currently have a result available, deliver it immediately.
            deliverResult(positions);
        }
        
        if (takeContentChanged() || positions == null)
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
    public void onCanceled(List<Position> data)
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
        if (positions != null)
        {
            onReleaseResources(positions);
            positions = null;
        }
    }
    
    /**
     * Helper function to take care of releasing resources associated
     * with an actively loaded data set.
     */
    protected void onReleaseResources(List<Position> routes)
    {
        // For a simple List<> there is nothing to do.  For something
        // like a Cursor, we would close it here.

    }
}
