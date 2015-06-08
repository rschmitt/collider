package com.github.rschmitt.collider;

import clojure.lang.*;

import javax.annotation.concurrent.Immutable;
import java.util.*;
import java.util.function.*;
import java.util.stream.Collector;

import static java.util.stream.Collector.Characteristics.UNORDERED;

@Immutable
public class ClojureMap<K, V> implements Map<K, V> {
    private final Map<K, V> delegate;

    @SuppressWarnings("unchecked")
    static <K, V> ClojureMap<K, V> create(Object... init) {
        return (ClojureMap<K, V>) create(PersistentHashMap.create(init));
    }

    @SuppressWarnings("unchecked")
    private static <K, V> ClojureMap<K, V> create(Map<K, V> ts) {
        return new ClojureMap<>(ts);
    }

    @SuppressWarnings("unchecked")
    static <K, V> ClojureMap<K, V> wrap(IPersistentMap map) {
        return create((Map<K, V>) map);
    }

    @SuppressWarnings("unchecked")
    protected ClojureMap(Object delegate) {
        this.delegate = (Map<K, V>) delegate;
    }

    @SuppressWarnings("unchecked")
    public ClojureMap<K, V> assoc(K key, V value) {
        Associative assoc = RT.assoc(delegate, key, value);
        return ClojureMap.create((Map<K, V>) assoc);
    }

    @SuppressWarnings("unchecked")
    public ClojureMap<K, V> dissoc(K key) {
        Object dissoc = RT.dissoc(delegate, key);
        return ClojureMap.create((Map<K, V>) dissoc);
    }

    @SafeVarargs
    public final ClojureMap<K, V> merge(ClojureMap<K, V>... maps) {
        TransientMap<K, V> ret = asTransient();
        for (ClojureMap<K, V> map : maps) {
            for (Entry<K, V> entry : map.entrySet()) {
                ret.put(entry.getKey(), entry.getValue());
            }
        }
        return ret.toPersistent();
    }

    public TransientMap<K, V> asTransient() {
        IEditableCollection asEditable = (IEditableCollection) delegate;
        ITransientCollection asTransient = asEditable.asTransient();
        return new TransientMap<>((ITransientMap) asTransient);
    }

    public <R> ClojureMap<R, V> mapKeys(Function<? super K, ? extends R> f) {
        return entrySet().stream().collect(toClojureMap(e -> f.apply(e.getKey()), Entry::getValue));
    }

    public <R> ClojureMap<K, R> mapValues(Function<? super V, ? extends R> f) {
        return entrySet().stream().collect(toClojureMap(Entry::getKey, e -> f.apply(e.getValue())));
    }

    public ClojureMap<K, V> filterKeys(Predicate<? super K> p) {
        return entrySet().stream().filter(e -> p.test(e.getKey())).collect(toClojureMap(Entry::getKey, Entry::getValue));
    }

    public ClojureMap<K, V> filterValues(Predicate<? super V> p) {
        return entrySet().stream().filter(e -> p.test(e.getValue())).collect(toClojureMap(Entry::getKey, Entry::getValue));
    }

    public ClojureMap<K, V> excludeKeys(Predicate<? super K> p) {
        return filterKeys(p.negate());
    }

    public ClojureMap<K, V> excludeValues(Predicate<? super V> p) {
        return filterValues(p.negate());
    }

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

    ////////////////////////////////
    // Mindless delegation goes here
    ////////////////////////////////

    @Override
    public int size() {
        return delegate.size();
    }

    @Override
    public boolean isEmpty() {
        return delegate.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return delegate.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return delegate.containsValue(value);
    }

    @Override
    public V get(Object key) {
        return delegate.get(key);
    }

    @Override
    public V put(K key, V value) {
        return delegate.put(key, value);
    }

    @Override
    public V remove(Object key) {
        return delegate.remove(key);
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        delegate.putAll(m);
    }

    @Override
    public void clear() {
        delegate.clear();
    }

    @Override
    public Set<K> keySet() {
        return delegate.keySet();
    }

    @Override
    public Collection<V> values() {
        return delegate.values();
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return delegate.entrySet();
    }

    @Override
    public boolean equals(Object o) {
        return delegate.equals(o);
    }

    @Override
    public int hashCode() {
        return delegate.hashCode();
    }

    @Override
    public V getOrDefault(Object key, V defaultValue) {
        return delegate.getOrDefault(key, defaultValue);
    }

    @Override
    public void forEach(BiConsumer<? super K, ? super V> action) {
        delegate.forEach(action);
    }

    @Override
    public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
        delegate.replaceAll(function);
    }

    @Override
    public V putIfAbsent(K key, V value) {
        return delegate.putIfAbsent(key, value);
    }

    @Override
    public boolean remove(Object key, Object value) {
        return delegate.remove(key, value);
    }

    @Override
    public boolean replace(K key, V oldValue, V newValue) {
        return delegate.replace(key, oldValue, newValue);
    }

    @Override
    public V replace(K key, V value) {
        return delegate.replace(key, value);
    }

    @Override
    public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
        return delegate.computeIfAbsent(key, mappingFunction);
    }

    @Override
    public V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        return delegate.computeIfPresent(key, remappingFunction);
    }

    @Override
    public V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        return delegate.compute(key, remappingFunction);
    }

    @Override
    public V merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        return delegate.merge(key, value, remappingFunction);
    }
}
