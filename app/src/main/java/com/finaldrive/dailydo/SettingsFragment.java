package com.finaldrive.dailydo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.finaldrive.dailydo.helper.TimeFormatHelper;
import com.finaldrive.dailydo.service.NotificationService;


/**
 *
 */
public class SettingsFragment extends Fragment {

    private static final String CLASS_NAME = "SettingsFragment";
    private static final int REQUEST_CODE_TONE_PICKER = 1;

    public SettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final SharedPreferences sharedPreferences = this.getActivity().getSharedPreferences(getString(R.string.pref_daily_do), Context.MODE_PRIVATE);
        final boolean isDailyResetEnabled = sharedPreferences.getBoolean(getString(R.string.pref_daily_reset_enabled), true);
        final int hourOfReset = sharedPreferences.getInt(getString(R.string.pref_daily_reset_hour), 0);
        final int minuteOfReset = sharedPreferences.getInt(getString(R.string.pref_daily_reset_minute), 0);
        final View contentView = inflater.inflate(R.layout.fragment_settings, container, false);
        final TextView textView = (TextView) contentView.findViewById(R.id.daily_reset_text_view);
        textView.setText(TimeFormatHelper.format(this.getActivity(), hourOfReset, minuteOfReset));
        final View dailyResetButton = contentView.findViewById(R.id.daily_reset_button);
        dailyResetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Uri notificationTone = NotificationService.getNotificationTone(SettingsFragment.this.getActivity());
                final Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Select tone");
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_DEFAULT_URI, Settings.System.DEFAULT_NOTIFICATION_URI);
                if (notificationTone != null) {
                    intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, notificationTone);
                } else {
                    intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, Settings.System.DEFAULT_NOTIFICATION_URI);
                }
                startActivityForResult(intent, REQUEST_CODE_TONE_PICKER);
            }
        });

        return contentView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(CLASS_NAME, String.format("ActivityResult on RequestCode=%d and ResultCode=%d", requestCode, resultCode));
        switch (resultCode) {
            case Activity.RESULT_OK:
                if (requestCode == REQUEST_CODE_TONE_PICKER) {
                    final Uri pickedTone = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
                    NotificationService.setNotificationTone(this.getActivity(), pickedTone);
                }
                break;
        }
    }
}
