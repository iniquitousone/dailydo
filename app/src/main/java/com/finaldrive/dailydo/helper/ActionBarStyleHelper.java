package com.finaldrive.dailydo.helper;

import android.app.Activity;
import android.os.Build;
import android.widget.ImageView;

/**
 * Utility class to help setup the ActionBar style where appropriate.
 */
public final class ActionBarStyleHelper {

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
            final float density = activity.getResources().getDisplayMetrics().density;
            if (isShowTitle) {
                // Material design demands the title be 72dp from the left edge of the screen.
                final int rightPadding = dpToPixels((72 - 16 - 24), density);
                homeIcon.setPadding(0, 0, rightPadding, 0);
            }
            activity.getActionBar().setDisplayHomeAsUpEnabled(false);
            activity.getActionBar().setDisplayShowTitleEnabled(isShowTitle);
        }
    }

    private static int dpToPixels(int dp, float density) {
        return (int) (dp * density + 0.5f);
    }
}
