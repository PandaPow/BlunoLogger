package com.dfrobot.angelo.blunobasicdemo;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.Viewport;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.GestureDetector;
import androidx.core.view.GestureDetectorCompat;

public class CustomGraph extends GraphView {
    private GestureDetectorCompat mDetector;
    public double defaultMinX = 1;
    public double defaultMaxX = 0;

    public CustomGraph(Context context) {
        super(context);
        this.init(context);
    }

    public CustomGraph(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.init(context);
    }

    public CustomGraph(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.init(context);
    }

    protected void init(Context context) {
        mDetector = new GestureDetectorCompat(context, new MyGestureListener());
    }

    @Override
    public boolean onTouchEvent(MotionEvent event){
        this.mDetector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    public void setDefaultXBounds(double newMin, double newMax){
        if(newMax < newMin){
            return;
        }
        defaultMinX = newMin;
        defaultMaxX = newMax;
    }

    class MyGestureListener extends GestureDetector.SimpleOnGestureListener {
        private static final String DEBUG_TAG = "Gestures";

        @Override
        public boolean onDown(MotionEvent event){
            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent event){
            if(defaultMinX < defaultMaxX){
                getViewport().setXAxisBoundsManual(true);
                getViewport().setMinX(defaultMinX);
                getViewport().setMaxX(defaultMaxX);
                onDataChanged(true,false);
                return false;
            }
            return false;
        }
    }

}
