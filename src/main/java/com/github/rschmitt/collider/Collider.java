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

    public static <T> ClojureList<T> clojureList() {
        return ClojureList.create();
    }

    public static <T> ClojureList<T> clojureList(T... ts) {
        return ClojureList.create(ts);
    }

    public static <T> ClojureSet<T> clojureSet() {
        return ClojureSet.create();
    }

    public static <T> ClojureSet<T> clojureSet(T... ts) {
        return ClojureSet.create(ts);
    }
}
