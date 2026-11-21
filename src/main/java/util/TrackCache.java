package util;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public final class TrackCache {
    private final LinkedHashMap<Integer, byte[]> cache;
    private final long byteLimit;
    private long currentBytes;

    public TrackCache(long byteLimit) {
        if (byteLimit < 0) {
            throw new IllegalArgumentException("Byte limit cannot be negative");
        }
        this.cache = new LinkedHashMap<>(16, 0.75f, true);
        this.byteLimit = byteLimit;
        this.currentBytes = 0L;
    }
    public synchronized byte[] get(int trackId) {
        return cache.get(trackId);
    }
    public synchronized void put(int trackId, byte[] data) {
        if (data == null) {
            throw new IllegalArgumentException("Track data cannot be null");
        }
        if (data.length > byteLimit) {
            return;
        }
        byte[] existing = cache.remove(trackId);
        if (existing != null) {
            currentBytes -= existing.length;
        }
        Iterator<Map.Entry<Integer, byte[]>> iterator = cache.entrySet().iterator();
        while (currentBytes + data.length > byteLimit && iterator.hasNext()) {
            Map.Entry<Integer, byte[]> eldest = iterator.next();
            currentBytes -= eldest.getValue().length;
            iterator.remove();
        }
        cache.put(trackId, data);
        currentBytes += data.length;
    }
    public synchronized int size() {
        return cache.size();
    }

    public synchronized long totalBytes() {
        return currentBytes;
    }
}
