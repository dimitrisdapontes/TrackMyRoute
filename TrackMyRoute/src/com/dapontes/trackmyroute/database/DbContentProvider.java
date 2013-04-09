package com.dapontes.trackmyroute.database;

import java.util.Arrays;
import java.util.HashSet;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;


public class DbContentProvider extends ContentProvider
{
    // database
    private DatabaseHelper database;

    // Used for the UriMacher
    private static final int POSITIONS = 10;
    private static final int POSITION_ID = 20;
    private static final int ROUTES = 30;
    private static final int ROUTE_ID = 40;
    private static final int ROUTE_POSITIONS = 50;

    private static final String AUTHORITY = "com.dapontes.trackmyroute.contentprovider";
    
    public static final Uri POSITIONS_CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/positions");
    public static final Uri ROUTES_CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/routes");
    public static final Uri ROUTE_POSITIONS_CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/route_positions");

    private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static
    {
        sURIMatcher.addURI(AUTHORITY, "positions", POSITIONS);
        sURIMatcher.addURI(AUTHORITY, "positions/#", POSITION_ID);
        sURIMatcher.addURI(AUTHORITY, "routes", ROUTES);
        sURIMatcher.addURI(AUTHORITY, "routes/#", ROUTE_ID);
        sURIMatcher.addURI(AUTHORITY, "route_positions/#", ROUTE_POSITIONS);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs)
    {
        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase sqlDB = database.getWritableDatabase();
        int rowsDeleted = 0;
        switch (uriType)
        {
            case POSITIONS:
                rowsDeleted = sqlDB.delete(PositionsTable.DATABASE_TABLE, selection, selectionArgs);
                break;

            case POSITION_ID:
                String positionId = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection))
                {
                    rowsDeleted = sqlDB.delete(PositionsTable.DATABASE_TABLE, PositionsTable.COLUMN_ID
                            + "=" + positionId, null);
                }
                else
                {
                    rowsDeleted = sqlDB.delete(PositionsTable.DATABASE_TABLE, PositionsTable.COLUMN_ID
                            + "=" + positionId + " and " + selection, selectionArgs);
                }
                break;

            case ROUTES:                
                rowsDeleted = sqlDB.delete(RoutesTable.DATABASE_TABLE, selection, selectionArgs);
                break;

