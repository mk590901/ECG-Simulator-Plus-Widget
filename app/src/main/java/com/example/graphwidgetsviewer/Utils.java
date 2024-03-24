package com.example.graphwidgetsviewer;

import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class Utils {
    final String TAG = Utils.class.getSimpleName();

    List<Integer> extractRangeData(final List<Integer> rowData, final int start, final int number) {
        List<Integer> result = new ArrayList();

        if (rowData.isEmpty()) {
            return result;
        }

        if (start < 0) {
            return result;
        }

        if (number <= 0) {
            return result;
        }

        int rowLength = rowData.size();

        if (start >= rowLength) {
            return result;
        }

        if (rowData.size() <= start + number) {
            result = rowData.subList(start, rowLength);
            return result;
        }
        result = rowData.subList(start, start + number);
        return result;
    }

    int getMinForFullBuffer(final CircularBuffer<Integer> buffer) {
        int result = 0;
        List<Integer> rowData = buffer.buffer();
        result = rowData.get(1);
        return result;
    }

    List<Integer> dataSeriesOverlay(final CircularBuffer<Integer> buffer) {
        if (buffer == null || buffer.size() == 0) {
            return new ArrayList<>();
        }

        int seriesSize =
            buffer.size() < buffer.capacity() - 1 ? buffer.size() : buffer.capacity() - 1;
        List<Integer> result =
            new ArrayList<>(Collections.nCopies(seriesSize, 0));
        for (int i = 0; i < seriesSize; i++) {
            if (i < buffer.size()) {
                int value = buffer.getDirect(i);
                result.set(i,value);
            }
        }
        return result;
    }

    List<Integer> dataSeriesNormal(StoreWrapper storeWrapper) {
        storeWrapper.storeCircularBufferParams();
        List<Integer> result = storeWrapper.buffer().getData();
        storeWrapper.restoreCircularBufferParams();
        return result;
    }

    public static int randomInRange(int min, int max) {
        if (min >= max) {
            throw new IllegalArgumentException("max must be greater than min");
        }
        Random random = new Random();
        return random.nextInt((max - min) + 1) + min;
    }
}
