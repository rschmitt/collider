package com.github.rschmitt.collider;

/**
 * A collection of factory methods to create immutable collections.
 */
public class Collider {
    public static <K, V> ClojureMap<K, V> clojureMap() {
        return ClojureMap.create();
    }

    public static <K, V> ClojureMap<K, V> clojureMap(K key, V value) {
        return ClojureMap.create(key, value);
    }

    public static <K, V> ClojureMap<K, V> clojureMap(K key1, V val1, K key2, V val2) {
        return ClojureMap.create(key1, val1, key2, val2);
    }

    public static <K, V> ClojureMap<K, V> clojureMap(K key1, V val1, K key2, V val2, K key3, V val3) {
        return ClojureMap.create(key1, val1, key2, val2, key3, val3);
    }

    public static <K, V> ClojureMap<K, V> clojureMap(K key1, V val1, K key2, V val2, K key3, V val3, K key4, V val4) {
        return ClojureMap.create(key1, val1, key2, val2, key3, val3, key4, val4);
    }

    public static <K, V> ClojureMap<K, V> clojureMap(K key1, V val1, K key2, V val2, K key3, V val3, K key4, V val4, K key5, V val5) {
        return ClojureMap.create(key1, val1, key2, val2, key3, val3, key4, val4, key5, val5);
    }

    @SafeVarargs
    public static <T> ClojureList<T> clojureList(T... elements) {
        return ClojureList.create(elements);
    }

    @SafeVarargs
    public static <T> ClojureSet<T> clojureSet(T... elements) {
        return ClojureSet.create(elements);
    }
}
