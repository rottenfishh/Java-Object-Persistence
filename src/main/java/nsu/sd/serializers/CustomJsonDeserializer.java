package nsu.sd.serializers;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import exception.TypeNotSupportedException;
import nsu.sd.metadata.ClassMetadata;
import nsu.sd.metadata.FieldMetadata;
import nsu.sd.MetadataRegistry;
import java.io.IOException;
import java.lang.reflect.Type;

/**
 * Кастомный десериализатор.
 * Работает только для классов, помеченных @JsonSerializable.
 * Создает пустой объект,
 * получает метаданные класса, проходит по ним в цикле и десериализует.
 */
public class CustomJsonDeserializer extends StdDeserializer<Object> {

    private final MetadataRegistry registry;

    public CustomJsonDeserializer(MetadataRegistry registry) {
        super(Object.class);
        this.registry = registry;
    }

    @Override
    public Object deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonNode node = p.getCodec().readTree(p);

        JsonNode classNameNode = node.get("_className");
        if (classNameNode == null) {
            throw new RuntimeException("Unable to deserialize: _className is missing in JSON");
        }

        String className = classNameNode.asText();
        try {
            Class<?> clazz = Class.forName(className);
            Object instance = clazz.getDeclaredConstructor().newInstance();

            ClassMetadata metadata = registry.getClassMetadata(instance);
            for (FieldMetadata fieldMeta : metadata.getFields().values()) {
                if (fieldMeta.isIgnore()) continue;
                if (!fieldMeta.isSupported()) {
                    throw new TypeNotSupportedException("Field " + fieldMeta.getName() + "not supported");
                }

                String jsonFieldName = fieldMeta.getName();
                JsonNode fieldNode = node.get(jsonFieldName);

                if (fieldNode != null && !fieldNode.isNull()) {
                    Type genericType = fieldMeta.getType();
                    ObjectMapper smartMapper = (ObjectMapper) p.getCodec();

                    JavaType jacksonType = smartMapper.getTypeFactory().constructType(genericType);
                    Object value = smartMapper.convertValue(fieldNode, jacksonType);

                    fieldMeta.getField().set(instance, value);
                }
            }
            return instance;
        } catch (Exception e) {
            System.out.println("Error while deserializing class " + className + " " + e);
        }
        return null;
    }
}