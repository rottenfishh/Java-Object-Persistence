package nsu.sd.lazy.json;

import net.bytebuddy.implementation.bind.annotation.FieldValue;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.SuperCall;
import nsu.sd.lazy.indexing.IndexedJsonReader;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.Callable;

public class LazyGetterInterceptor {

    @RuntimeType
    public static Object intercept(
            @Origin Method method,
            @SuperCall Callable<?> zuper,
            @FieldValue("__$lazyState") LazyState lazyState,
            @FieldValue("__$reader") IndexedJsonReader reader
    ) throws Exception {

        String name = method.getName();
        if (!name.startsWith("get") || method.getParameterCount() != 0 || name.length() <= 3) {
            return zuper.call();
        }

        String fieldName = Character.toLowerCase(name.charAt(3)) + name.substring(4);

        if (!lazyState.hasHandle(fieldName)) {
            return zuper.call();
        }

        if (lazyState.isLoaded(fieldName)) {
            return lazyState.getLoaded(fieldName);
        }

        Field field = findField(lazyState.getModelClass(), fieldName);
        Class<?> type = field.getType();

        Object value = reader.readField(lazyState.getHandle(fieldName), type);
        lazyState.setLoaded(fieldName, value);
        return value;
    }

    private static Field findField(Class<?> clazz, String fieldName) throws NoSuchFieldException {
        Class<?> cur = clazz;
        while (cur != null) {
            try {
                Field field = cur.getDeclaredField(fieldName);
                field.setAccessible(true);
                return field;
            } catch (NoSuchFieldException ignored) {
                cur = cur.getSuperclass();
            }
        }
        throw new NoSuchFieldException(fieldName);
    }
}