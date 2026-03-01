package nsu.sd;

import nsu.sd.annotations.*;
import nsu.sd.metadata.ClassMetadata;
import nsu.sd.metadata.FieldMetadata;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Serializer сначала проверяет для объекта isSerializable. Если да, то он вызывает getClassMetadata, чтобы получить их
 */
public class MetadataScanner {
    // TODO: create our own exceptions
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
