package nsu.sd.lazy.nodeJson;

import net.bytebuddy.implementation.bind.annotation.FieldValue;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;

import java.lang.reflect.Method;

public class LazyNodeGetterInterceptor {

    @RuntimeType
    public static Object intercept(
            @Origin Method method,
            @FieldValue("__$lazyNodeValue") LazyNodeValue lazyValue
    ) throws Exception {
        Object target = lazyValue.get();
        return method.invoke(target);
    }
}