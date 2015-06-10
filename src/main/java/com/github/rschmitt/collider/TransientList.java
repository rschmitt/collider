package com.github.rschmitt.collider;

import java.util.Collection;

import javax.annotation.concurrent.NotThreadSafe;

import clojure.lang.IPersistentCollection;
import clojure.lang.IPersistentVector;
import clojure.lang.ITransientVector;
import clojure.lang.PersistentVector;

/**
 * A list that can be modified in-place and then converted to a {@link ClojureList} in O(1) time.
 * <p/>
 * Instances of this class are not thread-safe; it is recommended that this class be used in a
 * thread-local fashion. In Clojure 1.7.0-RC1 and later, it is permitted to use this class from
 * multiple threads, and this is safe as long as access is correctly synchronized. In older versions
 * of Clojure, thread-local usage is enforced, and transients can only be modified by their owning
 * thread.
 */
@NotThreadSafe
public class TransientList<T> {
    private volatile ITransientVector delegate;

    TransientList() {
        this.delegate = PersistentVector.EMPTY.asTransient();
    }

    TransientList(ITransientVector delegate) {
        this.delegate = delegate;
    }

    /**
     * Add {@code t} to the end of this list.
     */
    public void append(T t) {
        this.delegate = (ITransientVector) delegate.conj(t);
    }

    /**
     * Add all elements in {@code c} to the end of this list. Elements will be added in the
     * iteration order of their collection.
     */
    public void appendAll(Collection<? extends T> c) {
        for (T t : c) {
            append(t);
        }
    }

    /**
     * Returns the element currently at position {@code index} in this list.
     *
     * @throws IndexOutOfBoundsException if {@code index} is out of bounds (<tt>index &lt; 0 || index &gt;= size()</tt>)
     */
    public void set(int index, T t) {
        delegate.assocN(index, t);
    }

    /**
     * Returns the element currently at position {@code index} in this list.
     *
     * @throws IndexOutOfBoundsException if {@code index} is out of bounds (<tt>index &lt; 0 || index &gt;= size()</tt>)
     */
    public T get(int index) {
        return (T) delegate.nth(index);
    }

    /**
     * Returns the number of elements currently in this list.
     */
    public int size() {
        return delegate.count();
    }

    /**
     * Returns a persistent immutable version of this TransientList. This operation is performed in
     * constant time. Note that after this method is called, this transient instance will no longer
     * be usable and attempts to modify it will fail.
     */
    public ClojureList<T> toPersistent() {
        IPersistentCollection persistent = delegate.persistent();
        IPersistentVector asVector = (IPersistentVector) persistent;
        return ClojureList.wrap(asVector);
    }
}
