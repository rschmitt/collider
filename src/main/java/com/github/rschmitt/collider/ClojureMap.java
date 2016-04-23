package com.github.rschmitt.collider;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import javax.annotation.concurrent.Immutable;

import clojure.lang.Associative;
import clojure.lang.IEditableCollection;
import clojure.lang.IPersistentMap;
import clojure.lang.ITransientCollection;
import clojure.lang.ITransientMap;
import clojure.lang.PersistentHashMap;
import clojure.lang.RT;

import static com.github.rschmitt.collider.Collider.toClojureMap;

/**
 * A generic persistent immutable Map implementation, with three types of methods:
 * <ol>
 * <li>Read methods from {@link Map}, such as {@link #get}</li>
 * <li>Write methods from Map, such as {@link #put}; these will throw {@link
 * UnsupportedOperationException}</li> and have been marked as {@code @Deprecated}
 * <li>Persistent "modification" methods, such as {@link #assoc}; these will efficiently create
 * modified copies of the current map</li>
 * </ol>
 */
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

    /**
     * Returns a copy of this map which also contains a mapping from {@code key} to {@code value}.
     * If a mapping for {@code key} already exists in the current map, it will be overwritten.
     */
    @SuppressWarnings("unchecked")
    public ClojureMap<K, V> assoc(K key, V value) {
        Associative assoc = RT.assoc(delegate, key, value);
        return ClojureMap.create((Map<K, V>) assoc);
    }

    /**
     * Returns a copy of this map without a mapping for {@code key}.
     */
    @SuppressWarnings("unchecked")
    public ClojureMap<K, V> dissoc(K key) {
        Object dissoc = RT.dissoc(delegate, key);
        return ClojureMap.create((Map<K, V>) dissoc);
    }

    /**
     * Returns a map that consists of all bindings from the current map, as well as {@code maps}.
     * If a mapping occurs in more than one map, the mapping in the rightmost map will take
     * precedence.
     */
    @SafeVarargs
    public final ClojureMap<K, V> merge(ClojureMap<K, V>... maps) {
        if (maps.length == 0) return this;
        if (Stream.of(maps).allMatch(Map::isEmpty)) return this;
        if (isEmpty() && maps.length == 1) return maps[0];
        TransientMap<K, V> ret = asTransient();
        for (ClojureMap<K, V> map : maps) {
            for (Entry<K, V> entry : map.entrySet()) {
                ret.put(entry.getKey(), entry.getValue());
            }
        }
        return ret.toPersistent();
    }

    /**
     * Returns a mutable copy of this map.
     */
    public Map<K, V> toMutableMap() {
        return new HashMap<>(this);
    }

    /**
     * Returns a transient version of this map in constant time.
     */
    public TransientMap<K, V> asTransient() {
        IEditableCollection asEditable = (IEditableCollection) delegate;
        ITransientCollection asTransient = asEditable.asTransient();
        return new TransientMap<>((ITransientMap) asTransient);
    }

    /**
     * Maps {@code f} over the keys in this map, returning a new map containing the result. If
     * {@code f} produces collisions, the result is undefined.
     */
    public <R> ClojureMap<R, V> mapKeys(Function<? super K, ? extends R> f) {
        return entrySet().stream().collect(toClojureMap(e -> f.apply(e.getKey()), Entry::getValue));
    }

    /**
     * Maps {@code f} over the values in this map, returning a new map containing the result.
     */
    public <R> ClojureMap<K, R> mapValues(Function<? super V, ? extends R> f) {
        return entrySet().stream().collect(toClojureMap(Entry::getKey, e -> f.apply(e.getValue())));
    }

    /**
     * Returns a new map containing only the mappings whose keys match {@code p}.
     */
    public ClojureMap<K, V> filterKeys(Predicate<? super K> p) {
        return entrySet().stream().filter(e -> p.test(e.getKey())).collect(toClojureMap(Entry::getKey, Entry::getValue));
    }

    /**
     * Returns a new map containing only the mappings whose values match {@code p}.
     */
    public ClojureMap<K, V> filterValues(Predicate<? super V> p) {
        return entrySet().stream().filter(e -> p.test(e.getValue())).collect(toClojureMap(Entry::getKey, Entry::getValue));
    }

    /**
     * Returns a new map containing none of the mappings whose keys match {@code p}.
     */
    public ClojureMap<K, V> excludeKeys(Predicate<? super K> p) {
        return filterKeys(p.negate());
    }

    /**
     * Returns a new map containing none of the mappings whose values match {@code p}.
     */
    public ClojureMap<K, V> excludeValues(Predicate<? super V> p) {
        return filterValues(p.negate());
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
    public String toString() {
        return delegate.toString();
    }

    @Override
    public V getOrDefault(Object key, V defaultValue) {
        return delegate.getOrDefault(key, defaultValue);
    }

    @Override
    public void forEach(BiConsumer<? super K, ? super V> action) {
        delegate.forEach(action);
    }

    /**
     * @deprecated This operation will fail; use {@link #assoc} instead
     */
    @Override
    @Deprecated
    public V put(K key, V value) {
        return delegate.put(key, value);
    }

    /**
     * @deprecated This operation will fail; use {@link #dissoc} instead
     */
    @Override
    @Deprecated
    public V remove(Object key) {
        return delegate.remove(key);
    }

    /**
     * @deprecated This operation will fail; use {@link #merge(ClojureMap[])} instead
     */
    @Override
    @Deprecated
    public void putAll(Map<? extends K, ? extends V> m) {
        delegate.putAll(m);
    }

    /**
     * @deprecated This operation will fail; use {@link Collider#clojureMap()} instead
     */
    @Override
    @Deprecated
    public void clear() {
        delegate.clear();
    }

    /**
     * @deprecated This operation will fail; use {@link #mapValues} instead
     */
    @Override
    @Deprecated
    public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
        delegate.replaceAll(function);
    }

    /**
     * @deprecated This operation will fail.
     */
    @Override
    @Deprecated
    public V putIfAbsent(K key, V value) {
        return delegate.putIfAbsent(key, value);
    }

    /**
     * @deprecated This operation will fail; use {@link #dissoc} instead
     */
    @Override
    @Deprecated
    public boolean remove(Object key, Object value) {
        return delegate.remove(key, value);
    }

    /**
     * @deprecated This operation will fail; use {@link #assoc} instead
     */
    @Override
    @Deprecated
    public boolean replace(K key, V oldValue, V newValue) {
        return delegate.replace(key, oldValue, newValue);
    }

    /**
     * @deprecated This operation will fail; use {@link #assoc} instead
     */
    @Override
    @Deprecated
    public V replace(K key, V value) {
        return delegate.replace(key, value);
    }

    /**
     * @deprecated This operation will fail.
     */
    @Override
    @Deprecated
    public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
        return delegate.computeIfAbsent(key, mappingFunction);
    }

    /**
     * @deprecated This operation will fail.
     */
    @Override
    @Deprecated
    public V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        return delegate.computeIfPresent(key, remappingFunction);
    }

    /**
     * @deprecated This operation will fail.
     */
    @Override
    @Deprecated
    public V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        return delegate.compute(key, remappingFunction);
    }

    /**
     * @deprecated This operation will fail.
     */
    @Override
    @Deprecated
    public V merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        return delegate.merge(key, value, remappingFunction);
    }
}
