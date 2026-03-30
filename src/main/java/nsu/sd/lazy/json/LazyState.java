package nsu.sd.lazy.json;

import lombok.Getter;
import nsu.sd.metadata.IndexClassMetadata;
import nsu.sd.metadata.IndexFieldMetadata;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LazyState {
    private final IndexClassMetadata objectMeta;
    private final Map<String, Object> loadedFields = new ConcurrentHashMap<>();
    @Getter
    private final Class<?> modelClass;

    public LazyState(IndexClassMetadata objectMeta, Class<?> modelClass) {
        this.objectMeta = objectMeta;
        this.modelClass = modelClass;
    }

    public boolean hasHandle(String fieldName) {
        return objectMeta.getFields().containsKey(fieldName);
    }

    public IndexFieldMetadata getHandle(String fieldName) {
        return objectMeta.getFields().get(fieldName);
    }

    public boolean isLoaded(String fieldName) {
        return loadedFields.containsKey(fieldName);
    }

    public Object getLoaded(String fieldName) {
        return loadedFields.get(fieldName);
    }

    public void setLoaded(String fieldName, Object value) {
        loadedFields.put(fieldName, value);
    }
}