package com.finaldrive.dailydo.listener;

import android.view.View;
import android.widget.AbsListView;
import android.widget.ListView;

/**
 * Abstract scroll listener for a ListView to that determines when an upward or downward scroll occurred.
 */
public abstract class ListViewScrollListener implements AbsListView.OnScrollListener {

    private ListView listView;
    private int lastFirstVisibleItem;
    private int lastTop;

    protected ListViewScrollListener(ListView listView) {
        this.listView = listView;
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        // Unused.
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        final View firstVisibleEntry = listView.getChildAt(firstVisibleItem);
        final int currentTop = firstVisibleEntry == null ? 0 : firstVisibleEntry.getTop();

        if (firstVisibleItem == lastFirstVisibleItem) {
            // Compare the location of the top of the first visible view to determine direction.
            if (lastTop > currentTop) {
                onDownwardScroll();
            } else if (lastTop < currentTop) {
                onUpwardScroll();
            }
        } else {
            if (lastFirstVisibleItem < firstVisibleItem) {
                onDownwardScroll();
            } else if (lastFirstVisibleItem > firstVisibleItem) {
                onUpwardScroll();
            }
        }
        lastFirstVisibleItem = firstVisibleItem;
        lastTop = currentTop;
    }

    public abstract void onDownwardScroll();

    public abstract void onUpwardScroll();
}
