package com.example.graphwidgetsviewer;

import android.graphics.Path;
import android.graphics.Point;
import android.util.Log;
import android.util.Size;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class StoreWrapper {

    final String TAG = StoreWrapper.class.getSimpleName();

    enum GraphMode {
        overlay,
        flowing
    }

    final int FREQ = 24;      // frames-per-seconds
    final int PERIOD = 1000;  // 1s = 1000ms

    int _drawSeriesLength;   //  Drawable data size per second
    final int _seriesNumber;      //  Data buffer size
    final int _seriesLength;      //  Number of displayed drawable data pieces
    private GraphMode _mode;  //  Mode

    private boolean _simulation = false;

    private final CircularBuffer<Integer> buffer_;  // Buffer for drawing

//    private final CircularBuffer<Integer> sensorBuffer_;  // Buffer for drawing

    private final Utils utils_ = new Utils();

    private ECGSensor sensor;

//    private final HandlerThreadWrapper handlerThreadWrapper;

    private double step;
    Path path;
    Path pathBefore;
    Path pathAfter;
    Point point;
    boolean full;
    int writeIndex;
    int readIndex;
    int size;

    public StoreWrapper(final ECGSensor sensor, int _seriesNumber, GraphMode _mode, boolean _simulation) {
        this.sensor = sensor;
        this._seriesLength = sensor.getSeriesLength();
        this._seriesNumber = _seriesNumber;
        this._mode = _mode;
        this._simulation = _simulation;
        this._drawSeriesLength = (int) (_seriesLength / ((double) PERIOD / FREQ)) + 1;
        this.buffer_ = new CircularBuffer<>(_seriesLength * _seriesNumber);
    }

    CircularBuffer<Integer> buffer() {
        return buffer_;
    }

    public int seriesLength() {
        return _drawSeriesLength;
    }

    public void updateBuffer(/*final int counter*/) {
        int seriesSize = seriesLength();
        List<Integer> dataExtracted = sensor.readRow(seriesSize);
        buffer_.writeRow(dataExtracted);
    }

    public double getMin() {
        int minV = Integer.MIN_VALUE;
        if (buffer_.size() == 0) {
            return minV;
        }
        List<Integer> rowData = buffer_.buffer();
        if (buffer_.size() == buffer_.capacity() - 1) {
            minV = utils_.getMinForFullBuffer(buffer_);
            for (int i = 1; i < buffer_.capacity(); i++) {
                Integer value = rowData.get(i);
                if (value != null) {
                    if (value < minV) {
                        minV = value;
                    }
                }
            }
        } else {
            minV = rowData.get(0);
            for (int i = 1; i < buffer_.size(); i++) {
                if (i < rowData.size()) {
                    if (rowData.get(i) < minV) {
                        minV = rowData.get(i);
                    }
                }
                else {
                    Log.e(TAG, "Min error");
                }
            }
        }
        return minV;
    }

    public double getMax() {
        int maxV = Integer.MAX_VALUE;
        if (buffer_.size() == 0) {
            return maxV;
        }
        List<Integer> rowData = buffer_.buffer();
        if (buffer_.size() == buffer_.capacity() - 1) {
            maxV = utils_.getMinForFullBuffer(buffer_);
            for (int i = 1; i < buffer_.capacity(); i++) {
                Integer value = rowData.get(i);
                if (value != null) {
                    if (value > maxV) {
                        maxV = value;
                    }
                }
            }
        } else {
            maxV = rowData.get(0);
            for (int i = 1; i < buffer_.size(); i++) {
                if (i < rowData.size()) {
                    if (rowData.get(i) > maxV) {
                        maxV = rowData.get(i);
                    }
                }
                else {
                    Log.e(TAG, "Max error");
                }
            }
        }
        return maxV;
    }

    public List<Double> prepareData(final Size size, final double shiftH) {
        List<Double> data = new ArrayList<>();

        double width = size.getWidth();
        double height = size.getHeight();

        double minV = getMin();
        double maxV = getMax();

        if (minV == maxV) {
            minV = minV / 2;
            maxV = maxV + minV / 2;
        }

        double dv = maxV - minV;
        step = width / (buffer_.capacity());
        double coeff = (height - 2 * shiftH) / dv;

        List<Integer> dataTemp = (_mode == GraphMode.overlay)
                ? utils_.dataSeriesOverlay(buffer_) : utils_.dataSeriesNormal(this);
        data = new ArrayList<>(Collections.nCopies(dataTemp.size(), 0.0));
        if (data.isEmpty() || dataTemp.isEmpty()) {
            return data;
        }
        for (int i = 0; i < dataTemp.size(); i++) {
            data.set(i, (maxV - dataTemp.get(i)) * coeff + shiftH);
        }
        return data;
    }

    public Path preparePath(final List<Double> data) {
        Path path = new Path();
        if (data.isEmpty()) {
            return path;
        }
        path.moveTo(0, data.get(0).floatValue());
        for (int i = 1; i < data.size(); i++) {
            path.lineTo((float)(i * step), data.get(i).floatValue());
        }
        return path;
    }

    public Path preparePathBefore(final List<Double> data) {
        Path path = new Path();
        if (data.isEmpty()) {
            return path;
        }
        int idx_ = buffer_.writeIndex()-1;
        if (idx_ >= data.size()) {
            idx_ = data.size() - 1;
            Log.e(TAG, "prepareBefore");
        }
        int idx = idx_ < 0 ? 0 : idx_;
        path.moveTo(0, data.get(0).floatValue());
        for (int i = 1; i < idx - 1; i++) {
            path.lineTo((float)(i * step), data.get(i).floatValue());
        }
        return path;
    }

    public Path preparePathAfter(final List<Double> data) {
        Path path = new Path();
        if (data.isEmpty()) {
            return path;
        }
        int idx_ = buffer_.writeIndex();    // -1???
        if (idx_ >= data.size()) {
            //Log.e(TAG, "[" + ident_ + "] preparePathAfter [" + idx_ + "->" +  data.size() + "]");
            idx_ = data.size() - 1;
        }
        int idx = idx_ < buffer_.capacity() - 1 ? idx_ : buffer_.capacity() - 1;
        path.moveTo((float)(idx * step), data.get(idx).floatValue());
        for (int i = idx + 1; i < data.size(); i++) {
            path.lineTo((float)(i * step), data.get(i).floatValue());
        }
        return path;
    }

    public Point preparePoint(final List<Double> data) {
        if (buffer_.size() == 0) {
            return new Point(Integer.MIN_VALUE, Integer.MIN_VALUE);
        }
        int idx_ = buffer_.writeIndex() - 1;
        if (idx_ >= data.size()) {
            idx_ = data.size() - 1;
            Log.e(TAG, "preparePoint");
        }
        int idx = idx_ < 0 ? 0 : idx_;
        Point point = new Point((int)(idx * step), data.get(idx).intValue());
        return point;
    }

    public void prepareDrawing(final Size size, final double shiftH) {
        List<Double> data = prepareData(size, shiftH);
        path = preparePath(data);
        pathBefore = preparePathBefore(data);
        pathAfter = preparePathAfter(data);
        point = preparePoint(data);
    }

    public void storeCircularBufferParams() {
        full = buffer_.isFull();
        writeIndex = buffer_.writeIndex();
        readIndex = buffer_.readIndex();
        size = buffer_.size();
    }

    public void restoreCircularBufferParams() {
        buffer_.setFull(full);
        buffer_.setWriteIndex(writeIndex);
        buffer_.setReadIndex(readIndex);
        buffer_.setSize(size);
    }

    public void setMode(final GraphMode mode) {
        _mode = mode;
    }
    public GraphMode mode() {
        return _mode;
    }

    public boolean isFull() {
        return buffer_.isFull();
    }

    public void setSimulationMode(final boolean simulation) {
        _simulation = simulation;
        if (_simulation) {
            //handlerThreadWrapper.startPeriodicTask(1000);
            sensor.start();
        }
        else {
            //handlerThreadWrapper.stopPeriodicTask();
            sensor.stop();
        }

    }
    public boolean isSimulationMode() {
        return _simulation;
    }

    public void stopThreadWrapper() {
//        if (handlerThreadWrapper != null) {
//            handlerThreadWrapper.stopPeriodicTask();
//            handlerThreadWrapper.stopThread();
//            Log.d(TAG,"stopThreadWrapper");
//        }
        sensor.close();
    }
}
