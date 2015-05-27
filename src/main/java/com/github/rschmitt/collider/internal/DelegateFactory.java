package com.github.rschmitt.collider.internal;

import net.fushizen.invokedynamic.proxy.DynamicProxy;

import java.util.Collection;

public class DelegateFactory {
    public static DynamicProxy create(Class interfaceClass, Class delegateClass) {
        try {
            return DynamicProxy.builder()
                    .withInterfaces(interfaceClass, Collection.class, Iterable.class)
                    .withSuperclass(delegateClass)
                    .withConstructor(Object.class)
                    .withInvocationHandler(new DelegationHandler(interfaceClass, delegateClass))
                    .build();
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }
}
