package nsu.sd.lazy.nodeJson;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class LazyNodeValue {
    private final JsonNode node;
    private final ObjectMapper mapper;
    private final JavaType type;

    private volatile boolean loaded;
    private Object value;

    public LazyNodeValue(JsonNode node, ObjectMapper mapper, JavaType type) {
        this.node = node;
        this.mapper = mapper;
        this.type = type;
    }

    public Object get() {
        if (!loaded) {
            synchronized (this) {
                if (!loaded) {
                    value = mapper.convertValue(node, type);
                    loaded = true;
                }
            }
        }
        return value;
    }
}