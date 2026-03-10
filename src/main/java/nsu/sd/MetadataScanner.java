package nsu.sd;

import nsu.sd.annotations.*;
import nsu.sd.metadata.ClassMetadata;
import nsu.sd.metadata.FieldMetadata;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

/**
 * сканирует аннотации из класса.
 * если поле = контейнер, проверяется, входит ли он в список поддерживаемых
 * если нет, то supported = false.
 * во всех остальных случаях, supported = true
 */
public class MetadataScanner {
    // TODO: create our own exceptions
    private static final Set<Class<?>> supportedContainerTypes = Set.of(List.class, ArrayList.class, LinkedList.class,
            Map.class, HashMap.class, LinkedHashMap.class, Set.class, HashSet.class, LinkedHashSet.class);

    public ClassMetadata scanCLass(Class<?> clazz) {
        ClassMetadata metadata = new ClassMetadata();
        metadata.setName(clazz.getSimpleName());
        metadata.setClazz(clazz);
        metadata.setSerializable(true);
        metadata.setFields(new HashMap<>());
        for (Field field : clazz.getDeclaredFields()) {
            field.setAccessible(true);
            FieldMetadata fieldMetadata = getFieldMetadata(field);
            metadata.getFields().put(field.getName(), fieldMetadata);
        }
        return metadata;
    }

    private FieldMetadata getFieldMetadata(Field field) {
        FieldMetadata metadata = new FieldMetadata();
        metadata.setName(field.getName());
        if (field.isAnnotationPresent(JsonElement.class)) {
            JsonElement element = field.getAnnotation(JsonElement.class);
            if (!Objects.equals(element.name(), "")) {
                metadata.setName(element.name());
            }
        }
        metadata.setField(field);
        Type type = field.getGenericType();
        metadata.setSupported(true);

        if (type instanceof ParameterizedType pt) {
            Class<?> raw = (Class<?>) pt.getRawType();
            if (!supportedContainerTypes.contains(raw)) {
                metadata.setSupported(false);
            }
            if (raw == Map.class) {
                Type keyType = pt.getActualTypeArguments()[0];
                if (keyType != String.class) {
                    metadata.setSupported(false);
                }
            }
        }

        metadata.setType(type);

        if (field.isAnnotationPresent(JsonLazy.class)){
            metadata.setLazy(true);
        }
        if (field.isAnnotationPresent(JsonIgnore.class)) {
            metadata.setIgnore(true);
        }
        if (field.isAnnotationPresent(JsonUnwrapped.class)) {
            metadata.setUnwrapped(true);
        }
        return metadata;
    }
}
