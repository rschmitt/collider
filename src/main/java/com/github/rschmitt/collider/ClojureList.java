package com.github.rschmitt.collider;

import com.github.rschmitt.collider.internal.DelegateFactory;

import net.fushizen.invokedynamic.proxy.DynamicProxy;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collector;

import clojure.lang.IEditableCollection;
import clojure.lang.IPersistentVector;
import clojure.lang.ITransientCollection;
import clojure.lang.ITransientVector;
import clojure.lang.PersistentVector;

public abstract class ClojureList<T> implements List<T> {
    private static final DynamicProxy dynamicProxy = DelegateFactory.create(List.class, ClojureList.class);

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
        try {
            return ((ClojureList<T>) dynamicProxy.constructor().invoke(ts));
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
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
}
