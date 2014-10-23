package com.finaldrive.dailydo.helper;

import android.view.animation.TranslateAnimation;

/**
 * Provides static values for TranslateAnimations.
 */
public final class TranslateAnimationHelper {

    public static final TranslateAnimation DOWNWARD_BUTTON_TRANSLATION;
    public static final TranslateAnimation UPWARD_BUTTON_TRANSLATION;
    private static final int DURATION = 200;

    static {
        DOWNWARD_BUTTON_TRANSLATION = new TranslateAnimation(0, 0, 0, 400);
        DOWNWARD_BUTTON_TRANSLATION.setDuration(DURATION);
        UPWARD_BUTTON_TRANSLATION = new TranslateAnimation(0, 0, 400, 0);
        UPWARD_BUTTON_TRANSLATION.setDuration(DURATION);
    }
}
