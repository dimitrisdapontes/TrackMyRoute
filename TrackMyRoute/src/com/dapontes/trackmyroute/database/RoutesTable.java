package com.dapontes.trackmyroute.database;

import java.util.Date;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.dapontes.trackmyroute.Route;

public class RoutesTable
{
    protected static final String TAG = "RoutesTable";

    // table name
    public static final String DATABASE_TABLE = "routes";

    // table column names
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_START_DATE = "startdate";
    public static final String COLUMN_END_DATE = "enddate";
    public static final String COLUMN_TOTAL_METERS = "totalmeters";
    public static final String COLUMN_TOTAL_SECONDS = "totalseconds";

    // table column indexes
    static final int INDEX_ID = 0;
    static final int INDEX_START_DATE = 1;
    static final int INDEX_END_DATE = 2;
    static final int INDEX_TOTAL_METERS = 3;
    static final int INDEX_TOTAL_SECONDS = 4;

    protected final static String CREATE_TABLE_ROUTES = 
            "CREATE TABLE " + DATABASE_TABLE + "(" 
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
		    + COLUMN_START_DATE + " INTEGER," 
            + COLUMN_END_DATE + " INTEGER," 
		    + COLUMN_TOTAL_METERS + " INTEGER,"
		    + COLUMN_TOTAL_SECONDS + " INTEGER)";

    public static void onCreate(SQLiteDatabase database)
    {
        database.execSQL(CREATE_TABLE_ROUTES);
    } 

    public static void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion)
    {
        Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                + newVersion + ", which will destroy all old data");
        database.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE);
        onCreate(database);
    }
    
    public static Route getRoute(Cursor cursor)
    {
        long id = cursor.getLong(INDEX_ID);
        long startDate = cursor.getLong(INDEX_START_DATE);
        long endDate = cursor.getLong(INDEX_END_DATE);
        int totalMeters = cursor.getInt(INDEX_TOTAL_METERS);
        int totalSeconds = cursor.getInt(INDEX_TOTAL_SECONDS);
        
        return new Route(id, new Date(startDate), new Date(endDate), totalMeters, totalSeconds);
    }
}
