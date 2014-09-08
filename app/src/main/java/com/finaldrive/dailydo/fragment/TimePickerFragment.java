package com.finaldrive.dailydo.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.format.DateFormat;

/**
 * Abstract Fragment used to display a TimePickerDialog to allow user input hour and minute.
 */
public abstract class TimePickerFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener {

    public static final String TITLE = "TITLE";
    public static final String HOUR_OF_DAY = "HOUR_OF_DAY";
    public static final String MINUTE = "MINUTE";

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final String title = getArguments().getString(TITLE);
        final int hourOfDay = getArguments().getInt(HOUR_OF_DAY);
        final int minute = getArguments().getInt(MINUTE);
        final Dialog dialog = new TimePickerDialog(getActivity(), AlertDialog.THEME_HOLO_DARK, this, hourOfDay, minute, DateFormat.is24HourFormat(getActivity()));
        if (title != null) {
            dialog.setTitle(title);
        }
        return dialog;
    }
}
