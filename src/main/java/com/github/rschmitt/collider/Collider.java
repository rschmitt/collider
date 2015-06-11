package com.github.rschmitt.collider;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

import clojure.lang.IPersistentMap;
import clojure.lang.IPersistentSet;
import clojure.lang.IPersistentVector;

import static java.util.stream.Collector.Characteristics.UNORDERED;

/**
 * A collection of factory methods to create immutable collections. These methods are designed to be
 * imported statically, either individually or with a star import.
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
        return map.entrySet().stream().collect(toClojureMap(Entry::getKey, Entry::getValue));
    }

    @SuppressWarnings("unchecked")
    public static <T> ClojureList<T> intoClojureList(List<? extends T> list) {
        if (list instanceof ClojureList) return (ClojureList<T>) list;
        if (list instanceof IPersistentVector) return (ClojureList<T>) ClojureList.wrap((IPersistentVector) list);

        // Work around an inference bug in some older JDKs
        Collector<T, TransientList<T>, ClojureList<T>> collector = toClojureList();

        return list.stream().collect(collector);
    }

    @SuppressWarnings("unchecked")
    public static <T> ClojureSet<T> intoClojureSet(Set<? extends T> set) {
        if (set instanceof ClojureSet) return (ClojureSet<T>) set;
        if (set instanceof IPersistentSet) return (ClojureSet<T>) ClojureSet.wrap((IPersistentSet) set);

        // Work around an inference bug in some older JDKs
        Collector<T, TransientSet<T>, ClojureSet<T>> collector = toClojureSet();

        return set.stream().collect(collector);
    }

    /**
     * Returns a {@link Collector} that efficiently accumulates values into a ClojureMap. If
     * multiple mappings are produced for the same key, the last mapping produced will be the one in
     * the returned map.
     *
     * @param keyMapper   a function from the input type to keys
     * @param valueMapper a function from the input type to values
     * @param <T>         the type of the input element in the stream
     * @param <K>         the key type for the map that will be returned
     * @param <V>         the value type for the map that will be returned
     */
    public static <T, K, V> Collector<T, TransientMap<K, V>, ClojureMap<K, V>> toClojureMap(
            Function<? super T, ? extends K> keyMapper,
            Function<? super T, ? extends V> valueMapper
    ) {
        return new Collector<T, TransientMap<K, V>, ClojureMap<K, V>>() {
            @Override
            public Supplier<TransientMap<K, V>> supplier() {
                return TransientMap::new;
            }

            @Override
            public BiConsumer<TransientMap<K, V>, T> accumulator() {
                return (map, t) -> map.put(keyMapper.apply(t), valueMapper.apply(t));
            }

            @Override
            public BinaryOperator<TransientMap<K, V>> combiner() {
                return (x, y) -> {
                    x.putAll(y.toPersistent());
                    return x;
                };
            }

            @Override
            public Function<TransientMap<K, V>, ClojureMap<K, V>> finisher() {
                return TransientMap::toPersistent;
            }

            @Override
            public Set<Characteristics> characteristics() {
                return EnumSet.of(UNORDERED);
            }
        };
    }

    /**
     * Returns a {@link Collector} that efficiently accumulates values into a ClojureMap while
     * detecting collisions. If multiple mappings are produced for the same key, the {@code
     * mergeFunction} will be invoked to determine which value to use.
     *
     * @param keyMapper     a function from the input type to keys
     * @param valueMapper   a function from the input type to values
     * @param mergeFunction a function used to resolve collisions between values associated with the
     *                      same key
     * @param <T>           the type of the input element in the stream
     * @param <K>           the key type for the map that will be returned
     * @param <V>           the value type for the map that will be returned
     */
    public static <T, K, V> Collector<T, TransientMap<K, V>, ClojureMap<K, V>> toStrictClojureMap(
            Function<? super T, ? extends K> keyMapper,
            Function<? super T, ? extends V> valueMapper,
            BinaryOperator<V> mergeFunction
    ) {
        return new Collector<T, TransientMap<K, V>, ClojureMap<K, V>>() {
            @Override
            public Supplier<TransientMap<K, V>> supplier() {
                return TransientMap::new;
            }

            @Override
            public BiConsumer<TransientMap<K, V>, T> accumulator() {
                return (map, t) -> putUnique(map, keyMapper.apply(t), valueMapper.apply(t));
            }

            @Override
            public BinaryOperator<TransientMap<K, V>> combiner() {
                return (x, y) -> {
                    ClojureMap<K, V> source = y.toPersistent();
                    for (Entry<K, V> entry : source.entrySet()) {
                        putUnique(x, entry.getKey(), entry.getValue());
                    }
                    return x;
                };
            }

            private void putUnique(TransientMap<K, V> map, K key, V value) {
                if (map.contains(key)) {
                    value = mergeFunction.apply(value, map.get(key));
                }
                map.put(key, value);
            }

            @Override
            public Function<TransientMap<K, V>, ClojureMap<K, V>> finisher() {
                return TransientMap::toPersistent;
            }

            @Override
            public Set<Characteristics> characteristics() {
                return Collections.emptySet();
            }
        };
    }

    /**
     * Returns a {@link Collector} that efficiently accumulates values into a TransientList.
     *
     * @param <T> the type of the input element in the stream
     */
    public static <T> Collector<T, TransientList<T>, ClojureList<T>> toClojureList() {
        return new Collector<T, TransientList<T>, ClojureList<T>>() {
            @Override
            public Supplier<TransientList<T>> supplier() {
                return TransientList::new;
            }

            @Override
            public BiConsumer<TransientList<T>, T> accumulator() {
                return TransientList::append;
            }

            @Override
            public BinaryOperator<TransientList<T>> combiner() {
                return (a, b) -> {
                    a.appendAll(b.toPersistent());
                    return a;
                };
            }

            @Override
            public Function<TransientList<T>, ClojureList<T>> finisher() {
                return TransientList::toPersistent;
            }

            @Override
            public Set<Characteristics> characteristics() {
                return Collections.emptySet();
            }
        };
    }

    /**
     * Returns a {@link Collector} that efficiently accumulates values into a ClojureSet.
     *
     * @param <T> the type of the input element in the stream
     */
    public static <T> Collector<T, TransientSet<T>, ClojureSet<T>> toClojureSet() {
        return new Collector<T, TransientSet<T>, ClojureSet<T>>() {
            @Override
            public Supplier<TransientSet<T>> supplier() {
                return TransientSet::new;
            }

            @Override
            public BiConsumer<TransientSet<T>, T> accumulator() {
                return TransientSet::add;
            }

            @Override
            public BinaryOperator<TransientSet<T>> combiner() {
                return (a, b) -> {
                    a.addAll(b.toPersistent());
                    return a;
                };
            }

            @Override
            public Function<TransientSet<T>, ClojureSet<T>> finisher() {
                return TransientSet::toPersistent;
            }

            @Override
            public Set<Characteristics> characteristics() {
                return EnumSet.of(UNORDERED);
            }
        };
    }
}
