package com.example.graphwidgetsviewer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class CircularBuffer<T> {
    private final List<T> buffer;
    private int readIndex = 0;
    private int writeIndex = 0;
    private int size = 0;
    private boolean full = false;
    private final Lock lock = new ReentrantLock();

    public CircularBuffer(int capacity) {
        buffer = new ArrayList<>(capacity);
        for (int i = 0; i < capacity; i++) {
            buffer.add(null);
        }
    }

    public void writeAsync(T value) {
        lock.lock();
        try {
            write(value);
        } finally {
            lock.unlock();
        }
    }

    public List<T> buffer() {
        return buffer;
    }

    public void writeRow(List<T> list) {
        lock.lock();
        try {
            for (T element : list) {
                write(element);
            }
        } finally {
            lock.unlock();
        }
    }

    public void write(T value) {
        buffer.set(writeIndex, value);
        writeIndex = (writeIndex + 1) % buffer.size();
        size++;
        if (writeIndex == readIndex) {
            full = true;
            readIndex = (readIndex + 1) % buffer.size();
            size = buffer.size() - 1;
        }
    }

    public List<T> readRow(int orderedSize) {
        lock.lock();
        try {
            List<T> result = new ArrayList<>();
            int cycles = Math.min(orderedSize, size());
            for (int i = 0; i < cycles; i++) {
                T value = read();
                result.add(value);
            }
            return result;
        } finally {
            lock.unlock();
        }
    }

    public T readAsync() {
        lock.lock();
        try {
            return read();
        } finally {
            lock.unlock();
        }
    }

    public T read() {
        if (!full && readIndex == writeIndex) {
            return null;
        }

        T value = buffer.get(readIndex);
        size--;
        readIndex = (readIndex + 1) % buffer.size();
        full = false;
        return value;
    }

    public List<T> getData() {
        int orderedSize = size();
        List<T> result = new ArrayList<>();
        int cycles = Math.min(orderedSize, size());
        for (int i = 0; i < cycles; i++) {
            T value = read();
            result.add(value);
        }
        return result;
    }

    public boolean isEmpty() {
        return !full && readIndex == writeIndex;
    }

    public int capacity() {
        return buffer.size();
    }

    public T get(int index) {
        return buffer.get((readIndex + index) % buffer.size());
    }

    public T getDirect(int index) {
        return buffer.get(index);
    }

    public String trace() {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < capacity(); i++) {
            T value = buffer.get(i);
            String strValue = (value == null) ? "-" : value.toString();
            if (i == readIndex && i == writeIndex) {
                result.append("[").append(strValue).append("]");
            } else if (i == readIndex) {
                result.append("(").append(strValue).append(")");
            } else if (i == writeIndex) {
                result.append("[").append(strValue).append("]");
            } else {
                result.append("{").append(strValue).append("}");
            }
        }
        return result.toString();
    }

    public boolean isFull() {
        return full;
    }

    public int writeIndex() {
        return writeIndex;
    }

    public int readIndex() {
        return readIndex;
    }

    public int size() {
        return size;
    }

    public void setWriteIndex(int writeIndex) {
        this.writeIndex = writeIndex;
    }

    public void setReadIndex(int readIndex) {
        this.readIndex = readIndex;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public void setFull(boolean full) {
        this.full = full;
    }
}
