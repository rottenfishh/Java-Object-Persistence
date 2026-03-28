package nsu.sd.lazy;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.BeanDeserializerModifier;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import nsu.sd.MetadataRegistry;
import nsu.sd.serializers.CustomJsonSerializer;
import nsu.sd.serializers.LazyCustomJsonDeserializer;

import java.io.File;
import java.io.IOException;

public class LazyJsonMapper {
    private final ObjectMapper mapper;
    private final MetadataRegistry registry;

    public LazyJsonMapper() {
        this.registry = new MetadataRegistry();
        this.mapper = new ObjectMapper();
        this.mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);

        SimpleModule module = new SimpleModule();

        module.setSerializerModifier(new BeanSerializerModifier() {
            @Override
            public JsonSerializer<?> modifySerializer(
                    SerializationConfig config,
                    BeanDescription beanDesc,
                    JsonSerializer<?> defaultSerializer
            ) {
                if (MetadataRegistry.isSerializable(beanDesc.getBeanClass())) {
                    return new CustomJsonSerializer(registry);
                }
                return defaultSerializer;
            }
        });

        module.setDeserializerModifier(new BeanDeserializerModifier() {
            @Override
            public JsonDeserializer<?> modifyDeserializer(
                    DeserializationConfig config,
                    BeanDescription beanDesc,
                    JsonDeserializer<?> deserializer
            ) {
                if (MetadataRegistry.isSerializable(beanDesc.getBeanClass())) {
                    return new LazyCustomJsonDeserializer(registry);
                }
                return deserializer;
            }
        });

        mapper.registerModule(module);
    }

    public String toJson(Object obj) throws JsonProcessingException {
        CustomJsonSerializer.SEEN_OBJECTS.get().clear();
        try {
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
        } finally {
            //CustomJsonSerializer.SEEN_OBJECTS.get().clear();
        }
    }

    public Object fromJson(String json, Class<?> clazz) throws JsonProcessingException {
        LazyCustomJsonDeserializer.RESOLVED_OBJECTS.get().clear();
        try {
            return mapper.readValue(json, clazz);
        } finally {
            LazyCustomJsonDeserializer.RESOLVED_OBJECTS.get().clear();
        }
    }

    public void toJsonFile(File file, Object obj) throws IOException {
        CustomJsonSerializer.SEEN_OBJECTS.get().clear();
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(file, obj);
        } finally {
            CustomJsonSerializer.SEEN_OBJECTS.get().clear();
        }
    }

    public Object fromJsonFile(File file, Class<?> clazz) throws IOException {
        LazyCustomJsonDeserializer.RESOLVED_OBJECTS.get().clear();
        try {
            return mapper.readValue(file, clazz);
        } finally {
            LazyCustomJsonDeserializer.RESOLVED_OBJECTS.get().clear();
        }
    }
}