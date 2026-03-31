package nsu.sd.metadata;

import lombok.Getter;
import lombok.Setter;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class IndexClassMetadata {
    String className;
    Integer objectId;
    Long offset;
    Long length;
    Type type;
    Boolean loaded;
    Map<String, IndexFieldMetadata> fields;

    public IndexClassMetadata() {
        fields = new HashMap<>();
    }
}
