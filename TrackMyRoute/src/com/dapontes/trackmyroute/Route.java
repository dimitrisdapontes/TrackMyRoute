package com.dapontes.trackmyroute;

import java.util.Date;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import com.dapontes.trackmyroute.database.DbContentProvider;
import com.dapontes.trackmyroute.database.PositionsTable;
import com.dapontes.trackmyroute.database.RoutesTable;
import com.dapontes.trackmyroute.support.Utilities;

public class Route implements Parcelable
{
	public long id;
	public Date startDate;
	public Date endDate;
	public int totalMeters;
	public int totalSeconds;
	
	public Route(long id, Date startDate, Date endDate, int totalMeters, int totalSeconds)
	{		
		this.id = id;
		this.startDate = startDate;
		this.endDate = endDate;
		this.totalMeters = totalMeters;
		this.totalSeconds = totalSeconds;
	}
	
	/**
	 * Finalizes the route by updating the record in the database
	 * @param context The current context
	 * @param endDate The end date of the route
	 */
	public void finalize(Context context, Date endDate)
	{
        // count the total meters of the route
        Uri uri = Uri.parse(DbContentProvider.ROUTE_POSITIONS_CONTENT_URI  + "/" + id);
        Cursor positionCursor = context.getContentResolver().query(uri, null, null, null, null);
        
        int totalMeters = 0;            
        Location previousLocation = null;            
        while (positionCursor.moveToNext())
        {
            Position position = PositionsTable.getPosition(positionCursor);
            Location currentLocation = Utilities.locationFromCoordinates(position.longitude, position.latitude);
            
            if (previousLocation != null)
            {
                totalMeters += previousLocation.distanceTo(currentLocation);
            }
            
            previousLocation = currentLocation; 
        }
        positionCursor.close();
        
        // update the record in the database
        String where = RoutesTable.COLUMN_ID + "=" + id;
        
        ContentValues values = new ContentValues();
        values.put(RoutesTable.COLUMN_END_DATE, endDate.getTime());
        values.put(RoutesTable.COLUMN_TOTAL_SECONDS, (endDate.getTime() - startDate.getTime()));
        values.put(RoutesTable.COLUMN_TOTAL_METERS, totalMeters);
        
        context.getContentResolver().update(DbContentProvider.ROUTES_CONTENT_URI, values, where, null);
	}

    @Override
    public int describeContents()
    {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags)
    {
        parcel.writeLong(id);
        parcel.writeLong(startDate.getTime());
        parcel.writeLong(endDate.getTime());
        parcel.writeInt(totalMeters);
        parcel.writeInt(totalSeconds);
    }
    
    public static final Parcelable.Creator<Route> CREATOR = new Parcelable.Creator<Route>()
    {
        public Route createFromParcel(Parcel parcel)
        {
            long id = parcel.readLong();
            Date startDate = new Date(parcel.readLong());
            Date endDate = new Date(parcel.readLong());
            int totalMeters = parcel.readInt();
            int totalSeconds = parcel.readInt();
            
            return new Route(id, startDate, endDate, totalMeters, totalSeconds);            
        }

        @Override
        public Route[] newArray(int size)
        {
            return new Route[size];
        }
    };
}
