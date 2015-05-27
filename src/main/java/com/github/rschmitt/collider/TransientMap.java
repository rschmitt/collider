package com.github.rschmitt.collider;

import java.util.Map;

import clojure.lang.ITransientMap;
import clojure.lang.PersistentArrayMap;

public class TransientMap<K, V> {
    private volatile ITransientMap delegate;

    TransientMap() {
        this.delegate = PersistentArrayMap.EMPTY.asTransient();
    }

    TransientMap(ITransientMap delegate) {
        this.delegate = delegate;
    }

    public void put(K key, V value) {
        delegate = delegate.assoc(key, value);
    }

    public void putAll(Map<? extends K, ? extends V> map) {
        for (Map.Entry<? extends K, ? extends V> entry : map.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    public void remove(K key) {
        delegate = delegate.without(key);
    }

    public V get(K key) {
        return (V) delegate.valAt(key);
    }

    public boolean contains(K key) {
        Object NOT_FOUND = new Object();
        return delegate.valAt(key, NOT_FOUND) != NOT_FOUND;
    }

    public ClojureMap<K, V> toPersistent() {
        return ClojureMap.wrap(delegate.persistent());
    }
}
