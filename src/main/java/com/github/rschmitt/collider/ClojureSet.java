package com.github.rschmitt.collider;

import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Stream;

import javax.annotation.concurrent.Immutable;

import clojure.lang.IEditableCollection;
import clojure.lang.IPersistentSet;
import clojure.lang.ITransientCollection;
import clojure.lang.ITransientSet;
import clojure.lang.PersistentHashSet;

import static java.util.stream.Collector.Characteristics.UNORDERED;

/**
 * A generic persistent immutable Set implementation, with three types of methods:
 * <ol>
 * <li>Read methods from {@link Set}, such as {@link #contains}</li>
 * <li>Write methods from Set, such as {@link #add}; these will throw {@link
 * UnsupportedOperationException}</li> and have been marked as {@code @Deprecated}
 * <li>Persistent "modification" methods, such as {@link #with}; these will efficiently create
 * modified copies of the current set</li>
 * </ol>
 */
@Immutable
public class ClojureSet<T> implements Set<T> {
    private final Set<T> delegate;

    @SuppressWarnings("unchecked")
    protected ClojureSet(Object delegate) {
        this.delegate = (Set<T>) delegate;
    }

    @SafeVarargs
    static <T> ClojureSet<T> create(T... ts) {
        return create(PersistentHashSet.create(ts));
    }

    static <T> ClojureSet<T> wrap(IPersistentSet clojureSet) {
        return create(clojureSet);
    }

    @SuppressWarnings("unchecked")
    private static <T> ClojureSet<T> create(IPersistentSet ts) {
        return new ClojureSet<>(ts);
    }

    /**
     * Returns a copy of this set that includes {@code t}.
     */
    public ClojureSet<T> with(T t) {
        return ClojureSet.create((IPersistentSet) ((IPersistentSet) delegate).cons(t));
    }

    /**
     * Returns a copy of this set that does not include {@code t}.
     */
    public ClojureSet<T> without(T t) {
        return wrap(((IPersistentSet) delegate).disjoin(t));
    }

    /**
     * Maps {@code f} over the elements in this set, returning a new set containing the result.
     */
    public <U> ClojureSet<U> map(Function<? super T, ? extends U> f) {
        return stream().map(f).collect(toClojureSet());
    }

    /**
     * Returns a new set containing only the elements in this set matching {@code p}.
     */
    public ClojureSet<T> filter(Predicate<? super T> p) {
        return stream().filter(p).collect(toClojureSet());
    }

    /**
     * Returns a new set containing none of the elements in this set matching {@code p}.
     */
    public ClojureSet<T> exclude(Predicate<? super T> p) {
        return filter(p.negate());
    }

    /**
     * Returns a mutable copy of this set.
     */
    public Set<T> toMutableSet() {
        return new HashSet<>(this);
    }

    /**
     * Returns a transient version of this set in constant time.
     */
    public TransientSet<T> asTransient() {
        IEditableCollection asEditable = (IEditableCollection) delegate;
        ITransientCollection asTransient = asEditable.asTransient();
        return new TransientSet<>((ITransientSet) asTransient);
    }

    /**
     * Returns a {@link Collector} that accumulates values into a TransientSet, returning a
     * ClojureSet upon completion.
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
    public boolean contains(Object o) {
        return delegate.contains(o);
    }

    @Override
    public Iterator<T> iterator() {
        return delegate.iterator();
    }

    @Override
    public Object[] toArray() {
        return delegate.toArray();
    }

    @Override
    public <T1> T1[] toArray(T1[] a) {
        return delegate.toArray(a);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return delegate.containsAll(c);
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
    public Spliterator<T> spliterator() {
        return delegate.spliterator();
    }

    @Override
    public Stream<T> stream() {
        return delegate.stream();
    }

    @Override
    public Stream<T> parallelStream() {
        return delegate.parallelStream();
    }

    @Override
    public void forEach(Consumer<? super T> action) {
        delegate.forEach(action);
    }

    /**
     * @deprecated This operation will fail; use {@link #with} instead
     */
    @Override
    @Deprecated
    public boolean add(T t) {
        return delegate.add(t);
    }

    /**
     * @deprecated This operation will fail; use {@link #without} instead
     */
    @Override
    @Deprecated
    public boolean remove(Object o) {
        return delegate.remove(o);
    }

    /**
     * @deprecated This operation will fail; use {@link #with} instead
     */
    @Override
    @Deprecated
    public boolean addAll(Collection<? extends T> c) {
        return delegate.addAll(c);
    }

    /**
     * @deprecated This operation will fail; use {@link #filter} instead
     */
    @Override
    @Deprecated
    public boolean retainAll(Collection<?> c) {
        return delegate.retainAll(c);
    }

    /**
     * @deprecated This operation will fail; use {@link #exclude} instead
     */
    @Override
    @Deprecated
    public boolean removeAll(Collection<?> c) {
        return delegate.removeAll(c);
    }

    /**
     * @deprecated This operation will fail; use {@link Collider#clojureSet(Object[])} instead
     */
    @Override
    @Deprecated
    public void clear() {
        delegate.clear();
    }

    /**
     * @deprecated This operation will fail; use {@link #exclude} instead
     */
    @Override
    @Deprecated
    public boolean removeIf(Predicate<? super T> filter) {
        return delegate.removeIf(filter);
    }
}
