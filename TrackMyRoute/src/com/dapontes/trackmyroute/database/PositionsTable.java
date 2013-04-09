package com.dapontes.trackmyroute.database;

import java.util.Date;

import com.dapontes.trackmyroute.Position;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class PositionsTable
{
	protected static final String TAG = "PositionsTable";
	
	// table name
	public static final String DATABASE_TABLE = "positions";
	
	// table column names
	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_TRACK_TIME = "tracktime";
	public static final String COLUMN_LONGITUDE = "longitude";
	public static final String COLUMN_LATITUDE = "latitude";
	public static final String COLUMN_ROUTE_ID = "routeid";
	
	// table column indexes
	static final int INDEX_ID = 0;
	static final int INDEX_WHEN = 1;
	static final int INDEX_LONGITUDE = 2;
	static final int INDEX_LATITUDE = 3;
	static final int INDEX_ROUTE_ID = 4;
	
	protected final static String CREATE_TABLE_POSITIONS = 
		"CREATE TABLE " + DATABASE_TABLE + "(" +
			COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
			COLUMN_TRACK_TIME + " INTEGER," +
			COLUMN_LONGITUDE + " REAL," +
			COLUMN_LATITUDE + " REAL," +
			COLUMN_ROUTE_ID + " INTEGER)";
	
	public static void onCreate(SQLiteDatabase database)
	{
		database.execSQL(CREATE_TABLE_POSITIONS);
	}
	
	public static void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion)
	{
		Log.w(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion + ", which will destroy all old data");
		database.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE);		
		onCreate(database);
	}
	
	public static Position getPosition(Cursor cursor)
	{
	    int id = cursor.getInt(INDEX_ID);
        long when = cursor.getLong(INDEX_WHEN);
        double longitude = cursor.getDouble(INDEX_LONGITUDE);
        double latitude = cursor.getDouble(INDEX_LATITUDE);
        int routeId = cursor.getInt(INDEX_ROUTE_ID);
        
        return new Position(id, longitude, latitude, new Date(when), routeId);
	}
}
