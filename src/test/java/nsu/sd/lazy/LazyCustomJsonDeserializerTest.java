package nsu.sd.lazy;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import nsu.sd.MetadataRegistry;
import nsu.sd.metadata.ClassMetadata;
import nsu.sd.metadata.FieldMetadata;
import nsu.sd.serializers.LazyCustomJsonDeserializer;
import nsu.sd.testClasses.Profile;
import nsu.sd.testClasses.UserWithLazyProfile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class LazyCustomJsonDeserializerTest {

    private ObjectMapper mapper;
    private MetadataRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new FakeMetadataRegistry();
        mapper = new ObjectMapper();

        SimpleModule module = new SimpleModule();
        module.addDeserializer(Object.class, new LazyCustomJsonDeserializer(registry));
        mapper.registerModule(module);

        LazyCustomJsonDeserializer.RESOLVED_OBJECTS.get().clear();
    }

    @Test
    void shouldDeserializeNonLazyFieldsNormally() throws Exception {
        String json = """
                {
                  "_className": "nsu.sd.testClasses.UserWithLazyProfile",
                  "name": "Alice"
                }
                """;

        Object result = read(json);

        assertNotNull(result);
        assertInstanceOf(UserWithLazyProfile.class, result);

        UserWithLazyProfile user = (UserWithLazyProfile) result;
        assertEquals("Alice", user.getName());
        assertNull(user.getProfile());
    }

    @Test
    void shouldCreateLazyProxyForLazyField() throws Exception {
        String json = """
                {
                  "_className": "nsu.sd.testClasses.UserWithLazyProfile",
                  "name": "Alice",
                  "profile": {
                    "city": "Moscow",
                    "age": 20
                  }
                }
                """;

        Object result = read(json);

        assertNotNull(result);
        UserWithLazyProfile user = (UserWithLazyProfile) result;

        assertEquals("Alice", user.getName());
        assertNotNull(user.getProfile());

        // проверяем, что это не обычный Profile, а proxy-класс
        assertNotEquals(Profile.class, user.getProfile().getClass());
    }

    @Test
    void shouldLoadLazyFieldOnFirstGetterCall() throws Exception {
        String json = """
                {
                  "_className": "nsu.sd.testClasses.UserWithLazyProfile",
                  "name": "Alice",
                  "profile": {
                    "city": "Moscow",
                    "age": 20
                  }
                }
                """;

        UserWithLazyProfile user = (UserWithLazyProfile) read(json);

        assertNotNull(user.getProfile());

        // здесь уже должен произойти ленивый convertValue из JsonNode в Profile
        assertEquals("Moscow", user.getProfile().getCity());
        assertEquals(20, user.getProfile().getAge());
    }

    @Test
    void shouldResolveReferenceByRef() throws Exception {
        String firstJson = """
                {
                  "_className": "nsu.sd.testClasses.UserWithLazyProfile",
                  "@id": 1,
                  "name": "Alice"
                }
                """;

        String refJson = """
                {
                  "@ref": 1
                }
                """;

        Object first = read(firstJson);
        Object second = read(refJson);

        assertSame(first, second);
    }

    @Test
    void shouldThrowWhenClassNameMissing() {
        String json = """
                {
                  "name": "Alice"
                }
                """;

        RuntimeException ex = assertThrows(RuntimeException.class, () -> read(json));
        assertTrue(ex.getMessage().contains("_className is missing"));
    }

    @Test
    void shouldThrowWhenRefNotFound() {
        String json = """
                {
                  "@ref": 999
                }
                """;

        RuntimeException ex = assertThrows(RuntimeException.class, () -> read(json));
        assertTrue(ex.getMessage().contains("Object with ID 999 not found"));
    }

    @Test
    void shouldSkipNullLazyField() throws Exception {
        String json = """
            {
              "_className": "nsu.sd.testClasses.UserWithLazyProfile",
              "name": "Alice",
              "profile": null
            }
            """;

        UserWithLazyProfile user = (UserWithLazyProfile) read(json);

        assertEquals("Alice", user.getName());
        assertNull(user.getProfile());
    }

    private Object read(String json) throws Exception {
        JsonParser parser = mapper.getFactory().createParser(json);
        parser.nextToken();
        return mapper.readValue(parser, Object.class);
    }

    /**
     * Фейковый registry только для теста.
     */
    static class FakeMetadataRegistry extends MetadataRegistry {
        @Override
        public ClassMetadata getClassMetadata(Object instance) {
            try {
                if (instance instanceof UserWithLazyProfile) {
                    return userMetadata();
                }
                if (instance instanceof Profile) {
                    return profileMetadata();
                }
                throw new IllegalArgumentException("Unsupported test class: " + instance.getClass());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        private ClassMetadata userMetadata() throws Exception {
            ClassMetadata metadata = new ClassMetadata();
            Map<String, FieldMetadata> fields = new LinkedHashMap<>();

            Field nameField = UserWithLazyProfile.class.getDeclaredField("name");
            nameField.setAccessible(true);

            FieldMetadata nameMeta = new FieldMetadata();
            nameMeta.setName("name");
            nameMeta.setField(nameField);
            nameMeta.setType(nameField.getGenericType());
            nameMeta.setSupported(true);
            nameMeta.setIgnore(false);
            nameMeta.setLazy(false);

            fields.put("name", nameMeta);

            Field profileField = UserWithLazyProfile.class.getDeclaredField("profile");
            profileField.setAccessible(true);

            FieldMetadata profileMeta = new FieldMetadata();
            profileMeta.setName("profile");
            profileMeta.setField(profileField);
            profileMeta.setType(profileField.getGenericType());
            profileMeta.setSupported(true);
            profileMeta.setIgnore(false);
            profileMeta.setLazy(true);

            fields.put("profile", profileMeta);

            metadata.setFields(fields);
            return metadata;
        }

        private ClassMetadata profileMetadata() throws Exception {
            ClassMetadata metadata = new ClassMetadata();
            Map<String, FieldMetadata> fields = new LinkedHashMap<>();

            Field cityField = Profile.class.getDeclaredField("city");
            cityField.setAccessible(true);

            FieldMetadata cityMeta = new FieldMetadata();
            cityMeta.setName("city");
            cityMeta.setField(cityField);
            cityMeta.setType(cityField.getGenericType());
            cityMeta.setSupported(true);
            cityMeta.setIgnore(false);
            cityMeta.setLazy(false);

            fields.put("city", cityMeta);

            Field ageField = Profile.class.getDeclaredField("age");
            ageField.setAccessible(true);

            FieldMetadata ageMeta = new FieldMetadata();
            ageMeta.setName("age");
            ageMeta.setField(ageField);
            ageMeta.setType(ageField.getGenericType());
            ageMeta.setSupported(true);
            ageMeta.setIgnore(false);
            ageMeta.setLazy(false);

            fields.put("age", ageMeta);

            metadata.setFields(fields);
            return metadata;
        }
    }
}