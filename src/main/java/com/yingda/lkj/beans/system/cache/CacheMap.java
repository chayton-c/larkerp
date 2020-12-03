package com.yingda.lkj.beans.system.cache;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author hood  2019/12/24
 */
public class CacheMap<K, V> implements Map<K, V> {

    private Map<K, V> wrappedMap;

    public CacheMap() {
        this(new ConcurrentHashMap<>());
    }

    public CacheMap(Map<K, V> wrappedMap) {
        this.wrappedMap = wrappedMap;
    }


    public int size() {
        return wrappedMap.size();
    }

    public boolean isEmpty() {
        return wrappedMap.isEmpty();
    }

    public boolean containsKey(Object key) {
        return wrappedMap.containsKey(key);
    }

    public boolean containsValue(Object value) {
        return wrappedMap.containsValue(value);
    }

    public V get(Object key) {
        return wrappedMap.get(key);
    }

    public V put(K key, V value) {
        return wrappedMap.put(key, value);
    }

    public V remove(Object key) {
        return wrappedMap.remove(key);
    }

    public void putAll(Map<? extends K, ? extends V> m) {
        for(java.util.Map.Entry<? extends K, ? extends V> entry : m.entrySet()) {
            this.put(entry.getKey(), entry.getValue());
        }
    }

    public void clear() {
        wrappedMap.clear();
    }

    public Set<K> keySet() {
        return wrappedMap.keySet();
    }

    public Collection<V> values() {
        return wrappedMap.values();
    }

    public Set<java.util.Map.Entry<K, V>> entrySet() {
        return wrappedMap.entrySet();
    }
}
