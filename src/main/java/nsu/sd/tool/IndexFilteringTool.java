package nsu.sd.tool;

import com.fasterxml.jackson.databind.ObjectMapper;
import nsu.sd.lazy.indexing.IndexedJsonReader;
import nsu.sd.metadata.IndexClassMetadata;
import nsu.sd.metadata.IndexFieldMetadata;
import nsu.sd.tool.filter.Expression;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class IndexFilteringTool {

    public static List<IndexClassMetadata> filter(
            List<IndexClassMetadata> jsonMetadata,
            Expression filter,
            String filePath
    ) {
        Set<String> neededFields = filter.requiredFields();
        ObjectMapper objectMapper = new ObjectMapper();
        IndexedJsonReader reader = new IndexedJsonReader(filePath, objectMapper);

        List<IndexClassMetadata> result = new ArrayList<>();

        for (IndexClassMetadata metadata : jsonMetadata) {
            Map<String, Object> values = new HashMap<>();

            for (Map.Entry<String, IndexFieldMetadata> entry : metadata.getFields().entrySet()) {
                String fullPath = entry.getKey();
                IndexFieldMetadata fieldMeta = entry.getValue();
                String leafName = fieldMeta.getFieldName();

                if (!(neededFields.contains(fullPath) ||
                        neededFields.contains(leafName))) {
                    continue;
                }

                Object fieldValue = reader.readField(fieldMeta);

                values.put(fullPath, fieldValue);

                if (leafName != null && !values.containsKey(leafName)) {
                    values.put(leafName, fieldValue);
                }
            }

            JsonKeysReader keysReader = new JsonKeysReader(values);

            if (filter.evaluate(keysReader)) {
                result.add(metadata);
            }
        }

        return result;
    }
}