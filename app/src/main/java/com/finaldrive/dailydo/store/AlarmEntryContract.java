package com.finaldrive.dailydo.store;

import android.provider.BaseColumns;

/**
 * Class to represent the contract between the internal database and its columns for Alarm entries.
 */
public final class AlarmEntryContract {

    private AlarmEntryContract() {
    }

    public static abstract class AlarmEntry implements BaseColumns {

        public static final String TABLE = "alarm";

        public static final String COLUMN_HOUR = "hour";

        public static final String COLUMN_MINUTE = "minute";

        public static final String COLUMN_DAYS_REPEATING = "daysRepeating";

        public static final String COLUMN_IS_ENABLED = "isEnabled";
    }
}
