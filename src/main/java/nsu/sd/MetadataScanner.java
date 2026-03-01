package nsu.sd;

import nsu.sd.annotations.*;
import nsu.sd.metadata.ClassMetadata;
import nsu.sd.metadata.FieldMetadata;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class MetadataScanner {
    private Map<Class<?>, ClassMetadata> registry;

    public boolean isSerializable(Object object) {
        Class<?> clazz = object.getClass();
        return clazz.isAnnotationPresent(JsonSerializable.class);
    }

    public ClassMetadata getClassMetadata(Object object) {
        Class<?> clazz = object.getClass();
        if (!registry.containsKey(clazz)) {
            ClassMetadata metadata = scanCLass(object, clazz);
            registry.put(clazz, metadata);
            return metadata;
        }
        return registry.get(clazz);
    }

    // TODO: create our own exceptions
    private ClassMetadata scanCLass(Object object, Class<?> clazz) {
        if (Objects.isNull(object)) {
            throw new NullPointerException("object is null");
        }

        if (!isSerializable(object)) {
            throw new IllegalArgumentException("object is not serializable");
        }

        ClassMetadata metadata = new ClassMetadata();
        metadata.setName(clazz.getSimpleName());
        metadata.setClazz(clazz);
        metadata.setSerializable(true);
        metadata.setFields(new HashMap<>());
        for (Field field : clazz.getDeclaredFields()) {
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
            if (element.name() != null) {
                metadata.setName(element.name());
            }
        }
        metadata.setField(field);
        metadata.setType(field.getGenericType());
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
