package nsu.sd.lazy.nodeJson;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.implementation.MethodDelegation;

import static net.bytebuddy.matcher.ElementMatchers.*;

public class LazyNodeProxyFactory {

    public static <T> T createProxy(Class<T> clazz, LazyNodeValue lazyValue) {
        try {
            Class<? extends T> proxyClass = new ByteBuddy()
                    .subclass(clazz)
                    .defineField("__$lazyNodeValue", LazyNodeValue.class)
                    .method(
                            not(isDeclaredBy(Object.class))
                                    .and(isPublic())
                                    .and(not(isFinalizer()))
                    )
                    .intercept(MethodDelegation.to(LazyNodeGetterInterceptor.class))
                    .make()
                    .load(clazz.getClassLoader(), ClassLoadingStrategy.Default.INJECTION)
                    .getLoaded();

            T instance = proxyClass.getDeclaredConstructor().newInstance();

            var field = proxyClass.getDeclaredField("__$lazyNodeValue");
            field.setAccessible(true);
            field.set(instance, lazyValue);

            return instance;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create lazy node proxy for " + clazz.getName(), e);
        }
    }
}