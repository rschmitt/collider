package com.github.rschmitt.collider;

import com.github.rschmitt.collider.internal.DelegateFactory;

import net.fushizen.invokedynamic.proxy.DynamicProxy;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collector;

import clojure.lang.Associative;
import clojure.lang.IEditableCollection;
import clojure.lang.IPersistentMap;
import clojure.lang.ITransientCollection;
import clojure.lang.ITransientMap;
import clojure.lang.PersistentHashMap;
import clojure.lang.RT;

import static java.util.stream.Collector.Characteristics.UNORDERED;

public abstract class ClojureMap<K, V> implements Map<K, V> {
    private static final DynamicProxy dynamicProxy = DelegateFactory.create(Map.class, ClojureMap.class);

    private final Map<K, V> delegate;

    static <K, V> ClojureMap<K, V> create(Object... init) {
        return create(PersistentHashMap.create(init));
    }

    @SuppressWarnings("unchecked")
    private static <K, V> ClojureMap<K, V> create(Map<K, V> ts) {
        try {
            return ((ClojureMap<K, V>) dynamicProxy.constructor().invoke(ts));
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }

    static <K, V> ClojureMap<K, V> wrap(IPersistentMap map) {
        return create((Map) map);
    }

    protected ClojureMap(Object delegate) {
        this.delegate = (Map) delegate;
    }

    public ClojureMap<K, V> assoc(K key, V value) {
        Associative assoc = RT.assoc(delegate, key, value);
        return ClojureMap.create((Map<K, V>) assoc);
    }

    public ClojureMap<K, V> merge(ClojureMap<K, V>... maps) {
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
        return new TransientMap((ITransientMap) asTransient);
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
}
