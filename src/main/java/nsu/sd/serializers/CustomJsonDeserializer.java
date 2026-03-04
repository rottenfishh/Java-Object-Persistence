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
    private final Class<?> targetClass;

    public CustomJsonDeserializer(MetadataRegistry registry, Class<?> targetClass) {
        super(targetClass);
        this.registry = registry;
        this.basicMapper = new ObjectMapper();
        this.targetClass = targetClass;
    }

    @Override
    public Object deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonNode node = p.getCodec().readTree(p);

        try {
            Object instance = targetClass.getDeclaredConstructor().newInstance();

            ClassMetadata metadata = registry.getClassMetadata(instance);
            for (FieldMetadata fieldMeta : metadata.getFields().values()) {
                if (fieldMeta.isIgnore()) continue;

                String jsonFieldName = fieldMeta.getName();
                JsonNode fieldNode = node.get(jsonFieldName);

                if (fieldNode != null && !fieldNode.isNull()) {
                    Class<?> fieldClassType = fieldMeta.getField().getType();
                    Object value = p.getCodec().treeToValue(fieldNode, fieldClassType);
                    fieldMeta.getField().set(instance, value);
                }
            }
            return instance;
        } catch (Exception e) {
            System.out.println("Error while deserializing class " + targetClass.getName() + " " + e);
        }
        return null;
    }
}