package com.github.rschmitt.collider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import javax.annotation.concurrent.Immutable;

import clojure.lang.IEditableCollection;
import clojure.lang.IPersistentVector;
import clojure.lang.ITransientCollection;
import clojure.lang.ITransientVector;
import clojure.lang.PersistentVector;

import static com.github.rschmitt.collider.Collider.toClojureList;

/**
 * A generic persistent immutable List implementation, with three types of methods:
 * <ol>
 * <li>Read methods from {@link List}, such as {@link #get}</li>
 * <li>Write methods from List, such as {@link #add}; these will throw {@link
 * UnsupportedOperationException}</li> and have been marked as {@code @Deprecated}
 * <li>Persistent "modification" methods, such as {@link #append}; these will efficiently create
 * modified copies of the current list</li>
 * </ol>
 */
@Immutable
public class ClojureList<T> implements List<T> {
    private final List<T> delegate;

    @SuppressWarnings("unchecked")
    protected ClojureList(Object delegate) {
        this.delegate = (List<T>) delegate;
    }

    @SafeVarargs
    static <T> ClojureList<T> create(T... ts) {
        return create(PersistentVector.create(ts));
    }

    static <T> ClojureList<T> wrap(IPersistentVector vector) {
        return create(vector);
    }

    @SuppressWarnings("unchecked")
    private static <T> ClojureList<T> create(IPersistentVector ts) {
        return new ClojureList<>(ts);
    }

    /**
     * Returns a copy of this list with {@code t} appended.
     */
    public ClojureList<T> append(T t) {
        return ClojureList.create(((IPersistentVector) delegate).cons(t));
    }

    /**
     * Maps {@code f} over the elements in this list, returning a new list containing the result.
     */
    public <U> ClojureList<U> map(Function<? super T, ? extends U> f) {
        return stream().map(f).collect(toClojureList());
    }

    /**
     * Returns a new list containing only the elements in this list matching {@code p}.
     */
    public ClojureList<T> filter(Predicate<? super T> p) {
        return stream().filter(p).collect(toClojureList());
    }

    /**
     * Returns a new list containing none of the elements in this list matching {@code p}.
     */
    public ClojureList<T> exclude(Predicate<? super T> p) {
        return filter(p.negate());
    }

    /**
     * Returns a mutable copy of this list.
     */
    public List<T> toMutableList() {
        return new ArrayList<>(this);
    }

    /**
     * Returns a transient version of this list in constant time.
     */
    public TransientList<T> asTransient() {
        IEditableCollection asEditable = (IEditableCollection) delegate;
        ITransientCollection asTransient = asEditable.asTransient();
        return new TransientList<>((ITransientVector) asTransient);
    }

    @Override
    public ClojureList<T> subList(int fromIndex, int toIndex) {
        return wrap((IPersistentVector) delegate.subList(fromIndex, toIndex));
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
    public String toString() {
        return delegate.toString();
    }

    @Override
    public T get(int index) {
        return delegate.get(index);
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
     * @deprecated This operation will fail; use {@link #exclude} instead
     */
    @Override
    @Deprecated
    public boolean removeIf(Predicate<? super T> filter) {
        return delegate.removeIf(filter);
    }

    /**
     * @deprecated This operation will fail.
     */
    @Override
    @Deprecated
    public boolean remove(Object o) {
        return delegate.remove(o);
    }

    /**
     * @deprecated This operation will fail; use {@link #append} instead
     */
    @Override
    @Deprecated
    public boolean add(T t) {
        return delegate.add(t);
    }

    /**
     * @deprecated This operation will fail; use {@link #append} instead
     */
    @Override
    @Deprecated
    public boolean addAll(Collection<? extends T> c) {
        return delegate.addAll(c);
    }

    /**
     * @deprecated This operation will fail; use {@link #append} instead
     */
    @Override
    @Deprecated
    public boolean addAll(int index, Collection<? extends T> c) {
        return delegate.addAll(index, c);
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
     * @deprecated This operation will fail; use {@link #filter} instead
     */
    @Override
    @Deprecated
    public boolean retainAll(Collection<?> c) {
        return delegate.retainAll(c);
    }

    /**
     * @deprecated This operation will fail; use {@link #map} instead
     */
    @Override
    @Deprecated
    public void replaceAll(UnaryOperator<T> operator) {
        delegate.replaceAll(operator);
    }

    /**
     * @deprecated This operation will fail.
     */
    @Override
    @Deprecated
    public void sort(Comparator<? super T> c) {
        delegate.sort(c);
    }

    /**
     * @deprecated This operation will fail; use {@link Collider#clojureList(Object[])} instead
     */
    @Override
    @Deprecated
    public void clear() {
        delegate.clear();
    }

    /**
     * @deprecated This operation will fail.
     */
    @Override
    @Deprecated
    public T set(int index, T element) {
        return delegate.set(index, element);
    }

    /**
     * @deprecated This operation will fail.
     */
    @Override
    @Deprecated
    public void add(int index, T element) {
        delegate.add(index, element);
    }

    /**
     * @deprecated This operation will fail.
     */
    @Override
    @Deprecated
    public T remove(int index) {
        return delegate.remove(index);
    }
}
