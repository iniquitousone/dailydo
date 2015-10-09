package com.finaldrive.dailydo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.TimePicker;

import com.finaldrive.dailydo.fragment.TimePickerFragment;
import com.finaldrive.dailydo.helper.NotificationToneHelper;
import com.finaldrive.dailydo.helper.TimeFormatHelper;
import com.finaldrive.dailydo.service.AlarmService;

/**
 * Fragment for handling Settings such as the daily reset time and the notification tone.
 */
public class SettingsFragment extends Fragment {

    private static final String CLASS_NAME = "SettingsFragment";
    private static final int REQUEST_CODE_TONE_PICKER = 1;
    // Class level variable so the onActivityResult() can leverage the view
    private TextView notificationToneTextView;

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
        final SharedPreferences.Editor editor = sharedPreferences.edit();
        final int hourOfReset = sharedPreferences.getInt(getString(R.string.pref_daily_reset_hour), 0);
        final int minuteOfReset = sharedPreferences.getInt(getString(R.string.pref_daily_reset_minute), 0);
        final boolean isVibrate = sharedPreferences.getBoolean(getString(R.string.pref_notification_vibrate), false);
        final View contentView = inflater.inflate(R.layout.fragment_settings, container, false);
        // Setup the daily reset view
        final TextView dailyResetTextView = (TextView) contentView.findViewById(R.id.daily_reset_text_view);
        dailyResetTextView.setText(TimeFormatHelper.format(this.getActivity(), hourOfReset, minuteOfReset));
        final View dailyResetButton = contentView.findViewById(R.id.daily_reset_button);
        dailyResetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final int hourOfReset = sharedPreferences.getInt(getString(R.string.pref_daily_reset_hour), 0);
                final int minuteOfReset = sharedPreferences.getInt(getString(R.string.pref_daily_reset_minute), 0);
                final Bundle bundle = new Bundle();
                bundle.putString(TimePickerFragment.TITLE, "Set a time for your DOs to uncheck everyday");
                bundle.putInt(TimePickerFragment.HOUR_OF_DAY, hourOfReset);
                bundle.putInt(TimePickerFragment.MINUTE, minuteOfReset);
                final TimePickerFragment dailyResetTimePickerFragment = new TimePickerFragment() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        if (counter > 0) {
                            return;
                        }
                        counter++;
                        editor.putInt(getString(R.string.pref_daily_reset_hour), hourOfDay);
                        editor.putInt(getString(R.string.pref_daily_reset_minute), minute);
                        editor.commit();
                        dailyResetTextView.setText(TimeFormatHelper.format(this.getActivity(), hourOfDay, minute));
                        AlarmService.scheduleNextReset(getActivity());
                    }
                };
                dailyResetTimePickerFragment.setArguments(bundle);
                dailyResetTimePickerFragment.show(SettingsFragment.this.getActivity().getFragmentManager(), "DailyResetTimePickerFragment");
            }
        });
        // Setup the notification tone view
        final Ringtone ringtone = RingtoneManager.getRingtone(this.getActivity(), NotificationToneHelper.getTone(this.getActivity()));
        notificationToneTextView = (TextView) contentView.findViewById(R.id.notification_tone_text_view);
        notificationToneTextView.setText(ringtone.getTitle(this.getActivity()));
        final View notificationToneButton = contentView.findViewById(R.id.notification_tone_button);
        notificationToneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Uri notificationTone = NotificationToneHelper.getTone(SettingsFragment.this.getActivity());
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
        // Setup the notification vibrate switch view
        final SwitchCompat notificationVibrateSwitch = (SwitchCompat) contentView.findViewById(R.id.notification_vibrate_toggle);
        notificationVibrateSwitch.setChecked(isVibrate);
        notificationVibrateSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                Log.d(CLASS_NAME, String.format("Notification vibrate isEnabled=%s", notificationVibrateSwitch.isChecked()));
                editor.putBoolean(getString(R.string.pref_notification_vibrate), notificationVibrateSwitch.isChecked());
                editor.commit();
            }
        });
        final View notificationVibrateButton = contentView.findViewById(R.id.notification_vibrate_button);
        notificationVibrateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                notificationVibrateSwitch.toggle();
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
                    NotificationToneHelper.setTone(this.getActivity(), pickedTone);
                    final Ringtone ringtone = RingtoneManager.getRingtone(this.getActivity(), pickedTone);
                    notificationToneTextView.setText(ringtone.getTitle(this.getActivity()));
                }
                break;
        }
    }
}
