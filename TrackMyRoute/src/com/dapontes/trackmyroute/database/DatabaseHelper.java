package com.dapontes.trackmyroute.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper
{
	protected static final String DATABASE_NAME = "trackmyroute.db";
	protected static final int DATABASE_VERSION = 1;
	
	public DatabaseHelper(Context context)
	{
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db)
	{
		RoutesTable.onCreate(db);
		PositionsTable.onCreate(db);
		
		db.execSQL("CREATE VIEW vPositions AS SELECT *, datetime(tracktime/1000, 'unixepoch', 'localtime') FROM positions");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
	{
		RoutesTable.onUpgrade(db, oldVersion, newVersion);
		PositionsTable.onUpgrade(db, oldVersion, newVersion);
	}
}
