package com.dapontes.trackmyroute.adapters;

import java.util.List;

import com.dapontes.trackmyroute.Position;
import com.dapontes.trackmyroute.R;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class PositionsAdapter extends ArrayAdapter<Position>
{
    private final LayoutInflater inflater;
    
    public PositionsAdapter(Context context)
    {
        super(context, android.R.layout.simple_expandable_list_item_2);
        inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
    
    public void setData(List<Position> positions)
    {
        clear();
        if (positions != null)
        {
            for (int i=0; i<positions.size(); i++)
            {
                add(positions.get(i));
            }
        }
    }
    
    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        View view = (convertView == null ? inflater.inflate(R.layout.position_row, parent, false) : convertView);
        
        Position item = getItem(position);        
        ((TextView)view.findViewById(R.id.positionLabel)).setText(item.toString());
        
        return view;
    }
    
    public Position getCentroid()
    {
        double minX = Double.MAX_VALUE, minY = Double.MAX_VALUE, maxX = Double.MIN_VALUE, maxY = Double.MIN_VALUE;
        
        for(int i=0; i<getCount(); i++)
        {
            Position current = getItem(i);
            
            minX = Math.min(minX, current.longitude);
            minY = Math.min(minY, current.latitude);
            maxX = Math.max(maxX,  current.longitude);
            maxY = Math.max(maxY, current.latitude);
        }
        
        return new Position((minX + maxX) / 2, (minY + maxY) / 2);
    }
}
