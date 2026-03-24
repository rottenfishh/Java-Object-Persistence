package nsu.sd.serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import exception.TypeNotSupportedException;
import nsu.sd.MetadataRegistry;
import nsu.sd.metadata.ClassMetadata;
import nsu.sd.metadata.FieldMetadata;

import java.io.IOException;
import java.util.IdentityHashMap;

/**
 * Кастомный сериализатор.
 * Работает только для классов, помеченных @JsonSerializable.
 * Получает метаданные класса, проходит по ним в цикле и сериализует.
 * Поддерживает циклические ссылки.
 */
public class CustomJsonSerializer extends StdSerializer<Object> {

    private final MetadataRegistry registry;
    public static final ThreadLocal<IdentityHashMap<Object, Integer>> SEEN_OBJECTS = ThreadLocal.withInitial(IdentityHashMap::new);

    public CustomJsonSerializer(MetadataRegistry registry) {
        super(Object.class);
        this.registry = registry;
    }

    @Override
    public void serialize(Object value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        IdentityHashMap<Object, Integer> seenObjects = SEEN_OBJECTS.get();

        if (seenObjects.containsKey(value)) {
            gen.writeStartObject();
            gen.writeNumberField("@ref", seenObjects.get(value));
            gen.writeEndObject();
            return;
        }

        int newId = seenObjects.size() + 1;
        seenObjects.put(value, newId);

        ClassMetadata metadata = registry.getClassMetadata(value);

        gen.writeStartObject();
        gen.writeNumberField("@id", newId);
        gen.writeStringField("_className", metadata.getClazz().getName());

        for (FieldMetadata fieldMeta : metadata.getFields().values()) {

            if (fieldMeta.isIgnore()) {
                continue;
            }
            if (!fieldMeta.isSupported()) {
                throw new TypeNotSupportedException("Field " + fieldMeta.getType() + "not supported");
            }

            try {
                Object fieldValue = fieldMeta.getField().get(value);
                gen.writeObjectField(fieldMeta.getName(), fieldValue);

            } catch (IllegalAccessException e) {
                System.out.println("Error while accessing field: " + fieldMeta.getName() + " " + e);
            }
        }

        gen.writeEndObject();
    }
}
