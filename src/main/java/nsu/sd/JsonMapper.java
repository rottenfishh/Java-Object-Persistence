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
                    return new CustomJsonDeserializer(registry, beanDesc.getBeanClass());
                }
                return deserializer;
            }
        });

        mapper.registerModule(module);
    }

    // Для строк
    public String toJson(Object obj) throws JsonProcessingException {
        return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
    }

    public Object fromJson(String json, Class<?> clazz) throws JsonProcessingException {
        return mapper.readValue(json, clazz);
    }

    // Для файлов
    public void toJsonFile(File file, Object obj) throws IOException {
        mapper.writerWithDefaultPrettyPrinter().writeValue(file, obj);
    }

    public Object fromJsonFile(File file, Class<?> clazz) throws IOException {
        return mapper.readValue(file, clazz);
    }
}