package nsu.sd;

import nsu.sd.annotations.JsonSerializable;
import nsu.sd.metadata.ClassMetadata;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Точка входа к метаданным.
 * Serializer должен проверить, что object isSerializable, а далее если да, взять его из registry
 */
public class MetadataRegistry {
    private final Map<Class<?>, ClassMetadata> registry;
    MetadataScanner scanner;

    public MetadataRegistry() {
        registry = new HashMap<>();
        scanner = new MetadataScanner();
    }

    public static boolean isSerializable(Object object) {
        Class<?> clazz = object.getClass();
        return clazz.isAnnotationPresent(JsonSerializable.class);
    }

    public ClassMetadata getClassMetadata(Object object) {
        if (Objects.isNull(object)) {
            throw new IllegalArgumentException("Object is null");
        }

        if (!isSerializable(object)) {
            throw new IllegalArgumentException("Object is not serializable");
        }

        Class<?> clazz = object.getClass();
        if (!registry.containsKey(clazz)) {
            ClassMetadata metadata = scanner.scanCLass(clazz);
            registry.put(clazz, metadata);
            return metadata;
        }
        return registry.get(clazz);
    }
}
