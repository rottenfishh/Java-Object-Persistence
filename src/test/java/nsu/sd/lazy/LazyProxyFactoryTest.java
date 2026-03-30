package nsu.sd.lazy;

import com.fasterxml.jackson.databind.ObjectMapper;
import nsu.sd.lazy.indexing.IndexedJsonReader;
import nsu.sd.lazy.indexing.JsonIndexer;
import nsu.sd.lazy.json.LazyProxyFactory;
import nsu.sd.lazy.json.LazyState;
import nsu.sd.metadata.IndexClassMetadata;
import nsu.sd.testClasses.User;
import nsu.sd.tool.filter.Expression;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class LazyProxyFactoryTest {

    @Test
    void shouldLoadFieldLazilyFromGetter() throws Exception {
        String json = """
                [
                  {"name":"Alice","age":20,"city":"Moscow"}
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
        LazyState state = new LazyState(index.get(0), User.class);

        User proxy = LazyProxyFactory.createProxy(User.class, state, reader);

        assertFalse(state.isLoaded("age"));
        assertEquals(20, proxy.getAge());
        assertTrue(state.isLoaded("age"));

        assertFalse(state.isLoaded("city"));
        assertEquals("Moscow", proxy.getCity());
        assertTrue(state.isLoaded("city"));
    }
}