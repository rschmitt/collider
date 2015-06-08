package com.github.rschmitt.collider;

import clojure.lang.*;

import javax.annotation.concurrent.Immutable;
import java.util.*;
import java.util.function.*;
import java.util.stream.Collector;
import java.util.stream.Stream;

@Immutable
public class ClojureList<T> implements List<T> {
    private final List<T> delegate;

    @SuppressWarnings("unchecked")
    protected ClojureList(Object delegate) {
        this.delegate = (List<T>) delegate;
    }

    @SafeVarargs
    public static <T> ClojureList<T> create(T... ts) {
        return create(PersistentVector.create(ts));
    }

    static <T> ClojureList<T> wrap(IPersistentVector vector) {
        return create(vector);
    }

    @SuppressWarnings("unchecked")
    private static <T> ClojureList<T> create(IPersistentVector ts) {
        return new ClojureList<>(ts);
    }

    public ClojureList<T> append(T t) {
        return ClojureList.create(((IPersistentVector) delegate).cons(t));
    }

    public <U> ClojureList<U> map(Function<? super T, ? extends U> f) {
        return stream().map(f).collect(toClojureList());
    }

    public ClojureList<T> filter(Predicate<? super T> p) {
        return stream().filter(p).collect(toClojureList());
    }

    public ClojureList<T> exclude(Predicate<? super T> p) {
        return filter(p.negate());
    }

    public TransientList<T> asTransient() {
        IEditableCollection asEditable = (IEditableCollection) delegate;
        ITransientCollection asTransient = asEditable.asTransient();
        return new TransientList<>((ITransientVector) asTransient);
    }

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
    public boolean addAll(int index, Collection<? extends T> c) {
        return delegate.addAll(index, c);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return delegate.removeAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return delegate.retainAll(c);
    }

    @Override
    public void replaceAll(UnaryOperator<T> operator) {
        delegate.replaceAll(operator);
    }

    @Override
    public void sort(Comparator<? super T> c) {
        delegate.sort(c);
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
    public T get(int index) {
        return delegate.get(index);
    }

    @Override
    public T set(int index, T element) {
        return delegate.set(index, element);
    }

    @Override
    public void add(int index, T element) {
        delegate.add(index, element);
    }

    @Override
    public T remove(int index) {
        return delegate.remove(index);
    }

    @Override
    public int indexOf(Object o) {
        return delegate.indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        return delegate.lastIndexOf(o);
    }

    @Override
    public ListIterator<T> listIterator() {
        return delegate.listIterator();
    }

    @Override
    public ListIterator<T> listIterator(int index) {
        return delegate.listIterator(index);
    }

    @Override
    public List<T> subList(int fromIndex, int toIndex) {
        return delegate.subList(fromIndex, toIndex);
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
