package nsu.sd;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.BeanDeserializerModifier;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import nsu.sd.serializers.CustomJsonDeserializer;
import nsu.sd.serializers.CustomJsonSerializer;

import java.io.File;
import java.io.IOException;

/**
 * Главный класс, который настраивает перехват работы джексона кастомными (де)сериализаторами.
 * Для вызовов сериализации и десериализации нужно использовать его.
 */
public class JsonMapper {
    private final ObjectMapper mapper;
    private final MetadataRegistry registry;

    public JsonMapper() {
        this.registry = new MetadataRegistry();
        this.mapper = new ObjectMapper();
        this.mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);

        SimpleModule module = new SimpleModule();

        module.setSerializerModifier(new BeanSerializerModifier() {
            @Override
            public JsonSerializer<?> modifySerializer(SerializationConfig config,
                                                      BeanDescription beanDesc, JsonSerializer<?> defaultSerializer) {
                if (MetadataRegistry.isSerializable(beanDesc.getBeanClass())) {
                    return new CustomJsonSerializer(registry);
                }
                return defaultSerializer;
            }
        });

        module.setDeserializerModifier(new BeanDeserializerModifier() {
            @Override
            public JsonDeserializer<?> modifyDeserializer(DeserializationConfig config,
                                                          BeanDescription beanDesc, JsonDeserializer<?> deserializer) {
                if (MetadataRegistry.isSerializable(beanDesc.getBeanClass())) {
                    return new CustomJsonDeserializer(registry);
                }
                return deserializer;
            }
        });

        mapper.registerModule(module);
    }

    // Для строк
    public String toJson(Object obj) throws JsonProcessingException   {
        CustomJsonSerializer.SEEN_OBJECTS.get().clear();
        try {
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
        } finally {
            CustomJsonSerializer.SEEN_OBJECTS.get().clear();
        }
    }

    public Object fromJson(String json, Class<?> clazz) throws JsonMappingException, JsonProcessingException   {
        CustomJsonDeserializer.RESOLVED_OBJECTS.get().clear();
        try {
            return mapper.readValue(json, clazz);
        } finally {
            CustomJsonDeserializer.RESOLVED_OBJECTS.get().clear();
        }
    }

    // Для файлов
    public void toJsonFile(File file, Object obj) throws IOException {
        CustomJsonSerializer.SEEN_OBJECTS.get().clear();
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(file, obj);
        } finally {
            CustomJsonSerializer.SEEN_OBJECTS.get().clear();
        }
    }

    public Object fromJsonFile(File file, Class<?> clazz) throws IOException {
        CustomJsonDeserializer.RESOLVED_OBJECTS.get().clear();
        try {
            return mapper.readValue(file, clazz);
        } finally {
            CustomJsonDeserializer.RESOLVED_OBJECTS.get().clear();
        }
    }
}