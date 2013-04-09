package com.dapontes.trackmyroute.adapters;

import java.text.NumberFormat;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.dapontes.trackmyroute.R;
import com.dapontes.trackmyroute.Route;
import com.dapontes.trackmyroute.support.Utilities;

public class RoutesAdapter extends ArrayAdapter<Route>
{
    private final LayoutInflater inflater;
    
    public RoutesAdapter(Context context)
    {
        super(context, android.R.layout.simple_expandable_list_item_2); 
        inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
    
    public void setData(List<Route> routes)
    {
        clear();
        if (routes != null)
        {
            for (int i=0; i<routes.size(); i++)
            {
                add(routes.get(i));
            }
        }
    }
    
    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        View view = (convertView == null ? inflater.inflate(R.layout.route_row, parent, false) : convertView);        
        Route route = getItem(position);
        
        String title = Utilities.DateToText(route.startDate) + "\n" + Utilities.TimeToText(route.startDate);
        String time = Utilities.VerbalizeTimeSpan(route.startDate, route.endDate, view.getContext().getResources(), true);
        String distance = NumberFormat.getInstance().format(route.totalMeters) + view.getContext().getResources().getString(R.string.meters_short);              
        
        ((TextView)view.findViewById(R.id.row_title)).setText(title);
        ((TextView)view.findViewById(R.id.row_time)).setText(time);
        ((TextView)view.findViewById(R.id.row_distance)).setText(distance);
        
        return view;
    }
}
