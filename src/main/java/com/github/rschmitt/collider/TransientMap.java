package com.github.rschmitt.collider;

import java.util.Map;

import javax.annotation.concurrent.NotThreadSafe;

import clojure.lang.ITransientMap;
import clojure.lang.PersistentArrayMap;

/**
 * A map that can be modified in-place and then converted to a {@link ClojureMap} in O(1) time.
 * <p/>
 * Instances of this class are not thread-safe; it is recommended that this class be used in a
 * thread-local fashion. In Clojure 1.7.0-RC1 and later, it is permitted to use this class from
 * multiple threads, and this is safe as long as access is correctly synchronized. In older
 * versions of Clojure, thread-local usage is enforced, and transients can only be modified by their
 * owning thread.
 */
@NotThreadSafe
public class TransientMap<K, V> {
    private volatile ITransientMap delegate;

    TransientMap() {
        this.delegate = PersistentArrayMap.EMPTY.asTransient();
    }

    TransientMap(ITransientMap delegate) {
        this.delegate = delegate;
    }

    /**
     * Add a binding from {@code key} to {@code value} to this map, overwriting any existing
     * binding for {@code key}.
     */
    public void put(K key, V value) {
        delegate = delegate.assoc(key, value);
    }

    /**
     * Copy all bindings from {@code map} into this map.
     */
    public void putAll(Map<? extends K, ? extends V> map) {
        for (Map.Entry<? extends K, ? extends V> entry : map.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    /**
     * Removes {@code key} from this map. If {@code key} is not present, this operation does nothing.
     */
    public void remove(K key) {
        delegate = delegate.without(key);
    }

    /**
     * Returns the value currently associated with {@code key} in this map, or {@code null} if none
     * exists.
     */
    @SuppressWarnings("unchecked")
    public V get(K key) {
        return (V) delegate.valAt(key);
    }

    /**
     * Returns whether there is currently an entry for {@code key} in this map.
     */
    public boolean contains(K key) {
        Object NOT_FOUND = new Object();
        return delegate.valAt(key, NOT_FOUND) != NOT_FOUND;
    }

    /**
     * Returns the number of entries currently in this map.
     */
    public int size() {
        return delegate.count();
    }

    /**
     * Returns a persistent immutable version of this TransientMap. This operation is performed in
     * constant time. Note that after this method is called, this transient instance will no longer
     * be usable and attempts to modify it will fail.
     */
    public ClojureMap<K, V> toPersistent() {
        return ClojureMap.wrap(delegate.persistent());
    }
}
