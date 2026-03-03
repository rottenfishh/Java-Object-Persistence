package nsu.sd.serializers;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import nsu.sd.metadata.ClassMetadata;
import nsu.sd.metadata.FieldMetadata;
import nsu.sd.MetadataRegistry;
import java.io.IOException;

/**
 * Кастомный десериализатор.
 * Работает только для классов, помеченных @JsonSerializable.
 * Создает пустой объект,
 * получает метаданные класса, проходит по ним в цикле и десериализует.
 */
public class CustomJsonDeserializer extends StdDeserializer<Object> {

    private final MetadataRegistry registry;
    private final ObjectMapper basicMapper;

    public CustomJsonDeserializer(MetadataRegistry registry) {
        super(Object.class);
        this.registry = registry;
        this.basicMapper = new ObjectMapper();
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

                String jsonFieldName = fieldMeta.getName();
                JsonNode fieldNode = node.get(jsonFieldName);

                if (fieldNode != null && !fieldNode.isNull()) {
                    Class<?> targetClass = fieldMeta.getField().getType();
                    Object value = p.getCodec().treeToValue(fieldNode, targetClass);

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