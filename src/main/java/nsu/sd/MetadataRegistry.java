package nsu.sd;

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

    public ClassMetadata getClassMetadata(Object object) {
        if (Objects.isNull(object)) {
            throw new IllegalArgumentException("object is null");
        }

        Class<?> clazz = object.getClass();
        if (!registry.containsKey(clazz)) {
            ClassMetadata metadata = scanner.getClassMetadata(object);
            registry.put(clazz, metadata);
            return metadata;
        }
        return registry.get(clazz);
    }
}
