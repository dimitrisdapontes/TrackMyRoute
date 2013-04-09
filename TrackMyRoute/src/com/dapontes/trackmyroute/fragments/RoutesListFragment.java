package com.dapontes.trackmyroute.fragments;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.dapontes.trackmyroute.R;
import com.dapontes.trackmyroute.Route;
import com.dapontes.trackmyroute.activities.RouteInfo;
import com.dapontes.trackmyroute.adapters.RoutesAdapter;
import com.dapontes.trackmyroute.database.DbContentProvider;
import com.dapontes.trackmyroute.loaders.RoutesLoader;

public class RoutesListFragment extends ListFragment implements LoaderManager.LoaderCallbacks<List<Route>>
{
    RoutesAdapter adapter;
    
    @Override
    public Loader<List<Route>> onCreateLoader(int id, Bundle args)
    {
        return new RoutesLoader(getActivity());
    }

    @Override
    public void onLoadFinished(Loader<List<Route>> loader, List<Route> data)
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
    public void onLoaderReset(Loader<List<Route>> loader)
    {
        adapter.setData(null);
    }
    
    @Override
    public void onListItemClick(ListView l, View v, int position, long id)
    {
        //super.onListItemClick(l, v, position, id);
        Route selectedRoute = (Route)l.getItemAtPosition(position);
        
        Intent intent = new Intent(v.getContext(), RouteInfo.class);
        intent.putExtra("selectedRoute", (Parcelable)selectedRoute);
        v.getContext().startActivity(intent);
    }    
    
    public AdapterView.OnItemLongClickListener onListItemLongClickListener = new AdapterView.OnItemLongClickListener()
    {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id)
        {
            final Route selectedRoute = (Route)parent.getItemAtPosition(position);
            final String date = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", new Locale("el")).format(selectedRoute.startDate);
            
            AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
            builder.setTitle("Delete Route");
            builder.setMessage("Do you want to delete Route " + date + " ?");
            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    Uri uri = Uri.parse(DbContentProvider.ROUTES_CONTENT_URI  + "/" + Long.toString(selectedRoute.id));
                    getActivity().getContentResolver().delete(uri, null, null);
                    
                    // delete the route and refresh the adapter
                    adapter.remove(selectedRoute);
                    adapter.notifyDataSetChanged();
                }                       
            });         
            builder.setNegativeButton("No", null);
            builder.show();
            return true;
        }        
    };

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {     
        super.onActivityCreated(savedInstanceState);
        
        setEmptyText(getString(R.string.no_routes));
        
        // setup long click (for deleting route)
        getListView().setOnItemLongClickListener(onListItemLongClickListener);                
        
        // create a routes adapter to show the saved routes
        adapter = new RoutesAdapter(getActivity());
        setListAdapter(adapter);
        
        // start out with a progress indicator.
        setListShown(false);

        // prepare the loader.  Either re-connect with an existing one, or start a new one.
        getLoaderManager().initLoader(0, null, this);
    }
}
