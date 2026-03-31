package nsu.sd.lazy;

import nsu.sd.lazy.indexing.JsonIndexer;
import nsu.sd.metadata.IndexClassMetadata;
import nsu.sd.tool.filter.Expression;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class JsonIndexerTest {

    @Test
    void shouldIndexTopLevelFields() {
        String json = """
                [
                  {"name":"Alice","age":20,"city":"Moscow"},
                  {"name":"Bob","age":17,"city":"SPb"}
                ]
                """;

        Expression expr = new Expression() {
            @Override
            public boolean evaluate(nsu.sd.tool.JsonKeysReader reader) {
                return true;
            }

            @Override
            public Set<String> requiredFields() {
                return Set.of("name", "age", "city");
            }
        };

        List<IndexClassMetadata> index = JsonIndexer.parse(
                new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8)),
                null,
                expr
        );

        assertEquals(2, index.size());

        IndexClassMetadata first = index.getFirst();
        assertTrue(first.getFields().containsKey("name"));
        assertTrue(first.getFields().containsKey("age"));
        assertTrue(first.getFields().containsKey("city"));
    }
}