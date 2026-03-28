package nsu.sd.lazy;

import com.fasterxml.jackson.databind.ObjectMapper;
import nsu.sd.lazy.indexing.IndexedJsonReader;
import nsu.sd.lazy.indexing.JsonIndexer;
import nsu.sd.metadata.IndexClassMetadata;
import nsu.sd.testClasses.User;
import nsu.sd.tool.filter.Expression;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class LazyJsonFilterTest {

    @Test
    void shouldFilterObjectsLazily() throws Exception {
        String json = """
                [
                  {"name":"Alice","age":20,"city":"Moscow"},
                  {"name":"Bob","age":17,"city":"SPb"},
                  {"name":"Carol","age":25,"city":"Moscow"}
                ]
                """;

        Path tempFile = Files.createTempFile("users", ".json");
        Files.writeString(tempFile, json, StandardCharsets.UTF_8);

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

        IndexedJsonReader reader = new IndexedJsonReader(tempFile.toString(), new ObjectMapper());

        LazyObjectFilter<User> filter = user ->
                user.getAge() != null &&
                        user.getAge() >= 18 &&
                        "Moscow".equals(user.getCity());

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        IndexClassMetadata first = index.get(0);

        Object rawCity = reader.readField(first.getFields().get("city"), String.class);

        nsu.sd.tool.LazyFilter.filter(index, User.class, reader, filter, out);

        String result = out.toString(StandardCharsets.UTF_8);

        assertTrue(result.contains("Alice"));
        assertTrue(result.contains("Carol"));
        assertFalse(result.contains("Bob"));
    }
}