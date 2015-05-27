package com.github.rschmitt.collider;

import java.util.Collection;

import clojure.lang.IPersistentCollection;
import clojure.lang.IPersistentVector;
import clojure.lang.ITransientVector;
import clojure.lang.PersistentVector;

public class TransientList<T> {
    private volatile ITransientVector delegate;

    TransientList() {
        this.delegate = PersistentVector.EMPTY.asTransient();
    }

    TransientList(ITransientVector delegate) {
        this.delegate = delegate;
    }

    public void append(T t) {
        this.delegate = (ITransientVector) delegate.conj(t);
    }

    public void appendAll(Collection<? extends T> c) {
        for (T t : c) {
            append(t);
        }
    }

    public ClojureList<T> toPersistent() {
        IPersistentCollection persistent = delegate.persistent();
        IPersistentVector asVector = (IPersistentVector) persistent;
        return ClojureList.wrap(asVector);
    }
}
