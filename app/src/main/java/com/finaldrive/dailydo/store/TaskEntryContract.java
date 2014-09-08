package com.finaldrive.dailydo.store;

import android.provider.BaseColumns;

/**
 * Class to represent the contract between the internal database and its columns for Task entries.
 */
public final class TaskEntryContract {

    private TaskEntryContract() {
    }

    public static abstract class TaskEntry implements BaseColumns {

        public static final String TABLE = "task";

        public static final String COLUMN_TITLE = "title";

        public static final String COLUMN_NOTE = "note";

        public static final String COLUMN_ROW_NUMBER = "rowNumber";

        public static final String COLUMN_IS_CHECKED = "isChecked";
    }
}
