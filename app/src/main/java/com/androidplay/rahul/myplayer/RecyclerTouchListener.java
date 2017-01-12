    package com.androidplay.rahul.myplayer;

    import android.content.Context;
    import android.support.v7.widget.RecyclerView;
    import android.util.Log;
    import android.view.GestureDetector;
    import android.view.MotionEvent;
    import android.view.View;

    /**
     * Created by Rahul on 31-12-2016.
     */

    public class RecyclerTouchListener implements RecyclerView.OnItemTouchListener  {

    private GestureDetector gestureDetector;
    private RecyclerClick_Listener clickListener;

    public RecyclerTouchListener(Context context, final RecyclerView recyclerView, final RecyclerClick_Listener clickListener) {
        this.clickListener = clickListener;
        gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            Log.i("clickedd","recycler listener on single tap");
            return true;

        }

        @Override
        public void onLongPress(MotionEvent e) {
            Log.i("clickedd","recycler listener on long press");
            View child = recyclerView.findChildViewUnder(e.getX(), e.getY());
            if (child != null && clickListener != null) {
                clickListener.onLongClick(child, recyclerView.getChildAdapterPosition(child));
            }
            }
        });
    }

    @Override
    public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {

    View child = rv.findChildViewUnder(e.getX(), e.getY());
    if (child != null && clickListener != null && gestureDetector.onTouchEvent(e)) {
        clickListener.onClick(child, rv.getChildAdapterPosition(child));
    }
    return false;
    }

    @Override
    public void onTouchEvent(RecyclerView rv, MotionEvent e) {
    }

    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

    }
    }
