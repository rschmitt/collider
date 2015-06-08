package com.github.rschmitt.collider;

import clojure.lang.IPersistentCollection;
import clojure.lang.IPersistentSet;
import clojure.lang.ITransientSet;
import clojure.lang.PersistentHashSet;

import javax.annotation.concurrent.NotThreadSafe;
import java.util.Collection;

@NotThreadSafe
public class TransientSet<T> {
    private volatile ITransientSet delegate;

    TransientSet() {
        this.delegate = (ITransientSet) PersistentHashSet.EMPTY.asTransient();
    }

    TransientSet(ITransientSet delegate) {
        this.delegate = delegate;
    }

    public void add(T t) {
        this.delegate = (ITransientSet) delegate.conj(t);
    }

    public void addAll(Collection<? extends T> collection) {
        for (T t : collection) {
            add(t);
        }
    }

    public void remove(Object o) {
        this.delegate = delegate.disjoin(o);
    }

    public ClojureSet<T> toPersistent() {
        IPersistentCollection persistent = delegate.persistent();
        IPersistentSet asSet = (IPersistentSet) persistent;
        return ClojureSet.wrap(asSet);
    }
}
