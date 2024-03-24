package com.example.graphwidgetsviewer;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Log;

public class HandlerThreadWrapper {
    final String TAG = HandlerThreadWrapper.class.getSimpleName();
    final private HandlerThread handlerThread;
    final private Handler handler;
    private int counter = 0;
    private boolean isTaskRunning = false;
    final ECGSensor ecgSensor;
    final private CircularBuffer<Integer> sensorBuffer;
    final private int seriesLength;
    final private int cycles;

    public HandlerThreadWrapper(final ECGSensor ecgSensor) {
        // Create a new HandlerThread
        this.ecgSensor = ecgSensor;
        this.sensorBuffer = ecgSensor.sensorBuffer();
        this.seriesLength = ecgSensor.getSeriesLength();
        this.cycles = ecgSensor.sensorFrequency();

        handlerThread = new HandlerThread("HandlerThreadWrapper");
        handlerThread.start(); // Start the thread

        // Create a Handler associated with the HandlerThread's Looper
        handler = new Handler(handlerThread.getLooper());
    }

    public void startPeriodicTask(final long intervalMillis) {
        // Check if task is already running
        if (!isTaskRunning) {
            counter = 0;
            isTaskRunning = true;
            // Post a Runnable to the HandlerThread's message queue with a delay
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    // Perform background task here
                    ecgSensor.updateSensorBuffer(counter++);
                    if (counter >= cycles) {
                        counter = 1;
                    }
                    // After the background task is done, update UI using the main (UI) thread
                    new Handler(Looper.getMainLooper()).post(() -> {
                        // Update UI components here
                    });

                    // Schedule the next execution of the task
                    handler.postDelayed(this, intervalMillis);
                }
            }, intervalMillis); // Initial delay before first execution
        }
        else {
            Log.d(TAG,"task already running");
        }
    }

    public void stopPeriodicTask() {
        // Remove any pending Runnable tasks from the Handler's queue
        if (!isTaskRunning) {
            Log.d(TAG,"task already stopped");
            return;
        }
        handler.removeCallbacksAndMessages(null);
        isTaskRunning = false;
        Log.d(TAG,"stopPeriodicTask");

    }

    public void stopThread() {
        // Quit the HandlerThread
        handlerThread.quit();
    }

    public boolean isTaskRunning() {
        return isTaskRunning;
    }
}
