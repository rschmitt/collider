package com.github.rschmitt.collider.internal;

import net.fushizen.invokedynamic.proxy.DynamicInvocationHandler;

import java.lang.invoke.*;
import java.lang.reflect.Field;

public class DelegationHandler implements DynamicInvocationHandler {
    private final Class interfaceClass;
    private final Class delegateClass;

    public DelegationHandler(Class interfaceClass, Class delegateClass) {
        this.interfaceClass = interfaceClass;
        this.delegateClass = delegateClass;
    }

    @Override
    public CallSite handleInvocation(
            MethodHandles.Lookup lookup,
            String methodName,
            MethodType methodType,
            MethodHandle superMethod
    ) throws Throwable {
        if (superMethod != null)
            try {
                // Before binding to superMethod, we have to verify that we're not actually binding
                // to a method supplied by java.lang.Object, or a default superinterface method.
                delegateClass.getDeclaredMethod(methodName, methodType.dropParameterTypes(0, 1).parameterArray());
                return new ConstantCallSite(superMethod.asType(methodType));
            } catch (NoSuchMethodException ignore) { }

        Field delegateField = delegateClass.getDeclaredField("delegate");
        delegateField.setAccessible(true);
        MethodHandle getterHandle = lookup.unreflectGetter(delegateField);

        MethodHandle targetMethod = lookup.findVirtual(interfaceClass, methodName, methodType.dropParameterTypes(0, 1));
        MethodHandle delegatingMethod = MethodHandles.filterArguments(targetMethod, 0, getterHandle);
        return new ConstantCallSite(delegatingMethod.asType(methodType));
    }
}
