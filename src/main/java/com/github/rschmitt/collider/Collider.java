package com.github.rschmitt.collider;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collector;

import clojure.lang.IPersistentMap;
import clojure.lang.IPersistentSet;
import clojure.lang.IPersistentVector;

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
        if (elements == null) return ClojureList.create((T) null);
        return ClojureList.create(elements);
    }

    @SafeVarargs
    public static <T> ClojureSet<T> clojureSet(T... elements) {
        if (elements == null) return ClojureSet.create((T) null);
        return ClojureSet.create(elements);
    }

    public static <K, V> TransientMap<K, V> transientMap() {
        ClojureMap<K, V> emptyMap = clojureMap();
        return emptyMap.asTransient();
    }

    public static <T> TransientList<T> transientList() {
        ClojureList<T> emptyList = clojureList();
        return emptyList.asTransient();
    }

    public static <T> TransientSet<T> transientSet() {
        ClojureSet<T> emptySet = clojureSet();
        return emptySet.asTransient();
    }

    @SuppressWarnings("unchecked")
    public static <K, V> ClojureMap<K, V> intoClojureMap(Map<? extends K, ? extends V> map) {
        if (map instanceof ClojureMap) return (ClojureMap<K, V>) map;
        if (map instanceof IPersistentMap) return ClojureMap.wrap((IPersistentMap) map);
        return map.entrySet().stream().collect(ClojureMap.toClojureMap(Entry::getKey, Entry::getValue));
    }

    @SuppressWarnings("unchecked")
    public static <T> ClojureList<T> intoClojureList(List<? extends T> list) {
        if (list instanceof ClojureList) return (ClojureList<T>) list;
        if (list instanceof IPersistentVector) return (ClojureList<T>) ClojureList.wrap((IPersistentVector) list);

        // Work around an inference bug in some older JDKs
        Collector<T, TransientList<T>, ClojureList<T>> collector = ClojureList.toClojureList();

        return list.stream().collect(collector);
    }

    @SuppressWarnings("unchecked")
    public static <T> ClojureSet<T> intoClojureSet(Set<? extends T> set) {
        if (set instanceof ClojureSet) return (ClojureSet<T>) set;
        if (set instanceof IPersistentSet) return (ClojureSet<T>) ClojureSet.wrap((IPersistentSet) set);

        // Work around an inference bug in some older JDKs
        Collector<T, TransientSet<T>, ClojureSet<T>> collector = ClojureSet.toClojureSet();

        return set.stream().collect(collector);
    }
}
