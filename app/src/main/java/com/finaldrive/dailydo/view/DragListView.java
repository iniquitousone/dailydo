package com.finaldrive.dailydo.view;

import android.content.ClipData;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

/**
 * Generic DragListView for handling swapping two items in a ListView using the Drag-and-drop API.
 */
public class DragListView extends ListView {

    private static final String CLASS_NAME = "DragListView";
    private boolean isDragging = false;
    private int mDownX = -1;
    private int mDownY = -1;
    private View draggedView = null;
    private List list;
    public int mPosition = -1;
    public int dPosition = -1;

    public DragListView(Context context) {
        super(context);
        init(context);
    }

    public DragListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public DragListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public void init(Context context) {}

    public void setList(List list) {
        this.list = list;
    }

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
        switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                mDownX = (int) motionEvent.getX();
                mDownY = (int) motionEvent.getY();
                if ((mDownX >= 0 && mDownX <= 50) && !isDragging) {
                    isDragging = true;
                    mPosition = pointToPosition(mDownX, mDownY) - getFirstVisiblePosition();
                    draggedView = getChildAt(mPosition);
                    ClipData dragData = ClipData.newPlainText(String.valueOf(draggedView.getId()), "");
                    DragShadowBuilder dragShadowBuilder = new DragShadowBuilder(draggedView);
                    draggedView.startDrag(dragData, dragShadowBuilder, null, 0);
                    draggedView.setVisibility(View.INVISIBLE);
                }
                break;
        }

        return super.onTouchEvent(motionEvent);
    }

    @Override
    public boolean onDragEvent(DragEvent dragEvent) {
        switch (dragEvent.getAction()) {
            case DragEvent.ACTION_DRAG_STARTED:
                return true;
            case DragEvent.ACTION_DRAG_ENTERED:
                return true;
            case DragEvent.ACTION_DRAG_LOCATION:
                return true;
            case DragEvent.ACTION_DROP:
                int dDownY = (int) dragEvent.getY();
                dPosition = pointToPosition(1, dDownY) - getFirstVisiblePosition();
                return true;
            case DragEvent.ACTION_DRAG_ENDED:
                Log.d(CLASS_NAME, String.format("OriginalPosition=%d and CoveredPosition=%d", mPosition, dPosition));
                if (dPosition >= 0 && mPosition != dPosition) {
                    Object temp = list.get(mPosition);
                    list.set(mPosition, list.get(dPosition));
                    list.set(dPosition, temp);
                    ((ArrayAdapter) getAdapter()).notifyDataSetChanged();
                }
                draggedView.setVisibility(View.VISIBLE);
                isDragging = false;
                return true;
            case DragEvent.ACTION_DRAG_EXITED:
                return true;
            default:
                return false;
        }
    }

    public void setIsDragging(boolean isDragging) {
        this.isDragging = isDragging;
    }
}
