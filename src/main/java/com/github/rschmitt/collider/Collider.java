package com.github.rschmitt.collider;

public class Collider {
    public static <K, V> ClojureMap<K, V> clojureMap() {
        return ClojureMap.create();
    }

    public static <K, V> ClojureMap<K, V> clojureMap(K k, V v) {
        return ClojureMap.create(k, v);
    }

    public static <K, V> ClojureMap<K, V> clojureMap(K k1, V v1, K k2, V v2) {
        return ClojureMap.create(k1, v1, k2, v2);
    }

    public static <K, V> ClojureMap<K, V> clojureMap(K k1, V v1, K k2, V v2, K k3, V v3) {
        return ClojureMap.create(k1, v1, k2, v2, k3, v3);
    }

    public static <K, V> ClojureMap<K, V> clojureMap(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4) {
        return ClojureMap.create(k1, v1, k2, v2, k3, v3, k4, v4);
    }

    public static <K, V> ClojureMap<K, V> clojureMap(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5) {
        return ClojureMap.create(k1, v1, k2, v2, k3, v3, k4, v4, k5, v5);
    }

    @SafeVarargs
    public static <T> ClojureList<T> clojureList(T... ts) {
        return ClojureList.create(ts);
    }

    @SafeVarargs
    public static <T> ClojureSet<T> clojureSet(T... ts) {
        return ClojureSet.create(ts);
    }
}
