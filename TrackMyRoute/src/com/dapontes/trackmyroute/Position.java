package com.dapontes.trackmyroute;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.os.Parcel;
import android.os.Parcelable;

public class Position implements Parcelable
{
	public long id;
	public double longitude;
	public double latitude;
	public Date trackTime;
	public long routeId;
	
	public Position(double longitude, double latitude)
	{
	    this.id = -1;
        this.longitude = longitude;
        this.latitude = latitude;
        this.trackTime = new Date();
        this.routeId = -1;
	}
	
	public Position(double longitude, double latitude, Date trackTime, long routeId)
    {       
        this.id = -1;
        this.longitude = longitude;
        this.latitude = latitude;
        this.trackTime = trackTime;
        this.routeId = routeId;
    }
	
	public Position(long id, double longitude, double latitude, Date trackTime, long routeId)
	{		
		this.id = id;
		this.longitude = longitude;
		this.latitude = latitude;
		this.trackTime = trackTime;
		this.routeId = routeId;
	}
	
	@Override
	public String toString()
	{
	    return 
            Double.toString(longitude) + " " + 
            Double.toString(latitude) + " " + 
            new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", new Locale("el")).format(trackTime);
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
        parcel.writeDouble(longitude);
        parcel.writeDouble(latitude);
        parcel.writeLong(trackTime.getTime());
        parcel.writeLong(routeId);
    }
    
    public static final Parcelable.Creator<Position> Creator = new Parcelable.Creator<Position>()
    {
        public Position createFromParcel(Parcel parcel)
        {
            long id = parcel.readLong();
            double longitude = parcel.readDouble();
            double latitude = parcel.readDouble();
            Date trackTime = new Date(parcel.readLong());
            long routeId = parcel.readLong();
            
            return new Position(id, longitude, latitude, trackTime, routeId);            
        }

        @Override
        public Position[] newArray(int size)
        {
            return new Position[size];
        }
    };
}
