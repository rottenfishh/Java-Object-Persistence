package nsu.sd;

import nsu.sd.lazy.indexing.JsonIndexer;
import nsu.sd.metadata.IndexClassMetadata;
import nsu.sd.tool.filter.Equals;
import nsu.sd.tool.filter.Expression;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class IndexBuilderTest {

    @Test
    void shouldIndexTopLevelAndNestedFieldByLeafName() {
        String json = """
            [
              {
                "@id": 1,
                "_className": "User",
                "price": 100,
                "address": {
                  "price": 200
                },
                "name": "Alice"
              }
            ]
            """;

        Expression filter = new Equals("price", 100);

        List<IndexClassMetadata> result = JsonIndexer.parse(
                new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8)),
                null,
                filter
        );

        assertEquals(1, result.size());

        IndexClassMetadata meta = result.getFirst();
        assertEquals(1, meta.getObjectId());
        assertEquals("User", meta.getClassName());

        assertTrue(meta.getFields().containsKey("price"));
        assertTrue(meta.getFields().containsKey("address.price"));
        System.out.println(meta.getFields().get("price"));
    }
}