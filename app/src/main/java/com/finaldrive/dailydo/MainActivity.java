package com.finaldrive.dailydo;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ImageSpan;
import android.view.Menu;

import com.finaldrive.dailydo.view.SlidingTabLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * Main activity for housing the various Fragment(s) the user will interact with.
 */
public class MainActivity extends FragmentActivity {

    private static final String CLASS_NAME = "MainActivity";
    private FragmentPagerAdapter fragmentPagerAdapter;
    private SlidingTabLayout slidingTabLayout;
    private ViewPager viewPager;

    /**
     * Entry point into the application. Sets up data and renders ListView.
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Call super constructor to establish default Cursor.
        super.onCreate(savedInstanceState);
        // Set the View that will be rendered for this Activity.
        setContentView(R.layout.activity_main);
        setupSharedPreferences();
        final List<Fragment> fragmentList = new ArrayList<Fragment>(3);
        fragmentList.add(new TasksFragment());
        fragmentList.add(new NotificationsFragment());
        fragmentList.add(new SettingsFragment());
        fragmentPagerAdapter = new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                return fragmentList.get(position);
            }

            @Override
            public int getCount() {
                return fragmentList.size();
            }

            @Override
            public CharSequence getPageTitle(int position) {
                switch (position) {
                    case 0:
                        final Drawable taskIcon = getResources().getDrawable(R.drawable.ic_list_white);
                        taskIcon.setBounds(0, 0, taskIcon.getIntrinsicWidth(), taskIcon.getIntrinsicHeight());
                        final ImageSpan taskIconImageSpan = new ImageSpan(taskIcon);
                        final SpannableStringBuilder taskIconSpan = new SpannableStringBuilder(" ");
                        taskIconSpan.setSpan(taskIconImageSpan, 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        return taskIconSpan;

                    case 1:
                        final Drawable notificationsIcon = getResources().getDrawable(R.drawable.ic_action_alarms);
                        notificationsIcon.setBounds(0, 0, notificationsIcon.getIntrinsicWidth(), notificationsIcon.getIntrinsicHeight());
                        final ImageSpan notificationsIconImageSpan = new ImageSpan(notificationsIcon);
                        final SpannableStringBuilder notificationsIconSpan = new SpannableStringBuilder(" ");
                        notificationsIconSpan.setSpan(notificationsIconImageSpan, 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        return notificationsIconSpan;

                    case 2:
                        final Drawable settingsIcon = getResources().getDrawable(R.drawable.ic_settings_white);
                        settingsIcon.setBounds(0, 0, settingsIcon.getIntrinsicWidth(), settingsIcon.getIntrinsicHeight());
                        final ImageSpan settingsIconImageSpan = new ImageSpan(settingsIcon);
                        final SpannableStringBuilder settingsIconSpan = new SpannableStringBuilder(" ");
                        settingsIconSpan.setSpan(settingsIconImageSpan, 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        return settingsIconSpan;

                    default:
                        return "";
                }
            }
        };
        viewPager = (ViewPager) findViewById(R.id.view_pager);
        viewPager.setAdapter(fragmentPagerAdapter);
        slidingTabLayout = (SlidingTabLayout) findViewById(R.id.sliding_tab_layout);
        slidingTabLayout.setCustomTabView(R.layout.tab_layout, R.id.tab_layout_text_view);
        slidingTabLayout.setViewPager(viewPager);
        slidingTabLayout.setDistributeEvenly(true);
        slidingTabLayout.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
            @Override
            public int getIndicatorColor(int position) {
                return Color.WHITE;
            }
        });
    }

    /**
     * Setups the SharedPreferences to use across the application. If they have not been initialize, they will here.
     * Otherwise, it will simply re-use what is already in the {@link SharedPreferences}.
     */
    private void setupSharedPreferences() {
        final SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.pref_daily_do), MODE_PRIVATE);
        final boolean isDailyResetEnabled = sharedPreferences.getBoolean(getString(R.string.pref_daily_reset_enabled), true);
        final int hourOfReset = sharedPreferences.getInt(getString(R.string.pref_daily_reset_hour), 0);
        final int minuteOfReset = sharedPreferences.getInt(getString(R.string.pref_daily_reset_minute), 0);
        final boolean isNotified = sharedPreferences.getBoolean(getString(R.string.pref_notified), false);
        final SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(getString(R.string.pref_daily_reset_enabled), isDailyResetEnabled);
        editor.putInt(getString(R.string.pref_daily_reset_hour), hourOfReset);
        editor.putInt(getString(R.string.pref_daily_reset_minute), minuteOfReset);
        editor.putBoolean(getString(R.string.pref_notified), isNotified);
        editor.commit();
    }

    /**
     * The BroadcastReceiver for this Activity is only relevant while it is already running.
     */
    @Override
    protected void onResume() {
        super.onResume();
    }

    /**
     * Overridden to free up resources and so the framework does not complain.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    /**
     * Inflates the associated {@link menu/main.xml}, responsible for the ActionBar items.
     *
     * @param menu being used
     * @return boolean for if item was selected
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
}
