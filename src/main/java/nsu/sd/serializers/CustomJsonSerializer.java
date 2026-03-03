package nsu.sd.serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import nsu.sd.MetadataRegistry;
import nsu.sd.metadata.ClassMetadata;
import nsu.sd.metadata.FieldMetadata;

import java.io.IOException;

public class CustomJsonSerializer extends StdSerializer<Object> {

    private final MetadataRegistry registry;

    public CustomJsonSerializer(MetadataRegistry registry) {
        super(Object.class);
        this.registry = registry;
    }

    @Override
    public void serialize(Object value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        ClassMetadata metadata = registry.getClassMetadata(value);

        gen.writeStartObject();
        gen.writeStringField("_className", metadata.getClazz().getName());

        for (FieldMetadata fieldMeta : metadata.getFields().values()) {

            if (fieldMeta.isIgnore()) {
                continue;
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
