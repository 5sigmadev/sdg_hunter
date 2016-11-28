package com.fivesigmagames.sdghunter;

import android.support.multidex.MultiDexApplication;

import com.fivesigmagames.sdghunter.R;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

/**
 * Created by ppanero on 02/11/2016.
 */

public class SDGApplication extends MultiDexApplication {

    // CONSTANTS
    private String UI_TRACKER = "UA-86775714-1";
    // VARS
    private Tracker mTracker;

    public String getUI_TRACKER() {
        return UI_TRACKER;
    }

    /**
     * Gets the default {@link Tracker} for this {@link MultiDexApplication}.
     * @return tracker
     */
    synchronized public Tracker getDefaultTracker() {
        if (mTracker == null) {
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            // To enable debug logging use: adb shell setprop log.tag.GAv4 DEBUG
            mTracker = analytics.newTracker(R.xml.global_tracker);
        }
        return mTracker;
    }

    public void sendEvent(String category, String action, String label){
        if(mTracker == null){
            this.getDefaultTracker();
        }
        else {
            mTracker.send(new HitBuilders.EventBuilder()
                    .setCategory(category)
                    .setAction(action)
                    .setLabel(label)
                    .build());
        }
    }

    public void onCreate ()
    {
        // Setup handler for uncaught exceptions.
        Thread.setDefaultUncaughtExceptionHandler (new Thread.UncaughtExceptionHandler()
        {
            @Override
            public void uncaughtException (Thread thread, Throwable e)
            {
                handleUncaughtException (thread, e);
            }
        });
    }

    public void handleUncaughtException (Thread thread, Throwable e)
    {

        GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
        // To enable debug logging use: adb shell setprop log.tag.GAv4 DEBUG
        mTracker = analytics.newTracker(R.xml.global_tracker);
        mTracker.send(new HitBuilders.EventBuilder()
                .setCategory("UNCAUGHT ERROR")
                .setAction(e.getMessage())
                .setLabel("SDG Hunter")
                .build());

        e.printStackTrace();
        System.exit(1); // kill off the crashed app
    }
}
