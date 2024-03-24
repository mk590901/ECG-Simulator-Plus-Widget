package com.example.graphwidgetsviewer;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

public class Obtain {
    final String TAG = Obtain.class.getSimpleName();
    private final long period;
    private int cycles;
    private GraphWidget widget;
    private Handler mHandler;
    private boolean mIsPeriodicalActionActive;

    public Obtain(final GraphWidget widget, final long period) {
        this.widget = widget;
        this.period = period;

        mHandler = new Handler(Looper.getMainLooper());
        mIsPeriodicalActionActive = false;

    }

    public void setState(final int cycles) {
        this.cycles = cycles;
    }

    public void start() {
        Log.d(TAG, "start");
        mHandler.removeCallbacksAndMessages(null);
        mIsPeriodicalActionActive = true;
        mHandler.post(periodicalRunnable);
    }

    public void redraw(/*int counter*/) {
        widget.update(/*counter*/);
    }

    private void callbackFunction() {
        redraw();
    }

    public void stop() {
        Log.d(TAG, "stop");
        mIsPeriodicalActionActive = false;
        mHandler.removeCallbacks(periodicalRunnable);
    }


    private Runnable periodicalRunnable = new Runnable() {
        @Override
        public void run() {
            if (mIsPeriodicalActionActive) {
                callbackFunction();
                mHandler.postDelayed(this, period);
            }
        }
    };

}
