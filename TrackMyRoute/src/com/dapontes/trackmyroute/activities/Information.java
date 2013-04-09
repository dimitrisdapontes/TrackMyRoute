package com.dapontes.trackmyroute.activities;

import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import android.app.Activity;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.view.Menu;
import android.widget.TextView;

import com.bugsense.trace.BugSenseHandler;
import com.dapontes.trackmyroute.R;
import com.dapontes.trackmyroute.support.Logger;
import com.dapontes.trackmyroute.support.Utilities;

public class Information extends Activity
{
    private TextView versionText;
    private TextView buildDateText;
    
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        BugSenseHandler.initAndStartSession(this, "06c7982d");
        setContentView(R.layout.activity_information);
        
        versionText = (TextView)findViewById(R.id.version_text);
        buildDateText = (TextView)findViewById(R.id.build_date_text);
                       
        try
        {            
            versionText.setText(this.getPackageManager().getPackageInfo(this.getPackageName(), 0 ).versionName);
            
            Date buildDate = getBuildDate();
            if (buildDate != null)
            {
                buildDateText.setText(Utilities.DateToText(buildDate) + " " + Utilities.TimeToText(buildDate));
            }                       
            else
            {
                buildDateText.setText("-");
            }
        }
        catch (NameNotFoundException e)
        {
            Logger.write("Unable to get app version. Ex=" + e.toString(), "Information-onCreate", this);
            e.printStackTrace();
        }               
    }

    private Date getBuildDate()
    {
        try
        {
            ApplicationInfo ai = getPackageManager().getApplicationInfo(getPackageName(), 0);
            ZipFile zf = new ZipFile(ai.sourceDir);
            ZipEntry ze = zf.getEntry("classes.dex");
            return new Date(ze.getTime());            
         }
        catch(Exception e)
        {
            Logger.write("Unable to get build date. Ex=" + e.toString(), "Information-getBuildDate", this);
            return null;
        }
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.information, menu);
        return true;
    }

}
