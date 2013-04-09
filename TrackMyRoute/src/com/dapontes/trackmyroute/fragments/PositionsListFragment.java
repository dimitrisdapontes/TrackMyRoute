package com.dapontes.trackmyroute.fragments;

import java.util.List;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import com.dapontes.trackmyroute.Position;
import com.dapontes.trackmyroute.R;
import com.dapontes.trackmyroute.adapters.PositionsAdapter;
import com.dapontes.trackmyroute.loaders.PositionsLoader;

public class PositionsListFragment extends ListFragment implements LoaderManager.LoaderCallbacks<List<Position>>
{
    private static final String TAG = "PositionsListFragment";
    
    private PositionsAdapter adapter;
    private long routeId;
    
    @Override
    public Loader<List<Position>> onCreateLoader(int id, Bundle args)
    {
        return new PositionsLoader(getActivity(), this.routeId);
    }

    @Override
    public void onLoadFinished(Loader<List<Position>> loader, List<Position> data)
    {
        adapter.setData(data);
        
        if (isResumed())
        {
            setListShown(true);
        }
        else
        {
            setListShownNoAnimation(true);
        }
    }

    @Override
    public void onLoaderReset(Loader<List<Position>> loader)
    {
        adapter.setData(null);
    }
    
    @Override
    public void onListItemClick(ListView l, View v, int position, long id)
    {
        super.onListItemClick(l, v, position, id);
        
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {     
        super.onActivityCreated(savedInstanceState);
        
        setEmptyText(getString(R.string.no_routes));
        
        // create a routes adapter to show the saved routes
        adapter = new PositionsAdapter(getActivity());
        setListAdapter(adapter);
    }      
    
    public void loadRoutePositions(long routeId)
    {
        this.routeId = routeId;
        
        setListShown(false);              
        getLoaderManager().initLoader(0, null, this);
    }
    
    public void addPosition(Position position)
    {
        Log.i(TAG, "adding position");
        adapter.add(position);
        adapter.notifyDataSetChanged();
    }
}
