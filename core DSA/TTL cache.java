import java.util.HashMap;
import java.util.Map;

class TTLCache {

    class CacheEntry {

        int value;
        long expiryTime;

        CacheEntry(int value, long expiryTime) {
            this.value = value;
            this.expiryTime = expiryTime;
        }
    }

    private final Map<Integer, CacheEntry> cache;

    public TTLCache() {
        cache = new HashMap<>();
    }

    public void put(int key, int value, long ttlMillis) {

        long expiry = System.currentTimeMillis() + ttlMillis;

        cache.put(key, new CacheEntry(value, expiry));
    }

    public int get(int key) {

        if (!cache.containsKey(key))
            return -1;

        CacheEntry entry = cache.get(key);

        if (System.currentTimeMillis() > entry.expiryTime) {

            cache.remove(key);

            return -1;
        }

        return entry.value;
    }

    public void remove(int key) {
        cache.remove(key);
    }
}
