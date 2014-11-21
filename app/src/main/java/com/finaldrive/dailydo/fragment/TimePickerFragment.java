package com.finaldrive.dailydo.fragment;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;

/**
 * Abstract Fragment used to display a TimePickerDialog to allow user input hour and minute.
 */
public abstract class TimePickerFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener {

    public static final String TITLE = "TITLE";
    public static final String HOUR_OF_DAY = "HOUR_OF_DAY";
    public static final String MINUTE = "MINUTE";
    public int counter = 0;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final String title = getArguments().getString(TITLE);
        final int hourOfDay = getArguments().getInt(HOUR_OF_DAY);
        final int minute = getArguments().getInt(MINUTE);
        final Dialog dialog = new TimePickerDialog(getActivity(), this, hourOfDay, minute, DateFormat.is24HourFormat(getActivity()));
        if (title != null) {
            dialog.setTitle(title);
        }
        return dialog;
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        counter++;
    }
}
