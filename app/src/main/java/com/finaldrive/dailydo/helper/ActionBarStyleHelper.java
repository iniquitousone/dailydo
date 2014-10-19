package com.finaldrive.dailydo.helper;

import android.app.Activity;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.TypedValue;
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
            final DisplayMetrics displayMetrics = activity.getResources().getDisplayMetrics();
            // Dimensions come from general horizontal margin (16dp) and left edge content spacing (72dp).
            final int leftPadding = (int) (8 * displayMetrics.density + 0.5f);
            final int rightPadding = (int) (16 * displayMetrics.density + 0.5f);
            homeIcon.setPadding(leftPadding, 0, rightPadding, 0);
            activity.getActionBar().setDisplayHomeAsUpEnabled(false);
            activity.getActionBar().setDisplayShowTitleEnabled(isShowTitle);
        }
    }
}
