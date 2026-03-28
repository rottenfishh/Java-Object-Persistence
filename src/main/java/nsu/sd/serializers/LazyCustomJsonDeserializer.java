package nsu.sd.serializers;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import exception.TypeNotSupportedException;
import nsu.sd.lazy.LazyNodeProxyFactory;
import nsu.sd.lazy.LazyNodeValue;
import nsu.sd.metadata.ClassMetadata;
import nsu.sd.metadata.FieldMetadata;
import nsu.sd.MetadataRegistry;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * Кастомный десериализатор.
 * Работает только для классов, помеченных @JsonSerializable.
 * Создает пустой объект,
 * получает метаданные класса, проходит по ним в цикле и десериализует.
 */
public class LazyCustomJsonDeserializer extends StdDeserializer<Object> {

    private final MetadataRegistry registry;
    public static final ThreadLocal<Map<Integer, Object>> RESOLVED_OBJECTS = ThreadLocal.withInitial(HashMap::new);

    public LazyCustomJsonDeserializer(MetadataRegistry registry) {
        super(Object.class);
        this.registry = registry;
    }

    @Override
    public Object deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        Map<Integer, Object> resolvedObjects = RESOLVED_OBJECTS.get();

        JsonNode node = p.getCodec().readTree(p);

        if (node.has("@ref")) {
            int refId = node.get("@ref").asInt();
            Object existingObj = resolvedObjects.get(refId);
            if (existingObj == null) {
                throw new RuntimeException("Wrong reference! Object with ID " + refId + " not found.");
            }
            return existingObj;
        }

        JsonNode classNameNode = node.get("_className");
        if (classNameNode == null) {
            throw new RuntimeException("Unable to deserialize: _className is missing in JSON");
        }

        String className = classNameNode.asText();
        int objId = node.has("@id") ? node.get("@id").asInt() : -1;
        try {
            Class<?> clazz = Class.forName(className);
            Object instance = clazz.getDeclaredConstructor().newInstance();

            if (objId != -1) {
                resolvedObjects.put(objId, instance);
            }

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

                    Object value;

                    if (fieldNode.has("@ref")) {
                        int refId = fieldNode.get("@ref").asInt();
                        Object existingObj = resolvedObjects.get(refId);
                        if (existingObj == null) {
                            throw new RuntimeException("Wrong reference! Object with ID " + refId + " not found.");
                        }
                        value = existingObj;
                    } else if (fieldMeta.isLazy() && fieldNode.isObject()) {
                        Class<?> rawClass = jacksonType.getRawClass();

                        LazyNodeValue lazyNodeValue = new LazyNodeValue(fieldNode, smartMapper, jacksonType);
                        value = LazyNodeProxyFactory.createProxy(rawClass, lazyNodeValue);
                    } else {
                        value = smartMapper.convertValue(fieldNode, jacksonType);
                    }

                    fieldMeta.getField().set(instance, value);
                }
            }
            return instance;
        }  catch (Exception e) {
        throw new RuntimeException("Error while deserializing class " + className, e);
    }
    }
}