            case ROUTE_ID:
                String routeId = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection))
                {                    
                    // first delete the positions of the route
                    sqlDB.delete(PositionsTable.DATABASE_TABLE, PositionsTable.COLUMN_ROUTE_ID + '=' + routeId, selectionArgs);
                    
                    // then delete the route itself
                    rowsDeleted = sqlDB.delete(RoutesTable.DATABASE_TABLE, RoutesTable.COLUMN_ID + "=" + routeId, null);
                }
                else
                {
                    rowsDeleted = sqlDB.delete(RoutesTable.DATABASE_TABLE, RoutesTable.COLUMN_ID + "=" + routeId + " and " + selection, selectionArgs);
                }
                break;

            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsDeleted;
    }

    @Override
    public String getType(Uri uri)
    {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values)
    {
        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase sqlDB = database.getWritableDatabase();
        long id = 0;
        switch (uriType)
        {
            case POSITIONS:
                id = sqlDB.insert(PositionsTable.DATABASE_TABLE, null, values);
                getContext().getContentResolver().notifyChange(uri, null);
                return Uri.parse(POSITIONS_CONTENT_URI + "/" + id);

            case ROUTES:
                id = sqlDB.insert(RoutesTable.DATABASE_TABLE, null, values);
                getContext().getContentResolver().notifyChange(uri, null);
                return Uri.parse(ROUTES_CONTENT_URI + "/" + id);

            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
    }

    @Override
    public boolean onCreate()
    {
        database = new DatabaseHelper(getContext());
        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder)
    {
        // Using SQLiteQueryBuilder instead of query() method
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

        // Check if the caller has requested a column which does not exists
        checkColumns(projection, uri);

        int uriType = sURIMatcher.match(uri);
        switch (uriType)
        {
            case POSITIONS:
                queryBuilder.setTables(PositionsTable.DATABASE_TABLE);
                break;

            case POSITION_ID:
                queryBuilder.setTables(PositionsTable.DATABASE_TABLE);
                queryBuilder.appendWhere(PositionsTable.COLUMN_ID + "=" + uri.getLastPathSegment());
                break;

            case ROUTES:
                queryBuilder.setTables(RoutesTable.DATABASE_TABLE);
                break;

            case ROUTE_ID:
                queryBuilder.setTables(RoutesTable.DATABASE_TABLE);
                queryBuilder.appendWhere(RoutesTable.COLUMN_ID + "=" + uri.getLastPathSegment());
                break;
                
            case ROUTE_POSITIONS:
                queryBuilder.setTables(PositionsTable.DATABASE_TABLE);
                queryBuilder.appendWhere(PositionsTable.COLUMN_ROUTE_ID + "=" + uri.getLastPathSegment());
                break;

            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        SQLiteDatabase db = database.getWritableDatabase();
        Cursor cursor = queryBuilder.query(db, projection, selection, selectionArgs, null, null, sortOrder);

        // Make sure that potential listeners are getting notified
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs)
    {
        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase sqlDB = database.getWritableDatabase();
        int rowsUpdated = 0;
        switch (uriType)
        {
            case POSITIONS:
                rowsUpdated = sqlDB.update(PositionsTable.DATABASE_TABLE, values, selection, selectionArgs);
                break;

            case POSITION_ID:
                String positionId = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection))
                {
                    rowsUpdated = sqlDB.update(PositionsTable.DATABASE_TABLE, values, PositionsTable.COLUMN_ID
                            + "=" + positionId, null);
                }
                else
                {
                    rowsUpdated = sqlDB.update(PositionsTable.DATABASE_TABLE, values, PositionsTable.COLUMN_ID
                            + "=" + positionId + " and " + selection, selectionArgs);
                }
                break;

            case ROUTES:
                rowsUpdated = sqlDB.update(RoutesTable.DATABASE_TABLE, values, selection, selectionArgs);
                break;

            case ROUTE_ID:
                String routeId = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection))
                {
                    rowsUpdated = sqlDB.update(RoutesTable.DATABASE_TABLE, values, RoutesTable.COLUMN_ID
                            + "=" + routeId, null);
                }
                else
                {
                    rowsUpdated = sqlDB.update(RoutesTable.DATABASE_TABLE, values, RoutesTable.COLUMN_ID
                            + "=" + routeId + " and " + selection, selectionArgs);
                }
                break;

            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsUpdated;
    }

    private void checkColumns(String[] projection, Uri uri)
    {
        String[] positionColumns =
        {
                PositionsTable.COLUMN_TRACK_TIME,
                PositionsTable.COLUMN_LONGITUDE,
                PositionsTable.COLUMN_LATITUDE,
                PositionsTable.COLUMN_ROUTE_ID,
                PositionsTable.COLUMN_ID
        };

        String[] routeColumns =
        {
                RoutesTable.COLUMN_START_DATE,
                RoutesTable.COLUMN_END_DATE,
                RoutesTable.COLUMN_TOTAL_METERS,
                RoutesTable.COLUMN_ID
        };

        String[] available = null;

        int uriType = sURIMatcher.match(uri);
        switch (uriType)
        {
            case POSITIONS:
            case POSITION_ID:
            case ROUTE_POSITIONS:
                available = positionColumns;
                break;

            case ROUTES:
            case ROUTE_ID:
                available = routeColumns;
                break;               

            default:
                throw new IllegalArgumentException("Unknown table to check in projection");
        }

        if (projection != null)
        {
            HashSet<String> requestedColumns = new HashSet<String>(Arrays.asList(projection));
            HashSet<String> availableColumns = new HashSet<String>(Arrays.asList(available));

            // Check if all columns which are requested are available
            if (!availableColumns.containsAll(requestedColumns))
            {
                throw new IllegalArgumentException("Unknown columns in projection");
            }
        }
    }

}
