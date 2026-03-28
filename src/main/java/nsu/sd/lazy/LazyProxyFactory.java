package nsu.sd.lazy;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.implementation.MethodDelegation;
import nsu.sd.lazy.indexing.IndexedJsonReader;

import static net.bytebuddy.matcher.ElementMatchers.*;

public class LazyProxyFactory {

    public static <T> T createProxy(Class<T> clazz, LazyState state, IndexedJsonReader reader) {
        try {
            Class<? extends T> proxyClass = new ByteBuddy()
                    .subclass(clazz)
                    .defineField("__$lazyState", LazyState.class)
                    .defineField("__$reader", IndexedJsonReader.class)
                    .method(nameStartsWith("get")
                            .and(takesArguments(0))
                            .and(not(isDeclaredBy(Object.class))))
                    .intercept(MethodDelegation.to(LazyGetterInterceptor.class))
                    .make()
                    .load(clazz.getClassLoader(), ClassLoadingStrategy.Default.INJECTION)
                    .getLoaded();

            T instance = proxyClass.getDeclaredConstructor().newInstance();

            var lazyStateField = proxyClass.getDeclaredField("__$lazyState");
            lazyStateField.setAccessible(true);
            lazyStateField.set(instance, state);

            var readerField = proxyClass.getDeclaredField("__$reader");
            readerField.setAccessible(true);
            readerField.set(instance, reader);

            return instance;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create lazy proxy for " + clazz.getName(), e);
        }
    }
}