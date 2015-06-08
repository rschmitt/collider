package com.github.rschmitt.collider;


import clojure.lang.*;

import java.util.*;
import java.util.function.*;
import java.util.stream.Collector;
import java.util.stream.Stream;

import static java.util.stream.Collector.Characteristics.UNORDERED;

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

    public ClojureSet<T> with(T t) {
        return ClojureSet.create((IPersistentSet) ((IPersistentSet) delegate).cons(t));
    }

    public ClojureSet<T> without(T t) {
        return wrap(((IPersistentSet) delegate).disjoin(t));
    }

    public <U> ClojureSet<U> map(Function<? super T, ? extends U> f) {
        return stream().map(f).collect(toClojureSet());
    }

    public ClojureSet<T> filter(Predicate<? super T> p) {
        return stream().filter(p).collect(toClojureSet());
    }

    public ClojureSet<T> exclude(Predicate<? super T> p) {
        return filter(p.negate());
    }

    public TransientSet<T> asTransient() {
        IEditableCollection asEditable = (IEditableCollection) delegate;
        ITransientCollection asTransient = asEditable.asTransient();
        return new TransientSet<>((ITransientSet) asTransient);
    }

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
    public boolean add(T t) {
        return delegate.add(t);
    }

    @Override
    public boolean remove(Object o) {
        return delegate.remove(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return delegate.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        return delegate.addAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return delegate.retainAll(c);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return delegate.removeAll(c);
    }

    @Override
    public void clear() {
        delegate.clear();
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
    public boolean removeIf(Predicate<? super T> filter) {
        return delegate.removeIf(filter);
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
}
