package com.github.rschmitt.collider;

import java.util.Collection;

import javax.annotation.concurrent.NotThreadSafe;

import clojure.lang.IPersistentCollection;
import clojure.lang.IPersistentSet;
import clojure.lang.ITransientSet;
import clojure.lang.PersistentHashSet;

/**
 * A set that can be modified in-place and then converted to a {@link ClojureSet} in O(1) time.
 * <p/>
 * Instances of this class are not thread-safe; it is recommended that this class be used in a
 * thread-local fashion. In Clojure 1.7.0-RC1 and later, it is permitted to use this class from
 * multiple threads, and this is safe as long as access is correctly synchronized. In older versions
 * of Clojure, thread-local usage is enforced, and transients can only be modified by their owning
 * thread.
 */
@NotThreadSafe
public class TransientSet<T> {
    private volatile ITransientSet delegate;

    TransientSet() {
        this.delegate = (ITransientSet) PersistentHashSet.EMPTY.asTransient();
    }

    TransientSet(ITransientSet delegate) {
        this.delegate = delegate;
    }

    /**
     * Idempotently adds {@code t} to this set.
     */
    public void add(T t) {
        this.delegate = (ITransientSet) delegate.conj(t);
    }

    /**
     * Adds all members of {@code collection} to this set.
     */
    public void addAll(Collection<? extends T> collection) {
        for (T t : collection) {
            add(t);
        }
    }

    /**
     * Returns whether {@code t} is currently a member of this set.
     */
    public boolean contains(T t) {
        return delegate.contains(t);
    }

    /**
     * Removes {@code t} from this set.
     */
    public void remove(T t) {
        this.delegate = delegate.disjoin(t);
    }

    /**
     * Returns the number of elements in this set.
     */
    public int size() {
        return delegate.count();
    }

    /**
     * Returns a persistent immutable version of this TransientSet. This operation is performed in
     * constant time. Note that after this method is called, this transient instance will no longer
     * be usable and attempts to modify it will fail.
     */
    public ClojureSet<T> toPersistent() {
        IPersistentCollection persistent = delegate.persistent();
        IPersistentSet asSet = (IPersistentSet) persistent;
        return ClojureSet.wrap(asSet);
    }
}
