package nsu.sd.metadata;

import com.fasterxml.jackson.core.JsonToken;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class IndexFieldMetadata {
    String fieldName;
    String fullPath;
    Long length;
    Integer objectId;
    Long offset;
    JsonToken valueToken;
    Boolean loaded;
}
