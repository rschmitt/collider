package com.github.rschmitt.collider;

import com.github.rschmitt.collider.internal.DelegateFactory;

import net.fushizen.invokedynamic.proxy.DynamicProxy;

import java.util.EnumSet;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collector;

import clojure.lang.IEditableCollection;
import clojure.lang.IPersistentSet;
import clojure.lang.ITransientCollection;
import clojure.lang.ITransientSet;
import clojure.lang.PersistentHashSet;

import static java.util.stream.Collector.Characteristics.UNORDERED;

public abstract class ClojureSet<T> implements Set<T> {
    private static final DynamicProxy dynamicProxy = DelegateFactory.create(Set.class, ClojureSet.class);

    private final Set<T> delegate;

    protected ClojureSet(Object delegate) {
        this.delegate = (Set) delegate;
    }

    static <T> ClojureSet<T> create(T... ts) {
        return create(PersistentHashSet.create(ts));
    }

    static <T> ClojureSet<T> wrap(IPersistentSet clojureSet) {
        return create(clojureSet);
    }

    @SuppressWarnings("unchecked")
    private static <T> ClojureSet<T> create(IPersistentSet ts) {
        try {
            return ((ClojureSet<T>) dynamicProxy.constructor().invoke(ts));
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
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

    public TransientSet<T> asTransient() {
        IEditableCollection asEditable = (IEditableCollection) delegate;
        ITransientCollection asTransient = asEditable.asTransient();
        return new TransientSet((ITransientSet) asTransient);
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
}
