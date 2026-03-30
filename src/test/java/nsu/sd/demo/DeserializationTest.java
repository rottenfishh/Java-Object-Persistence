package nsu.sd.demo;

import nsu.sd.MetadataRegistry;
import nsu.sd.lazy.LazyJsonMapper;
import nsu.sd.serializers.LazyCustomJsonDeserializer;
import nsu.sd.testClasses.Project;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class DeserializationTest {
    private LazyJsonMapper mapper;
    private MetadataRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new MetadataRegistry();
        mapper = new LazyJsonMapper();

        LazyCustomJsonDeserializer.RESOLVED_OBJECTS.get().clear();
    }

    @Test
    public void testNestedObject() throws IOException {
        File file = new File("src/test/resources/nested.json");
        Project project = (Project) mapper.fromJsonFile(file, Project.class);
        assertNotNull(project);
        assertEquals("Bob", project.backend.name);
    }
}
