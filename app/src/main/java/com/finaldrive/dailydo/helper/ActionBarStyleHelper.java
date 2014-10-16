package com.finaldrive.dailydo.helper;

import android.app.Activity;
import android.os.Build;
import android.widget.ImageView;

/**
 * Utility class to help setup the ActionBar style where appropriate.
 */
public class ActionBarStyleHelper {

    /**
     * Setup the ActionBar for custom-ish layout to match that of Material Design.
     * This only applies to {@link Build.VERSION_CODES#KITKAT} and prior versions (since it pre-dates the Material Design).
     *
     * @param activity    whose ActionBar is to be modified
     * @param isShowTitle
     */
    public static final void setupActionBar(final Activity activity, boolean isShowTitle) {
        if (android.os.Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
            final ImageView homeIcon = (ImageView) activity.findViewById(android.R.id.home);
            // Dimensions come from general horizontal margin (16dp) and left edge content spacing (72dp).
            homeIcon.setPadding(16, 0, 72 - 16, 0);
            activity.getActionBar().setDisplayHomeAsUpEnabled(false);
            activity.getActionBar().setDisplayShowTitleEnabled(isShowTitle);
        }
    }
}
